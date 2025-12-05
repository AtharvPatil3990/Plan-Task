package ui.home;

import static com.google.android.material.snackbar.BaseTransientBottomBar.ANIMATION_MODE_FADE;

import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.google.android.material.snackbar.Snackbar;
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
    LinearProgressIndicator progressBar;
    TextView tvProgressCount, tvPendingTaskCount, tvCompletedTaskCount, tvNoTaskStatement;
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
                    boolean isReminderToday = resultBundle.getBoolean("isReminderToday");

                    if(isReminderToday)
                        Toast.makeText(requireContext(), "Task added to list", Toast.LENGTH_SHORT).show();

                    else Snackbar.make(requireContext() ,view, "View added task in calender", Snackbar.LENGTH_SHORT)
                            .setAnimationMode(ANIMATION_MODE_FADE)
                            .setAnchorView(requireActivity().findViewById(R.id.bottom_navigation))
                            .show();

//              Add a task to the database
                    DBHelper dbHelper = new DBHelper(requireContext());
                    TaskModel task = new TaskModel();
                    task.setTitle(title);
                    task.setDescription(description);
                    task.setReminder_time(reminderTimeInMills);
                    task.setPriority(0); // 0 = normal task, 1 = high priority task
                    task.setIsStatusCompleted(false);
                    long id = dbHelper.insertTask(task);
                    dbHelper.close();
                    task.setId(id);

                    if(isReminderToday) {
                        taskArrayList.add(task);
                        assert rvTaskList.getAdapter() != null;
                        rvTaskList.getAdapter().notifyItemInserted(taskArrayList.size() - 1);
                        rvTaskList.scrollToPosition(taskArrayList.size() - 1);
                        progressCount.totalTaskCount++;
                        progressCount.pendingTaskCount++;
                        updateTaskCount();
                    }
                }
                else {
                    updateTaskCount();
                }
            }
        });

        progressCount = new ProgressCount(0,0);
        tvDisplayName = view.findViewById(R.id.tvWelcomeText);
        tvDisplayDate = view.findViewById(R.id.tvDate);
        rvTaskList = view.findViewById(R.id.rvTaskList);
        btnAddTask = view.findViewById(R.id.btnAddTask);
        ivNoTasks = view.findViewById(R.id.ivEmptyList);
        progressBar = view.findViewById(R.id.progressIndicator);
        tvProgressCount = view.findViewById(R.id.tvProgressCount);
        tvPendingTaskCount = view.findViewById(R.id.tvPendingTaskCount);
        tvCompletedTaskCount = view.findViewById(R.id.tvCompletedTaskCount);
        tvNoTaskStatement = view.findViewById(R.id.tvNoTaskStatement);

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
        updateTaskCount();

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
        RecyclerViewAdapter rvAdapter = new RecyclerViewAdapter(requireContext(), taskArrayList, getParentFragmentManager());

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
            setListStateEmpty(true);

        rvAdapter.setOnTaskMenuItemClickListener(new OnTaskMenuItemClickListener() {
            @Override
            public void onUpdateTask(TaskModel taskModel, int position) {
//                todo: setup update layout
            }
            @Override
            public void onDeleteTask(TaskModel taskModel, int position) {
                deleteTask(taskModel, position);
            }
        });

        rvAdapter.setOnTaskStatusChangedListener((taskModel, position) -> {
            DBHelper dbHelper = new DBHelper(requireContext());
            dbHelper.updateTaskState(taskModel.getId(), taskModel.isStatusCompleted());
            dbHelper.close();
            if(taskModel.isStatusCompleted()) {
                if(taskModel.getReminder_time() == 0){
                    Snackbar.make(requireContext(), requireView(), "No reminder set — task appears in calendar after completion.", Snackbar.LENGTH_SHORT)
                            .setAnimationMode(Snackbar.ANIMATION_MODE_FADE)
                            .show();
                }
                progressCount.pendingTaskCount--;
                progressCount.completedTaskCount++;
            }
            else {
                progressCount.pendingTaskCount++;
                progressCount.completedTaskCount--;
            }
            assert rvTaskList.getAdapter() != null;
            rvTaskList.getAdapter().notifyItemChanged(position);
            updateTaskCount();
        });

        rvAdapter.setBottomSheetTaskActionListener(new BottomSheetTaskActionListener() {
            @Override
            public void onEditTask(TaskModel taskModel, int position) {
//                todo: set update task method here
                Toast.makeText(requireContext(), "Task Edit pressed", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteTask(TaskModel taskModel, int positon) {
                deleteTask(taskModel, positon);
                Toast.makeText(requireContext(), "Task Deleted", Toast.LENGTH_SHORT).show();
            }
        });

    }

    void setListStateEmpty(boolean isListEmpty){
        if(isListEmpty) {
            ivNoTasks.setVisibility(View.VISIBLE);
            tvNoTaskStatement.setVisibility(View.VISIBLE);
        }
        else {
            ivNoTasks.setVisibility(View.GONE);
            tvNoTaskStatement.setVisibility(View.GONE);
            setRecyclerView();
        }
    }

    public void updateTaskCount(){
        if(progressCount.totalTaskCount== 0){
            progressBar.setProgress(0);
            tvProgressCount.setText("0%");
            setListStateEmpty(true);
        }
        else {
            setListStateEmpty(false);

            int progress = (int) (((float) progressCount.completedTaskCount / progressCount.totalTaskCount) * 100);
            animateProgressBar(progressBar.getProgress(), progress);
        }

        tvPendingTaskCount.setText(String.valueOf(progressCount.pendingTaskCount));
        tvCompletedTaskCount.setText(String.valueOf(progressCount.completedTaskCount));
    }

    private void animateProgressBar(int from, int to){
        ValueAnimator valueAnimator = ValueAnimator.ofInt(from, to);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.setDuration(600);

        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(@NonNull ValueAnimator animation) {
                int progress = (int) animation.getAnimatedValue();
                progressBar.setProgress(progress);
                tvProgressCount.setText(progress + "%");
            }
        });
        valueAnimator.start();
    }

    private void deleteTask(TaskModel taskModel, int position){
        DBHelper dbHelper = new DBHelper(requireContext());
        dbHelper.deleteTask(taskModel.getId());
        dbHelper.close();
        assert rvTaskList.getAdapter() != null;
        rvTaskList.getAdapter().notifyItemRemoved(position);

        taskArrayList.remove(position);
        progressCount.totalTaskCount--;
        if (progressCount.totalTaskCount == 0)
            setListStateEmpty(true);
        if(taskModel.isStatusCompleted())
            progressCount.completedTaskCount--;
        else
            progressCount.pendingTaskCount--;

        updateTaskCount();
    }

    @Override
    public void onResume() {
        super.onResume();
        progressCount.totalTaskCount = taskArrayList.size();

        updateTaskCount();
    }
}