package com.example.weighttrackingappproject_isaac_garcia;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
{
    // Key used to send the logged-in user's database ID to another activity.
    public static final String EXTRA_USER_ID =
            "com.example.weighttrackingappproject_isaac_garcia.USER_ID";

    // Validation constants make the requirements easy to update.
    private static final int MIN_USERNAME_LENGTH = 4;
    private static final int MIN_PASSWORD_LENGTH = 8;

    private Button btnLogin;
    private Button btnCreateAccount;
    private EditText etUsername;
    private EditText etPassword;

    private AppDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        connectUserInterface();
        dbHelper = new AppDatabaseHelper(this);
        configureButtonListeners();
    }

    /**
     * Connects Java variables to the controls in activity_main.xml.
     */
    private void connectUserInterface()
    {
        btnLogin = findViewById(R.id.btnLogin);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
    }

    /**
     * Assigns actions to the login and account-creation buttons.
     */
    private void configureButtonListeners()
    {
        btnLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                handleLogin();
            }
        });

        btnCreateAccount.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                handleAccountCreation();
            }
        });
    }

    /**
     * Validates the user's input and retrieves the user's database ID.
     */
    private void handleLogin()
    {
        clearInputErrors();

        String username = getUsername();
        String password = getPassword();

        if (!validateCredentials(username, password))
        {
            return;
        }

        /*
         * The database returns the user's primary key after verifying
         * the username and password. A value of -1 means that no matching
         * account was found.
         */
        long userId = dbHelper.getUserId(username, password);

        if (userId != -1)
        {
            Toast.makeText(
                    this,
                    "Login successful",
                    Toast.LENGTH_SHORT
            ).show();

            clearPasswordField();
            goToDataGrid(userId);
        }
        else
        {
            Toast.makeText(
                    this,
                    "Login failed. Check your username and password.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    /**
     * Validates the user's input and attempts to create an account.
     */
    private void handleAccountCreation()
    {
        clearInputErrors();

        String username = getUsername();
        String password = getPassword();

        if (!validateCredentials(username, password))
        {
            return;
        }

        boolean accountCreated = dbHelper.registerUser(username, password);

        if (accountCreated)
        {
            Toast.makeText(
                    this,
                    "Account created. You can now log in.",
                    Toast.LENGTH_SHORT
            ).show();

            clearPasswordField();
        }
        else
        {
            etUsername.setError(
                    "This username may already be in use."
            );
            etUsername.requestFocus();

            Toast.makeText(
                    this,
                    "Account could not be created.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }

    /**
     * Validates the username and password before a database request occurs.
     */
    private boolean validateCredentials(String username, String password)
    {
        boolean valid = true;

        if (username.isEmpty())
        {
            etUsername.setError("Username is required.");
            valid = false;
        }
        else if (username.length() < MIN_USERNAME_LENGTH)
        {
            etUsername.setError(
                    "Username must contain at least "
                            + MIN_USERNAME_LENGTH
                            + " characters."
            );
            valid = false;
        }
        else if (username.contains(" "))
        {
            etUsername.setError(
                    "Username cannot contain spaces."
            );
            valid = false;
        }

        if (password.isEmpty())
        {
            etPassword.setError("Password is required.");

            if (valid)
            {
                etPassword.requestFocus();
            }

            valid = false;
        }
        else if (password.length() < MIN_PASSWORD_LENGTH)
        {
            etPassword.setError(
                    "Password must contain at least "
                            + MIN_PASSWORD_LENGTH
                            + " characters."
            );

            if (valid)
            {
                etPassword.requestFocus();
            }

            valid = false;
        }

        if (!valid && etUsername.getError() != null)
        {
            etUsername.requestFocus();
        }

        return valid;
    }

    /**
     * Returns the username without spaces at the beginning or end.
     */
    private String getUsername()
    {
        return etUsername.getText().toString().trim();
    }

    /**
     * Returns the password exactly as entered.
     */
    private String getPassword()
    {
        return etPassword.getText().toString();
    }

    /**
     * Removes old validation messages before checking the fields again.
     */
    private void clearInputErrors()
    {
        etUsername.setError(null);
        etPassword.setError(null);
    }

    /**
     * Clears the password after a successful account action.
     */
    private void clearPasswordField()
    {
        etPassword.setText("");
    }

    /**
     * Opens the daily weight-entry screen and sends the logged-in
     * user's database ID.
     */
    private void goToDataGrid(long userId)
    {
        Intent intent = new Intent(
                MainActivity.this,
                DataGridActivity.class
        );

        intent.putExtra(EXTRA_USER_ID, userId);
        startActivity(intent);
    }
}