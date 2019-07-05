package com.develop.android.attendance;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddAttActivity extends AppCompatActivity {
    Button submit, clearall, checkall;
    ArrayList<CheckBox> selectedcheckBox = new ArrayList<CheckBox>();
    private List<RollNumbers> totalRoll = new ArrayList<>();
    private List<RollNumbers> checkedRoll = new ArrayList<>();
    private List<RollNumbers> absentRoll = new ArrayList<>();
    private List<RollNumbers> previousRoll = new ArrayList<>();
    private FirebaseDatabase mFirebaseDatabase;
    private ChildEventListener mChildEventListener;
    private DatabaseReference mStatusDatabaseReference, mAttendanceDatabaseReference,mAbsentDatabaseRefernece, mRollDatabaseReference, mCoursesDatabaseReference, mTimeDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_att);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mCoursesDatabaseReference = mFirebaseDatabase.getReference().child("Courses");
        mRollDatabaseReference = mFirebaseDatabase.getReference().child("Roll");
        mAttendanceDatabaseReference = mFirebaseDatabase.getReference().child("Attendance");
        mAbsentDatabaseRefernece=mFirebaseDatabase.getReference().child("AbsentAttendance");
        mTimeDatabaseReference = mFirebaseDatabase.getReference().child("Time");
        mStatusDatabaseReference = mFirebaseDatabase.getReference().child("Status");
        final LinearLayout ll = (LinearLayout) findViewById(R.id.my_layout);
        submit = (Button) findViewById(R.id.submit_button);
        clearall = (Button) findViewById(R.id.clearall);
        checkall = (Button) findViewById(R.id.checkall);
        final Intent intent = getIntent();
        final String value = intent.getStringExtra("CourseName");
        final String yearval = intent.getStringExtra("Year");
        final String status = intent.getStringExtra("status");
        TextView course = (TextView) findViewById(R.id.namecourse);
        course.setText(value+"_Year_"+yearval);
        submit.setText("Submit");
        checkedRoll.clear();
        Query query1 = mAttendanceDatabaseReference.child(yearval).child(value);
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    GenericTypeIndicator<List<RollNumbers>> t = new GenericTypeIndicator<List<RollNumbers>>() {
                    };
                    previousRoll = dataSnapshot.getValue(t);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Query query = mRollDatabaseReference.child(yearval).child(value);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GenericTypeIndicator<List<RollNumbers>> t = new GenericTypeIndicator<List<RollNumbers>>() {
                };
                totalRoll = dataSnapshot.getValue(t);
                getData(ll);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        clearall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cc;
                for (int i = 0; i < ll.getChildCount(); i++) {
                    cc = (CheckBox) ll.getChildAt(i);
                    cc.setChecked(false);
                }
                checkedRoll.clear();
            }
        });
        checkall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CheckBox cc;
                for (int i = 0; i < ll.getChildCount(); i++) {
                    cc = (CheckBox) ll.getChildAt(i);
                    cc.setChecked(true);
                }
                absentRoll.clear();
            }
        });
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (status.equals("True") && status != null) {
                    mAttendanceDatabaseReference.child(yearval).child(value).setValue(checkedRoll);
                    absentRoll=totalRoll;
                    absentRoll.removeAll(checkedRoll);
                    mAbsentDatabaseRefernece.child(yearval).child(value).setValue(absentRoll);
                    Log.d("chk", checkedRoll + "");
                    DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm");
                    String date = df.format(Calendar.getInstance().getTime());
                    mTimeDatabaseReference.child(yearval).child(value).setValue(date);
                    Toast.makeText(AddAttActivity.this, "Successfully Added Attendance", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(AddAttActivity.this, "Permission Not Granted", Toast.LENGTH_LONG).show();
                }
                Intent intent = new Intent(AddAttActivity.this, AdminActivity.class);
                AddAttActivity.this.startActivity(intent);
            }
        });
    }

    public void getData(LinearLayout ll) {
        for (int i = 0; i < totalRoll.size(); i++) {
            final CheckBox cb = new CheckBox(this);
            cb.setText(totalRoll.get(i).getRollnum());
            cb.setId(i);
            ll.addView(cb);
            for (int j = 0; j < previousRoll.size(); j++) {
                if (previousRoll.get(j).rollnum.equals(totalRoll.get(i).rollnum)) {
                    cb.setChecked(true);
                    selectedcheckBox.add(cb);
                    RollNumbers x = new RollNumbers(totalRoll.get(i).rollnum);
                    checkedRoll.add(x);
                    break;
                }
            }
            cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    String checkedText = buttonView.getText() + "";
                    RollNumbers x = new RollNumbers(checkedText);
                    if (isChecked) {
                        checkedRoll.add(x);
                        selectedcheckBox.add(cb);
                    } else {
                        checkedRoll.remove(x);
                        selectedcheckBox.remove(cb);
                        for (int te = 0; te < checkedRoll.size(); te++) {
                            if (x.getRollnum().equals(checkedRoll.get(te).getRollnum()))
                            {
                                checkedRoll.remove(te);
                            }
                        }
                    }
                }
            });
        }
    }
}
