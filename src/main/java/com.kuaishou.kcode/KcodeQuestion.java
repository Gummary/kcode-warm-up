package com.kuaishou.kcode;

import java.io.*;
import java.util.HashMap;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kcode
 * Created on 2020-05-20
 */
public class KcodeQuestion {

    private final HashMap<String, HashMap<Long, String>> logMap;
    private final ArrayBlockingQueue<char[]> queue;
    private ArrayBlockingQueue<TimeStampLog> tsqueue;


    public KcodeQuestion() {
        logMap = new HashMap<>(2<<8);
        queue = new ArrayBlockingQueue<>(1024);
        tsqueue = new ArrayBlockingQueue<>(64);
    }

    /**
     * prepare() 方法用来接受输入数据集，数据集格式参考README.md
     *
     * @param inputStream
     */
    public void prepare(InputStream inputStream) throws Exception {

        Thread producer = new Thread(new Producer(inputStream, queue));
        Thread consumer = new Thread(new Consumer(queue, tsqueue));
        Thread logSorter1 = new Thread(new LogSorter(tsqueue, logMap));
        producer.start();
        consumer.start();
        logSorter1.start();

        producer.join();
        consumer.join();
        logSorter1.join();

    }

     /**
     * getResult() 方法是由kcode评测系统调用，是评测程序正确性的一部分，请按照题目要求返回正确数据
     * 输入格式和输出格式参考 README.md
     *
     * @param timestamp 秒级时间戳
     * @param methodName 方法名称
     */
    public String getResult(Long timestamp, String methodName) {
        return logMap.get(methodName).get(timestamp);
    }

}
