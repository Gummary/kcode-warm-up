package com.kuaishou.kcode;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class LogSorter implements Runnable{

    private final ArrayBlockingQueue<TimeStampLog> timeStampLogArrayBlockingQueue;
    private final HashMap<String, HashMap<Long, String>> resultMap;

    public LogSorter(ArrayBlockingQueue<TimeStampLog> queue, HashMap<String, HashMap<Long, String>> results) {
        timeStampLogArrayBlockingQueue = queue;
        resultMap = results;
    }


    @Override
    public void run() {
        while(true) {
            TimeStampLog tslog = timeStampLogArrayBlockingQueue.poll();
            if(tslog == null) {
                if (Signal.NOTIMESTAMP) {
                    break;
                }
                continue;
            }
            ArrayList<Log> allLogs = tslog.getLogs();
            Long currentTimestamp = tslog.getTimestamp();
            if(currentTimestamp == -1){
                continue;
            }
            HashMap<String, ArrayList<Log>> map = new HashMap<>();
            for (Log log:
                    allLogs) {
                if(map.containsKey(log.getMethodName())) {
                    ArrayList<Log> logs = map.get(log.getMethodName());
                    logs.add(log);
                } else {
                    ArrayList<Log> logs = new ArrayList<>();
                    logs.add(log);
                    map.put(log.getMethodName(), logs);
                }
            }

            for (String methodName :
                    map.keySet()) {
                ArrayList<Log> logs = map.get(methodName);
                double sum = logs.stream().mapToDouble(Log::getResponseTime).sum();

                logs.sort(Comparator.comparingInt(Log::getResponseTime));

                int qps = logs.size();
                int p99_idx = (int) Math.ceil((double)logs.size()*0.99)-1;
                int p50_idx = (int)Math.ceil((double)logs.size()*0.5)-1;
                int p99 = logs.get(p99_idx).getResponseTime();
                int p50 = logs.get(p50_idx).getResponseTime();
                int avg = (int) Math.ceil(sum / (double) logs.size());
                int max = logs.get(logs.size()-1).getResponseTime();

                String result = String.valueOf(qps) + ',' + p99 + ',' + p50 + ',' + avg + ',' + max;
                HashMap<Long, String> qpsMap = resultMap.getOrDefault(methodName, new HashMap<>(4500));
                qpsMap.put(currentTimestamp, result);
                resultMap.put(methodName, qpsMap);
            }
        }
    }
}
