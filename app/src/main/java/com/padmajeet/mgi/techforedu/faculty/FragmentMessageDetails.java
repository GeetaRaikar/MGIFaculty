package com.padmajeet.mgi.techforedu.faculty;

import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.gson.Gson;
import com.padmajeet.mgi.techforedu.faculty.model.Message;
import com.padmajeet.mgi.techforedu.faculty.model.Staff;
import com.padmajeet.mgi.techforedu.faculty.model.Student;
import com.padmajeet.mgi.techforedu.faculty.util.SessionManager;
import com.padmajeet.mgi.techforedu.faculty.util.Utility;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.app.Activity.RESULT_OK;
import static android.os.Environment.DIRECTORY_DOWNLOADS;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentMessageDetails extends Fragment {
    private TextView tvSubject, tvDate, tvDesc, tvCreatorType, tvClass, tvStudent, tvAttachmentFile;
    private LinearLayout llHeaderStrip,llAttachmentFile;
    private EditText etSubject,etDescription;
    private Message selectedMessage;
    private Button btnEdit, btnDelete;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference studentCollectionRef = db.collection("Student");
    private CollectionReference messageCollectionRef = db.collection("Message");
    private DocumentReference batchDocRef;
    private SweetAlertDialog pDialog;
    private Staff loggedInUser;
    private String loggedInUserId;
    private ListenerRegistration messageListener;
    private String selectedBatchName;
    private String name;
    private int count;
    private int[] height = {120, 160, 200, 240, 280, 320, 360, 400, 440, 480, 520};
    private String[] fileName2;
    private final int REQ_CODE_SPEECH_INPUT = 100;
    private ImageButton ibMic;
    private StringBuffer comment = new StringBuffer();

    public FragmentMessageDetails() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String selectedMessageJson = getArguments().getString("selectedMessage");
        SessionManager sessionManager = new SessionManager(getContext());
        Gson gson = Utility.getGson();
        selectedMessage = gson.fromJson(selectedMessageJson, Message.class);
        if (selectedMessage.getBatchIdList().size() != 0) {
            selectedBatchName = getArguments().getString("selectedBatchName");
        }
        String userJson = sessionManager.getString("loggedInUser");
        loggedInUser = gson.fromJson(userJson, Staff.class);
        loggedInUserId = sessionManager.getString("loggedInUserId");
        pDialog = Utility.createSweetAlertDialog(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_message_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvSubject = view.findViewById(R.id.tvSubject);
        tvDate = view.findViewById(R.id.tvDate);
        tvDesc = view.findViewById(R.id.tvDesc);
        tvCreatorType = view.findViewById(R.id.tvCreatorType);
        tvClass = view.findViewById(R.id.tvClass);
        tvStudent = view.findViewById(R.id.tvStudent);
        llHeaderStrip = view.findViewById(R.id.llHeaderStrip);
        tvAttachmentFile = view.findViewById(R.id.tvAttachmentFile);
        llAttachmentFile = view.findViewById(R.id.llAttachmentFile);

        if (selectedMessage.getCategory().equalsIgnoreCase("A")) {
            tvClass.setVisibility(View.GONE);
        }
        if (selectedMessage.getCategory().equalsIgnoreCase("C")) {
            tvClass.setText("" + selectedBatchName);
        }
        if (selectedMessage.getCategory().equalsIgnoreCase("S")) {
            if (pDialog != null) {
                pDialog.show();
            }
            name = "";
            System.out.println("selectedMessage.getCategory() " + selectedMessage.getCategory());
            System.out.println("name " + name);
            tvStudent.setVisibility(View.VISIBLE);
            tvClass.setText("" + selectedBatchName);
            count = 0;
            List<String> recipientIdList = new ArrayList<>();
            recipientIdList.addAll(selectedMessage.getRecipientIdList());
            System.out.println("recipientIdList " + recipientIdList.size());
            int index = (int) Math.ceil((float) recipientIdList.size() / 5.0);
            float density = getResources().getDisplayMetrics().density;
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) llHeaderStrip.getLayoutParams();
            params.height = Math.round((float) height[index] * density);
            System.out.println("index " + index);
            llHeaderStrip.setLayoutParams(params);
            for (int i = 0; i < recipientIdList.size(); i++) {
                studentCollectionRef.document("/" + recipientIdList.get(i))
                        .get()
                        .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Student student = documentSnapshot.toObject(Student.class);
                                if (TextUtils.isEmpty(name)) {
                                    name = name + student.getFirstName() + " " + student.getLastName();
                                    System.out.println("name " + name);
                                    count++;
                                    if (count == recipientIdList.size()) {
                                        if (pDialog != null) {
                                            pDialog.dismiss();
                                        }
                                        tvStudent.setText("" + name);
                                    }
                                } else {
                                    name = name + " , " + student.getFirstName() + " " + student.getLastName();
                                    count++;
                                    if (count == recipientIdList.size()) {
                                        if (pDialog != null) {
                                            pDialog.dismiss();
                                        }
                                        System.out.println("name " + name);
                                        tvStudent.setText("" + name);
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
        btnEdit = view.findViewById(R.id.btnEdit);
        btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditWindow();
            }
        });

        btnDelete = view.findViewById(R.id.btnDelete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.WARNING_TYPE)
                        .setTitleText("Are you sure?")
                        .setContentText("You won't be able to recover this message!")
                        .setConfirmText("Delete!")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                                deleteMessage();
                            }
                        })
                        .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sDialog) {
                                sDialog.dismissWithAnimation();
                            }
                        });
                dialog.setCancelable(false);
                dialog.show();
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

    @Override
    public void onStart() {
        super.onStart();
        messageListener = messageCollectionRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                tvSubject.setText("" + selectedMessage.getSubject());
                tvDate.setText("" + Utility.formatDateToString(selectedMessage.getCreatedDate().getTime()));

                if (selectedMessage.getCreatorType().equals("A")) {
                    tvCreatorType.setText("Admin");
                } else if (selectedMessage.getCreatorType().equals("F")) {
                    tvCreatorType.setText("Teacher");
                }
                tvDesc.setText("" + selectedMessage.getDescription());
                String url = selectedMessage.getAttachmentUrl();
                if(TextUtils.isEmpty(url)){
                    llAttachmentFile.setVisibility(View.GONE);
                }else{
                    llAttachmentFile.setVisibility(View.VISIBLE);

                    String fileName = url.substring(url.lastIndexOf('/') + 1);
                    String fileName1 = fileName.substring(0, fileName.lastIndexOf('?'));
                    fileName2 = fileName1.split("%2F");
                    System.out.println("fileName2 "+fileName2[1]);
                    tvAttachmentFile.setText(""+fileName2[1]);
                }

                tvAttachmentFile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DownloadManager downloadManager=(DownloadManager)getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                        Uri uri=Uri.parse(url);
                        DownloadManager.Request request=new DownloadManager.Request(uri);
                        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                        request.setDestinationInExternalFilesDir(getContext(),DIRECTORY_DOWNLOADS,fileName2[1]);
                        downloadManager.enqueue(request);
                        Toast.makeText(getContext(),"Download started",Toast.LENGTH_SHORT).show();
                /*Show toast after 10 sec.
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getContext(),"Download Complete",Toast.LENGTH_SHORT).show();
                    }
                },10000);

                 */
                    }
                });
            }
        });
    }

    BroadcastReceiver broadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            openFile();
        }
    };

    //TODO
    private void openFile(){
        Toast.makeText(getContext(),"Download Complete",Toast.LENGTH_SHORT).show();
       /* Intent install = new Intent(Intent.ACTION_VIEW);
        String filePath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        +"/"+fileName2[1];
        System.out.println("File Path - "+filePath);
        File file = new File(filePath);
        MimeTypeMap map = MimeTypeMap.getSingleton();
        String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName());
        String type = map.getMimeTypeFromExtension(ext);
        System.out.println("Type - "+type);

        */
        //if (type == null) {
        //type = "*/*";
        //}
        /*
        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.fromFile(file);
        System.out.println("data - "+data);
        intent.setDataAndType(data, type);
        startActivity(intent);
        */
    }

    @Override
    public void onResume() {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter(
                DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        getActivity().registerReceiver(broadcast, intentFilter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(broadcast);
    }

    private void showEditWindow() {
        LayoutInflater inflater = getLayoutInflater();
        final View dialogLayout = inflater.inflate(R.layout.dialog_edit_message, null);
        etSubject = dialogLayout.findViewById(R.id.etSubject);
        etSubject.setText("" + selectedMessage.getSubject());
        etDescription = dialogLayout.findViewById(R.id.etDescription);
        etDescription.setText("" + selectedMessage.getDescription());
        ibMic = dialogLayout.findViewById(R.id.ibMic);
        ibMic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptSpeechInput();
            }
        });
        SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("Edit Message")
                .setConfirmText("Update")
                .setCustomView(dialogLayout)

                .setCancelButton("Cancel", new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {
                        sDialog.dismissWithAnimation();
                    }
                })
                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                    @Override
                    public void onClick(SweetAlertDialog sDialog) {

                        String subject = etSubject.getText().toString().trim();
                        if (TextUtils.isEmpty(subject)) {
                            etSubject.setError("Enter subject");
                            etSubject.requestFocus();
                            return;
                        }
                        String desc = etDescription.getText().toString().trim();
                        if (TextUtils.isEmpty(desc)) {
                            etDescription.setError("Enter the description");
                            etDescription.requestFocus();
                            return;
                        }
                        sDialog.dismissWithAnimation();
                        if (pDialog != null) {
                            pDialog.show();
                        }
                        selectedMessage.setSubject(subject);
                        selectedMessage.setDescription(desc);
                        selectedMessage.setModifierType("A");
                        selectedMessage.setModifierId(loggedInUserId);
                        selectedMessage.setModifiedDate(new Date());
                        messageCollectionRef.document(selectedMessage.getId()).set(selectedMessage).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    if (pDialog != null) {
                                        pDialog.dismiss();
                                    }
                                    SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                            .setTitleText("Success")
                                            .setContentText("Message successfully updated")
                                            .setConfirmText("Ok")
                                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                @Override
                                                public void onClick(SweetAlertDialog sDialog) {
                                                    sDialog.dismissWithAnimation();
                                                }
                                            });
                                    dialog.setCancelable(false);
                                    dialog.show();

                                } else {
                                    Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    }
                });
        dialog.getWindow().setGravity(Gravity.CENTER);
        dialog.setCancelable(false);
        dialog.show();
    }

    private void deleteMessage() {
        messageCollectionRef.document(selectedMessage.getId())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        SweetAlertDialog dialog = new SweetAlertDialog(getContext(), SweetAlertDialog.SUCCESS_TYPE)
                                .setTitleText("Success")
                                .setContentText("Message successfully deleted")
                                .setConfirmText("Ok")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sDialog) {
                                        sDialog.dismissWithAnimation();
                                        //TODO
                                        FragmentManager manager = getActivity().getSupportFragmentManager();
                                        FragmentTransaction fragmentTransaction = manager.beginTransaction();
                                        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                                        fragmentTransaction.replace(R.id.contentLayout, new FragmentMessage());
                                        fragmentTransaction.addToBackStack(null);
                                        fragmentTransaction.commit();
                                    }
                                });
                        dialog.setCancelable(false);
                        dialog.show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }


}
