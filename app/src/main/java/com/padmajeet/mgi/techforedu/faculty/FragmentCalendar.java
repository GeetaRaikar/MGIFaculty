package com.padmajeet.mgi.techforedu.faculty;

import android.os.Bundle;
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
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.padmajeet.mgi.techforedu.faculty.model.Batch;
import com.padmajeet.mgi.techforedu.faculty.model.BatchSubjectFaculty;
import com.padmajeet.mgi.techforedu.faculty.model.Calendar;
import com.padmajeet.mgi.techforedu.faculty.model.Staff;
import com.padmajeet.mgi.techforedu.faculty.util.SessionManager;
import com.padmajeet.mgi.techforedu.faculty.util.Utility;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentCalendar extends Fragment {

    private View view = null;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference calendarCollectionRef = db.collection("Calendar");
    private CollectionReference batchSubjectFacultyCollectionRef = db.collection("BatchSubjectFaculty");
    private CollectionReference batchCollectionRef = db.collection("Batch");
    private ListenerRegistration calendarListener;
    private List<Calendar> calendarList = new ArrayList<>();
    private Calendar calendar;
    private List<String> batchIdList = new ArrayList<>();
    private ArrayList<Batch> batchList = new ArrayList<>();
    private Batch batch, selectedBatch;
    private List<BatchSubjectFaculty> batchSubjectFacultyList = new ArrayList<>();
    private BatchSubjectFaculty batchSubjectFaculty;
    private Staff loggedInUser;
    private String academicYearId, loggedInUserId, schoolId;
    private RecyclerView rvCalendar;
    private RecyclerView.Adapter calendarAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private Spinner spBatch;
    private LinearLayout llNoList;
    private Gson gson;
    private SweetAlertDialog pDialog;
    private SessionManager sessionManager;
    int[] circles = {R.drawable.circle_blue_filled, R.drawable.circle_brown_filled, R.drawable.circle_green_filled, R.drawable.circle_pink_filled, R.drawable.circle_orange_filled};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(getContext());
        gson = Utility.getGson();
        String userJson = sessionManager.getString("loggedInUser");
        loggedInUser = gson.fromJson(userJson, Staff.class);
        loggedInUserId = sessionManager.getString("loggedInUserId");
        academicYearId = sessionManager.getString("academicYearId");
        schoolId = sessionManager.getString("schoolId");
        String batchIdJson = sessionManager.getString("batchIdList");
        batchIdList = gson.fromJson(batchIdJson,new TypeToken<List<String>>() {
        }.getType());
        System.out.println("sessionManager batchIdList "+batchIdJson+" value "+batchIdList.toString());
        pDialog = Utility.createSweetAlertDialog(getContext());
    }

    public FragmentCalendar() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_calendar, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.calendar));
        spBatch = view.findViewById(R.id.spBatch);
        rvCalendar = (RecyclerView) view.findViewById(R.id.rvCalendar);
        layoutManager = new LinearLayoutManager(getContext());
        rvCalendar.setLayoutManager(layoutManager);
        llNoList = view.findViewById(R.id.llNoList);

        getBatches();

        spBatch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedBatch = batchList.get(position);
                getCalendarOfBatch();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return view;
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
                                        selectedBatch = batchList.get(0);
                                        getCalendarOfBatch();
                                    } else {
                                        spBatch.setVisibility(View.GONE);
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

    private void getCalendarOfBatch() {
        if(academicYearId != null) {
            if (!pDialog.isShowing()) {
                pDialog.show();
            }
            calendarListener = calendarCollectionRef
                    .whereEqualTo("academicYearId", academicYearId)
                    .whereEqualTo("batchId", selectedBatch.getId())
                    .orderBy("fromDate", Query.Direction.ASCENDING)
                    .orderBy("toDate", Query.Direction.ASCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                return;
                            }
                            if (calendarList.size() != 0) {
                                calendarList.clear();
                            }
                            if (pDialog != null) {
                                pDialog.dismiss();
                            }
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                calendar = document.toObject(Calendar.class);
                                calendar.setId(document.getId());
                                calendarList.add(calendar);
                            }
                            System.out.println("Calendar  -" + calendarList.size());
                            if (calendarList.size() != 0) {
                                calendarAdapter = new CalendarAdapter(calendarList);
                                rvCalendar.setAdapter(calendarAdapter);
                                calendarAdapter.notifyDataSetChanged();
                                rvCalendar.setVisibility(View.VISIBLE);
                                llNoList.setVisibility(View.GONE);
                            } else {
                                rvCalendar.setVisibility(View.GONE);
                                llNoList.setVisibility(View.VISIBLE);
                            }
                        }
                    });
        }

    }

    class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.MyViewHolder> {
        private List<Calendar> calendarList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvEvent, tvDescription, tvDate, tvDaysOfMonth, tvMonth;
            public LinearLayout llImage;

            public MyViewHolder(View view) {
                super(view);
                tvEvent = view.findViewById(R.id.tvEvent);
                tvDescription = view.findViewById(R.id.tvDescription);
                tvDate = view.findViewById(R.id.tvDate);
                tvDaysOfMonth = view.findViewById(R.id.tvDaysOfMonth);
                tvMonth = view.findViewById(R.id.tvMonth);
                llImage = view.findViewById(R.id.llImage);
            }
        }


        public CalendarAdapter(List<Calendar> calendarList) {
            this.calendarList = calendarList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_calendar, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final Calendar calendar = calendarList.get(position);
            holder.tvEvent.setText("" + calendar.getEvent());
            if (TextUtils.isEmpty(calendar.getDescription())) {
                holder.tvDescription.setVisibility(View.GONE);
            } else {
                holder.tvDescription.setText("" + calendar.getDescription());
            }
            int colorCode = position % 5;
            holder.llImage.setBackground(getResources().getDrawable(circles[colorCode]));
            String eventDate = null;
            if (calendar.getFromDate() != null) {
                eventDate = Utility.formatDateToString(calendar.getFromDate().getTime());
                Format formatter = new SimpleDateFormat("MMM");
                holder.tvMonth.setText("" + formatter.format(calendar.getFromDate()).toUpperCase());
                formatter = new SimpleDateFormat("dd");
                holder.tvDaysOfMonth.setText("" + formatter.format(calendar.getFromDate()));

            }
            if (calendar.getToDate() != null) {
                eventDate = eventDate + " to " + Utility.formatDateToString(calendar.getToDate().getTime());
            }
            if (eventDate != null) {
                holder.tvDate.setText("" + eventDate);
            }

        }

        @Override
        public int getItemCount() {
            return calendarList.size();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (calendarListener != null) {
            calendarListener.remove();
        }
    }
}
