package com.kuaishou.kcode;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author kcode
 * Created on 2020-05-20
 */
public class KcodeMain {

    public static void main(String[] args) throws Exception {
        InputStream fileInputStream = new FileInputStream(args[0]);
        KcodeQuestion question = new KcodeQuestion();
        long startTime = System.currentTimeMillis();
        question.prepare(fileInputStream);
        long endTime = System.currentTimeMillis();
        System.out.println("Prepare time: " + (endTime-startTime));

        InputStream resultInputStream = new FileInputStream(args[1]);
        BufferedReader br = new BufferedReader(new InputStreamReader(resultInputStream));
        String line;
        long total = 0;
        while((line = br.readLine()) != null) {
            String[] result = line.split("\\|");
            String[] query = result[0].split(",");
            startTime = System.currentTimeMillis();
            String ret = question.getResult(Long.parseLong(query[0]), query[1]);
            total += System.currentTimeMillis() - startTime;
            if(!ret.equals(result[1])) {
                System.out.println(result[0]);
                System.out.println(ret);
                System.out.println(result[1]);
                question.debugGetResult(Long.parseLong(query[0]), query[1]);
                System.out.println("-----------");
            }
        }
        System.out.println("Query Time: " + total);

        fileInputStream.close();
        resultInputStream.close();

    }
}