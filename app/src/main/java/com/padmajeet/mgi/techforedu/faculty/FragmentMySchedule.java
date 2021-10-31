package com.padmajeet.mgi.techforedu.faculty;


import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;
import devs.mulham.horizontalcalendar.HorizontalCalendar;
import devs.mulham.horizontalcalendar.HorizontalCalendarView;
import devs.mulham.horizontalcalendar.utils.HorizontalCalendarListener;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.padmajeet.mgi.techforedu.faculty.model.Batch;
import com.padmajeet.mgi.techforedu.faculty.model.Period;
import com.padmajeet.mgi.techforedu.faculty.model.Staff;
import com.padmajeet.mgi.techforedu.faculty.model.Subject;
import com.padmajeet.mgi.techforedu.faculty.model.TimeTable;
import com.padmajeet.mgi.techforedu.faculty.util.SessionManager;
import com.padmajeet.mgi.techforedu.faculty.util.Utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentMySchedule extends Fragment {

    private View view = null;
    private LinearLayout llNoList;
    private ArrayList<TimeTable> timeTableList = new ArrayList<>();
    private ArrayList<Subject> subjectList = new ArrayList<>();
    private ArrayList<Period> periodList = new ArrayList<>();
    private TimeTable timeTable;
    private Subject subject;
    private Period period;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference batchCollectionRef = db.collection("Batch");
    private CollectionReference timeTableCollectionRef = db.collection("TimeTable");
    private CollectionReference subjectCollectionRef = db.collection("Subject");
    private CollectionReference periodCollectionRef = db.collection("Period");
    private RecyclerView rvSchedule;
    private RecyclerView.Adapter scheduleAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private Staff loggedInUser;
    private String instituteId,loggedInUserId;
    private String academicYearId;
    private Gson gson;
    private SweetAlertDialog pDialog;
    private SessionManager sessionManager;
    private ListenerRegistration timeTableListener;
    private Date selectedDate;
    private HorizontalCalendar horizontalCalendar;
    private TextView currentMonthTextView;
    private List<String> batchIdList = new ArrayList<>();
    private ArrayList<Batch> batchList = new ArrayList<>();
    private Batch batch, selectedBatch;
    private List<String> subjectIdList;

    public class TimeTableForPeriod{
        private String subject;
        private String batch;
        private String period;
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

        public String getBatch() {
            return batch;
        }

        public void setBatch(String batch) {
            this.batch = batch;
        }

        public String getPeriod() {
            return period;
        }

        public void setPeriod(String period) {
            this.period = period;
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

    public FragmentMySchedule() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(getContext());
        gson = Utility.getGson();
        String userJson = sessionManager.getString("loggedInUser");
        loggedInUser = gson.fromJson(userJson, Staff.class);
        loggedInUserId= sessionManager.getString("loggedInUserId");
        instituteId = sessionManager.getString("instituteId");
        academicYearId= sessionManager.getString("academicYearId");
        String batchIdJson = sessionManager.getString("batchIdList");
        batchIdList = gson.fromJson(batchIdJson,new TypeToken<List<String>>() {
        }.getType());
        String subjectIdJson = sessionManager.getString("subjectIdList");
        subjectIdList = gson.fromJson(subjectIdJson,new TypeToken<List<String>>() {
        }.getType());
        pDialog = Utility.createSweetAlertDialog(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_schedule, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.mySchedule));
        llNoList = view.findViewById(R.id.llNoList);
        rvSchedule = view.findViewById(R.id.rvSchedule);
        layoutManager = new LinearLayoutManager(getContext());
        rvSchedule.setLayoutManager(layoutManager);
        getBatch();
        Calendar endDate = Calendar.getInstance();
        endDate.add(Calendar.MONTH, 1);
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
        horizontalCalendar = new HorizontalCalendar.Builder(getActivity(), R.id.calendarView)
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
                getTimeTableOfDate();
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
    }
    private void getPeriod(){
        if(periodList.size() > 0) {
            periodList.clear();
        }
        periodCollectionRef
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                            period = document.toObject(Period.class);
                            period.setId(document.getId());
                            periodList.add(period);
                        }
                        System.out.println("periodList "+periodList.toString());
                        getTimeTableOfDate();
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
    private void getSubject(){
        if (subjectList.size() != 0) {
            subjectList.clear();
        }
        if(subjectIdList.size() > 0) {
            Set<String> setSubject = new LinkedHashSet<String>(subjectIdList);
            System.out.println("setSubject " + setSubject.toString());
            for (String x : setSubject) {
                subjectCollectionRef.document("/" + x)
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                subject = documentSnapshot.toObject(Subject.class);
                                if (subject != null) {
                                    subject.setId(documentSnapshot.getId());
                                    subjectList.add(subject);
                                }
                                if (setSubject.size() == subjectList.size()) {
                                    if (pDialog != null) {
                                        pDialog.dismiss();
                                    }
                                    getPeriod();
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
    private void getBatch() {
        if(batchIdList.size() > 0) {
            if (batchList.size() != 0) {
                batchList.clear();
            }
            Set<String> setBatch = new LinkedHashSet<String>(batchIdList);
            System.out.println("setBatch " + setBatch.toString());
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
    private void getTimeTableOfDate() {
        if(academicYearId != null && loggedInUserId != null) {
            timeTableCollectionRef
                    .whereEqualTo("academicYearId", academicYearId)
                    .whereEqualTo("staffId", loggedInUserId)
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
                                ArrayList<TimeTableForPeriod> timeTableForPeriodArrayList = new ArrayList<>();
                                for (TimeTable t : timeTableList) {
                                    TimeTableForPeriod timeTableForPeriod = new TimeTableForPeriod();
                                    timeTableForPeriod.setId(t.getId());
                                    for (Period p : periodList) {
                                        if (p.getId().equals(t.getPeriodId())) {
                                            timeTableForPeriod.setPeriod(p.getNumber());
                                            timeTableForPeriod.setDurationInMin(p.getDuration());
                                            timeTableForPeriod.setFromTime(p.getFromTime());
                                            timeTableForPeriod.setToTime(p.getToTime());
                                            break;
                                        }
                                    }
                                    for (Subject s : subjectList) {
                                        if (s.getId().equals(t.getSubjectId())) {
                                            timeTableForPeriod.setSubject(s.getName());
                                            break;
                                        }
                                    }
                                    for (Batch b : batchList) {
                                        if (b.getId().equals(t.getBatchId())) {
                                            timeTableForPeriod.setBatch(b.getName());
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
                                for (TimeTableForPeriod timeTableForPeriod : timeTableForPeriodArrayList) {
                                    System.out.println("fromTime " + timeTableForPeriod.getFromTime());
                                }
                                scheduleAdapter = new ScheduleAdapter(timeTableForPeriodArrayList);
                                rvSchedule.setAdapter(scheduleAdapter);
                                rvSchedule.setVisibility(View.VISIBLE);
                                llNoList.setVisibility(View.GONE);
                            } else {
                                rvSchedule.setVisibility(View.GONE);
                                llNoList.setVisibility(View.VISIBLE);
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
    class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.MyViewHolder> {
        private List<TimeTableForPeriod> timeTableForPeriodArrayList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvSubject,tvBatch,tvPeriod,tvTime,tvDuration;
            public MyViewHolder(View view) {
                super(view);
                tvSubject = view.findViewById(R.id.tvSubject);
                tvBatch = view.findViewById(R.id.tvBatch);
                tvPeriod = view.findViewById(R.id.tvPeriod);
                tvTime = view.findViewById(R.id.tvTime);
                tvDuration = view.findViewById(R.id.tvDuration);
            }
        }

        public ScheduleAdapter(List<TimeTableForPeriod> timeTableForPeriodArrayList) {
            this.timeTableForPeriodArrayList = timeTableForPeriodArrayList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_schedule, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final TimeTableForPeriod timeTableForPeriod = timeTableForPeriodArrayList.get(position);
            holder.tvPeriod.setText("" + timeTableForPeriod.getPeriod());
            holder.tvDuration.setText("" + timeTableForPeriod.getDurationInMin());
            holder.tvTime.setText("" + timeTableForPeriod.getFromTime()+" - "+timeTableForPeriod.getToTime());
            holder.tvSubject.setText("" + timeTableForPeriod.getSubject());
            holder.tvBatch.setText("" + timeTableForPeriod.getBatch());
        }

        @Override
        public int getItemCount() {
            return timeTableForPeriodArrayList.size();
        }
    }
}
