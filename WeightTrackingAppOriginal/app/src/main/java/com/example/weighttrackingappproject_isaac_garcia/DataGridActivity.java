package com.example.weighttrackingappproject_isaac_garcia;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.ArrayList;

public class DataGridActivity extends AppCompatActivity
{

    RecyclerView rvWeights;
    EditText etDate;
    EditText etWeight;
    Button btnAddEntry;
    Button btnSmsScreen;

    ArrayList<WeightEntry> weightEntries;
    WeightAdapter adapter;

    AppDatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_grid);

        // Connect UI
        rvWeights = findViewById(R.id.rvWeights);
        etDate = findViewById(R.id.etDate);
        etWeight = findViewById(R.id.etWeight);
        btnAddEntry = findViewById(R.id.btnAddEntry);
        btnSmsScreen = findViewById(R.id.btnSmsScreen);

        dbHelper = new AppDatabaseHelper(this);

        // Load data from database
        weightEntries = new ArrayList<>();
        loadWeightsFromDatabase();

        adapter = new WeightAdapter(weightEntries, dbHelper);
        rvWeights.setLayoutManager(new LinearLayoutManager(this));
        rvWeights.setAdapter(adapter);

        // Add new weight
        btnAddEntry.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String date = etDate.getText().toString().trim();
                String weight = etWeight.getText().toString().trim();

                if (TextUtils.isEmpty(date) || TextUtils.isEmpty(weight))
                {
                    Toast.makeText(DataGridActivity.this,
                            "Please enter both date and weight",
                            Toast.LENGTH_SHORT).show();
                }
                else
                {
                    long id = dbHelper.insertWeight(date, weight);

                    if (id != -1)
                    {
                        weightEntries.add(0, new WeightEntry(id, date, weight));
                        adapter.notifyItemInserted(0);
                        rvWeights.scrollToPosition(0);

                        etDate.setText("");
                        etWeight.setText("");
                    }
                    else
                    {
                        Toast.makeText(DataGridActivity.this,
                                "Error saving entry",
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        // Open SMS screen
        btnSmsScreen.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                SmsActivity.start(DataGridActivity.this);
            }
        });
    }

    private void loadWeightsFromDatabase()
    {
        Cursor cursor = dbHelper.getAllWeights();
        weightEntries.clear();

        if (cursor.moveToFirst())
        {
            int idIndex = cursor.getColumnIndex(AppDatabaseHelper.COLUMN_WEIGHT_ID);
            int dateIndex = cursor.getColumnIndex(AppDatabaseHelper.COLUMN_WEIGHT_DATE);
            int weightIndex = cursor.getColumnIndex(AppDatabaseHelper.COLUMN_WEIGHT_VALUE);

            do
            {
                long id = cursor.getLong(idIndex);
                String date = cursor.getString(dateIndex);
                String weight = cursor.getString(weightIndex);

                weightEntries.add(new WeightEntry(id, date, weight));
            }
            while (cursor.moveToNext());
        }

        cursor.close();
    }
}
