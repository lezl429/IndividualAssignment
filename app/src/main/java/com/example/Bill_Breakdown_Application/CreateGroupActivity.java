package com.example.Bill_Breakdown_Application;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;

public class CreateGroupActivity extends AppCompatActivity {

    private EditText groupNameEditText;
    private EditText friendsListEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        groupNameEditText = findViewById(R.id.groupNameEditText);
        friendsListEditText = findViewById(R.id.friendNameEditText);
    }

    public void createGroup(View view) {
        String groupName = groupNameEditText.getText().toString().trim();
        String friendsList = friendsListEditText.getText().toString().trim();

        if (!groupName.isEmpty() && !friendsList.isEmpty()) {
            String[] friendNamesArray = friendsList.split(",\\s*"); // Split input by comma and optional spaces
            ArrayList<String> friendNames = new ArrayList<>(Arrays.asList(friendNamesArray));

            Intent resultIntent = new Intent();
            resultIntent.putExtra("group_name", groupName);
            resultIntent.putStringArrayListExtra("friend_names", friendNames);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }
}





