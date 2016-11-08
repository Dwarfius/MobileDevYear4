package com.uni.dpriho200.mobdev4;

import android.provider.DocumentsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class LogIn extends AppCompatActivity
{
    static String viewState, eventValidation;
    static boolean cookiesReady = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        final EditText userField = (EditText)findViewById(R.id.userField);
        final EditText paswField = (EditText)findViewById(R.id.paswField);

        //tracking the cookies - we need them for ASP.NET session tracking
        CookieManager cookieManager = new CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Document doc = Jsoup.connect("https://celcat.gcu.ac.uk/calendar/Login.aspx").get();
                    viewState = doc.select("#__VIEWSTATE").first().val();
                    eventValidation = doc.select("#__EVENTVALIDATION").first().val();
                    cookiesReady = true;
                    //Log.i("CW", "ViewState=" + viewState + " | EventValidation" + eventValidation);
                    //HttpCookie cookie = ((CookieManager)CookieManager.getDefault()).getCookieStore().getCookies().get(0);
                    //Log.i("CW", cookie.getName() + ": " + cookie.getValue());
                } catch(Exception e) {
                    Log.e("CW", e.toString());
                }
            }
        });
        t.start();

        Button btn = (Button)findViewById(R.id.logInBtn);
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
                            payload += "__VIEWSTATE=" + viewState;
                            payload += "&__EVENTVALIDATION=" + eventValidation;
                            payload += "&txtUserName=dpriho200";// + userField.getText();
                            payload += "&txtUserPass=Ffzkdpcu1";// + paswField.getText();
                            payload += "&btnLogin=+++Log+In+++";

                            //payload formed, sending
                            URL url = new URL("https://celcat.gcu.ac.uk/calendar/Login.aspx");
                            HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                            try {
                                HttpCookie cookie = ((CookieManager)CookieManager.getDefault()).getCookieStore().getCookies().get(0);
                                String cookieStr = cookie.getName() + "=" + cookie.getValue();

                                conn.setRequestMethod("POST");
                                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                                conn.setRequestProperty("Cookie", cookieStr);
                                conn.setDoOutput(true);
                                conn.setFixedLengthStreamingMode(payload.length());

                                OutputStream out = new BufferedOutputStream(conn.getOutputStream());
                                out.write(payload.getBytes("UTF-8"));
                                out.flush();
                                out.close();
                                conn.connect();
                                int responseCode=conn.getResponseCode();
                                String response = "";
                                if (responseCode == HttpsURLConnection.HTTP_OK) {
                                    String line;
                                    BufferedReader br=new BufferedReader(new InputStreamReader(conn.getInputStream()));
                                    while ((line=br.readLine()) != null)
                                        response+=line;
                                }
                                Log.i("CW", "Page: (" + responseCode + ") " + response);
                            } finally {
                                conn.disconnect();
                            }
                        } catch(Exception e) {
                            Log.e("CW", e.toString());
                        }
                    }
                });
                t.start();
            }
        });
    }

    static String readStream(BufferedInputStream stream) {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        String result = "";
        try {
            while ((length = stream.read(buffer)) != -1) {
                output.write(buffer, 0, length);
            }
            result = output.toString("UTF-8");
        } catch(Exception e) {
            Log.e("CW", e.toString());
        }
        return result;
    }
}
