package com.uni.dpriho200.mobdev4;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
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
        CookieHandler.setDefault(cookieManager);
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL url = new URL("https://celcat.gcu.ac.uk/calendar/Login.aspx");
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    try {
                        InputStream in = new BufferedInputStream(conn.getInputStream());

                    } finally {
                        conn.disconnect();
                    }
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
                    payload += "&txtUserName=" + userField.getText();
                    payload += "&txtUserPass=" + paswField.getText();
                    payload += "&btnLogin=+++Log+In+++";

                    //payload formed, sending
                    URL url = new URL("https://celcat.gcu.ac.uk/calendar/Login.aspx");
                    HttpURLConnection conn = (HttpURLConnection)url.openConnection();
                    try {
                        conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                        conn.setDoOutput(true);
                        conn.setFixedLengthStreamingMode(payload.length());

                        OutputStream out = new BufferedOutputStream(conn.getOutputStream());

                    } finally {
                        conn.disconnect();
                    }
                } catch(Exception e) {
                    Log.e("CW", e.toString());
                }
            }
        });
    }

    static String readStream(BufferedInputStream stream)
    {
        return "";
    }
}
