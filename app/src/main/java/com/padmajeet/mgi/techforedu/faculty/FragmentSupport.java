package com.padmajeet.mgi.techforedu.faculty;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.padmajeet.mgi.techforedu.faculty.model.Institute;
import com.padmajeet.mgi.techforedu.faculty.util.SessionManager;
import com.padmajeet.mgi.techforedu.faculty.util.Utility;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentSupport extends Fragment {
    private Institute institute;
    private FirebaseFirestore db= FirebaseFirestore.getInstance();
    private CollectionReference instituteCollectionRef=db.collection("Institute");
    private String instituteId;
    private Button btnAdminNumber;
    private Button btnAdminEmail;
    private SweetAlertDialog pDialog;

    public FragmentSupport() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(getContext());
        instituteId=sessionManager.getString("instituteId");
        pDialog= Utility.createSweetAlertDialog(getContext());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_support, container, false);
        ((ActivityHome)getActivity()).getSupportActionBar().setTitle(getString(R.string.support));
        btnAdminNumber = view.findViewById(R.id.btnAdminNumber);
        btnAdminEmail = view.findViewById(R.id.btnAdminEmail);
        if(instituteId != null) {
            if(pDialog == null && !pDialog.isShowing()) {
                pDialog.show();
            }
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
                            if (institute != null) {
                                btnAdminNumber.setText("" + institute.getPrimaryContactNumber());
                                btnAdminEmail.setText("" + institute.getEmailId());
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        }

        btnAdminNumber.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mobileNumber = "tel:"+institute.getPrimaryContactNumber();
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse(mobileNumber));
                startActivity(intent);
            }



        });

        btnAdminEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mailTo = "mailto:" +institute.getEmailId();
                Intent emailIntent = new Intent(Intent.ACTION_SEND,Uri.parse(mailTo));
                emailIntent.setType("text/plain");
                // emailIntent = new Intent (Intent.ACTION_VIEW , Uri.parse("mailto:" + "admin@gmail.com"));
                // emailIntent.setRecipient(emailIntent.RecipientType.TO, new InternetAddress("admin@gmail.com"));
                startActivity(emailIntent);
            }

            // return view;

        });
        return view;
    }

}
