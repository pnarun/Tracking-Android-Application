package com.example.newtrackingdevice;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

public class RegisterActivity extends AppCompatActivity
{
    EditText name,mailid,mpassword,contact;
    Button submit;
    FirebaseAuth fauth;//firebase instance object
    DatabaseReference ref;
    ProgressBar pbar;
    ImageView back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        name=(EditText)findViewById(R.id.nameText);
        mailid=(EditText)findViewById(R.id.mailText);
        mpassword=(EditText)findViewById(R.id.pwdText);
        contact=(EditText)findViewById(R.id.contactText);
        fauth=FirebaseAuth.getInstance();//to get current instance of the database.
        pbar=(ProgressBar)findViewById(R.id.regProgBar);
        submit=(Button)findViewById(R.id.submitButton);
        if(fauth.getCurrentUser()!=null)
        {
            Toast.makeText(this, "You're already a user..", Toast.LENGTH_SHORT).show();
        }

        submit.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String email=mailid.getText().toString().trim();
                String password=mpassword.getText().toString().trim();
                //input validation.
                if(TextUtils.isEmpty(email))
                {
                    mailid.setError("Required Email-ID");
                    return;
                }
                if(TextUtils.isEmpty(password))
                {
                    mpassword.setError("Required password");
                    return;
                }
                if(password.length()<6)
                {
                    mpassword.setError("password must be >= 6 characters");
                    return;
                }
                pbar.setVisibility(View.VISIBLE);
                //registering user
                fauth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(RegisterActivity.this, "Registered successfully...", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(getApplicationContext(),NewDeviceAuthenticationActivity.class));//to move to first activity upon successful registeration
                        }
                        else
                        {
                            Toast.makeText(RegisterActivity.this, "Error: "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            pbar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }
        });
    }
}
