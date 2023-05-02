package uga.edu.roomiebudget;

import static android.content.ContentValues.TAG;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

/**
 * Activity to register for a new account after the user has clicked the login button.
 * The user can either register with an existing group or create a new group.
 */
public class RegisterActivity extends AppCompatActivity {
    private Button signUpButton;
    private Button signUpButton2;
    private EditText nameET;
    private EditText newGroupNameET;
    private EditText newPasswordET;
    private EditText newRE_ENTER;
    private EditText newEMAIL;
    private Context context;
    private FirebaseAuth fAuth = FirebaseAuth.getInstance();
    private HousingDataBaseManager hdb;

    /**
     * Creates the register activity.
     * @param savedInstanceState If the fragment is being re-created from
     *      a previous saved state, this is the state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        context = this;
        hdb = new HousingDataBaseManager(this);
        newEMAIL = findViewById(R.id.editTextTextPersonName);
        signUpButton = findViewById(R.id.button2);
        signUpButton2 = findViewById(R.id.button4);
        nameET = findViewById(R.id.editText3);
        newGroupNameET = findViewById(R.id.groupName);
        newPasswordET = findViewById(R.id.editText5);
        newRE_ENTER = findViewById(R.id.editTextTextPassword);
        signUpButton.setOnClickListener(new ButtonClickListener());
        signUpButton2.setOnClickListener(new ButtonClickListener());
    }

    /**
     * Acts as a listener for the Create User Without Group and Create User with Existing
     * Group buttons. Displays necessary error messages.
     */
    private class ButtonClickListener implements View.OnClickListener {

        /**
         * On click method for registration buttons. Sends user back to home page after they have
         * correctly registered.
         */
        private Intent intent;
        @Override
        public void onClick(View view) {
            intent = new Intent(view.getContext(), LoginActivity.class);
            String email = newEMAIL == null ? "" : newEMAIL.getText().toString();
            String name = nameET == null ? "" : nameET.getText().toString();
            String groupName = newGroupNameET == null ? "" : newGroupNameET.getText().toString();
            String password = newPasswordET == null ? "" : newPasswordET.getText().toString();
            String re_enter = newRE_ENTER == null ? "" : newRE_ENTER.getText().toString();

            if (email.isEmpty() && name.isEmpty() && groupName.isEmpty() && password.isEmpty() && re_enter.isEmpty()) {
                Toast.makeText(context, "Please fill out all fields.", Toast.LENGTH_SHORT).show();
            } else if (re_enter.equals(password)) {
                switch (view.getId()) {
                    case R.id.button2:
                        hdb.createUserWithoutGroup(email, groupName, name, password);
                        break;
                    case R.id.button4:
                        Log.e(TAG, "MAKING USER WITH GROUP");
                        hdb.createUserWithGroup(email, groupName, name, password);
                        break;
                }
            } else {
                Toast.makeText(context, "Passwords do not match.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}