package com.example.newdevicetracker;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NewUserAuthenticationActivity extends AppCompatActivity
{
    EditText logmailid,logpassword;
    Button loginButton;
    TextView regLab,displayText;
    FirebaseAuth fAuth;
    DatabaseReference ref;
    ProgressBar logBar;
    OnlineDevices device;
    String id;
    ImageView img;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user_authentication);
        displayText=(TextView)findViewById(R.id.displayText);
        img=(ImageView) findViewById(R.id.typeImage);
        img.setImageResource(R.drawable.gps);
        logmailid=(EditText)findViewById(R.id.mailField);
        logpassword=(EditText)findViewById(R.id.pwdField);
        logmailid.setHint("User ID");
        logpassword.setHint("password");
        logmailid.setHintTextColor(getColor(R.color.grey));
        logpassword.setHintTextColor(getColor(R.color.grey));
        loginButton=(Button)findViewById(R.id.loginButton);
        regLab=(TextView)findViewById(R.id.regDisplay);
        logBar=(ProgressBar)findViewById(R.id.logProgBar);
        fAuth=FirebaseAuth.getInstance();
        device=new OnlineDevices();
        logBar.setVisibility(View.INVISIBLE);
        regLab.setClickable(true);
        regLab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(getApplicationContext(),RegisterActivity.class));
            }
        });
        loginButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final String email = logmailid.getText().toString().trim();
                String password = logpassword.getText().toString().trim();
                //input validation.
                if (TextUtils.isEmpty(email)) {
                    logmailid.setError("Required Email-ID");
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    logpassword.setError("Required password");
                    return;
                }
                if (password.length() < 6) {
                    logpassword.setError("password must be >= 6 characters");
                    return;
                }
                logBar.setVisibility(View.VISIBLE);
                //authenticating the user
                fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(NewUserAuthenticationActivity.this, "Logging in..", Toast.LENGTH_SHORT).show();
                            logBar.setVisibility(View.INVISIBLE);
                            startActivity(new Intent(getApplicationContext(), IDExtractorActivity.class));//to move to first device extractor upon successful login.
                        } else
                        {
                            Toast.makeText(NewUserAuthenticationActivity.this, "Log in unsuccessful:" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            logBar.setVisibility(View.INVISIBLE);
                        }
                    }
                });
            }

        });
    }
}
