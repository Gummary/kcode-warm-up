package com.kuaishou.kcode;

public class Log implements Comparable{
    private final Long timeStamp;
    private final String methodName;

    public Long getTimeStamp() {
        return timeStamp;
    }

    public String getMethodName() {
        return methodName;
    }

    public int getResponseTime() {
        return responseTime;
    }

    private final int responseTime;

    public Log(Long timeStamp, String methodName, int responseTime) {
        this.timeStamp = timeStamp;
        this.methodName = methodName;
        this.responseTime = responseTime;
    }

    @Override
    public int compareTo(Object o) {
        return responseTime - ((Log)o).getResponseTime();
    }
}
