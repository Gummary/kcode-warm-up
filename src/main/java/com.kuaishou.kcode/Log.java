package com.kuaishou.kcode;

public class Log {

    private final String methodName;
    private final int responseTime;

    public String getMethodName() {
        return methodName;
    }

    public int getResponseTime() {
        return responseTime;
    }


    public Log(String methodName, int responseTime) {
        this.methodName = methodName;
        this.responseTime = responseTime;
    }

}
