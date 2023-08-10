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

public class BreakdownActivity extends AppCompatActivity {
    private EditText totalBillEdt, breakdownDetailsEdt;
    private TextView breakdownResultTV;
    private Button breakdownBtn, resetBtn, btnSaveResult;
    private DatabaseHelper dbHelper;
    private Spinner friendGroupSpinner;
    private EditText numOfPeopleEditText;
    private ArrayList<String> friendGroups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breakdown);

        totalBillEdt = findViewById(R.id.edtTotalBill);
        breakdownDetailsEdt = findViewById(R.id.edtBreakdownDetails);
        breakdownResultTV = findViewById(R.id.tvBreakdownResult);
        breakdownBtn = findViewById(R.id.btnCalculateBreakdown);
        numOfPeopleEditText = findViewById(R.id.edtNumberOfPeople);
        friendGroupSpinner = findViewById(R.id.friendGroupSpinner);
        resetBtn = findViewById(R.id.idBtnReset);
        btnSaveResult = findViewById(R.id.btnSaveResult);

        dbHelper = new DatabaseHelper(this);

        // Get the list of friend groups from the database
        friendGroups = dbHelper.getAllFriendGroups();
        friendGroups.add(0, "No Group Selected"); // Add an option for no group

        // Create an ArrayAdapter for the spinner
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, friendGroups);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        friendGroupSpinner.setAdapter(spinnerAdapter);

        // Set an OnItemSelectedListener to monitor spinner selection
        friendGroupSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                if (position == 0) {
                    // No group selected, enable the EditText
                    numOfPeopleEditText.setText("No group selected.");
                    numOfPeopleEditText.setEnabled(false);
                    breakdownResultTV.setText(""); // Clear the breakdown result
                } else {
                    // Disable the EditText and set the number of people based on the group size
                    numOfPeopleEditText.setEnabled(false);
                    String selectedGroupName = friendGroups.get(position);
                    int groupSize = dbHelper.getFriendGroupSize(selectedGroupName);
                    numOfPeopleEditText.setText(String.valueOf(groupSize));

                    // Get the list of friend names in the selected group
                    ArrayList<String> friendNames = dbHelper.getFriendsForGroup(selectedGroupName);

                    // Update the breakdown result EditText with the list of friend names
                    StringBuilder result = new StringBuilder("Individual Amounts:\n");
                    for (String friendName : friendNames) {
                        result.append(friendName).append(":\n");
                    }
                    breakdownResultTV.setText(result.toString());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                // Do nothing
            }
        });


        breakdownBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calculateBreakdown();
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
                String result = breakdownResultTV.getText().toString(); // Get the individual amount result
                saveResultToStorage(result);
            }
        });
    }

    private void calculateBreakdown() {
        String totalBillStr = totalBillEdt.getText().toString();
        String breakdownDetailsStr = breakdownDetailsEdt.getText().toString();

        if (TextUtils.isEmpty(totalBillStr) || TextUtils.isEmpty(breakdownDetailsStr)) {
            breakdownResultTV.setText("Please enter all values.");
            return;
        }

        try {
            double totalBill = Double.parseDouble(totalBillStr);

            // Fetch the friend names based on the selected group
            String selectedGroupName = friendGroups.get(friendGroupSpinner.getSelectedItemPosition());
            ArrayList<String> friendNames = dbHelper.getFriendsForGroup(selectedGroupName);

            String[] breakdownValues = breakdownDetailsStr.split(",");
            double[] individualAmounts = new double[breakdownValues.length];

            boolean isNoGroupSelected = selectedGroupName.equals("No Group Selected");

            int numberOfPeople = isNoGroupSelected ? breakdownValues.length : friendNames.size();

            for (int i = 0; i < breakdownValues.length; i++) {
                breakdownValues[i] = breakdownValues[i].trim();
                if (TextUtils.isEmpty(breakdownValues[i])) {
                    breakdownResultTV.setText("Invalid breakdown format.");
                    return;
                }
            }

            if (breakdownValues.length != numberOfPeople) {
                breakdownResultTV.setText("Number of breakdown values should match the number of people in the group.");
                return;
            }

            boolean isPercentageOrRatio = false;
            for (String value : breakdownValues) {
                if (value.contains("%")) {
                    isPercentageOrRatio = true;
                    break;
                }
            }

            if (isPercentageOrRatio) {
                double totalPercentageOrRatio = 0;
                for (String value : breakdownValues) {
                    if (value.contains("%")) {
                        value = value.replace("%", "").trim();
                        totalPercentageOrRatio += Double.parseDouble(value);
                    }
                }

                for (int i = 0; i < breakdownValues.length; i++) {
                    String value = breakdownValues[i].replace("%", "").trim();
                    double percentageOrRatio = Double.parseDouble(value);
                    individualAmounts[i] = (percentageOrRatio / totalPercentageOrRatio) * totalBill;
                }
            } else {
                double totalAmount = 0;
                for (String value : breakdownValues) {
                    double amount = Double.parseDouble(value);
                    totalAmount += amount;
                }

                for (int i = 0; i < breakdownValues.length; i++) {
                    double amount = Double.parseDouble(breakdownValues[i]);
                    individualAmounts[i] = (amount / totalAmount) * totalBill;
                }
            }

            DecimalFormat decimalFormat = new DecimalFormat("#.##");
            StringBuilder result = new StringBuilder("Individual Amounts:\n");
            for (int i = 0; i < individualAmounts.length; i++) {
                String individualName = isNoGroupSelected ? "Person " + (i + 1) : friendNames.get(i);
                result.append(individualName).append(" : RM").append(decimalFormat.format(individualAmounts[i])).append("\n");
            }

            breakdownResultTV.setText(result.toString());
        } catch (NumberFormatException e) {
            breakdownResultTV.setText("Invalid input. Please enter valid numbers.");
        }
    }



    private void resetFields() {
        totalBillEdt.setText("");
        breakdownDetailsEdt.setText("");
        breakdownResultTV.setText("");
    }

    private void saveResultToStorage(String result) {
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        dbHelper.insertResult(result);
        Toast.makeText(this, "Result saved successfully!", Toast.LENGTH_SHORT).show();
    }

    public static Intent getIntent(Context context) {
        return new Intent(context, BreakdownActivity.class);
    }
}

