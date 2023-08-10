package com.example.Bill_Breakdown_Application;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class SplitActivity extends AppCompatActivity {
    private EditText amountEdt, peopleEdt;
    private TextView amtTV;
    private Button resetBtn, amtBtn, btnSaveResult;
    private DatabaseHelper dbHelper;
    private EditText numOfPeopleEditText;
    private Spinner friendGroupSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_split);

        dbHelper = new DatabaseHelper(this);

        amountEdt = findViewById(R.id.idEdtAMount);
        peopleEdt = findViewById(R.id.idEdtPeople);
        amtBtn = findViewById(R.id.idBtnGetAmount);
        resetBtn = findViewById(R.id.idBtnReset);
        amtTV = findViewById(R.id.idTVIndividualAmount);
        numOfPeopleEditText = findViewById(R.id.idEdtPeople);
        friendGroupSpinner = findViewById(R.id.friendGroupSpinner);
        btnSaveResult = findViewById(R.id.btnSaveResult);

        amtBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateIndividualAmount();
            }
        });

        // Get the list of friend groups from the database
        ArrayList<String> friendGroups = dbHelper.getAllFriendGroups();
        friendGroups.add(0, "No Group Selected"); // Add an option for no group

        // Create an ArrayAdapter for the spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, friendGroups);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        friendGroupSpinner.setAdapter(spinnerAdapter);

        // Set an OnItemSelectedListener to monitor spinner selection
        friendGroupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                // Check if the "No Group Selected" option is selected
                if (position == 0) {
                    // Enable the EditText
                    numOfPeopleEditText.setEnabled(true);
                } else {
                    // Disable the EditText and set the number of people based on the group size
                    numOfPeopleEditText.setEnabled(false);
                    String selectedGroupName = friendGroups.get(position);
                    int groupSize = dbHelper.getFriendGroupSize(selectedGroupName);
                    numOfPeopleEditText.setText(String.valueOf(groupSize));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });

        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetFields();
            }
        });

        btnSaveResult.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String result = amtTV.getText().toString(); // Get the individual amount result
                saveResultToStorage(result);
            }
        });
    }

    private void calculateIndividualAmount() {
        String amountStr = amountEdt.getText().toString();
        String peopleStr = peopleEdt.getText().toString();

        if (TextUtils.isEmpty(amountStr) || TextUtils.isEmpty(peopleStr)) {
            amtTV.setText("Please enter all values.");
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            int people = Integer.parseInt(peopleStr);

            if (people <= 0) {
                amtTV.setText("Number of people should be greater than zero.");
                return;
            }

            double individualAmt = amount / people;
            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            String formattedIndividualAmt = decimalFormat.format(individualAmt);

            // Prepare the result message
            StringBuilder resultMessage = new StringBuilder("Individual Amounts:\n");

            String selectedGroupName = friendGroupSpinner.getSelectedItem().toString();
            boolean isNoGroupSelected = selectedGroupName.equals("No Group Selected");

            if (isNoGroupSelected) {
                for (int i = 0; i < people; i++) {
                    resultMessage.append("Person ").append(i + 1).append(": RM").append(formattedIndividualAmt).append("\n");
                }
            } else {
                ArrayList<String> individualNames = dbHelper.getFriendsForFriendGroup(selectedGroupName);
                for (int i = 0; i < individualNames.size(); i++) {
                    resultMessage.append(individualNames.get(i)).append(": RM").append(formattedIndividualAmt).append("\n");
                }
            }

            // Save the result to the database
            String result = resultMessage.toString();
            //dbHelper.insertResultIntoDatabase(result);

            amtTV.setText(result);
        } catch (NumberFormatException e) {
            amtTV.setText("Invalid input. Please enter valid numbers.");
        }
    }

    private void resetFields() {
        amountEdt.setText("");
        peopleEdt.setText("");
        amtTV.setText("");
    }

    private void saveResultToStorage(String result) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.insertResult(result);
        Toast.makeText(this, "Result saved successfully!", Toast.LENGTH_SHORT).show();
    }

    public static Intent getIntent(Context context) {
        return new Intent(context, SplitActivity.class);
    }
}

