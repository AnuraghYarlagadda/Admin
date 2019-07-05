package com.develop.android.attendance;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static java.lang.Thread.sleep;

public class ShowAttActivity extends AppCompatActivity {
    private static List<RollNumbers> roll = new ArrayList<>();
    TextView text;
    int pflag,aflag;
    private Set<String> presentlist = new HashSet<>();
    private Set<String> absentlist = new HashSet<>();
    private String value, yearval, what, time;
    private FirebaseDatabase mFirebaseDatabase;
    private ChildEventListener mChildEventListener;
    private DatabaseReference mAttendanceDatabaseReference, mTimeDatabaseReference, mAbsentDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_att);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        aflag=pflag=0;
        mAttendanceDatabaseReference = mFirebaseDatabase.getReference().child("Attendance");
        mTimeDatabaseReference = mFirebaseDatabase.getReference().child("Time");
        mAbsentDatabaseReference = mFirebaseDatabase.getReference().child("AbsentAttendance");
        final LinearLayout ll1 = (LinearLayout) findViewById(R.id.my_layout1);
        Button downloadpdf = (Button) findViewById(R.id.downloadpdf);
        final Button clickpdf = (Button) findViewById(R.id.clickpdf);
        text = (TextView) findViewById(R.id.status);
        Intent intent = getIntent();
        value = intent.getStringExtra("CourseName");
        yearval = intent.getStringExtra("Year");
        what = intent.getStringExtra("what");
        Query query1 = mTimeDatabaseReference.child(yearval).child(value);
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                time = (String) dataSnapshot.getValue();
                text.setText("Last Updated On: " + time + "\n" + value + "_" + yearval + "_" + what);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        if (what.equals("present")) {
            roll.clear();
            Query query = mAttendanceDatabaseReference.child(yearval).child(value);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    GenericTypeIndicator<List<RollNumbers>> t = new GenericTypeIndicator<List<RollNumbers>>() {
                    };
                    if (dataSnapshot.exists()) {
                        pflag=1;
                        roll = dataSnapshot.getValue(t);
                        for (int i = 0; i < roll.size(); i++) {
                            presentlist.add(roll.get(i).getRollnum());
                        }
                        Set<String> tree_Set = new TreeSet<String>(presentlist);
                        List<String> stringsList = new ArrayList<>(tree_Set);
                        for (int i = 0; i < stringsList.size(); i++) {
                            TextView cb = new TextView(ShowAttActivity.this);
                            cb.setText(stringsList.get(i));
                            cb.setId(roll.size());
                            ll1.addView(cb);
                        }
                        TextView cb = new TextView(ShowAttActivity.this);
                        cb.setText("Present Count:" + roll.size());
                        cb.setId(roll.size());
                        ll1.addView(cb);

                    } else {
                        pflag=0;
                        TextView cb = new TextView(ShowAttActivity.this);
                        cb.setText("Present Count: 0");
                        cb.setId(0);
                        ll1.addView(cb);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        } else {
            roll.clear();
            Query queryabsent = mAbsentDatabaseReference.child(yearval).child(value);
            queryabsent.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    GenericTypeIndicator<List<RollNumbers>> t = new GenericTypeIndicator<List<RollNumbers>>() {
                    };
                    if (dataSnapshot.exists()) {
                        aflag=1;
                        roll = dataSnapshot.getValue(t);
                        for (int i = 0; i < roll.size(); i++) {
                            absentlist.add(roll.get(i).getRollnum());
                        }
                        Set<String> tree_Set = new TreeSet<String>(absentlist);
                        List<String> stringsList = new ArrayList<>(tree_Set);
                        for (int i = 0; i < stringsList.size(); i++) {
                            TextView cb = new TextView(ShowAttActivity.this);
                            cb.setText(stringsList.get(i));
                            cb.setId(roll.size());
                            ll1.addView(cb);
                        }
                        TextView cb = new TextView(ShowAttActivity.this);
                        cb.setText("Absent Count:" + roll.size());
                        cb.setId(roll.size());
                        ll1.addView(cb);

                    } else {
                        aflag=0;
                        TextView cb = new TextView(ShowAttActivity.this);
                        cb.setText("Absent Count: 0");
                        cb.setId(0);
                        ll1.addView(cb);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        downloadpdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requestAppPermissions();
                if (hasReadPermissions() && hasWritePermissions()) {
                    createPdf(what);
                }
            }
        });
        clickpdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                File pdfFile = new File(Environment.getExternalStorageDirectory() + "/Attendance/" + value + "_year" + yearval + "_" + what + ".pdf");
                if (pdfFile.exists()) {
                    Uri excelPath;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                        excelPath = FileProvider.getUriForFile(ShowAttActivity.this, "com.develop.android.attendance", pdfFile);
                    } else {
                        excelPath = Uri.fromFile(pdfFile);
                    }
                    Intent pdfIntent = new Intent(Intent.ACTION_VIEW);
                    pdfIntent.setDataAndType(excelPath, "application/pdf");
                    pdfIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    pdfIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    pdfIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    try {
                        startActivity(pdfIntent);
                    } catch (ActivityNotFoundException e) {
                        Toast.makeText(ShowAttActivity.this, "No Application available to view PDF", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(ShowAttActivity.this, "File not found!", Toast.LENGTH_SHORT).show();
                }

            }
        });
    }

    private void createPdf(String what) {
        // create a new document
        if (what.equals("present")) {
            Set<String> tree_Set = new TreeSet<String>(presentlist);
            List<String> stringsList = new ArrayList<>(tree_Set);
            PdfDocument document = new PdfDocument();

            // crate a page description
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            // start a page
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();
            Drawable d =  ResourcesCompat.getDrawable(getResources(), R.drawable.pdfimage, null);
            d.setBounds(0, 0, 595, 190);
            d.draw(canvas);
            paint.setTextSize(12);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            paint.setColor(Color.BLACK);
            canvas.drawText( time,20,220,paint);
            paint.setTextSize(30);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            paint.setColor(Color.BLACK);
            canvas.drawText(value,200,220,paint);
            paint.setTextSize(12);
            canvas.drawText("Year : "+yearval,540,220,paint);
            paint.setTextSize(20);
            canvas.drawText("Present List",250,250,paint);
            paint.reset();
            paint.setTextSize(15);
            paint.setColor(Color.BLACK);
            if(pflag==1)
            {
                int x = 100;
                int y = 300;
                for (int i = 0; i < stringsList.size(); i++) {
                    canvas.drawText(stringsList.get(i).toString(), x, y, paint);
                    y += 20;
                    if(y>800)
                    {
                        document.finishPage(page);
                        pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 2).create();
                        page = document.startPage(pageInfo);
                        canvas=page.getCanvas();
                        paint.setTextSize(15);
                        paint.setColor(Color.BLACK);
                        x = 100;
                        y = 40;
                        i+=1;
                        canvas.drawText(stringsList.get(i).toString(), x, y, paint);
                        y += 20;

                    }
                }
                canvas.drawText("Present Count:"+stringsList.size(),x,y,paint);
            }
            else
            {
                int x = 100;
                int y = 300;
                canvas.drawText("Present Count: 0",x,y,paint);
            }

            document.finishPage(page);
            String directory_path = Environment.getExternalStorageDirectory().getPath() + "/Attendance/";
            File file = new File(directory_path);
            if (!file.exists()) {
                file.mkdirs();
            }
            String targetPdf = directory_path + value + "_year" + yearval + "_" + what + ".pdf";
            File filePath = new File(targetPdf);
            try {
                if (hasWritePermissions()) {
                    document.writeTo(new FileOutputStream(filePath));
                    try {
                        sleep(1000);
                    } catch (Exception e) {

                    }
                    Toast.makeText(this, "Pdf File Saved", Toast.LENGTH_SHORT).show();
                }

            } catch (IOException e) {
                Toast.makeText(this, "You didn't permit storage access", Toast.LENGTH_SHORT).show();
            }
            // close the document
            document.close();
        } else {
            Set<String> tree_Set = new TreeSet<String>(absentlist);
            List<String> stringsList = new ArrayList<>(tree_Set);
            PdfDocument document = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create();
            PdfDocument.Page page = document.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();
            Drawable d =  ResourcesCompat.getDrawable(getResources(), R.drawable.pdfimage, null);
            d.setBounds(0, 0, 595, 190);
            d.draw(canvas);
            paint.setTextSize(12);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            paint.setColor(Color.BLACK);
            canvas.drawText( time,20,220,paint);
            paint.setTextSize(30);
            paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            paint.setColor(Color.BLACK);
            canvas.drawText(value,200,220,paint);
            paint.setTextSize(12);
            canvas.drawText("Year : "+yearval,540,220,paint);
            paint.setTextSize(20);
            canvas.drawText("Absent List",250,250,paint);
            paint.reset();
            paint.setTextSize(15);
            paint.setColor(Color.BLACK);
            if(aflag==1)
            {
                int x = 100;
                int y = 300;
                for (int i = 0; i < stringsList.size(); i++) {
                    canvas.drawText(stringsList.get(i).toString(), x, y, paint);
                    y += 20;
                    if(y>800)
                    {
                        document.finishPage(page);
                        pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 2).create();
                        page = document.startPage(pageInfo);
                        canvas=page.getCanvas();
                        paint.setTextSize(15);
                        paint.setColor(Color.BLACK);
                        x = 100;
                        y = 40;
                        i+=1;
                        canvas.drawText(stringsList.get(i).toString(), x, y, paint);
                        y += 20;

                    }
                }
                canvas.drawText("Absent Count:"+stringsList.size(),x,y,paint);
            }
            else
            {
                int x = 100;
                int y = 300;
                canvas.drawText("Absent Count: 0",x,y,paint);
            }
            document.finishPage(page);
            String directory_path = Environment.getExternalStorageDirectory().getPath() + "/Attendance/";
            File file = new File(directory_path);
            if (!file.exists()) {
                file.mkdirs();
            }
            String targetPdf = directory_path + value + "_year" + yearval + "_" + what + ".pdf";
            File filePath = new File(targetPdf);
            try {
                if (hasWritePermissions()) {
                    document.writeTo(new FileOutputStream(filePath));
                    try {
                        sleep(1000);
                    } catch (Exception e) {

                    }
                    Toast.makeText(this, "Pdf File Saved", Toast.LENGTH_SHORT).show();
                }

            } catch (IOException e) {
                Toast.makeText(this, "You didn't permit storage access", Toast.LENGTH_SHORT).show();
            }
            // close the document
            document.close();
        }
        return;
    }

    private void requestAppPermissions() {
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }

        if (hasReadPermissions() && hasWritePermissions()) {
            return;
        }

        ActivityCompat.requestPermissions(this,
                new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, 112); // your request code
    }

    private boolean hasReadPermissions() {
        return (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }

    private boolean hasWritePermissions() {
        return (ContextCompat.checkSelfPermission(getBaseContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED);
    }
}