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
import java.util.Locale;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentTimeTable extends Fragment {
    private View view = null;
    private Spinner spBatch;
    private LinearLayout llNoList;
    private ArrayList<TimeTable> timeTableList = new ArrayList<>();
    private ArrayList<Staff> staffList = new ArrayList<>();
    private ArrayList<Subject> subjectList = new ArrayList<>();
    private ArrayList<Period> periodList = new ArrayList<>();
    private TimeTable timeTable;
    private Subject subject;
    private Period period;
    private Staff staff;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference batchCollectionRef = db.collection("Batch");
    private CollectionReference timeTableCollectionRef = db.collection("TimeTable");
    private CollectionReference staffCollectionRef = db.collection("Staff");
    private CollectionReference subjectCollectionRef = db.collection("Subject");
    private CollectionReference periodCollectionRef = db.collection("Period");
    private RecyclerView rvTimeTable;
    private RecyclerView.Adapter timeTableAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private Staff loggedInUser;
    private String instituteId;
    private String academicYearId;
    private Gson gson;
    private SweetAlertDialog pDialog;
    private SessionManager sessionManager;
    private ListenerRegistration timeTableListener;
    private Date selectedDate;
    private HorizontalCalendar horizontalCalendar;
    private TextView currentMonthTextView;
    private List<String> batchIdList = new ArrayList<>();
    private List<String> subjectIdList = new ArrayList<>();
    private List<Batch> batchList = new ArrayList<>();
    private Batch batch, selectedBatch=null;

    public class TimeTableForPeriod{
        private String subject;
        private String staff;
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

        public String getStaff() {
            return staff;
        }

        public void setStaff(String staff) {
            this.staff = staff;
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

    @Override
    public void onStart() {
        super.onStart();

        if(instituteId != null) {

            pDialog=Utility.createSweetAlertDialog(getContext());
            if(pDialog!=null && !pDialog.isShowing()){
                pDialog.show();
            }
            staffCollectionRef
                    .whereEqualTo("instituteId", instituteId)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (pDialog != null) {
                                pDialog.dismiss();
                            }
                            if (staffList.size() > 0) {
                                staffList.clear();
                            }
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                staff = document.toObject(Staff.class);
                                staff.setId(document.getId());
                                staffList.add(staff);
                            }
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
        String userJson = sessionManager.getString("loggedInUser");
        loggedInUser = gson.fromJson(userJson, Staff.class);
        instituteId = sessionManager.getString("instituteId");
        academicYearId= sessionManager.getString("academicYearId");
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

    public FragmentTimeTable() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_time_table, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.timeTable));
        llNoList = view.findViewById(R.id.llNoList);
        rvTimeTable = view.findViewById(R.id.rvTimeTable);
        spBatch = view.findViewById(R.id.spBatch);
        layoutManager = new LinearLayoutManager(getContext());
        rvTimeTable.setLayoutManager(layoutManager);
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
                System.out.println("position "+position);
                System.out.println("selectedDate "+selectedDate);
                getTimeTableOfBatch();
            }

            @Override
            public void onCalendarScroll(HorizontalCalendarView calendarView, int dx, int dy) {

            }

            @Override
            public boolean onDateLongClicked(Calendar date, int position) {
                return true;
            }
        });
        System.out.println("selectedDate "+selectedDate);

        getBatches();

        spBatch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedBatch = batchList.get(position);
                getSubjectPeriodOfBatch();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if(batchList.size() > 0) {
                    selectedBatch = batchList.get(0);
                    getSubjectPeriodOfBatch();
                }else{
                    getSubjectPeriodOfBatch();
                }
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
                                    System.out.println("batchList " + batchList.size());
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
                                        spBatch.setVisibility(View.VISIBLE);

                                        List<String> batchNameList = new ArrayList<>();
                                        for (Batch batch : batchList) {
                                            batchNameList.add(batch.getName());
                                        }
                                        ArrayAdapter<String> batchAdaptor = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, batchNameList);
                                        batchAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        spBatch.setAdapter(batchAdaptor);
                                        //selectedBatch=batchList.get(0);
                                        //getSubjectPeriodOfBatch();
                                    } else {
                                        spBatch.setVisibility(View.GONE);
                                        rvTimeTable.setVisibility(View.GONE);
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
        if(selectedBatch != null) {
            if (!pDialog.isShowing()) {
                pDialog.show();
            }
            if (subjectList.size() > 0) {
                subjectList.clear();
            }
            subjectCollectionRef
                    .whereEqualTo("batchId", selectedBatch.getId())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                subject = document.toObject(Subject.class);
                                subject.setId(document.getId());
                                subjectList.add(subject);
                            }
                            System.out.println("subjectList " + subjectList.size());
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
            if (!pDialog.isShowing()) {
                pDialog.show();
            }
            if (periodList.size() > 0) {
                periodList.clear();
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
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                period = document.toObject(Period.class);
                                period.setId(document.getId());
                                periodList.add(period);
                            }
                            System.out.println("periodList " + periodList.size());
                            getTimeTableOfBatch();
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

    }
    private void getTimeTableOfBatch() {
        if(academicYearId != null) {
            timeTableCollectionRef
                    .whereEqualTo("academicYearId", academicYearId)
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
                                    for (Staff st : staffList) {
                                        if (st.getId().equals(t.getStaffId())) {
                                            String name = st.getFirstName();
                                            if (!TextUtils.isEmpty(st.getLastName())) {
                                                name = name + " " + st.getLastName();
                                            }
                                            timeTableForPeriod.setStaff(name);
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
                                timeTableAdapter = new TimeTableAdapter(timeTableForPeriodArrayList);
                                rvTimeTable.setAdapter(timeTableAdapter);
                                rvTimeTable.setVisibility(View.VISIBLE);
                                llNoList.setVisibility(View.GONE);
                            } else {
                                rvTimeTable.setVisibility(View.GONE);
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
    class TimeTableAdapter extends RecyclerView.Adapter<TimeTableAdapter.MyViewHolder> {
        private List<TimeTableForPeriod> timeTableForPeriodArrayList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvSubject,tvStaff,tvPeriod,tvTime,tvDuration;
            public MyViewHolder(View view) {
                super(view);
                tvSubject = view.findViewById(R.id.tvSubject);
                tvStaff = view.findViewById(R.id.tvStaff);
                tvPeriod = view.findViewById(R.id.tvPeriod);
                tvTime = view.findViewById(R.id.tvTime);
                tvDuration = view.findViewById(R.id.tvDuration);
            }
        }

        public TimeTableAdapter(List<TimeTableForPeriod> timeTableForPeriodArrayList) {
            this.timeTableForPeriodArrayList = timeTableForPeriodArrayList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_time_table, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final TimeTableForPeriod timeTableForPeriod = timeTableForPeriodArrayList.get(position);
            holder.tvPeriod.setText("" + timeTableForPeriod.getPeriod());
            holder.tvDuration.setText("" + timeTableForPeriod.getDurationInMin());
            holder.tvTime.setText("" + timeTableForPeriod.getFromTime()+" - "+timeTableForPeriod.getToTime());
            holder.tvSubject.setText("" + timeTableForPeriod.getSubject());
            holder.tvStaff.setText("" + timeTableForPeriod.getStaff());
        }

        @Override
        public int getItemCount() {
            return timeTableForPeriodArrayList.size();
        }
    }

}
