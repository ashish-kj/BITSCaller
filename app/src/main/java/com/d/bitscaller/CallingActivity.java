package com.d.bitscaller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

public class CallingActivity extends AppCompatActivity {

    private TextView nameContact;
    private ImageView profileImage;
    private ImageView cancelCallBtn,acceptCallBtn;

    private String receiverUserId="",receiverUserImage="",receiverUsername="";
    private String senderUserId="",senderUserImage="",senderUsername="",checker = "";
    private String callingId="",ringingId="";
    private DatabaseReference usersRef;

    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calling);
        senderUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        receiverUserId = getIntent().getExtras().get("visit_user_id").toString();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        mediaPlayer = MediaPlayer.create(this,R.raw.iphone_remix_lol);

        nameContact =findViewById(R.id.name_calling);
        profileImage =findViewById(R.id.profile_image_calling);
        cancelCallBtn =findViewById(R.id.cancel_call);
        acceptCallBtn =findViewById(R.id.make_call);

        cancelCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mediaPlayer.stop();
                checker = "clicked";

                cancelCallingUser();
            }
        });

        acceptCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mediaPlayer.stop();

                final HashMap<String,Object> callingPickUpMap = new HashMap<>();
                callingPickUpMap.put("picked","picked");

                usersRef.child(senderUserId).child("Ringing").updateChildren(callingPickUpMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful())
                        {
                            Intent intent = new Intent(CallingActivity.this,VideoChatActivity.class);
                            startActivity(intent);
                        }
                    }
                });
            }
        });

        getAndSetUserProfileInfo();
    }



    private void getAndSetUserProfileInfo() {
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(receiverUserId).exists())
                {
                    receiverUserImage = dataSnapshot.child(receiverUserId).child("image").getValue().toString();
                    receiverUsername = dataSnapshot.child(receiverUserId).child("name").getValue().toString();

                    nameContact.setText(receiverUsername);
                    Picasso.get().load(receiverUserImage).placeholder(R.drawable.profile_image).into(profileImage);
                }
                if(dataSnapshot.child(senderUserId).exists())
                {
                    senderUserImage = dataSnapshot.child(senderUserId).child("image").getValue().toString();
                    senderUsername = dataSnapshot.child(senderUserId).child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mediaPlayer.start();

        usersRef.child(receiverUserId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!checker.equals("clicked") && !dataSnapshot.hasChild("Calling") && !dataSnapshot.hasChild("Ringing"))
                {

                    final HashMap<String, Object> callingInfo = new HashMap<>();
                    callingInfo.put("calling",receiverUserId);

                    usersRef.child(senderUserId)
                            .child("Calling")
                            .updateChildren(callingInfo)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        final HashMap<String, Object> ringingInfo = new HashMap<>();
                                        ringingInfo.put("ringing",senderUserId);

                                        usersRef.child(receiverUserId).child("Ringing").updateChildren(ringingInfo);
                                    }
                                }
                            });
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.child(senderUserId).hasChild("Ringing") && !dataSnapshot.child(senderUserId).hasChild("Calling"))
                {
                    acceptCallBtn.setVisibility(View.VISIBLE);
                }

                if(dataSnapshot.child(receiverUserId).child("Ringing").hasChild("picked"))
                {
                    mediaPlayer.stop();
                    Intent intent = new Intent(CallingActivity.this,VideoChatActivity.class);
                    startActivity(intent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void cancelCallingUser() {
        //from sender side
        usersRef.child(senderUserId).child("Calling").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.hasChild("calling"))
                {
                    callingId = dataSnapshot.child("calling").getValue().toString();
                    usersRef.child(callingId)
                            .child("Ringing")
                            .removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        usersRef.child(senderUserId)
                                                .child("Calling")
                                                .removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        startActivity(new Intent(CallingActivity.this,RegistrationActivity.class));
                                                        finish();
                                                    }
                                                });
                                    }
                                }
                            });
                }
                else
                {
                    startActivity(new Intent(CallingActivity.this,RegistrationActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //from receiver side
        usersRef.child(senderUserId).child("Ringing").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists() && dataSnapshot.hasChild("ringing"))
                {
                    ringingId = dataSnapshot.child("ringing").getValue().toString();
                    usersRef.child(ringingId)
                            .child("Calling")
                            .removeValue()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        usersRef.child(senderUserId)
                                                .child("Ringing")
                                                .removeValue()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        startActivity(new Intent(CallingActivity.this,RegistrationActivity.class));
                                                        finish();
                                                    }
                                                });
                                    }
                                }
                            });
                }
                else
                {
                    startActivity(new Intent(CallingActivity.this,RegistrationActivity.class));
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {

            }
        });
    }
}
