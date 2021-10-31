package com.padmajeet.mgi.techforedu.faculty;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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
import com.padmajeet.mgi.techforedu.faculty.model.Staff;
import com.padmajeet.mgi.techforedu.faculty.model.StaffType;
import com.padmajeet.mgi.techforedu.faculty.util.SessionManager;
import com.padmajeet.mgi.techforedu.faculty.util.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentColleague extends Fragment {

    private View view;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference staffCollectionRef = db.collection("Staff");
    private CollectionReference staffTypeCollectionRef = db.collection("StaffType");
    private ListenerRegistration staffTypeListener,staffListener;
    private ArrayList<Staff> staffList = new ArrayList<>();
    private Staff staff, loggedInUser;
    private ArrayList<StaffType> staffTypeList = new ArrayList<>();
    private StaffType staffType, selectedStaffType;
    private Gson gson;
    private SessionManager sessionManager;
    private SweetAlertDialog pDialog;
    private LinearLayout llNoList;
    private Spinner spStaffType;
    private RecyclerView rvStaff;
    private RecyclerView.Adapter staffAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private String loggedInUserId, academicYearId, instituteId;
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
        instituteId = sessionManager.getString("instituteId");
        pDialog = Utility.createSweetAlertDialog(getContext());
    }


    public FragmentColleague() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_colleague, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.colleagues));
        spStaffType = view.findViewById(R.id.spStaffType);
        rvStaff = view.findViewById(R.id.rvStaff);
        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext());
        rvStaff.setLayoutManager(layoutManager);
        llNoList = (LinearLayout) view.findViewById(R.id.llNoList);
        getStaffType();
        return view;
    }

    private void getStaffType() {
        if(instituteId != null) {
            if (!pDialog.isShowing()) {
                pDialog.show();
            }
            staffTypeListener = staffTypeCollectionRef
                    .whereEqualTo("instituteId", instituteId)
                    .orderBy("type", Query.Direction.ASCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                return;
                            }
                            if (staffTypeList.size() != 0) {
                                staffTypeList.clear();
                            }
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                                staffType = documentSnapshot.toObject(StaffType.class);
                                staffType.setId(documentSnapshot.getId());
                                staffTypeList.add(staffType);
                            }
                            if (pDialog != null) {
                                pDialog.dismiss();
                            }
                            System.out.println("staffTypeList " + staffTypeList.size());
                            if (staffTypeList.size() != 0) {
                                List<String> staffTypeNameList = new ArrayList<>();
                                for (StaffType staffType1 : staffTypeList) {
                                    staffTypeNameList.add(staffType1.getType());
                                }
                                ArrayAdapter<String> batchAdaptor = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, staffTypeNameList);
                                batchAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spStaffType.setAdapter(batchAdaptor);

                                spStaffType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                        selectedStaffType = staffTypeList.get(position);
                                        getStaff();
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {

                                    }
                                });
                            } else {
                                rvStaff.setVisibility(View.GONE);
                                llNoList.setVisibility(View.VISIBLE);
                            }
                        }
                    });
        }
    }

    private void getStaff() {
        if (staffList.size() != 0) {
            staffList.clear();
        }
        if (!pDialog.isShowing()) {
            pDialog.show();
        }
        staffListener = staffCollectionRef
                .whereEqualTo("staffTypeId", selectedStaffType.getId())
                .whereIn("status", Arrays.asList("A", "F"))
                .orderBy("createdDate", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }
                        if (staffList.size() != 0) {
                            staffList.clear();
                        }
                        for (DocumentSnapshot documentSnapshot:queryDocumentSnapshots.getDocuments()){
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            staff = documentSnapshot.toObject(Staff.class);
                            staff.setId(documentSnapshot.getId());
                            staffList.add(staff);
                        }
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        if(loggedInUser != null) {
                            if (staffList.size() != 0 && selectedStaffType.getId().equalsIgnoreCase(loggedInUser.getStaffTypeId())) {
                                for (int i = 0; i < staffList.size(); i++) {
                                    if (staffList.get(i).getId().equals(loggedInUser.getId())) {
                                        staffList.remove(staffList.get(i));
                                        break;
                                    }
                                }
                            }
                        }
                        if (staffList.size() != 0) {
                            if (pDialog != null) {
                                pDialog.dismiss();
                            }
                            rvStaff.setVisibility(View.VISIBLE);
                            llNoList.setVisibility(View.GONE);
                            staffAdapter = new StaffAdapter(staffList);
                            rvStaff.setAdapter(staffAdapter);
                        } else {
                            if (pDialog != null) {
                                pDialog.dismiss();
                            }
                            rvStaff.setVisibility(View.GONE);
                            llNoList.setVisibility(View.VISIBLE);
                        }
                    }
                });
        // [END get_all_users]
    }

    class StaffAdapter extends RecyclerView.Adapter<StaffAdapter.MyViewHolder> {
        private List<Staff> staffList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvName, tvMobileNumber, tvDesignation, tvFirstLetter;
            public ImageView ivProfilePic;
            private LinearLayout llImage;

            public MyViewHolder(View view) {
                super(view);
                tvName = (TextView) view.findViewById(R.id.tvName);
                tvMobileNumber = (TextView) view.findViewById(R.id.tvMobileNumber);
                tvDesignation = (TextView) view.findViewById(R.id.tvDesignation);
                ivProfilePic = view.findViewById(R.id.ivProfilePic);
                llImage = (LinearLayout) view.findViewById(R.id.llImage);
                tvFirstLetter = (TextView) view.findViewById(R.id.tvFirstLetter);
            }
        }


        public StaffAdapter(List<Staff> staffList) {
            this.staffList = staffList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_colleague, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final Staff staff = staffList.get(position);
            String staffName = "";
            if (staff.getTitle().isEmpty()) {
                staffName = staff.getFirstName();
            } else {
                staffName = staff.getTitle() + " " + staff.getFirstName();
            }
            if (!(staff.getMiddleName().isEmpty())) {
                staffName = staffName + " " + staff.getMiddleName();
            }
            if (!(staff.getLastName().isEmpty())) {
                staffName = staffName + " " + staff.getLastName();
            }

            holder.tvName.setText("" + staffName);
            int len = staff.getMobileNumber().length();
            if (len != 10) {
                holder.tvMobileNumber.setText("" + null);
            } else {
                holder.tvMobileNumber.setText("" + staff.getMobileNumber());
                holder.tvMobileNumber.setTextColor(getResources().getColor(R.color.colorPrimary));
                holder.tvMobileNumber.setPaintFlags(holder.tvMobileNumber.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

                holder.tvMobileNumber.setOnClickListener(new View.OnClickListener() {

                    public void onClick(View view) {
                        String mobileNumber = "tel:" + staff.getMobileNumber();
                        Intent intent = new Intent(Intent.ACTION_DIAL);
                        intent.setData(Uri.parse(mobileNumber));
                        startActivity(intent);
                    }
                });
            }
            //System.out.println("imageUrl "+imageUrl);
            if (TextUtils.isEmpty(staff.getImageUrl())) {
                holder.ivProfilePic.setVisibility(View.GONE);
                holder.llImage.setVisibility(View.VISIBLE);
                int colorCode = position % 5;
                holder.llImage.setBackground(getResources().getDrawable(circles[colorCode]));
                holder.tvFirstLetter.setText("" + staff.getFirstName().toUpperCase().charAt(0));
            } else {
                holder.llImage.setVisibility(View.GONE);
                holder.ivProfilePic.setVisibility(View.VISIBLE);
                Glide.with(getContext())
                        .load(staff.getImageUrl())
                        .fitCenter()
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.ic_faculty)
                        .into(holder.ivProfilePic);
            }
            holder.tvDesignation.setText("" + staff.getDesignation());
        }

        @Override

        public int getItemCount() {
            return staffList.size();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(staffListener != null){
            staffListener.remove();
        }
        if(staffTypeListener != null){
            staffListener.remove();
        }
    }
}
