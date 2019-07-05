package com.develop.android.attendance;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.formula.functions.T;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class CourseDetails extends AppCompatActivity {
    final Context context = this;
    String presentCourse, presentyear;
    int rowval,colval,sheetnumber,snamecolval;
    AlertDialog.Builder builder;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mCoursesDatabaseReference, mRollDatabaseReference,mStatusDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course_details);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mCoursesDatabaseReference = mFirebaseDatabase.getReference().child("Courses");
        mRollDatabaseReference = mFirebaseDatabase.getReference().child("Roll");
        mStatusDatabaseReference = mFirebaseDatabase.getReference().child("Status");
        Toolbar toolbar = findViewById(R.id.toolbar);
        TextView course = (TextView) findViewById(R.id.course);
        TextView tutor = (TextView) findViewById(R.id.faculty);
        TextView emails = (TextView) findViewById(R.id.emails);
        TextView mobile=(TextView)findViewById(R.id.mobile);
        TextView disyear=(TextView)findViewById(R.id.year);
        TextView venue=(TextView)findViewById(R.id.venue);
        emails.setSelected(true);
        setSupportActionBar(toolbar);
        Button delcourse = (Button) findViewById(R.id.delete);
        Button addexcel = (Button) findViewById(R.id.addexcel);
        Button addatt = (Button) findViewById(R.id.add);
        Button showatt = (Button) findViewById(R.id.show);
        ImageButton edit=(ImageButton) findViewById(R.id.edit);
        Intent intent = getIntent();
        final String value = intent.getStringExtra("CourseName");
        course.setText(value);
        final String faculty = intent.getStringExtra("faculty");
        tutor.setText(faculty);
        final String status = intent.getStringExtra("status");
        final String mobilenumber=intent.getStringExtra("mobile");
        final String venueplace=intent.getStringExtra("venue");
        final String Year=intent.getStringExtra("Year");
        disyear.setText("Year :"+Year);
        mobile.setText(mobilenumber);
        venue.setText(venueplace);
        final ArrayList<String> email = intent.getStringArrayListExtra("emails");
        emails.setText(email.toString().replaceAll("\\[|\\]", ""));

        final String yearval = intent.getStringExtra("Year");
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater li = LayoutInflater.from(context);
                View promptsView = li.inflate(R.layout.editprompt, null);
                android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(context);
                alertDialogBuilder.setView(promptsView);
                final TextView coursename = (TextView) promptsView.findViewById(R.id.editText);
                final EditText facultyname = (EditText) promptsView.findViewById(R.id.editText2);
                final EditText mobileNumber = (EditText) promptsView.findViewById(R.id.editText3);
                final EditText venue = (EditText) promptsView.findViewById(R.id.editText4);
                final TextView year = (TextView) promptsView.findViewById(R.id.editText5);
                final EditText emails = (EditText) promptsView.findViewById(R.id.editText6);
                String em="";
                for(int i=0;i<email.size();i++)
                {
                    em+=email.get(i)+",";
                }
                coursename.setText(value);
                if(faculty==null)
                {
                    facultyname.setText("No record");
                }
                else
                {
                    facultyname.setText(faculty);
                }
                if(mobilenumber==null)
                {
                    mobileNumber.setText("No record");
                }
                else
                {
                    mobileNumber.setText(mobilenumber);
                }
                if(venueplace==null)
                {
                    venue.setText("No record");
                }
                else
                {
                    venue.setText(venueplace);
                }
                year.setText(Year);
                emails.setText(em);
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
                                        }
                                        Courses course = new Courses(coursename.getText().toString(), facultyname.getText().toString(), emailslist, year.getText().toString(),mobileNumber.getText().toString(),venue.getText().toString());
                                        mCoursesDatabaseReference.child(year.getText().toString()).child(coursename.getText().toString()).setValue(course);
                                        mCoursesDatabaseReference.child("All").child(coursename.getText().toString()).setValue(course);
                                        Toast.makeText(CourseDetails.this, "Course Updated", Toast.LENGTH_SHORT).show();
                                        Intent i=new Intent(CourseDetails.this,AdminActivity.class);
                                        startActivity(i);
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {


                                        dialog.cancel();
                                    }
                                });
                android.app.AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });
        delcourse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                builder = new AlertDialog.Builder(CourseDetails.this);
                builder.setMessage("Do you want to delete this Course?")
                        .setCancelable(false)
                        .setPositiveButton("Yes, Delete", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                confirmdelete(value, yearval);
                                finish();
                            }
                        })
                        .setNegativeButton("No, Keep it", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //  Action for 'NO' Button
                                dialog.cancel();
                            }
                        });
                //Creating dialog box
                AlertDialog alert = builder.create();
                //Setting the title manually
                alert.setTitle("Course Delete Alert");
                alert.show();
            }
        });
        showatt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LayoutInflater li = LayoutInflater.from(CourseDetails.this);
                View promptsView = li.inflate(R.layout.buttonprompt, null);
                android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(CourseDetails.this);
                alertDialogBuilder.setView(promptsView);
                final Button present=(Button)promptsView.findViewById(R.id.present);
                final Button absent=(Button)promptsView.findViewById(R.id.absent);
                android.app.AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                present.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(CourseDetails.this, ShowAttActivity.class);
                        intent.putExtra("CourseName", value);
                        intent.putExtra("Year", yearval);
                        intent.putExtra("what","present");
                        CourseDetails.this.startActivity(intent);
                    }
                });
                absent.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(CourseDetails.this, ShowAttActivity.class);
                        intent.putExtra("CourseName", value);
                        intent.putExtra("Year", yearval);
                        intent.putExtra("what","absent");
                        CourseDetails.this.startActivity(intent);
                    }
                });
            }
        });
        addatt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(status.equals("True")&&status!=null) {
                    final String courseName = value;
                    final String Year = yearval;
                    Query query = FirebaseDatabase.getInstance().getReference().child("Roll").child(Year).child(courseName);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.exists()) {
                                Intent intent = new Intent(CourseDetails.this, AddAttActivity.class);
                                intent.putExtra("CourseName", courseName);
                                intent.putExtra("Year", Year);
                                intent.putExtra("status", status);
                                CourseDetails.this.startActivity(intent);
                            } else {
                                Toast.makeText(CourseDetails.this, "Please Add Excel", Toast.LENGTH_LONG).show();
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else
                {
                    Toast.makeText(CourseDetails.this, "Permission Not Granted", Toast.LENGTH_LONG).show();
                }
            }
        });
        addexcel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.putExtra("name", value);
                presentCourse = value;
                presentyear = yearval;
                LayoutInflater li = LayoutInflater.from(CourseDetails.this);
                View promptsView = li.inflate(R.layout.promptexcel, null);
                android.app.AlertDialog.Builder alertDialogBuilder = new android.app.AlertDialog.Builder(CourseDetails.this);
                alertDialogBuilder.setView(promptsView);
                final EditText Row = (EditText) promptsView.findViewById(R.id.editText);
                final EditText column = (EditText) promptsView.findViewById(R.id.editText2);
                final EditText namecolumn=(EditText)promptsView.findViewById(R.id.editText3);
                final EditText sheet=(EditText)promptsView.findViewById(R.id.editText4);
                alertDialogBuilder.setCancelable(false)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int id) {
                                rowval = Integer.parseInt(Row.getText().toString());
                                colval = Integer.parseInt(column.getText().toString());
                                snamecolval=Integer.parseInt(namecolumn.getText().toString());
                                sheetnumber=Integer.parseInt(sheet.getText().toString());
                                startActivityForResult(intent, 2);
                            }
                        })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {


                                        dialog.cancel();
                                    }
                                });
                android.app.AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
            }
        });
    }

    public void confirmdelete(String value, String yearval) {
        String x = value;
        String y = yearval;
        Query query = FirebaseDatabase.getInstance().getReference().child("Courses").child(y).orderByChild("courseName").equalTo(x);
        FirebaseDatabase.getInstance().getReference().child("Roll").child(y).child(x).setValue(null);
        FirebaseDatabase.getInstance().getReference().child("Attendance").child(y).child(x).setValue(null);
        FirebaseDatabase.getInstance().getReference().child("Courses").child(y).child(x).setValue(null);
        FirebaseDatabase.getInstance().getReference().child("Courses").child("All").child(x).setValue(null);
        FirebaseDatabase.getInstance().getReference().child("Status").child(y).child(x).setValue(null);
        FirebaseDatabase.getInstance().getReference().child("Time").child(y).child(x).setValue(null);
        FirebaseDatabase.getInstance().getReference().child("AbsentAttendance").child(y).child(x).setValue(null);
        Intent ix = new Intent(CourseDetails.this, AdminActivity.class);
        startActivity(ix);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
                dataSnapshot.getRef().setValue(null);
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

        Toast.makeText(CourseDetails.this, "Course " + x + " Deleted", Toast.LENGTH_LONG).show();
    }

    public String examine(Uri uri) {
        String extension = "";
        if (uri != null) {
            String path = new File(uri.getPath()).getAbsolutePath();
            if (path != null) {
                String filename;
                Cursor cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor == null) { // Source is Dropbox or other similar local file path
                    filename = uri.getPath();
                } else {
                    cursor.moveToFirst();
                    int idx = cursor.getColumnIndex(MediaStore.Files.FileColumns.DISPLAY_NAME);
                    filename = cursor.getString(idx);
                    cursor.close();
                }
                if (filename != null) {
                    String name = filename.substring(filename.lastIndexOf("."));
                    extension = filename.substring(filename.lastIndexOf(".") + 1);
                }

            }
        }
        return extension;
    }
    static String encodeUserEmail(String userEmail) {
        return userEmail.replace(".", ",");
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        List<RollNumbers> roll = new ArrayList<>();
        List<String>rollnumber=new ArrayList<>();
        List<String>sname=new ArrayList<>();
        if (data == null)
            return;
        String temp, Name = null;
        String xls = "xls";
        switch (requestCode) {
            case 2:
                try {
                    if (resultCode == RESULT_OK) {
                        Uri uri = data.getData();
                        String extension = examine(uri);
                        if (extension.equals(xls)) {
                            try {
                                InputStream is = getContentResolver().openInputStream(data.getData());
                                HSSFWorkbook workbook = new HSSFWorkbook(is);
                                HSSFSheet sheet = workbook.getSheetAt(sheetnumber-1);
                                Bundle extras = data.getExtras();
                                if (extras != null)
                                    Name = extras.getString("name");
                                Iterator<Row> rowIterator = sheet.iterator();
                                while (rowIterator.hasNext()) {
                                    Row row = rowIterator.next();
                                    // For each row, iterate through all the columns
                                    Iterator<Cell> cellIterator = row.cellIterator();

                                    while (cellIterator.hasNext()) {
                                        Cell cell = cellIterator.next();
                                        if ((cell.getColumnIndex() == colval-1) && (cell.getRowIndex() >= rowval-1)) {
                                            switch (cell.getCellType()) {
                                                case Cell.CELL_TYPE_NUMERIC:
                                                    //Toast.makeText(getApplicationContext(),cell.getNumericCellValue()+"",Toast.LENGTH_LONG).show();
                                                    break;
                                                case Cell.CELL_TYPE_STRING:
                                                    temp = cell.getStringCellValue();
                                                    rollnumber.add(temp);
                                                    break;
                                            }
                                        }
                                        if ((cell.getColumnIndex() == snamecolval-1) && (cell.getRowIndex() >= rowval-1)) {
                                            switch (cell.getCellType()) {
                                                case Cell.CELL_TYPE_NUMERIC:
                                                    //Toast.makeText(getApplicationContext(),cell.getNumericCellValue()+"",Toast.LENGTH_LONG).show();
                                                    break;
                                                case Cell.CELL_TYPE_STRING:
                                                    temp = cell.getStringCellValue();
                                                    sname.add(temp);
                                                    break;
                                            }
                                        }

                                    }
                                }
                                for(int i=0;i<sname.size();i++)
                                {
                                    roll.add(new RollNumbers(rollnumber.get(i)+"  "+sname.get(i)));
                                }
                                mRollDatabaseReference.child(presentyear).child(presentCourse).setValue(roll);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        } else {
                            try {
                                InputStream is = getContentResolver().openInputStream(data.getData());
                                XSSFWorkbook workbook = new XSSFWorkbook(is);
                                XSSFSheet sheet = workbook.getSheetAt(sheetnumber-1);
                                //Toast.makeText(getApplicationContext(),sheet.getLastRowNum()+"",Toast.LENGTH_LONG).show();
                                Bundle extras = data.getExtras();
                                if (extras != null)
                                    Name = extras.getString("name");
                                //Toast.makeText(getApplicationContext(),"is"+Name,Toast.LENGTH_LONG).show();
                                Iterator<Row> rowIterator = sheet.iterator();
                                while (rowIterator.hasNext()) {
                                    Row row = rowIterator.next();
                                    // For each row, iterate through all the columns
                                    Iterator<Cell> cellIterator = row.cellIterator();

                                    while (cellIterator.hasNext()) {
                                        Cell cell = cellIterator.next();
                                        if ((cell.getColumnIndex() == colval-1) && (cell.getRowIndex() >=rowval-1)) {
                                            switch (cell.getCellType()) {
                                                case Cell.CELL_TYPE_NUMERIC:
                                                    //Toast.makeText(getApplicationContext(),cell.getNumericCellValue()+"",Toast.LENGTH_LONG).show();
                                                    break;
                                                case Cell.CELL_TYPE_STRING:
                                                    temp = cell.getStringCellValue();
                                                    rollnumber.add(temp);
                                                    // Toast.makeText(getApplicationContext(),cell.getStringCellValue()+"",Toast.LENGTH_LONG).show();
                                                    break;
                                            }
                                        }
                                        else if ((cell.getColumnIndex() == snamecolval-1) && (cell.getRowIndex() >=rowval-1)) {
                                            switch (cell.getCellType()) {
                                                case Cell.CELL_TYPE_NUMERIC:
                                                    //Toast.makeText(getApplicationContext(),cell.getNumericCellValue()+"",Toast.LENGTH_LONG).show();
                                                    break;
                                                case Cell.CELL_TYPE_STRING:
                                                    temp = cell.getStringCellValue();
                                                    sname.add(temp);
                                                    break;
                                            }
                                        }
                                    }
                                }
                                for(int i=0;i<sname.size();i++)
                                {
                                    roll.add(new RollNumbers(rollnumber.get(i)+"  "+sname.get(i)));
                                }
                                mRollDatabaseReference.child(presentyear).child(presentCourse).setValue(roll);

                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(CourseDetails.this, "Excel sheet Added", Toast.LENGTH_LONG).show();
        }
    }

}
