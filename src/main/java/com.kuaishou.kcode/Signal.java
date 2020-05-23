package com.kuaishou.kcode;

public class Signal {
    private boolean noData = false;

    public synchronized void setNoData(boolean noData) {
        this.noData = noData;
    }

    public synchronized boolean isNoData(){
        return noData == true;
    }
}
