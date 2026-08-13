package com.example.weighttrackingappproject_isaac_garcia;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

public class DataGridActivity extends AppCompatActivity
        implements WeightAdapter.OnDeleteClickListener
{
    private static final double MIN_WEIGHT = 20.0;
    private static final double MAX_WEIGHT = 1000.0;

    private RecyclerView rvWeights;
    private EditText etDate;
    private EditText etWeight;
    private Button btnAddEntry;
    private Button btnSmsScreen;

    // Statistics
    private TextView tvCurrentWeight;
    private TextView tvHighestWeight;
    private TextView tvLowestWeight;
    private TextView tvAverageWeight;
    private TextView tvWeightChange;

    private ArrayList<WeightEntry> weightEntries;
    private WeightAdapter adapter;
    private AppDatabaseHelper dbHelper;

    // Stores the primary key of the currently logged-in user.
    private long currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_grid);

        /*
         * Retrieves the user ID sent from MainActivity.
         * This value is required for all user-specific database operations.
         */
        currentUserId = getIntent().getLongExtra(
                MainActivity.EXTRA_USER_ID,
                -1
        );

        if (currentUserId == -1)
        {
            Toast.makeText(
                    this,
                    "Unable to identify the logged-in user.",
                    Toast.LENGTH_LONG
            ).show();

            finish();
            return;
        }

        connectUserInterface();
        initializeDatabase();
        initializeWeightList();
        configureDateField();
        configureButtonListeners();
        updateStatistics();
    }

    /**
     * Connects the Java variables to activity_data_grid.xml.
     */
    private void connectUserInterface()
    {
        rvWeights = findViewById(R.id.rvWeights);
        etDate = findViewById(R.id.etDate);
        etWeight = findViewById(R.id.etWeight);
        btnAddEntry = findViewById(R.id.btnAddEntry);
        btnSmsScreen = findViewById(R.id.btnSmsScreen);

        tvCurrentWeight = findViewById(R.id.tvCurrentWeight);
        tvHighestWeight = findViewById(R.id.tvHighestWeight);
        tvLowestWeight = findViewById(R.id.tvLowestWeight);
        tvAverageWeight = findViewById(R.id.tvAverageWeight);
        tvWeightChange = findViewById(R.id.tvWeightChange);
    }

    /**
     * Creates the database helper used by this activity.
     */
    private void initializeDatabase()
    {
        dbHelper = new AppDatabaseHelper(this);
    }

    /**
     * Loads the current user's saved data and configures the RecyclerView.
     */
    private void initializeWeightList()
    {
        weightEntries = new ArrayList<>();

        loadWeightsFromDatabase();
        sortWeightEntries();

        adapter = new WeightAdapter(
                weightEntries,
                this
        );

        rvWeights.setLayoutManager(
                new LinearLayoutManager(this)
        );

        rvWeights.setAdapter(adapter);
    }

    /**
     * Prevents manual date entry and opens a date picker.
     */
    private void configureDateField()
    {
        etDate.setFocusable(false);
        etDate.setClickable(true);

        etDate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                showDatePicker();
            }
        });
    }

    /**
     * Assigns actions to each button.
     */
    private void configureButtonListeners()
    {
        btnAddEntry.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                handleAddEntry();
            }
        });

        btnSmsScreen.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                SmsActivity.start(DataGridActivity.this);
            }
        });
    }

    /**
     * Displays a calendar and enters the selected date
     * in YYYY-MM-DD format.
     */
    private void showDatePicker()
    {
        Calendar calendar = Calendar.getInstance();

        int currentYear = calendar.get(Calendar.YEAR);
        int currentMonth = calendar.get(Calendar.MONTH);
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog =
                new DatePickerDialog(
                        this,
                        (view,
                         selectedYear,
                         selectedMonth,
                         selectedDay) ->
                        {
                            String formattedDate =
                                    String.format(
                                            Locale.US,
                                            "%04d-%02d-%02d",
                                            selectedYear,
                                            selectedMonth + 1,
                                            selectedDay
                                    );

                            etDate.setText(formattedDate);
                            etDate.setError(null);
                        },
                        currentYear,
                        currentMonth,
                        currentDay
                );

        datePickerDialog.show();
    }

    /**
     * Coordinates validation and storage of a new weight entry.
     */
    private void handleAddEntry()
    {
        clearInputErrors();

        String date = getDateInput();
        String weightText = getWeightInput();

        if (!validateEntry(date, weightText))
        {
            return;
        }

        saveWeightEntry(date, weightText);
    }

    /**
     * Checks that the selected date and entered weight are valid.
     */
    private boolean validateEntry(
            String date,
            String weightText)
    {
        boolean valid = true;

        if (date.isEmpty())
        {
            etDate.setError("Please select a date.");
            valid = false;
        }

        if (weightText.isEmpty())
        {
            etWeight.setError("Weight is required.");

            if (valid)
            {
                etWeight.requestFocus();
            }

            valid = false;
        }
        else
        {
            try
            {
                double weight =
                        Double.parseDouble(weightText);

                if (weight < MIN_WEIGHT
                        || weight > MAX_WEIGHT)
                {
                    etWeight.setError(
                            "Enter a weight between "
                                    + MIN_WEIGHT
                                    + " and "
                                    + MAX_WEIGHT
                                    + "."
                    );

                    if (valid)
                    {
                        etWeight.requestFocus();
                    }

                    valid = false;
                }
            }
            catch (NumberFormatException exception)
            {
                etWeight.setError(
                        "Enter a valid numeric weight."
                );

                if (valid)
                {
                    etWeight.requestFocus();
                }

                valid = false;
            }
        }

        return valid;
    }
    /**
     * Saves a validated entry for the logged-in user,
     * sorts the list, and refreshes the statistics.
     */
    private void saveWeightEntry(
            String date,
            String weightText)
    {
        double weightValue =
                Double.parseDouble(weightText);

        /*
         * The user ID is included so SQLite connects the
         * weight record to the correct account.
         */
        long id = dbHelper.insertWeight(
                currentUserId,
                date,
                weightValue
        );

        if (id == -1)
        {
            Toast.makeText(
                    this,
                    "A weight entry may already exist for this date.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }

        WeightEntry newEntry = new WeightEntry(
                id,
                date,
                weightValue
        );

        weightEntries.add(newEntry);

        sortWeightEntries();
        adapter.notifyDataSetChanged();
        updateStatistics();

        int newEntryPosition =
                weightEntries.indexOf(newEntry);

        if (newEntryPosition >= 0)
        {
            rvWeights.scrollToPosition(
                    newEntryPosition
            );
        }

        clearEntryFields();

        Toast.makeText(
                this,
                "Weight entry saved.",
                Toast.LENGTH_SHORT
        ).show();
    }

    /**
     * Receives a delete request from the RecyclerView adapter.
     */
    @Override
    public void onDeleteClick(
            WeightEntry entry,
            int position)
    {
        showDeleteConfirmation(entry, position);
    }

    /**
     * Prevents accidental deletion by asking the user to confirm.
     */
    private void showDeleteConfirmation(
            WeightEntry entry,
            int position)
    {
        new AlertDialog.Builder(this)
                .setTitle("Delete weight entry")
                .setMessage(
                        "Delete the entry for "
                                + entry.getDate()
                                + "?"
                )
                .setPositiveButton(
                        "Delete",
                        (dialog, which) ->
                                deleteWeightEntry(
                                        entry,
                                        position
                                )
                )
                .setNegativeButton(
                        "Cancel",
                        null
                )
                .show();
    }

    /**
     * Deletes the selected entry only when it belongs
     * to the currently logged-in user.
     */
    private void deleteWeightEntry(
            WeightEntry entry,
            int position)
    {
        boolean deleted = dbHelper.deleteWeight(
                entry.getId(),
                currentUserId
        );

        if (deleted)
        {
            int actualPosition =
                    weightEntries.indexOf(entry);

            if (actualPosition >= 0)
            {
                weightEntries.remove(actualPosition);

                adapter.notifyItemRemoved(
                        actualPosition
                );
            }
            else if (position >= 0
                    && position < weightEntries.size())
            {
                weightEntries.remove(position);

                adapter.notifyItemRemoved(position);
            }

            updateStatistics();

            Toast.makeText(
                    this,
                    "Weight entry deleted.",
                    Toast.LENGTH_SHORT
            ).show();
        }
        else
        {
            Toast.makeText(
                    this,
                    "The weight entry could not be deleted.",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    /**
     * Sorts weight entries from newest date to oldest date.
     *
     * Dates use the YYYY-MM-DD format, so they can be
     * compared correctly as strings. If two records have
     * the same date, the larger database ID appears first.
     */
    private void sortWeightEntries()
    {
        Collections.sort(
                weightEntries,
                new Comparator<WeightEntry>()
                {
                    @Override
                    public int compare(
                            WeightEntry firstEntry,
                            WeightEntry secondEntry)
                    {
                        int dateComparison =
                                secondEntry
                                        .getDate()
                                        .compareTo(
                                                firstEntry.getDate()
                                        );

                        if (dateComparison != 0)
                        {
                            return dateComparison;
                        }

                        return Long.compare(
                                secondEntry.getId(),
                                firstEntry.getId()
                        );
                    }
                }
        );
    }

    /**
     * Calculates all statistics and displays the results.
     */
    private void updateStatistics()
    {
        WeightStatistics statistics =
                calculateWeightStatistics();

        if (statistics == null)
        {
            displayEmptyStatistics();
            return;
        }

        tvCurrentWeight.setText(
                String.format(
                        Locale.US,
                        "Current Weight: %.1f lb",
                        statistics.currentWeight
                )
        );

        tvHighestWeight.setText(
                String.format(
                        Locale.US,
                        "Highest Weight: %.1f lb",
                        statistics.highestWeight
                )
        );

        tvLowestWeight.setText(
                String.format(
                        Locale.US,
                        "Lowest Weight: %.1f lb",
                        statistics.lowestWeight
                )
        );

        tvAverageWeight.setText(
                String.format(
                        Locale.US,
                        "Average Weight: %.1f lb",
                        statistics.averageWeight
                )
        );

        tvWeightChange.setText(
                String.format(
                        Locale.US,
                        "Weight Change: %+.1f lb",
                        statistics.weightChange
                )
        );
    }

    /**
     * Traverses the ArrayList one time to calculate the highest,
     * lowest, average, current, and total weight-change values.
     */
    private WeightStatistics calculateWeightStatistics()
    {
        if (weightEntries.isEmpty())
        {
            return null;
        }

        WeightEntry newestEntry =
                weightEntries.get(0);

        WeightEntry oldestEntry =
                weightEntries.get(
                        weightEntries.size() - 1
                );

        double currentWeight =
                newestEntry.getWeightValue();

        double oldestWeight =
                oldestEntry.getWeightValue();

        double highestWeight = currentWeight;
        double lowestWeight = currentWeight;
        double totalWeight = 0.0;

        for (WeightEntry entry : weightEntries)
        {
            double weight =
                    entry.getWeightValue();

            totalWeight += weight;

            if (weight > highestWeight)
            {
                highestWeight = weight;
            }

            if (weight < lowestWeight)
            {
                lowestWeight = weight;
            }
        }

        double averageWeight =
                totalWeight / weightEntries.size();

        double weightChange =
                currentWeight - oldestWeight;

        return new WeightStatistics(
                currentWeight,
                highestWeight,
                lowestWeight,
                averageWeight,
                weightChange
        );
    }
    /**
     * Displays placeholder values when no weight records exist.
     */
    private void displayEmptyStatistics()
    {
        tvCurrentWeight.setText(
                "Current Weight: --"
        );

        tvHighestWeight.setText(
                "Highest Weight: --"
        );

        tvLowestWeight.setText(
                "Lowest Weight: --"
        );

        tvAverageWeight.setText(
                "Average Weight: --"
        );

        tvWeightChange.setText(
                "Weight Change: --"
        );
    }

    /**
     * Stores the results produced by the statistics algorithm.
     */
    private static class WeightStatistics
    {
        private final double currentWeight;
        private final double highestWeight;
        private final double lowestWeight;
        private final double averageWeight;
        private final double weightChange;

        private WeightStatistics(
                double currentWeight,
                double highestWeight,
                double lowestWeight,
                double averageWeight,
                double weightChange)
        {
            this.currentWeight = currentWeight;
            this.highestWeight = highestWeight;
            this.lowestWeight = lowestWeight;
            this.averageWeight = averageWeight;
            this.weightChange = weightChange;
        }
    }

    /**
     * Loads only the weight records belonging to
     * the currently logged-in user.
     */
    private void loadWeightsFromDatabase()
    {
        weightEntries.clear();

        try (Cursor cursor =
                     dbHelper.getWeightsForUser(
                             currentUserId
                     ))
        {
            int idIndex =
                    cursor.getColumnIndexOrThrow(
                            AppDatabaseHelper
                                    .COLUMN_WEIGHT_ID
                    );

            int dateIndex =
                    cursor.getColumnIndexOrThrow(
                            AppDatabaseHelper
                                    .COLUMN_WEIGHT_DATE
                    );

            int weightIndex =
                    cursor.getColumnIndexOrThrow(
                            AppDatabaseHelper
                                    .COLUMN_WEIGHT_VALUE
                    );

            while (cursor.moveToNext())
            {
                long id =
                        cursor.getLong(idIndex);

                String date =
                        cursor.getString(dateIndex);

                double weight =
                        cursor.getDouble(weightIndex);

                weightEntries.add(
                        new WeightEntry(
                                id,
                                date,
                                weight
                        )
                );
            }
        }
    }

    /**
     * Returns the selected date.
     */
    private String getDateInput()
    {
        return etDate
                .getText()
                .toString()
                .trim();
    }

    /**
     * Returns the entered weight.
     */
    private String getWeightInput()
    {
        return etWeight
                .getText()
                .toString()
                .trim();
    }

    /**
     * Clears any validation errors.
     */
    private void clearInputErrors()
    {
        etDate.setError(null);
        etWeight.setError(null);
    }

    /**
     * Clears the input fields after a successful save.
     */
    private void clearEntryFields()
    {
        etDate.setText("");
        etWeight.setText("");
        etWeight.clearFocus();
    }
}