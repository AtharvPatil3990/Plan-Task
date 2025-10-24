package ui.home;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.plantask.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import data.DBHelper;
import data.TaskModel;

public class HomeFragment extends Fragment {

    TextView tvDisplayName, tvDisplayDate;
    FloatingActionButton btnAddTask;
    RecyclerView rvTaskList;
    ArrayList<TaskModel> taskArrayList;
    ImageView ivNoTasks;
    LinearProgressIndicator progressIndicator;
    TextView tvProgressCountText;

    int completedTaskCount = 0, totalTaskCount = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        getParentFragmentManager().setFragmentResultListener("newTask", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle resultBundle) {
                boolean resultStatus = resultBundle.getBoolean("result");
                Log.e("Result", "Entered the Result callback");
                if(resultStatus){
                    Log.e("Result", "Entered the Result callback true condition");

                    String title = resultBundle.getString("title");
                    String description = resultBundle.getString("description");
                    long reminderTimeInMills = resultBundle.getLong("reminderTimeInMills");

//                    Add a task to the database
                    DBHelper dbHelper = new DBHelper(requireContext());
                    TaskModel task = new TaskModel();
                    task.setTitle(title);
                    task.setDescription(description);
                    task.setReminder_time(reminderTimeInMills);
                    task.setPriority(0); // 0 = normal task, 1 = high priority task
                    task.setStatus("to_do");
                    long id = dbHelper.insertTask(task);
                    task.setId(id);
                    dbHelper.close();
                    taskArrayList.add(task);
                    ivNoTasks.setVisibility(View.INVISIBLE);
                    assert rvTaskList.getAdapter() != null;
                    rvTaskList.getAdapter().notifyItemInserted(taskArrayList.size()-1);
                    rvTaskList.scrollToPosition(taskArrayList.size()-1);
                    Toast.makeText(requireContext(), "Task added successfully", Toast.LENGTH_SHORT).show();
                    totalTaskCount = totalTaskCount + 1;
                    updateTaskIndicator();
                }
                else {
                    Toast.makeText(requireContext(), "An error occurred please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tvDisplayName = view.findViewById(R.id.tvWelcomeText);
        tvDisplayDate = view.findViewById(R.id.tvDate);
        rvTaskList = view.findViewById(R.id.rvTaskList);
        btnAddTask = view.findViewById(R.id.btnAddTask);
        ivNoTasks = view.findViewById(R.id.ivEmptyList);
        progressIndicator = view.findViewById(R.id.progressIndicator);
        tvProgressCountText = view.findViewById(R.id.tvProgressCount);

        btnAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.action_addNewTask);
            }
        });


        setAndGetName();
        setDate();

        rvTaskList.setLayoutManager(new LinearLayoutManager(requireContext()));
        setRecyclerView();

        return view;
    }
    private void setDate(){
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MM/yyyy", Locale.getDefault());
        String date = sdf.format(calendar.getTime());

        tvDisplayDate.setText(date);
    }

    private void setAndGetName(){
        SharedPreferences sharedPreferences = getContext().getSharedPreferences("PlanTaskPref", Context.MODE_PRIVATE);
        String userName = sharedPreferences.getString("Username", null);

        if(userName == null){
            askUserName();
        }
        else
            tvDisplayName.setText("Welcome, " + userName);

    }

    private void askUserName(){

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("PlanTaskPref", Context.MODE_PRIVATE);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getContext());
        dialogBuilder.setTitle("Enter your name");
        LayoutInflater inflater = getLayoutInflater();
        View userNameView = inflater.inflate(R.layout.username_input_layout, null);

        dialogBuilder.setView(userNameView)
            .setPositiveButton("Done", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    TextInputEditText editText = userNameView.findViewById(R.id.etUsername);
                    String name = editText.getText().toString();
                    if(!name.isEmpty()){
                        tvDisplayName.setText("Welcome, " + name);
                        sharedPreferences.edit()
                                .putString("Username", name)
                                .apply();
                    }
                    else
                        tvDisplayName.setText("Welcome!");
                }
            });

        dialogBuilder.setCancelable(false)
                     .show();
    }
    void setRecyclerView() {
        DBHelper db = new DBHelper(requireContext());
        taskArrayList = db.fetchAllTasks();
        db.close();
        RecyclerViewAdapter rvAdapter = new RecyclerViewAdapter(requireContext(), taskArrayList);
        rvAdapter.setOnTaskMenuItemClickListener(new OnTaskMenuItemClickListener() {
            @Override
            public void onUpdateTask(TaskModel taskModel, int position) {

            }
            @Override
            public void onDeleteTask(TaskModel taskModel, int position) {
                DBHelper dbHelper = new DBHelper(requireContext());
                dbHelper.deleteTask(taskModel.getId());
                assert rvTaskList.getAdapter() != null;
                rvTaskList.getAdapter().notifyItemRemoved(position);

                taskArrayList.remove(position);
                totalTaskCount--;
                if (totalTaskCount == 0)
                    setEmptyListState(true);
                if(taskModel.isStatusCompleted())
                    completedTaskCount--;
                updateTaskIndicator();
                dbHelper.close();
            }
        });

        rvAdapter.setOnTaskStatusChangedListener((taskModel, position) -> {
            DBHelper dbHelper = new DBHelper(requireContext());
            dbHelper.updateTaskState(taskModel.getId(), taskModel.isStatusCompleted());
            dbHelper.close();
            updateTaskIndicator();
        });

        rvTaskList.setAdapter(rvAdapter);
        totalTaskCount = taskArrayList.size();

        for (int i = 0; i < totalTaskCount; i++) {
            if(taskArrayList.get(i).isStatusCompleted())
                completedTaskCount++;
        }

        if(taskArrayList.isEmpty()){
            setEmptyListState(true);
        }
    }

    void setEmptyListState(boolean isListEmpty){
        if(isListEmpty) {
            ivNoTasks.setVisibility(View.VISIBLE);
        }
        else {
            ivNoTasks.setVisibility(View.GONE);
            setRecyclerView();
        }
    }

    public void updateTaskIndicator(){
        if(totalTaskCount == 0){
            progressIndicator.setProgress(0);
            tvProgressCountText.setText("0%");
        }
        int progress = (completedTaskCount / totalTaskCount) * 100;
        tvProgressCountText.setText(progress + "%");
        progressIndicator.setProgress(progress);
    }
}
