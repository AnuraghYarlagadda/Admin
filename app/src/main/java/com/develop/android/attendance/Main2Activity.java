package com.develop.android.attendance;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class Main2Activity extends AppCompatActivity {
    TextView name, email, phno;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseAuth mFirebaseAuth;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Intent intent = new Intent(Main2Activity.this, AdminActivity.class);
                    Main2Activity.this.startActivity(intent);
                    return true;
                case R.id.navigation_dashboard:
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mFirebaseAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mFirebaseAuth.getCurrentUser();
        name = (TextView) findViewById(R.id.textView);
        email = (TextView) findViewById(R.id.textView2);
        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        String uname = mFirebaseAuth.getCurrentUser().getDisplayName().toString();
        name.setText(uname);
        String uemail = mFirebaseAuth.getCurrentUser().getEmail().toString();
        email.setText(uemail);
        if (user.getPhotoUrl() != null) {
            String photoUrl = user.getPhotoUrl().toString();
            Picasso.with(this).load(photoUrl).resize(300, 300).centerCrop().into(imageView);

            //Set profile picture
        }

    }

}
