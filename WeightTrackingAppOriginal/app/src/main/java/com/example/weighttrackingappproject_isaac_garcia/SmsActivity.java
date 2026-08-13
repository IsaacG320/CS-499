package com.example.weighttrackingappproject_isaac_garcia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SmsActivity extends AppCompatActivity
{

    private static final int SMS_PERMISSION_CODE = 100;

    Button btnCheckPermission;
    TextView tvPermissionStatus;

    public static void start(Context context)
    {
        Intent intent = new Intent(context, SmsActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms);

        btnCheckPermission = findViewById(R.id.btnCheckPermission);
        tvPermissionStatus = findViewById(R.id.tvPermissionStatus);

        btnCheckPermission.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                checkSmsPermission();
            }
        });
    }

    private void checkSmsPermission()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED)
        {
            tvPermissionStatus.setText("SMS permission granted. Sending a test goal notification.");
            sendTestSms();
        }
        else
        {
            tvPermissionStatus.setText("SMS permission not granted. Requesting permission from user.");
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.SEND_SMS},
                    SMS_PERMISSION_CODE
            );
        }
    }

    private void sendTestSms()
    {
        try
        {
            // For emulator, a placeholder number is fine
            String phoneNumber = "5554";
            String message = "Goal weight notification: You have reached your target weight!";

            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);

            tvPermissionStatus.setText("SMS permission granted. Test goal notification sent.");
        }
        catch (Exception e)
        {
            tvPermissionStatus.setText("SMS permission granted, but SMS could not be sent in this environment.");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == SMS_PERMISSION_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                tvPermissionStatus.setText("Permission granted. Sending a test goal notification.");
                sendTestSms();
            }
            else
            {
                tvPermissionStatus.setText("Permission denied. The app will not send SMS notifications, but all other features will still work.");
            }
        }
    }
}
