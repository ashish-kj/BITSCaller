package com.d.bitscaller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.opentok.android.OpentokError;
import com.opentok.android.Publisher;
import com.opentok.android.PublisherKit;
import com.opentok.android.Session;
import com.opentok.android.Stream;
import com.opentok.android.Subscriber;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class VideoChatActivity extends AppCompatActivity implements Session.SessionListener, PublisherKit.PublisherListener {

    private static String API_KEY ="46919034";
    private static String SESSION_ID ="2_MX40NjkxOTAzNH5-MTU5OTk0OTQ2MDAzM35YNEFQOEpMZnNaR3BDVE1nZmU2YTNCcHp-fg";
    private static String TOKEN ="T1==cGFydG5lcl9pZD00NjkxOTAzNCZzaWc9MjhjMDkxMGIzZGYxZDdmNTBlODI1OTcyYTM5MzMyOGJiMzdkODVmYzpzZXNzaW9uX2lkPTJfTVg0ME5qa3hPVEF6Tkg1LU1UVTVPVGswT1RRMk1EQXpNMzVZTkVGUU9FcE1abk5hUjNCRFZFMW5abVUyWVROQ2NIcC1mZyZjcmVhdGVfdGltZT0xNTk5OTQ5NTIxJm5vbmNlPTAuMzY1NjYzMDE0OTcyNjQ2OTcmcm9sZT1wdWJsaXNoZXImZXhwaXJlX3RpbWU9MTYwMjU0MTUyMCZpbml0aWFsX2xheW91dF9jbGFzc19saXN0PQ==";
    private static final String LOG_TAG = VideoChatActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM = 124;

    private FrameLayout mPublisherViewController;
    private FrameLayout mSubscriberViewController;

    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;

    private ImageView closeVideoChatBtn;
    private DatabaseReference usersRef;
    private String userID = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_chat);

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        closeVideoChatBtn = findViewById(R.id.close_video_chat_btn);
        closeVideoChatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                usersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child(userID).hasChild("Ringing"))
                        {
                            usersRef.child(userID).child("Ringing").removeValue();

                            if(mPublisher != null)
                            {
                                mPublisher.destroy();
                            }

                            if(mSubscriber != null)
                            {
                                mSubscriber.destroy();
                            }

                            startActivity(new Intent(VideoChatActivity.this, RegistrationActivity.class));
                            finish();
                        }
                        if(dataSnapshot.child(userID).hasChild("Calling"))
                        {
                            usersRef.child(userID).child("Calling").removeValue();
                            if(mPublisher != null)
                            {
                                mPublisher.destroy();
                            }

                            if(mSubscriber != null)
                            {
                                mSubscriber.destroy();
                            }

                            startActivity(new Intent(VideoChatActivity.this, RegistrationActivity.class));
                            finish();
                        }
                        else
                        {
                            if(mPublisher != null)
                            {
                                mPublisher.destroy();
                            }

                            if(mSubscriber != null)
                            {
                                mSubscriber.destroy();
                            }
                            startActivity(new Intent(VideoChatActivity.this, RegistrationActivity.class));
                            finish();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });

        requestPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults, VideoChatActivity.this);
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions()
    {
        String[] perms ={Manifest.permission.INTERNET,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
        if(EasyPermissions.hasPermissions(this,perms))
        {
            mPublisherViewController = findViewById(R.id.publisher_container);
            mSubscriberViewController = findViewById(R.id.subscriber_container);

            //initialise and connect to the session
            mSession = new Session.Builder(this,API_KEY,SESSION_ID).build();
            mSession.setSessionListener((Session.SessionListener) VideoChatActivity.this);
            mSession.connect(TOKEN);

        }
        else
        {
            EasyPermissions.requestPermissions(this,"This App need the Mic and Camera Permission!!",RC_VIDEO_APP_PERM,perms);
        }
    }

    @Override
    public void onStreamCreated(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onStreamDestroyed(PublisherKit publisherKit, Stream stream) {

    }

    @Override
    public void onError(PublisherKit publisherKit, OpentokError opentokError) {

    }

    //2.Publishing a stream to the session
    @Override
    public void onConnected(Session session) {
        Log.i(LOG_TAG,"Session Completed");

        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(VideoChatActivity.this);

        mPublisherViewController.addView(mPublisher.getView());

        if(mPublisher.getView() instanceof GLSurfaceView)
        {
            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
        }

        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOG_TAG,"Stream Disconnected");
    }

    //3. Subscribing to the streams
    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(LOG_TAG,"Stream Received");

        if(mSubscriber == null)
        {
            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriberViewController.addView(mSubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG,"Stream Dropped");

        if(mSubscriber!=null)
        {
            mSubscriber =null;
            mSubscriberViewController.removeAllViews();
        }
    }

    @Override
    public void onError(Session session, OpentokError opentokError) {
        Log.i(LOG_TAG,"Stream Error");
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
    }
}
