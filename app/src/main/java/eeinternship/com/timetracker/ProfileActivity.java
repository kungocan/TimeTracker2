package eeinternship.com.timetracker;

import android.accounts.AccountManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ExpandableListView;

import java.util.ArrayList;

import Data.NotificationData;
import Data.ProfileDataDropdown;
import Data.ProfileDataLine;
import Data.Project;
import Data.Ticket;
import Data.UploadSpreadsheetData;
import Data.UserData;

public class ProfileActivity extends AppCompatActivity {

    ExpandableListAdapter listAdapter;
    ExpandableListView expListView;
    private ApplicationTimeTracker applicationTimeTracker;
    private UserData userData;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);


        applicationTimeTracker = (ApplicationTimeTracker) getApplication();
        userData = applicationTimeTracker.getUserData();

        for(ProfileDataDropdown profileDataDropdown : userData.getProfileDataDropdownArrayList()){
            for(ProfileDataLine profileDataLine: profileDataDropdown.getProfileDataLineArrayList()){
                for(Project project: userData.getProjectList()){
                    if(profileDataLine.getProjectName().equals(project.projectName))
                        profileDataLine.setProjectColor(project.getTicketColor());
                }
            }
        }
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("PROFILE");

        // action bar color
        actionBar.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#323232")));

        // status bar color
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        expListView = (ExpandableListView) findViewById(R.id.expandle_listview);
        listAdapter = new ExpandableListAdapter(this,userData.getProfileDataDropdownArrayList());
        expListView.setAdapter(listAdapter);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.notification_btn:
                Intent notificationAC=new Intent(this,NotificationActivity.class);
                startActivity(notificationAC);
                break;
            case R.id.settings_btn:
                Intent settingsAC=new Intent(this,SettingsActivity.class);
                startActivity(settingsAC);
                break;
            case R.id.account_picker:
                chooseAccount();

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }


    public void chooseAccount(){
        Intent intent = AccountManager.newChooseAccountIntent(null, null, new String[]{"com.google"},
                false, null, null, null, null);
        startActivityForResult(intent, 999);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 999&& resultCode == RESULT_OK) {
            String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
            Log.i("Choosen accountName:", accountName);
            userData = new UserData();
            userData.setUserAcount(accountName);
            applicationTimeTracker.setUserData(userData);
            applicationTimeTracker.setAllData();
        }
    }
}
