package com.example.Bill_Breakdown_Application;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void openPage1(View view) {
        startActivity(SplitActivity.getIntent(this));
    }

    public void openPage2(View view) {
        startActivity(BreakdownActivity.getIntent(this));
    }

    public void openPage3(View view) {
        startActivity(ResultActivity.getIntent(this));
    }

    public void openPage4(View view) {
        startActivity(FriendGroupActivity.getIntent(this));
    }
}






