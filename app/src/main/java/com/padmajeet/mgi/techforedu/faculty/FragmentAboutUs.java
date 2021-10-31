package com.padmajeet.mgi.techforedu.faculty;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.padmajeet.mgi.techforedu.faculty.model.Institute;
import com.padmajeet.mgi.techforedu.faculty.util.SessionManager;
import com.padmajeet.mgi.techforedu.faculty.util.Utility;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class FragmentAboutUs extends Fragment {

    private Institute institute;
    private View view;
    private TextView tvName, tvDesc, tvYear, tvAddress1, tvAddress2, tvAddress3, tvContact1, tvContact2, tvEmail, tvMission, tvVision, tvMissionLbl, tvVisionLbl;
    private LinearLayout llEstablishedYear;
    private MaterialCardView cvAddress, cvContact, cvMission;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference instituteCollectionRef = db.collection("Institute");
    private String instituteId;
    private SessionManager sessionManager;
    private SweetAlertDialog pDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(getContext());;
        instituteId = sessionManager.getString("instituteId");
        pDialog= Utility.createSweetAlertDialog(getContext());
    }

    public FragmentAboutUs() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_about_us, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.aboutUs));

        cvMission = view.findViewById(R.id.cvMission);
        cvContact = view.findViewById(R.id.cvContact);
        cvAddress = view.findViewById(R.id.cvAddress);
        llEstablishedYear = view.findViewById(R.id.llEstablishedYear);
        tvName = view.findViewById(R.id.tvName);
        tvDesc = view.findViewById(R.id.tvDesc);
        tvYear = view.findViewById(R.id.tvYear);
        tvAddress1 = view.findViewById(R.id.tvAddress1);
        tvAddress2 = view.findViewById(R.id.tvAddress2);
        tvAddress3 = view.findViewById(R.id.tvAddress3);
        tvContact1 = view.findViewById(R.id.tvContact1);
        tvContact2 = view.findViewById(R.id.tvContact2);
        tvEmail = view.findViewById(R.id.tvEmail);
        tvMission = view.findViewById(R.id.tvMission);
        tvVision = view.findViewById(R.id.tvVision);
        tvMissionLbl = view.findViewById(R.id.tvMissionLbl);
        tvVisionLbl = view.findViewById(R.id.tvVisionLbl);

        if(pDialog == null && !pDialog.isShowing()){
            pDialog.show();
        }
        //System.out.println("schoolId - "+schoolId);
        instituteCollectionRef
                .document(instituteId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        if (pDialog != null && pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                        institute = documentSnapshot.toObject(Institute.class);
                        //System.out.println("school - "+school);
                        if (institute != null) {
                            tvName.setText("" + institute.getName());
                            if (!TextUtils.isEmpty(institute.getAboutInstitute())) {
                                tvDesc.setText("" + institute.getAboutInstitute());
                            }
                            String address = institute.getAddress();
                            if (!(address.isEmpty())) {
                                cvAddress.setVisibility(View.VISIBLE);
                                tvAddress1.setText("" + address);
                                tvAddress2.setText("");
                                tvAddress3.setText("");
                            }
                            String primaryContact = institute.getPrimaryContactNumber();
                            if (!TextUtils.isEmpty(primaryContact)) {
                                tvContact1.setText("" + primaryContact);
                            }
                            String secondaryContact = institute.getSecondaryContactNumber();
                            if (!TextUtils.isEmpty(secondaryContact)) {
                                tvContact2.setText("" + secondaryContact);
                            }
                            String email = institute.getEmailId();
                            if (!TextUtils.isEmpty(email)) {
                                tvEmail.setText("" + email);
                            }
                            String mission = institute.getMission();
                            String vision = institute.getVision();
                            tvYear.setText("" + institute.getYearOfEstablishment());
                            if (!TextUtils.isEmpty(mission) || !TextUtils.isEmpty(vision)) {
                                cvMission.setVisibility(View.VISIBLE);
                                if (TextUtils.isEmpty(mission)) {
                                    tvMissionLbl.setVisibility(View.GONE);
                                } else {
                                    tvMission.setText("" + mission);
                                }
                                if (TextUtils.isEmpty(vision)) {
                                    tvVisionLbl.setVisibility(View.GONE);
                                } else {
                                    tvVision.setText("" + vision);
                                }
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (pDialog != null && pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                    }
                });


        return view;
    }

}
