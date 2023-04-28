package uga.edu.roomiebudget;

import static android.app.PendingIntent.getActivity;
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

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {

    private Button continueButton;
    private EditText usernameET;
    private EditText passwordET;
    private Intent intent;

    HousingDataBaseManager hdb;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        continueButton = findViewById(R.id.button);
        usernameET = findViewById(R.id.userLogin);
        passwordET = findViewById(R.id.passwordLogin);
        hdb = new HousingDataBaseManager(this);


        try {
            if (hdb.getCred()[0] != null && hdb.getCred()[1] != null) {
                intent = new Intent(this, PurchasedListActivity.class);
                this.startActivity(intent);
            }

        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        continueButton.setOnClickListener(new ButtonClickListener());
    }

    private class ButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            String username = usernameET.getText().toString();
            String password = passwordET.getText().toString();
            hdb.signinUser(username,password);
        }
    }


}