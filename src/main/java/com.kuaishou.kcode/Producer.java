package com.kuaishou.kcode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class Producer implements Runnable{

    private final ArrayBlockingQueue<char[]> blockingQueue;
    private final BufferedReader bufferedReader;
    private static final int BUFFERSIZE = 1024;
    private final ConcurrentHashMap<String, String> runningInfo;
    private AveragerMeter readAM;

    public Producer(InputStream is,
                    ArrayBlockingQueue<char[]> queue,
                    ConcurrentHashMap<String, String> runningInfo) {
        this.bufferedReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.US_ASCII));
        this.blockingQueue = queue;
        this.runningInfo = runningInfo;
        readAM = new AveragerMeter();
    }
    public Producer(InputStream is,
                    ArrayBlockingQueue<char[]> queue) {
        this.bufferedReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.US_ASCII));
        this.blockingQueue = queue;
        this.runningInfo = null;
        readAM = new AveragerMeter();
    }


    @Override
    public void run() {
        int offset = 0;
        int bufferRemain = BUFFERSIZE-offset;
        try {
            char[] buffer = new char[BUFFERSIZE];
//            Long start = System.currentTimeMillis();
            while(this.bufferedReader.read(buffer, offset, bufferRemain) > 0) {
                char[] tmpbuffer;
                int last_index = BUFFERSIZE - 1;
                while(buffer[last_index] != '\n') {
                    last_index -= 1;
                }

                int roundSize = BUFFERSIZE - last_index - 1;
                this.blockingQueue.put(buffer);
                // index
                offset = roundSize;
                // length
                bufferRemain = BUFFERSIZE - roundSize;
                tmpbuffer = buffer;
                buffer = new char[BUFFERSIZE];
                if (roundSize >= 0) {
                    System.arraycopy(tmpbuffer, last_index + 1, buffer, 0, roundSize);
                }
            }
            this.blockingQueue.put(buffer);
            Signal.NODATA = true;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
