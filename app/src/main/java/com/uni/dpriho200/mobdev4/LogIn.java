package com.uni.dpriho200.mobdev4;

import android.content.Intent;
import android.os.Bundle;
import android.renderscript.ScriptGroup;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

public class LogIn extends AppCompatActivity
{
    private String viewState, eventValidation;
    private boolean cookiesReady = false;

    private EditText userField, paswField; //yes, it's paSSword, but shorted for a better layout
    private CheckBox rememberChkBox;

    static final String host = "https://celcat.gcu.ac.uk/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        NotesDB.init(this);

        userField = (EditText)findViewById(R.id.userField);
        paswField = (EditText)findViewById(R.id.paswField);
        rememberChkBox = (CheckBox)findViewById(R.id.rememberChkBox);
        final File saveFile = new File(getCacheDir(), "userInfo");
        rememberChkBox.setChecked(saveFile.exists());
        final Button btn = (Button)findViewById(R.id.logInBtn);

        //tracking the cookies - we need them for ASP.NET session tracking
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread t = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            //safety check
                            if(!cookiesReady) {
                                Log.w("CW", "Didn't finish receiving log in data, returning early.");
                                return;
                            }

                            //first, forming the post data
                            String payload = "";
                            payload += "__VIEWSTATE=" + URLEncoder.encode(viewState, "UTF-8");
                            payload += "&__EVENTVALIDATION=" + URLEncoder.encode(eventValidation, "UTF-8");
                            payload += "&txtUserName=" + userField.getText();
                            payload += "&txtUserPass=" + paswField.getText();
                            payload += "&btnLogin=+++Log+In+++";
                            Log.v("CW", "Logging in with: " + payload);

                            //payload formed, sending
                            URL url = new URL("https://celcat.gcu.ac.uk/calendar/Login.aspx");
                            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                            try {
                                //logging in
                                conn.setRequestMethod("POST");
                                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                                conn.setRequestProperty("Cookie", serializeAllCookies());
                                conn.setDoOutput(true);
                                conn.setFixedLengthStreamingMode(payload.length());
                                conn.setInstanceFollowRedirects(false);

                                OutputStream out = new BufferedOutputStream(conn.getOutputStream());
                                out.write(payload.getBytes("UTF-8"));
                                out.flush();
                                out.close();
                                conn.connect();

                                int responseCode = conn.getResponseCode();
                                Log.i("CW", "Login: " + responseCode);
                                //if it was successful, we should receive an auth cookie and be redirected
                                if(responseCode == HttpURLConnection.HTTP_MOVED_TEMP) {
                                    conn.disconnect(); //being polite to the OS, cleaning up after ourselves

                                    // Now that we've logged in - following the redirect to the calendar page
                                    String redirLoc = conn.getHeaderField("location");
                                    conn = (HttpURLConnection)new URL(host + redirLoc).openConnection();
                                    conn.setRequestProperty("Cookie", serializeAllCookies()); //cookie was auto-added to the CookieStorage, so just reserealize all of them
                                    responseCode = conn.getResponseCode();

                                    //now that we're at the right place, parse out the calendar data
                                    String response;
                                    if (responseCode == HttpsURLConnection.HTTP_OK) {
                                        response = readStream(new BufferedInputStream(conn.getInputStream()));
                                        ArrayList<UniClass> classes = parseCalendar(response);
                                        if(classes != null) {
                                            // we successfully managed to get the information we looked for
                                            // save the user info if he requested it
                                            if(rememberChkBox.isChecked())
                                                saveUserInfo(saveFile);
                                            else
                                                saveFile.delete();

                                            // starting the new activity to display all we gathered
                                            Intent intent = new Intent(LogIn.this, CalendarView.class);
                                            intent.putParcelableArrayListExtra("Classes", classes);
                                            intent.putExtra("User", userField.getText().toString().toLowerCase());
                                            startActivity(intent);
                                        }
                                    }
                                }
                            } finally {
                                conn.disconnect();
                            }
                        } catch(Exception e) { Log.e("CW", e.toString(), e); }
                    }
                });
                t.start();
            }
        });

        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Document doc = Jsoup.connect(host + "calendar/Login.aspx").get();
                    viewState = doc.select("#__VIEWSTATE").first().val();
                    eventValidation = doc.select("#__EVENTVALIDATION").first().val();
                    cookiesReady = true;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(rememberChkBox.isChecked()) {
                                try {
                                    InputStream is = new FileInputStream(saveFile);
                                    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                                    userField.setText(reader.readLine());
                                    paswField.setText(reader.readLine());
                                    btn.callOnClick();
                                } catch(Exception e) { Log.e("CW", e.toString(), e); }
                            }
                        }
                    });
                } catch(Exception e) { Log.e("CW", e.toString(), e); }
            }
        });
        t.start();
    }

    @Override
    protected void onResume() {
        userField.setText("");
        paswField.setText("");
        super.onResume();
    }

    private ArrayList<UniClass> parseCalendar(String html)
    {
        Log.v("CW", "Raw login response: " + html);
        //since it's not part of the HTML's body, we have to manually parse it out
        String startMarker = "v.events.list = ";
        int start = html.indexOf(startMarker);
        int end = html.indexOf("v.links.list = [];");
        String data = html.substring(start + startMarker.length(), end - 3); //ends with "; \n", getting rid of it
        Log.v("CW", "Parsed out JSON: " + data);
        try {
            JSONArray array = new JSONArray(data);
            ArrayList<UniClass> classes = new ArrayList<>();
            for(int i=0; i<array.length(); i++) {
                UniClass uniClass = new UniClass((JSONObject)array.get(i));
                uniClass.setNotesCount(NotesDB.count(uniClass.getId(), userField.getText().toString()));
                classes.add(uniClass);
            }
            return classes;
        } catch(Exception e) { Log.e("CW", e.toString(), e); }
        return null;
    }

    private String readStream(BufferedInputStream stream) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        String result = "";
        try {
            while ((length = stream.read(buffer)) != -1)
                output.write(buffer, 0, length);
            result = output.toString("UTF-8");
        } catch(Exception e) { Log.e("CW", e.toString(), e); }
        return result;
    }

    private String serializeAllCookies()
    {
        List<HttpCookie> cookies = ((CookieManager)CookieManager.getDefault()).getCookieStore().getCookies();
        String res = "";
        for (HttpCookie cookie : cookies)
            res += cookie.getName() + "=" + cookie.getValue() + ";";
        res = res.substring(0, res.length() - 1); //getting rid of last extra ;
        return res;
    }

    private void saveUserInfo(File saveFile)
    {
        try {
            OutputStream os = new FileOutputStream(saveFile);
            OutputStreamWriter writer = new OutputStreamWriter(os);
            writer.write(userField.getText() + "\n" + paswField.getText());
            writer.flush();
            writer.close();
        } catch (Exception e) { Log.e("CW", e.toString()); }
    }
}
