package com.padmajeet.mgi.techforedu.faculty;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.padmajeet.mgi.techforedu.faculty.model.Batch;
import com.padmajeet.mgi.techforedu.faculty.model.Event;
import com.padmajeet.mgi.techforedu.faculty.util.Utility;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentEventDetails extends Fragment {

    private TextView tvEvent,tvDate,tvDesc,tvCreatorType,tvClass,tvDressCode;
    private Event selectedEvent;
    private FirebaseFirestore db=FirebaseFirestore.getInstance();
    private CollectionReference batchCollectionRef=db.collection("Batch");
    private DocumentReference batchDocRef;

    public FragmentEventDetails() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String selectedEventJson = getArguments().getString("selectedEvent");
        Gson gson = Utility.getGson();
        selectedEvent = gson.fromJson(selectedEventJson,Event.class);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvEvent = view.findViewById(R.id.tvEvent);
        tvDate = view.findViewById(R.id.tvDate);
        tvDesc = view.findViewById(R.id.tvDesc);
        tvDressCode = view.findViewById(R.id.tvDressCode);
        tvCreatorType = view.findViewById(R.id.tvCreatorType);
        tvClass = view.findViewById(R.id.tvClass);
        tvEvent.setText(""+selectedEvent.getName());
        tvDressCode.setText(""+selectedEvent.getDressCode());
        tvDate.setText("" + Utility.formatDateToString(selectedEvent.getCreatedDate().getTime()));

        if(selectedEvent.getCreatorType().equals("A")){
            tvCreatorType.setText("Admin");
        }
        else if(selectedEvent.getCreatorType().equals("F")){
            tvCreatorType.setText("Teacher");
        }
        tvDesc.setText(""+selectedEvent.getDescription());
        batchDocRef = batchCollectionRef.document("/" + selectedEvent.getBatchId());
        batchDocRef.get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Batch batch = documentSnapshot.toObject(Batch.class);
                        tvClass.setText(""+batch.getName());
                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });
    }
}
