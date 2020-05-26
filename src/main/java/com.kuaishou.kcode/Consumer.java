package com.kuaishou.kcode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class Consumer implements Runnable{

    private final ArrayBlockingQueue<char []> blockingQueue;
    private final ConcurrentHashMap<String, ArrayList<Integer>> concurrentHashMap;
    private final Signal signal;

    public Consumer(ArrayBlockingQueue<char[]> queue, ConcurrentHashMap<String, ArrayList<Integer>> map, Signal signal) {
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
                        String timestamp = new String(data, startMessageIdx,10);
                        for(int  j = i;j > startMessageIdx; j--){
                            if(data[j] ==','){
                                secondDotIdx = j;
                                break;
                            }
                        }
                        String methodName = new String(data, startMessageIdx + 14, secondDotIdx - startMessageIdx - 14);

                        int responseTime = Integer.parseInt(new String(data, secondDotIdx + 1, i - secondDotIdx - 1));
                        String queryKey = methodName + timestamp;
                        startMessageIdx = i+1;
                        concurrentHashMap.compute(queryKey, (key, value)->{
                                if (value == null) {
                                    value = new ArrayList<>();
                                }
                                int pos = this.binarySearch(value, responseTime);
                                value.add(pos, responseTime);
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
