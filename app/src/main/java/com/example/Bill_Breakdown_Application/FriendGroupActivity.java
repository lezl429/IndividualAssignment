package com.example.Bill_Breakdown_Application;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class FriendGroupActivity extends AppCompatActivity {

    private DatabaseHelper dbHelper;
    Button deleteButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_group);

        deleteButton = findViewById(R.id.deleteGroupButton);
        deleteButton.setOnClickListener(this::onDeleteGroupButtonClick);

        // Initialize the database helper
        dbHelper = new DatabaseHelper(this);

        // Add example groups with friends if they do not exist in the database
        insertExampleGroupsAndFriends();

        // Display groups in the UI
        displayGroups();
    }


    // Insert example groups and friends if they do not exist in the database
    private void insertExampleGroupsAndFriends() {
        // Check if "Group 1" exists, if not, insert it with friends
        if (!dbHelper.groupExists("Group 1")) {
            long groupId1 = dbHelper.insertGroupIntoDatabase("Group 1");
            dbHelper.insertFriendIntoDatabase(groupId1, "Friend A");
            dbHelper.insertFriendIntoDatabase(groupId1, "Friend B");
            dbHelper.insertFriendIntoDatabase(groupId1, "Friend C");
        }
    }

    // Method to open CreateGroupActivity
    public void openCreateGroupActivity(View view) {
        Intent intent = new Intent(this, CreateGroupActivity.class);
        startActivityForResult(intent, 1); // Request code 1
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            String newGroupName = data.getStringExtra("group_name");
            ArrayList<String> newFriendNames = data.getStringArrayListExtra("friend_names");
            ArrayList<String> updatedFriends = data.getStringArrayListExtra("updated_friends");

            if (newGroupName != null && newFriendNames != null && !newFriendNames.isEmpty()) {
                // Insert new group and friends into the database
                long groupId = dbHelper.insertGroupIntoDatabase(newGroupName);
                for (String friendName : newFriendNames) {
                    dbHelper.insertFriendIntoDatabase(groupId, friendName);
                }

                // Update the UI to reflect the changes
                displayGroups();
            }

            if (updatedFriends != null && !updatedFriends.isEmpty()) {
                // Assuming you have a reference to your ListView and ArrayAdapter
                ListView friendsListView = findViewById(R.id.friendsListView);
                ArrayAdapter<String> adapter = (ArrayAdapter<String>) friendsListView.getAdapter();

                // Clear the adapter and add the updated friends
                adapter.clear();
                adapter.addAll(updatedFriends);

                // Notify the adapter that the data has changed
                adapter.notifyDataSetChanged();
            }

        }
    }

    // Display the list of groups in the UI
    private void displayGroups() {
        ArrayList<String> groupNames = dbHelper.getAllGroupNames();
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, groupNames);
        ListView groupListView = findViewById(R.id.groupListView);
        groupListView.setAdapter(adapter);

        groupListView.setOnItemClickListener((parent, view, position, id) -> {
            String groupName = groupNames.get(position);
            // Retrieve the groupId for the selected group (you need to implement this logic)
            long groupId = dbHelper.getGroupIdForGroupName(groupName); // Replace this with your logic

            // Retrieve friends for the selected group from the database
            ArrayList<String> friends = dbHelper.getFriendsForGroup(groupName);

            // Handle displaying friends list in UI
            if (friends != null && !friends.isEmpty()) {
                // Create an intent to navigate to FriendsListActivity
                Intent intent = FriendsListActivity.getIntent(FriendGroupActivity.this, friends, groupId);
                FriendGroupActivity.this.startActivity(intent);
            }
        });

    }

    public void onDeleteGroupButtonClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Get the list of group names from the database
        ArrayList<String> groupNames = dbHelper.getAllGroupNames();
        String[] groupArray = groupNames.toArray(new String[groupNames.size()]);

        builder.setItems(groupArray, (dialog, which) -> {
            String groupNameToDelete = groupArray[which];

            AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(this);
            confirmBuilder.setTitle("Confirm Group Deletion");
            confirmBuilder.setMessage("Are you sure you want to delete the group '" + groupNameToDelete + "'?");

            confirmBuilder.setPositiveButton("Delete", (dialog2, which2) -> {
                // Delete the group from the database
                dbHelper.deleteGroup(groupNameToDelete);

                // Update the UI to reflect the changes
                displayGroups();
            });

            confirmBuilder.setNegativeButton("Cancel", (dialog2, which2) -> {
                // Do nothing, user canceled the deletion
            });

            confirmBuilder.show();
        });

        builder.show();
    }




    // Static method to get an intent for starting this activity
    public static Intent getIntent(Context context) {
        return new Intent(context, FriendGroupActivity.class);
    }
}






