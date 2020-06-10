package com.kuaishou.kcode;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

public class Consumer implements Runnable{

    private final ArrayBlockingQueue<char []> blockingQueue;
    private final HashMap<String, HashMap<Long, String>> resultMap;
    private final Signal signal;
    private Long currentTimestamp;
    private ArrayDeque<Log> allLogs;

    public Consumer(ArrayBlockingQueue<char[]> queue,
                    HashMap<String, HashMap<Long, String>> map,
                    Signal signal) {
        this.blockingQueue = queue;
        resultMap = map;
        this.signal = signal;
        currentTimestamp = -1L;
        allLogs = new ArrayDeque<>();
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
            double sum = 0;
            ArrayList<Log> logs = map.get(methodName);
            logs.sort(Comparator.comparingInt(Log::getResponseTime));
            for (Log l :
                    logs) {
                sum += l.getResponseTime();
            }

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
    }

    @Override
    public void run() {
        while (true) {
            char[] data = blockingQueue.poll();

            if (data != null)  {
//                Long start = System.currentTimeMillis();
                int dataLength = data.length;
                int startMessageIdx = 0;
                int secondDotIdx = 0;

                for (int i = 0; i < dataLength; i++) {
                    if (data[i] == '\0') {
                        break;
                    } else if (data[i] == '\n') {
                        Long timestamp = Long.parseLong(new String(data, startMessageIdx,10));
                        for(int  j = i;j > startMessageIdx; j--){
                            if(data[j] ==','){
                                secondDotIdx = j;
                                break;
                            }
                        }
                        String methodName = new String(data, startMessageIdx + 14, secondDotIdx - startMessageIdx - 14);

                        int responseTime = Integer.parseInt(new String(data, secondDotIdx + 1, i - secondDotIdx - 1));
                        Log log = new Log(methodName, responseTime);
                        if(!currentTimestamp.equals(timestamp)) {
                            calculateResult();
                            currentTimestamp = timestamp;
                        }
                        allLogs.push(log);

                        startMessageIdx = i+1;
                    }
                }
//                System.out.println(System.currentTimeMillis() - start);
            }
            else if(this.signal.isNoData()) {
                calculateResult();
                break;
            } else {
//                System.out.println("Waiting for data");
            }
        }
    }
}
