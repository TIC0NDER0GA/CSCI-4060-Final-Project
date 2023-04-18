package uga.edu.roomiebudget;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private Button loginB;

    private Button signupB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loginB = findViewById(R.id.loginButton);
        signupB = findViewById(R.id.signupButton);
        loginB.setOnClickListener(new ButtonClickListener());
        signupB.setOnClickListener(new ButtonClickListener());
    }

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