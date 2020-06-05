package com.kuaishou.kcode;

public class Operation {
    //1587987945950,mockUser2,43
    private long timeStampMs;
    private String methodName;
    private int duration;

    public Operation() {
    }

    public Operation(long timeStampMs, String methodName, int duration) {
        this.timeStampMs = timeStampMs;
        this.methodName = methodName;
        this.duration = duration;
    }

    public long getTimeStampMs() {
        return timeStampMs;
    }

    public String getMethodName() {
        return methodName;
    }

    public int getDuration() {
        return duration;
    }

    public Operation setTimeStampMs(long timeStampMs) {
        this.timeStampMs = timeStampMs;
        return this;
    }

    public Operation setMethodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    public Operation setDuration(int duration) {
        this.duration = duration;
        return this;
    }
}