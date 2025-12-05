package ui.home;

import android.app.Dialog;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.plantask.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import data.TaskModel;

public class TaskBottomSheet extends BottomSheetDialogFragment {
    private final TaskModel task;
    int position;
    BottomSheetTaskActionListener bottomSheetTaskActionListener;
    TaskBottomSheet(TaskModel task, int position, BottomSheetTaskActionListener bottomSheetTaskActionListener){
        this.task = task;
        this.position = position;
        this.bottomSheetTaskActionListener = bottomSheetTaskActionListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        BottomSheetDialog dialog = (BottomSheetDialog) super.onCreateDialog(savedInstanceState);
        View view = LayoutInflater.from(getContext()).inflate(R.layout.bottom_sheet_task_detail, null);

        Button btnEdit = view.findViewById(R.id.btnEdit);
        Button btnDelete = view.findViewById(R.id.btnDelete);
        TextView tvTitle = view.findViewById(R.id.tvTaskTitle);
        TextView tvDes = view.findViewById(R.id.tvTaskDescription);
        TextView tvCreationTime = view.findViewById(R.id.tvTaskCreationTime);
        TextView tvDueTime = view.findViewById(R.id.tvTaskDueTime);
        TextView tvStatus = view.findViewById(R.id.tvTaskStatus);

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

            tvStatus.setText("Completed");

            if(DateUtils.isToday(task.getCompletion_time())) {
                String completionTimeString = "Today, " + sdfToday.format(task.getCompletion_time());
                tvCompletionTime.setText(completionTimeString);
            }
            else
                tvCompletionTime.setText(sdfLong.format(task.getCompletion_time()));
        }
        else
            tvStatus.setText("Pending");

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

        dialog.setContentView(view);
        return dialog;
    }
}

interface BottomSheetTaskActionListener {
    void onEditTask(TaskModel taskModel, int position);
    void onDeleteTask(TaskModel taskModel, int position);
}