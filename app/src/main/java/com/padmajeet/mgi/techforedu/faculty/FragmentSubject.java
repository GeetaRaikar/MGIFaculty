package com.padmajeet.mgi.techforedu.faculty;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.padmajeet.mgi.techforedu.faculty.model.Batch;
import com.padmajeet.mgi.techforedu.faculty.model.BatchSubjectFaculty;
import com.padmajeet.mgi.techforedu.faculty.model.Staff;
import com.padmajeet.mgi.techforedu.faculty.model.Subject;
import com.padmajeet.mgi.techforedu.faculty.util.SessionManager;
import com.padmajeet.mgi.techforedu.faculty.util.Utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import cn.pedant.SweetAlert.SweetAlertDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentSubject extends Fragment {

    private View view = null;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference batchCollectionRef = db.collection("Batch");
    private CollectionReference batchSubjectFacultyCollectionRef = db.collection("BatchSubjectFaculty");
    private CollectionReference subjectCollectionRef = db.collection("Subject");
    private ListenerRegistration subjectListener;
    private Subject subject;
    private ArrayList<Subject> subjectList = new ArrayList<>();
    private List<String> batchIdList=new ArrayList<>();
    private ArrayList<Batch> batchList = new ArrayList<>();
    private Batch batch, selectedBatch;
    private BatchSubjectFaculty batchSubjectFaculty;
    private ArrayList<BatchSubjectFaculty> batchSubjectFacultyList = new ArrayList<>();
    private Gson gson;
    private Staff loggedInUser;
    private String loggedInUserId, instituteId, academicYearId;
    private SweetAlertDialog pDialog;
    private SessionManager sessionManager;
    private LinearLayout llNoList;
    private Spinner spBatch;
    private RecyclerView rvSubject;
    private RecyclerView.Adapter subjectAdapter;
    private RecyclerView.LayoutManager layoutManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(getContext());
        gson = Utility.getGson();
        String userJson = sessionManager.getString("loggedInUser");
        loggedInUser = gson.fromJson(userJson, Staff.class);
        loggedInUserId = sessionManager.getString("loggedInUserId");
        academicYearId = sessionManager.getString("academicYearId");
        instituteId = sessionManager.getString("instituteId");
        String batchIdJson = sessionManager.getString("batchIdList");
        batchIdList = gson.fromJson(batchIdJson,new TypeToken<List<String>>() {
        }.getType());
        System.out.println("sessionManager batchIdList "+batchIdJson+" value "+batchIdList.toString());
        pDialog = Utility.createSweetAlertDialog(getContext());
    }

    public FragmentSubject() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_subject, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.subject));
        spBatch = view.findViewById(R.id.spBatch);
        llNoList = (LinearLayout) view.findViewById(R.id.llNoList);
        rvSubject = (RecyclerView) view.findViewById(R.id.rvSubject);
        layoutManager = new LinearLayoutManager(getContext());
        rvSubject.setLayoutManager(layoutManager);
        getBatches();

        spBatch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedBatch = batchList.get(position);
                getSubject();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void getBatches() {
        if(batchIdList.size() > 0) {
            if (!pDialog.isShowing()) {
                pDialog.show();
            }
            if (batchList.size() != 0) {
                batchList.clear();
            }
            Set<String> setBatch = new LinkedHashSet<String>(batchIdList);
            for (String x : setBatch) {
                batchCollectionRef.document("/" + x)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                batch = documentSnapshot.toObject(Batch.class);
                                batch.setId(documentSnapshot.getId());
                                batchList.add(batch);
                                System.out.println("batchList " + batchList.size());
                                if (setBatch.size() == batchList.size()) {
                                    if (pDialog != null) {
                                        pDialog.dismiss();
                                    }
                                    //sorting batch name
                                    Collections.sort(batchList, new Comparator<Batch>() {
                                        @Override
                                        public int compare(Batch b1, Batch b2) {
                                            try {
                                                return b1.getName().compareTo(b2.getName());
                                            } catch (android.net.ParseException e) {
                                                return 0;
                                            }
                                        }
                                    });
                                    //Spinner set up
                                    spBatch.setVisibility(View.VISIBLE);
                                    List<String> batchNameList = new ArrayList<>();
                                    for (Batch batch : batchList) {
                                        batchNameList.add(batch.getName());
                                    }
                                    ArrayAdapter<String> batchAdaptor = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, batchNameList);
                                    batchAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    spBatch.setAdapter(batchAdaptor);
                                    selectedBatch = batchList.get(0);
                                    getSubject();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
            }
        }
    }

    private void getSubject() {
        if (pDialog != null && !pDialog.isShowing()) {
            pDialog.show();
        }
        subjectListener = subjectCollectionRef
                .whereEqualTo("batchId", selectedBatch.getId())
                .orderBy("createdDate", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }
                        if (subjectList.size() != 0) {
                            subjectList.clear();
                        }
                        for (DocumentSnapshot documentSnapshot:queryDocumentSnapshots.getDocuments()){
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            subject = documentSnapshot.toObject(Subject.class);
                            subject.setId(documentSnapshot.getId());
                            subjectList.add(subject);
                        }
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        if (subjectList.size() != 0) {
                            subjectAdapter = new SubjectAdapter(subjectList);
                            rvSubject.setAdapter(subjectAdapter);
                            rvSubject.setVisibility(View.VISIBLE);
                            llNoList.setVisibility(View.GONE);
                        } else {
                            rvSubject.setVisibility(View.GONE);
                            llNoList.setVisibility(View.VISIBLE);
                        }
                    }
                });
        // [END get_all_users]
    }

    class SubjectAdapter extends RecyclerView.Adapter<SubjectAdapter.MyViewHolder> {
        private List<Subject> subjectList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvSubject, tvSubjectType;
            public ImageView ivSubjectPic;

            public MyViewHolder(View view) {
                super(view);
                tvSubject = view.findViewById(R.id.tvSubject);
                tvSubjectType = view.findViewById(R.id.tvSubjectType);
                ivSubjectPic = view.findViewById(R.id.ivSubjectPic);
            }
        }


        public SubjectAdapter(List<Subject> subjectList) {
            this.subjectList = subjectList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_subject, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final Subject subject = subjectList.get(position);
            holder.tvSubject.setText("" + subject.getName());
            if (TextUtils.isEmpty(subject.getType())) {
                holder.tvSubjectType.setText("" + subject.getType());
            } else {
                holder.tvSubjectType.setVisibility(View.GONE);
            }
            if (TextUtils.isEmpty(subject.getImageUrl())) {
                Glide.with(getContext())
                        .load(R.drawable.no_image)
                        .fitCenter()
                        .apply(RequestOptions.circleCropTransform())
                        .into(holder.ivSubjectPic);
            } else {
                Glide.with(getContext())
                        .load(subject.getImageUrl())
                        .fitCenter()
                        .apply(RequestOptions.circleCropTransform())
                        .into(holder.ivSubjectPic);
            }

        }

        @Override
        public int getItemCount() {
            return subjectList.size();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(subjectListener != null){
            subjectListener.remove();
        }
    }
}





