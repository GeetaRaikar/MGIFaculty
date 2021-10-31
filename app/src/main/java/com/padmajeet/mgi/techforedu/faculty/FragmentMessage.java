package com.padmajeet.mgi.techforedu.faculty;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;

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
import com.padmajeet.mgi.techforedu.faculty.model.Message;
import com.padmajeet.mgi.techforedu.faculty.model.Staff;
import com.padmajeet.mgi.techforedu.faculty.model.Student;
import com.padmajeet.mgi.techforedu.faculty.util.SessionManager;
import com.padmajeet.mgi.techforedu.faculty.util.Utility;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentMessage extends Fragment {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference batchCollectionRef = db.collection("Batch");
    private CollectionReference sectionCollectionRef = db.collection("Section");
    private CollectionReference studentCollectionRef = db.collection("Student");
    private CollectionReference messageCollectionRef = db.collection("Message");
    private ListenerRegistration messageListener;
    private List<Message> messageList = new ArrayList<>();
    private RecyclerView rvMessage;
    private RecyclerView.Adapter messageAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private String academicYearId;
    private String instituteId;
    private SweetAlertDialog pDialog;
    private LinearLayout llNoList;
    private List<String> sectionIdList = new ArrayList<>();
    private List<String> filterSectionIdList=new ArrayList<>();
    //private List<Section> sectionList = new ArrayList<>();
    //private Section section;
    private List<String> batchIdList = new ArrayList<>();
    private List<Batch> batchList = new ArrayList<>();
    private LinearLayout llBatch;
    private Batch batch,selectedBatch;
    private Spinner spBatch;
    private List<Student> studentList = new ArrayList<>();
    private Student student;
    private Student selectedStudent;
    private LinearLayout llStudent;
    private Spinner spStudent;
    private RadioButton radioAllBatch, radioSelectBatch, radioSelectStudent;
    private RadioGroup radioGroup;
    private SessionManager sessionManager;
    private Gson gson;
    private Staff loggedInUser;
    private Boolean isBatch = false;
    private int[] circles = {R.drawable.circle_blue_filled, R.drawable.circle_brown_filled, R.drawable.circle_green_filled, R.drawable.circle_pink_filled, R.drawable.circle_orange_filled};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gson = Utility.getGson();
        SessionManager sessionManager = new SessionManager(getContext());
        String loggedInUserJson = sessionManager.getString("loggedInUser");
        loggedInUser = gson.fromJson(loggedInUserJson, Staff.class);
        academicYearId = sessionManager.getString("academicYearId");
        instituteId = sessionManager.getString("instituteId");
        String batchIdJson = sessionManager.getString("batchIdList");
        batchIdList = gson.fromJson(batchIdJson,new TypeToken<List<String>>() {
        }.getType());
        System.out.println("sessionManager batchIdList "+batchIdJson+" value "+batchIdList.toString());
        /*String sectionIdJson = sessionManager.getString("sectionIdList");
        sectionIdList = gson.fromJson(sectionIdJson,new TypeToken<List<String>>() {
        }.getType());
        System.out.println("sessionManager sectionIdList "+sectionIdJson+" value "+sectionIdList.toString());*/
        pDialog = Utility.createSweetAlertDialog(getContext());
    }

    public FragmentMessage() {
        // Required empty public constructor
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_message, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.message));
        spBatch = view.findViewById(R.id.spBatch);
        spStudent = view.findViewById(R.id.spStudent);
        llBatch = view.findViewById(R.id.llBatch);
        llStudent = view.findViewById(R.id.llStudent);
        radioAllBatch = view.findViewById(R.id.radioAllBatch);
        radioSelectBatch = view.findViewById(R.id.radioSelectBatch);
        radioSelectStudent = view.findViewById(R.id.radioSelectStudent);
        radioGroup = view.findViewById(R.id.radioGroup);

        llNoList = view.findViewById(R.id.llNoList);
        rvMessage = view.findViewById(R.id.rvMessage);
        layoutManager = new LinearLayoutManager(getContext());
        rvMessage.setLayoutManager(layoutManager);

        //Initial method call for category A
        getMessagesOfAllBatch();

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = (RadioButton) group.findViewById(checkedId);
                if (radioAllBatch.isChecked()) {
                    llBatch.setVisibility(View.GONE);
                    llStudent.setVisibility(View.GONE);
                    System.out.println("radioAllBatch " + radioAllBatch);
                    getMessagesOfAllBatch();
                }
                if (radioSelectBatch.isChecked()) {
                    llBatch.setVisibility(View.VISIBLE);
                    llStudent.setVisibility(View.GONE);
                    System.out.println("radioSelectBatch " + radioSelectBatch);
                    isBatch = true;
                    onSelectBatch();
                }
                if (radioSelectStudent.isChecked()) {
                    llBatch.setVisibility(View.VISIBLE);
                    llStudent.setVisibility(View.VISIBLE);
                    isBatch = false;
                    onSelectBatch();
                }
            }
        });

        spBatch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedBatch = batchList.get(position);
                if (isBatch) {
                    System.out.println("category_view " + isBatch);
                    getMessagesOfSelectClass();
                } else {
                    System.out.println("category_view " + isBatch);
                    //getSectionsOfClass();
                    getKidsOfClass();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.addMessage);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentAddMessage fragmentAddMessage = new FragmentAddMessage();
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                fragmentTransaction.replace(R.id.contentLayout, fragmentAddMessage).addToBackStack(null).commit();
            }
        });
        getBatches();
    }

    private void onSelectBatch() {
        System.out.println("batchList "+batchList.get(0).getName());
        if (selectedBatch == null) {
            selectedBatch = batchList.get(0);
        }
        if (isBatch) {
            System.out.println("isBatch " + isBatch);
            getMessagesOfSelectClass();
        } else {
            System.out.println("isBatch " + isBatch);
            //getSectionsOfClass();
            getKidsOfClass();
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

    private void getKidsOfClass() {
        if (pDialog != null && !pDialog.isShowing()) {
            pDialog.show();
        }
        studentCollectionRef
                .whereEqualTo("currentBatchId", selectedBatch.getId())
                .orderBy("createdDate", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if (studentList.size() != 0) {
                            studentList.clear();
                        }
                        System.out.println("queryDocumentSnapshots " + queryDocumentSnapshots.size());
                        for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            Student student = documentSnapshot.toObject(Student.class);
                            student.setId(documentSnapshot.getId());
                            if(student.getStatus().equalsIgnoreCase("A")||student.getStatus().equalsIgnoreCase("N")){
                                studentList.add(student);
                            }
                        }
                        System.out.println("studentList " + studentList.size());
                        if (studentList.size() != 0) {
                            spStudent.setVisibility(View.VISIBLE);
                            List<String> studentNameList = new ArrayList<>();
                            for (Student student : studentList) {
                                String name = student.getFirstName();
                                if (!TextUtils.isEmpty(student.getMiddleName())) {
                                    name = name + " " + student.getMiddleName();
                                }
                                if (!TextUtils.isEmpty(student.getLastName())) {
                                    name = name + " " + student.getLastName();
                                }
                                studentNameList.add(name);
                            }
                            ArrayAdapter<String> studentAdaptor = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, studentNameList);
                            studentAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spStudent.setAdapter(studentAdaptor);
                            spStudent.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    selectedStudent = studentList.get(position);
                                    getMessagesOfSelectStudent();
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                        } else {
                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                            spStudent.setVisibility(View.GONE);
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (pDialog != null && pDialog.isShowing()) {
                            pDialog.dismiss();
                        }
                    }
                });
    }

    private void getMessagesOfAllBatch() {
        System.out.println("academicYearId " + academicYearId);
        if(academicYearId != null) {
            if (pDialog != null && !pDialog.isShowing()) {
                pDialog.show();
            }
            messageListener = messageCollectionRef
                    .whereEqualTo("academicYearId", academicYearId)
                    .whereEqualTo("category", "A")
                    .orderBy("createdDate", Query.Direction.DESCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                return;
                            }
                            if (messageList.size() != 0) {
                                messageList.clear();
                            }
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                                Message message = documentSnapshot.toObject(Message.class);
                                message.setId(documentSnapshot.getId());
                                messageList.add(message);
                            }
                            if (pDialog != null) {
                                pDialog.dismiss();
                            }
                            System.out.println("messageList.size() - " + messageList.size());
                            if (messageList.size() != 0) {
                                messageAdapter = new MessageAdapter(messageList);
                                rvMessage.setAdapter(messageAdapter);
                                rvMessage.setVisibility(View.VISIBLE);
                                llNoList.setVisibility(View.GONE);
                            } else {
                                rvMessage.setVisibility(View.GONE);
                                llNoList.setVisibility(View.VISIBLE);
                            }
                        }
                    });
        }
    }

    private void getMessagesOfSelectClass() {
        if(academicYearId != null) {
            if (pDialog != null && !pDialog.isShowing()) {
                pDialog.show();
            }
            messageListener = messageCollectionRef
                    .whereEqualTo("academicYearId", academicYearId)
                    .whereEqualTo("category", "C")
                    .whereArrayContains("batchList", selectedBatch.getId())
                    .orderBy("createdDate", Query.Direction.DESCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                return;
                            }
                            if (messageList.size() != 0) {
                                messageList.clear();
                            }
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                                Message message = documentSnapshot.toObject(Message.class);
                                message.setId(documentSnapshot.getId());
                                messageList.add(message);
                            }
                            if (pDialog != null) {
                                pDialog.dismiss();
                            }
                            if (messageList.size() != 0) {
                                messageAdapter = new MessageAdapter(messageList);
                                rvMessage.setAdapter(messageAdapter);
                                rvMessage.setVisibility(View.VISIBLE);
                                llNoList.setVisibility(View.GONE);
                            } else {
                                rvMessage.setVisibility(View.GONE);
                                llNoList.setVisibility(View.VISIBLE);
                            }
                        }
                    });
        }
    }

    private void getMessagesOfSelectStudent() {
        if(academicYearId != null) {
            if (pDialog != null && !pDialog.isShowing()) {
                pDialog.show();
            }
            messageListener = messageCollectionRef
                    .whereEqualTo("academicYearId", academicYearId)
                    .whereEqualTo("category", "S")
                    .whereArrayContains("recipientId", selectedStudent.getId())
                    .orderBy("createdDate", Query.Direction.DESCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                return;
                            }
                            if (messageList.size() != 0) {
                                messageList.clear();
                            }
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                                Message message = documentSnapshot.toObject(Message.class);
                                message.setId(documentSnapshot.getId());
                                messageList.add(message);
                            }
                            if (pDialog != null) {
                                pDialog.dismiss();
                            }
                            if (messageList.size() != 0) {
                                messageAdapter = new MessageAdapter(messageList);
                                rvMessage.setAdapter(messageAdapter);
                                rvMessage.setVisibility(View.VISIBLE);
                                llNoList.setVisibility(View.GONE);
                            } else {
                                rvMessage.setVisibility(View.GONE);
                                llNoList.setVisibility(View.VISIBLE);
                            }
                        }
                    });
        }
    }

    class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MyViewHolder> {
        private List<Message> messageList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvSubject, tvCreatorType, tvSubjectHeader, tvDate;
            LinearLayout llImage;
            View row;

            public MyViewHolder(View view) {
                super(view);
                tvSubject = view.findViewById(R.id.tvSubject);
                tvCreatorType = view.findViewById(R.id.tvCreatorType);
                tvSubjectHeader = view.findViewById(R.id.tvSubjectHeader);
                llImage = view.findViewById(R.id.llImage);
                tvDate = view.findViewById(R.id.tvDate);
                row = view;
            }
        }


        public MessageAdapter(List<Message> messageList) {
            this.messageList = messageList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_message, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final Message message = messageList.get(position);
            holder.tvSubject.setText("" + message.getSubject());
            holder.tvDate.setText("" + Utility.formatDateToString(message.getCreatedDate().getTime()));
            int colorCode = position % 5;
            holder.llImage.setBackground(getResources().getDrawable(circles[colorCode]));
            holder.tvSubjectHeader.setText("" + message.getSubject().toUpperCase().charAt(0));
            if (message.getCreatorType().equals("A")) {
                holder.tvCreatorType.setText("Admin");
            } else if (message.getCreatorType().equals("F")) {
                holder.tvCreatorType.setText("Teacher");
            }

            holder.row.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    gson = Utility.getGson();
                    if (message.getBatchIdList().size() != 0) {
                        String name = "";
                        for (int i = 0; i < message.getBatchIdList().size(); i++) {
                            for (Batch batch :batchList) {
                                if (batch.getId().equalsIgnoreCase(message.getBatchIdList().get(i))) {
                                    if (!TextUtils.isEmpty(name)) {
                                        name = name + ", " + batch.getName();
                                    } else {
                                        name = batch.getName();
                                    }
                                    break;
                                }
                            }
                        }
                        bundle.putString("selectedBatchName", name);
                    }
                    String selectedMessage = gson.toJson(message);
                    bundle.putString("selectedMessage", selectedMessage);
                    FragmentMessageDetails fragmentMessageDetails = new FragmentMessageDetails();
                    fragmentMessageDetails.setArguments(bundle);
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = manager.beginTransaction();
                    fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    fragmentTransaction.replace(R.id.contentLayout, fragmentMessageDetails);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            });
        }

        @Override
        public int getItemCount() {
            return messageList.size();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if(messageListener != null){
            messageListener.remove();
        }
    }
}
