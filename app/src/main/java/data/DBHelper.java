package data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;

import kotlinx.coroutines.scheduling.Task;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "plan_task_db";
    private static final int DB_VERSION = 1;

    private static final String TABLE_NAME = "table_task";
    private static final String COL_TASK_ID = "task_id";
    private static final String COL_TITLE = "title";
    private static final String COL_DESCRIPTION = "description";
    private static final String COL_PRIORITY = "task_priority";
    private static final String COL_STATUS = "status";
    private static final String COL_CREATION_TIME = "creation_time";
    private static final String COL_COMPLETION_TIME = "completion_time";
    private static final String COL_REMINDER_TIME = "reminder_time";
    public DBHelper(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

//    create table task_table (task_id INTEGER PRIMARY KEY AUTOINCREMENT, title TEXT NOT NULL, description TEXT, task_priority INTEGER DEFAULT 0,
//                   due_date INTEGER, status TEXT DEFAULT to_do, creation_time INTEGER, completion_TIME INTEGER, reminder_time INTEGER)

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableQuery = "CREATE TABLE " + TABLE_NAME + " ( " + COL_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " + COL_TITLE + " TEXT NOT NULL, " + COL_DESCRIPTION + " TEXT, " +
                COL_PRIORITY + " INTEGER DEFAULT 0, "+ COL_STATUS + " TEXT CHECK(status IN ('to_do', 'completed')), " + COL_CREATION_TIME + " INTEGER, " +
                COL_COMPLETION_TIME + " INTEGER, " + COL_REMINDER_TIME + " INTEGER )";
        db.execSQL(createTableQuery);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public long insertTask(TaskModel task){
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COL_TITLE, task.getTitle());
        cv.put(COL_CREATION_TIME, System.currentTimeMillis());  // creation time set here
        cv.put(COL_DESCRIPTION, task.getDescription());
        cv.put(COL_PRIORITY, task.getPriority());
        cv.put(COL_REMINDER_TIME, task.getReminder_time());
        cv.put(COL_STATUS, task.getStatus());

        return db.insert(TABLE_NAME,null,cv);
    }

    public void deleteTask(long task_id){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NAME, COL_TASK_ID + " = " + task_id, null);
    }

    public ArrayList<TaskModel> fetchAllTasks(){
        SQLiteDatabase db = getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);

        ArrayList<TaskModel> tasksArr = new ArrayList<>();
        while(cursor.moveToNext()){
            TaskModel task = new TaskModel();
            task.setId(cursor.getLong(0));
            task.setTitle(cursor.getString(1));
            task.setDescription(cursor.getString(2));
            task.setPriority(cursor.getInt(3));
            task.setStatus(cursor.getString(4));
            task.setCreation_time(cursor.getLong(5));
            task.setCompletion_time(cursor.getLong(6));
            task.setReminder_time(cursor.getLong(7));

            tasksArr.add(task);
        }
        return tasksArr;
    }
}