package com.example.foodapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.example.foodapp.databinding.ActivityCreateAccountBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CreateAccountActivity extends AppCompatActivity {
    FirebaseAuth auth;
    FirebaseAuth.AuthStateListener authStateListener;
    FirebaseUser currentUser;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ActivityCreateAccountBinding binding;
    private CollectionReference collectionReference = db.collection("Users");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_create_account);

        auth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();

                if(currentUser != null){
                    //user logged in
                }else {
                    //no user yet
                }
            }
        };
        //btn login to tak naprawdę do zrobienia konta przycisk, zapomniałem id zmienić :/ a potem się już nie dało
        binding.btnLogin.setOnClickListener(view -> {
         if(!TextUtils.isEmpty(binding.email.getText().toString().trim())
             && !TextUtils.isEmpty(binding.etPassword.getText().toString().trim())
             && !TextUtils.isEmpty(binding.etUsername.getText().toString().trim())){

             String email = binding.email.getText().toString().trim();
             String password = binding.etPassword.getText().toString().trim();
             String username = binding.etUsername.getText().toString().trim();
             createUserEmailAccount(email, password, username);

         }else {
             Toast.makeText(
                     CreateAccountActivity.this,
                     "Empty fields aren't allowed",
                     Toast.LENGTH_SHORT)
                     .show();
         }

        });
    }

    private void createUserEmailAccount(String email, String password, String username){
        if(!TextUtils.isEmpty(email)
        && !TextUtils.isEmpty(password)
        && !TextUtils.isEmpty(username)) {
            binding.loginProgress.setVisibility(View.VISIBLE);

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()){
                            //take user to AddJournalActivity
                            currentUser = auth.getCurrentUser();
                            assert currentUser != null;
                            String currentUserId = currentUser.getUid();

                            //Create a UserMap so we can create a user in the User collection
                            Map<String, String> userObj = new HashMap<>();
                            userObj.put("userId",currentUserId);
                            userObj.put("username",username);

                            //Save to firestore
                            collectionReference.add(userObj)
                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                        @Override
                                        public void onSuccess(DocumentReference documentReference) {
                                            documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if( task.getResult().exists()){
                                                        binding.loginProgress.setVisibility(View.INVISIBLE);
                                                        String name = task.getResult().getString("username");

                                                        FoodApi foodApi = FoodApi.getInstance();
                                                        foodApi.setUserId(currentUserId);
                                                        foodApi.setUsername(name);

//                                                        Intent intent = new Intent(CreateAccountActivity.this, PostJournalActivity.class);
//                                                        intent.putExtra("username", name);
//                                                        intent.putExtra("userId", currentUserId);
//                                                        startActivity(intent);
                                                    }else {

                                                    }
                                                }
                                            });
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {

                                        }
                                    });
                            }
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {

                        }
                    });
        }
    }

    @Override
    protected void onStart() {
//        super.onStart();
//        currentUser = auth.getCurrentUser();
//        auth.addAuthStateListener(authStateListener);
    }
}