package com.kuaishou.kcode;

import sun.rmi.runtime.Log;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author kcode
 * Created on 2020-05-20
 */
public class KcodeQuestion {

    private final ConcurrentHashMap<String, HashMap<Long, ArrayList<Integer>>> logMap;
    private static final int NUM_THREAD = 5;


    public KcodeQuestion() {
        logMap = new ConcurrentHashMap<>(2<<7);
    }

    /**
     * prepare() 方法用来接受输入数据集，数据集格式参考README.md
     *
     * @param inputStream
     */
    public void prepare(InputStream inputStream) {
        ArrayBlockingQueue<char[]> queue = new ArrayBlockingQueue<>(NUM_THREAD*2);
        Signal signal = new Signal();
        Thread producer = new Thread(new Producer(inputStream, queue, signal));
        producer.start();
        Thread[] consumers = new Thread[NUM_THREAD];
        for (int i = 0; i < consumers.length; i++) {
            consumers[i]  = new Thread(new Consumer(queue, this.logMap, signal));
            consumers[i].start();
        }

        try {
            producer.join();
            for (Thread t :
                    consumers) {
                t.join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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
        // do something
        HashMap<Long, ArrayList<Integer>> logs = this.logMap.get(methodName);
        ArrayList<Integer> responseTimes = logs.get(timestamp);

        int qps = responseTimes.size();
//        Collections.sort(responseTimes);
        int sum = 0;
        for (Integer responseTime:
                responseTimes) {
            sum += responseTime;
        }
        int p99_idx = (int) Math.ceil((double)responseTimes.size()*0.99)-1;
        int p50_idx = (int)Math.ceil((double)responseTimes.size()*0.5)-1;
        int p99 = responseTimes.get(p99_idx);
        int p50 = responseTimes.get(p50_idx);
        int avg = (int) Math.ceil((double)sum / (double) responseTimes.size());
        int max = responseTimes.get(responseTimes.size()-1);

        return String.valueOf(qps) +
                ',' +
                p99 +
                ',' +
                p50 +
                ',' +
                avg +
                ',' +
                max;
    }

    public void debugGetResult(Long timestamp, String methodName) {
        // do something
        HashMap<Long, ArrayList<Integer>> logs = this.logMap.get(methodName);
        ArrayList<Integer> responseTimes = logs.get(timestamp);

        int qps = responseTimes.size();
        Collections.sort(responseTimes);
        int sum = 0;
        for (Integer responseTime:
                responseTimes) {
            sum += responseTime;
        }
        int p99_idx = (int) Math.ceil((double)responseTimes.size()*0.99);
        int p50_idx = (int)Math.ceil((double)responseTimes.size()*0.5);
        int p99 = responseTimes.get(p99_idx);
        int p50 = responseTimes.get(p50_idx);
        int avg = (int) Math.ceil((double)sum / (double) responseTimes.size());
        int max = responseTimes.get(responseTimes.size()-1);

//        String info = "P50 index: " + p50_idx + ","
//                + "P50: " + responseTimes.get(p50_idx) + ","
//                + "P50 index-1: " + responseTimes.get(p50_idx-1) + ","
//                + "P50 index+1: " + responseTimes.get(p50_idx+1) + ",";
//        System.out.println(info);
    }



}
