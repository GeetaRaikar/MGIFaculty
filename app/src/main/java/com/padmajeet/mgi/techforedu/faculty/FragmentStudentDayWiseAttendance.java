package com.padmajeet.mgi.techforedu.faculty;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.padmajeet.mgi.techforedu.faculty.model.Attendance;
import com.padmajeet.mgi.techforedu.faculty.model.Staff;
import com.padmajeet.mgi.techforedu.faculty.model.Subject;
import com.padmajeet.mgi.techforedu.faculty.util.SessionManager;
import com.padmajeet.mgi.techforedu.faculty.util.Utility;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentStudentDayWiseAttendance extends Fragment {

    private ListView lvDayWiseAttendance;
    private LinearLayout llNoList;
    private ImageView ivNoData;
    private TextView tvNoData;
    private Gson gson;
    private String academicYearId, instituteId,studentId,currentBatchId;
    private List<Subject> subjectList= new ArrayList<>();
    private Attendance attendance;
    private HashMap<Integer,List<Attendance>> attendanceHashMap = new HashMap<>();
    private List<List<Attendance>> dayWiseAttendanceList = new ArrayList<>();
    private List<Integer> countList = new ArrayList<>();
    private SessionManager sessionManager;
    private Staff loggedInUser;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference attendanceCollectionRef = db.collection("Attendance");
    private List<Attendance> attendanceList = new ArrayList<>();

    public FragmentStudentDayWiseAttendance() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sessionManager = new SessionManager(getContext());
        String facultyJson = sessionManager.getString("loggedInUser");
        gson = Utility.getGson();
        loggedInUser = gson.fromJson(facultyJson, Staff.class);
        academicYearId = sessionManager.getString("academicYearId");
        Bundle bundle = getArguments();
        studentId = bundle.getString("studentId");
        currentBatchId = bundle.getString("currentBatchId");
        String subjectListJson = bundle.getString("subjectList");
        System.out.println("subjectListJson - "+subjectListJson);
        subjectList = gson.fromJson(subjectListJson,new TypeToken<List<Subject>>(){}.getType());
        System.out.println("size "+subjectList.size());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_student_day_wise_attendance, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //((ActivityHome)getActivity()).getSupportActionBar().setTitle(getString(R.string.daywiseAttendance));
        lvDayWiseAttendance = view.findViewById(R.id.lvDayWiseAttendance);
        llNoList = view.findViewById(R.id.llNoList);
        tvNoData = view.findViewById(R.id.tvNoData);
        ivNoData = view.findViewById(R.id.ivNoData);
        getAttendanceOfStudent();
    }
    private void getAttendanceOfStudent(){
        attendanceList.clear();
        attendanceCollectionRef
                .whereEqualTo("academicYearId",academicYearId)
                .whereEqualTo("batchId",currentBatchId)
                .whereEqualTo("studentId",studentId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot documentSnapshot:queryDocumentSnapshots.getDocuments()) {
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            attendance = documentSnapshot.toObject(Attendance.class);
                            attendance.setId(documentSnapshot.getId());
                            attendanceList.add(attendance);
                        }
                        if(attendanceList.size() > 0) {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                            int k =0;
                            for(int i=0;i<attendanceList.size();i++){
                                Date attendanceDate = attendanceList.get(i).getDate();
                                if(attendanceDate!=null){
                                    List<Attendance> tempAttendanceList = new ArrayList<>();
                                    tempAttendanceList.add(attendanceList.get(i));
                                    System.out.println("First Date - "+attendanceDate);
                                    for(int j=i+1;j<attendanceList.size();j++){
                                        Date nextDate = attendanceList.get(j).getDate();
                                        System.out.println("Next Date - "+nextDate);
                                        if(sdf.format(attendanceDate).equals(sdf.format(nextDate))){
                                            System.out.println("Matched");
                                            tempAttendanceList.add(attendanceList.get(j));
                                            i++;
                                        }
                                        else{
                                            break;
                                        }
                                    }
                                    String keyDate = sdf.format(attendanceDate);

                                    attendanceHashMap.put(k,tempAttendanceList);
                                    dayWiseAttendanceList.add(tempAttendanceList);
                                    countList.add(k);
                                    k++;
                                }
                            }
                            System.out.println("HashMap size - "+attendanceHashMap.size());
                            if(dayWiseAttendanceList.size()>0){
                                AttendanceAdaptor attendanceAdaptor = new AttendanceAdaptor(getContext());
                                lvDayWiseAttendance.setAdapter(attendanceAdaptor);
                            }
                        }else{
                            lvDayWiseAttendance.setVisibility(View.GONE);
                            llNoList.setVisibility(View.VISIBLE);
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                    }
                });

        }
    class AttendanceAdaptor extends ArrayAdapter<Integer>{
        Context context;
        public AttendanceAdaptor(@NonNull Context context) {
            super(context, R.layout.row_day_wise_attendance,countList);
            this.context = context;
        }
        private class ViewHolder{
            private TextView date;
            private Button session1,session2,session3,session4,session5,session6,session7;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            System.out.println("position - "+position);
            View row = convertView;
            ViewHolder holder = new ViewHolder();
            if(convertView == null){
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                row = inflater.inflate(R.layout.row_day_wise_attendance,parent,false);
                holder.date = (TextView) row.findViewById(R.id.date);
                holder.session1 = (Button) row.findViewById(R.id.session1);
                holder.session2 = (Button) row.findViewById(R.id.session2);
                holder.session3 = (Button) row.findViewById(R.id.session3);
                holder.session4 = (Button) row.findViewById(R.id.session4);
                holder.session5 = (Button) row.findViewById(R.id.session5);
                holder.session6 = (Button) row.findViewById(R.id.session6);
                holder.session7 = (Button) row.findViewById(R.id.session7);
                row.setTag(holder);
            }
            else{
                holder = (ViewHolder) row.getTag();
            }
            List<Attendance> currentAttendanceList = attendanceHashMap.get(position);
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yy");
            String currentDate = sdf.format(currentAttendanceList.get(0).getDate());
            holder.date.setText(""+currentDate);
            for(int i=0;i<currentAttendanceList.size();i++){
                Attendance curAttendance = currentAttendanceList.get(i);
                String subName = "";
                for (Subject subject:subjectList){
                    if(curAttendance.getSubjectId().equals(subject.getId())){
                        subName = subject.getCode();
                        break;
                    }
                }
                switch(i){
                    case 0: holder.session1.setVisibility(View.VISIBLE);
                        holder.session1.setText(subName);
                        if(curAttendance.getStatus().equalsIgnoreCase("P")){
                            //Drawable d = getResources().getDrawable(android.R.drawable.b);
                            //holder.session1.setBackground(R.drawable.add_btn_back);
                            holder.session1.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                        }
                        else if(curAttendance.getStatus().equalsIgnoreCase("A")){
                            holder.session1.setBackgroundColor(getResources().getColor(R.color.colorRed));
                        }
                        break;
                    case 1: holder.session2.setVisibility(View.VISIBLE);
                        holder.session2.setText(subName);
                        if(curAttendance.getStatus().equalsIgnoreCase("P")){
                            //Drawable d = getResources().getDrawable(android.R.drawable.b);
                            //holder.session1.setBackground(R.drawable.add_btn_back);
                            holder.session2.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                        }
                        else if(curAttendance.getStatus().equalsIgnoreCase("A")){
                            holder.session2.setBackgroundColor(getResources().getColor(R.color.colorRed));
                        }
                        break;
                    case 2: holder.session3.setVisibility(View.VISIBLE);
                        holder.session3.setText(subName);
                        if(curAttendance.getStatus().equalsIgnoreCase("P")){
                            //Drawable d = getResources().getDrawable(android.R.drawable.b);
                            //holder.session1.setBackground(R.drawable.add_btn_back);
                            holder.session3.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                        }
                        else if(curAttendance.getStatus().equalsIgnoreCase("A")){
                            holder.session3.setBackgroundColor(getResources().getColor(R.color.colorRed));
                        }
                        break;
                    case 3: holder.session4.setVisibility(View.VISIBLE);
                        holder.session4.setText(subName);
                        if(curAttendance.getStatus().equalsIgnoreCase("P")){
                            //Drawable d = getResources().getDrawable(android.R.drawable.b);
                            //holder.session1.setBackground(R.drawable.add_btn_back);
                            holder.session4.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                        }
                        else if(curAttendance.getStatus().equalsIgnoreCase("A")){
                            holder.session4.setBackgroundColor(getResources().getColor(R.color.colorRed));
                        }
                        break;
                    case 4: holder.session5.setVisibility(View.VISIBLE);
                        holder.session5.setText(subName);
                        if(curAttendance.getStatus().equalsIgnoreCase("P")){
                            //Drawable d = getResources().getDrawable(android.R.drawable.b);
                            //holder.session1.setBackground(R.drawable.add_btn_back);
                            holder.session5.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                        }
                        else if(curAttendance.getStatus().equalsIgnoreCase("A")){
                            holder.session5.setBackgroundColor(getResources().getColor(R.color.colorRed));
                        }
                        break;
                    case 5: holder.session6.setVisibility(View.VISIBLE);
                        holder.session1.setText(subName);
                        if(curAttendance.getStatus().equalsIgnoreCase("P")){
                            //Drawable d = getResources().getDrawable(android.R.drawable.b);
                            //holder.session1.setBackground(R.drawable.add_btn_back);
                            holder.session6.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                        }
                        else if(curAttendance.getStatus().equalsIgnoreCase("A")){
                            holder.session6.setBackgroundColor(getResources().getColor(R.color.colorRed));
                        }
                        break;
                    case 6: holder.session7.setVisibility(View.VISIBLE);
                        holder.session7.setText(subName);
                        if(curAttendance.getStatus().equalsIgnoreCase("P")){
                            //Drawable d = getResources().getDrawable(android.R.drawable.b);
                            //holder.session1.setBackground(R.drawable.add_btn_back);
                            holder.session7.setBackgroundColor(getResources().getColor(R.color.colorGreen));
                        }
                        else if(curAttendance.getStatus().equalsIgnoreCase("A")){
                            holder.session7.setBackgroundColor(getResources().getColor(R.color.colorRed));
                        }
                        break;
                }
            }
            return row;
        }
    }
}
