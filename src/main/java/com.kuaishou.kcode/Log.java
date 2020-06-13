package com.kuaishou.kcode;

public class Log implements Comparable{
    private final String methodName;


    public String getMethodName() {
        return methodName;
    }

    public int getResponseTime() {
        return responseTime;
    }

    private final int responseTime;

    public Log(String methodName, int responseTime) {
        this.methodName = methodName;
        this.responseTime = responseTime;
    }

    @Override
    public int compareTo(Object o) {
        return responseTime - ((Log)o).getResponseTime();
    }
}
