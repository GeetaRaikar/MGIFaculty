package com.padmajeet.mgi.techforedu.faculty;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.padmajeet.mgi.techforedu.faculty.model.Staff;
import com.padmajeet.mgi.techforedu.faculty.util.SessionManager;
import com.padmajeet.mgi.techforedu.faculty.util.Utility;
import java.util.Date;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentProfile extends Fragment {

    private View view;
    private Staff loggedInUser;
    private EditText etMobileNumber, etEmail, etAddress,etFirstName,etLastName;
    private ImageView ivProfilePic;
    private Button btUpdateProfile;
    private boolean isEmailEdited, isFirstNameEdited, isLastNameEdited, isAddressEdited;
    private Fragment currentFragment;
    private Gson gson;
    private String loggedInUserId;
    private TextView tvResetPassword;
    private TextView tvStaffName;
    private ImageView ivProfile;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference staffCollectionRef = db.collection("Staff");

    public FragmentProfile() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(getContext());
        gson = Utility.getGson();
        String staffJson = sessionManager.getString("loggedInUser");
        loggedInUser = gson.fromJson(staffJson, Staff.class);
        loggedInUserId = sessionManager.getString("loggedInUserId");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.profile));

        currentFragment = this;
        isEmailEdited = false;
        isFirstNameEdited = false;
        isLastNameEdited = false;
        isAddressEdited = false;
        btUpdateProfile = view.findViewById(R.id.btUpdateProfile);
        btUpdateProfile.setVisibility(View.INVISIBLE);
        ivProfilePic = view.findViewById(R.id.ivProfilePic);
        tvStaffName = view.findViewById(R.id.tvStaffName);
        etMobileNumber = view.findViewById(R.id.etMobileNumber);
        etFirstName = view.findViewById(R.id.etFirstName);
        etLastName = view.findViewById(R.id.etLastName);
        etEmail = view.findViewById(R.id.etEmail);
        etAddress = view.findViewById(R.id.etAddress);

        if(loggedInUser != null) {
            String imageUrl = loggedInUser.getImageUrl();
            System.out.println("imageUrl " + imageUrl);
            Glide.with(this)
                    .load(imageUrl)
                    .fitCenter()
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(R.drawable.ic_profile_large)
                    .into(ivProfilePic);


            tvStaffName.setText("" + loggedInUser.getFirstName() + " " + loggedInUser.getLastName());

            etMobileNumber.setText(loggedInUser.getMobileNumber());

            if (TextUtils.isEmpty(loggedInUser.getFirstName())) {
                String firstNameUnavailable = getString(R.string.unavailable);
                etFirstName.setHint(firstNameUnavailable);
            } else {
                etFirstName.setText(loggedInUser.getFirstName());
            }

            if (TextUtils.isEmpty(loggedInUser.getLastName())) {
                String lastNameUnavailable = getString(R.string.unavailable);
                etLastName.setHint(lastNameUnavailable);
            } else {
                etLastName.setText(loggedInUser.getLastName());
            }

            if (TextUtils.isEmpty(loggedInUser.getEmailId())) {
                String emailIdUnavailable = getString(R.string.unavailable);
                etEmail.setHint(emailIdUnavailable);
            } else {
                etEmail.setText(loggedInUser.getEmailId());
            }

            if (!TextUtils.isEmpty(loggedInUser.getAddress())) {
                etAddress.setText(loggedInUser.getAddress());
            }

        }

        ImageView ivEditFirstName = view.findViewById(R.id.ivEditFirstName);
        ivEditFirstName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isFirstNameEdited = true;
                btUpdateProfile.setVisibility(View.VISIBLE);
                etFirstName.setEnabled(true);
                etFirstName.requestFocus();
            }
        });

        ImageView ivEditLastName = view.findViewById(R.id.ivEditLastName);
        ivEditLastName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isLastNameEdited = true;
                btUpdateProfile.setVisibility(View.VISIBLE);
                etLastName.setEnabled(true);
                etLastName.requestFocus();
            }
        });

        ImageView ivEditEmail = view.findViewById(R.id.ivEditEmail);
        ivEditEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isEmailEdited = true;
                btUpdateProfile.setVisibility(View.VISIBLE);
                etEmail.setEnabled(true);
                etEmail.requestFocus();
            }
        });

        ImageView ivEditAddress = view.findViewById(R.id.ivEditAddress);
        ivEditAddress.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isAddressEdited = true;
                btUpdateProfile.setVisibility(View.VISIBLE);
                etAddress.setEnabled(true);
                etAddress.requestFocus();
            }
        });
        tvResetPassword = view.findViewById(R.id.tvResetPassword);
        tvResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Reset Password")
                        .setContentText("After password reset you need to login again. Do you want to proceed?")
                        .setConfirmText("Proceed")
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
                                Intent intent = new Intent(getActivity(), ActivityForgotPassword.class);
                                getActivity().overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                                startActivity(intent);
                            }
                        });
                dialog.setCancelable(false);
                dialog.show();
            }
        });

        btUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(loggedInUser != null) {
                    updateUserProfile();
                }
            }
        });

        return view;
    }

    void updateUserProfile() {
        boolean canSave = true;
        if (isFirstNameEdited) {
            String updatedFirstName = etFirstName.getText().toString().trim();
            if (TextUtils.isEmpty(updatedFirstName)) {
                etFirstName.setError(getString(R.string.errInvalidFirstName));
                canSave = false;
            } else {
                loggedInUser.setFirstName(updatedFirstName);
            }
        }
        if (isLastNameEdited) {
            String updatedLastName = etLastName.getText().toString().trim();
            if (TextUtils.isEmpty(updatedLastName)) {
                etLastName.setError(getString(R.string.errInvalidLastName));
                canSave = false;
            } else {
                loggedInUser.setLastName(updatedLastName);
            }
        }
        if (isEmailEdited) {
            String updatedEmail = etEmail.getText().toString().trim();
            if (!Utility.isEmailValid(updatedEmail)) {
                etEmail.setError(getString(R.string.errInvalidEmail));
                canSave = false;
            } else {
                loggedInUser.setEmailId(updatedEmail);
            }
        }
        if (isAddressEdited) {
            String updatedAddress = etAddress.getText().toString().trim();
            if (!TextUtils.isEmpty(updatedAddress)) {
                loggedInUser.setAddress(updatedAddress);
            }
        }

        if (canSave) {
            loggedInUser.setModifiedDate(new Date());
            etEmail.setEnabled(false);
            etAddress.setEnabled(false);
            btUpdateProfile.setVisibility(View.GONE);
            //Update
            staffCollectionRef.document(loggedInUserId).set(loggedInUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        SessionManager sessionManager = new SessionManager(getContext());
                        String userJson = gson.toJson(loggedInUser);
                        sessionManager.putString("loggedInUser", userJson);
                        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
                        View hView = navigationView.getHeaderView(0);
                        TextView nav_user = (TextView) getActivity().findViewById(R.id.tv_nav_name);
                        nav_user.setText(loggedInUser.getFirstName());
                        tvStaffName.setText("" + loggedInUser.getFirstName() + " " + loggedInUser.getLastName());
                    } else {
                        Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }

}
