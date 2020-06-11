package com.kuaishou.kcode;

public class Signal {
    private volatile boolean noData = false;

    public void setNoData() {
        this.noData = true;
    }

    public boolean isNoData(){
        return noData == true;
    }
}
