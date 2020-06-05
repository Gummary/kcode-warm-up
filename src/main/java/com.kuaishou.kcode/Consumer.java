package com.kuaishou.kcode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class Consumer implements Runnable {

    private final ConcurrentHashMap<String, HashMap<Long, String>> result;
    private final ArrayBlockingQueue<LogContainer> queue;
    private final Signal signal;

    public Consumer(ConcurrentHashMap<String, HashMap<Long, String>> result, ArrayBlockingQueue<LogContainer> queue, Signal signal) {
        this.result = result;
        this.queue = queue;
        this.signal = signal;
    }

    private String CalculateResult(ArrayList<Integer> responseTimes){
        int qps = responseTimes.size();
        int sum = 0;
        for (Integer responseTime :
                responseTimes) {
            sum += responseTime;
        }
        int p99_idx = (int) Math.ceil((double) responseTimes.size() * 0.99) - 1;
        int p50_idx = (int) Math.ceil((double) responseTimes.size() * 0.5) - 1;
        int p99 = responseTimes.get(p99_idx);
        int p50 = responseTimes.get(p50_idx);
        int avg = (int) Math.ceil((double) sum / (double) responseTimes.size());
        int max = responseTimes.get(responseTimes.size() - 1);

        return String.valueOf(qps) + ',' + p99 + ',' + p50 + ',' + avg + ',' + max;
    }


    @Override
    public void run() {
        LogContainer lc = null;
        while (true) {
            lc = this.queue.poll();
            if (lc != null) {
                Long timeStamp = lc.getTimeStamp();
                HashMap<String, ArrayList<Integer>> currentLogs = lc.getLogs();
                for (Map.Entry<String, ArrayList<Integer>> entry :
                        currentLogs.entrySet()) {
                    String methodName = entry.getKey();
                    ArrayList<Integer> responseTimes = entry.getValue();
                    Collections.sort(responseTimes);
                    String currentResult = CalculateResult(responseTimes);
                    result.compute(methodName,(key, value)->{
                       if(value == null){
                           value = new HashMap<>();
                       }
                       value.put(timeStamp, currentResult);
                       return value;
                    });
                }
            } else if (this.signal.isNoData()) {
                break;
            }
        }
    }
}
