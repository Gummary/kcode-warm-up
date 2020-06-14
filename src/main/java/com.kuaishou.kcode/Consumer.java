package com.kuaishou.kcode;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class Consumer implements Runnable{

    private final ArrayBlockingQueue<char []> blockingQueue;
    private final ArrayBlockingQueue<TimeStampLog> tsQueue;
    private Long currentTimestamp;
    private ArrayList<Log> allLogs;
    private final ConcurrentHashMap<String, String> runningInfo;
    private AveragerMeter calResultAM;
    private AveragerMeter processBlockAM;
    private long start = -1L;

    public Consumer(ArrayBlockingQueue<char[]> queue,
                    ArrayBlockingQueue<TimeStampLog> tsqueue,
                    ConcurrentHashMap<String, String> runningInfo) {
        this.blockingQueue = queue;
        tsQueue = tsqueue;
        currentTimestamp = -1L;
        allLogs = null;
        this.runningInfo = runningInfo;
        calResultAM = new AveragerMeter();
        processBlockAM = new AveragerMeter();
    }

    public Consumer(ArrayBlockingQueue<char[]> queue,
                    ArrayBlockingQueue<TimeStampLog> tsqueue) {
        this.blockingQueue = queue;
        tsQueue = tsqueue;
        currentTimestamp = -1L;
        allLogs = null;
        runningInfo = null;
        calResultAM = new AveragerMeter();
        processBlockAM = new AveragerMeter();
    }

    private void calculateResult() {
        if(start != -1L) {
            calResultAM.Update(System.currentTimeMillis() - start);
        }
        start = System.currentTimeMillis();
        try {
            tsQueue.put(new TimeStampLog(allLogs, currentTimestamp));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        start = System.currentTimeMillis();

        while (true) {
            char[] data = new char[0];
            try {
                data = blockingQueue.take();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int dataLength = data.length;
            if(dataLength < 10){
                break;
            }

            int startMessageIdx = 0;
            int secondDotIdx = 0;

            for (int i = 0; i < dataLength; i++) {
                if (data[i] == '\0') {
                    break;
                } else if (data[i] == '\n') {
                    Long timestamp = Long.parseLong(new String(data, startMessageIdx, 10));
                    for (int j = i; j > startMessageIdx; j--) {
                        if (data[j] == ',') {
                            secondDotIdx = j;
                            break;
                        }
                    }
                    String methodName = new String(data, startMessageIdx + 14, secondDotIdx - startMessageIdx - 14);

                    int responseTime = Integer.parseInt(new String(data, secondDotIdx + 1, i - secondDotIdx - 1));
                    Log log = new Log(methodName, responseTime);
                    if (!currentTimestamp.equals(timestamp)) {
                        calculateResult();
                        currentTimestamp = timestamp;
                        allLogs = new ArrayList<>();
                    }
                    allLogs.add(log);

                    startMessageIdx = i + 1;
                }
            }
        }
        calculateResult();
        Signal.NODATA = true;
        this.runningInfo.put("Consumer", "Calculate Period"+calResultAM.getAverage() + "Calculate Total"+calResultAM.getSum());
//        this.runningInfo.put("consumer",
//                        "Calculate Time Avg:"+calResultAM.getAverage() +
//                        " Calculate Time Sum:" + calResultAM.getSum() +
//                        " Process Time Avg:" + processBlockAM.getAverage() +
//                        " Process Time sum:" + processBlockAM.getSum()
//                );

    }
}
