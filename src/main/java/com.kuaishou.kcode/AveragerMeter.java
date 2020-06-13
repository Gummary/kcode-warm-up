package com.kuaishou.kcode;

public class AveragerMeter {
    private double sum = 0;
    private int count = 0;

    public double getSum() {
        return sum;
    }

    public double getAverage() {
        return average;
    }

    private double average = 0;

    public void Update(int val) {
        Update((double)val);
    }
    public void Update(Long val) {
        Update((double)val);
    }

    public void Update(double val) {
        this.sum += val;
        this.count += 1;
        this.average = sum / count;
    }

}
