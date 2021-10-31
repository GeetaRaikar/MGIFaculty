package com.padmajeet.mgi.techforedu.faculty;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.padmajeet.mgi.techforedu.faculty.model.BatchSubjectFaculty;
import com.padmajeet.mgi.techforedu.faculty.model.Staff;
import com.padmajeet.mgi.techforedu.faculty.util.SessionManager;
import com.padmajeet.mgi.techforedu.faculty.util.Utility;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;
import cn.pedant.SweetAlert.SweetAlertDialog;


public class ActivityHome extends AppCompatActivity {

    private Gson gson;
    private Staff loggedInUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private DrawerLayout drawer;
    private SessionManager sessionManager;
    private SweetAlertDialog dialog;
    private String loggedInUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        final NavigationView navigationView = findViewById(R.id.nav_view);

        sessionManager = new SessionManager(getApplicationContext());
        sessionManager.putString("isFirstLoadingHomePage","Yes");
        String userJson = sessionManager.getString("loggedInUser");
        loggedInUserId=sessionManager.getString("loggedInUserId");
        gson = Utility.getGson();
        loggedInUser = gson.fromJson(userJson, Staff.class);
        View header = navigationView.getHeaderView(0);
        TextView tv_nav_name = (TextView) header.findViewById(R.id.tv_nav_name);
        TextView tv_nav_mobnum = (TextView) header.findViewById(R.id.tv_nav_mobnum);
        ImageView ivProfilePic = header.findViewById(R.id.ivProfilePic);
        if (loggedInUser != null) {
            String firstName = loggedInUser.getFirstName();
            String lastName = loggedInUser.getLastName();
            String name = "";
            if (!TextUtils.isEmpty(firstName)) {
                name = firstName;
            }
            if (!TextUtils.isEmpty(lastName)) {
                name = name + " " + lastName;
            }
            tv_nav_name.setText(name);
            tv_nav_mobnum.setText("" + loggedInUser.getMobileNumber());
        }

