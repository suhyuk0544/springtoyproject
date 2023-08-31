package com.example.springtoyproject.config;

import org.json.JSONObject;

import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyEditor;
import java.beans.PropertyEditorSupport;

public class JsonPropertyEditor extends PropertyEditorSupport {

    @Override
    public String getAsText() {
        return JSONObject.valueToString(this.getValue());
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        this.setValue(JSONObject.stringToValue(text));
    }

}
