package com.kuaishou.kcode;

import java.util.ArrayList;
import java.util.HashMap;

public class LogContainer {
    private Long timeStamp;

    public Long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public HashMap<String, ArrayList<Integer>> getLogs() {
        return logs;
    }

    public void setLogs(HashMap<String, ArrayList<Integer>> logs) {
        this.logs = logs;
    }

    private HashMap<String, ArrayList<Integer>> logs;

    public LogContainer(Long timeStamp, HashMap<String, ArrayList<Integer>> logs) {
        this.timeStamp = timeStamp;
        this.logs = logs;
    }
}