        getBatchFaculty();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, drawer);
        NavigationUI.setupWithNavController(navigationView, navController);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                int id = menuItem.getItemId();
                Menu menuNav = navigationView.getMenu();
                navigationView.setCheckedItem(id);
                switch (id) {
                    case R.id.nav_home:
                        menuNav.findItem(R.id.nav_home).setChecked(true);
                        replaceFragment(new FragmentHome(), getString(R.string.home));
                        break;
                    case R.id.nav_take_attendance:
                        menuNav.findItem(R.id.nav_take_attendance).setChecked(true);
                        replaceFragment(new FragmentTakeAttendance(), getString(R.string.takeAttendance));
                        break;
                    case R.id.nav_attendance:
                        menuNav.findItem(R.id.nav_attendance).setChecked(true);
                        replaceFragment(new FragmentAttendance(), getString(R.string.studentAttendance));
                        break;
                    case R.id.nav_colleague:
                        menuNav.findItem(R.id.nav_colleague).setChecked(true);
                        replaceFragment(new FragmentColleague(), getString(R.string.colleagues));
                        break;
                    case R.id.nav_event:
                        menuNav.findItem(R.id.nav_event).setChecked(true);
                        replaceFragment(new FragmentEvent(), getString(R.string.event));
                        break;
                    case R.id.nav_calender:
                        menuNav.findItem(R.id.nav_calender).setChecked(true);
                        replaceFragment(new FragmentCalendar(), getString(R.string.calendar));
                        break;
                    case R.id.nav_subject:
                        menuNav.findItem(R.id.nav_subject).setChecked(true);
                        replaceFragment(new FragmentSubject(), getString(R.string.subject));
                        break;
                    case R.id.nav_home_work:
                        menuNav.findItem(R.id.nav_home_work).setChecked(true);
                        replaceFragment(new FragmentHomeWork(), getString(R.string.assignment));
                        break;
                    case R.id.nav_message:
                        menuNav.findItem(R.id.nav_message).setChecked(true);
                        replaceFragment(new FragmentMessage(), getString(R.string.message));
                        break;
                    case R.id.nav_my_schedule:
                        menuNav.findItem(R.id.nav_my_schedule).setChecked(true);
                        replaceFragment(new FragmentMySchedule(), getString(R.string.mySchedule));
                        break;
                    case R.id.nav_holiday:
                        menuNav.findItem(R.id.nav_holiday).setChecked(true);
                        replaceFragment(new FragmentHoliday(), getString(R.string.holiday));
                        break;
                    case R.id.nav_timetable:
                        menuNav.findItem(R.id.nav_timetable).setChecked(true);
                        replaceFragment(new FragmentTimeTable(), getString(R.string.timeTable));
                        break;
                    case R.id.nav_exam_schedule:
                        menuNav.findItem(R.id.nav_exam_schedule).setChecked(true);
                        replaceFragment(new FragmentExamSeries(), getString(R.string.examSchedule));
                        break;
                    case R.id.nav_score_card:
                        menuNav.findItem(R.id.nav_score_card).setChecked(true);
                        replaceFragment(new FragmentExamSeriesScore(), getString(R.string.scoreCard));
                        break;
                    case R.id.nav_aboutus:
                        menuNav.findItem(R.id.nav_aboutus).setChecked(true);
                        replaceFragment(new FragmentAboutUs(), getString(R.string.aboutUs));
                        break;
                    case R.id.nav_appinfo:
                        menuNav.findItem(R.id.nav_appinfo).setChecked(true);
                        replaceFragment(new FragmentAppInfo(), getString(R.string.appinfo));
                        break;
                    case R.id.nav_support:
                        menuNav.findItem(R.id.nav_support).setChecked(true);
                        replaceFragment(new FragmentSupport(), getString(R.string.support));
                        break;
                    case R.id.nav_logout:
                        dialog = new SweetAlertDialog(ActivityHome.this, SweetAlertDialog.WARNING_TYPE)
                                .setTitleText("Logout?")
                                .setContentText("Do you really want to logout from the App? ")
                                .setConfirmText("OK")
                                .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();

                                    }
                                })
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();

                                        SessionManager session = new SessionManager(getApplication());
                                        session.remove("loggedInUser");
                                        session.remove("loggedInUserId");
                                        session.remove("academicYear");
                                        session.remove("academicYearId");
                                        session.remove("instituteId");
                                        //session.clear();
                                        Intent intent = new Intent(getApplicationContext(), ActivityLogin.class);
                                        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                                        startActivity(intent);
                                        finish();
                                    }
                                });
                        dialog.setCancelable(false);
                        dialog.show();
                        break;
                }
                drawer.closeDrawer(GravityCompat.START);
                return false;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            getSupportActionBar().setTitle(R.string.profile);
            FragmentProfile fragmentProfile = new FragmentProfile();
            replaceFragment(fragmentProfile, "FRAGMENT_PROFILE");

        }

        return super.onOptionsItemSelected(item);
    }

    private void replaceFragment(Fragment fragment, String tag) {
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        fragmentTransaction.replace(R.id.contentLayout, fragment, tag);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }


    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, drawer)
                || super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(sessionManager.getString("isFirstLoadingHomePage").equalsIgnoreCase("Yes")){
            sessionManager.putString("isFirstLoadingHomePage","No");
            System.out.println(sessionManager.getString("isFirstLoadingHomePage"));
            return;
        }else {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                System.out.println("Back Pressed");
                SweetAlertDialog dialog = new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Exit App")
                        .setContentText("Do you really want to exit the App? ")
                        .setConfirmText("Ok")
                        .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                                FragmentManager manager = getSupportFragmentManager();
                                FragmentTransaction fragmentTransaction = manager.beginTransaction();
                                fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                                fragmentTransaction.replace(R.id.contentLayout, new FragmentHome()).addToBackStack(null).commit();
                            }
                        })
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                                finish();
                            }
                        });
                dialog.setCancelable(false);
                dialog.show();
            }
        }
    }

    private void getBatchFaculty(){
        List<BatchSubjectFaculty> batchSubjectFacultyList=new ArrayList<>();
        List<String> batchIdList=new ArrayList<>();
        List<String> subjectIdList=new ArrayList<>();
        System.out.println("loggedInUser.getId() "+loggedInUserId);
        db.collection("BatchSubjectFaculty")
                .whereEqualTo("staffId", loggedInUserId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            BatchSubjectFaculty batchSubjectFaculty = documentSnapshot.toObject(BatchSubjectFaculty.class);
                            batchSubjectFaculty.setId(documentSnapshot.getId());
                            batchSubjectFacultyList.add(batchSubjectFaculty);
                            batchIdList.add(batchSubjectFaculty.getBatchId());
                            subjectIdList.add(batchSubjectFaculty.getSubjectId());
                        }
                        System.out.println("batchFacultyList "+batchSubjectFacultyList.size());
                        sessionManager.putString("batchIdList", gson.toJson(batchIdList));
                        sessionManager.putString("subjectIdList", gson.toJson(subjectIdList));
                        System.out.println("batchIdList "+batchIdList.size());
                        System.out.println("subjectIdList "+subjectIdList.size());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }
}
