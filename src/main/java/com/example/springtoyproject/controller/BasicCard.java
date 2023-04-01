package com.example.springtoyproject.controller;

import lombok.Builder;
import lombok.Getter;
import org.json.JSONArray;
import org.json.JSONObject;

@Getter
@Builder
public class BasicCard {
    private String title;
    private String description;
    private String imageUrl;
    private String messageLabel;
    private String messageText;
    private String linkLabel;
    private String linkUrl;

    public JSONObject toJson() {
        JSONObject Box = new JSONObject();
        Box.put("title", title);
        Box.put("description", description);
        Box.put("thumbnail", new JSONObject().put("imageUrl", imageUrl));

        JSONArray buttons = new JSONArray();
        buttons.put(new JSONObject().put("action", "message").put("label", messageLabel).put("messageText", messageText));
        buttons.put(new JSONObject().put("action", "webLink").put("label", linkLabel).put("webLinkUrl", linkUrl));
        Box.put("buttons", buttons);

        return Box;
    }
}

