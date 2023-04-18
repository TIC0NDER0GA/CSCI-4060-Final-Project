package uga.edu.roomiebudget;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

public class RegisterActivity extends AppCompatActivity {

    private Button signUpButton;
    private EditText nameET;
    private EditText newUsernameET;
    private EditText newPasswordET;

    private EditText newRE_ENTER;
    private EditText newEMAIL;

    private Context context;

    private FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private HousingDataBaseManager hdb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        context = this;
        hdb = new HousingDataBaseManager(this);
        signUpButton = findViewById(R.id.button2);
        nameET = findViewById(R.id.editText3);
        newUsernameET = findViewById(R.id.editText4);
        newPasswordET = findViewById(R.id.editText5);
        newRE_ENTER = findViewById(R.id.editTextTextPassword);
        signUpButton.setOnClickListener(new ButtonClickListener());
    }

    private class ButtonClickListener implements View.OnClickListener {

        private Intent intent;
        @Override
        public void onClick(View view) {
            intent = new Intent(view.getContext(), LoginActivity.class);

            String email = newEMAIL == null ? "" : newEMAIL.getText().toString();
            String name = nameET == null ? "" : nameET.getText().toString();
            String username = newUsernameET == null ? "" : newUsernameET.getText().toString();
            String password = newPasswordET == null ? "" : newPasswordET.getText().toString();
            String re_enter = newRE_ENTER == null ? "" : newRE_ENTER.getText().toString();

            if (email.isEmpty() && name.isEmpty() && username.isEmpty() && password.isEmpty() && re_enter.isEmpty()) {
                Toast.makeText(context, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
            } else if (re_enter.equals(password)) {
               createUser(email, username, name, password);
           } else {
               Toast.makeText(context, "Passwords do not match.", Toast.LENGTH_SHORT).show();
           }

        }

        public void createUser(String email, String username, String name, String password) {
            fAuth.createUserWithEmailAndPassword(email,password)
                    .addOnCompleteListener((Executor) this, (OnCompleteListener<AuthResult>) new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                    hdb.addUser(email,username,name,password);
                                    Log.d(TAG, "USER with email: " + email + "and password: " + password + "created.");
                                    startActivity(intent);
                                hdb.addUser(email,username,name,password);
                                Log.d(TAG, "USER with email: " + email + "and password: " + password + "created.");
                                startActivity(intent);
                            } else {
                                Log.e(TAG, "USER creation fail.");
                                Toast.makeText(context, task.getException().toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }
}