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

    private final ConcurrentHashMap<String, HashMap<Long, String>> resultMap;
    private static final int NUM_THREAD = 16;


    public KcodeQuestion() {
        resultMap = new ConcurrentHashMap<>();
    }


//    日志的时间戳是顺序排列的，如何利用这个特点加速
    /**
     * prepare() 方法用来接受输入数据集，数据集格式参考README.md
     *
     * @param inputStream
     */
    public void prepare(InputStream inputStream) {
        ArrayBlockingQueue<LogContainer> queue = new ArrayBlockingQueue<>(NUM_THREAD);
        Signal signal = new Signal();
        Thread producer = new Thread(new Producer(inputStream, queue, signal));
        producer.start();
        Thread[] consumers = new Thread[NUM_THREAD];
        for (int i = 0; i < consumers.length; i++) {
            consumers[i]  = new Thread(new Consumer(resultMap, queue, signal));
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
    }

     /**
     * getResult() 方法是由kcode评测系统调用，是评测程序正确性的一部分，请按照题目要求返回正确数据
     * 输入格式和输出格式参考 README.md
     *
     * @param timestamp 秒级时间戳
     * @param methodName 方法名称
     */
    public String getResult(Long timestamp, String methodName) {
        return resultMap.get(methodName).get(timestamp);
    }

    public void debugGetResult(Long timestamp, String methodName) {

    }



}
