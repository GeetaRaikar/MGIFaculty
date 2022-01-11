package com.padmajeet.mgi.techforedu.faculty;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import cn.pedant.SweetAlert.SweetAlertDialog;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
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
import com.padmajeet.mgi.techforedu.faculty.model.Attendance;
import com.padmajeet.mgi.techforedu.faculty.model.Batch;
import com.padmajeet.mgi.techforedu.faculty.model.Staff;
import com.padmajeet.mgi.techforedu.faculty.model.Student;
import com.padmajeet.mgi.techforedu.faculty.model.Subject;
import com.padmajeet.mgi.techforedu.faculty.util.SessionManager;
import com.padmajeet.mgi.techforedu.faculty.util.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentAttendanceSummary extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference batchCollectionRef = db.collection("Batch");
    private CollectionReference subjectCollectionRef = db.collection("Subject");
    private CollectionReference attendanceCollectionRef = db.collection("Attendance");
    private CollectionReference studentCollectionRef = db.collection("Student");
    private ListenerRegistration studentListener;
    private ListenerRegistration attendanceListener;
    private SessionManager sessionManager;
    private Bundle bundle=new Bundle();
    private Gson gson;
    private Staff loggedInUser;
    private LinearLayout llNoList;
    private ImageView ivNoData;
    private TextView tvNoData;
    private PieChart chart;
    private LinearLayout rlAttendanceSummary;
    private List<Attendance> attendanceList = new ArrayList<>();
    private List<Subject> subjectList = new ArrayList<>();
    private List<Batch> batchList = new ArrayList<>();
    private List<String> batchIdList = new ArrayList<>();
    private TableLayout tblSubject;
    private String instituteId, academicYearId;
    private Batch selectedBatch, batch;
    private Spinner spBatch;
    private List<SubjectAttendance> subjectAttendanceList = new ArrayList<>();
    private int totalStudents;
    private List<Student> studentList= new ArrayList<>();
    private SweetAlertDialog pDialog;

    private class SubjectAttendance{
        String subjectCode;
        String subjectName;
        String totalSubjectClass;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(getContext());
        gson = Utility.getGson();
        String userJson = sessionManager.getString("loggedInUser");
        loggedInUser = gson.fromJson(userJson, Staff.class);
        academicYearId = sessionManager.getString("academicYearId");
        instituteId = sessionManager.getString("instituteId");
        String batchIdJson = sessionManager.getString("batchIdList");
        batchIdList = gson.fromJson(batchIdJson,new TypeToken<List<String>>() {
        }.getType());
        System.out.println("sessionManager batchIdList "+batchIdJson+" value "+batchIdList.toString());
        pDialog = Utility.createSweetAlertDialog(getContext());

    }
    public FragmentAttendanceSummary() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_attendance_summary, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((ActivityHome)getActivity()).getSupportActionBar().setTitle(getString(R.string.attendanceSummary));
        spBatch = view.findViewById(R.id.spBatch);
        rlAttendanceSummary = view.findViewById(R.id.rlAttendanceSummary);
        chart = view.findViewById(R.id.chartAttendanceSummary);
        tblSubject = view.findViewById(R.id.tblSubject);
        llNoList = view.findViewById(R.id.llNoList);
        tvNoData = view.findViewById(R.id.tvNoData);
        ivNoData = view.findViewById(R.id.ivNoData);
        //chart.setCenterTextTypeface(tfLight);
        //chart.setCenterText(generateCenterSpannableText());
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.WHITE);
        chart.setTransparentCircleColor(Color.WHITE);
        chart.setTransparentCircleAlpha(110);
        chart.setHoleRadius(58f);
        chart.setTransparentCircleRadius(61f);
        chart.setDrawCenterText(true);
        chart.setRotationEnabled(true);
        chart.setHighlightPerTapEnabled(true);
        //chart.setMaxAngle(180f); // HALF CHART
        //chart.setRotationAngle(180f);
        chart.setCenterTextOffset(0, -20);
        chart.animateY(1400, Easing.EaseInOutQuad);
        //chart.setDescription("Subject-wise attendance percentage");
        Legend l = chart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        l.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        l.setDrawInside(false);
        l.setXEntrySpace(7f);
        l.setYEntrySpace(0f);
        l.setYOffset(0f);
        // entry label styling
        chart.setEntryLabelColor(Color.WHITE);
        //chart.setEntryLabelTypeface(tfRegular);
        chart.setEntryLabelTextSize(12f);
        getBatches();
    }

    private void getBatches() {
        if(batchIdList.size() > 0) {
            if(!pDialog.isShowing()){
                pDialog.show();
            }
            if (batchList.size() != 0) {
                batchList.clear();
            }
            Set<String> setBatch = new LinkedHashSet<String>(batchIdList);
            for (String x : setBatch) {
                //batchIdList.add(batchFacultyList.get(i).getBatchId());
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
                                    //Spinner set up
                                    if (batchList.size() != 0) {
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
                                        List<String> batchNameList = new ArrayList<>();
                                        for (Batch batch : batchList) {
                                            batchNameList.add(batch.getName());
                                        }
                                        ArrayAdapter<String> batchAdaptor = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, batchNameList);
                                        batchAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        spBatch.setAdapter(batchAdaptor);

                                        spBatch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                            @Override
                                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                selectedBatch = batchList.get(position);
                                                getSubjectOfBatch();
                                                //getAttendancesOfBatch();
                                            }

                                            @Override
                                            public void onNothingSelected(AdapterView<?> parent) {

                                            }
                                        });
                                    } else {
                                    }
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
        }
    }
    private void getSubjectOfBatch(){
        if(!pDialog.isShowing()){
            pDialog.show();
        }
        if(subjectList.size() > 0) {
            subjectList.clear();
        }
        subjectCollectionRef
                .whereEqualTo("batchId", selectedBatch.getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            Subject subject = document.toObject(Subject.class);
                            subject.setId(document.getId());
                            subjectList.add(subject);
                        }
                        if(subjectList.size() > 0){
                            getStudentOfBatch();
                        }else{

                        }
                        if(pDialog.isShowing()){
                            pDialog.dismiss();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(pDialog.isShowing()){
                            pDialog.dismiss();
                        }
                    }
                });
    }
    private void getStudentOfBatch() {
        if(academicYearId != null) {
            if (!pDialog.isShowing()) {
                pDialog.show();
            }
            if (studentList.size() > 0) {
                studentList.clear();
            }
            studentListener = studentCollectionRef
                    .whereEqualTo("academicYearId", academicYearId)
                    .whereEqualTo("currentBatchId", selectedBatch.getId())
                    .whereIn("status", Arrays.asList("A", "F"))
                    .orderBy("createdDate", Query.Direction.ASCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                return;
                            }
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                                Student student = documentSnapshot.toObject(Student.class);
                                student.setId(documentSnapshot.getId());
                                studentList.add(student);
                                System.out.println("student " + student.getId());
                            }
                            if (studentList.size() > 0) {
                                totalStudents = studentList.size();
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
                                getAttendancesOfBatch();
                            } else {

                            }
                            if (pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                        }
                    });
        }
    }
    private  void getAttendancesOfBatch(){
        if(academicYearId != null) {
            if(!pDialog.isShowing()){
                pDialog.show();
            }
            if(attendanceList.size() > 0) {
                attendanceList.clear();
            }
            attendanceListener = attendanceCollectionRef
                    .whereEqualTo("academicYearId", academicYearId)
                    .whereEqualTo("batchId", selectedBatch.getId())
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                return;
                            }
                            if (attendanceList.size() != 0) {
                                attendanceList.clear();
                            }
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                                Attendance attendance = documentSnapshot.toObject(Attendance.class);
                                attendance.setId(documentSnapshot.getId());
                                System.out.println("attendance " + attendance.getStudentId());
                                attendanceList.add(attendance);
                            }
                            System.out.println("attendanceList " + attendanceList.size());
                            if (attendanceList.size() > 0) {
                                rlAttendanceSummary.setVisibility(View.VISIBLE);
                                llNoList.setVisibility(View.GONE);
                                ArrayList<String> subjectNameList = new ArrayList<>();
                                ArrayList<PieEntry> subjectAttendancePercentageList = new ArrayList<>();
                                if (subjectAttendanceList.size() > 0) {
                                    subjectAttendanceList.clear();
                                }
                                for (Subject subject : subjectList) {
                                    int totalAbsent = 0;
                                    int total_attendance_subject = 0;
                                    subjectNameList.add(subject.getName());
                                    System.out.println("Subject Name - " + subject.getName());
                                    int totalPresent = 0;
                                    for (Attendance attendance : attendanceList) {
                                        if (attendance.getSubjectId().equals(subject.getId())) {
                                            total_attendance_subject++;
                                            if (attendance.getStatus().equals("P")) {
                                                totalPresent++;
                                            }
                                        }

                                    }
                                    totalAbsent = total_attendance_subject - totalPresent;
                                    System.out.println("total Absent" + totalAbsent);
                                    float percentage = 0;
                                    int totalClassConducted = 0;
                                    if (totalStudents != 0) {
                                        if (totalPresent != 0) {
                                            percentage = (totalPresent * 100.0f) / total_attendance_subject;
                                            subjectAttendancePercentageList.add(new PieEntry(percentage, subject.getCode()));
                                            System.out.println(percentage + "  " + subject.getName());
                                            totalClassConducted = total_attendance_subject / totalStudents;
                                        }
                                    }
                                    SubjectAttendance subjectAttendance = new SubjectAttendance();
                                    subjectAttendance.subjectCode = subject.getCode();
                                    subjectAttendance.subjectName = subject.getName();
                                    subjectAttendance.totalSubjectClass = "" + totalClassConducted;
                                    subjectAttendanceList.add(subjectAttendance);
                                }
                                addSubjectRows();
                                //PieEntry pieEntry = new PieEntry(subjectAttendancePercentageList);
                                PieDataSet dataSet = new PieDataSet(subjectAttendancePercentageList, "- Subjects");
                                dataSet.setSliceSpace(3f);
                                dataSet.setSelectionShift(5f);
                                dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                                //dataSet.setSelectionShift(0f);

                                PieData data = new PieData(dataSet);
                                data.setValueFormatter(new PercentFormatter());
                                data.setValueTextSize(11f);
                                data.setValueTextColor(Color.WHITE);
                                chart.setData(data);
                                chart.getDescription().setText("Subjects wise attendance summary");

                                chart.invalidate();
                            } else {
                                rlAttendanceSummary.setVisibility(View.GONE);
                                llNoList.setVisibility(View.VISIBLE);
                            }
                            if (pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                        }
                    });
        }
    }
    private void addSubjectRows() {
        if(!pDialog.isShowing()){
            pDialog.show();
        }
        cleanTable(tblSubject);
        for (SubjectAttendance subjectAttendance:subjectAttendanceList){
            TableRow row = new TableRow(getContext());
            row.setWeightSum(1);

            TextView tvCode = new TextView(getContext());
            tvCode.setBackgroundResource(R.drawable.cell_border_color_primary);
            tvCode.setText(subjectAttendance.subjectCode);
            tvCode.setPadding(5,5,5,5);
            row.addView(tvCode);

            TextView tvName = new TextView(getContext());
            /*
            LinearLayout.LayoutParams Param = new LinearLayout.LayoutParams
                    (LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT,1f);*/
            tvName.setText(subjectAttendance.subjectName);
            tvName.setBackgroundResource(R.drawable.cell_border_color_primary);
            tvName.setPadding(5,5,5,5);
            //tvName.setLayoutParams(Param);
            row.addView(tvName);

            TextView tvTotal = new TextView(getContext());
            tvTotal.setBackgroundResource(R.drawable.cell_border_color_primary);
            tvTotal.setText(subjectAttendance.totalSubjectClass);
            tvTotal.setPadding(5,5,5,5);
            // tvTotal.setLayoutParams(Param);
            row.addView(tvTotal);

            tblSubject.addView(row);
        }
        if(pDialog.isShowing()){
            pDialog.dismiss();
        }
    }
    private void cleanTable(TableLayout table) {

        int childCount = table.getChildCount();

        // Remove all rows except the first one
        if (childCount > 1) {
            table.removeViews(1, childCount - 1);
        }
    }
}
