package com.example.votingapp;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.votingapp.BroadcastReciever.AlarmReciever;
import com.example.votingapp.Common.Common;
import com.example.votingapp.Model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {
    MaterialEditText edtNewUser,edtNewPassword,edtNewEmail;//for register
    MaterialEditText edtUser,edtPassword;//for sign in

    Button btnRegister,btnSignIn;

    FirebaseDatabase database;
    DatabaseReference users;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        registerAlarm();
        //Firebase
        database = FirebaseDatabase.getInstance();
        users = database.getReference("Users");

        edtUser = (MaterialEditText)findViewById(R.id.edtUser);
        edtPassword = (MaterialEditText)findViewById(R.id.edtPassword);

        btnRegister = (Button)findViewById(R.id.btn_register);
        btnSignIn = (Button)findViewById(R.id.btn_sign_in);

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRegisterDialog();
            }
        });
        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn(edtUser.getText().toString(),edtPassword.getText().toString());
            }
        });

    }

    private void registerAlarm() {

        Calendar calendar = Calendar.getInstance();
        if(System.currentTimeMillis()>calendar.getTimeInMillis())
        {
            calendar.add(Calendar.DAY_OF_MONTH,1);
        }
        calendar.set(Calendar.HOUR_OF_DAY,12);
        calendar.set(Calendar.MINUTE,17);
        calendar.set(Calendar.SECOND,0);

        Intent intent = new Intent(MainActivity.this, AlarmReciever.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(MainActivity.this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager)this.getSystemService(this.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),AlarmManager.INTERVAL_DAY,pendingIntent);
    }

    private void signIn(final String user, final String password) {
        users.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(user).exists()){
                    if(!user.isEmpty()){
                        User login = dataSnapshot.child(user).getValue(User.class);
                        if(login.getPassword().equals(password)){
                            Intent homeActivity = new Intent(MainActivity.this,Home.class);
                            Common.currentUser = login;
                            startActivity(homeActivity);
                            finish();
                        }
                        else
                            Toast.makeText(MainActivity.this,"Wrong password !",Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(MainActivity.this,"Please enter your user name",Toast.LENGTH_SHORT).show();
                    }
                }
                else{
                    Toast.makeText(MainActivity.this,"There`s no such user",Toast.LENGTH_SHORT).show();
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private void showRegisterDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle("Register");
        alertDialog.setMessage("Please fill full information");

        LayoutInflater inflater = this.getLayoutInflater();
        View register_layout = inflater.inflate(R.layout.register_layout,null);

        edtNewUser = (MaterialEditText)register_layout.findViewById(R.id.edtNewUserName);
        edtNewEmail = (MaterialEditText)register_layout.findViewById(R.id.edtNewEmail);
        edtNewPassword = (MaterialEditText)register_layout.findViewById(R.id.edtNewPassword);

        alertDialog.setView(register_layout);
        alertDialog.setIcon(R.drawable.ic_account_circle_black_24dp);

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final User user = new User(edtNewUser.getText().toString(),
                        edtNewPassword.getText().toString(),
                        edtNewEmail.getText().toString());

                users.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                            if(dataSnapshot.child(user.getUserName()).exists())
                                Toast.makeText(MainActivity.this,"This user already exists !", Toast.LENGTH_SHORT).show();
                            else
                            {
                                users.child(user.getUserName()).setValue(user);
                                Toast.makeText(MainActivity.this,"Registration completed !",Toast.LENGTH_SHORT).show();
                            }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {


                    }
                });
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }
}
