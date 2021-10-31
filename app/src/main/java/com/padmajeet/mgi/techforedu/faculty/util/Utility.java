package com.padmajeet.mgi.techforedu.faculty.util;


import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.padmajeet.mgi.techforedu.faculty.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class Utility {
    public static void unCheckAllMenuItems(@NonNull final Menu menu) {
        int size = menu.size();
        for (int i = 0; i < size; i++) {
            final MenuItem item = menu.getItem(i);
            if(item.hasSubMenu()) {
                // Un check sub menu items
                unCheckAllMenuItems(item.getSubMenu());
            } else {
                item.setChecked(false);
            }
        }
    }
    public static Date getDateOnly(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    public static String formatDateToString(long datetime){
        String dateString = null;
        Date date=new Date(datetime);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        dateString = dateFormat.format(date);
        return dateString;
    }
    public static String formatDateToStringForStudentFee(long datetime){
        String dateString = null;
        Date date=new Date(datetime);
        SimpleDateFormat dateFormat = new SimpleDateFormat("E,MMM dd yyyy");
        dateString = dateFormat.format(date);
        return dateString;
    }
    public static String formatDateTimeToString(long datetime){
        String dateString = null;
        Date date=new Date(datetime);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm a");
        dateString = dateFormat.format(date);
        return dateString;
    }
    public static boolean isEmailValid(String email) {
        String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"+"[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
        Pattern pattern = Pattern.compile(EMAIL_PATTERN);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }
    //TODO
    public static boolean isDateValid(String date) {
        String Date_PATTERN = "^(((0[1-9]|[12]\\d|3[01])\\/(0[13578]|1[02])\\/((19|[2-9]\\d)\\d{2}))|((0[1-9]|[12]\\d|30)\\/(0[13456789]|1[012])\\/((19|[2-9]\\d)\\d{2}))|((0[1-9]|1\\d|2[0-8])\\/02\\/((19|[2-9]\\d)\\d{2}))|(29\\/02\\/((1[6-9]|[2-9]\\d)(0[48]|[2468][048]|[13579][26])|((16|[2468][048]|[3579][26])00))))$";
        Pattern pattern = Pattern.compile(Date_PATTERN);
        Matcher matcher = pattern.matcher(date);
        return matcher.matches();
    }
    public static boolean isTimeValid(String time) {
        String Time_PATTERN = "((1[0-2]|0?[1-9]):([0-5][0-9]) ?([AaPp][Mm]))";
        Pattern pattern = Pattern.compile(Time_PATTERN);
        Matcher matcher = pattern.matcher(time);
        return matcher.matches();
    }
    public static boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        Log.d("password - ",password);
        return password.length() >= 3;
    }

    public static boolean isValidPhone(String phone) {
        String PHONE_PATTERN = "[0-9]{10}";
        Pattern pattern = Pattern.compile(PHONE_PATTERN);
        Matcher matcher = pattern.matcher(phone);
        return matcher.matches();
    }

    public static boolean isJsonStringEmpty(String jsonString){
        return jsonString == null || jsonString.equals("") || jsonString.equals("[]");
    }


    public static String generateOTP(){
        String otp = ""+((int)(Math.random()*9000)+1000);
        return otp;
    }

    public static void hideKeyboard(Context context,boolean hide) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        if(hide){
            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
        }
        else{
            imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT, 0);
        }
    }

    public static void isActiveNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if(networkInfo!=null && networkInfo.isAvailable()){

        }
        else {
            Toast.makeText(context, "You are NOT connected to internet. Please connect to internet to work with Logistica App", Toast.LENGTH_LONG).show();
        }
    }

    public static String generateTransactionId(String mobileNumber){
        String transactionIdString = "L"+mobileNumber+generateOTP();
        return transactionIdString;
    }

    public static String generateReferalCode(String mobileNumber){
        String transactionIdString = "L"+mobileNumber+generateOTP();
        return transactionIdString;
    }

    public static SweetAlertDialog createSweetAlertDialog(Context context){
        SweetAlertDialog pDialog = new SweetAlertDialog(context, SweetAlertDialog.PROGRESS_TYPE);
        pDialog.getProgressHelper().setBarColor(context.getResources().getColor(R.color.colorPrimary));
        pDialog.setTitleText("Loading");
        pDialog.setCancelable(false);
        return pDialog;
    }

    public static Gson getGson(){
        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'hh:mm:ss").create();
        return gson;
    }

}