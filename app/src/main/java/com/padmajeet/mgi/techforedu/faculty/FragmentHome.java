package com.padmajeet.mgi.techforedu.faculty;


import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.gson.Gson;
import com.padmajeet.mgi.techforedu.faculty.model.AcademicYear;
import com.padmajeet.mgi.techforedu.faculty.model.Staff;
import com.padmajeet.mgi.techforedu.faculty.util.SessionManager;
import com.padmajeet.mgi.techforedu.faculty.util.Utility;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


/**
 * A simple {@link Fragment} subclass.
 */
public class FragmentHome extends Fragment {
    private TextView tvStaffName,tvStudentName,tvClass;
    private ImageView ivProfilePic;
    private FirebaseFirestore db= FirebaseFirestore.getInstance();
    private CollectionReference staffCollectionRef=db.collection("Staff");
    private Gson gson;
    private Staff loggedInUser;
    private String LoggedInUserId;
    private AcademicYear academicYear;
    private String academicYearId;
    private String className="";
    private GridLayout gridLayout;
    private PublisherAdView mPublisherAdView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SessionManager sessionManager = new SessionManager(getContext());
        gson = Utility.getGson();
        String staffJson = sessionManager.getString("loggedInUser");
        loggedInUser = gson.fromJson(staffJson, Staff.class);
        String selectedAcademicYearJson = sessionManager.getString("AcademicYear");
        academicYearId = sessionManager.getString("AcademicYearId");
    }

    public FragmentHome() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        return inflater.inflate(R.layout.fragment_home, container, false);


    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((ActivityHome)getActivity()).getSupportActionBar().setTitle(getString(R.string.home));
        mPublisherAdView = view.findViewById(R.id.publisherAdView);
        PublisherAdRequest adRequest = new PublisherAdRequest.Builder().build();
        mPublisherAdView.loadAd(adRequest);
        ivProfilePic = view.findViewById(R.id.ivProfilePic);
        tvStaffName =view.findViewById(R.id.tvStaffName);
        tvStudentName =view.findViewById(R.id.tvName);
        tvClass = view.findViewById(R.id.tvClass);

        gridLayout = view.findViewById(R.id.mainGrid);

        setSingleEvent(gridLayout);

        if(loggedInUser != null) {

            String name=loggedInUser.getFirstName();
            if(TextUtils.isEmpty(loggedInUser.getLastName())){
                name=name+" "+loggedInUser.getLastName();
            }
            tvStaffName.setText("Welcome "+name);

            String imageUrl = loggedInUser.getImageUrl();
            if (TextUtils.isEmpty(imageUrl)) {
                Glide.with(this)
                        .load(R.drawable.ic_staff)
                        .fitCenter()
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.ic_profile_large)
                        .into(ivProfilePic);
            } else {
                Glide.with(this)
                        .load(imageUrl)
                        .fitCenter()
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.ic_profile_large)
                        .into(ivProfilePic);
            }
            tvStudentName.setText(""+loggedInUser.getFirstName()+" "+loggedInUser.getLastName());
        }



    }

    private void setSingleEvent(GridLayout gridLayout) {
        NavigationView navigationView = (NavigationView) getActivity().findViewById(R.id.nav_view);
        Menu menuNav = navigationView.getMenu();
        for (int i = 0; i < gridLayout.getChildCount(); i++) {
            LinearLayout linearLayout = (LinearLayout) gridLayout.getChildAt(i);
            final int id = i;
            linearLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Toast.makeText(getContext(), "Clicked at index " + id,Toast.LENGTH_SHORT).show();
                    switch (id) {
                        case 0:
                            menuNav.findItem(R.id.nav_message).setChecked(true);
                            replaceFragment(new FragmentMessage(), getString(R.string.message));
                            break;
                        case 1:
                            menuNav.findItem(R.id.nav_event).setChecked(true);
                            replaceFragment(new FragmentEvent(), getString(R.string.event));
                            break;
                        case 2:
                            menuNav.findItem(R.id.nav_take_attendance).setChecked(true);
                            replaceFragment(new FragmentTakeAttendance(), getString(R.string.takeAttendance));
                            break;
                        case 3:
                            menuNav.findItem(R.id.nav_calender).setChecked(true);
                            replaceFragment(new FragmentCalendar(), getString(R.string.academicCalendar));
                            break;
                        case 4:
                            menuNav.findItem(R.id.nav_subject).setChecked(true);
                            replaceFragment(new FragmentSubject(), getString(R.string.subject));
                            break;
                        case 5:
                            menuNav.findItem(R.id.nav_attendance).setChecked(true);
                            replaceFragment(new FragmentAttendance(), getString(R.string.attendance));
                            break;
                        case 6:
                            menuNav.findItem(R.id.nav_home_work).setChecked(true);
                            replaceFragment(new FragmentHomeWork(), getString(R.string.assignment));
                            break;
                        case 7:
                            menuNav.findItem(R.id.nav_colleague).setChecked(true);
                            replaceFragment(new FragmentColleague(), getString(R.string.colleagues));
                            break;
                        case 8:
                            menuNav.findItem(R.id.nav_my_schedule).setChecked(true);
                            replaceFragment(new FragmentMySchedule(), getString(R.string.mySchedule));
                            break;
                            /*
                        case 7:
                            menuNav.findItem(R.id.nav_competition_winner).setChecked(true);
                            replaceFragment(new FragmentCompetitionWinner(), getString(R.string.competition));
                            break;
                        case 8:
                            menuNav.findItem(R.id.nav_achievement).setChecked(true);
                            replaceFragment(new FragmentAchievements(), getString(R.string.achievement));
                            break;
                        case 9:
                            menuNav.findItem(R.id.nav_student_fees).setChecked(true);
                            replaceFragment(new FragmentStudentFees(), getString(R.string.fees));
                            break;
                            */
                        case 9:
                            menuNav.findItem(R.id.nav_holiday).setChecked(true);
                            replaceFragment(new FragmentHoliday(), getString(R.string.holiday));
                            break;
                        case 10:
                            menuNav.findItem(R.id.nav_support).setChecked(true);
                            replaceFragment(new FragmentSupport(), getString(R.string.contact));
                            break;
                        case 11:
                            menuNav.findItem(R.id.nav_timetable).setChecked(true);
                            replaceFragment(new FragmentTimeTable(), getString(R.string.timeTable));
                            break;
                        case 12:
                            menuNav.findItem(R.id.nav_exam_schedule).setChecked(true);
                            replaceFragment(new FragmentExamSeries(), getString(R.string.examSchedule));
                            break;
                        case 13:
                            menuNav.findItem(R.id.nav_score_card).setChecked(true);
                            replaceFragment(new FragmentExamSeriesScore(), getString(R.string.scoreCard));
                            break;
                    }
                }
            });
        }
    }

    private void replaceFragment(Fragment fragment, String tag) {
        FragmentManager manager = getActivity().getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = manager.beginTransaction();
        fragmentTransaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        fragmentTransaction.replace(R.id.contentLayout, fragment, tag);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    /** Called when leaving the activity */
    @Override
    public void onPause() {
        if (mPublisherAdView != null) {
            mPublisherAdView.pause();
        }
        super.onPause();
    }

    /** Called when returning to the activity */
    @Override
    public void onResume() {
        super.onResume();
        if (mPublisherAdView != null) {
            mPublisherAdView.resume();
        }
    }

    /** Called before the activity is destroyed */
    @Override
    public void onDestroy() {
        if (mPublisherAdView != null) {
            mPublisherAdView.destroy();
        }
        super.onDestroy();
    }

}
