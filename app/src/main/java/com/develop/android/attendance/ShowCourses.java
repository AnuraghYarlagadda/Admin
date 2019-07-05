package com.develop.android.attendance;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.inputmethodservice.Keyboard;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.lang.Thread.sleep;

public class ShowCourses extends AppCompatActivity {

    private TextView mTextMessage;
    Button showfullatt;
    private CourseAdapter mCourseAdapter;
    private ListView mCourseListView;
    private ChildEventListener mChildEventListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mCoursesDatabaseReference,mRollDatabaseReference,mStatusDatabaseReference;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent intent=new Intent(ShowCourses.this,AdminActivity.class);
                    ShowCourses.this.startActivity(intent);
                    return true;
                case R.id.navigation_dashboard:
                    Intent dintent=new Intent(ShowCourses.this,Main2Activity.class);
                    ShowCourses.this.startActivity(dintent);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_courses);
        showfullatt=(Button)findViewById(R.id.showbutton);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mCoursesDatabaseReference = mFirebaseDatabase.getReference().child("Courses");
        mRollDatabaseReference=mFirebaseDatabase.getReference().child("Roll");
        mStatusDatabaseReference=mFirebaseDatabase.getReference().child("Status");
        Intent intent = getIntent();
        final String courseYear = intent.getStringExtra("CourseYear");
        mTextMessage = (TextView) findViewById(R.id.message);
        mCourseListView = (ListView) findViewById(R.id.list);
        final List<Courses> courses = new ArrayList<>();
        mCourseAdapter = new CourseAdapter(this, R.layout.adminlist_item, courses);
        mCourseListView.setAdapter(mCourseAdapter);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        if(courseYear.equals("All"))
        {
            showfullatt.setVisibility(View.GONE);
        }
        else
        {
            showfullatt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LayoutInflater li = LayoutInflater.from(ShowCourses.this);
                    View promptsView = li.inflate(R.layout.buttonprompt, null);
                    android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(ShowCourses.this);
                    alertDialogBuilder.setView(promptsView);
                    final Button present=(Button)promptsView.findViewById(R.id.present);
                    final Button absent=(Button)promptsView.findViewById(R.id.absent);
                    android.app.AlertDialog alertDialog = alertDialogBuilder.create();
                    alertDialog.show();
                    present.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(ShowCourses.this, ShowFullActivity.class);
                            intent.putExtra("Year", courseYear);
                            intent.putExtra("what","present");
                            ShowCourses.this.startActivity(intent);
                        }
                    });
                    absent.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(ShowCourses.this, ShowFullActivity.class);
                            intent.putExtra("Year", courseYear);
                            intent.putExtra("what","absent");
                            ShowCourses.this.startActivity(intent);
                        }
                    });
                }
            });
        }
        mChildEventListener=new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                Courses course = dataSnapshot.getValue(Courses.class);
                mCourseAdapter.add(course);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
                Courses course = dataSnapshot.getValue(Courses.class);
                mCourseAdapter.remove(course);
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
        mCoursesDatabaseReference.child(courseYear).addChildEventListener(mChildEventListener);
    }
    public void deleteCourse(final Courses thisCourse){
        String x=thisCourse.getCourseName();
        String y=thisCourse.getYear();
        Query query=FirebaseDatabase.getInstance().getReference().child("Courses").child(y).orderByChild("courseName").equalTo(x);
        FirebaseDatabase.getInstance().getReference().child("Roll").child(y).child(x).setValue(null);
        FirebaseDatabase.getInstance().getReference().child("Attendance").child(y).child(x).setValue(null);
        FirebaseDatabase.getInstance().getReference().child("Courses").child(y).child(x).setValue(null);
        FirebaseDatabase.getInstance().getReference().child("Courses").child("All").child(x).setValue(null);
        FirebaseDatabase.getInstance().getReference().child("Status").child(y).child(x).setValue(null);
        FirebaseDatabase.getInstance().getReference().child("Time").child(y).child(x).setValue(null);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                dataSnapshot.getRef().setValue(null);
                mCourseAdapter.remove(thisCourse);
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Toast.makeText(this, "course"+x+"Deleted", Toast.LENGTH_SHORT).show();

    }

    public int presentstatus(final Courses thisCourse){
        Query query=mStatusDatabaseReference.child(thisCourse.year).child(thisCourse.courseName);
        final int[] flag = {0};
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.d("staiteys",dataSnapshot.getValue()+"");
                String stat= (String) dataSnapshot.getValue();
                if(stat.equals("False")){
                    flag[0] =1;
                }
                else{
                    flag[0]=0;
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return flag[0];
    }
public  void gocourse(final Courses thisCourse,String status){
        Intent intent=new Intent(ShowCourses.this,CourseDetails.class);
        intent.putExtra("CourseName",thisCourse.courseName);
        intent.putExtra("Year",thisCourse.year);
        intent.putExtra("faculty",thisCourse.courseFaculty);
        intent.putExtra("emails",thisCourse.emails);
        intent.putExtra("mobile",thisCourse.mobileNumber);
        intent.putExtra("venue",thisCourse.venue);
        intent.putExtra("status",status);
        ShowCourses.this.startActivity(intent);
    }
}
