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
    private static final int NUM_THREAD = 8;


    public KcodeQuestion() {
        logMap = new HashMap<>(2<<7);
    }

    /**
     * prepare() 方法用来接受输入数据集，数据集格式参考README.md
     *
     * @param inputStream
     */
    public void prepare(InputStream inputStream) throws Exception {


        ConcurrentHashMap<String, String> runningInfo = new ConcurrentHashMap<>();
        ArrayBlockingQueue<char[]> queue = new ArrayBlockingQueue<>(NUM_THREAD);
        Thread producer = new Thread(new Producer(inputStream, queue, runningInfo));
        Thread consumer = new Thread(new Consumer(queue, logMap, runningInfo));
        producer.start();
        consumer.start();

        producer.join();
        consumer.join();

        throw new Exception(runningInfo.get("consumer") + runningInfo.get("producer"));

//        for (Map.Entry<String, HashMap<Long, ArrayList<Integer>>> entry:
//        this.logMap.entrySet()){
//            System.out.println(entry.getKey().length());
//        }
//        System.out.println(this.logMap.size());
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
