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
    TextView tvProgressCount, tvPendingTaskCount, tvCompletedTaskCount;
    ProgressCount progressCount;

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
                    task.setIsStatusCompleted(false);
                    long id = dbHelper.insertTask(task);
                    task.setId(id);
                    taskArrayList.add(task);

                    assert rvTaskList.getAdapter() != null;
                    rvTaskList.getAdapter().notifyItemInserted(taskArrayList.size()-1);
                    rvTaskList.scrollToPosition(taskArrayList.size()-1);
                    Toast.makeText(requireContext(), "Task added successfully", Toast.LENGTH_SHORT).show();
                    progressCount.addTotalTaskCount();
                    progressCount.addPendingTaskCount();
                    updateTaskIndicator();
                }
                else {
                    Toast.makeText(requireContext(), "An error occurred please try again.", Toast.LENGTH_SHORT).show();
                    updateTaskIndicator();
                }
            }
        });
        progressCount = new ProgressCount(0,0);
        tvDisplayName = view.findViewById(R.id.tvWelcomeText);
        tvDisplayDate = view.findViewById(R.id.tvDate);
        rvTaskList = view.findViewById(R.id.rvTaskList);
        btnAddTask = view.findViewById(R.id.btnAddTask);
        ivNoTasks = view.findViewById(R.id.ivEmptyList);
        progressIndicator = view.findViewById(R.id.progressIndicator);
        tvProgressCount = view.findViewById(R.id.tvProgressCount);
        tvPendingTaskCount = view.findViewById(R.id.tvPendingTaskCount);
        tvCompletedTaskCount = view.findViewById(R.id.tvCompletedTaskCount);

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
        updateTaskIndicator();

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
                        String displayName = "Welcome, " + name;
                        tvDisplayName.setText(displayName);
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
        taskArrayList = db.fetchTodayTask();
        db.close();
        RecyclerViewAdapter rvAdapter = new RecyclerViewAdapter(requireContext(), taskArrayList);

        rvTaskList.setAdapter(rvAdapter);
        progressCount.totalTaskCount = taskArrayList.size();
        int completedTaskCount = 0;
        for (int i = 0; i < taskArrayList.size(); i++) {
            if(taskArrayList.get(i).isStatusCompleted())
                completedTaskCount++;
        }
        progressCount.completedTaskCount = completedTaskCount;
        progressCount.pendingTaskCount = (progressCount.totalTaskCount - completedTaskCount);
        if(taskArrayList.isEmpty())
            setEmptyListState(true);

        rvAdapter.setOnTaskMenuItemClickListener(new OnTaskMenuItemClickListener() {
            @Override
            public void onUpdateTask(TaskModel taskModel, int position) {

            }
            @Override
            public void onDeleteTask(TaskModel taskModel, int position) {
                DBHelper dbHelper = new DBHelper(requireContext());
                dbHelper.deleteTask(taskModel.getId());
                dbHelper.close();
                assert rvTaskList.getAdapter() != null;
                rvTaskList.getAdapter().notifyItemRemoved(position);

                taskArrayList.remove(position);
                progressCount.totalTaskCount--;
                if (progressCount.totalTaskCount == 0)
                    setEmptyListState(true);
                if(taskModel.isStatusCompleted())
                    progressCount.completedTaskCount--;
                else
                    progressCount.pendingTaskCount--;

                updateTaskIndicator();
            }
        });

//        Change the status of task from todo -> completed
        rvAdapter.setOnTaskStatusChangedListener((taskModel, position) -> {
            DBHelper dbHelper = new DBHelper(requireContext());
            dbHelper.updateTaskState(taskModel.getId(), taskModel.isStatusCompleted());
            dbHelper.close();
            if(taskModel.isStatusCompleted()) {
                progressCount.pendingTaskCount--;
                progressCount.completedTaskCount++;
            }
            else {
                progressCount.pendingTaskCount++;
                progressCount.completedTaskCount--;
            }
            assert rvTaskList.getAdapter() != null;
            rvTaskList.getAdapter().notifyItemChanged(position);
            updateTaskIndicator();
        });
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
        if(progressCount.totalTaskCount== 0){
            progressIndicator.setProgress(0);
            tvProgressCount.setText("0%");
            ivNoTasks.setVisibility(View.VISIBLE);
        }
        else {
            ivNoTasks.setVisibility(View.GONE);

            int progress = (int) (((float) progressCount.completedTaskCount / progressCount.totalTaskCount) * 100);
            String progressText = progress + "%";
            tvProgressCount.setText(progressText);
            progressIndicator.setProgress(progress);
        }
        String completedCountText = "Completed: " + progressCount.completedTaskCount;
        String pendingCountText = "Pending: " + progressCount.pendingTaskCount;

        tvPendingTaskCount.setText(pendingCountText);
        tvCompletedTaskCount.setText(completedCountText);
    }
    @Override
    public void onResume() {
        super.onResume();
        progressCount.totalTaskCount = taskArrayList.size();

        updateTaskIndicator();
    }
}