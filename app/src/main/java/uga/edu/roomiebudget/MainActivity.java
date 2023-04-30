package uga.edu.roomiebudget;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

/**
 * The main activity class to begin the Roomie Budget Application
 */
public class MainActivity extends AppCompatActivity {

    private Button loginB;
    private Button signupB;

    /**
     * Creates the main activity and sets the view for the login page
     * @param savedInstanceState If the fragment is being re-created from a previous saved state,
     *                           this is the state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginB = findViewById(R.id.loginButton);
        signupB = findViewById(R.id.signupButton);
        loginB.setOnClickListener(new ButtonClickListener());
        signupB.setOnClickListener(new ButtonClickListener());
    }

    /**
     * Class which acts as a listener for both the login and signup buttons.
     * It then brings the user to the correct page based on which button was clicked.
     */
    private class ButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            Intent intent = new Intent();
            switch (view.getId()) {
                case R.id.loginButton:
                    intent = new Intent(view.getContext(), LoginActivity.class);
                    break;
                case R.id.signupButton:
                    intent = new Intent(view.getContext(), RegisterActivity.class);
                    break;
            }
            startActivity(intent);
        }
    }
}