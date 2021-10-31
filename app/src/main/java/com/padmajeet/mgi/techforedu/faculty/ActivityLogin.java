package com.padmajeet.mgi.techforedu.faculty;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.padmajeet.mgi.techforedu.faculty.model.AcademicYear;
import com.padmajeet.mgi.techforedu.faculty.model.Institute;
import com.padmajeet.mgi.techforedu.faculty.model.Staff;
import com.padmajeet.mgi.techforedu.faculty.model.StaffType;
import com.padmajeet.mgi.techforedu.faculty.util.SessionManager;
import com.padmajeet.mgi.techforedu.faculty.util.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import cn.pedant.SweetAlert.SweetAlertDialog;


public class ActivityLogin extends AppCompatActivity {
    private String mobileNumber, password;
    private EditText etMobileNumber, etPassword;
    private Button btnSubmit, btnLogin;
    private LinearLayout llLogin;
    private TextView tvFor;
    private Staff loggedInUser;
    private String loggedInUserId;
    private Gson gson = Utility.getGson();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference staffTypeCollectionRef = db.collection("StaffType");
    private CollectionReference staffCollectionRef = db.collection("Staff");
    private CollectionReference academicYearCollectionRef = db.collection("AcademicYear");
    private CollectionReference instituteCollectionRef = db.collection("Institute");
    private DocumentReference instituteDocRef;
    private Institute institute;
    private StaffType staffType;
    private List<StaffType> staffTypeList = new ArrayList<>();
    private DocumentReference staffDocRef;
    private String academicYearId;
    private AcademicYear academicYear;
    private String staffId;
    private SweetAlertDialog pDialog;
    private SessionManager sessionManager;
    private TextView tvName;
    private ImageView appIcon;
    private int count = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        gson = Utility.getGson();
        sessionManager = new SessionManager(ActivityLogin.this);
        pDialog = Utility.createSweetAlertDialog(ActivityLogin.this);

        staffId = sessionManager.getString("loggedInUserId");

        // System.out.println("adminJson - " + adminId);
        tvName = findViewById(R.id.tvName);
        appIcon = findViewById(R.id.appIcon);
        etMobileNumber = (EditText) findViewById(R.id.etMobileNumber);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
        btnLogin = (Button) findViewById(R.id.btnLogin);

        llLogin = (LinearLayout) findViewById(R.id.llLogin);
        etPassword = (EditText) findViewById(R.id.etPassword);

