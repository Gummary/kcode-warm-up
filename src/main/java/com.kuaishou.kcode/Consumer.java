package com.kuaishou.kcode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class Consumer implements Runnable{

    private final ArrayBlockingQueue<char []> blockingQueue;
    private final ConcurrentHashMap<String, HashMap<Long, ArrayList<Integer>>> concurrentHashMap;
    private final Signal signal;

    public Consumer(ArrayBlockingQueue<char[]> queue, ConcurrentHashMap<String, HashMap<Long, ArrayList<Integer>>> map, Signal signal) {
        this.blockingQueue = queue;
        this.concurrentHashMap = map;
        this.signal = signal;
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

    @Override
    public void run() {
        while (true) {
            char[] data = blockingQueue.poll();

            if (data != null)  {
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
                        startMessageIdx = i+1;
                        concurrentHashMap.compute(methodName, (key, value)->{
                            if (value == null) {
                                value = new HashMap<>();
                            }
                            ArrayList<Integer> l =  value.getOrDefault(timestamp, new ArrayList<>(2<<20));
                            int pos = this.binarySearch(l, responseTime);
                            l.add(pos, responseTime);
                            value.put(timestamp, l);
                            return value;
                        });
                    }
                }
            }
            else if(this.signal.isNoData()) {
                break;
            }
        }
    }
}
