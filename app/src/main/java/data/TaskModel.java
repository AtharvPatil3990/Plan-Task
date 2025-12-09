package data;

public class TaskModel {
    private int id;
    private String title;
    private String description;
    private int priority;
    private long creation_time;
    private long completion_time;
    private long reminder_time;
    private boolean isStatusCompleted;
    private boolean isNull;

    public TaskModel() {}

//    public TaskModel(String title, String description, int priority, String status, long creation_time, long completion_time, long reminder_time){
//        this.title = title;
//        this.description = description;
//        this.priority = priority;
//        this.status = status;
//        this.creation_time = creation_time;
//        this.completion_time = completion_time;
//        this.reminder_time = reminder_time;
//    }
//    public TaskModel(String title, boolean isStatusCompleted) {
//        this.title = title;
//        this.isStatusCompleted = isStatusCompleted;
//    }
//    public TaskModel(TaskModel taskModel){
//        this.id = taskModel.getId();
//        this.title = taskModel.getTitle();
//        this.isStatusCompleted = taskModel.getStatus().equals("completed");
//        this.description = taskModel.getDescription();
//    }

    // --- Getters ---
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getPriority() { return priority; }
    public long getCreation_time() { return creation_time; }
    public long getCompletion_time() { return completion_time; }
    public long getReminder_time() { return reminder_time; }
    public boolean isStatusCompleted() { return isStatusCompleted; }

    public boolean isNull(){
        return isNull;
    }


    // --- Setters ---
    public void setId(int id) { this.id = id; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setPriority(int priority) { this.priority = priority; }
    public void setCreation_time(long creation_time) { this.creation_time = creation_time; }
    public void setCompletion_time(long completion_time) { this.completion_time = completion_time; }
    public void setReminder_time(long reminder_time) { this.reminder_time = reminder_time; }
    public void setIsStatusCompleted(boolean isStatusCompleted) {
        this.isStatusCompleted = isStatusCompleted;
    }
    public void setIsNull(boolean isNull){
        this.isNull = isNull;
    }

}
