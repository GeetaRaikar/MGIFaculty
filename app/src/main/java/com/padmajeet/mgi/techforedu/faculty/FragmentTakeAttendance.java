package com.padmajeet.mgi.techforedu.faculty;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.card.MaterialCardView;
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

import com.padmajeet.mgi.techforedu.faculty.model.Attendance;
import com.padmajeet.mgi.techforedu.faculty.model.Batch;
import com.padmajeet.mgi.techforedu.faculty.model.Period;
import com.padmajeet.mgi.techforedu.faculty.model.Staff;
import com.padmajeet.mgi.techforedu.faculty.model.Student;
import com.padmajeet.mgi.techforedu.faculty.model.Subject;
import com.padmajeet.mgi.techforedu.faculty.model.TimeTable;
import com.padmajeet.mgi.techforedu.faculty.util.SessionManager;
import com.padmajeet.mgi.techforedu.faculty.util.Utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import cn.pedant.SweetAlert.SweetAlertDialog;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentTakeAttendance extends Fragment {

    private View view=null;
    private RecyclerView rvClasses,rvTimetable;
    private LinearLayout llNoList;
    private GridView gvStudents;
    private MaterialCardView cvAttendanceSummary;
    private PieChart chart;
    private Button btnSave,btnUpdate;
    private Gson gson;
    private Staff loggedInUser;
    private String loggedInUserId,academicYearId,instituteId;
    private Batch selectedBatch,batch;
    private Subject subject;
    private Period period;
    private TextView tvAttendanceDateTaken,tvNoDataForTimeTable;
    private int presentStudents =0;
    private int totalStudents = 0;
    private FrameLayout flStudents;
    private LinearLayout llButtons;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference studentCollectionRef = db.collection("Student");
    private CollectionReference batchCollectionRef = db.collection("Batch");
    private CollectionReference subjectCollectionRef = db.collection("Subject");
    private CollectionReference periodCollectionRef = db.collection("Period");
    private CollectionReference timeTableCollectionRef = db.collection("TimeTable");
    private CollectionReference attendanceCollectionRef = db.collection("Attendance");
    private ListenerRegistration studentListener;
    private ListenerRegistration attendanceListener;
    private SweetAlertDialog pDialog;
    private List<Batch> batchList = new ArrayList<>();
    private List<Student> studentList = new ArrayList<>();
    private List<Subject> subjectList = new ArrayList<>();
    private List<Period> periodList = new ArrayList<>();
    private List<Attendance> attendanceList = new ArrayList<>();
    private List<StudentAttendance> studentAttendanceList = new ArrayList<>();
    private boolean isAttendanceTaken;
    private int selectedClassPos = 0;
    private int selectedSectionPos = 0;
    private SessionManager sessionManager;
    private List<String> batchIdList=new ArrayList<>();
    private List<String> sectionIdList=new ArrayList<>();
    private List<TimeTable> timeTableList=new ArrayList<>();
    private List<TimeTableForPeriod> timeTableForPeriodArrayList = new ArrayList<>();
    private TimeTable timeTable;
    private Date selectedDate;
    private HorizontalCalendar horizontalCalendar;
    private TimeTableForPeriod selectedTimeTableForPeriod;
    private int []squares = {R.drawable.square_blue_filled,R.drawable.square_brown_filled,R.drawable.square_primary_filled,R.drawable.square_pink_filled,R.drawable.square_orange_filled};


    class StudentAttendance{
        public Student student;
        public Attendance attendance;
        public StudentAttendance(Student s, Attendance a){
            this.student = s;
            this.attendance = a;
        }
    }
    public class TimeTableForPeriod{
        private String subject;
        private String period;
        private String subjectId;
        private String periodId;
        private int durationInMin;
        private String fromTime;
        private String toTime;
        private String id;

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }

        public String getPeriod() {
            return period;
        }

        public void setPeriod(String period) {
            this.period = period;
        }

        public String getSubjectId() {
            return subjectId;
        }

        public void setSubjectId(String subjectId) {
            this.subjectId = subjectId;
        }

        public String getPeriodId() {
            return periodId;
        }

        public void setPeriodId(String periodId) {
            this.periodId = periodId;
        }

        public int getDurationInMin() {
            return durationInMin;
        }

        public void setDurationInMin(int durationInMin) {
            this.durationInMin = durationInMin;
        }

        public String getFromTime() {
            return fromTime;
        }

        public void setFromTime(String fromTime) {
            this.fromTime = fromTime;
        }

        public String getToTime() {
            return toTime;
        }

        public void setToTime(String toTime) {
            this.toTime = toTime;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

    }

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

    public FragmentTakeAttendance() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_take_attendance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.takeAttendance));
        llNoList = view.findViewById(R.id.llNoList);
        rvTimetable = view.findViewById(R.id.rvTimetable);
        rvClasses = view.findViewById(R.id.rvClasses);
        gvStudents = view.findViewById(R.id.gvStudents);
        btnSave = view.findViewById(R.id.btnSave);
        btnUpdate = view.findViewById(R.id.btnUpdate);
        cvAttendanceSummary = view.findViewById(R.id.cvAttendanceSummary);
        tvAttendanceDateTaken= view.findViewById(R.id.tvAttendanceDateTaken);
        chart = view.findViewById(R.id.chartAttendanceSummary);
        flStudents = view.findViewById(R.id.flStudents);
        llButtons = view.findViewById(R.id.llButtons);
        tvNoDataForTimeTable = view.findViewById(R.id.tvNoDataForTimeTable);

        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.YEAR, 1);
        Calendar startDate = Calendar.getInstance();
        startDate.add(Calendar.YEAR, -1);
        final Calendar defaultDate = Calendar.getInstance();
        //defaultDate.add(Calendar.YEAR, -2);
        //first time loading fragment
        defaultDate.set(Calendar.MILLISECOND, 0);
        defaultDate.set(Calendar.SECOND, 0);
        defaultDate.set(Calendar.MINUTE, 0);
        defaultDate.set(Calendar.HOUR, 0);
        defaultDate.set(Calendar.HOUR_OF_DAY,0);
        selectedDate = defaultDate.getTime();
        horizontalCalendar = new HorizontalCalendar.Builder(getActivity(), R.id.dateView)
                .range(startDate, endDate)
                .datesNumberOnScreen(5)   // Number of Dates cells shown on screen (default to 5).
                .configure()    // starts configuration.
                .formatTopText("MMM")   // default to "MMM".
                .formatMiddleText("dd") // default to "dd".
                .formatBottomText("EEE")  // default to "EEE".
                .textSize(10f, 18f, 10f)
                //.textColor(Color.GRAY, Color.BLUE)    // default to (Color.GRAY, Color.WHITE)
                .colorTextTop(Color.GRAY, Color.GRAY)
                .colorTextMiddle(Color.GRAY, Color.BLUE)
                .colorTextBottom(Color.GRAY, Color.GRAY)
                .showTopText(true)  // show or hide TopText (default to true).
                .showBottomText(true)   // show or hide BottomText (default to true).
                .selectorColor(Color.TRANSPARENT)               // set selection indicator bar's color (default to colorAccent).
                .end()          // ends configuration.
                .defaultSelectedDate(defaultDate)    // Date to be selected at start (default to current day `Calendar.getInstance()`).
                .build();


        horizontalCalendar.setCalendarListener(new HorizontalCalendarListener() {
            @Override
            public void onDateSelected(Calendar date, int position) {
                //Toast.makeText(getContext(), DateFormat.getDateInstance().format(date) + " is selected!", Toast.LENGTH_SHORT).show();
                selectedDate = date.getTime();
                System.out.println("selectedDate "+selectedDate);
                getTimeTableOfBatchWithDate();
                //getAttendanceOfTimeTableForDate();
            }

            @Override
            public void onCalendarScroll(HorizontalCalendarView calendarView,
                                         int dx, int dy) {

            }

            @Override
            public boolean onDateLongClicked(Calendar date, int position) {
                return true;
            }
        });

        System.out.println("selectedDate "+selectedDate);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitAttendance();
            }
        });
        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateAttendance();
            }
        });
        llNoList = view.findViewById(R.id.llNoList);
        rvClasses.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        rvTimetable.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        getBatches();
    }
    private void getTimeTableOfBatchWithDate() {
        if(academicYearId != null && loggedInUserId != null) {
            timeTableCollectionRef
                    .whereEqualTo("academicYearId", academicYearId)
                    .whereEqualTo("staffId", loggedInUserId)
                    .whereEqualTo("batchId", selectedBatch.getId())
                    .whereEqualTo("date", selectedDate)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (timeTableList.size() > 0) {
                                timeTableList.clear();
                            }
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                timeTable = document.toObject(TimeTable.class);
                                timeTable.setId(document.getId());
                                timeTableList.add(timeTable);
                            }
                            System.out.println("timeTableList => " + timeTableList.size());
                            if (timeTableList.size() > 0) {
                                rvTimetable.setVisibility(View.VISIBLE);
                                tvNoDataForTimeTable.setVisibility(View.GONE);
                                if (timeTableForPeriodArrayList.size() > 0) {
                                    timeTableForPeriodArrayList.clear();
                                }
                                for (TimeTable t : timeTableList) {
                                    TimeTableForPeriod timeTableForPeriod = new TimeTableForPeriod();
                                    timeTableForPeriod.setId(t.getId());
                                    timeTableForPeriod.setPeriodId(t.getPeriodId());
                                    for (Period p : periodList) {
                                        if (p.getId().equals(t.getPeriodId())) {
                                            timeTableForPeriod.setPeriod(p.getNumber());
                                            timeTableForPeriod.setDurationInMin(p.getDuration());
                                            timeTableForPeriod.setFromTime(p.getFromTime());
                                            timeTableForPeriod.setToTime(p.getToTime());
                                            break;
                                        }
                                    }
                                    timeTableForPeriod.setSubjectId(t.getSubjectId());
                                    for (Subject s : subjectList) {
                                        if (s.getId().equals(t.getSubjectId())) {
                                            timeTableForPeriod.setSubject(s.getName());
                                            break;
                                        }
                                    }
                                    timeTableForPeriodArrayList.add(timeTableForPeriod);
                                }
                                System.out.println("timeTableForPeriodArrayList => " + timeTableForPeriodArrayList.size());

                                Collections.sort(timeTableForPeriodArrayList, new Comparator<TimeTableForPeriod>() {
                                    @Override
                                    public int compare(TimeTableForPeriod t1, TimeTableForPeriod t2) {
                                        try {
                                            return new SimpleDateFormat("hh:mm a").parse(t1.getFromTime()).compareTo(new SimpleDateFormat("hh:mm a").parse(t2.getFromTime()));
                                        } catch (ParseException e) {
                                            return 0;
                                        }
                                    }
                                });
                                for (int i = 0; i < timeTableForPeriodArrayList.size(); i++) {
                                    System.out.println("fromTime " + timeTableForPeriodArrayList.get(i).getFromTime());
                                }
                                cvAttendanceSummary.setVisibility(View.GONE);
                                tvAttendanceDateTaken.setVisibility(View.VISIBLE);
                                flStudents.setVisibility(View.VISIBLE);
                                rvTimetable.setVisibility(View.VISIBLE);
                                TimeTableAdapter timeTableAdapter = new TimeTableAdapter();
                                rvTimetable.setAdapter(timeTableAdapter);
                                selectedTimeTableForPeriod = timeTableForPeriodArrayList.get(0);
                                getAttendanceOfTimeTableForDate();
                            } else {
                                rvTimetable.setVisibility(View.GONE);
                                tvNoDataForTimeTable.setVisibility(View.VISIBLE);
                                flStudents.setVisibility(View.GONE);
                                tvAttendanceDateTaken.setVisibility(View.GONE);
                                cvAttendanceSummary.setVisibility(View.GONE);
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            pDialog.dismiss();
                        }
                    });
        }
    }
    private void getAttendanceOfTimeTableForDate(){
        if(academicYearId != null) {
            if(attendanceList.size() > 0) {
                attendanceList.clear();
            }
            attendanceListener = attendanceCollectionRef
                    .whereEqualTo("academicYearId", academicYearId)
                    .whereEqualTo("batchId", selectedBatch.getId())
                    .whereEqualTo("subjectId", selectedTimeTableForPeriod.getSubjectId())
                    .whereEqualTo("periodId", selectedTimeTableForPeriod.getPeriodId())
                    .whereEqualTo("date", selectedDate)
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
                                isAttendanceTaken = true;
                                tvAttendanceDateTaken.setText(R.string.attendance_taken_cap);
                                btnSave.setVisibility(View.GONE);
                                btnUpdate.setVisibility(View.VISIBLE);
                            } else {
                                isAttendanceTaken = false;
                                tvAttendanceDateTaken.setText(R.string.mark_attendance_cap);
                                btnSave.setVisibility(View.VISIBLE);
                                btnUpdate.setVisibility(View.GONE);
                            }
                            System.out.println("studentList " + studentList.size());
                            studentAttendanceOfBatch();
                        }
                    });
        }
    }
    private void getStudentOfBatch() {
        cvAttendanceSummary.setVisibility(View.GONE);
        flStudents.setVisibility(View.VISIBLE);
        gvStudents.setVisibility(View.VISIBLE);
        if(studentList.size() > 0) {
            studentList.clear();
        }
        if(academicYearId != null) {
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
                        }
                    });
        }
    }
    private void studentAttendanceOfBatch(){
        if(studentAttendanceList.size() > 0) {
            studentAttendanceList.clear();
        }
        if(!isAttendanceTaken) {
            for(Student student:studentList) {
                Attendance tempAttendance = new Attendance();
                tempAttendance.setAcademicYearId(academicYearId);
                tempAttendance.setBatchId(selectedBatch.getId());
                tempAttendance.setSectionId(student.getCurrentSectionId());
                tempAttendance.setSubjectId(selectedTimeTableForPeriod.getSubjectId());
                tempAttendance.setPeriodId(selectedTimeTableForPeriod.getPeriodId());
                tempAttendance.setStudentId(student.getId());
                tempAttendance.setDate(selectedDate);
                tempAttendance.setLate(false);
                tempAttendance.setLateTimeInMin(0);
                tempAttendance.setInstituteId(instituteId);
                tempAttendance.setStatus("P");
                tempAttendance.setCreatorId(loggedInUserId);
                tempAttendance.setCreatorType("A");
                tempAttendance.setModifierId(loggedInUserId);
                tempAttendance.setModifierType("A");
                studentAttendanceList.add(new StudentAttendance(student, tempAttendance));
            }
        }
        else {
            for (Attendance attendance:attendanceList){
                for(Student student:studentList){
                    if(attendance.getStudentId().equals(student.getId())){
                        studentAttendanceList.add(new StudentAttendance(student,attendance));
                        break;
                    }
                }
            }
        }
        if(studentAttendanceList.size()!=0) {
            StudentAdapter studentAdapter = new StudentAdapter(getContext(),studentAttendanceList);
            //sorting student name
            Collections.sort(studentAttendanceList, new Comparator<StudentAttendance>() {
                @Override
                public int compare(StudentAttendance sa1, StudentAttendance sa2) {
                    try {
                        return sa1.student.getFirstName().compareTo(sa2.student.getFirstName());
                    } catch (android.net.ParseException e) {
                        return 0;
                    }
                }
            });
            gvStudents.setAdapter(studentAdapter);
            llNoList.setVisibility(View.GONE);
            gvStudents.setVisibility(View.VISIBLE);
            llButtons.setVisibility(View.VISIBLE);
        }else {
            gvStudents.setVisibility(View.GONE);
            llNoList.setVisibility(View.VISIBLE);
            llButtons.setVisibility(View.GONE);
        }
    }
    private void getBatches() {
        if(batchIdList.size() > 0) {
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
                                if (setBatch.size() == batchList.size()) {
                                    if (pDialog != null) {
                                        pDialog.dismiss();
                                    }
                                    //Spinner set up
                                    if (batchList.size() != 0) {
                                        System.out.println("batchList " + batchList.size());
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
                                        BatchAdapter batchAdapter = new BatchAdapter();
                                        rvClasses.setAdapter(batchAdapter);
                                        selectedBatch = batchList.get(0);
                                        getSubjectPeriodOfBatch();
                                        getStudentOfBatch();
                                    } else {
                                        rvClasses.setVisibility(View.GONE);
                                        rvTimetable.setVisibility(View.GONE);
                                        llNoList.setVisibility(View.VISIBLE);
                                    }
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
    private void getSubjectPeriodOfBatch(){
        if(pDialog!=null && !pDialog.isShowing()){
            pDialog.show();
        }
        subjectCollectionRef
                .whereEqualTo("batchId", selectedBatch.getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        if(subjectList.size() > 0) {
                            subjectList.clear();
                        }
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            subject = document.toObject(Subject.class);
                            subject.setId(document.getId());
                            subjectList.add(subject);
                        }
                        System.out.println("subjectList "+subjectList.size());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                    }
                });
        if(pDialog!=null && !pDialog.isShowing()){
            pDialog.show();
        }
        periodCollectionRef
                .whereEqualTo("batchId", selectedBatch.getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        if(periodList.size() > 0) {
                            periodList.clear();
                        }
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            period = document.toObject(Period.class);
                            period.setId(document.getId());
                            periodList.add(period);
                        }
                        System.out.println("periodList "+periodList.size());
                        getTimeTableOfBatchWithDate();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                    }
                });

    }
    private void submitAttendance() {
        presentStudents = 0;
        totalStudents = studentAttendanceList.size();
        for(StudentAttendance studentAttendance:studentAttendanceList){
            Attendance attendance = studentAttendance.attendance;
            if(attendance.getStatus().equals("P")){
                presentStudents++;
            }
            attendanceCollectionRef.add(attendance);
        }
        SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Success")
                .setContentText("Attendance marked successfully")
                .setConfirmText("Ok")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        tvAttendanceDateTaken.setText(R.string.attendanceSummary);
                        displayChart();
                    }
                });
        dialog.setCancelable(false);
        dialog.show();

    }
    private void displayChart(){

        chart.clear();
        chart.setDrawHoleEnabled(true);
        chart.setHoleColor(Color.WHITE);

        chart.setTransparentCircleColor(Color.WHITE);
        chart.setTransparentCircleAlpha(110);

        chart.setHoleRadius(58f);
        chart.setTransparentCircleRadius(61f);

        chart.setDrawCenterText(true);

        chart.setRotationEnabled(true);
        chart.setHighlightPerTapEnabled(true);

        chart.setMaxAngle(180f); // HALF CHART
        chart.setRotationAngle(180f);
        chart.setCenterTextOffset(0, -20);

        chart.animateY(1400, Easing.EaseInOutQuad);
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

        flStudents.setVisibility(View.GONE);
        cvAttendanceSummary.setVisibility(View.VISIBLE);

        int absentStudents = totalStudents - presentStudents;

        ArrayList<PieEntry> attendanceList = new ArrayList<>();
        attendanceList.add(new PieEntry(presentStudents,"Presentees"));
        attendanceList.add(new PieEntry(absentStudents,"Absentees"));

        PieDataSet dataSet = new PieDataSet(attendanceList, "- Attendance");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);
        dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
        //dataSet.setSelectionShift(0f);

        PieData data = new PieData(dataSet);
        //data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        chart.setData(data);

        chart.invalidate();
    }
    private void updateAttendance() {
        presentStudents = 0;
        totalStudents = studentAttendanceList.size();
        for(StudentAttendance studentAttendance:studentAttendanceList){
            Attendance attendance = studentAttendance.attendance;
            if(attendance.getStatus().equals("P")){
                presentStudents++;
            }
            attendanceCollectionRef.document(attendance.getId()).set(attendance);
        }
        SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Success")
                .setContentText("Attendance updated successfully")
                .setConfirmText("Ok")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        displayChart();
                    }
                });
        dialog.setCancelable(false);
        dialog.show();

    }
    class StudentAdapter extends ArrayAdapter<StudentAttendance> {
        Context context;
        List<StudentAttendance> studentAttendanceList;
        public StudentAdapter(@NonNull Context context,  @NonNull List<StudentAttendance> objects) {
            super(context, R.layout.row_student, objects);
            this.context = context;
            this.studentAttendanceList = objects;
        }

        @Override
        public int getCount() {
            return studentAttendanceList.size();
        }

        @Override
        public StudentAttendance getItem(int position) {
            return studentAttendanceList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final Student student = studentAttendanceList.get(position).student;
            final Attendance attendance = studentAttendanceList.get(position).attendance;
            View row ;

            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.row_student_attendance,parent,false);
            TextView tvFirstName = row.findViewById(R.id.tvFirstName);
            TextView tvLastName = row.findViewById(R.id.tvLastName);
            ImageView ivProfilePic = row.findViewById(R.id.ivProfilePic);
            final ImageButton ibChoosePhoto = row.findViewById(R.id.ibChoosePhoto);
            int profileDrawable=R.drawable.ic_female_01;

            tvFirstName.setText(""+student.getFirstName());
            tvLastName.setText(""+student.getLastName());
            if(student.getGender().equalsIgnoreCase("Male")){
                profileDrawable=R.drawable.ic_male_01;
            }
            if(!TextUtils.isEmpty(student.getImageUrl())) {
                Glide.with(getContext())
                        .load(student.getImageUrl())
                        .fitCenter()
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(profileDrawable)
                        .into(ivProfilePic);
            }else{
                Glide.with(getContext())
                        .load(profileDrawable)
                        .fitCenter()
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(profileDrawable)
                        .into(ivProfilePic);
            }

            if(attendance.getStatus().equalsIgnoreCase("P")){
                ibChoosePhoto.setVisibility(View.VISIBLE);
            }
            else if(attendance.getStatus().equalsIgnoreCase("A")){
                ibChoosePhoto.setVisibility(View.GONE);
            }

            row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if(ibChoosePhoto.getVisibility()== View.VISIBLE){
                        ibChoosePhoto.setVisibility(View.GONE);
                        attendance.setStatus("A");
                    }
                    else{
                        ibChoosePhoto.setVisibility(View.VISIBLE);
                        attendance.setStatus("P");
                    }
                }
            });
            return row;
        }

    }
    class BatchAdapter extends RecyclerView.Adapter<BatchAdapter.MyViewHolder> {

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvClassName;
            private ImageView ivClassPic;
            private LinearLayout llImage;
            private View row;
            public MyViewHolder(View view) {
                super(view);
                row = view;
                tvClassName = view.findViewById(R.id.tvClassName);
                ivClassPic = view.findViewById(R.id.ivClassPic);
                llImage = view.findViewById(R.id.llImage);
            }
        }


        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_class, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            final Batch batch = batchList.get(holder.getAdapterPosition());
            holder.tvClassName.setText("" + batch.getName());
            //System.out.println("Image path" + url);
            if (!TextUtils.isEmpty(batch.getImageUrl())) {
                Glide.with(getContext())
                        .load(batch.getImageUrl())
                        .fitCenter()
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.no_image)
                        .into(holder.ivClassPic);
            }
            if(selectedClassPos == holder.getAdapterPosition()){
                holder.tvClassName.setTextColor(getResources().getColor(R.color.colorGreen));
                holder.llImage.setBackground(getResources().getDrawable(R.drawable.circle_green));
            }
            else{
                holder.tvClassName.setTextColor(getResources().getColor(R.color.colorBlack));
                holder.llImage.setBackground(null);
            }
            holder.row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedClassPos = holder.getAdapterPosition();
                    selectedBatch = batch;
                    cvAttendanceSummary.setVisibility(View.GONE);
                    getSubjectPeriodOfBatch();
                    getStudentOfBatch();
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return batchList.size();
        }
    }
    class TimeTableAdapter extends RecyclerView.Adapter<TimeTableAdapter.MyViewHolder> {

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvSubjectName, tvPeriodName;
            private LinearLayout llImage,llSelected;
            private View row;
            public MyViewHolder(View view) {
                super(view);
                row = view;
                tvSubjectName = view.findViewById(R.id.tvSubjectName);
                tvPeriodName = view.findViewById(R.id.tvPeriodName);
                llImage = view.findViewById(R.id.llImage);
                llSelected = view.findViewById(R.id.llSelected);
            }
        }


        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_section, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            final TimeTableForPeriod timeTableForPeriod = timeTableForPeriodArrayList.get(holder.getAdapterPosition());
            holder.tvSubjectName.setText("" + timeTableForPeriod.getSubject());
            holder.tvPeriodName.setText("" + timeTableForPeriod.getPeriod());
            int colorCode = holder.getAdapterPosition()%5;
            holder.llImage.setBackground(getResources().getDrawable(squares[colorCode]));
            if(selectedSectionPos==holder.getAdapterPosition()){
                holder.llSelected.setVisibility(View.VISIBLE);
            }
            else {
                holder.llSelected.setVisibility(View.GONE);
            }
            holder.row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    selectedSectionPos = holder.getAdapterPosition();
                    selectedTimeTableForPeriod = timeTableForPeriod;
                    getAttendanceOfTimeTableForDate();
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return timeTableForPeriodArrayList.size();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(studentListener != null){
            studentListener.remove();
        }
        if(attendanceListener != null){
            attendanceListener.remove();
        }
    }
}
