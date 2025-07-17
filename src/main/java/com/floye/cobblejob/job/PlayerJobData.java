package com.floye.cobblejob.job;

public class PlayerJobData {

    private String currentJob;
    private int xp;
    private int level;
    public static final int MAX_LEVEL = 100;

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
        if (this.level >= MAX_LEVEL) {
            // Player is at max level, so don't add any XP
            this.xp = getXpForNextLevel();
            return;
        }

        this.xp += amount;
        while (this.xp >= getXpForNextLevel()) {
            if (this.level < MAX_LEVEL) {
                this.xp -= getXpForNextLevel();
                this.level++;
            } else {
                // Reached max level during the XP gain, cap the XP and exit
                this.level = MAX_LEVEL;
                this.xp = getXpForNextLevel();
                return;
            }
        }
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