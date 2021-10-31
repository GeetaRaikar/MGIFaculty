package com.padmajeet.mgi.techforedu.faculty;


import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentExamScoreCard extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference scoreCardCollectionRef = db.collection("ScoreCard");
    private CollectionReference studentCollectionRef = db.collection("Student");
    private ListenerRegistration scoreCardListener;
    private TextView tvBatch,tvExamSeries,tvSubject,tvDate,tvNoData;
    private RecyclerView rvStudent;
    private RecyclerView.Adapter studentAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private LinearLayout llButtons,llNoList;
    private Button btnSave,btnSubmit;
    private ImageView ivNoData;
    private Gson gson;
    private Staff loggedInUser;
    private Exam currentExam;
    private List<Student> studentList=new ArrayList<>();
    private List<ScoreCard> scoreCardList=new ArrayList<>();
    private List<StudentScoreCard> studentScoreCardList = new ArrayList<>();
    private String academicYearId;
    private String selectedBatchName;

    class StudentScoreCard{
        public Student student;
        public ScoreCard scoreCard;
        public StudentScoreCard(Student s, ScoreCard sc){
            this.student = s;
            this.scoreCard = sc;
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
        rvStudent = view.findViewById(R.id.rvStudent);
        layoutManager = new LinearLayoutManager(getContext());
        rvStudent.setLayoutManager(layoutManager);
        llButtons = view.findViewById(R.id.llButtons);
        llNoList = view.findViewById(R.id.llNoList);
        btnSave = view.findViewById(R.id.btnSave);
        btnSubmit = view.findViewById(R.id.btnSubmit);
        ivNoData = view.findViewById(R.id.ivNoData);

        tvBatch.setText(""+selectedBatchName);
        tvExamSeries.setText(""+currentExam.getExamSeriesId());
        tvSubject.setText(""+currentExam.getSubjectId());
        tvDate.setText(""+Utility.formatDateToString(currentExam.getDate().getTime()));

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        getStudentsOfBatch();
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processMarksOfStudents();
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
                            rvStudent.setVisibility(View.VISIBLE);
                            llNoList.setVisibility(View.GONE);
                        } else {
                            rvStudent.setVisibility(View.GONE);
                            llNoList.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }
    private void processMarksOfStudents(){
        if(studentList!=null){
            if(scoreCardList.size() > 0){
                scoreCardList.clear();
            }
            for(int i=0;i<studentList.size();i++){
                Student student = studentList.get(i);
                ScoreCard scoreCard = new ScoreCard();
                scoreCard.setExamId(currentExam.getId());
                scoreCard.setCreatorId(loggedInUser.getId());
                scoreCard.setModifierId(loggedInUser.getId());
                scoreCard.setCreatorType("F");
                scoreCard.setModifierType("F");
                scoreCard.setStudentId(student.getId());
                scoreCard.setMarks(-1f);
                scoreCard.setResult("Fail");
                View view = rvStudent.getChildAt(i);
                if (view != null) {
                    EditText etMarks = view.findViewById(R.id.etMarks);
                    if(etMarks != null){
                        String marks =etMarks.getText().toString().trim();
                        if(TextUtils.isEmpty(marks)){
                            etMarks.setError("Enter the marks");
                            etMarks.requestFocus();
                            return;
                        }
                        try{
                            scoreCard.setMarks(Float.parseFloat(marks));
                            if(scoreCard.getMarks() >= currentExam.getCutOffMarks()){
                                scoreCard.setResult("Pass");
                            }
                        }
                        catch (Exception e){
                            e.printStackTrace();
                            etMarks.setError("Enter the valid marks");
                            etMarks.requestFocus();
                            return;
                        }
                    }
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
        final int[] count = {0};
        for(ScoreCard scoreCard:scoreCardList) {
            scoreCardCollectionRef
                    .add(scoreCard)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            count[0]++;
                            if(count[0] == scoreCardList.size()) {
                                currentExam.setStatus("MA");
                                AlertDialog.Builder builder = new AlertDialog.Builder(
                                        getContext());
                                builder.setTitle("Success");
                                builder.setCancelable(true);
                                builder.setMessage("Marks update successfully done");
                                builder.setPositiveButton("OK",
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog,
                                                                int which) {
                                                dialog.cancel();
                                            }
                                        });
                                builder.show();
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
                                        } catch (android.net.ParseException e) {
                                            return 0;
                                        }
                                    }
                                });
                                StudentAdapter studentAdapter  = new StudentAdapter(studentList);
                                rvStudent.setAdapter(studentAdapter);
                                rvStudent.setVisibility(View.VISIBLE);
                                llNoList.setVisibility(View.GONE);
                            }
                            else if(currentExam.getStatus().equals("MA")){
                                llButtons.setVisibility(View.GONE);
                                getScoreCardOfBatchForSubject();
                            }
                        }else{
                            rvStudent.setVisibility(View.GONE);
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

    class StudentMarkAdapter extends RecyclerView.Adapter<StudentMarkAdapter.MyViewHolder> {
        private List<StudentScoreCard> studentScoreCardList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvName,tvUsn;
            public EditText etMarks;
            public MyViewHolder(View view) {
                super(view);
                tvName = view.findViewById(R.id.tvName);
                tvUsn = view.findViewById(R.id.tvUsn);
                etMarks = view.findViewById(R.id.etMarks);
            }
        }

        public StudentMarkAdapter(List<StudentScoreCard> studentScoreCardList) {
            this.studentScoreCardList = studentScoreCardList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_student_exam_add_marks, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final StudentScoreCard studentScoreCard = studentScoreCardList.get(position);
            StringBuffer name = new StringBuffer(studentScoreCard.student.getFirstName());
            if(!TextUtils.isEmpty(studentScoreCard.student.getLastName())){
                name = name.append(" "+studentScoreCard.student.getLastName());
            }
            holder.tvName.setText(name);
            holder.tvUsn.setText(studentScoreCard.student.getUsn());
            holder.etMarks.setText(""+studentScoreCard.scoreCard.getMarks());
            holder.etMarks.setEnabled(false);
            holder.etMarks.setBackground(null);
        }

        @Override
        public int getItemCount() {
            return studentScoreCardList.size();
        }
    }

    class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.MyViewHolder> {
        private List<Student> studentList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvName,tvUsn;
            public EditText etMarks;
            public MyViewHolder(View view) {
                super(view);
                tvName = view.findViewById(R.id.tvName);
                tvUsn = view.findViewById(R.id.tvUsn);
                etMarks = view.findViewById(R.id.etMarks);
            }
        }

        public StudentAdapter(List<Student> studentList) {
            this.studentList = studentList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_student_exam_add_marks, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final Student student = studentList.get(position);
            StringBuffer name = new StringBuffer(student.getFirstName());
            if(!TextUtils.isEmpty(student.getLastName())){
                name = name.append(" "+student.getLastName());
            }
            holder.tvName.setText(name);
            holder.tvUsn.setText(student.getUsn());
        }

        @Override
        public int getItemCount() {
            return studentList.size();
        }
    }
}
