package ui.home;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationManagerCompat;

import data.DBHelper;

public class TaskActionReceiver extends BroadcastReceiver {
    public static final String ACTION_MARK_DONE = "MARK_DONE";
    public static final String ACTION_CANCEL = "CANCEL";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getStringExtra("ACTION");
        int taskID = intent.getIntExtra("TASK_ID", -1);

        if(action == null) return;

        if(action.equals(ACTION_MARK_DONE)) {
            DBHelper db = new DBHelper(context);
            db.updateTaskState(taskID, true);
            db.close();
            NotificationManagerCompat.from(context).cancel(taskID);
            return;
        }

        if(action.equals((ACTION_CANCEL))){
            NotificationManagerCompat.from(context).cancel(taskID);
        }
    }
}
