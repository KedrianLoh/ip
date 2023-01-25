public class Task {
    protected String description;
    protected boolean isDone;

    public Task(String description) {
        this.description = description;
        this.isDone = false;
    }

    public String getStatusIcon() {
        return (isDone ? "[X]" : "[ ]");
    }

    public void markAsDone() {
        this.isDone = true;
        System.out.println("Nice! I've marked this task as done:");
    }

    public void unmarkDone() {
        this.isDone = false;
        System.out.println("OK, I've marked this task as not done yet:");
    }
}
