package com.example.weighttrackingappproject_isaac_garcia;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
{

    Button btnLogin;
    Button btnCreateAccount;
    EditText etUsername;
    EditText etPassword;

    AppDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Connect UI
        btnLogin = findViewById(R.id.btnLogin);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);

        // Database helper
        dbHelper = new AppDatabaseHelper(this);

        // Login button: check username/password
        btnLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password))
                {
                    Toast.makeText(MainActivity.this,
                            "Please enter a username and password",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean valid = dbHelper.checkUserLogin(username, password);

                if (valid)
                {
                    Toast.makeText(MainActivity.this,
                            "Login successful",
                            Toast.LENGTH_SHORT).show();
                    goToDataGrid();
                }
                else
                {
                    Toast.makeText(MainActivity.this,
                            "Login failed. Please check your credentials or create an account.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        // Create Account button: register new user
        btnCreateAccount.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String username = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (TextUtils.isEmpty(username) || TextUtils.isEmpty(password))
                {
                    Toast.makeText(MainActivity.this,
                            "Please enter a username and password",
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                boolean success = dbHelper.registerUser(username, password);

                if (success)
                {
                    Toast.makeText(MainActivity.this,
                            "Account created. You can now log in.",
                            Toast.LENGTH_SHORT).show();
                }
                else
                {
                    Toast.makeText(MainActivity.this,
                            "Account could not be created. Username may already exist.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void goToDataGrid()
    {
        Intent intent = new Intent(MainActivity.this, DataGridActivity.class);
        startActivity(intent);
    }
}
