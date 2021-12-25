package com.padmajeet.mgi.techforedu.faculty;

import android.net.ParseException;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
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

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentExamSeries extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference examSeriesCollectionRef = db.collection("ExamSeries");
    private CollectionReference examCollectionRef = db.collection("Exam");
    private CollectionReference subjectCollectionRef = db.collection("Subject");
    private CollectionReference batchCollectionRef = db.collection("Batch");
    private ListenerRegistration subjectListener;
    private ListenerRegistration examSeriesListener;
    private LinearLayout llNoList;
    private RecyclerView rvExamSeries;
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
    private TextView tvExamSeriesName,tvExamSeriesDate;
    private Spinner spBatch;
    private Batch selectedBatch, batch;
    private List<Batch> batchList=new ArrayList<>();
    private List<String> batchIdList;
    private List<String> subjectIdList;

    public FragmentExamSeries() {
        // Required empty public constructor
    }

    @Override
    public void onStop() {
        super.onStop();
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
        return inflater.inflate(R.layout.fragment_exam_series, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((ActivityHome)getActivity()).getSupportActionBar().setTitle(getString(R.string.examSchedule));
        spBatch = view.findViewById(R.id.spBatch);
        llNoList = view.findViewById(R.id.llNoList);
        rvExamSeries = view.findViewById(R.id.rvExamSeries);
        layoutManager = new LinearLayoutManager(getContext());
        rvExamSeries.setLayoutManager(layoutManager);

        getBatches();

        spBatch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedBatch = batchList.get(position);
                getSubject();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedBatch = batchList.get(0);
                getSubject();
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
                                if (setBatch.size() == batchList.size()) {
                                    System.out.println("batchList " + batchList.size());
                                    //sorting batch name
                                    Collections.sort(batchList, new Comparator<Batch>() {
                                        @Override
                                        public int compare(Batch b1, Batch b2) {
                                            try {
                                                return b1.getName().compareTo(b2.getName());
                                            } catch (ParseException e) {
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
                                    //selectedBatch=batchList.get(0);
                                    //getSubject();
                                }
                                if (pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                if (pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                            }
                        });
            }
        }else{
            spBatch.setVisibility(View.GONE);
            rvExamSeries.setVisibility(View.GONE);
            llNoList.setVisibility(View.VISIBLE);
        }
    }
    private void getSubject() {
        if(selectedBatch != null) {
            if (!pDialog.isShowing()) {
                pDialog.show();
            }
            if (subjectList.size() != 0) {
                subjectList.clear();
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
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                                Subject subject = documentSnapshot.toObject(Subject.class);
                                subject.setId(documentSnapshot.getId());
                                subjectList.add(subject);
                            }
                            System.out.println("subjectList =>" + subjectList.size());
                            if (subjectList.size() != 0) {
                                getExamSeriesOfBatch();
                            } else {
                                if (pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                            }
                        }
                    });
        }
    }
    private void  getExamSeriesOfBatch() {
        if(academicYearId != null && selectedBatch != null) {
            if (!pDialog.isShowing()) {
                pDialog.show();
            }
            if (examSeriesList.size() != 0) {
                examSeriesList.clear();
            }
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
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                                ExamSeries examSeries = documentSnapshot.toObject(ExamSeries.class);
                                examSeries.setId(documentSnapshot.getId());
                                examSeriesList.add(examSeries);
                            }
                            System.out.println("examSeriesList =>" + examSeriesList.size());
                            if (examSeriesList.size() != 0) {
                                getAllExamOfExamSeriesOfBatch();
                            } else {
                                if (pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                                rvExamSeries.setVisibility(View.GONE);
                                llNoList.setVisibility(View.VISIBLE);
                            }
                        }
                    });
        }
    }
    public void getAllExamOfExamSeriesOfBatch(){
        if (!pDialog.isShowing()) {
            pDialog.show();
        }
        if(examList.size()>0) {
            examList.clear();
        }
        examCollectionRef
                .whereEqualTo("batchId", selectedBatch.getId())
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Exam exam = document.toObject(Exam.class);
                            exam.setId(document.getId());
                            examList.add(exam);
                        }
                        System.out.println("examList =>"+examList.size());
                        if (pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                        if(examList.size() > 0){
                            for(Exam ex:examList) {
                                for (Subject sub : subjectList) {
                                    if(ex.getSubjectId().equals(sub.getId())){
                                        ex.setSubjectId(sub.getName());
                                        break;
                                    }
                                }
                            }
                            examSeriesAdapter = new ExamSeriesAdapter(examSeriesList);
                            rvExamSeries.setAdapter(examSeriesAdapter);
                            rvExamSeries.setVisibility(View.VISIBLE);
                            llNoList.setVisibility(View.GONE);
                        }else {
                            rvExamSeries.setVisibility(View.GONE);
                            llNoList.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                    }
                });
    }
    BottomSheetDialog bottomSheetDialog;
    class ExamSeriesAdapter extends RecyclerView.Adapter<ExamSeriesAdapter.MyViewHolder> {
        private List<ExamSeries> examSeriesList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvESName,tvESDate,tvStatus,tvDetails;
            public TableLayout tblExam;
            public MyViewHolder(View view) {
                super(view);
                tvESName = view.findViewById(R.id.tvESName);
                tvESDate = view.findViewById(R.id.tvESDate);
                tvStatus = view.findViewById(R.id.tvStatus);
                tvDetails = view.findViewById(R.id.tvDetails);
            }
        }

        public ExamSeriesAdapter(List<ExamSeries> examSeriesList) {
            this.examSeriesList = examSeriesList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_exam_series, parent, false);
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
                if (examSeries.getToDate().getTime() > new Date().getTime()) {
                    //holder.tvStatus.setVisibility(View.GONE);
                } else {
                    holder.tvStatus.setVisibility(View.VISIBLE);
                }
            }
            List<Exam> examForExamSeries = new ArrayList<>();
            for (Exam exam:examList){
                if(examSeries.getId().equals(exam.getExamSeriesId())) {
                    examForExamSeries.add(exam);
                }
            }
            if(examForExamSeries.size() == 0){
                holder.tvStatus.setText("Note : Exams are not yet declared.");
                holder.tvStatus.setVisibility(View.VISIBLE);
                holder.tvDetails.setVisibility(View.GONE);
            }
            holder.tvDetails.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    List<Exam> examForExamSeries = new ArrayList<>();
                    for (Exam exam:examList){
                        if(examSeries.getId().equals(exam.getExamSeriesId())) {
                            examForExamSeries.add(exam);
                        }
                    }
                    if(examForExamSeries.size() > 0){
                        onCreateBottomSheet(examSeries,examForExamSeries);
                        bottomSheetDialog.show();
                    }else{
                        bottomSheetDialog.hide();
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return examSeriesList.size();
        }
    }
    public void onCreateBottomSheet(ExamSeries examSeries,List<Exam> examForExamSeries){
        if(bottomSheetDialog == null){
            View viewBottom = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_view_exam, null);
            bottomSheetDialog = new BottomSheetDialog(getContext());
            bottomSheetDialog.setContentView(viewBottom);
            tvExamSeriesName = viewBottom.findViewById(R.id.tvExamSeriesName);
            tvExamSeriesDate = viewBottom.findViewById(R.id.tvExamSeriesDate);
            System.out.println("examSeries "+examSeries.getName());
            tvExamSeriesName.setText(""+examSeries.getName());
            String date=Utility.formatDateToString(examSeries.getFromDate().getTime());
            if(examSeries.getToDate() != null){
                date = date + " to " +Utility.formatDateToString(examSeries.getToDate().getTime());
            }
            tvExamSeriesDate.setText(""+date);
            RecyclerView rvExam = viewBottom.findViewById(R.id.rvExam);
            TextView tvNoData = viewBottom.findViewById(R.id.tvNoData);
            RecyclerView.LayoutManager layoutManagerForExam = new LinearLayoutManager(getContext());
            rvExam.setLayoutManager(layoutManagerForExam);
            RecyclerView.Adapter examAdapter;
            if(examForExamSeries.size() > 0){
                examAdapter = new ExamAdapter(examForExamSeries);
                rvExam.setAdapter(examAdapter);
                rvExam.setVisibility(View.VISIBLE);
                tvNoData.setVisibility(View.GONE);
            }else{
                rvExam.setVisibility(View.GONE);
                tvNoData.setVisibility(View.VISIBLE);
            }
        }
    }
    class ExamAdapter extends RecyclerView.Adapter<ExamAdapter.MyViewHolder> {
        private List<Exam> examForExamSeries;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvName,tvDate,tvTime,tvCutOffMarks,tvTotalMarks;
            public MyViewHolder(View view) {
                super(view);
                tvName = view.findViewById(R.id.tvName);
                tvDate = view.findViewById(R.id.tvDate);
                tvTime = view.findViewById(R.id.tvTime);
                tvCutOffMarks = view.findViewById(R.id.tvCutOffMarks);
                tvTotalMarks = view.findViewById(R.id.tvTotalMarks);
            }
        }

        public ExamAdapter(List<Exam> examForExamSeries) {
            this.examForExamSeries = examForExamSeries;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_exam, parent, false);
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

        }

        @Override
        public int getItemCount() {
            return examForExamSeries.size();
        }
    }

}
