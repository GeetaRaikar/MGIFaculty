package com.padmajeet.mgi.techforedu.faculty;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.padmajeet.mgi.techforedu.faculty.model.Holiday;
import com.padmajeet.mgi.techforedu.faculty.model.Staff;
import com.padmajeet.mgi.techforedu.faculty.util.SessionManager;
import com.padmajeet.mgi.techforedu.faculty.util.Utility;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class FragmentHoliday extends Fragment {
    private LinearLayout llNoList;
    private List<Holiday> holidayList = new ArrayList<>();
    private Bundle bundle = new Bundle();
    private String academicYearId;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference holidayCollectionRef = db.collection("Holiday");
    private ListenerRegistration holidayListener;
    private RecyclerView rvHoliday;
    private RecyclerView.Adapter holidayAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private Staff loggedInUser;
    private String instituteId;
    private Gson gson;
    int []circles = {R.drawable.circle_blue_filled,R.drawable.circle_brown_filled,R.drawable.circle_green_filled,R.drawable.circle_pink_filled,R.drawable.circle_orange_filled};

    public FragmentHoliday() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(getContext());
        gson = Utility.getGson();
        String userJson=sessionManager.getString("loggedInUser");
        loggedInUser=gson.fromJson(userJson, Staff.class);
        academicYearId= sessionManager.getString("academicYearId");
        instituteId=sessionManager.getString("instituteId");
        System.out.println("instituteId - "+instituteId);
        System.out.println("academicYearId - "+academicYearId);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_holiday, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.holiday));
        rvHoliday = view.findViewById(R.id.rvHoliday);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext());
        rvHoliday.setLayoutManager(layoutManager);
        llNoList = view.findViewById(R.id.llNoList);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(instituteId != null && academicYearId != null) {
            final SweetAlertDialog pDialog;
            pDialog = Utility.createSweetAlertDialog(getContext());
            pDialog.show();

            holidayListener = holidayCollectionRef
                    .whereEqualTo("instituteId", instituteId)
                    .whereEqualTo("academicYearId", academicYearId)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                return;
                            }
                            if (holidayList.size() != 0) {
                                holidayList.clear();
                            }
                            if (pDialog != null) {
                                pDialog.dismiss();
                            }
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                Holiday holiday = documentSnapshot.toObject(Holiday.class);
                                holiday.setId(documentSnapshot.getId());
                                holidayList.add(holiday);
                            }
                            System.out.println("holidayList.size() - " + holidayList.size());
                            if (holidayList.size() != 0) {
                                holidayAdapter = new HolidayAdapter(holidayList);
                                rvHoliday.setAdapter(holidayAdapter);
                                rvHoliday.setVisibility(View.VISIBLE);
                                llNoList.setVisibility(View.GONE);
                            } else {
                                rvHoliday.setVisibility(View.GONE);
                                llNoList.setVisibility(View.VISIBLE);
                            }
                        }
                    });
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        if (holidayListener != null) {
            holidayListener.remove();
        }
    }

    class HolidayAdapter extends RecyclerView.Adapter<HolidayAdapter.MyViewHolder> {
        private List<Holiday> holidayList;
        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvEvent, tvDate,tvDaysOfMonth,tvMonth;
            private LinearLayout llImage;
            public MyViewHolder(View view) {
                super(view);
                tvEvent = view.findViewById(R.id.tvEvent);
                tvDate = view.findViewById(R.id.tvDate);
                tvDaysOfMonth=view.findViewById(R.id.tvDaysOfMonth);
                tvMonth=view.findViewById(R.id.tvMonth);
                llImage = view.findViewById(R.id.llImage);
            }
        }


        public HolidayAdapter(List<Holiday> holidayList) {
            this.holidayList = holidayList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_calendar, parent, false);
            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final Holiday holiday = holidayList.get(position);
            holder.tvEvent.setText("" + holiday.getEvent());
            int colorCode = position%5;
            holder.llImage.setBackground(getResources().getDrawable(circles[colorCode]));
            String eventDate = null;
            if (holiday.getFromDate() != null) {
                eventDate = Utility.formatDateToString(holiday.getFromDate().getTime());
                Format formatter = new SimpleDateFormat("MMM");
                holder.tvMonth.setText(""+formatter.format(holiday.getFromDate()).toUpperCase());
                formatter = new SimpleDateFormat("dd");
                holder.tvDaysOfMonth.setText(""+formatter.format(holiday.getFromDate()));

            }
            if (holiday.getToDate() != null) {
                eventDate = eventDate + " to " + Utility.formatDateToString(holiday.getToDate().getTime());
            }
            if (eventDate != null) {
                holder.tvDate.setText("" + eventDate);
            }
        }

        @Override
        public int getItemCount() {
            return holidayList.size();
        }
    }

}
