package ui.home;

import android.app.Dialog;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.plantask.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.Locale;

import data.DBHelper;
import data.TaskModel;

public class TaskBottomSheet extends BottomSheetDialogFragment {
    private final TaskModel task;
    int position;
    int sheetMode;
    BottomSheetTaskActionListener bottomSheetTaskActionListener;
    public final static int VIEW_MODE = 1;
    public final static int EDIT_MODE = 0;
    Chip chipStatus;

    public TaskBottomSheet(TaskModel task, int position, int sheetMode , BottomSheetTaskActionListener bottomSheetTaskActionListener){
        this.task = task;
        this.position = position;
        this.sheetMode = sheetMode;
        this.bottomSheetTaskActionListener = bottomSheetTaskActionListener;
    }
    public TaskBottomSheet(TaskModel task, int sheetMode){
        this.task = task;
        this.sheetMode = sheetMode;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_task_detail, null);

        Button btnEdit = view.findViewById(R.id.btnEdit);
        Button btnDelete = view.findViewById(R.id.btnDelete);
        ImageButton btnBack = view.findViewById(R.id.btnBack);

        TextView tvTitle = view.findViewById(R.id.tvTaskTitle);
        TextView tvDes = view.findViewById(R.id.tvTaskDescription);
        TextView tvCreationTime = view.findViewById(R.id.tvTaskCreationTime);
        TextView tvDueTime = view.findViewById(R.id.tvTaskDueTime);
        chipStatus = view.findViewById(R.id.taskStatusChip);

        if(sheetMode == EDIT_MODE){
            btnEdit.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);
        }
        else {
            // In VIEW_MODE
            btnEdit.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
        }

        setChipStatus();

        tvTitle.setText(task.getTitle());

        SimpleDateFormat sdfLong = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault());
        SimpleDateFormat sdfToday = new SimpleDateFormat("hh:mm a", Locale.getDefault());

        if(task.getDescription().isEmpty()){
            tvDes.setText("No description");
        }
        else
            tvDes.setText(task.getDescription());

        if(DateUtils.isToday(task.getCreation_time())) {
            String creationTimeString = "Today, " + sdfToday.format(task.getCreation_time());
            tvCreationTime.setText(creationTimeString);
        }
        else tvCreationTime.setText(sdfLong.format(task.getCreation_time()));

        if(task.getReminder_time() == 0)
            tvDueTime.setText("No reminder set");

        else {
            if(DateUtils.isToday(task.getReminder_time())) {
                String remTimeString = "Today, " + sdfToday.format(task.getReminder_time());
                tvDueTime.setText(remTimeString);
            }
            else
                tvDueTime.setText(sdfLong.format(task.getReminder_time()));
        }

        if(task.isStatusCompleted()) {
            TextView tvCompletionTime = view.findViewById(R.id.tvTaskCompletionTime);
            TextView tvCompletionLabel = view.findViewById(R.id.tvCompletionLabel);
            View viewAboveCreationTime = view.findViewById(R.id.viewAboveCreationTime);

            tvCompletionLabel.setVisibility(View.VISIBLE);
            tvCompletionTime.setVisibility(View.VISIBLE);
            viewAboveCreationTime.setVisibility(View.VISIBLE);

            if(DateUtils.isToday(task.getCompletion_time())) {
                String completionTimeString = "Today, " + sdfToday.format(task.getCompletion_time());
                tvCompletionTime.setText(completionTimeString);
            }
            else
                tvCompletionTime.setText(sdfLong.format(task.getCompletion_time()));
        }

        if(sheetMode == EDIT_MODE) {
            btnEdit.setOnClickListener(v -> {
                v.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.button_click_scale_animation));

                bottomSheetTaskActionListener.onEditTask(task, position);
                dismiss();
            });

            btnDelete.setOnClickListener(v -> {
                v.startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.button_click_scale_animation));

                bottomSheetTaskActionListener.onDeleteTask(task, position);
                dismiss();
            });
        }
        chipStatus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(@NonNull CompoundButton buttonView, boolean isChecked) {
                Log.d("isChecked","isChecked value: " + isChecked);
                DBHelper db = new DBHelper(requireContext());
                if(isChecked){
                    db.updateTaskState(task.getId(), true);
                    Toast.makeText(requireContext(), "Task marked as completed", Toast.LENGTH_SHORT).show();
                }
                else{
                    db.updateTaskState(task.getId(), false);
                    Toast.makeText(requireContext(), "Task marked as pending", Toast.LENGTH_SHORT).show();
                }
                task.setIsStatusCompleted(isChecked);
                Log.d("isChecked", "task status: " + task.isStatusCompleted());
                setChipStatus();
                db.close();
                bottomSheetTaskActionListener.onTaskStatusChange(position);
            }
        });

        btnBack.setOnClickListener(v -> {
            dismiss();
        });

        dialog.setContentView(view);
        return dialog;
    }

    private void setChipStatus(){
        if(task.isStatusCompleted()) {
            chipStatus.setChecked(true);
            chipStatus.setText("Completed");
            chipStatus.setChipIconResource(R.drawable.task_completed_icon);
            chipStatus.setChipBackgroundColorResource(R.color.completed);
        }
        else{
            chipStatus.setChecked(false);
            chipStatus.setText("Pending");
            chipStatus.setChipIconResource(R.drawable.task_todo_icon);
            chipStatus.setChipBackgroundColorResource(R.color.todo);
        }
    }
}

interface BottomSheetTaskActionListener {
    void onEditTask(TaskModel taskModel, int position);
    void onDeleteTask(TaskModel taskModel, int position);
    void onTaskStatusChange(int position);
}