package com.padmajeet.mgi.techforedu.faculty;


import android.content.Context;
import android.content.DialogInterface;
import android.net.ParseException;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.padmajeet.mgi.techforedu.faculty.model.Exam;
import com.padmajeet.mgi.techforedu.faculty.model.ScoreCard;
import com.padmajeet.mgi.techforedu.faculty.model.Staff;
import com.padmajeet.mgi.techforedu.faculty.model.Student;
import com.padmajeet.mgi.techforedu.faculty.util.SessionManager;
import com.padmajeet.mgi.techforedu.faculty.util.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentExamScoreCard extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference scoreCardCollectionRef = db.collection("ScoreCard");
    private CollectionReference studentCollectionRef = db.collection("Student");
    private CollectionReference examCollectionRef = db.collection("Exam");
    private ListenerRegistration scoreCardListener;
    private TextView tvBatch,tvExamSeries,tvSubject,tvDate,tvNoData,tvTotalMarks,tvCutOffMarks,tvEmptyMarks;
    private LinearLayout llButtons,llNoList;
    private Button btnSave,btnUpdate;
    private ImageView ivNoData;
    private Gson gson;
    private Staff loggedInUser;
    private Exam currentExam;
    private List<Student> studentList=new ArrayList<>();
    private List<ScoreCard> scoreCardList=new ArrayList<>();
    private List<ScoreCard> updateScoreCardList=new ArrayList<>();
    private List<StudentScoreCard> studentScoreCardList = new ArrayList<>();
    private String academicYearId;
    private String selectedBatchName;
    private ListView lvStudent;
    private List<TempScoreCard> tempScoreCardList = new ArrayList<>();
    private int count = 0;
    private SweetAlertDialog pDialog;

    class StudentScoreCard{
        public Student student;
        public ScoreCard scoreCard;
        public StudentScoreCard(Student s, ScoreCard sc){
            this.student = s;
            this.scoreCard = sc;
        }
    }
    class TempScoreCard{
        public Student student;
        public String tempMarks;
        public TempScoreCard(Student s, String tempMarks){
            this.student = s;
            this.tempMarks = tempMarks;
        }
    }
    public FragmentExamScoreCard() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(getContext());
        String facultyJson = sessionManager.getString("loggedInUser");
        gson = Utility.getGson();
        loggedInUser = gson.fromJson(facultyJson, Staff.class);
        academicYearId = sessionManager.getString("academicYearId");
        Bundle bundle = getArguments();
        selectedBatchName = bundle.getString("selectedBatchName");
        String examJson = bundle.getString("selectedExam");
        currentExam = gson.fromJson(examJson,Exam.class);
        pDialog = Utility.createSweetAlertDialog(getContext());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_exam_score_card, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((ActivityHome)getActivity()).getSupportActionBar().setTitle(getString(R.string.scoreCard));
        tvBatch = view.findViewById(R.id.tvBatch);
        tvExamSeries = view.findViewById(R.id.tvExamSeries);
        tvSubject = view.findViewById(R.id.tvSubject);
        tvDate = view.findViewById(R.id.tvDate);
        tvNoData = view.findViewById(R.id.tvNoData);
        llButtons = view.findViewById(R.id.llButtons);
        llNoList = view.findViewById(R.id.llNoList);
        btnSave = view.findViewById(R.id.btnSave);
        btnSave = view.findViewById(R.id.btnSave);
        btnUpdate = view.findViewById(R.id.btnUpdate);
        ivNoData = view.findViewById(R.id.ivNoData);
        tvTotalMarks = view.findViewById(R.id.tvTotalMarks);
        tvCutOffMarks = view.findViewById(R.id.tvCutOffMarks);
        lvStudent = view.findViewById(R.id.lvStudent);
        tvEmptyMarks = view.findViewById(R.id.tvEmptyMarks);

        tvBatch.setText(""+selectedBatchName);
        tvExamSeries.setText(""+currentExam.getExamSeriesId());
        tvSubject.setText(""+currentExam.getSubjectId());
        tvDate.setText(""+Utility.formatDateToString(currentExam.getDate().getTime()));
        tvTotalMarks.setText(""+currentExam.getTotalMarks());
        tvCutOffMarks.setText(""+currentExam.getCutOffMarks());

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        getStudentsOfBatch();
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processMarksOfStudents();
            }
        });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //processMarksOfStudents();
                processUpdatedMarksOfStudents();
            }
        });
    }
    private void getScoreCardOfBatchForSubject() {
        scoreCardListener = scoreCardCollectionRef
                .whereEqualTo("examId", currentExam.getId())
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }
                        if (scoreCardList.size() != 0) {
                            scoreCardList.clear();
                        }
                        for (DocumentSnapshot documentSnapshot:queryDocumentSnapshots.getDocuments()){
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            ScoreCard scoreCard = documentSnapshot.toObject(ScoreCard.class);
                            scoreCard.setId(documentSnapshot.getId());
                            scoreCard.setGrade(""+scoreCard.getMarks());
                            scoreCardList.add(scoreCard);
                        }
                        if (scoreCardList.size() != 0) {
                            studentScoreCardList.clear();
                            for (ScoreCard scoreCard:scoreCardList){
                                for(Student student:studentList){
                                    if (scoreCard.getStudentId().equals(student.getId())) {
                                        studentScoreCardList.add(new StudentScoreCard(student, scoreCard));
                                    }
                                }
                            }

                            /*
                            //sorting student name
                            Collections.sort(studentScoreCardList, new Comparator<StudentScoreCard>() {
                                @Override
                                public int compare(StudentScoreCard sa1, StudentScoreCard sa2) {
                                    try {
                                        return sa1.student.getFirstName().compareTo(sa2.student.getFirstName());
                                    } catch (android.net.ParseException e) {
                                        return 0;
                                    }
                                }
                            });
                            StudentMarkAdapter studentMarkAdapter = new StudentMarkAdapter(studentScoreCardList);
                            rvStudent.setAdapter(studentMarkAdapter);
                            rvStudent.setVisibility(View.VISIBLE);*/
                            StudentMarkAdapter studentMarkAdapter = new StudentMarkAdapter(getContext(), studentScoreCardList);
                            lvStudent.setAdapter(studentMarkAdapter);
                            llNoList.setVisibility(View.GONE);
                        } else {
                            lvStudent.setVisibility(View.GONE);
                            llNoList.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }
    private void processMarksOfStudents(){
        if(pDialog != null && !pDialog.isShowing()){
            pDialog.show();
        }
        tvEmptyMarks.setVisibility(View.GONE);
        if(tempScoreCardList!=null){
            if(scoreCardList.size() > 0){
                scoreCardList.clear();
            }
            for(int i=0;i<tempScoreCardList.size();i++){
                Student student = tempScoreCardList.get(i).student;
                ScoreCard scoreCard = new ScoreCard();
                scoreCard.setExamId(currentExam.getId());
                scoreCard.setCreatorId(loggedInUser.getId());
                scoreCard.setModifierId(loggedInUser.getId());
                scoreCard.setCreatorType("F");
                scoreCard.setModifierType("F");
                scoreCard.setStudentId(student.getId());
                scoreCard.setMarks(-1f);
                scoreCard.setResult("Fail");
                if(TextUtils.isEmpty(tempScoreCardList.get(i).tempMarks)){
                    if(pDialog != null && pDialog.isShowing()){
                        pDialog.dismiss();
                    }
                    tvEmptyMarks.setVisibility(View.VISIBLE);
                    return;
                }
                try{
                    scoreCard.setMarks(Float.parseFloat(tempScoreCardList.get(i).tempMarks));
                    if(scoreCard.getMarks() > currentExam.getTotalMarks()){
                        if(pDialog != null && pDialog.isShowing()){
                            pDialog.dismiss();
                        }
                        tvEmptyMarks.setText("Marks should be less then total marks.");
                        tvEmptyMarks.setVisibility(View.VISIBLE);
                        return;
                    }
                    if(scoreCard.getMarks() >= currentExam.getCutOffMarks()){
                        scoreCard.setResult("Pass");
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                    if(pDialog != null && pDialog.isShowing()){
                        pDialog.dismiss();
                    }
                    tvEmptyMarks.setText("Invalid marks.");
                    tvEmptyMarks.setVisibility(View.VISIBLE);
                    return;
                }
                scoreCardList.add(scoreCard);
            }
            saveMarksOfStudents();
        }
        else{
            Toast.makeText(getContext(),"No students",Toast.LENGTH_LONG).show();
        }
    }
    private void saveMarksOfStudents(){
        count = 0;
        for(ScoreCard scoreCard:scoreCardList) {
            scoreCardCollectionRef
                    .add(scoreCard)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            count++;
                            if(count == scoreCardList.size()) {
                                currentExam.setStatus("MA");
                                currentExam.setModifiedDate(new Date());
                                currentExam.setModifierId(loggedInUser.getId());
                                currentExam.setModifierType("F");
                                examCollectionRef.document(currentExam.getId()).update(
                                        "status", "MA",
                                        "modifiedDate", new Date(),
                                        "modifierId", loggedInUser.getId(),
                                        "modifierType", "F"
                                ).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            if(pDialog != null && pDialog.isShowing()){
                                                pDialog.dismiss();
                                            }
                                            SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                                    .setTitleText("Success")
                                                    .setContentText("Successfully marks updated.")
                                                    .setConfirmText("Ok")
                                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                        @Override
                                                        public void onClick(SweetAlertDialog sDialog) {
                                                            sDialog.dismissWithAnimation();
                                                        }
                                                    });
                                            dialog.setCancelable(false);
                                            dialog.show();
                                            btnSave.setVisibility(View.GONE);
                                            btnUpdate.setVisibility(View.VISIBLE);
                                        } else {
                                            if(pDialog != null && pDialog.isShowing()){
                                                pDialog.dismiss();
                                            }
                                            Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
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
    private void getStudentsOfBatch(){
        if(studentList.size() > 0){
            studentList.clear();
        }
        studentCollectionRef
                .whereEqualTo("academicYearId", academicYearId)
                .whereEqualTo("currentBatchId", currentExam.getBatchId())
                .whereIn("status", Arrays.asList("A", "F"))
                .orderBy("usn", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            Student student = documentSnapshot.toObject(Student.class);
                            student.setId(documentSnapshot.getId());
                            studentList.add(student);
                        }
                        System.out.println("studentList " + studentList.size());
                        if(studentList.size() > 0 ){
                            System.out.println("currentExam.getStatus() "+currentExam.getStatus());
                            if(currentExam.getStatus().equals("A")){
                                //sorting student name
                                Collections.sort(studentList, new Comparator<Student>() {
                                    @Override
                                    public int compare(Student s1, Student s2) {
                                        try {
                                            return s1.getFirstName().compareTo(s2.getFirstName());
                                        } catch (ParseException e) {
                                            return 0;
                                        }
                                    }
                                });
                                tempScoreCardList.clear();
                                for (Student stu: studentList) {
                                    tempScoreCardList.add(new TempScoreCard(stu,""));
                                }

                                StudentAdapter studentAdapter = new StudentAdapter(getContext(), tempScoreCardList);
                                lvStudent.setAdapter(studentAdapter);
                                llNoList.setVisibility(View.GONE);
                            }
                            else if(currentExam.getStatus().equals("MA")){
                                //llButtons.setVisibility(View.GONE);
                                btnSave.setVisibility(View.GONE);
                                btnUpdate.setVisibility(View.VISIBLE);
                                getScoreCardOfBatchForSubject();
                            }
                        }else{
                            lvStudent.setVisibility(View.GONE);
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
    class StudentAdapter extends BaseAdapter {
        Context context;
        List<TempScoreCard> tempScoreCardList;
        LayoutInflater inflter;

        public StudentAdapter(Context applicationContext, List<TempScoreCard> tempScoreCardList) {
            this.context = applicationContext;
            this.tempScoreCardList = tempScoreCardList;
            inflter = (LayoutInflater.from(applicationContext));
        }

        @Override
        public int getCount() {
            return tempScoreCardList.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = inflter.inflate(R.layout.row_student_exam_add_marks, null);
            TextView tvName = (TextView) view.findViewById(R.id.tvName);
            TextView tvUsn = (TextView) view.findViewById(R.id.tvUsn);
            EditText etMarks = (EditText) view.findViewById(R.id.etMarks);

            StringBuffer name = new StringBuffer(tempScoreCardList.get(i).student.getFirstName());
            if(!TextUtils.isEmpty(tempScoreCardList.get(i).student.getLastName())){
                name = name.append(" "+tempScoreCardList.get(i).student.getLastName());
            }
            tvName.setText(name);
            tvUsn.setText(tempScoreCardList.get(i).student.getUsn());
            if(!TextUtils.isEmpty(tempScoreCardList.get(i).tempMarks)){
                etMarks.setText(""+tempScoreCardList.get(i).tempMarks);
            }
            etMarks.setId(i);
            etMarks.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus){
                        final int position = v.getId();
                        final EditText TempMarks = (EditText) v;
                        tempScoreCardList.get(position).tempMarks=TempMarks.getText().toString();
                    }
                }
            });
            return view;
        }
    }
    class StudentMarkAdapter extends BaseAdapter {
        Context context;
        List<StudentScoreCard> studentScoreCardList;
        LayoutInflater inflter;

        public StudentMarkAdapter(Context applicationContext, List<StudentScoreCard> studentScoreCardList) {
            this.context = applicationContext;
            this.studentScoreCardList = studentScoreCardList;
            inflter = (LayoutInflater.from(applicationContext));
        }

        @Override
        public int getCount() {
            return studentScoreCardList.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = inflter.inflate(R.layout.row_student_exam_add_marks, null);
            TextView tvName = (TextView) view.findViewById(R.id.tvName);
            TextView tvUsn = (TextView) view.findViewById(R.id.tvUsn);
            EditText etMarks = (EditText) view.findViewById(R.id.etMarks);

            StringBuffer name = new StringBuffer(studentScoreCardList.get(i).student.getFirstName());
            if(!TextUtils.isEmpty(studentScoreCardList.get(i).student.getLastName())){
                name = name.append(" "+studentScoreCardList.get(i).student.getLastName());
            }
            tvName.setText(name);
            tvUsn.setText(studentScoreCardList.get(i).student.getUsn());
            if(!TextUtils.isEmpty(studentScoreCardList.get(i).scoreCard.getGrade())){
                etMarks.setText(""+studentScoreCardList.get(i).scoreCard.getGrade());
            }
            etMarks.setId(i);
            etMarks.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                public void onFocusChange(View v, boolean hasFocus) {
                    if (!hasFocus){
                        final int position = v.getId();
                        final EditText TempMarks = (EditText) v;
                        studentScoreCardList.get(position).scoreCard.setGrade(TempMarks.getText().toString());
                    }
                }
            });
            return view;
        }
    }
    private void processUpdatedMarksOfStudents(){
        if(pDialog != null && !pDialog.isShowing()){
            pDialog.show();
        }
        tvEmptyMarks.setVisibility(View.GONE);
        if(studentScoreCardList!=null){
            for(int i=0;i<studentScoreCardList.size();i++){
                if(TextUtils.isEmpty(studentScoreCardList.get(i).scoreCard.getGrade())){
                    if(pDialog.isShowing()){
                        pDialog.dismiss();
                    }
                    tvEmptyMarks.setVisibility(View.VISIBLE);
                    return;
                }
                try{
                    Float m=Float.parseFloat(studentScoreCardList.get(i).scoreCard.getGrade());
                    if(m > currentExam.getTotalMarks()){
                        if(pDialog.isShowing()){
                            pDialog.dismiss();
                        }
                        tvEmptyMarks.setText("Marks should be less then total marks.");
                        tvEmptyMarks.setVisibility(View.VISIBLE);
                        return;
                    }
                    if(m >= currentExam.getCutOffMarks()){
                        studentScoreCardList.get(i).scoreCard.setResult("Pass");
                    }
                }
                catch (Exception e){
                    e.printStackTrace();
                    if(pDialog.isShowing()){
                        pDialog.dismiss();
                    }
                    tvEmptyMarks.setText("Invalid marks.");
                    tvEmptyMarks.setVisibility(View.VISIBLE);
                    return;
                }
                //scoreCardList.add(studentScoreCardList.get(i).scoreCard);
            }
            System.out.println("studentScoreCardList "+studentScoreCardList.size());
            if(updateScoreCardList.size() > 0){
                updateScoreCardList.clear();
            }
            for(int i=0;i<studentScoreCardList.size();i++) {
                float m=Float.parseFloat(studentScoreCardList.get(i).scoreCard.getGrade());
                float om=studentScoreCardList.get(i).scoreCard.getMarks();
                if(om != m){
                    System.out.println("(scoreCardList.get(i).getGrade()) "+m);
                    System.out.println("(scoreCardList.get(i).getMarks()) "+om);
                    studentScoreCardList.get(i).scoreCard.setMarks(m);
                    updateScoreCardList.add(studentScoreCardList.get(i).scoreCard);
                }
            }
            System.out.println("updateScoreCardList "+updateScoreCardList.size());
            if(updateScoreCardList.size() > 0) {
                updateMarksOfStudents();
            }else{
                if(pDialog.isShowing()){
                    pDialog.dismiss();
                }
            }
        }
        else{
            Toast.makeText(getContext(),"No students",Toast.LENGTH_LONG).show();
        }
    }
    private void updateMarksOfStudents(){
        count = 0;
        for(ScoreCard updateScoreCard:updateScoreCardList) {
            scoreCardCollectionRef
                    .document(updateScoreCard.getId())
                    .update(
                            "marks", updateScoreCard.getMarks(),
                            "result",updateScoreCard.getResult(),
                            "modifiedDate", new Date(),
                            "modifierId", loggedInUser.getId(),
                            "modifierType", "F"
                    ).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            count++;
                            System.out.println("pDialog.isShowing() "+pDialog.isShowing());
                            System.out.println("count "+count);
                            System.out.println("updateScoreCardList.size() "+updateScoreCardList.size());
                            if(count == updateScoreCardList.size()) {
                                if(pDialog.isShowing()){
                                    pDialog.hide();
                                }
                                SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                        .setTitleText("Success")
                                        .setContentText("Successfully marks updated.")
                                        .setConfirmText("Ok")
                                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                            @Override
                                            public void onClick(SweetAlertDialog sDialog) {
                                                sDialog.dismissWithAnimation();
                                            }
                                        });
                                dialog.setCancelable(false);
                                dialog.show();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            if(pDialog != null && pDialog.isShowing()){
                                pDialog.dismiss();
                            }
                        }
                    });
        }
    }
}
