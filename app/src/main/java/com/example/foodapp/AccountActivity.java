package com.example.foodapp;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;


import com.example.foodapp.databinding.ActivityAccountBinding;
import com.example.foodapp.databinding.ActivityOrderingBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firestore.v1.WriteResult;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Model.Order;
import Model.User;

public class AccountActivity extends AppCompatActivity {
    ActivityAccountBinding binding;

    FirebaseAuth auth;
    FirebaseAuth.AuthStateListener authStateListener;
    FirebaseUser currentUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseStorage storage = FirebaseStorage.getInstance();

    ActivityResultLauncher<String> takePhoto;

    private StorageReference storageReference;
    private Uri imageUri;
    private CollectionReference collectionReference = db.collection("Users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        storageReference = storage.getReference();
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        binding = DataBindingUtil.setContentView(this, R.layout.activity_account);

        // binding.pbAccount.setVisibility(View.VISIBLE);
        takePhoto = registerForActivityResult(new ActivityResultContracts.GetContent(), new ActivityResultCallback<Uri>() {
            @Override
            public void onActivityResult(Uri result) {
                binding.ivAccountImageAccountActivity.setImageURI(result);
                imageUri = result;
                saveIMG();
            }
        });

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
                                Picasso.get()
                                        .load(Uri.parse(user.getImageURL()))
                                        .placeholder(R.drawable.ic_accountimage)
                                        .fit()
                                        .into(binding.ivAccountImageAccountActivity);
                            }

                        }
                    }
                }).addOnFailureListener(e -> Toast.makeText(AccountActivity.this, "Something went wrong", Toast.LENGTH_SHORT).show());

        binding.ivOrderFood.setOnClickListener(view -> startActivity(new Intent(AccountActivity.this, OrderingActivity.class)));
        binding.ivListOfOrders.setOnClickListener(view -> startActivity(new Intent(AccountActivity.this, FoodListActivity.class)));
        binding.ivAccount.setOnClickListener(view -> startActivity(new Intent(AccountActivity.this, AccountActivity.class)));
        binding.ivSettings.setOnClickListener(view -> startActivity(new Intent(AccountActivity.this, SettingsActivity.class)));
        binding.ivAccountImageAccountActivity.setOnClickListener(view -> {
            takePhoto.launch("image/*");
        });
    }

    private void saveIMG(){
        binding.pbAccount.setVisibility(View.VISIBLE);
        if(imageUri != null)
        {
            //Ścieżka gdzie ma być w Cloud Storage zapisywany obrazek
            StorageReference filepath = storageReference.child("foodapp_images")
                    .child("myImage" + Timestamp.now().getSeconds());

            //Wrzucenie obrazka
            filepath.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> filepath.getDownloadUrl().addOnSuccessListener(uri -> {
                        String imageURl = uri.toString();


                        //// Wyłuskiwanie dokumentu korzystając z query
                        Query query = collectionReference.whereEqualTo("userId", auth.getUid());
                        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        //Uaktualnianie URL użytkownika
                                        db.collection("Users")
                                                .document(document.getId())
                                                .update("imageURL", imageURl);
                                    }
                                }
                            }
                        });


                    }).addOnFailureListener(e -> Toast.makeText(AccountActivity.this,"Loading image into database went wrong",Toast.LENGTH_SHORT).show())).addOnFailureListener(e -> Toast.makeText(AccountActivity.this,"Uri is empty",Toast.LENGTH_SHORT).show());
        }
        binding.pbAccount.setVisibility(View.INVISIBLE);
    }

//    public void getDoc(){
//        CollectionReference usersRef = db.collection("Users");
//        usersRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isSuccessful()) {
//                    for (QueryDocumentSnapshot document : task.getResult()) {
//                        String email = document.getString("email");
//                        String fname = document.getString("fName");
//                        String uid = document.getId();
//                        DocumentReference uidRef = db.collection("ticket_information").document(uid);
//                        uidRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
//                            @Override
//                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
//                                if (task.isSuccessful()) {
//                                    DocumentSnapshot document = task.getResult();
//                                    if (document.exists()) {
//                                        String icPassport = document.getString("ic_passport");
//                                        String phone = document.getString("phone");
//                                        String ticketDate = document.getString("ticket_date");
//
//                                        Log.d(TAG, email + "/" + fname + "/" + icPassport + "/" + phone + "/" + ticketDate);
//                                    } else {
//                                        Log.d(TAG, "No such document");
//                                    }
//                                } else {
//                                    Log.d(TAG, "get failed with ", task.getException());
//                                }
//                            }
//                        });
//                    }
//                } else {
//                    Log.d(TAG, "Error getting documents: ", task.getException());
//                }
//            }
//        });
//    }


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