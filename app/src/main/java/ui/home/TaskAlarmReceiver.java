package ui.home;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;

import androidx.core.content.res.ResourcesCompat;

import com.android.plantask.MainActivity;
import com.android.plantask.R;

import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TaskAlarmReceiver extends BroadcastReceiver {
    public final static String NOTIFICATION_CHANNEL_ID = "task_reminder_channel";
    public final static String NAME = "Task Reminder";

    @Override
    public void onReceive(Context context, Intent intent) {
        int taskID = intent.getIntExtra("TASK_ID", -1);
        String title = intent.getStringExtra("TASK_TITLE");
        long remTimeInMills = intent.getLongExtra("REM_TIME", -1);

        showNotification(context, taskID, title, remTimeInMills);
    }

    private void showNotification(Context context, int taskID, String title, long remTimeInMills){
//        Getting Current time for showing in notification
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
        String remTime = "Due time: " + sdf.format(remTimeInMills);

//        Setting Notification Manager
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

//        Notification Action: Mark as Done
        int markDoneReqCode = taskID + 1000;
        Intent markDoneIntent =  new Intent(context, TaskActionReceiver.class)
                .putExtra("TASK_ID", taskID)
                .putExtra("ACTION", TaskActionReceiver.ACTION_MARK_DONE);
        PendingIntent markDonePendingIntent = PendingIntent.getBroadcast(context, markDoneReqCode, markDoneIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification.Action markDoneAction = new Notification.Action.Builder(
                Icon.createWithResource(context, R.drawable.mark_done_icon),
                "Mark Done",
                markDonePendingIntent
        ).build();

//        Notification Action: Cancel
        int cancelReqCode = taskID + 2000;
        Intent cancelIntent =  new Intent(context, TaskActionReceiver.class)
                .putExtra("TASK_ID", taskID)
                .putExtra("ACTION", TaskActionReceiver.ACTION_CANCEL);
        PendingIntent cancelPendingIntent = PendingIntent.getBroadcast(context, cancelReqCode, cancelIntent, PendingIntent.FLAG_IMMUTABLE);
        Notification.Action cancelAction = new Notification.Action.Builder(
                Icon.createWithResource(context, R.drawable.close_icon),
                "Cancel",
                cancelPendingIntent
        ).build();

//        Intent for notification
        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("TASK_ID", taskID);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent notificationPendingIntent = PendingIntent.getActivity(context, taskID, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.plan_task_logo_notify);
//       Building Notification
        Notification notification = new Notification.Builder(context, NOTIFICATION_CHANNEL_ID)
                .setContentTitle(title)
                .setContentIntent(notificationPendingIntent)
                .setContentText(remTime)
                .setSubText("Reminder for task")
                .addAction(cancelAction)
                .addAction(markDoneAction)
                .setSmallIcon(R.drawable.notification_icon)
                .setLargeIcon(bitmap)
                .setAutoCancel(true)
                .build();

        manager.notify(taskID, notification);
    }
}
