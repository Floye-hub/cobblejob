package com.floye.cobblejob.job;

public class PlayerJobData {
    private String currentJob;

    public PlayerJobData(String currentJob) {
        this.currentJob = currentJob;
    }

    public String getCurrentJob() {
        return currentJob;
    }

    public void setCurrentJob(String currentJob) {
        this.currentJob = currentJob;
    }
}