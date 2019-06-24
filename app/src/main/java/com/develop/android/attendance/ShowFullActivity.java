package com.develop.android.attendance;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
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
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ShowFullActivity extends AppCompatActivity {
    private static List<RollNumbers> totalRoll = new ArrayList<>();
    private final Set<String> fullatt = new HashSet<>();
    private ChildEventListener mAttendanceChildEventListener;
    private String year;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mCoursesDatabaseReference, mAttendanceDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_full);
        Intent intent = getIntent();
        final String courseYear = intent.getStringExtra("CourseYear");
        year = courseYear;
        totalRoll.clear();
        fullatt.clear();
        Button downloadpdf = (Button) findViewById(R.id.downloadpdf);
        final Button clickpdf = (Button) findViewById(R.id.clickpdf);
        final LinearLayout ll1 = (LinearLayout) findViewById(R.id.my_layout1);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mAttendanceDatabaseReference = mFirebaseDatabase.getReference().child("Attendance");
        mAttendanceDatabaseReference.child(courseYear).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    GenericTypeIndicator<List<RollNumbers>> t = new GenericTypeIndicator<List<RollNumbers>>() {
                    };
                    totalRoll = snapshot.getValue(t);
                    final LinearLayout ll1 = (LinearLayout) findViewById(R.id.my_layout1);
                    for (int i = 0; i < totalRoll.size(); i++)
                        fullatt.add(totalRoll.get(i).getRollnum());

                }
                getData(ll1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        downloadpdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int flag=createPdf();
                if (flag==0)
                {
                    clickpdf.setClickable(false);
                }
            }
        });
        clickpdf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                File pdfFile = new File(Environment.getExternalStorageDirectory() + "/Attendance/" + "Year" + year + ".pdf");
                Uri excelPath;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

                    excelPath = FileProvider.getUriForFile(ShowFullActivity.this, "com.develop.android.attendance", pdfFile);
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
                    Toast.makeText(ShowFullActivity.this, "No Application available to view PDF", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void getData(LinearLayout ll) {

        Set<String> tree_Set = new TreeSet<String>(fullatt);
        List<String> stringsList = new ArrayList<>(tree_Set);
        for (int i = 0; i < stringsList.size(); i++) {
            TextView cb = new TextView(this);
            cb.setText(stringsList.get(i));
            cb.setId(i);
            ll.addView(cb);

        }
    }

    private int createPdf() {
        // create a new document
        PdfDocument document = new PdfDocument();

        // crate a page description
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(400, 1000, 1).create();


        // start a page
        PdfDocument.Page page = document.startPage(pageInfo);
        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();
        paint.setTextSize(15);
        paint.setColor(Color.BLACK);
        Set<String> tree_Set = new TreeSet<String>(fullatt);
        List<String> stringsList = new ArrayList<>(tree_Set);
        int flag=0;
        int x = 20;
        int y = 20;
        if(stringsList.size()!=0)
        {
            flag=1;
        }
        for (int i = 0; i < stringsList.size(); i++) {
            if (y > 980) {
                x += 200;
                y = 20;
            }
            canvas.drawText(stringsList.get(i).toString(), x, y, paint);
            y += 20;
        }

        document.finishPage(page);
        if(flag==1)
        {
            String directory_path = Environment.getExternalStorageDirectory().getPath() + "/Attendance/";
            File file = new File(directory_path);
            if (!file.exists()) {
                file.mkdirs();
            }
            int i = 1;
            String targetPdf = directory_path + "Year" + year + ".pdf";
            File filePath = new File(targetPdf);
            try {
                document.writeTo(new FileOutputStream(filePath));
                Toast.makeText(this, "Pdf File Saved", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                Log.e("main", "error " + e.toString());
                Toast.makeText(this, "Something wrong: " + e.toString(), Toast.LENGTH_LONG).show();
            }
        }
        else
        {
            Toast.makeText(this,"No records to Download",Toast.LENGTH_SHORT).show();
            return 0;
        }

        // close the document
        document.close();
        return 1;
    }
}
