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

/**
 * Activity to login after the user has clicked the login button.
 */
public class LoginActivity extends AppCompatActivity {

    private Button continueButton;
    private EditText usernameET;
    private EditText passwordET;
    private Intent intent;

    HousingDataBaseManager hdb;

    /**
     * Creates the login activity and checks that the users information is correct.
     * @param savedInstanceState If the fragment is being re-created from
     *      a previous saved state, this is the state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        continueButton = findViewById(R.id.button);
        usernameET = findViewById(R.id.userLogin);
        passwordET = findViewById(R.id.passwordLogin);
        hdb = new HousingDataBaseManager(this);


        try {
            if (hdb.getCred() != null && hdb.getCred()[0] != null) {
                hdb.signinUser(hdb.getCred()[0], hdb.getCred()[1]);
            } else {

            }

        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        continueButton.setOnClickListener(new ButtonClickListener());
    }

    /**
     * Class which acts as a listener for the continue button to allow the user to login.
     */
    private class ButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            String username = usernameET.getText().toString();
            String password = passwordET.getText().toString();
            hdb.signinUser(username,password);
        }
    }


}