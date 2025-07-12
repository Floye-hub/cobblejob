package com.floye.cobblejob.util;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonHelper {

    private static final Gson GSON = new Gson();

    public static JsonObject readJsonFile(Path path) throws IOException {
        if (!Files.exists(path)) {
            return null;
        }
        String jsonString = Files.readString(path);
        if (jsonString.isEmpty()) {
            return null;
        }
        try {
            return GSON.fromJson(jsonString, JsonObject.class);
        } catch (Exception e) {
            return null;
        }
    }

    public static void writeJsonFile(Path path, JsonObject json) throws IOException {
        Files.createDirectories(path.getParent());
        String jsonString = GSON.toJson(json);
        Files.writeString(path, jsonString);
    }
}