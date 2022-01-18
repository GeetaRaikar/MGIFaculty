package com.padmajeet.mgi.techforedu.faculty;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.padmajeet.mgi.techforedu.faculty.model.Batch;
import com.padmajeet.mgi.techforedu.faculty.model.BatchSubjectFaculty;
import com.padmajeet.mgi.techforedu.faculty.model.Feedback;
import com.padmajeet.mgi.techforedu.faculty.model.FeedbackCategory;
import com.padmajeet.mgi.techforedu.faculty.model.Staff;
import com.padmajeet.mgi.techforedu.faculty.util.SessionManager;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.padmajeet.mgi.techforedu.faculty.util.Utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class FragmentFeedback extends Fragment {

    private View view = null;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference batchCollectionRef = db.collection("Batch");
    private CollectionReference feedBackCollectionRef=db.collection("Feedback");
    private CollectionReference feedBackCategoryCollectionRef=db.collection("FeedbackCategory");
    private ListenerRegistration feedbackListener;
    private Feedback feedback;
    private ArrayList<Feedback> feedbackList = new ArrayList<>();
    private List<String> batchIdList=new ArrayList<>();
    private ArrayList<Batch> batchList = new ArrayList<>();
    private Batch batch, selectedBatch;
    private BatchSubjectFaculty batchSubjectFaculty;
    private ArrayList<BatchSubjectFaculty> batchSubjectFacultyList = new ArrayList<>();
    private Gson gson;
    private Staff loggedInUser;
    private String loggedInUserId, instituteId, academicYearId;
    private SweetAlertDialog pDialog;
    private SessionManager sessionManager;
    private LinearLayout llNoList;
    private Spinner spBatch;
    private RecyclerView rvFeedback;
    private RecyclerView.Adapter feedbackAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private int []circles = {R.drawable.circle_blue_filled,R.drawable.circle_brown_filled,R.drawable.circle_green_filled,R.drawable.circle_pink_filled,R.drawable.circle_orange_filled};
    private List<FeedbackCategory> feedbackCategoryList=new ArrayList<>();
    private FeedbackCategory selectedFeedBackCategory,feedbackCategory;
    private List<String> nameList = new ArrayList<>();

    public FragmentFeedback() {
        // Required empty public constructor
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feedback, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.subject));
        spBatch = view.findViewById(R.id.spBatch);
        llNoList = (LinearLayout) view.findViewById(R.id.llNoList);
        rvFeedback = (RecyclerView) view.findViewById(R.id.rvFeedback);
        layoutManager = new LinearLayoutManager(getContext());
        rvFeedback.setLayoutManager(layoutManager);

        getFeedBackCategory();
        getBatches();

        spBatch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedBatch = batchList.get(position);
                getFeedback();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

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
                                System.out.println("batchList " + batchList.size());
                                if (setBatch.size() == batchList.size()) {
                                    if (pDialog != null) {
                                        pDialog.dismiss();
                                    }
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
                                    //Spinner set up
                                    spBatch.setVisibility(View.VISIBLE);
                                    List<String> batchNameList = new ArrayList<>();
                                    for (Batch batch : batchList) {
                                        batchNameList.add(batch.getName());
                                    }
                                    ArrayAdapter<String> batchAdaptor = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, batchNameList);
                                    batchAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                    spBatch.setAdapter(batchAdaptor);
                                    selectedBatch = batchList.get(0);
                                    getFeedback();
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {

                            }
                        });
            }
        }else{
            spBatch.setVisibility(View.GONE);
            rvFeedback.setVisibility(View.GONE);
            llNoList.setVisibility(View.VISIBLE);
        }
    }
    private void getFeedback() {
        if(feedbackList.size()!=0){
            feedbackList.clear();
        }
        final SweetAlertDialog pDialog;
        pDialog = Utility.createSweetAlertDialog(getContext());
        pDialog.show();
        if(loggedInUser != null) {
            feedBackCollectionRef
                    .whereEqualTo("batchId", selectedBatch.getId())
                    .orderBy("createdDate", Query.Direction.DESCENDING)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            System.out.println("Feedback  -" + task.getResult().size());
                            if (task.isSuccessful()) {
                                if (pDialog != null) {
                                    pDialog.dismiss();
                                }
                                if(feedbackList.size()!=0){
                                    feedbackList.clear();
                                }
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    feedback = document.toObject(Feedback.class);
                                    feedback.setId(document.getId());
                                    feedbackList.add(feedback);
                                }
                                System.out.println("FeedBack  -" + feedbackList.size());
                                if (feedbackList.size() != 0) {
                                    feedbackAdapter = new FeedbackAdapter(feedbackList);
                                    rvFeedback.setAdapter(feedbackAdapter);
                                    rvFeedback.setVisibility(View.VISIBLE);
                                    llNoList.setVisibility(View.GONE);
                                } else {
                                    rvFeedback.setVisibility(View.GONE);
                                    llNoList.setVisibility(View.VISIBLE);
                                }
                            } else {
                                rvFeedback.setVisibility(View.GONE);
                                llNoList.setVisibility(View.VISIBLE);
                            }
                        }
                    });
        }
    }
    class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.MyViewHolder> {
        private List<Feedback> feedbackList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvFeedbackCategory, tvFeedback,tvDate,tvSubjectHeader,tvReviewerType;
            public LinearLayout llImage;

            public MyViewHolder(View view) {
                super(view);
                tvFeedbackCategory = view.findViewById(R.id.tvFeedbackCategory);
                tvFeedback = view.findViewById(R.id.tvFeedback);
                tvDate = view.findViewById(R.id.tvDate);
                llImage = view.findViewById(R.id.llImage);
                tvSubjectHeader= view.findViewById(R.id.tvSubjectHeader);
                tvReviewerType= view.findViewById(R.id.tvReviewerType);
            }
        }


        public FeedbackAdapter(List<Feedback> feedbackList) {
            this.feedbackList = feedbackList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_feedback, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final Feedback feedBack = feedbackList.get(position);
            holder.tvFeedback.setText(""+feedBack.getFeedback());
            int colorCode = position%5;
            holder.llImage.setBackground(getResources().getDrawable(circles[colorCode]));
            for(int i=0;i<feedbackCategoryList.size();i++){
                if(feedbackCategoryList.get(i).getId().equals(feedBack.getFeedbackCategoryId())){
                    holder.tvFeedbackCategory.setText(""+feedbackCategoryList.get(i).getCategory());
                    holder.tvSubjectHeader.setText(""+feedbackCategoryList.get(i).getCategory().toUpperCase().charAt(0));
                    break;
                }
            }
            holder.tvDate.setText(""+Utility.formatDateToString(feedBack.getCreatedDate().getTime()));
            holder.tvReviewerType.setText(feedBack.getReviewerType());
            if(feedBack.getReviewerType().equalsIgnoreCase("P")){
                holder.tvReviewerType.setBackground(getResources().getDrawable(R.color.colorOrange));
            }else {
                holder.tvReviewerType.setBackground(getResources().getDrawable(R.color.colorGreen));
            }
        }

        @Override
        public int getItemCount() {
            return feedbackList.size();
        }
    }
    private void getFeedBackCategory(){
        feedBackCategoryCollectionRef
                .whereEqualTo("instituteId",instituteId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                feedbackCategory = document.toObject(FeedbackCategory.class);
                                feedbackCategory.setId(document.getId());
                                System.out.println("FeedbackCategory -" + feedbackCategory.getCategory());
                                feedbackCategoryList.add(feedbackCategory);
                                nameList.add(feedbackCategory.getCategory());
                            }
                        } else {
                        }
                    }
                });
        // [END get_all_users]

    }


}