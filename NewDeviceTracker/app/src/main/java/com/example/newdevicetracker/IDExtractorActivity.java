package com.example.newdevicetracker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class IDExtractorActivity extends AppCompatActivity
{
    DatabaseReference ref;
    EditText searchText;
    String mailstring;
    String idstring;
    TextView actualID;
    ImageView copyButton;
    int flag;
    OnlineDevices device;
    public void nextClicked(View view)
    {
        startActivity(new Intent(getApplicationContext(),ActualUserMapActivity.class));
    }
    public void copyClicked(View view)
    {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("ID", actualID.getText().toString());
        clipboard.setPrimaryClip(clip);
        Toast.makeText(this, "ID copied", Toast.LENGTH_SHORT).show();
    }
    public void extractClicked(View view)
    {
        flag=0;
        ref=FirebaseDatabase.getInstance().getReference().child("OnlineDevices");
        searchText=(EditText)findViewById(R.id.searchBox);
        actualID=(TextView) findViewById(R.id.actualID);
        mailstring=searchText.getText().toString();
        ref.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if(dataSnapshot.exists())
                {
                    for (DataSnapshot snap : dataSnapshot.getChildren()) {
                        device = snap.getValue(OnlineDevices.class);
                        if (device.getMailid().equals(mailstring)) {
                            idstring = device.getUserid();
                            flag = 1;
                            break;
                        }
                    }
                    if (flag == 1) {
                        actualID.setText(idstring);
                        copyButton.setClickable(true);
                    } else if (flag == 0)
                        actualID.setText("No such device is online.");
                }
                else
                    Toast.makeText(IDExtractorActivity.this, "Database empty", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_i_d_extractor);
        copyButton=(ImageView)findViewById(R.id.copyButton);
        copyButton.setClickable(false);
        ref= FirebaseDatabase.getInstance().getReference().child("OnlineDevices");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                //for(DataSnapshot snap:dataSnapshot)
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
