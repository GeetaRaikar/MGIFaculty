package com.padmajeet.mgi.techforedu.faculty.util;

import android.content.Context;
import android.os.AsyncTask;

import com.padmajeet.mgi.techforedu.faculty.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

import javax.net.ssl.HttpsURLConnection;


public class HttpManager {
    private static Context context;
    public HttpManager(){

    }

    public HttpManager(Context context){
        HttpManager.context = context;
    }

    public static String getData(String uri) {
        System.out.println("\n GET URL - "+uri);
        BufferedReader reader = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(uri);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setUseCaches(false);
            connection.setConnectTimeout(60000);
            connection.setReadTimeout(60000);
            connection.connect();

            int responseCode=connection.getResponseCode();

            if (responseCode == HttpsURLConnection.HTTP_OK) {
                StringBuilder stringBuilder = new StringBuilder();
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));

                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }
                return stringBuilder.toString();
            }
            else{
                return null;
            }
        }
        catch (MalformedURLException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        finally {
            if(connection!=null){
                connection.disconnect();
            }
            if(reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static String postData(String uri, String jsonData){
        System.out.println("\nPOST URL - "+uri);
        URL url = null;
        HttpURLConnection connection = null;
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            url = new URL(uri);

            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setConnectTimeout(60000);
            connection.setReadTimeout(60000);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            connection.setFixedLengthStreamingMode(jsonData.getBytes().length);

            //make some HTTP header nicety
            connection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
            connection.setRequestProperty("X-Requested-With", "XMLHttpRequest");

            //open
            connection.connect();

            OutputStream os = connection.getOutputStream();
            writer = new BufferedWriter(new OutputStreamWriter(os, StandardCharsets.UTF_8));
            writer.write(jsonData);
            writer.flush();

            os.close();

            System.out.println("POST json Data- "+jsonData);
            int responseCode=connection.getResponseCode();
            System.out.println("Response Code in POST - "+responseCode);
            if (responseCode == HttpsURLConnection.HTTP_OK) {

                reader=new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuffer sb = new StringBuffer();
                String line="";
                while((line = reader.readLine()) != null) {
                    sb.append(line);
                    break;
                }

                return sb.toString();

            }
            else {
                return null;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            if(connection!=null){
                connection.disconnect();
            }
            if(reader != null){
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(writer!=null){
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    public static void sendSMS(String mobileNum,String message){

        StringBuffer url = new StringBuffer(context.getResources().getString(R.string.smsServiceUrl));
        String encoded_message=URLEncoder.encode(message);
        String route="6";
        String senderId = context.getResources().getString(R.string.smsServiceSenderId);
        String authkey = context.getResources().getString(R.string.smsServiceApiKey);

        url.append("authkey="+authkey);
        url.append("&mobiles="+mobileNum);
        url.append("&message="+encoded_message);
        url.append("&route="+route);
        url.append("&sender="+senderId);

        new AsyncTask<String,Void,String>(){

            @Override
            protected String doInBackground(String... params) {
                String url = params[0];
                URL myURL = null;
                HttpURLConnection connection = null;
                BufferedReader reader=null;
                try
                {
                    //prepare connection
                    myURL = new URL(url);
                    connection = (HttpURLConnection)myURL.openConnection();
                    connection.connect();
                    reader= new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    int responseCode=connection.getResponseCode();
                    System.out.println("Response Code in POST - "+responseCode);
                    if (responseCode == HttpsURLConnection.HTTP_OK) {

                        reader=new BufferedReader(new InputStreamReader(connection.getInputStream()));
                        StringBuffer sb = new StringBuffer();
                        String line="";
                        while((line = reader.readLine()) != null) {
                            sb.append(line);
                            break;
                        }
                        return sb.toString();
                    }
                    else {
                        return null;
                    }
                }
                catch (MalformedURLException e) {
                    e.printStackTrace();
                    return null;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
                finally {
                    if(connection!=null){
                        connection.disconnect();
                    }
                    if(reader != null){
                        try {
                            reader.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
            @Override
            protected void onPostExecute(String result) {
                System.out.println("SendSMS result - "+result);
            }
        }.execute(url.toString());
    }


    public static String getPostDataString(JSONObject params) throws Exception {

        StringBuilder result = new StringBuilder();
        result.append("{");
        boolean first = true;
        Iterator<String> itr = params.keys();
        while(itr.hasNext()){
            String key= itr.next();
            Object value = params.get(key);

            if (first) {
                first = false;
            }
            else {
                result.append(",");
            }
            result.append(URLEncoder.encode(key, "UTF-8"));
            result.append(":");
            result.append(URLEncoder.encode(value.toString(), "UTF-8"));
        }
        result.append("}");
        return result.toString();
    }
}
