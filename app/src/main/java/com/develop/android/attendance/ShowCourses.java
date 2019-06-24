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
    private static List<RollNumbers> totalRoll =new ArrayList<>();
    String presentCourse,presentyear;
    Button showfullatt,status;
    private CourseAdapter mCourseAdapter;
    private ListView mCourseListView;
    private ChildEventListener mChildEventListener,mRollChildEventListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mCoursesDatabaseReference,mRollDatabaseReference,mUsersDatabaseReference,mStatusDatabaseReference;

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
        final List<RollNumbers> fullatt=new ArrayList<RollNumbers>();
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
                    Intent intent = new Intent(ShowCourses.this, ShowFullActivity.class);
                    intent.putExtra("CourseYear", courseYear);
                    ShowCourses.this.startActivity(intent);
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
    public void addExcel(final Courses thisCourse){
        Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
        //  intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.setType("*/*");
        intent.putExtra("name",thisCourse.courseName);
        presentCourse=thisCourse.courseName;
        presentyear=thisCourse.year;
        Toast.makeText(this, "before!"+thisCourse.courseName, Toast.LENGTH_LONG).show();
        Log.d("chek", "addexcel in");
        startActivityForResult(intent, 2);
        Toast.makeText(this, "before!"+thisCourse.courseName, Toast.LENGTH_LONG).show();
        // Toast.makeText(this, "After in!", Toast.LENGTH_LONG).show();
        //}
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
        Log.d("staite",flag[0]+"");
        return flag[0];
    }
public  void gocourse(final Courses thisCourse,String status){
        Intent intent=new Intent(ShowCourses.this,CourseDetails.class);
        intent.putExtra("CourseName",thisCourse.courseName);
        intent.putExtra("Year",thisCourse.year);
        intent.putExtra("faculty",thisCourse.courseFaculty);
        intent.putExtra("emails",thisCourse.emails);
        intent.putExtra("status",status);
        ShowCourses.this.startActivity(intent);
    }
    public void showAtt(final Courses thisCourse){
        Query query=FirebaseDatabase.getInstance().getReference().child("Attendance").child(thisCourse.year).child(thisCourse.courseName);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Intent intent=new Intent(ShowCourses.this,ShowAttActivity.class);
                    intent.putExtra("CourseName",thisCourse.courseName);
                    intent.putExtra("Year",thisCourse.year);
                    ShowCourses.this.startActivity(intent);
                } else {
                    Log.e("hhh", "N");
                    Toast.makeText(ShowCourses.this, "Attendance Not Added", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }
    public void permitaddatt(final Courses thisCourse){
        final String courseName=thisCourse.courseName;
        final String Year=thisCourse.year;
        Query query=FirebaseDatabase.getInstance().getReference().child("Roll").child(Year).child(courseName);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Intent intent = new Intent(ShowCourses.this, AddAttActivity.class);
                    intent.putExtra("CourseName", courseName);
                    intent.putExtra("Year",Year);
                    ShowCourses.this.startActivity(intent);
                    Log.e("hhhh", "Y" + "");
                } else {
                    Log.e("hhh", "N");
                    Toast.makeText(ShowCourses.this, "Please Add Excel", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        List<RollNumbers> roll =new ArrayList<>();
        // HashMap<String, String> rolls = new HashMap()<String,String>;
        if (data == null)
            return;
        String temp,Name = null;
        switch (requestCode) {
            case 2:
                try {
                    if (resultCode == RESULT_OK) {
                        try {
                            Log.d("chek", "addexcel in try");
                            Toast.makeText(getApplicationContext(),"Ushh",Toast.LENGTH_LONG).show();
                            InputStream is = getContentResolver().openInputStream(data.getData());
                            XSSFWorkbook workbook = new XSSFWorkbook(is);
                            XSSFSheet sheet = workbook.getSheetAt(0);
                            //Toast.makeText(getApplicationContext(),sheet.getLastRowNum()+"",Toast.LENGTH_LONG).show();
                            Bundle extras=data.getExtras();
                            if(extras!=null)
                                Name=extras.getString("name");
                            //Toast.makeText(getApplicationContext(),"is"+Name,Toast.LENGTH_LONG).show();
                            Iterator<Row> rowIterator = sheet.iterator();
                            while (rowIterator.hasNext()) {
                                Row row = rowIterator.next();
                                // For each row, iterate through all the columns
                                Iterator<Cell> cellIterator = row.cellIterator();

                                while (cellIterator.hasNext()) {
                                    Cell cell = cellIterator.next();
                                    if((cell.getColumnIndex()==1)&&(cell.getRowIndex()>0)){
                                        switch (cell.getCellType()) {
                                            case Cell.CELL_TYPE_NUMERIC:
                                                //Toast.makeText(getApplicationContext(),cell.getNumericCellValue()+"",Toast.LENGTH_LONG).show();
                                                break;
                                            case Cell.CELL_TYPE_STRING:
                                                temp=cell.getStringCellValue();
                                                RollNumbers rollnumbers= new RollNumbers(temp);
                                                roll.add(new RollNumbers(temp));
                                                // Toast.makeText(getApplicationContext(),cell.getStringCellValue()+"",Toast.LENGTH_LONG).show();
                                                break;
                                        }
                                    }

                                }
                                mRollDatabaseReference.child(presentyear).child(presentCourse).setValue(roll);

                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
        }
    }
}
