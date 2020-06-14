package com.kuaishou.kcode;

import java.util.ArrayDeque;
import java.util.ArrayList;

public class TimeStampLog {
    public ArrayList<Log> getLogs() {
        return logs;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public TimeStampLog(ArrayList<Log> logs, Long timestamp) {
        this.logs = logs;
        this.timestamp = timestamp;
    }

    private final ArrayList<Log> logs;
    private final Long timestamp;
}
