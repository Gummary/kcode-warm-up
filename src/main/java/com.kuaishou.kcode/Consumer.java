package com.kuaishou.kcode;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class Consumer implements Runnable{

    private final ArrayBlockingQueue<char []> blockingQueue;
    private final ArrayBlockingQueue<TimeStampLog> tsQueue;
    private Long currentTimestamp;
    private ArrayList<Log> allLogs;

    public Consumer(ArrayBlockingQueue<char[]> queue,
                    ArrayBlockingQueue<TimeStampLog> tsqueue) {
        this.blockingQueue = queue;
        tsQueue = tsqueue;
        currentTimestamp = -1L;
        allLogs = null;
    }

    private void calculateResult() {
        try {
            tsQueue.put(new TimeStampLog(allLogs, currentTimestamp));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void run() {
        char[] data = new char[0];
        while (true) {
            data = blockingQueue.poll();
            if(data == null){
                if(Signal.NODATA) {
                    break;
                }
                continue;
            }
            int startMessageIdx = 0;
            int secondDotIdx = 0;

            for (int i = 0; i < data.length; i++) {
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
        Signal.NOTIMESTAMP = true;
//        this.runningInfo.put("Consumer", "Calculate Period"+calResultAM.getAverage() + "Calculate Total"+calResultAM.getSum());
//        this.runningInfo.put("consumer",
//                        "Calculate Time Avg:"+calResultAM.getAverage() +
//                        " Calculate Time Sum:" + calResultAM.getSum() +
//                        " Process Time Avg:" + processBlockAM.getAverage() +
//                        " Process Time sum:" + processBlockAM.getSum()
//                );

    }
}
