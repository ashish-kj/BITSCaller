package com.d.bitscaller;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

public class FindPeopleActivity extends AppCompatActivity
{
    private RecyclerView findFriendList;
    private EditText searchET;
    private String str = "";
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_people);

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");

        searchET = findViewById(R.id.search_user_text);
        findFriendList = findViewById(R.id.find_friends_list);
        findFriendList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));

        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {
                if(searchET.getText().toString().equals(""))
                {
                    Toast.makeText(FindPeopleActivity.this, "Please write a valid name to search", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    str = charSequence.toString();
                    onStart();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = null;

        if(str.equals(""))
        {
            options = new FirebaseRecyclerOptions.Builder<Contacts>()
                    .setQuery(userRef, Contacts.class)
                    .build();
        }
        else
        {
            options = new FirebaseRecyclerOptions.Builder<Contacts>()
                    .setQuery(userRef
                            .orderByChild("name")
                            .startAt(str)
                            .endAt(str + "\uf8ff"), Contacts.class)
                    .build();
        }

        FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, final int position, @NonNull final Contacts model) {
                holder.userNameTxt.setText(model.getName());
                Picasso.get().load(model.getImage()).into(holder.profileImageView);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        String visit_user_Id = getRef(position).getKey();

                        Intent intent = new Intent(FindPeopleActivity.this,ProfileActivity.class);
                        intent.putExtra("visit_user_id",visit_user_Id );
                        intent.putExtra("profile_image",model.getImage() );
                        intent.putExtra("profile_name",model.getName() );
                        startActivity(intent);
                    }
                });
            }

            @NonNull
            @Override
            public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.contact_design,parent,false);
                FindFriendsViewHolder viewHolder = new FindFriendsViewHolder(view);
                return viewHolder;
            }
        };
        findFriendList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();

    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder
    {
        TextView userNameTxt;
        Button videoCallBtn;
        ImageView profileImageView;
        RelativeLayout cardView;

        public FindFriendsViewHolder(@NonNull View itemView) {
            super(itemView);

            userNameTxt = itemView.findViewById(R.id.name_contact);
            videoCallBtn = itemView.findViewById(R.id.call_btn);
            profileImageView = itemView.findViewById(R.id.image_contact);
            cardView = itemView.findViewById(R.id.card_view1);

            videoCallBtn.setVisibility(View.GONE);

        }
    }



}
