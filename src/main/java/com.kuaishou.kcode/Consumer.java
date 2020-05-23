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
                        long time = Long.parseLong(s[0].substring(0, s[0].length()-3));
                        String methodName = s[1];
                        int responseTime = Integer.parseInt(s[2]);

                        if (this.concurrentHashMap.containsKey(methodName)) {
                            concurrentHashMap.computeIfPresent(methodName, (key, value) ->
                                                {
                                                    ArrayList<Integer> l =  value.getOrDefault(time, new ArrayList<>());
                                                    l.add(responseTime);
                                                    value.put(time, l);
                                                    return value;
                                                });
                        } else {
                            HashMap<Long, ArrayList<Integer>> logs = new HashMap<>();
                            ArrayList<Integer> responseTimes = new ArrayList<>();
                            responseTimes.add(responseTime);
                            logs.put(time, responseTimes);
                            concurrentHashMap.put(methodName, logs);
                        }
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
