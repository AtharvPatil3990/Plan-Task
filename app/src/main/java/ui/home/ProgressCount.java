package ui.home;

public class ProgressCount {
    int completedTaskCount = 0, totalTaskCount = 0, pendingTaskCount = 0;

    public ProgressCount(int totalCount, int completedCount){
        this.totalTaskCount = totalCount;
        this.completedTaskCount = completedCount;
        pendingTaskCount = totalCount - completedCount;
    }
}
