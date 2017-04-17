package com.bitengine.wdproject;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.*;
import android.widget.EditText;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity{

    private final String TAG = "MainActivity";
    @BindView(R.id.main_team_name) EditText teamName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_to_left);
    }

    public void goToLeaderScreen(View v){
        String name = teamName.getText().toString();
        if(name.isEmpty()){
            Toast.makeText(this, "Type team name first!", Toast.LENGTH_SHORT).show();
            return;
        }
        teamName.setText("");
        Intent i = new Intent(this, LeaderActivity.class);
        i.putExtra("TEAM_NAME",name);
        startActivity(i);
    }

    public void goToMemberScreen(View v){
        Intent i = new Intent(this, MemberActivity.class);
        startActivity(i);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.slide_in_from_right,R.anim.slide_out_to_right);
    }

}
