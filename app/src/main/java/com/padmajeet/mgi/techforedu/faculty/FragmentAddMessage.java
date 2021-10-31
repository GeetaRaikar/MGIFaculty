package com.padmajeet.mgi.techforedu.faculty;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
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
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.app.Activity.RESULT_OK;

public class FragmentAddMessage extends Fragment {

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference batchCollectionRef = db.collection("Batch");
    private CollectionReference studentCollectionRef = db.collection("Student");
    private CollectionReference messageCollectionRef = db.collection("Message");
    private LinearLayout llBatch, llSelectBatch, llStudent, llSelectStudent;
    private RadioButton radioAllBatch, radioSelectBatch, radioSelectStudent;
    private RadioGroup radioGroup;
    private EditText etDescription, etSubject;
    private TextView tvError;
    private Button btnSave;
    private SessionManager sessionManager;
    private Gson gson;
    private Staff loggedInUser;
    private String academicYearId,loggedInUserId,instituteId;
    private SweetAlertDialog pDialog;
    private List<String> batchIdList = new ArrayList<>();
    private List<String> selectedBatchIdList = new ArrayList<>();
    private List<Batch> selectedBatchList = new ArrayList<>();
    private Batch batch;
    private Message message;
    private List<Batch> batchList = new ArrayList<>();
    //List<Section> sectionList = new ArrayList<>();
    private CheckBox cbBatch, cbStudent;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private ImageButton ibMic;
    private StringBuffer comment = new StringBuffer();
    private String category_add = "A";
    private int count;

    class Stu {
        String name;
        String id;
        String currentBatchId;
        String currentSectionId;
        Date createdDate;
    }

    List<Stu> originalStudentList = new ArrayList<>();
    List<Stu> filterStudentList = new ArrayList<>();
    List<Stu> selectedStudentList = new ArrayList<>();
    List<String> selectedStudentIdList = new ArrayList<>();

