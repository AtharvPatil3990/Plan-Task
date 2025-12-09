package ui.home;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.plantask.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import data.TaskModel;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.TaskViewHolder> {
    ArrayList<TaskModel> taskArrayList;
    Context context;
    FragmentManager fragmentManager;
    OnTaskMenuItemClickListener menuItemClickListener;
    OnTaskStatusChangedListener taskStatusChangedListener;
    BottomSheetTaskActionListener bottomSheetTaskActionListener;
    public RecyclerViewAdapter(Context context, ArrayList<TaskModel> taskArrayList, FragmentManager fragmentManager){
        this.context = context;
        this.taskArrayList = taskArrayList;
        this.fragmentManager = fragmentManager;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        return new TaskViewHolder(inflater.inflate(R.layout.recycler_view_layout, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        holder.tvTaskTitle.setText(taskArrayList.get(position).getTitle());
        holder.updateChipCheckedStatusUI(taskArrayList.get(position).isStatusCompleted());

        holder.tvDueDate.setText(setDueDate(taskArrayList.get(position).getReminder_time()));

        holder.cvTask.setOnLongClickListener(v -> {
            showMenuPopup(v, holder.getLayoutPosition());
            return true;
        });

        holder.chipTaskStatus.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                v.startAnimation(AnimationUtils.loadAnimation(context, R.anim.button_click_scale_animation));
                int taskPosition = holder.getLayoutPosition();
                TaskModel taskModel = taskArrayList.get(taskPosition);
                taskModel.setIsStatusCompleted(!taskModel.isStatusCompleted());
                if(taskStatusChangedListener != null) {
                    taskStatusChangedListener.onTaskStatusChanged(taskModel, taskPosition);
                    holder.updateChipCheckedStatusUI(taskModel.isStatusCompleted());
                }
                return true;
            }
        });

        holder.cvTask.setOnClickListener(v -> {
            v.startAnimation(AnimationUtils.loadAnimation(v.getContext(), R.anim.button_click_scale_animation));
            int positionInside = holder.getLayoutPosition(); // position = index
            TaskModel taskModel = taskArrayList.get(positionInside);
            TaskBottomSheet taskBottomSheet = new TaskBottomSheet(taskModel, positionInside, TaskBottomSheet.EDIT_MODE, bottomSheetTaskActionListener);
            taskBottomSheet.show(fragmentManager, "Task Bottom Sheet");
        });
    }

    @Override
    public int getItemCount() {
        return taskArrayList.size();
    }

    private String setDueDate(long dueDateMills){
        String dueDateText = "";
        SimpleDateFormat sdf;
        if(dueDateMills != 0) {
            if (DateUtils.isToday(dueDateMills))
                dueDateText += "Today, ";

            else {
                sdf = new SimpleDateFormat("MMM dd,", Locale.getDefault());
                dueDateText += sdf.format(new Date(dueDateMills));
            }
            sdf = new SimpleDateFormat("hh:mm a", Locale.getDefault());
            dueDateText += " " + sdf.format(new Date(dueDateMills));
        }
        else{
            dueDateText = "No reminder set";
        }

        return dueDateText;
    }

    private void showMenuPopup(View v, int position){
        PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
        popupMenu.getMenuInflater().inflate(R.menu.task_delete_update_menu, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                if(menuItemClickListener!=null) {
                    if (id == R.id.update_menu)
                        menuItemClickListener.onUpdateTask(taskArrayList.get(position), position);
                    else
                        menuItemClickListener.onDeleteTask(taskArrayList.get(position), position);
                }
                return true;
            }
        });
        popupMenu.show();
    }
    public void setOnTaskMenuItemClickListener(OnTaskMenuItemClickListener listener){
        this.menuItemClickListener = listener;
    }
    public void setOnTaskStatusChangedListener(OnTaskStatusChangedListener listener){
        this.taskStatusChangedListener = listener;
    }
    public void setBottomSheetTaskActionListener(BottomSheetTaskActionListener listener){
        this.bottomSheetTaskActionListener = listener;
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder{
        TextView tvTaskTitle, tvDueDate;
        Chip chipTaskStatus;
        MaterialCardView cvTask;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            chipTaskStatus = itemView.findViewById(R.id.chipTaskStatus);
            cvTask = itemView.findViewById(R.id.cvTask);
        }
        private void updateChipCheckedStatusUI(boolean toBeChecked){
            if(toBeChecked) {
//                Task status saved as checked
                chipTaskStatus.setChecked(true);
                chipTaskStatus.setText("Done ");
                chipTaskStatus.setChipBackgroundColorResource(R.color.completed);
                chipTaskStatus.setChipIconResource(R.drawable.task_completed_icon);
            }
            else {
//                Task status saved as unchecked
                chipTaskStatus.setChecked(false);
                chipTaskStatus.setText("To Do");
                chipTaskStatus.setChipBackgroundColorResource(R.color.todo);
                chipTaskStatus.setChipIconResource(R.drawable.task_todo_icon);
            }
        }
    }
}

interface OnTaskMenuItemClickListener{
    void onUpdateTask(TaskModel taskModel, int position);
    void onDeleteTask(TaskModel taskModel, int position);
}

interface OnTaskStatusChangedListener{
    void onTaskStatusChanged(TaskModel taskModel, int position);
}