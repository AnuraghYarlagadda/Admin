package com.develop.android.attendance;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static android.os.SystemClock.sleep;

public class MainActivity extends AppCompatActivity {
    public static final int RC_SIGN_IN = 1;
    public String adminemail = "shinybandi@gmail.com";
    ImageButton enter;
    private TextView mTextMessage;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mCoursesDatabaseReference, mAdminDatabaseReference;
    private FirebaseAuth mFirebaseAuth;
    private ChildEventListener mChildEventListener;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    static String encodeUserEmail(String userEmail) {
        return userEmail.replace(".", ",");
    }

    static String decodeUserEmail(String userEmail) {
        return userEmail.replace(",", ".");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        enter = (ImageButton) findViewById(R.id.button);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mCoursesDatabaseReference = mFirebaseDatabase.getReference().child("Courses");
        mAdminDatabaseReference = mFirebaseDatabase.getReference().child("Admin");
        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    //user is signedin
                    Toast.makeText(MainActivity.this, "You're now signed in.", Toast.LENGTH_SHORT).show();
                } else {
                    //user is signed out
                    // Choose authentication providers
                    List<AuthUI.IdpConfig> providers = Arrays.asList(
                            new AuthUI.IdpConfig.EmailBuilder().build(),
                            new AuthUI.IdpConfig.GoogleBuilder().build());

                    // Create and launch sign-in intent
                    startActivityForResult(
                            AuthUI.getInstance()
                                    .createSignInIntentBuilder()
                                    .setIsSmartLockEnabled(false)
                                    .setAvailableProviders(providers)
                                    .build(),
                            RC_SIGN_IN);
                }

            }
        };
        mAdminDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                adminemail = dataSnapshot.getValue().toString();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        enter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ccurrentuser = mFirebaseAuth.getCurrentUser().getEmail();
                mAdminDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        adminemail = dataSnapshot.getValue().toString();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
                sleep(3000);
                Toast.makeText(MainActivity.this, "Loading...", Toast.LENGTH_LONG);
                if (ccurrentuser.equals(adminemail)) {
                    Intent intent = new Intent(MainActivity.this, AdminActivity.class);
                    MainActivity.this.startActivity(intent);
                } else {
                    Intent intent = new Intent(MainActivity.this, MainActivity.class);
                    MainActivity.this.startActivity(intent);
                }
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data == null)
            return;
        switch (requestCode) {
            case 1:
                if (requestCode == RC_SIGN_IN) {
                    if (resultCode == RESULT_OK) {
                        // Sign-in succeeded, set up the UI
                        Toast.makeText(this, "Signed in!", Toast.LENGTH_SHORT).show();
                    } else if (resultCode == RESULT_CANCELED) {
                        // Sign in was canceled by the user, finish the activity
                        Toast.makeText(this, "Sign in canceled", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_signout:
                AuthUI.getInstance().signOut(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onResume() {
        super.onResume();
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuthStateListener != null) {
            mFirebaseAuth.removeAuthStateListener(mAuthStateListener);
        }
    }

}