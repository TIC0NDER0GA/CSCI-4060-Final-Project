package uga.edu.roomiebudget;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LoginActivity extends AppCompatActivity {

    private Button continueButton;
    private EditText usernameET;
    private EditText passwordET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        continueButton = findViewById(R.id.button);
        usernameET = findViewById(R.id.editText1);
        passwordET = findViewById(R.id.editText2);

        continueButton.setOnClickListener(new ButtonClickListener());

    }

    private class ButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
//            Intent intent = new Intent(view.getContext(), ChildActivity.class);

            String username = usernameET.getText().toString();
            String password = passwordET.getText().toString();

//            intent.putExtra(MESSAGE_TYPE, message);
//            startActivity(intent);
        }
    }
}