package com.example.Bill_Breakdown_Application;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database details
    private static final String DATABASE_NAME = "friends_groups.db";
    private static final int DATABASE_VERSION = 1;

    // Table names and column names for groups and friends
    private static final String TABLE_GROUPS = "groups";
    private static final String COLUMN_GROUP_ID = "group_id";
    private static final String COLUMN_GROUP_NAME = "group_name";

    private static final String TABLE_FRIENDS = "friends";
    private static final String COLUMN_FRIEND_ID = "friend_id";
    private static final String COLUMN_FRIEND_NAME = "friend_name";
    private static final String COLUMN_GROUP_ID_FK = "group_id_fk";

    private static final String TABLE_RESULTS = "results";
    private static final String COLUMN_RESULT_ID = "result_id";
    private static final String COLUMN_RESULT_TEXT = "result_text";
    private static final String COLUMN_RESULT_TIMESTAMP = "result_timestamp";

    // Constructor initializes the database
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    // Create two tables: 'groups' and 'friends'
    public void onCreate(SQLiteDatabase db) {
        // SQL query to create the 'groups' table
        String createGroupsTableQuery = "CREATE TABLE " + TABLE_GROUPS +
                "(" + COLUMN_GROUP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_GROUP_NAME + " TEXT)";
        db.execSQL(createGroupsTableQuery);

        // SQL query to create the 'friends' table
        String createFriendsTableQuery = "CREATE TABLE " + TABLE_FRIENDS +
                "(" + COLUMN_FRIEND_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_FRIEND_NAME + " TEXT," +
                COLUMN_GROUP_ID_FK + " INTEGER)";
        db.execSQL(createFriendsTableQuery);

        //SQL query to create results table
        String createResultsTableQuery = "CREATE TABLE " +
                TABLE_RESULTS + "(" +
                COLUMN_RESULT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                COLUMN_RESULT_TEXT + " TEXT," +
                COLUMN_RESULT_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ")";
        db.execSQL(createResultsTableQuery);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrades if needed (not implemented in this code)
    }

    // Check if a group with the given name exists
    public boolean groupExists(String groupName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_GROUPS + " WHERE " + COLUMN_GROUP_NAME + " = ?", new String[]{groupName});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    // Insert a new group into the 'groups' table
    public long insertGroupIntoDatabase(String groupName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_GROUP_NAME, groupName);
        return db.insert(TABLE_GROUPS, null, values);
    }

    // Insert a new friend into the 'friends' table, associating them with a group
    public long insertFriendIntoDatabase(long groupId, String friendName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FRIEND_NAME, friendName);
        values.put(COLUMN_GROUP_ID_FK, groupId);
        return db.insert(TABLE_FRIENDS, null, values);
    }

    // Get a list of all group names stored in the 'groups' table
    public ArrayList<String> getAllGroupNames() {
        ArrayList<String> groupNames = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_GROUPS, new String[]{COLUMN_GROUP_NAME}, null, null, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            do {
                groupNames.add(cursor.getString(cursor.getColumnIndex(COLUMN_GROUP_NAME)));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return groupNames;
    }

    // Get a list of friends belonging to a specific group
    public ArrayList<String> getFriendsForGroup(String groupName) {
        ArrayList<String> friends = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // SQL query to retrieve friends associated with a specific group
        String query = "SELECT " + COLUMN_FRIEND_NAME +
                " FROM " + TABLE_FRIENDS +
                " INNER JOIN " + TABLE_GROUPS +
                " ON " + TABLE_FRIENDS + "." + COLUMN_GROUP_ID_FK + " = " + TABLE_GROUPS + "." + COLUMN_GROUP_ID +
                " WHERE " + TABLE_GROUPS + "." + COLUMN_GROUP_NAME + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{groupName});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                friends.add(cursor.getString(cursor.getColumnIndex(COLUMN_FRIEND_NAME)));
            } while (cursor.moveToNext());
            cursor.close();
        }
        return friends;
    }

    // Retrieve the groupId for a given group name
    public long getGroupIdForGroupName(String groupName) {
        SQLiteDatabase db = this.getReadableDatabase();
        long groupId = -1; // Default value if groupId is not found

        // Query the groups table to get the groupId for the given group name
        Cursor cursor = db.query(
                TABLE_GROUPS,
                new String[]{COLUMN_GROUP_ID},
                COLUMN_GROUP_NAME + " = ?",
                new String[]{groupName},
                null, null, null
        );

        if (cursor != null && cursor.moveToFirst()) {
            groupId = cursor.getLong(cursor.getColumnIndex(COLUMN_GROUP_ID));
            cursor.close();
        }

        return groupId;
    }

    public void removeFriendFromDatabase(String friendName) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_FRIENDS, COLUMN_FRIEND_NAME + " = ?", new String[]{friendName});
        db.close();
    }

    public void deleteGroup(String groupName) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Delete the group and its associated friends from the tables
        db.delete(TABLE_GROUPS, COLUMN_GROUP_NAME + " = ?", new String[]{groupName});
        db.delete(TABLE_FRIENDS, COLUMN_GROUP_ID_FK + " = ?", new String[]{String.valueOf(getGroupIdForGroupName(groupName))});

        db.close();
    }

    public ArrayList<String> getAllFriendGroups() {
        ArrayList<String> friendGroups = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_GROUPS, new String[]{COLUMN_GROUP_NAME}, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                friendGroups.add(cursor.getString(cursor.getColumnIndex(COLUMN_GROUP_NAME)));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return friendGroups;
    }

    public ArrayList<String> getFriendsForFriendGroup(String friendGroupName) {
        ArrayList<String> friends = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // SQL query to retrieve friends associated with a specific group
        String query = "SELECT " + COLUMN_FRIEND_NAME +
                " FROM " + TABLE_FRIENDS +
                " INNER JOIN " + TABLE_GROUPS +
                " ON " + TABLE_FRIENDS + "." + COLUMN_GROUP_ID_FK + " = " + TABLE_GROUPS + "." + COLUMN_GROUP_ID +
                " WHERE " + TABLE_GROUPS + "." + COLUMN_GROUP_NAME + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{friendGroupName});

        if (cursor != null && cursor.moveToFirst()) {
            do {
                friends.add(cursor.getString(cursor.getColumnIndex(COLUMN_FRIEND_NAME)));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return friends;
    }

    public int getFriendGroupSize(String groupName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] projection = {COLUMN_FRIEND_NAME};
        String selection = COLUMN_GROUP_ID_FK + " = (SELECT " + COLUMN_GROUP_ID + " FROM " + TABLE_GROUPS + " WHERE " + COLUMN_GROUP_NAME + " = ?)";
        String[] selectionArgs = {groupName};

        Cursor cursor = db.query(TABLE_FRIENDS, projection, selection, selectionArgs, null, null, null);

        int groupSize = 0;
        if (cursor != null) {
            groupSize = cursor.getCount();
            cursor.close();
        }

        return groupSize;
    }

    // Method to insert a result into the results table
    public long insertResult(String resultText) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_RESULT_TEXT, resultText);
        return db.insert(TABLE_RESULTS, null, values);
    }

    // Method to retrieve all saved results from the database
    public ArrayList<ResultEntry> getAllResults() {
        ArrayList<ResultEntry> results = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT " + COLUMN_RESULT_TEXT + ", " + COLUMN_RESULT_TIMESTAMP +
                " FROM " + TABLE_RESULTS + " ORDER BY " + COLUMN_RESULT_TIMESTAMP + " DESC";

        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                String result = cursor.getString(cursor.getColumnIndex(COLUMN_RESULT_TEXT));
                String timestamp = cursor.getString(cursor.getColumnIndex(COLUMN_RESULT_TIMESTAMP));
                results.add(new ResultEntry(result, timestamp));
            } while (cursor.moveToNext());
            cursor.close();
        }

        return results;
    }
}

