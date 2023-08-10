package com.example.Bill_Breakdown_Application;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class ResultActivity extends AppCompatActivity {

    private TextView tvSavedResults;
    private DatabaseHelper dbHelper;
    private ArrayList<String> savedResultsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        tvSavedResults = findViewById(R.id.tvSavedResults);
        dbHelper = new DatabaseHelper(this);
        savedResultsList = loadSavedResultsList();

        Button shareResultBtn = findViewById(R.id.btnShareResult);
        shareResultBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                promptForShareResult();
            }
        });

        loadSavedResults();
    }

    private void loadSavedResults() {
        dbHelper = new DatabaseHelper(this);
        ArrayList<ResultEntry> savedResultsList = dbHelper.getAllResults();

        if (savedResultsList != null && !savedResultsList.isEmpty()) {
            StringBuilder resultBuilder = new StringBuilder();
            for (ResultEntry resultEntry : savedResultsList) {
                String result = resultEntry.getResult();
                String timestamp = resultEntry.getTimestamp(); // Get the timestamp
                resultBuilder.append(result).append("\n")
                        .append("Timestamp: ").append(timestamp).append("\n\n");
            }
            tvSavedResults.setText(resultBuilder.toString());
        } else {
            tvSavedResults.setText("No saved results.");
        }
    }

    private ArrayList<String> loadSavedResultsList() {
        ArrayList<String> resultsList = new ArrayList<>();
        DatabaseHelper dbHelper = new DatabaseHelper(this);

        // Load saved results from the database
        ArrayList<ResultEntry> savedResults = dbHelper.getAllResults();

        for (ResultEntry result : savedResults) {
            resultsList.add(result.getResult());
        }

        return resultsList;
    }

    private void promptForShareResult() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Share Result");
        builder.setItems(savedResultsList.toArray(new String[0]), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String selectedResult = savedResultsList.get(which);
                sendEmailWithResult(selectedResult);
            }
        });
        builder.show();
    }

    private void sendEmailWithResult(String resultText) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("plain/text");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Shared Result");
        emailIntent.putExtra(Intent.EXTRA_TEXT, resultText);

        startActivity(Intent.createChooser(emailIntent, "Share Result via Email"));
    }


    public static Intent getIntent(Context context) {
        return new Intent(context, ResultActivity.class);
    }
}