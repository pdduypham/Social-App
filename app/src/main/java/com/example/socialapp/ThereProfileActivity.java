package com.example.socialapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.MenuItemCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.socialapp.adapters.AdapterPosts;
import com.example.socialapp.models.ModelPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class ThereProfileActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;

    RecyclerView postsRecyclerView;

    List<ModelPost> postList;
    AdapterPosts adapterPosts;
    String uid;

    ImageView ivAvatar, ivCover;
    TextView tvName, tvPhone, tvEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_there_profile);

        postsRecyclerView = findViewById(R.id.reclyclerview_posts);

        firebaseAuth = FirebaseAuth.getInstance();

        tvEmail = findViewById(R.id.tvEmail);
        tvName = findViewById(R.id.tvName);
        tvPhone = findViewById(R.id.tvPhone);
        ivAvatar = findViewById(R.id.ivAvatar);
        ivCover = findViewById(R.id.ivCover);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Trang c?? nh??n");
        actionBar.setDisplayShowHomeEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //Get uid t??? user ???????c click
        Intent intent = getIntent();
        uid = intent.getStringExtra("uid");

        Query query = FirebaseDatabase.getInstance().getReference("Users")
                .orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    String name = "" + ds.child("name").getValue();
                    String email = "" + ds.child("email").getValue();
                    String phone = "" + ds.child("phone").getValue();
                    String image = "" + ds.child("image").getValue();
                    String cover = "" + ds.child("cover").getValue();

                    tvName.setText(name);
                    tvEmail.setText(email);
                    tvPhone.setText(phone);

                    try {
                        Picasso.get().load(image).into(ivAvatar);
                    } catch (Exception e) {
                        Picasso.get().load(R.drawable.ic_default_img_white).into(ivAvatar);
                    }
                    try {
                        Picasso.get().load(cover).into(ivCover);
                    } catch (Exception e) {
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        postList = new ArrayList<>();

        checkUserStatus();

        loadHisPost();
    }

    private void loadHisPost() {
        //Linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(ThereProfileActivity.this);
        //Show post m???i nh???t tr?????c
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        postsRecyclerView.setLayoutManager(layoutManager);

        //Kh???i t???o post list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

        Query query = ref.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPost myPost = ds.getValue(ModelPost.class);

                    postList.add(myPost);
                    adapterPosts = new AdapterPosts(ThereProfileActivity.this, postList);
                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ThereProfileActivity.this, "" + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void searchHisPost(String searchQuery) {
        //Linear layout for recyclerview
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        //Show post m???i nh???t tr?????c
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        postsRecyclerView.setLayoutManager(layoutManager);

        //Kh???i t???o post list
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Posts");

        Query query = ref.orderByChild("uid").equalTo(uid);
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                postList.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ModelPost myPost = ds.getValue(ModelPost.class);

                    if (myPost.getpTitle().toLowerCase().contains(searchQuery.toLowerCase()) ||
                            myPost.getpDescr().toLowerCase().contains(searchQuery.toLowerCase())) {
                        postList.add(myPost);
                    }

                    adapterPosts = new AdapterPosts(ThereProfileActivity.this, postList);
                    postsRecyclerView.setAdapter(adapterPosts);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ThereProfileActivity.this, "" + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void checkUserStatus() {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
        } else {
            //User ch??a ????ng nh???p -> MainActivity
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_Add_Post).setVisible(false);

        MenuItem item = menu.findItem(R.id.action_Search);

        //Search View ????? search post trong profile
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                //???????c g???i khi user ???n Search
                if (!TextUtils.isEmpty(query)) {
                    searchHisPost(query);
                } else {
                    loadHisPost();
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //???????c g???i khi ng?????i d??ng ???n ph??m b???t k?? t??? keyboard
                if (!TextUtils.isEmpty(newText)) {
                    searchHisPost(newText);
                } else {
                    loadHisPost();
                }
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_Logout) {
            firebaseAuth.signOut();
            checkUserStatus();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }
}