    public FragmentAddMessage() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.add_message));
        SessionManager sessionManager = new SessionManager(getContext());
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


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_message, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        radioGroup = view.findViewById(R.id.radioGroup);
        radioAllBatch = view.findViewById(R.id.radioAllBatch);
        radioSelectBatch = view.findViewById(R.id.radioSelectBatch);
        radioSelectStudent = view.findViewById(R.id.radioSelectStudent);
        llBatch = view.findViewById(R.id.llBatch);
        llSelectBatch = view.findViewById(R.id.llSelectBatch);
        llStudent = view.findViewById(R.id.llStudent);
        llSelectStudent = view.findViewById(R.id.llSelectStudent);
        ibMic = view.findViewById(R.id.ibMic);
        etSubject = view.findViewById(R.id.etSubject);
        etDescription = view.findViewById(R.id.etDescription);
        tvError = view.findViewById(R.id.tvError);
        btnSave = view.findViewById(R.id.btnSave);

        getBatches();

        getAllStudent();

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = (RadioButton) group.findViewById(checkedId);
                if (radioAllBatch.isChecked()) {
                    category_add = "A";
                    llBatch.setVisibility(View.GONE);
                    llStudent.setVisibility(View.GONE);
                }
                if (radioSelectBatch.isChecked()) {
                    category_add = "C";
                    llBatch.setVisibility(View.VISIBLE);
                    llStudent.setVisibility(View.GONE);
                }
                if (radioSelectStudent.isChecked()) {
                    category_add = "S";
                    llBatch.setVisibility(View.VISIBLE);
                    llStudent.setVisibility(View.VISIBLE);
                }
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvError.setVisibility(View.GONE);
                if (selectedBatchIdList.size() != 0) {
                    selectedBatchIdList.clear();
                }
                if (selectedStudentIdList.size() != 0) {
                    selectedStudentIdList.clear();
                }
                if (category_add.equalsIgnoreCase("C")) {
                    selectedStudentIdList.clear();
                    if (selectedBatchList.isEmpty()) {
                        tvError.setText("Please select any one class");
                        tvError.setVisibility(View.VISIBLE);
                        return;
                    } else {
                        for (Batch batch : selectedBatchList) {
                            selectedBatchIdList.add(batch.getId());
                        }
                    }
                }
                if (category_add.equalsIgnoreCase("S")) {
                    if (selectedBatchList.isEmpty()) {
                        tvError.setText("Please select any one class");
                        tvError.setVisibility(View.VISIBLE);
                        return;
                    } else {
                        for (Batch batch : selectedBatchList) {
                            selectedBatchIdList.add(batch.getId());
                        }
                    }
                    if (selectedStudentList.isEmpty()) {
                        tvError.setText("Please select any one student");
                        tvError.setVisibility(View.VISIBLE);
                        return;
                    } else {
                        for (Stu stu : selectedStudentList) {
                            selectedStudentIdList.add(stu.id);
                        }
                    }
                }
                String subject = etSubject.getText().toString().trim();
                System.out.println("subject" + subject);
                if (TextUtils.isEmpty(subject)) {
                    etSubject.setError("Enter subject");
                    etSubject.requestFocus();
                    return;
                }
                String description = etDescription.getText().toString().trim();
                System.out.println("description" + description);
                if (TextUtils.isEmpty(description)) {
                    etDescription.setError("Enter Description");
                    etDescription.requestFocus();
                    return;
                }
                if(academicYearId != null && instituteId != null && loggedInUserId != null) {
                    message = new Message();
                    message.setSubject(subject);
                    message.setDescription(description);
                    message.setAcademicYearId(academicYearId);
                    message.setBatchIdList(selectedBatchIdList);
                    message.setInstituteId(instituteId);
                    message.setAttachmentUrl("");
                    message.setStatus("A");
                    message.setRecipientIdList(selectedStudentIdList);
                    message.setCategory(category_add);
                    message.setCreatedDate(new Date());
                    message.setCreatorId(loggedInUserId);
                    message.setCreatorType("A");
                    message.setModifiedDate(new Date());
                    message.setModifierId("A");
                    message.setModifierType("A");
                    message.setRecipientType("P");

                    addMessage();
                }
            }
        });

        ibMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });
    }

    /**
     * Showing google speech input dialog
     */
    private void promptSpeechInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,
                getString(R.string.speech_prompt));
        try {
            startActivityForResult(intent, REQ_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException a) {
            Toast.makeText(getContext(),
                    getString(R.string.speech_not_supported),
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        comment.delete(0, comment.length());
        comment.append(etDescription.getText().toString());
        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    comment.append(result.get(0)).append("\n");
                    etDescription.setText(comment.toString());
                }
                break;
            }
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
                                    //Check box set up
                                    if (batchList.size() != 0) {
                                        llSelectBatch.removeAllViews();
                                        int i = 0;
                                        LinkedHashMap<Integer, String> batch_list = new LinkedHashMap<Integer, String>();
                                        for (Batch batch : batchList) {
                                            i++;
                                            batch_list.put(i, batch.getName());
                                        }
                                        Set<?> set = batch_list.entrySet();
                                        // Get an iterator
                                        Iterator<?> iterator = set.iterator();
                                        // Display elements
                                        while (iterator.hasNext()) {
                                            @SuppressWarnings("rawtypes")
                                            Map.Entry me = (Map.Entry) iterator.next();

                                            cbBatch = new CheckBox(getContext());
                                            cbBatch.setId(Integer.parseInt(me.getKey().toString()));
                                            cbBatch.setText(me.getValue().toString());
                                            cbBatch.setOnClickListener(getOnClickBatch(cbBatch));
                                            llSelectBatch.addView(cbBatch);
                                        }
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

    private void addMessage() {

        if (!pDialog.isShowing()) {
            pDialog.show();
        }
        messageCollectionRef
                .add(message)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
        if (pDialog != null) {
            pDialog.dismiss();
        }
        SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                .setTitleText("Success")
                .setContentText("Messages successfully added")
                .setConfirmText("Ok")
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                        FragmentMessage fragmentMessage = new FragmentMessage();
                        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                        fragmentTransaction.replace(R.id.contentLayout, fragmentMessage).addToBackStack(null).commit();
                    }
                });
        dialog.setCancelable(false);
        dialog.show();

    }

    View.OnClickListener getOnClickBatch(final Button button) {
        return new View.OnClickListener() {
            public void onClick(View v) {
                boolean checked = ((CheckBox) v).isChecked();
                // Check which checkbox was clicked
                if (checked) {
                    for (Batch batch : batchList) {
                        if (button.getText().toString().equals(batch.getName())) {
                            // System.out.println("Batch Name" + button.getText().toString());
                            selectedBatchList.add(batch);
                            break;
                        }
                    }
                    if (category_add.equalsIgnoreCase("S")) {
                        //getSectionsOfClass();
                        filterStudentFromSchool();
                    }
                } else {
                    for (Batch batch : batchList) {
                        if (button.getText().toString().equals(batch.getName())) {
                            selectedBatchList.remove(batch);
                            break;
                        }
                    }
                    if (category_add.equalsIgnoreCase("S")) {
                        //getSectionsOfClass();
                        filterStudentFromSchool();
                    }
                }
            }
        };
    }

    View.OnClickListener getOnClickStudent(final Button button) {
        return new View.OnClickListener() {
            public void onClick(View v) {
                boolean checked = ((CheckBox) v).isChecked();
                // Check which checkbox was clicked
                if (checked) {
                    for (Stu stu : filterStudentList) {
                        if (button.getText().toString().equals(stu.name)) {
                            // System.out.println("Batch Name" + button.getText().toString());
                            selectedStudentList.add(stu);
                            break;
                        }
                    }
                } else {
                    for (Stu stu : filterStudentList) {
                        if (button.getText().toString().equals(stu.name)) {
                            selectedStudentList.remove(stu);
                            break;
                        }
                    }

                }

            }
        };
    }

    private void getAllStudent() {
        if(academicYearId != null) {
            if (pDialog != null && !pDialog.isShowing()) {
                pDialog.show();
            }
            if (originalStudentList.size() != 0) {
                originalStudentList.clear();
            }
            studentCollectionRef
                    .whereEqualTo("academicYearId", academicYearId)
                    .whereIn("status", Arrays.asList("A", "N"))
                    .orderBy("createdDate", Query.Direction.ASCENDING)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                                Student student = documentSnapshot.toObject(Student.class);
                                String name = student.getFirstName();
                                if (!TextUtils.isEmpty(student.getMiddleName())) {
                                    name = name + " " + student.getMiddleName();
                                }
                                if (!TextUtils.isEmpty(student.getLastName())) {
                                    name = name + " " + student.getLastName();
                                }
                                Stu stu = new Stu();
                                stu.id = documentSnapshot.getId();
                                stu.name = name;
                                stu.currentBatchId = student.getCurrentBatchId();
                                stu.currentSectionId = student.getCurrentSectionId();
                                stu.createdDate = student.getCreatedDate();
                                originalStudentList.add(stu);
                            }
                            if (pDialog != null && pDialog.isShowing()) {
                                pDialog.dismiss();
                            }
                            System.out.println("originalStudentList " + originalStudentList.size());
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
    }

    private void filterStudentFromSchool() {
        if (pDialog != null && !pDialog.isShowing()) {
            pDialog.show();
        }
        if (filterStudentList.size() != 0) {
            filterStudentList.clear();
        }
        for (Batch batch:selectedBatchList) {
            for (int i = 0; i < originalStudentList.size(); i++) {
                Stu object = originalStudentList.get(i);
                if (batch.getId().equalsIgnoreCase(object.currentBatchId)) {
                    filterStudentList.add(object);
                }
            }
        }
        System.out.println("studentList " + filterStudentList.size());
        if (filterStudentList.size() != 0) {
            btnSave.setVisibility(View.VISIBLE);
            tvError.setVisibility(View.GONE);
            llSelectStudent.removeAllViews();
            int i = 0;
            LinkedHashMap<Integer, String> student_list = new LinkedHashMap<Integer, String>();
            for (Stu stu : filterStudentList) {
                i++;
                student_list.put(i, stu.name);
            }
            Set<?> set = student_list.entrySet();
            // Get an iterator
            Iterator<?> iterator = set.iterator();
            // Display elements
            while (iterator.hasNext()) {
                @SuppressWarnings("rawtypes")
                Map.Entry me = (Map.Entry) iterator.next();

                cbStudent = new CheckBox(getContext());
                cbStudent.setId(Integer.parseInt(me.getKey().toString()));
                cbStudent.setText(me.getValue().toString());
                cbStudent.setOnClickListener(getOnClickStudent(cbStudent));
                llSelectStudent.addView(cbStudent);
            }
            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }
        } else {
            if (pDialog != null && pDialog.isShowing()) {
                pDialog.dismiss();
            }
            tvError.setText("No student for this classes, so you can't add message");
            tvError.setVisibility(View.VISIBLE);
            btnSave.setVisibility(View.GONE);
        }
    }
}
