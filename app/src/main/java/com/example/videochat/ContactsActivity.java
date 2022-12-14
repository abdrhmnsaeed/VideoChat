package com.example.videochat;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ContactsActivity extends AppCompatActivity {

    BottomNavigationView navView;
    RecyclerView myContactsList;
    ImageView findPeopleBtn;

    private DatabaseReference contactsRef, userRef;
    private FirebaseAuth mAuth;
    private String currentUserId;
    private String userName = "", profileImage = "", calledBy = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts");
        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        findPeopleBtn = findViewById(R.id.find_people_btn);
        myContactsList = findViewById(R.id.contacts_list);
        myContactsList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));


        findPeopleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent findPeopleIntent = new Intent(ContactsActivity.this, FindPeopleActivity.class);
                startActivity(findPeopleIntent);
            }
        });
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            switch (menuItem.getItemId()) {
                case R.id.navigation_home:
                    Intent mainIntent = new Intent(ContactsActivity.this, ContactsActivity.class);
                    startActivity(mainIntent);
                    break;

                case R.id.navigation_settings:
                    Intent settingsIntent = new Intent(ContactsActivity.this, SettingsActivity.class);
                    startActivity(settingsIntent);
                    break;

                case R.id.navigation_notifications:
                    Intent notificationsIntent = new Intent(ContactsActivity.this, NotificationsActivity.class);
                    startActivity(notificationsIntent);
                    break;

                case R.id.navigation_logout:
                    FirebaseAuth.getInstance().signOut();
                    Intent logoutIntent = new Intent(ContactsActivity.this, RegistrationActivity.class);
                    startActivity(logoutIntent);
                    finish();
                    break;
            }

            return true;
        }
    };

    @Override
    protected void onStart() {
        super.onStart();

        checkForRecievingCall();

        validateUser();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(contactsRef.child(currentUserId), Contacts.class).build();
        FirebaseRecyclerAdapter<Contacts, ContactsVeiwHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Contacts, ContactsVeiwHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final ContactsVeiwHolder holder, int i, @NonNull Contacts contacts) {
                final String listUserId = getRef(i).getKey();

                userRef.child(listUserId).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            userName = dataSnapshot.child("name").getValue().toString();
                            profileImage = dataSnapshot.child("image").getValue().toString();

                            holder.userNameText.setText(userName);
                            Picasso.get().load(profileImage).into(holder.profileImageView);
                        }
                        holder.callBtn.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent callingIntent = new Intent(ContactsActivity.this,CallingActivity.class);
                                callingIntent.putExtra("visit_user_id", listUserId);
                                startActivity(callingIntent);
                            }
                        });
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

            @NonNull
            @Override
            public ContactsVeiwHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_design, parent, false);
                ContactsVeiwHolder viewHolder = new ContactsVeiwHolder(view);
                return viewHolder;
            }
        };
        myContactsList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class ContactsVeiwHolder extends RecyclerView.ViewHolder {

        TextView userNameText;
        Button callBtn;
        ImageView profileImageView;

        public ContactsVeiwHolder(@NonNull View itemView) {
            super(itemView);

            userNameText = itemView.findViewById(R.id.name_contact);
            callBtn = itemView.findViewById(R.id.call_btn);
            profileImageView = itemView.findViewById(R.id.image_contact);
        }

    }
    private void validateUser(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        reference.child("Users").child(currentUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(!dataSnapshot.exists()){
                    Intent settingIntent = new Intent(ContactsActivity.this, SettingsActivity.class);
                    startActivity(settingIntent);
                    finish();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void checkForRecievingCall() {
        userRef.child(currentUserId).child("Ringing").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild("ringing")){
                    calledBy = dataSnapshot.child("ringing").getValue().toString();

                    Intent callingIntent = new Intent(ContactsActivity.this,CallingActivity.class);
                    callingIntent.putExtra("visit_user_id", calledBy);
                    startActivity(callingIntent);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