        tvFor = (TextView) findViewById(R.id.tvFor);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mobileNumber = etMobileNumber.getText().toString().trim();
                if (Utility.isValidPhone(mobileNumber)) {
                    getFaculty();
                } else {
                    etMobileNumber.setError(getString(R.string.errInvalidMobNum));
                    etMobileNumber.requestFocus();
                }
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                password = etPassword.getText().toString().trim();
                if (loggedInUser.getPassword().equals(password)) {
                    getCurrentAcademicYear();
                    gson = Utility.getGson();
                    String loggedInAdminStr = gson.toJson(loggedInUser);
                    sessionManager.putString("loggedInUser", loggedInAdminStr);
                    sessionManager.putString("loggedInUserId", loggedInUserId);
                    sessionManager.putString("instituteId", loggedInUser.getInstituteId());
                    Intent intent = new Intent(ActivityLogin.this, ActivityHome.class);
                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    startActivity(intent);
                    finish();
                } else {
                    etPassword.setError(getString(R.string.errInvalidPassword));
                    etPassword.requestFocus();
                }

            }
        });
        tvFor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sessionManager.putString("loggedInUser", gson.toJson(loggedInUser));
                sessionManager.putString("loggedInUserId", loggedInUserId);
                sessionManager.putString("instituteId", loggedInUser.getInstituteId());
                Intent intent = new Intent(ActivityLogin.this, ActivityForgotPassword.class);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                startActivity(intent);
                finish();
            }
        });

    }

    private void getFaculty() {
        if (pDialog != null && !pDialog.isShowing()) {
            pDialog.show();
        }
        //TODO
        count = 0;
        staffTypeCollectionRef
                .whereEqualTo("isMandatory", true)
                .whereEqualTo("staffCode", "F")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                staffType = document.toObject(StaffType.class);
                                staffType.setId(document.getId());
                                staffTypeList.add(staffType);
                            }
                            System.out.println("staffTypeList -" + staffTypeList.size());
                            if (staffTypeList.size() > 0) {
                                for (StaffType staffType : staffTypeList) {
                                    staffCollectionRef
                                            .whereEqualTo("staffTypeId", staffType.getId())
                                            .whereEqualTo("mobileNumber", mobileNumber)
                                            .whereIn("status", Arrays.asList("A", "F"))
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    count++;
                                                    System.out.println("task -" + task.getResult().size());
                                                    if (task.isSuccessful()) {
                                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                                            // System.out.println("Data -key -" + document.getId() + " value -" + document.getData());
                                                            loggedInUserId = document.getId();
                                                            loggedInUser = document.toObject(Staff.class);
                                                            loggedInUser.setId(document.getId());
                                                            // System.out.println("Data -key -" + document.getId() + " value -" + loggedInUser);
                                                            // System.out.println("loggedInUser -" + loggedInUser.getFirstName());
                                                        }
                                                        if (count == staffTypeList.size()) {
                                                            if (pDialog != null && pDialog.isShowing()) {
                                                                pDialog.dismiss();
                                                            }
                                                            if (loggedInUser == null) {
                                                                etMobileNumber.setError(getString(R.string.errInvalidMobNum));
                                                                etMobileNumber.requestFocus();
                                                                return;
                                                            } else {
                                                                tvName.setText("Hi " + loggedInUser.getFirstName() + "!");
                                                                showSchoolLogo();
                                                                if (loggedInUser.getStatus().equals("F")) {
                                                                    sessionManager.putString("loggedInUser", gson.toJson(loggedInUser));
                                                                    sessionManager.putString("loggedInUserId", loggedInUserId);
                                                                    sessionManager.putString("instituteId", loggedInUser.getInstituteId());
                                                                    Intent intent = new Intent(ActivityLogin.this, ActivityForgotPassword.class);
                                                                    overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                                                                    startActivity(intent);
                                                                    finish();
                                                                } else if (!loggedInUser.getStatus().equals("A")) {
                                                                    etMobileNumber.setError("Admin has deactivated your account");
                                                                    etMobileNumber.requestFocus();
                                                                    return;
                                                                } else {
                                                                    //Toast.makeText(getApplicationContext(), "Please login", Toast.LENGTH_SHORT).show();
                                                                    btnSubmit.setVisibility(View.GONE);
                                                                    llLogin.setVisibility(View.VISIBLE);
                                                                }

                                                            }
                                                        }
                                                    } else {
                                                        // Log.d(TAG, "Error getting documents: ", task.getException());
                                                        //System.out.println("Error getting documents: -" + task.getException());
                                                    }
                                                }
                                            });
                                }
                            } else {
                                System.out.println("Staff type collection is empty");
                            }
                        } else {
                            System.out.println("Staff type collection is empty");
                        }
                    }
                });

        // [END get_multiple]

    }

    private void showSchoolLogo() {
        instituteDocRef = instituteCollectionRef.document("/" + loggedInUser.getInstituteId());
        instituteDocRef
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        institute = documentSnapshot.toObject(Institute.class);
                        String imageUrl = institute.getLogoImagePath();
                        if (!TextUtils.isEmpty(imageUrl)) {
                            Glide.with(getApplicationContext())
                                    .load(imageUrl)
                                    .fitCenter()
                                    .placeholder(R.drawable.kiddie_logo)
                                    .into(appIcon);
                        }
                    }
                });
    }

    private void getCurrentAcademicYear() {
        academicYearCollectionRef
                .whereEqualTo("status", "A")
                .whereEqualTo("instituteId", loggedInUser.getInstituteId())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                academicYearId = document.getId();
                                academicYear = document.toObject(AcademicYear.class);
                                academicYear.setId(document.getId());
                                // System.out.println("academicYear "+academicYear.getYear());
                            }
                            if (!TextUtils.isEmpty(gson.toJson(academicYear))) {
                                sessionManager.putString("academicYear", gson.toJson(academicYear));
                                sessionManager.putString("academicYearId", academicYearId);
                            }
                        } else {
                            // Log.d(TAG, "Error getting documents: ", task.getException());
                            //System.out.println("Error getting documents: -" + task.getException());
                        }
                    }
                });
    }
}