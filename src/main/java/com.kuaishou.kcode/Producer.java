package com.kuaishou.kcode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;

public class Producer implements Runnable{

    private final ArrayBlockingQueue<char[]> blockingQueue;
    private final BufferedReader bufferedReader;
    private static final int BUFFERSIZE = 512;

    public Producer(InputStream is, ArrayBlockingQueue<char[]> queue) {
        this.bufferedReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.US_ASCII));
        this.blockingQueue = queue;
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
//                System.out.println(System.currentTimeMillis() - start);
//                start = System.currentTimeMillis();
            }
            this.blockingQueue.put(buffer);
            this.blockingQueue.put(new char[5]);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
