package com.develop.android.attendance;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class AdminActivity extends AppCompatActivity {
    final Context context = this;
    Button addCourse;
    Spinner year;
    private TextView mTextMessage;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mCoursesDatabaseReference, mRollDatabaseReference, mUsersDatabaseReference, mStatusDatabaseReference;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent intent = new Intent(AdminActivity.this, MainActivity.class);
                    AdminActivity.this.startActivity(intent);
                    return true;
                case R.id.navigation_dashboard:
                    Intent dintent = new Intent(AdminActivity.this, Main2Activity.class);
                    AdminActivity.this.startActivity(dintent);
                    return true;
            }
            return false;
        }
    };

    static String encodeUserEmail(String userEmail) {
        return userEmail.replace(".", ",");
    }

    static String decodeUserEmail(String userEmail) {
        return userEmail.replace(",", ".");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        addCourse = (Button) findViewById(R.id.button);

        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mCoursesDatabaseReference = mFirebaseDatabase.getReference().child("Courses");
        mUsersDatabaseReference = mFirebaseDatabase.getReference().child("Users");
        mStatusDatabaseReference = mFirebaseDatabase.getReference().child("Status");
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        addCourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater li = LayoutInflater.from(context);
                View promptsView = li.inflate(R.layout.prompt, null);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setView(promptsView);
                final EditText coursename = (EditText) promptsView.findViewById(R.id.editText);
                final EditText facultyname = (EditText) promptsView.findViewById(R.id.editText2);
                final EditText year = (EditText) promptsView.findViewById(R.id.editText3);
                final EditText emails = (EditText) promptsView.findViewById(R.id.editText4);

                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        final String[] emailsstrings = emails.getText().toString().trim().split("\\s*,\\s*");
                                        int n = emailsstrings.length;
                                        final ArrayList<String> emailslist = new ArrayList<String>(n);
                                        for (int i = 0; i < n; i++) {
                                            emailslist.add(emailsstrings[i]);
                                            UserDetails userDetails = new UserDetails(year.getText().toString(), coursename.getText().toString());
                                            mUsersDatabaseReference.child(encodeUserEmail(emailsstrings[i])).setValue(userDetails);
                                        }
                                        Courses course = new Courses(coursename.getText().toString(), facultyname.getText().toString(), emailslist, year.getText().toString());
                                        mCoursesDatabaseReference.child(year.getText().toString()).child(coursename.getText().toString()).setValue(course);
                                        mCoursesDatabaseReference.child("All").child(coursename.getText().toString()).setValue(course);
                                        mStatusDatabaseReference.child(year.getText().toString()).child(coursename.getText().toString()).setValue("False");
                                        Toast.makeText(AdminActivity.this, "Course Added", Toast.LENGTH_SHORT).show();
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {


                                        dialog.cancel();
                                    }
                                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });
        final Spinner spinner = (Spinner) findViewById(R.id.spinner);

        // Spinner click listener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            String item;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                item = parent.getItemAtPosition(position).toString();
                if (position > 0) {
                    Intent intent = new Intent(AdminActivity.this, ShowCourses.class);
                    intent.putExtra("CourseYear", item);
                    AdminActivity.this.startActivity(intent);
                    // Showing selected spinner item
                    Toast.makeText(parent.getContext(), "Selected Year: " + item, Toast.LENGTH_LONG).show();
                    spinner.setSelection(0);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        // Spinner Drop down elements
        List<String> categories = new ArrayList<String>();
        categories.add("Select");
        categories.add("1");
        categories.add("2");
        categories.add("3");
        categories.add("4");
        categories.add("All");

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        spinner.setAdapter(dataAdapter);
        spinner.setSelection(0);
    }
}
