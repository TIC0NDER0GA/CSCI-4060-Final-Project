package uga.edu.roomiebudget;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class RegisterActivity extends AppCompatActivity {

    private Button signUpButton;
    private EditText nameET;
    private EditText newUsernameET;
    private EditText newPasswordET;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        signUpButton = findViewById(R.id.button2);
        nameET = findViewById(R.id.editText3);
        newUsernameET = findViewById(R.id.editText4);
        newPasswordET = findViewById(R.id.editText5);

        signUpButton.setOnClickListener(new ButtonClickListener());
    }

    private class ButtonClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
//            Intent intent = new Intent(view.getContext(), ChildActivity.class);

            String name = nameET.getText().toString();
            String username = newUsernameET.getText().toString();
            String password = newPasswordET.getText().toString();

//            intent.putExtra(MESSAGE_TYPE, message);
//            startActivity(intent);
        }
    }
}