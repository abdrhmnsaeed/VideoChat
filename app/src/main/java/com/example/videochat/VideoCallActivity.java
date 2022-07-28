package com.example.videochat;

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

public class VideoCallActivity extends AppCompatActivity implements com.opentok.android.Session.SessionListener, PublisherKit.PublisherListener {
    private static String API_Key = "46714772";
    private static String SESSION_ID = "1_MX40NjcxNDc3Mn5-MTU4ODQ0MDM5NjcxMn5QTFJObm1NVmw2L2EyZnU1Vk5yZlBtaFF-fg";
    private static String TOKEN = "T1==cGFydG5lcl9pZD00NjcxNDc3MiZzaWc9NzdjNTk5NjlmOTNiYTdlZWQzNGY4NDc1MWIxZTkzNzFmOTg1MjU1YTpzZXNzaW9uX2lkPTFfTVg0ME5qY3hORGMzTW41LU1UVTRPRFEwTURNNU5qY3hNbjVRVEZKT2JtMU5WbXcyTDJFeVpuVTFWazV5WmxCdGFGRi1mZyZjcmVhdGVfdGltZT0xNTg4NDQwNDg3Jm5vbmNlPTAuOTIxODgyOTIxNzQxNTk3NCZyb2xlPXB1Ymxpc2hlciZleHBpcmVfdGltZT0xNTkxMDMyNDg1JmluaXRpYWxfbGF5b3V0X2NsYXNzX2xpc3Q9";
    private static final String LOG_TAG = VideoCallActivity.class.getSimpleName();
    private static final int RC_VIDEO_APP_PERM = 124;

    private FrameLayout mPublisherViewController;
    private FrameLayout mSubscriberViewController;
    private Session mSession;
    private Publisher mPublisher;
    private Subscriber mSubscriber;

    private ImageView closeVideoCallBtn;
    private DatabaseReference usersRef;
    private String userID = "";


    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);

        userID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        closeVideoCallBtn = findViewById(R.id.close_video_call_btn);
        closeVideoCallBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                usersRef.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.child(userID).hasChild("Ringing")){
                            usersRef.child(userID).child("Ringing").removeValue();

                            if(mPublisher != null){
                                mPublisher.destroy();
                            }

                            if(mSubscriber != null){
                                mSubscriber.destroy();
                            }

                            startActivity(new Intent(VideoCallActivity.this, RegistrationActivity.class));
                            finish();
                        }
                        if(dataSnapshot.child(userID).hasChild("Calling")){
                            usersRef.child(userID).child("Calling").removeValue();

                            if(mPublisher != null){
                                mPublisher.destroy();
                            }

                            if(mSubscriber != null){
                                mSubscriber.destroy();
                            }

                            startActivity(new Intent(VideoCallActivity.this, RegistrationActivity.class));
                            finish();
                        }else{
                            if(mPublisher != null){
                                mPublisher.destroy();
                            }

                            if(mSubscriber != null){
                                mSubscriber.destroy();
                            }

                            startActivity(new Intent(VideoCallActivity.this, RegistrationActivity.class));
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

        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, VideoCallActivity.this);
    }

    @AfterPermissionGranted(RC_VIDEO_APP_PERM)
    private void requestPermissions(){
        String[] perms = {Manifest.permission.INTERNET, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};

        if(EasyPermissions.hasPermissions(this, perms)){
            mPublisherViewController = findViewById(R.id.publisher_container);
            mSubscriberViewController = findViewById(R.id.subscriber_container);

//          1  initialize nd connect to the session
            mSession = new Session.Builder(this, API_Key, SESSION_ID).build();
            mSession.setSessionListener(VideoCallActivity.this);
            mSession.connect(TOKEN);
        }else{
            EasyPermissions.requestPermissions(this, "Hey, This App Needs The Mic And Camera, Please Allow.", RC_VIDEO_APP_PERM, perms);
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

//    2   publishing stream to the session
    @Override
    public void onConnected(Session session) {
        Log.i(LOG_TAG, "Session Connected");

        mPublisher = new Publisher.Builder(this).build();
        mPublisher.setPublisherListener(VideoCallActivity.this);

        mPublisherViewController.addView(mPublisher.getView());

        if(mPublisher.getView() instanceof GLSurfaceView){
            ((GLSurfaceView) mPublisher.getView()).setZOrderOnTop(true);
        }
        mSession.publish(mPublisher);
    }

    @Override
    public void onDisconnected(Session session) {
        Log.i(LOG_TAG,"Stream Disconnected");
    }

//    3   Subscribing to the stream
    @Override
    public void onStreamReceived(Session session, Stream stream) {
        Log.i(LOG_TAG,"Stream Recieved");

        if(mSubscriber == null){
            mSubscriber = new Subscriber.Builder(this, stream).build();
            mSession.subscribe(mSubscriber);
            mSubscriberViewController.addView(mSubscriber.getView());
        }
    }

    @Override
    public void onStreamDropped(Session session, Stream stream) {
        Log.i(LOG_TAG,"Stream Dropped");

        if(mSubscriber != null){
            mSubscriber = null;
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
