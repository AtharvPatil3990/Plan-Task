package com.android.plantask;

import android.Manifest;
import android.app.ComponentCaller;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;


import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import data.DBHelper;
import data.TaskModel;
import ui.home.TaskAlarmReceiver;
import ui.home.TaskBottomSheet;

public class MainActivity extends AppCompatActivity {
    public static final int NOTIFICATION_PERMISSION_REQ_CODE = 101;
    private int pendingTaskID = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, 0);
            return insets;
        });

        splashScreen.setKeepOnScreenCondition(() -> false);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        NavHostFragment navHostFragment = (NavHostFragment)getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);

        NavController navController = navHostFragment.getNavController();
        NavigationUI.setupWithNavController(bottomNavigationView ,navController);

//        Requesting permission for POST_NOTIFICATIONS for >=sdk 33
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            if(checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQ_CODE);
            }
        }

        createNotificationChannel();

        Intent intent = getIntent();
        int id = intent.getIntExtra("TASK_ID", -1);
        if(id != -1){
            pendingTaskID = id;
        }

    }

    private void createNotificationChannel(){
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.createNotificationChannel(new NotificationChannel(TaskAlarmReceiver.NOTIFICATION_CHANNEL_ID, TaskAlarmReceiver.NAME, NotificationManager.IMPORTANCE_HIGH));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(pendingTaskID != -1) {
            DBHelper db = new DBHelper(getApplicationContext());
            TaskModel task = db.getTaskFromID(pendingTaskID);
            db.close();
            Log.d("Reminder", "is task null: " + task.isNull());

            if (!task.isNull()) {
                TaskBottomSheet bottomSheet = new TaskBottomSheet(task, TaskBottomSheet.VIEW_MODE);
                bottomSheet.show(getSupportFragmentManager(), "Notification Bottom Sheet");
            }
        }
    }

    @Override
    public void onNewIntent(@NonNull Intent intent, @NonNull ComponentCaller caller) {
        super.onNewIntent(intent, caller);
        int taskID = intent.getIntExtra("TASK_ID", -1);
        if(taskID != -1){
            pendingTaskID = taskID;
        }
    }
}