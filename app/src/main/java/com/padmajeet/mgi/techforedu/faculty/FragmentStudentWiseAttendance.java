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
import android.widget.ImageButton;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.padmajeet.mgi.techforedu.faculty.model.Attendance;
import com.padmajeet.mgi.techforedu.faculty.model.Batch;
import com.padmajeet.mgi.techforedu.faculty.model.Staff;
import com.padmajeet.mgi.techforedu.faculty.model.Student;
import com.padmajeet.mgi.techforedu.faculty.model.Subject;
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
public class FragmentStudentWiseAttendance extends Fragment {

    private View view = null;
    private Spinner spBatch,spSubject;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference attendanceCollectionRef = db.collection("Attendance");
    private CollectionReference batchCollectionRef = db.collection("Batch");
    private CollectionReference studentCollectionRef = db.collection("Student");
    private CollectionReference subjectCollectionRef = db.collection("Subject");
    private Batch selectedBatch,batch;
    private LinearLayout llNoList;
    private Bundle bundle = new Bundle();
    private Attendance attendance;
    private Subject subject, selectedSubject;
    private Student student;
    private List<Batch> batchList = new ArrayList<>();
    private List<Attendance> attendanceList = new ArrayList<>();
    private List<Student> studentList = new ArrayList<>();
    private List<Subject> subjectList = new ArrayList<>();
    private RecyclerView rvAttendance;
    private RecyclerView.Adapter attendanceAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private StudentAttendance studentAttendance;
    private List<StudentAttendance> studentAttendanceList=new ArrayList<>();
    private Gson gson;
    private Staff loggedInUser;
    private String instituteId,academicYearId;
    private SweetAlertDialog pDialog;
    private List<String> batchIdList;

    public class StudentAttendance{
        int totalCount;
        int presentCount;
        Student student;
    }

    public FragmentStudentWiseAttendance() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(getContext());
        gson = Utility.getGson();
        String userJson=sessionManager.getString("loggedInUser");
        loggedInUser=gson.fromJson(userJson, Staff.class);
        instituteId=sessionManager.getString("instituteId");
        academicYearId=sessionManager.getString("academicYearId");

