package com.padmajeet.mgi.techforedu.faculty.util;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * Created by padmajeet on 4/10/19.
 */

public class SMS {
    public void sendSms(final String sms, final String number) {
        new AsyncTask<Void,Void,String>(){


            @Override
            protected String doInBackground(Void... params) {
                try {
                    // Construct data
                    String apiKey = "apikey=" + "5acGWHAOHpU-QhLXmuN2czQXRDAbFS3QAzdTkwNJ0x";
                    String message = "&message=" + sms;
                    String sender = "&sender=" + "PADMAJ";
                    String numbers = "&numbers=" + number;

                    // Send data
                    HttpURLConnection conn = (HttpURLConnection) new URL("https://api.textlocal.in/send/?").openConnection();
                    String data = apiKey + numbers + message + sender;
                    conn.setDoOutput(true);
                    conn.setRequestMethod("POST");
                    conn.setRequestProperty("Content-Length", Integer.toString(data.length()));
                    conn.getOutputStream().write(data.getBytes(StandardCharsets.UTF_8));
                    final BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    final StringBuffer stringBuffer = new StringBuffer();
                    String line;
                    while ((line = rd.readLine()) != null) {
                        stringBuffer.append(line);
                    }
                    rd.close();

                    return stringBuffer.toString();
                } catch (Exception e) {
                    System.out.println("Error SMS "+e);
                    return "Error "+e;
                }
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
                System.out.println(s);
            }
        }.execute();

    }
}
