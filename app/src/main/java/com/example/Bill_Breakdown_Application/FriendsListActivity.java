package com.example.Bill_Breakdown_Application;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class FriendsListActivity extends AppCompatActivity {

    private ArrayList<String> friends = new ArrayList<>();
    private long groupId;
    private int selectedPosition = -1;
    private ListView friendsListView;
    private ArrayAdapter<String> adapter;
    private DatabaseHelper dbHelper;
    // Find the "Add Friend" button and set a click listener
    private Button addFriendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends_list);
        Button addFriendButton = findViewById(R.id.addFriendButton);
        addFriendButton.setOnClickListener(this::onAddFriendButtonClick);

        // Initialize the database helper
        dbHelper = new DatabaseHelper(this);

        Intent intent = getIntent();
        if (intent != null) {
            friends = intent.getStringArrayListExtra("friends_list");
            groupId = intent.getLongExtra("group_id",-1); // Retrieve the groupId

            if (friends != null && !friends.isEmpty()) {
                setupListView();
            }
        }
    }

    private void setupListView() {
        friendsListView = findViewById(R.id.friendsListView);
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, friends);
        friendsListView.setAdapter(adapter);

        friendsListView.setOnItemClickListener((parent, view, position, id) -> {
            if (selectedPosition != -1) {
                parent.getChildAt(selectedPosition).setBackgroundColor(0); // Transparent
            }
            selectedPosition = position;
            view.setBackgroundColor(getResources().getColor(R.color.selectedColor));
        });
    }

    // Method triggered when the "Delete" button is clicked
    public void onDeleteButtonClick(View view) {
        if (selectedPosition != -1) {
            // Prompt for confirmation before deleting
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Confirm Friend Deletion");
            builder.setMessage("Are you sure you want to delete this friend?");

            builder.setPositiveButton("Delete", (dialog, which) -> {
                // Remove friend from the list
                String friendToDelete = friends.get(selectedPosition);
                friends.remove(selectedPosition);
                // Update the UI
                adapter.notifyDataSetChanged();

                // Update the friend list in the database
                dbHelper.removeFriendFromDatabase(friendToDelete);

                resetHighlightColors();
                selectedPosition = -1;
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> {
                // Do nothing, user canceled the deletion
            });

            builder.show();
        }
    }

    private void onAddFriendButtonClick(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add New Friend");
        builder.setMessage("Enter the name of the new friend:");

        // Create an EditText input field in the dialog
        final EditText input = new EditText(this);
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String newFriendName = input.getText().toString().trim();

            // Check if the input is not empty
            if (!newFriendName.isEmpty()) {
                // Add the new friend to the database and update the UI
                long newFriendId = dbHelper.insertFriendIntoDatabase(groupId, newFriendName);
                if (newFriendId != -1) {
                    friends.add(newFriendName);
                    adapter.notifyDataSetChanged();
                }
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            // Do nothing, user canceled the friend addition
        });

        builder.show();
    }


    // Method to reset highlighting of selected items
    private void resetHighlightColors() {
        for (int i = 0; i < friendsListView.getChildCount(); i++) {
            friendsListView.getChildAt(i).setBackgroundColor(Color.TRANSPARENT);
        }
    }

    // Static method to get an intent for starting this activity with the list of friends
    public static Intent getIntent(Context context, ArrayList<String> friends, long groupId) {
        Intent intent = new Intent(context, FriendsListActivity.class);
        intent.putStringArrayListExtra("friends_list", friends);
        intent.putExtra("group_id", groupId); // Pass the groupId as an extra
        return intent;
    }

}








