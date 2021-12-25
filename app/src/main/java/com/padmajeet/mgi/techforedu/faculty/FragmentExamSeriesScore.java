package com.padmajeet.mgi.techforedu.faculty;


import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.padmajeet.mgi.techforedu.faculty.model.Batch;
import com.padmajeet.mgi.techforedu.faculty.model.Exam;
import com.padmajeet.mgi.techforedu.faculty.model.ExamSeries;
import com.padmajeet.mgi.techforedu.faculty.model.Staff;
import com.padmajeet.mgi.techforedu.faculty.model.Subject;
import com.padmajeet.mgi.techforedu.faculty.util.SessionManager;
import com.padmajeet.mgi.techforedu.faculty.util.Utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentExamSeriesScore extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference examSeriesCollectionRef = db.collection("ExamSeries");
    private CollectionReference examCollectionRef = db.collection("Exam");
    private CollectionReference subjectCollectionRef = db.collection("Subject");
    private CollectionReference batchCollectionRef = db.collection("Batch");
    private ListenerRegistration subjectListener;
    private ListenerRegistration examSeriesListener;
    private LinearLayout llNoList;
    private RecyclerView rvExamSeriesScore;
    private RecyclerView.Adapter examSeriesAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private List<ExamSeries> examSeriesList = new ArrayList<>();
    private List<Exam> examList = new ArrayList<>();
    private List<Subject> subjectList = new ArrayList<>();
    private Gson gson;
    private Staff loggedInUser;
    private String academicYearId,instituteId;
    private DisplayMetrics metrics;
    private int width;
    private SweetAlertDialog pDialog;
    private Spinner spBatch;
    private Batch selectedBatch, batch;
    private List<Batch> batchList=new ArrayList<>();
    private List<String> batchIdList;
    private List<String> subjectIdList;

    public FragmentExamSeriesScore() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(getContext());
        gson = Utility.getGson();
        String staffJson = sessionManager.getString("loggedInUser");
        loggedInUser = gson.fromJson(staffJson, Staff.class);
        academicYearId = sessionManager.getString("academicYearId");
        instituteId = sessionManager.getString("instituteId");
        String batchIdJson = sessionManager.getString("batchIdList");
        batchIdList = gson.fromJson(batchIdJson,new TypeToken<List<String>>() {
        }.getType());
        System.out.println("sessionManager batchIdList "+batchIdJson+" value "+batchIdList.toString());
        String subjectIdJson = sessionManager.getString("subjectIdList");
        subjectIdList = gson.fromJson(subjectIdJson,new TypeToken<List<String>>() {
        }.getType());
        System.out.println("sessionManager subjectIdList "+subjectIdList.toString());
        pDialog = Utility.createSweetAlertDialog(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_exam_series_score, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((ActivityHome)getActivity()).getSupportActionBar().setTitle(getString(R.string.examSchedule));
        spBatch = view.findViewById(R.id.spBatch);
        llNoList = view.findViewById(R.id.llNoList);
        rvExamSeriesScore = view.findViewById(R.id.rvExamSeriesScore);
        layoutManager = new LinearLayoutManager(getContext());
        rvExamSeriesScore.setLayoutManager(layoutManager);

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
        }else{
            spBatch.setVisibility(View.GONE);
            rvExamSeriesScore.setVisibility(View.GONE);
            llNoList.setVisibility(View.VISIBLE);
        }
    }
    private void getSubject() {
        if(selectedBatch != null) {
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
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                                Subject subject = documentSnapshot.toObject(Subject.class);
                                subject.setId(documentSnapshot.getId());
                                subjectList.add(subject);
                            }
                            if (pDialog != null) {
                                pDialog.dismiss();
                            }
                            if (subjectList.size() != 0) {
                                getExamSeriesOfBatch();
                            } else {
                            }
                        }
                    });
        }
    }
    private void getExamSeriesOfBatch() {
        if(academicYearId != null && selectedBatch != null) {
            examSeriesListener = examSeriesCollectionRef
                    .whereEqualTo("academicYearId", academicYearId)
                    .whereEqualTo("batchId", selectedBatch.getId())
                    .orderBy("createdDate", Query.Direction.DESCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                return;
                            }
                            if (examSeriesList.size() != 0) {
                                examSeriesList.clear();
                            }
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                                ExamSeries examSeries = documentSnapshot.toObject(ExamSeries.class);
                                examSeries.setId(documentSnapshot.getId());
                                examSeriesList.add(examSeries);
                            }
                            if (examSeriesList.size() != 0) {
                                getAllExamOfExamSeriesOfBatch();
                            } else {
                                rvExamSeriesScore.setVisibility(View.GONE);
                                llNoList.setVisibility(View.VISIBLE);
                            }
                        }
                    });
        }
    }
    public void getAllExamOfExamSeriesOfBatch(){
        if(selectedBatch != null) {
            examCollectionRef
                    .whereEqualTo("batchId", selectedBatch.getId())
                    .orderBy("date", Query.Direction.ASCENDING)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (examList.size() > 0) {
                                examList.clear();
                            }
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                Exam exam = document.toObject(Exam.class);
                                exam.setId(document.getId());
                                examList.add(exam);
                            }
                            if (examList.size() > 0) {
                                for (Exam ex : examList) {
                                    for (Subject sub : subjectList) {
                                        if (ex.getSubjectId().equals(sub.getId())) {
                                            ex.setSubjectId(sub.getName());
                                            break;
                                        }
                                    }
                                }
                                examSeriesAdapter = new ExamSeriesAdapter(examSeriesList);
                                rvExamSeriesScore.setAdapter(examSeriesAdapter);
                                rvExamSeriesScore.setVisibility(View.VISIBLE);
                                llNoList.setVisibility(View.GONE);
                            } else {
                                rvExamSeriesScore.setVisibility(View.GONE);
                                llNoList.setVisibility(View.VISIBLE);
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
    class ExamSeriesAdapter extends RecyclerView.Adapter<ExamSeriesAdapter.MyViewHolder> {
        private List<ExamSeries> examSeriesList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvESName,tvESDate;
            public RecyclerView rvExamScore;
            public ImageView ivShow, ivHide;
            public MyViewHolder(View view) {
                super(view);
                tvESName = view.findViewById(R.id.tvESName);
                tvESDate = view.findViewById(R.id.tvESDate);
                ivShow = view.findViewById(R.id.ivShow);
                ivHide = view.findViewById(R.id.ivHide);
                rvExamScore = view.findViewById(R.id.rvExamScore);
            }
        }

        public ExamSeriesAdapter(List<ExamSeries> examSeriesList) {
            this.examSeriesList = examSeriesList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_exam_series_score, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final ExamSeries examSeries = examSeriesList.get(position);
            holder.tvESName.setText(""+examSeries.getName());
            if(examSeries.getFromDate() != null) {
                String date = Utility.formatDateToString(examSeries.getFromDate().getTime());
                if (examSeries.getToDate() != null) {
                    date = date + " to " + Utility.formatDateToString(examSeries.getToDate().getTime());
                }
                holder.tvESDate.setText("" + date);
            }
            /*
            if(examSeries.getToDate().getTime() > new Date().getTime()){
                holder.tvStatus.setVisibility(View.GONE);
            }else{
                holder.tvStatus.setVisibility(View.VISIBLE);
            }
            */
            RecyclerView.LayoutManager layoutManagerForExam = new LinearLayoutManager(getContext());
            holder.rvExamScore.setLayoutManager(layoutManagerForExam);
            RecyclerView.Adapter examAdapter;
            List<Exam> examForExamSeries = new ArrayList<>();
            for (Exam exam:examList){
                if(examSeries.getId().equals(exam.getExamSeriesId())) {
                    Exam selectedExam = exam;
                    selectedExam.setExamSeriesId(examSeries.getName());
                    examForExamSeries.add(selectedExam);
                }
            }
            if(examForExamSeries.size() > 0){
                examAdapter = new ExamAdapter(examForExamSeries);
                holder.rvExamScore.setAdapter(examAdapter);
            }
            holder.ivShow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.ivHide.setVisibility(View.VISIBLE);
                    holder.ivShow.setVisibility(View.GONE);
                    holder.rvExamScore.setVisibility(View.VISIBLE);
                }
            });
            holder.ivHide.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.ivHide.setVisibility(View.GONE);
                    holder.ivShow.setVisibility(View.VISIBLE);
                    holder.rvExamScore.setVisibility(View.GONE);
                }
            });
        }

        @Override
        public int getItemCount() {
            return examSeriesList.size();
        }
    }

    class ExamAdapter extends RecyclerView.Adapter<ExamAdapter.MyViewHolder> {
        private List<Exam> examForExamSeries;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvName,tvDate,tvTime,tvCutOffMarks,tvTotalMarks;
            public ImageView ivAddScore;
            public MyViewHolder(View view) {
                super(view);
                tvName = view.findViewById(R.id.tvName);
                tvDate = view.findViewById(R.id.tvDate);
                tvTime = view.findViewById(R.id.tvTime);
                tvCutOffMarks = view.findViewById(R.id.tvCutOffMarks);
                tvTotalMarks = view.findViewById(R.id.tvTotalMarks);
                ivAddScore = view.findViewById(R.id.ivAddScore);
            }
        }

        public ExamAdapter(List<Exam> examForExamSeries) {
            this.examForExamSeries = examForExamSeries;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_exam_score, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final Exam exam = examForExamSeries.get(position);
            holder.tvName.setText("" + exam.getSubjectId());
            holder.tvDate.setText(Utility.formatDateToString(exam.getDate().getTime()));
            holder.tvTime.setText(exam.getFromPeriod()+" - "+exam.getToPeriod());
            holder.tvCutOffMarks.setText(""+exam.getCutOffMarks());
            holder.tvTotalMarks.setText(""+exam.getTotalMarks());
            holder.ivAddScore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle bundle = new Bundle();
                    gson = Utility.getGson();
                    bundle.putString("selectedBatchName", selectedBatch.getName());
                    String selectedExam = gson.toJson(exam);
                    bundle.putString("selectedExam", selectedExam);
                    FragmentExamScoreCard fragmentExamScoreCard = new FragmentExamScoreCard();
                    fragmentExamScoreCard.setArguments(bundle);
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = manager.beginTransaction();
                    fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    fragmentTransaction.replace(R.id.contentLayout, fragmentExamScoreCard);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            });
        }

        @Override
        public int getItemCount() {
            return examForExamSeries.size();
        }
    }

}
