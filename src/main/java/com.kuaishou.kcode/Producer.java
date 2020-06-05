package com.kuaishou.kcode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;

public class Producer implements Runnable{

    private final ArrayBlockingQueue<LogContainer> blockingQueue;
    private final BufferedReader bufferedReader;
    private final Signal signal;
    private static final int BUFFERSIZE = 10 * 1024 * 1024;

    public Producer(InputStream is, ArrayBlockingQueue<LogContainer> queue, Signal signal) {
        this.bufferedReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.US_ASCII));
        this.blockingQueue = queue;
        this.signal = signal;
    }

    private void addToArray(Long timeStamp, HashMap<String, ArrayList<Integer>> logs)
    {
        if(timeStamp == -1) {
            return;
        }
        LogContainer lc = new LogContainer(timeStamp, logs);
        this.blockingQueue.add(lc);
    }

    @Override
    public void run() {
        String line = null;
        Long currentTimeStamp = -1L;
        HashMap<String, ArrayList<Integer>> logs = null;
        try {
            while((line = bufferedReader.readLine()) != null) {
                String[] logInfo = line.split(",");
                Long timeStamp = Long.parseLong(logInfo[0].substring(0, 10));
                String methoName = logInfo[1];
                int responseTime = Integer.parseInt(logInfo[2]);
                if(!currentTimeStamp.equals(timeStamp)) {
                    addToArray(currentTimeStamp, logs);
                    currentTimeStamp = timeStamp;
                    logs = new HashMap<>();
                }
                ArrayList<Integer> respTimes = logs.getOrDefault(methoName, new ArrayList<>());
                respTimes.add(responseTime);
                logs.put(methoName, respTimes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.signal.setNoData(true);
    }
}
