package me.novascomp.microservice.nvm.model;

public class LightweightPriority extends LightweightComponent {

    private String priorityId;

    public LightweightPriority() {
    }

    public LightweightPriority(String priorityId) {
        this.priorityId = priorityId;
    }

    public LightweightPriority(String text, String creatorKey) {
        super(text, creatorKey);
    }

    public String getPriorityId() {
        return priorityId;
    }

    public void setPriorityId(String priorityId) {
        this.priorityId = priorityId;
    }

}
