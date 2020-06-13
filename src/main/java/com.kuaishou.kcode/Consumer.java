package com.kuaishou.kcode;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class Consumer implements Runnable{

    private final ArrayBlockingQueue<char []> blockingQueue;
    private final HashMap<String, HashMap<Long, String>> resultMap;
    private Long currentTimestamp;
    private final ArrayDeque<Log> allLogs;
    private final HashMap<String, Integer> sumMap;
    private final ConcurrentHashMap<String, String> runningInfo;
    private AveragerMeter calResultAM;
    private AveragerMeter processBlockAM;

    public Consumer(ArrayBlockingQueue<char[]> queue,
                    HashMap<String, HashMap<Long, String>> map,
                    ConcurrentHashMap<String, String> runningInfo) {
        this.blockingQueue = queue;
        resultMap = map;
        currentTimestamp = -1L;
        allLogs = new ArrayDeque<>();
        sumMap = new HashMap<>();
        this.runningInfo = runningInfo;
        calResultAM = new AveragerMeter();
        processBlockAM = new AveragerMeter();
    }

    public Consumer(ArrayBlockingQueue<char[]> queue,
                    HashMap<String, HashMap<Long, String>> map) {
        this.blockingQueue = queue;
        resultMap = map;
        currentTimestamp = -1L;
        allLogs = new ArrayDeque<>();
        sumMap = new HashMap<>();
        runningInfo = null;
        calResultAM = new AveragerMeter();
        processBlockAM = new AveragerMeter();
    }


    private int binarySearch(ArrayList<Integer> list, Integer value) {
        int low = 0;
        int high = list.size() - 1;

        while (low <= high) {
            int mid = (low + high) / 2;
            Integer midVal = list.get(mid);
            int cmp = midVal.compareTo(value);
            if (cmp < 0) {
                low = mid + 1;
            } else if (cmp > 0) {
                high = mid - 1;
            } else {
                return mid;
            }
        }
        return low;
    }

    private void calculateResult() {
        if(currentTimestamp.equals(-1L)){
            return;
        }
        Long start = System.currentTimeMillis();
        HashMap<String, ArrayList<Log>> map = new HashMap<>();
        while(!allLogs.isEmpty()) {
            Log log = allLogs.pop();
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
            double sum = (double)sumMap.get(methodName);
            ArrayList<Log> logs = map.get(methodName);
            logs.sort(Comparator.comparingInt(Log::getResponseTime));

            int qps = logs.size();
            int p99_idx = (int) Math.ceil((double)logs.size()*0.99)-1;
            int p50_idx = (int)Math.ceil((double)logs.size()*0.5)-1;
            int p99 = logs.get(p99_idx).getResponseTime();
            int p50 = logs.get(p50_idx).getResponseTime();
            int avg = (int) Math.ceil(sum / (double) logs.size());
            int max = logs.get(logs.size()-1).getResponseTime();

            String result = String.valueOf(qps) + ',' + p99 + ',' + p50 + ',' + avg + ',' + max;
            HashMap<Long, String> methodQPSMap = resultMap.getOrDefault(methodName, new HashMap<>(4200));
            methodQPSMap.put(currentTimestamp, result);
            resultMap.put(methodName, methodQPSMap);
        }
        calResultAM.Update(System.currentTimeMillis() - start);
    }

    @Override
    public void run() {
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
                        sumMap.replaceAll((k, v)->0);
                    }
                    int sum = sumMap.getOrDefault(methodName, 0);
                    sumMap.put(methodName, sum+responseTime);
                    allLogs.push(log);

                    startMessageIdx = i + 1;
                }
            }
        }
    }
}
