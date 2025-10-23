package ui.home;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.plantask.R;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import data.TaskModel;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.TaskViewHolder> {
    ArrayList<TaskModel> taskArrayList;
    Context context;
    public RecyclerViewAdapter(Context context, ArrayList<TaskModel> taskArrayList){
        this.context = context;
        this.taskArrayList = taskArrayList;
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
        holder.updateChipCheckedStatus(taskArrayList.get(position).isStatusCompleted());

        holder.tvDueDate.setText(setDueDate(taskArrayList.get(position).getReminder_time()));
    }

    @Override
    public int getItemCount() {
        return taskArrayList.size();
    }

    private String setDueDate(long dueDateMills){
        String dueDateText = "";

        if(DateUtils.isToday(dueDateMills))
            dueDateText += "Today, ";

        else {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd,");
            dueDateText += sdf.format(new Date(dueDateMills));
        }
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        dueDateText += " " + sdf.format(new Date(dueDateMills));

        return dueDateText;
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder{
        TextView tvTaskTitle, tvDueDate;
        Chip chipTaskStatus;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTaskTitle = itemView.findViewById(R.id.tvTaskTitle);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            chipTaskStatus = itemView.findViewById(R.id.chipTaskStatus);

            chipTaskStatus.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    updateChipCheckedStatus(!chipTaskStatus.getText().equals("Completed"));
                    return true;
                }
            });
        }

        private void updateChipCheckedStatus(boolean toBeChecked){
            if(toBeChecked) {
//                Task status saved as checked
                chipTaskStatus.setChecked(true);
                chipTaskStatus.setText("Completed");
                chipTaskStatus.setChipBackgroundColorResource(R.color.green_secondary);
                chipTaskStatus.setChipIconResource(R.drawable.task_completed_icon);
            }
            else {
//                Task status saved as unchecked
                chipTaskStatus.setChecked(false);
                chipTaskStatus.setText("To Do");
                chipTaskStatus.setChipBackgroundColorResource(R.color.yellow_secondary);
                chipTaskStatus.setChipIconResource(R.drawable.task_todo_icon);
            }
        }
    }
}