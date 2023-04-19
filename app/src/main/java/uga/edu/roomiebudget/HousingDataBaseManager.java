package uga.edu.roomiebudget;

import static android.content.ContentValues.TAG;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HousingDataBaseManager {
    private FirebaseDatabase fdb = FirebaseDatabase.getInstance("https://roomiebudget-default-rtdb.firebaseio.com");
    private DatabaseReference fRef;
    private DatabaseReference uRef;

    private String email;
    private boolean exists = false;

    private final static String DATABASE_ENTRY = "/dorms";
    private final static String DATABASE_USERS = "/users";

    private Context context;

    private FirebaseAuth fAuth;

    private Intent intent;


    public  HousingDataBaseManager(Context parent) {
        context = parent;
        fAuth = FirebaseAuth.getInstance();
    }

    public void makeGroup(String group) {
        fRef = fdb.getReference(DATABASE_ENTRY);
        fRef.child(group).setValue("");
        fRef = fdb.getReference(DATABASE_ENTRY + "/" + group);
        fRef.child("Item_list").setValue("");
        fRef.child("Recently_purchased").setValue("");

    }

    public void addUser(String email, String user, String fullName, String password) {
        uRef = fdb.getReference(DATABASE_USERS);
        uRef.child(user).setValue("");
        uRef = fdb.getReference(DATABASE_USERS + "/" + user);
        uRef.child("email").setValue(email);
        uRef.child("full_name").setValue(fullName);
        uRef.child("password").setValue(password);
    }

    public void addMember(String group, String username) {
        fRef = fdb.getReference(DATABASE_ENTRY + "/" + group);
        fRef.child(username).setValue("");
    }

    public void addGroupToUser(String user, String group) {
        uRef = fdb.getReference(DATABASE_USERS + "/" + user);
        uRef.child("dorm").setValue(group);
    }


    public void signinUser(String email, String password) {
            fAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        intent = new Intent(context, MainActivity.class);
                        context.startActivity(intent);
                    } else {

                    }

                }
            });
    }



    public void addUserToGroup(String user, String password, String group) {
        uRef = fdb.getReference(DATABASE_USERS + "/" + group);
            uRef.child(group).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
    }


    public void createGroup(String email, String password, String group) {
        uRef = fdb.getReference(DATABASE_ENTRY);
        fAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    uRef.child(group).setValue("").addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isCanceled()) {
                            } else {
                                Toast.makeText(context, "Group already exists", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(context, "Invalid Login", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    public void createUser(String email, String username, String name, String password) {
        fAuth.createUserWithEmailAndPassword(email,password)
                .addOnCompleteListener( new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            addUser(email,username,name,password);
                            Log.d(TAG, "USER with email: " + email + "and password: " + password + "created.");
                            intent = new Intent(context, MainActivity.class);
                            context.startActivity(intent);
                        } else {
                            Log.e(TAG, "USER creation fail.");
                            Toast.makeText(context, task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }


}
