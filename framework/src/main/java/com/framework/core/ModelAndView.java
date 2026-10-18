package com.framework.core;

import java.util.LinkedHashMap;
import java.util.Map;

public class ModelAndView {
    private String view;
    private Map<String, Object> data;

    public ModelAndView(String view) {
        this.view = view;
        this.data = new LinkedHashMap<>();
    }

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public void setview(String view) {
        setView(view);
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data == null ? new LinkedHashMap<>() : data;
    }

    public void addAttribut(String cle, Object object) {
        data.put(cle, object);
    }

    public void addAttribute(String cle, Object object) {
        addAttribut(cle, object);
    }
}
