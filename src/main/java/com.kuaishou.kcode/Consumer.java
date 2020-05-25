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
                StringBuilder stringBuilder = new StringBuilder();
                for (char c :
                        data) {
                    if (c == '\0') {
                        break;
                    } else if (c == '\n') {
                        String[] s = stringBuilder.toString().split(",");
                        s[0] = s[0].substring(0, s[0].length()-3);
                        String methodName = s[1];
                        int responseTime = Integer.parseInt(s[2]);
                        String queryKey = methodName + s[0];
                        concurrentHashMap.compute(queryKey, (key, value)->{
                                if (value == null) {
                                    value = new ArrayList<>();
                                }
                                int pos = this.binarySearch(value, responseTime);
                                value.add(pos, responseTime);
                                return value;
                            });
                        stringBuilder.setLength(0);
                    } else {
                        stringBuilder.append(c);
                    }
                }
            }
            else if(this.signal.isNoData()) {
                break;
            }
        }
    }
}
