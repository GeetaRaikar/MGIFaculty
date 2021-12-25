package com.padmajeet.mgi.techforedu.faculty;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
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
import com.padmajeet.mgi.techforedu.faculty.model.Event;
import com.padmajeet.mgi.techforedu.faculty.model.EventType;
import com.padmajeet.mgi.techforedu.faculty.model.Staff;
import com.padmajeet.mgi.techforedu.faculty.util.SessionManager;
import com.padmajeet.mgi.techforedu.faculty.util.Utility;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentEvent extends Fragment {

    private View view;
    private ArrayList<Event> eventList = new ArrayList<>();
    private LinearLayout llNoList, llSelectBatch;
    private List<String> batchIdList = new ArrayList<>();
    private List<Batch> batchList = new ArrayList<>();
    private Batch selectedBatch,batch;
    private String event_Date = null;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference eventCollectionRef = db.collection("Event");
    private CollectionReference batchCollectionRef = db.collection("Batch");
    private CollectionReference eventTypeCollectionRef = db.collection("EventType");
    private ListenerRegistration eventListener,eventTypeListener;
    private EventType eventType;
    private List<EventType> eventTypeList = new ArrayList<>();
    private String loggedInUserId, academicYearId, instituteId;
    private Event event;
    private RecyclerView rvEvent;
    private RecyclerView.Adapter eventAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private Staff loggedInUser;
    private Gson gson;
    private SessionManager sessionManager;
    private SweetAlertDialog pDialog;
    private Spinner spBatch;
    private RadioGroup radioGroupRecipient, radioGroupResponseType, radioGroupBatch;
    private RadioButton radioParent, radioFaculty, radioNotResponse, radioResponse, radioAllBatch, radioSelectBatch;
    private Boolean isParent = true, schoolScope = true;
    private String category_view = "N";


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
        pDialog = Utility.createSweetAlertDialog(getContext());
    }

    public FragmentEvent() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_event, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.event));
        radioGroupRecipient=view.findViewById(R.id.radioGroupRecipient);
        radioParent = view.findViewById(R.id.radioParent);
        radioFaculty = view.findViewById(R.id.radioFaculty);
        radioGroupResponseType = view.findViewById(R.id.radioGroupResponseType);
        radioNotResponse = view.findViewById(R.id.radioNotResponse);
        radioResponse = view.findViewById(R.id.radioResponse);
        radioGroupBatch = view.findViewById(R.id.radioGroupBatch);
        radioAllBatch = view.findViewById(R.id.radioAllBatch);
        radioSelectBatch = view.findViewById(R.id.radioSelectBatch);
        llSelectBatch = view.findViewById(R.id.llSelectBatch);
        spBatch = view.findViewById(R.id.spBatch);
        rvEvent = view.findViewById(R.id.rvEvent);
        layoutManager = new LinearLayoutManager(getContext());
        rvEvent.setLayoutManager(layoutManager);
        llNoList = view.findViewById(R.id.llNoList);
        getEventTypes();
        getBatches();
        getEventOfSchool();
        radioGroupRecipient.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = (RadioButton) group.findViewById(checkedId);
                if (radioParent.isChecked()) {
                    isParent = true;
                    radioGroupBatch.setVisibility(View.VISIBLE);
                }
                if (radioFaculty.isChecked()) {
                    isParent = false;
                    radioGroupBatch.setVisibility(View.GONE);
                    llSelectBatch.setVisibility(View.GONE);
                }
                System.out.println(isParent + " isParent ");
                onChangeResponseType();
            }
        });
        radioGroupResponseType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = (RadioButton) group.findViewById(checkedId);
                if (radioNotResponse.isChecked()) {
                    category_view = "N";
                }
                if (radioResponse.isChecked()) {
                    category_view = "R";
                }
                System.out.println(isParent + " category_view " + category_view);
                if (isParent) {
                    onChangeAllSelectBatch();
                } else {
                    getEventOfFaculty();
                }
            }
        });
        radioGroupBatch.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = (RadioButton) group.findViewById(checkedId);
                if (radioAllBatch.isChecked()) {
                    schoolScope = true;
                    llSelectBatch.setVisibility(View.GONE);
                    getEventOfSchool();
                }
                if (radioSelectBatch.isChecked()) {
                    schoolScope = false;
                    llSelectBatch.setVisibility(View.VISIBLE);
                    onSelectBatch();
                }
            }
        });
        spBatch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedBatch = batchList.get(position);
                getEventOfBatch();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.addEvent);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentAddEvent fragmentAddEvent = new FragmentAddEvent();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                fragmentTransaction.replace(R.id.contentLayout, fragmentAddEvent).addToBackStack(null).commit();
            }
        });
        if(batchIdList.size() == 0){
            fab.hide();
        }
        return view;
    }

    private void onChangeResponseType() {
        System.out.println("onChangeResponseType");
        if (isParent) {
            onChangeAllSelectBatch();
        } else {
            getEventOfFaculty();
        }
    }

    private void onChangeAllSelectBatch() {
        System.out.println("onChangeAllSelectBatch");
        if (schoolScope) {
            llSelectBatch.setVisibility(View.GONE);
            getEventOfSchool();
        } else {
            llSelectBatch.setVisibility(View.VISIBLE);
            onSelectBatch();
        }
    }

    private void onSelectBatch() {
        System.out.println("onSelectBatch");
        if (selectedBatch == null) {
            if(batchList.size() > 0) {
                selectedBatch = batchList.get(0);
            }
        }
        getEventOfBatch();
    }

    private void getEventOfSchool() {
        if(instituteId != null) {
            if (!pDialog.isShowing()) {
                pDialog.show();
            }
            eventListener = eventCollectionRef
                    .whereEqualTo("instituteId", instituteId)
                    .whereEqualTo("schoolScope", true)
                    .whereEqualTo("recipientType", "P")
                    .whereEqualTo("category", category_view)
                    .orderBy("fromDate", Query.Direction.DESCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                return;
                            }
                            if (eventList.size() != 0) {
                                eventList.clear();
                            }
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                                event = documentSnapshot.toObject(Event.class);
                                event.setId(documentSnapshot.getId());
                                eventList.add(event);
                            }
                            if (pDialog != null) {
                                pDialog.dismiss();
                            }
                            if (eventList.size() != 0) {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                                // System.out.println("event Size -"+eventList.size());
                                eventAdapter = new EventAdapter(eventList);
                                rvEvent.setAdapter(eventAdapter);
                                rvEvent.setVisibility(View.VISIBLE);
                                llNoList.setVisibility(View.GONE);
                            } else {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                                rvEvent.setVisibility(View.GONE);
                                llNoList.setVisibility(View.VISIBLE);
                            }
                        }
                    });
        }else{
            rvEvent.setVisibility(View.GONE);
            llNoList.setVisibility(View.VISIBLE);
        }
    }

    private void getEventOfFaculty() {
        if(instituteId != null) {
            if (!pDialog.isShowing()) {
                pDialog.show();
            }
            eventListener = eventCollectionRef
                    .whereEqualTo("instituteId", instituteId)
                    .whereEqualTo("recipientType", "F")
                    .whereEqualTo("category", category_view)
                    .orderBy("fromDate", Query.Direction.DESCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                return;
                            }
                            if (eventList.size() != 0) {
                                eventList.clear();
                            }
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                                event = documentSnapshot.toObject(Event.class);
                                event.setId(documentSnapshot.getId());
                                eventList.add(event);
                            }
                            if (pDialog != null) {
                                pDialog.dismiss();
                            }
                            if (eventList.size() != 0) {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                                // System.out.println("event Size -"+eventList.size());
                                eventAdapter = new EventAdapter(eventList);
                                rvEvent.setAdapter(eventAdapter);
                                rvEvent.setVisibility(View.VISIBLE);
                                llNoList.setVisibility(View.GONE);
                            } else {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                                rvEvent.setVisibility(View.GONE);
                                llNoList.setVisibility(View.VISIBLE);
                            }
                        }
                    });
        }else{
            rvEvent.setVisibility(View.GONE);
            llNoList.setVisibility(View.VISIBLE);
        }
    }

    private void getEventOfBatch() {
        if(instituteId != null && selectedBatch != null) {
            if (!pDialog.isShowing()) {
                pDialog.show();
            }
            eventListener = eventCollectionRef
                    .whereEqualTo("instituteId", instituteId)
                    .whereEqualTo("recipientType", "P")
                    .whereEqualTo("category", category_view)
                    .whereIn("batchId", Arrays.asList("", selectedBatch.getId()))
                    .orderBy("fromDate", Query.Direction.DESCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                return;
                            }
                            if (eventList.size() != 0) {
                                eventList.clear();
                            }
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                                event = documentSnapshot.toObject(Event.class);
                                event.setId(documentSnapshot.getId());
                                eventList.add(event);
                            }
                            if (pDialog != null) {
                                pDialog.dismiss();
                            }
                            if (eventList.size() != 0) {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                                // System.out.println("event Size -"+eventList.size());
                                eventAdapter = new EventAdapter(eventList);
                                rvEvent.setAdapter(eventAdapter);
                                rvEvent.setVisibility(View.VISIBLE);
                                llNoList.setVisibility(View.GONE);
                            } else {
                                if (pDialog != null && pDialog.isShowing()) {
                                    pDialog.dismiss();
                                }
                                rvEvent.setVisibility(View.GONE);
                                llNoList.setVisibility(View.VISIBLE);
                            }
                        }
                    });
        }else{
            rvEvent.setVisibility(View.GONE);
            llNoList.setVisibility(View.VISIBLE);
        }
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
                                if (setBatch.size() == batchList.size()) {
                                    if (pDialog != null) {
                                        pDialog.dismiss();
                                    }
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
                                        spBatch.setVisibility(View.VISIBLE);

                                        List<String> batchNameList = new ArrayList<>();
                                        for (Batch batch : batchList) {
                                            batchNameList.add(batch.getName());
                                        }
                                        ArrayAdapter<String> batchAdaptor = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, batchNameList);
                                        batchAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        spBatch.setAdapter(batchAdaptor);
                                    } else {
                                        if (pDialog != null) {
                                            pDialog.dismiss();
                                        }
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
        }else{
            spBatch.setVisibility(View.GONE);
        }
    }

    private void getEventTypes() {
        if(instituteId != null) {
            eventTypeListener = eventTypeCollectionRef
                    .whereEqualTo("instituteId", instituteId)
                    .orderBy("createdDate", Query.Direction.ASCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                return;
                            }
                            if (eventTypeList.size() != 0) {
                                eventTypeList.clear();
                            }
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                                eventType = documentSnapshot.toObject(EventType.class);
                                eventType.setId(documentSnapshot.getId());
                                eventTypeList.add(eventType);
                            }
                            if (pDialog != null) {
                                pDialog.dismiss();
                            }
                        }
                    });
        }

    }

    class EventAdapter extends RecyclerView.Adapter<EventAdapter.MyViewHolder> {
        private List<Event> eventList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvName, tvDressCode, tvDate, tvDescription;
            public ImageView ivDelete, ivProfilePic;

            public MyViewHolder(View view) {
                super(view);
                tvName = (TextView) view.findViewById(R.id.tvName);
                tvDescription = (TextView) view.findViewById(R.id.tvDescription);
                tvDressCode = (TextView) view.findViewById(R.id.tvDressCode);
                tvDate = (TextView) view.findViewById(R.id.tvDate);
                ivDelete = view.findViewById(R.id.ivDelete);
                ivProfilePic = view.findViewById(R.id.ivProfilePic);
            }
        }


        public EventAdapter(List<Event> eventList) {
            this.eventList = eventList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_event, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {

            final Event event = eventList.get(position);
            holder.tvName.setText("" + event.getName());
            if (TextUtils.isEmpty(event.getDressCode())) {
                holder.tvDressCode.setVisibility(View.GONE);
            } else {
                holder.tvDressCode.setText("" + event.getDressCode());
            }
            for (EventType type : eventTypeList) {
                if (type.getId().equalsIgnoreCase(event.getTypeId())) {
                    if (TextUtils.isEmpty(type.getImageUrl())) {
                        Glide.with(getContext())
                                .load(R.drawable.no_image)
                                .fitCenter()
                                .apply(RequestOptions.circleCropTransform())
                                .into(holder.ivProfilePic);
                    } else {
                        Glide.with(getContext())
                                .load(type.getImageUrl())
                                .fitCenter()
                                .apply(RequestOptions.circleCropTransform())
                                .into(holder.ivProfilePic);
                    }
                    break;
                }
            }

            if (TextUtils.isEmpty(event.getDescription())) {
                holder.tvDescription.setVisibility(View.GONE);
            } else {
                holder.tvDescription.setText("" + event.getDescription());
            }

            if (event.getFromDate() != null) {
                event_Date = Utility.formatDateToString(event.getFromDate().getTime());
            }
            if (event.getToDate() != null) {
                event_Date = event_Date + " to " + Utility.formatDateToString(event.getToDate().getTime());
            }
            if (event_Date != null) {
                holder.tvDate.setText("" + event_Date);
            }
            /*holder.ivDelete.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    //System.out.println("Edit clicked");
                    SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                            .setTitleText("Delete...")
                            .setConfirmText("Confirm")
                            .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                }
                            })
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismissWithAnimation();
                                    eventCollectionRef.document(event.getId())
                                            .delete()
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    onChangeResponseType();//Repeat Calling event data's
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {

                                                }
                                            });
                                }
                            });
                    dialog.setCancelable(false);
                    dialog.show();
                }
            });*/

        }

        @Override
        public int getItemCount() {
            return eventList.size();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(eventTypeListener != null){
            eventTypeListener.remove();
        }
        if(eventListener != null){
            eventListener.remove();
        }
    }
}
