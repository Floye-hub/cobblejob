package com.floye.cobblejob.job;

public class PlayerJobData {
    private String currentJob;
    private int xp;
    private int level;

    public PlayerJobData(String currentJob, int xp, int level) {
        this.currentJob = currentJob;
        this.xp = xp;
        this.level = level;
    }

    public String getCurrentJob() {
        return currentJob;
    }
    public void setCurrentJob(String currentJob) {
        this.currentJob = currentJob;
    }



    public int getXp() {
        return xp;
    }

    public int getLevel() {
        return level;
    }

    public void addXp(int amount) {
        this.xp += amount;
        checkLevelUp();
    }

    private void checkLevelUp() {
        int xpNeeded = calculateXpForNextLevel();
        while (this.xp >= xpNeeded && xpNeeded > 0) {
            this.level++;
            this.xp -= xpNeeded;
            xpNeeded = calculateXpForNextLevel();
        }
    }

    private int calculateXpForNextLevel() {
        return 100 + (this.level * 50);
    }

    public int getXpForNextLevel() {
        return calculateXpForNextLevel();
    }
}