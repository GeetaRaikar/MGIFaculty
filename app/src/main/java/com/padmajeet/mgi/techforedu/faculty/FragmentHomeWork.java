package com.padmajeet.mgi.techforedu.faculty;


import android.app.DatePickerDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.documentfile.provider.DocumentFile;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import cn.pedant.SweetAlert.SweetAlertDialog;

import android.os.Handler;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.common.util.NumberUtils;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.padmajeet.mgi.techforedu.faculty.model.Batch;
import com.padmajeet.mgi.techforedu.faculty.model.BatchSubjectFaculty;
import com.padmajeet.mgi.techforedu.faculty.model.HomeWork;
import com.padmajeet.mgi.techforedu.faculty.model.Section;
import com.padmajeet.mgi.techforedu.faculty.model.Staff;
import com.padmajeet.mgi.techforedu.faculty.model.Subject;
import com.padmajeet.mgi.techforedu.faculty.util.SessionManager;
import com.padmajeet.mgi.techforedu.faculty.util.Utility;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static android.app.Activity.RESULT_OK;
import static android.os.Environment.DIRECTORY_DOWNLOADS;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentHomeWork extends Fragment {

    private View view = null;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference batchCollectionRef = db.collection("Batch");
    private CollectionReference sectionCollectionRef = db.collection("Section");
    private CollectionReference homeWorkCollectionRef = db.collection("HomeWork");
    private CollectionReference subjectCollectionRef = db.collection("Subject");
    private ListenerRegistration homeWorkListener,subjectListener;
    private Spinner spBatch, spSection, spClass, spDivision, spSubject;
    private EditText etName, etCreatedDate, etDueDate;
    private TextView tvAttachmentFile;
    private ImageButton ibChooseFile,ibRemoveFile;
    private Button btnSave;
    private ImageView ivCreatedDate, ivDueDate;
    private RecyclerView rvHomeWork;
    private LinearLayout llNoList;
    private RecyclerView.Adapter homeWorkAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private SweetAlertDialog pDialog;
    private DatePickerDialog picker;
    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private SessionManager sessionManager;
    private Gson gson;
    private String loggedInUserId, academicYearId, instituteId;
    private Staff loggedInUser;
    private List<BatchSubjectFaculty> batchSubjectFacultyList = new ArrayList<>();
    private BatchSubjectFaculty batchSubjectFaculty;
    private List<Subject> subjectList = new ArrayList<>();
    private Subject subject, selectedSubject;
    private List<Section> filterSectionList = new ArrayList<>();
    private List<String> sectionIdList = new ArrayList<>();
    private List<Section> sectionList = new ArrayList<>();
    private Section section, selectedSection;
    private List<String> batchIdList = new ArrayList<>();
    private List<Batch> batchList = new ArrayList<>();
    private Batch batch, selectedBatch;
    private List<HomeWork> homeWorkList = new ArrayList<>();
    private HomeWork homeWork;
    private BottomSheetDialog bottomSheetDialog;
    private int subSpinnerPos;
    private DownloadManager downloadManager;
    private final int PICKFILE_RESULT_CODE=100;
    private final int REQUEST_CODE=100;
    private final int PICK_FROM_CAMERA = 1;
    private final int PICK_FROM_GALLARY = 2;
    private Uri attachUri;
    private StorageTask mUploadTask;
    private StorageReference storageReference;
    private String attachmentUrl;


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
        /*String sectionIdJson = sessionManager.getString("sectionIdList");
        sectionIdList = gson.fromJson(sectionIdJson,new TypeToken<List<String>>() {
        }.getType());
        System.out.println("sessionManager sectionIdList "+sectionIdJson+" value "+sectionIdList.toString());*/
        pDialog = Utility.createSweetAlertDialog(getContext());

        storageReference= FirebaseStorage.getInstance().getReference("Document");
    }

    public FragmentHomeWork() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_home_work, container, false);
        ((ActivityHome) getActivity()).getSupportActionBar().setTitle(getString(R.string.home_work));
        spBatch = view.findViewById(R.id.spBatch);
        //spSection = view.findViewById(R.id.spSection);
        rvHomeWork = (RecyclerView) view.findViewById(R.id.rvHomeWork);
        layoutManager = new LinearLayoutManager(getContext());
        rvHomeWork.setLayoutManager(layoutManager);
        llNoList = view.findViewById(R.id.llNoList);

        //getSections();

        getBatches();
        spBatch.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedBatch = batchList.get(position);
                //getSectionsOfBatch();
                getHomeWork();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        /*
        spSection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSection = filterSectionList.get(position);
                getHomeWork();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });*/
        FloatingActionButton fab = (FloatingActionButton) view.findViewById(R.id.addHomeWork);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createBottomSheet();
                bottomSheetDialog.show();
            }
        });
        if(batchIdList.size() == 0){
            fab.hide();
        }
        return view;
    }

    /*
    private void getSections() {
        if (!pDialog.isShowing()) {
            pDialog.show();
        }
        if (sectionList.size() != 0) {
            sectionList.clear();
        }
        for (String x : sectionIdList) {
            //batchIdList.add(batchFacultyList.get(i).getBatchId());
            sectionCollectionRef.document("/" + x)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            section = documentSnapshot.toObject(Section.class);
                            section.setId(documentSnapshot.getId());
                            sectionList.add(section);
                            if (sectionIdList.size() == sectionList.size()) {
                                if (pDialog != null) {
                                    pDialog.dismiss();
                                }
                                getBatches();
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
    */
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
                                        //getSectionsOfBatch();
                                        getHomeWork();
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
        }else{
            spBatch.setVisibility(View.GONE);
            rvHomeWork.setVisibility(View.GONE);
            llNoList.setVisibility(View.VISIBLE);
        }
    }
    /*
    private void getSectionsOfBatch() {
        if (filterSectionList.size() > 0) {
            filterSectionList.clear();
        }
        for (Section section1 : sectionList) {
            if (section1.getBatchId().equalsIgnoreCase(selectedBatch.getId())) {
                filterSectionList.add(section1);
            }
        }
        spSection.setVisibility(View.VISIBLE);

        List<String> sectionNameList = new ArrayList<>();
        for (Section section : filterSectionList) {
            sectionNameList.add(section.getName());
        }
        ArrayAdapter<String> sectionAdaptor = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, sectionNameList);
        sectionAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spSection.setAdapter(sectionAdaptor);
        selectedSection=filterSectionList.get(0);
        getHomeWork();
    }
    */
    private void getHomeWork() {
        if(selectedBatch != null) {
            if (homeWorkList.size() != 0) {
                homeWorkList.clear();
            }
            if (!pDialog.isShowing()) {
                pDialog.show();
            }
            homeWorkListener = homeWorkCollectionRef
                    .whereEqualTo("batchId", selectedBatch.getId())
                    //.whereEqualTo("sectionId", selectedSection.getId())
                    .orderBy("createdDate", Query.Direction.DESCENDING)
                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                            if (e != null) {
                                return;
                            }
                            if (homeWorkList.size() != 0) {
                                homeWorkList.clear();
                            }
                            if (pDialog != null) {
                                pDialog.dismiss();
                            }
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                homeWork = document.toObject(HomeWork.class);
                                homeWork.setId(document.getId());
                                homeWorkList.add(homeWork);
                            }
                            if (homeWorkList.size() != 0) {
                                if (pDialog != null) {
                                    pDialog.dismiss();
                                }
                                for (HomeWork s : homeWorkList) {
                                    System.out.println("homeWorkList " + s.getName());
                                }
                                rvHomeWork.setVisibility(View.VISIBLE);
                                llNoList.setVisibility(View.GONE);
                                homeWorkAdapter = new HomeWorkAdapter(homeWorkList);
                                rvHomeWork.setAdapter(homeWorkAdapter);
                            } else {
                                if (pDialog != null) {
                                    pDialog.dismiss();
                                }
                                rvHomeWork.setVisibility(View.GONE);
                                llNoList.setVisibility(View.VISIBLE);
                            }
                        }
                    });
        }
    }
    class HomeWorkAdapter extends RecyclerView.Adapter<HomeWorkAdapter.MyViewHolder> {
        private List<HomeWork> homeWorkList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView tvName, tvSubjectName, tvCreatedDate, tvDueDate;
            public ImageView ivAttachmentHomeWork,ivSubjectPic;
            View row;

            public MyViewHolder(View view) {
                super(view);
                tvName = view.findViewById(R.id.tvName);
                tvSubjectName = view.findViewById(R.id.tvSubjectName);
                tvCreatedDate = view.findViewById(R.id.tvCreatedDate);
                tvDueDate = view.findViewById(R.id.tvDueDate);
                ivAttachmentHomeWork=view.findViewById(R.id.ivAttachmentHomeWork);
                ivSubjectPic= view.findViewById(R.id.ivSubjectPic);
                row = view;
            }
        }


        public HomeWorkAdapter(List<HomeWork> homeWorkList) {
            this.homeWorkList = homeWorkList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_home_work, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            final HomeWork homeWork = homeWorkList.get(position);
            holder.tvName.setText("" + homeWork.getName());
            subjectCollectionRef.document("/" + homeWork.getSubjectId())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            subject = documentSnapshot.toObject(Subject.class);
                            if(subject == null){
                                holder.tvSubjectName.setText("" + homeWork.getSubjectName());
                            }else{
                                holder.tvSubjectName.setText("" + subject.getName());
                            }

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            holder.tvSubjectName.setVisibility(View.GONE);
                        }
                    });
            holder.tvCreatedDate.setText("" + Utility.formatDateToString(homeWork.getCreatedDate().getTime()));
            holder.tvDueDate.setText("" + Utility.formatDateToString(homeWork.getDueDate().getTime()));

            if(TextUtils.isEmpty(homeWork.getAttachmentUrl())){
                holder.ivAttachmentHomeWork.setVisibility(View.GONE);
            }else{
                holder.ivAttachmentHomeWork.setVisibility(View.VISIBLE);
            }
            holder.ivAttachmentHomeWork.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String url = homeWork.getAttachmentUrl();
                    String fileName = url.substring(url.lastIndexOf('/') + 1);
                    String fileName1 = fileName.substring(0, fileName.lastIndexOf('?'));
                    String[] fileName2 = fileName1.split("%2F");
                    System.out.println("fileName2 "+fileName2[1]);
                    downloadManager=(DownloadManager)getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                    Uri uri=Uri.parse(url);
                    DownloadManager.Request request=new DownloadManager.Request(uri);
                    //Restrict the types of networks over which this download may proceed.
                    request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                    // Makes download visible in notifications while downloading, but disappears after download completes. Optional.
                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                    request.setDestinationInExternalFilesDir(getContext(),DIRECTORY_DOWNLOADS,fileName2[1]);
                    final long enqueue = downloadManager.enqueue(request);

                    BroadcastReceiver receiver = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            String action = intent.getAction();
                            if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
                                Toast.makeText(getContext(), "Download Completed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    };

                    getContext().registerReceiver(receiver,
                            new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
                }
            });


        }

        @Override
        public int getItemCount() {
            return homeWorkList.size();
        }
    }
    private void getSubjects() {
        if (!pDialog.isShowing()) {
            pDialog.show();
        }
        subjectListener = subjectCollectionRef
                .whereEqualTo("batchId", selectedBatch.getId())
                .orderBy("createdDate", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            return;
                        }
                        if (subjectList.size() != 0) {
                            subjectList.clear();
                        }
                        for (DocumentSnapshot documentSnapshot:queryDocumentSnapshots.getDocuments()){
                            // Log.d(TAG, document.getId()document.getId() + " => " + document.getData());
                            subject = documentSnapshot.toObject(Subject.class);
                            subject.setId(documentSnapshot.getId());
                            subjectList.add(subject);
                        }
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        if (subjectList.size() != 0) {
                            if (pDialog != null) {
                                pDialog.dismiss();
                            }
                            btnSave.setVisibility(View.VISIBLE);
                            spSubject.setVisibility(View.VISIBLE);
                            List<String> subNameList = new ArrayList<>();
                            for (Subject sub : subjectList) {
                                subNameList.add(sub.getName());
                            }
                            ArrayAdapter<String> subAdaptor = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, subNameList);
                            subAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spSubject.setAdapter(subAdaptor);
                            selectedSubject=subjectList.get(0);
                            spSubject.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    selectedSubject = subjectList.get(position);
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {

                                }
                            });
                        } else {
                            if (pDialog != null) {
                                pDialog.dismiss();
                            }
                            spSubject.setVisibility(View.GONE);
                            btnSave.setVisibility(View.GONE);
                        }
                    }
                });
        // [END get_all_users]
    }
    private void createBottomSheet() {
        if (bottomSheetDialog == null) {
            View sheet = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_add_home_work, null);
            bottomSheetDialog = new BottomSheetDialog(getContext());//new BottomSheetDialog(this,R.style.BottomSheetDialog)
            bottomSheetDialog.setContentView(sheet);
            etName = sheet.findViewById(R.id.etName);
            etDueDate = sheet.findViewById(R.id.etDueDate);
            ivDueDate = sheet.findViewById(R.id.ivDueDate);
            ivCreatedDate = sheet.findViewById(R.id.ivCreatedDate);
            spClass = sheet.findViewById(R.id.spClass);
            //spDivision = sheet.findViewById(R.id.spDivision);
            spSubject = sheet.findViewById(R.id.spBook);
            etCreatedDate = sheet.findViewById(R.id.etCreatedDate);
            btnSave = sheet.findViewById(R.id.btnSave);
            tvAttachmentFile = sheet.findViewById(R.id.tvAttachmentFile);
            ibChooseFile = sheet.findViewById(R.id.ibChooseFile);
            ibRemoveFile = sheet.findViewById(R.id.ibRemoveFile);

            ibChooseFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showFileChooser();
                }
            });

            ibRemoveFile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    removeFileChooser();
                }
            });

            etDueDate.setInputType(InputType.TYPE_NULL);

            if (!pDialog.isShowing()) {
                pDialog.show();
            }
            selectedBatch = new Batch();
            if (batchList.size() != 0) {
                List<String> batchNameList = new ArrayList<>();
                for (Batch batch : batchList) {
                    batchNameList.add(batch.getName());
                }
                ArrayAdapter<String> batchAdaptor = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, batchNameList);
                batchAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spClass.setAdapter(batchAdaptor);
                selectedBatch=batchList.get(0);
                spClass.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        selectedBatch = batchList.get(position);
                        subSpinnerPos = position;
                        //getSectionOfClass();
                        getSubjects();
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            } else {
                //btnSave.setVisibility(View.GONE);
            }

            final Calendar cal = new GregorianCalendar();
            cal.setTime(new Date());
            etCreatedDate.setText("" + String.format("%02d", cal.get(Calendar.DAY_OF_MONTH)) + "/" + String.format("%02d", (cal.get((Calendar.MONTH)) + 1)) + "/" + cal.get(Calendar.YEAR));
            etCreatedDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    etCreatedDate.clearFocus();
                    final Calendar cldr = Calendar.getInstance();
                    int day = cldr.get(Calendar.DAY_OF_MONTH);
                    int month = cldr.get(Calendar.MONTH);
                    int year = cldr.get(Calendar.YEAR);
                    // date picker dialog
                    picker = new DatePickerDialog(getContext(), R.style.CalendarDatePicker,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                    etCreatedDate.setText(String.format("%02d", dayOfMonth) + "/" + (String.format("%02d", (monthOfYear + 1))) + "/" + year);
                                }
                            }, year, month, day);
                    picker.setTitle("Select Created Date");
                    picker.show();
                }
            });
            ivCreatedDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    etCreatedDate.clearFocus();
                    final Calendar cldr = Calendar.getInstance();
                    int day = cldr.get(Calendar.DAY_OF_MONTH);
                    int month = cldr.get(Calendar.MONTH);
                    int year = cldr.get(Calendar.YEAR);
                    // date picker dialog
                    picker = new DatePickerDialog(getContext(), R.style.CalendarDatePicker,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                    etCreatedDate.setText(String.format("%02d", dayOfMonth) + "/" + (String.format("%02d", (monthOfYear + 1))) + "/" + year);
                                }
                            }, year, month, day);
                    picker.setTitle("Select Created Date");
                    picker.show();
                }
            });
            etDueDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    etDueDate.clearFocus();
                    final Calendar cldr = Calendar.getInstance();
                    int day = cldr.get(Calendar.DAY_OF_MONTH);
                    int month = cldr.get(Calendar.MONTH);
                    int year = cldr.get(Calendar.YEAR);
                    // date picker dialog
                    picker = new DatePickerDialog(getContext(), R.style.CalendarDatePicker,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                    etDueDate.setText(String.format("%02d", dayOfMonth) + "/" + (String.format("%02d", (monthOfYear + 1))) + "/" + year);
                                }
                            }, year, month, day);
                    picker.setTitle("Select Created Date");
                    picker.show();
                }
            });
            ivDueDate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    etDueDate.clearFocus();
                    final Calendar cldr = Calendar.getInstance();
                    int day = cldr.get(Calendar.DAY_OF_MONTH);
                    int month = cldr.get(Calendar.MONTH);
                    int year = cldr.get(Calendar.YEAR);
                    // date picker dialog
                    picker = new DatePickerDialog(getContext(), R.style.CalendarDatePicker,
                            new DatePickerDialog.OnDateSetListener() {
                                @Override
                                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                    etDueDate.setText(String.format("%02d", dayOfMonth) + "/" + (String.format("%02d", (monthOfYear + 1))) + "/" + year);
                                }
                            }, year, month, day);
                    picker.setTitle("Select Due Date");
                    picker.show();
                }
            });
            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String homeWorkName = etName.getText().toString().trim();
                    if (TextUtils.isEmpty(homeWorkName)) {
                        etName.setError("Enter assignment");
                        etName.requestFocus();
                        return;
                    }else{
                        if(TextUtils.isDigitsOnly(homeWorkName)){
                            etName.setError("Enter valid assignment");
                            etName.requestFocus();
                            return;
                        }else {
                            etName.setError(null);
                            etName.clearFocus();
                        }
                    }
                    Date created_date = null;
                    String createdDate = etCreatedDate.getText().toString().trim();
                    if (TextUtils.isEmpty(createdDate)) {
                        etCreatedDate.setError("Enter created date");
                        etCreatedDate.requestFocus();
                        return;
                    }else{
                        if (Utility.isDateValid(createdDate)) {
                            try {
                                etCreatedDate.setError(null);
                                etCreatedDate.clearFocus();
                                created_date = dateFormat.parse(createdDate);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        } else {
                            etCreatedDate.setError("InCorrect Date");
                            etCreatedDate.requestFocus();
                            return;
                        }
                    }
                    Date due_date = null;
                    String dueDate = etDueDate.getText().toString().trim();
                    if (TextUtils.isEmpty(dueDate)) {
                        etDueDate.setError("Enter due date");
                        etDueDate.requestFocus();
                        return;
                    }else{
                        if (Utility.isDateValid(dueDate)) {
                            try {
                                etDueDate.setError(null);
                                etDueDate.clearFocus();
                                due_date = dateFormat.parse(dueDate);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }
                        } else {
                            etDueDate.setError("InCorrect Date");
                            etDueDate.requestFocus();
                            return;
                        }
                    }
                    Date toDate=new Date();
                    if (created_date.getTime() < toDate.getTime()){
                        etCreatedDate.setError("Created date is already over");
                        etCreatedDate.requestFocus();
                        return;
                    }else{
                        etCreatedDate.setError(null);
                        etCreatedDate.clearFocus();
                    }
                    if (created_date.getTime() > due_date.getTime()) {
                        etDueDate.setError("Created date must be less then Due date");
                        etDueDate.requestFocus();
                        return;
                    }else{
                        etDueDate.setError(null);
                        etDueDate.clearFocus();
                    }
                    if(academicYearId != null && loggedInUserId != null) {
                        homeWork = new HomeWork();
                        homeWork.setAcademicYearId(academicYearId);
                        homeWork.setAttachmentUrl("");
                        homeWork.setBatchId(selectedBatch.getId());
                        homeWork.setDueDate(due_date);
                        homeWork.setName(homeWorkName);
                        homeWork.setSectionId("");
                        homeWork.setStatus("A");
                        homeWork.setSubjectName(selectedSubject.getName());
                        homeWork.setCreatedDate(created_date);
                        homeWork.setCreatorId(loggedInUserId);
                        homeWork.setCreatorType("F");
                        homeWork.setSubjectId(selectedSubject.getId());
                        homeWork.setModifiedDate(new Date());
                        homeWork.setModifierId(loggedInUserId);
                        homeWork.setModifierType("F");
                        if (attachUri != null) {
                            uploadFile();
                        } else {
                            addHomeWork(homeWork);
                        }
                        //initialization field
                        etName.setText("");
                        etCreatedDate.setText("");
                        etDueDate.setText("");
                    }
                    bottomSheetDialog.dismiss();
                }
            });
        }
    }
    private void showFileChooser(){
        Intent intent=new Intent();
        intent.setType("*/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Choose a file"),PICKFILE_RESULT_CODE);
    }
    private void removeFileChooser(){
        attachUri=null;
        tvAttachmentFile.setText("");
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICKFILE_RESULT_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            System.out.println("data.getData() "+data.getData());
            attachUri = data.getData();

            try {
                //getting image from gallery
                //Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContext().getContentResolver(), imageUri);

                //Setting image to ImageView
                //ivProfilePic.setImageBitmap(bitmap);
                if(mUploadTask !=null && mUploadTask.isInProgress()){
                    Toast.makeText(getContext(),"Upload in process...",Toast.LENGTH_SHORT).show();
                }else {
                    tvAttachmentFile.setText(DocumentFile.fromSingleUri(getContext(),attachUri).getName());
                    //uploadFile();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    private void  uploadFile(){
        if(attachUri!=null) {
            final ProgressDialog progressDialog=new ProgressDialog(getContext());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            final StorageReference fileRef = storageReference.child("HomeworkAttachement"+System.currentTimeMillis()+"."+getFileExtension(attachUri));

            mUploadTask=fileRef.putFile(attachUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();

                            Handler handler=new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.setProgress(0);
                                }
                            },5000);
                            Toast.makeText(getContext(),"Uploaded..",Toast.LENGTH_LONG).show();
                            pDialog.show();
                            //imageUrl=taskSnapshot.getStorage().getDownloadUrl().toString();
                            // System.out.println("Image Url of profile Stored "+taskSnapshot.getStorage().getDownloadUrl().getResult().toString());
                            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // getting image uri and converting into string
                                    Uri downloadUrl = uri;
                                    attachmentUrl = downloadUrl.toString();
                                    if (attachmentUrl != null) {
                                        tvAttachmentFile.setText(DocumentFile.fromSingleUri(getContext(),attachUri).getName());
                                        homeWork.setAttachmentUrl(attachmentUrl);
                                        addHomeWork(homeWork);
                                    }
                                }
                            });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(),exception.getMessage(),Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                            double progress=(100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage(((int)progress)+"% Uploaded...");
                        }
                    });
        }else {
            Toast.makeText(getContext(),"No file selected",Toast.LENGTH_SHORT).show();
        }

    }
    private String getFileExtension(Uri uri){
        ContentResolver contentResolver=getContext().getContentResolver();
        MimeTypeMap mime=MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(contentResolver.getType(uri));
    }
    /*
    private void getSectionOfClass() {
        if (filterSectionList.size() != 0) {
            filterSectionList.clear();
        }
        if (!pDialog.isShowing()) {
            pDialog.show();
        }
        for (Section section1 : sectionList) {
            if (section1.getBatchId().equalsIgnoreCase(selectedBatch.getId())) {
                filterSectionList.add(section1);
            }
        }
        spDivision.setVisibility(View.VISIBLE);

        List<String> sectionNameList = new ArrayList<>();
        for (Section section : filterSectionList) {
            sectionNameList.add(section.getName());
        }
        ArrayAdapter<String> sectionAdaptor = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, sectionNameList);
        sectionAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spDivision.setAdapter(sectionAdaptor);

        spDivision.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedSection = filterSectionList.get(position);
                getSubjects();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    */
    private void addHomeWork(HomeWork homeWork) {
        if(!pDialog.isShowing()) {
            pDialog.show();
        }
        homeWorkCollectionRef
                .add(homeWork)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Success")
                                .setContentText("HomeWork successfully added")
                                .setConfirmText("Ok")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();
                                        spBatch.setSelection(subSpinnerPos);
                                    }
                                });
                        dialog.setCancelable(false);
                        dialog.show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        //Log.w(TAG, "Error adding document", e);
                        if (pDialog != null) {
                            pDialog.dismiss();
                        }
                        Toast.makeText(getContext(), "Error", Toast.LENGTH_LONG).show();
                    }
                });
        // [END add_document]
        if (pDialog != null) {
            pDialog.dismiss();
        }
    }
    @Override
    public void onStop() {
        super.onStop();
        if (homeWorkListener != null) {
            homeWorkListener.remove();
        }
        if(subjectListener != null){
            subjectListener.remove();
        }
    }
}
