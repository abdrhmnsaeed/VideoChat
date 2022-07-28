package com.example.videochat;

//import androidx.appcompat.app.AlertController;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.PointerIconCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class FindPeopleActivity extends AppCompatActivity {

    private RecyclerView findFriendList;
    private EditText searchET;
    private String str = "";
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_people);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        findFriendList = findViewById(R.id.find_friends_list);
        searchET = findViewById(R.id.search_user_text);
        findFriendList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence charSeq, int start, int before, int count) {
                if(searchET.getText().toString().equals("")){
                    Toast.makeText(FindPeopleActivity.this, "Please enter a name to search", Toast.LENGTH_SHORT).show();
                }else{
                    str = charSeq.toString();
                    onStart();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = null;

        if(str.equals("")){
            options =
                    new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(userRef, Contacts.class).build();
        }else{
            options =
                    new FirebaseRecyclerOptions.Builder<Contacts>().setQuery(userRef.orderByChild("name").startAt(str).endAt(str + "\uf8ff"), Contacts.class).build();
        }
        FirebaseRecyclerAdapter<Contacts, FindFriendsVeiwHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Contacts, FindFriendsVeiwHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsVeiwHolder holder, final int position, @NonNull final Contacts model) {
                holder.userNameText.setText(model.getName());
                Picasso.get().load(model.getImage()).into(holder.profileImageView);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String visit_user_id = getRef(position).getKey();

                        Intent intent = new Intent(FindPeopleActivity.this, ProfileActivity.class);
                        intent.putExtra("visit_user_id", visit_user_id);
                        intent.putExtra("profile_image", model.getImage());
                        intent.putExtra("profile_name", model.getName());
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public FindFriendsVeiwHolder onCreateViewHolder(@NonNull ViewGroup p, int viewType) {
                View view = LayoutInflater.from(p.getContext()).inflate(R.layout.contact_design, p, false);
                FindFriendsVeiwHolder viewHolder = new FindFriendsVeiwHolder(view);
                return viewHolder;
            }
        };
        findFriendList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }

    public static class FindFriendsVeiwHolder extends RecyclerView.ViewHolder
    {

        TextView userNameText;
        Button videoCallBtn;
        ImageView profileImageView;
        RelativeLayout cardView;

        public FindFriendsVeiwHolder(@NonNull View itemView) {
            super(itemView);

            userNameText = itemView.findViewById(R.id.name_contact);
            videoCallBtn = itemView.findViewById(R.id.call_btn);
            profileImageView = itemView.findViewById(R.id.image_contact);
            cardView = itemView.findViewById(R.id.card_view1);

            videoCallBtn.setVisibility(View.GONE);
        }
    }
}
