package com.example.foodapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.foodapp.databinding.ActivityAccountBinding;
import com.example.foodapp.databinding.ActivityOrderingBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import Model.Order;
import Model.User;

public class AccountActivity extends AppCompatActivity {
    ActivityAccountBinding binding;

    FirebaseAuth auth;
    FirebaseAuth.AuthStateListener authStateListener;
    FirebaseUser currentUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference collectionReference = db.collection("Users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);


        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_account);

        collectionReference.whereEqualTo("userId", FoodApi.getInstance()
                        .getUserId())
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        if(!queryDocumentSnapshots.isEmpty()){
                            for (QueryDocumentSnapshot users : queryDocumentSnapshots){
                                User user = users.toObject(User.class);
                                binding.tvEmailKontaText.setText(auth.getCurrentUser().getEmail());
                                binding.tvNazwaKontaText.setText(user.getUsername());
                                binding.tvSrodkiKontaText.setText(user.getMoney());
                                binding.tvHasloKontaText.setText(user.getPassword());
                            }

                        }else {
                        }
                    }
                }).addOnFailureListener(e -> Toast.makeText(AccountActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show());

        binding.ivOrderFood.setOnClickListener(view -> startActivity(new Intent(AccountActivity.this, OrderingActivity.class)));
        binding.ivListOfOrders.setOnClickListener(view -> startActivity(new Intent(AccountActivity.this, FoodListActivity.class)));
        binding.ivAccount.setOnClickListener(view -> startActivity(new Intent(AccountActivity.this, AccountActivity.class)));
        binding.ivSettings.setOnClickListener(view -> startActivity(new Intent(AccountActivity.this, SettingsActivity.class)));


    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_speak:
                if( currentUser != null && auth != null){
//                    startActivity(new Intent(com.example.foodapp.FoodListActivity.this, PostJournalActivity.class));
                }
                break;
            case R.id.action_signout:
                if( currentUser != null && auth != null){
                    auth.signOut();
                }
                startActivity(new Intent(this, MainActivity.class));
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}