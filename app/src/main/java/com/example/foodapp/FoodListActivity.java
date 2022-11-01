package com.example.foodapp;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.foodapp.databinding.ActivityFoodListBinding;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import Model.Order;
import UI.OrderRecyclerAdapter;

public class FoodListActivity extends AppCompatActivity {
    ActivityFoodListBinding binding;

    FirebaseAuth auth;
    FirebaseAuth.AuthStateListener authStateListener;
    FirebaseUser currentUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference collectionReference = db.collection("Journal");

    private List<Order> orderList = new ArrayList<>();
    private OrderRecyclerAdapter orderRecyclerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_list);

        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_food_list);


        binding.rvList.setHasFixedSize(true);
        binding.rvList.setLayoutManager(new LinearLayoutManager(this));
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
                startActivity(new Intent(com.example.foodapp.FoodListActivity.this, MainActivity.class));
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        collectionReference.whereEqualTo("userId", FoodApi.getInstance()
                    .getUserId())
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        orderList.clear();
                        if(!queryDocumentSnapshots.isEmpty()){
                            for (QueryDocumentSnapshot journals : queryDocumentSnapshots){
                                Order order = journals.toObject(Order.class);
                                orderList.add(order);
                            }

                            OrderRecyclerAdapter myRecyclerViewAdapter = new OrderRecyclerAdapter(FoodListActivity.this, orderList);
                            binding.setMyAdapter(myRecyclerViewAdapter);
                                orderRecyclerAdapter = new OrderRecyclerAdapter(FoodListActivity.this, orderList);
                                binding.rvList.setAdapter(orderRecyclerAdapter);
                                orderRecyclerAdapter.notifyDataSetChanged();

                        }else {
                            binding.tvListNoEntry.setVisibility(View.VISIBLE);
                        }
                    }).addOnFailureListener(e -> Toast.makeText(com.example.foodapp.FoodListActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show());
    }
}