        String batchIdJson = sessionManager.getString("batchIdList");
        batchIdList = gson.fromJson(batchIdJson,new TypeToken<List<String>>() {
        }.getType());
        System.out.println("sessionManager batchIdList "+batchIdJson+" value "+batchIdList.toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_student_wise_attendance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((ActivityHome)getActivity()).getSupportActionBar().setTitle(getString(R.string.studentWiseAttendance));
        spBatch = view.findViewById(R.id.spBatch);
        spSubject = view.findViewById(R.id.spSubject);
        rvAttendance = (RecyclerView) view.findViewById(R.id.rvAttendance);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(getContext());
        rvAttendance.setLayoutManager(layoutManager);
        llNoList = view.findViewById(R.id.llNoList);
        pDialog = Utility.createSweetAlertDialog(getContext());
        getBatches();
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
                                System.out.println("batchList " + batchList.size());
                                if (setBatch.size() == batchList.size()) {
                                    if (pDialog != null) {
                                        pDialog.dismiss();
                                    }
                                    //Spinner set up
                                    if (batchList.size() != 0) {
                                        System.out.println("batchList " + batchList.size());
                                        List<String> batchNameList = new ArrayList<>();
                                        for (Batch batch : batchList) {
                                            batchNameList.add(batch.getName());
                                        }
                                        ArrayAdapter<String> batchAdaptor = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, batchNameList);
                                        batchAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                        spBatch.setAdapter(batchAdaptor);

                                        spBatch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                            @Override
                                            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                                selectedBatch = batchList.get(position);
                                                // gson = Utility.getGson();
                                                // selectedBatchCalender = gson.toJson(selectedBatch);
                                                getSubjectOfBatch();
                                            }

                                            @Override
                                            public void onNothingSelected(AdapterView<?> parent) {

                                            }
                                        });
                                    } else {
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
    private void getSubjectOfBatch() {
        if(subjectList.size()!=0){
            subjectList.clear();
        }
        subjectCollectionRef
                .whereEqualTo("batchId",selectedBatch.getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(pDialog!=null){
                            pDialog.dismiss();
                        }
                        for (DocumentSnapshot documentSnapshot:queryDocumentSnapshots.getDocuments()) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            subject = documentSnapshot.toObject(Subject.class);
                            subject.setId(documentSnapshot.getId());
                            //System.out.println("Section "+document.getId());
                            subjectList.add(subject);
                        }
                        if(subjectList.size()!=0) {
                            llNoList.setVisibility(View.GONE);
                            List<String> subjectNameList = new ArrayList<>();
                            for (Subject subject : subjectList) {
                                subjectNameList.add(subject.getName());
                            }
                            ArrayAdapter<String> sectionAdaptor = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, subjectNameList);
                            sectionAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spSubject.setAdapter(sectionAdaptor);

                            spSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    selectedSubject = subjectList.get(position);
                                    // gson = Utility.getGson();
                                    // selectedBatchCalender = gson.toJson(selectedBatch);
                                    getStudentOfBatch();

                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                        }
                        else{
                            llNoList.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(pDialog!=null){
                            pDialog.dismiss();
                        }
                    }
                });
        // [END get_all_users]
    }
    private void getStudentOfBatch() {
        if(studentList.size()!=0){
            studentList.clear();
        }
        if(studentAttendanceList.size()!=0){
            studentAttendanceList.clear();
        }
        if(!pDialog.isShowing()) {
            pDialog.show();
        }
        studentCollectionRef
                .whereEqualTo("currentBatchId",selectedBatch.getId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(pDialog!=null){
                            pDialog.dismiss();
                        }
                        for (DocumentSnapshot documentSnapshot:queryDocumentSnapshots.getDocuments()) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            student = documentSnapshot.toObject(Student.class);
                            student.setId(documentSnapshot.getId());
                            // System.out.println("Student "+document.getId());
                            studentList.add(student);
                        }
                        if(studentList.size()!=0) {
                            rvAttendance.setVisibility(View.VISIBLE);
                            llNoList.setVisibility(View.GONE);
                            getAttendanceList();

                        }else{
                            rvAttendance.setVisibility(View.GONE);
                            llNoList.setVisibility(View.VISIBLE);

                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if(pDialog!=null){
                            pDialog.dismiss();
                        }
                    }
                });
        // [END get_all_users]
    }
    private void getAttendanceList(){
        if(academicYearId != null) {
            if(attendanceList.size()!=0){
                attendanceList.clear();
            }
            if(studentAttendanceList.size()!=0){
                studentAttendanceList.clear();
            }

            if(!pDialog.isShowing()){
                pDialog.show();
            }
            attendanceCollectionRef
                    .whereEqualTo("academicYearId", academicYearId)
                    .whereEqualTo("batchId", selectedBatch.getId())
                    .whereEqualTo("subjectId", selectedSubject.getId())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                        @Override
                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                            if (pDialog != null) {
                                pDialog.dismiss();
                            }
                            for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                                attendance = documentSnapshot.toObject(Attendance.class);
                                attendance.setId(documentSnapshot.getId());
                                //  System.out.println("Attendance "+document.getId());
                                attendanceList.add(attendance);
                            }


                            if (attendanceList.size() != 0) {
                                rvAttendance.setVisibility(View.VISIBLE);
                                llNoList.setVisibility(View.GONE);
                                //totalClassConducted=(attendanceList.size()/studentList.size());
                                for (Student student : studentList) {
                                    int totalClass = 0;
                                    int totalPresence = 0;
                                    studentAttendance = new StudentAttendance();
                                    for (Attendance attendance : attendanceList) {
                                        if (student.getId().equals(attendance.getStudentId())) {
                                            totalClass++;
                                            if (attendance.getStatus().equalsIgnoreCase("P")) {
                                                totalPresence++;
                                            }
                                        }
                                    }
                                    //System.out.println("TotalPresent class - "+totalPresence);
                                    studentAttendance.totalCount = totalClass;
                                    studentAttendance.presentCount = totalPresence;
                                    studentAttendance.student = student;
                                    studentAttendanceList.add(studentAttendance);
                                    // System.out.println("TotalPresent student - "+studentAttendance.presentCount+" / "+studentAttendance.totalCount);
                                    if (studentAttendanceList.size() == studentList.size()) {
                                        for (int i = 0; i < studentAttendanceList.size(); i++) {
                                            if (studentAttendanceList.get(i).totalCount == 0) {
                                                studentAttendanceList.remove(studentAttendanceList.get(i));
                                            }
                                        }
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
                                        //adapter
                                        attendanceAdapter = new AttendanceAdapter(studentAttendanceList);
                                        rvAttendance.setAdapter(attendanceAdapter);
                                    }
                                }

                            } else {
                                rvAttendance.setVisibility(View.GONE);
                                llNoList.setVisibility(View.VISIBLE);

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

    class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.MyViewHolder> {
        private List<StudentAttendance> studentAttendanceList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvName, tvTotalClass,tvPresentClass,tvAttendancePercentage;
            private ImageView ivProfilePic;
            private ImageButton ibDayWiseAttendance;

            public MyViewHolder(View view) {
                super(view);
                tvName = view.findViewById(R.id.tvName);
                tvTotalClass = view.findViewById(R.id.tvTotalClass);
                tvPresentClass=view.findViewById(R.id.tvPresentClass);
                ivProfilePic=view.findViewById(R.id.ivProfilePic);
                tvAttendancePercentage = view.findViewById(R.id.tvAttendancePercentage);
                ibDayWiseAttendance = view.findViewById(R.id.ibDayWiseAttendance);
            }
        }


        public AttendanceAdapter(List<StudentAttendance> studentAttendanceList) {
            this.studentAttendanceList = studentAttendanceList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_attendance, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final StudentAttendance studentAttendance = studentAttendanceList.get(position);
            holder.tvName.setText(""+studentAttendance.student.getFirstName()+" "+studentAttendance.student.getLastName());
            holder.tvTotalClass.setText(""+studentAttendance.totalCount);
            holder.tvPresentClass.setText(""+studentAttendance.presentCount);
            float percentage = 0f;
            if(studentAttendance.totalCount>0) {
                percentage = (studentAttendance.presentCount * 100f) / studentAttendance.totalCount;
            }
            holder.tvAttendancePercentage.setText(""+percentage+"%");
            //System.out.println("StudentAttendance - "+studentAttendance.presentCount);
            String url = "" + studentAttendance.student.getImageUrl();
            //System.out.println("Image path" + url);
            if (!TextUtils.isEmpty(url)) {
                Glide.with(getContext())
                        .load(url)
                        .fitCenter()
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.ic_student)
                        .into(holder.ivProfilePic);
            }

            holder.ibDayWiseAttendance.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle bundle = new Bundle();
                    bundle.putString("studentId", studentAttendance.student.getId());
                    bundle.putString("currentBatchId", selectedBatch.getId());
                    bundle.putString("subjectList",gson.toJson(subjectList));
                    FragmentStudentDayWiseAttendance fragmentStudentDayWiseAttendance = new FragmentStudentDayWiseAttendance();
                    fragmentStudentDayWiseAttendance.setArguments(bundle);
                    FragmentManager manager = getActivity().getSupportFragmentManager();
                    FragmentTransaction fragmentTransaction = manager.beginTransaction();
                    fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    fragmentTransaction.replace(R.id.contentLayout, fragmentStudentDayWiseAttendance);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                }
            });
        }

        @Override
        public int getItemCount() {
            return studentAttendanceList.size();
        }
    }


}
