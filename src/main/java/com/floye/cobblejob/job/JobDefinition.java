package com.floye.cobblejob.job;

import com.google.gson.JsonObject;

public class JobDefinition {

    private final String jobTypeId;
    private final String displayName;
    private final String description;

    public JobDefinition(String jobTypeId, JsonObject json) {
        this.jobTypeId = jobTypeId;
        this.displayName = json.get("displayName").getAsString();
        this.description = json.get("description").getAsString();
    }

    public String getJobTypeId() {
        return jobTypeId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public String getLevelBonusDescription(int level) {
        return String.format("Bonus niveau %d: +%d%% de récompense", level, level);
    }
}