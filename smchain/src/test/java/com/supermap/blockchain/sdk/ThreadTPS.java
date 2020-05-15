package com.supermap.blockchain.sdk;

import java.io.File;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class ThreadTPS {
    private static String chaincodeName = "endorsercc";
    private static String channelName = "txchannel";
    private static File networkFile = new File(ThreadTPS.class.getResource("/network-config-testB0.yaml").getFile());
//    private static String functionName = "GetRecordByKey";
//    private static String functionName = "PutRecord";
//    private static String shpPath = ThreadTPS.class.getResource("BL/BL.shp").getFile();

    public static void main(String[] args) throws InterruptedException {
        int threadCount = 1;
//        List<byte[]> arrayList = new GeoData().getGeo(shpPath);
        List<Integer> arrayList = new ArrayList<>();
        for (int i = 0; i < 500 * threadCount; i++) {
            arrayList.add(i);
        }
        int count = arrayList.size() / threadCount;
        Vector<Thread> threads = new Vector<Thread>();

        long startTime = System.currentTimeMillis();
        for (int i = 0; i < threadCount; i++) {

            int toIndex = Math.min((i + 1) * count, arrayList.size());
            List<Integer> subArray = arrayList.subList(i * count, toIndex);

            SubThreadTPS subThreadTPS = new SubThreadTPS(subArray);
            Thread thread = new Thread(subThreadTPS);
            threads.add(thread);
            thread.start();
        }

        for (Thread t : threads) {
            t.join();
        }
        long endTime = System.currentTimeMillis();
        System.out.println((endTime - startTime) / 1000.0 + " s");
        System.out.println(4 * arrayList.size() / ((endTime - startTime) / 1000.0) + " TPS");
    }

    static class SubThreadTPS implements Runnable{
        List<Integer> geoBytes;
        SubThreadTPS(List<Integer> geoBytes) {
            this.geoBytes = geoBytes;
        }


        public void run() {
//            long startTime = System.currentTimeMillis();
            boolean put = true;
            boolean get = false;

            SmChain smChain = SmChain.getChain(channelName, networkFile);
            SmTransaction smTransaction = smChain.getTransaction();
            int i = 0;
            for (Integer integer : geoBytes) {
                if (get) {
                    smTransaction.invokeByString(
                            chaincodeName,
                            "PutRecord",
                            new String[]{"key"+integer.toString(), "value"+integer.toString()});
                } else {
                    String s = smTransaction.queryByString(
                            chaincodeName,
                            "GetRecordByKey",
                            new String[]{"key" + integer, "key" + (integer + 1), "key" + (integer + 2), "key" + (integer + 3), "4"});
                    System.out.println(s);
                }
                i++;
            }

//            long endTime = System.currentTimeMillis();
//            System.out.println((endTime - startTime) / 1000.0);
        }
    }

    private static String getSHA256(String str) {
        if (str == null) {
            return null;
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.reset();
            messageDigest.update(str.getBytes());
            return byte2Hex(messageDigest.digest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // StringBuffer 建立的字符串可以进行修改，并且不产生新的未使用对象
    private static String byte2Hex(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        String temp = null;
        for (int i = 0; i < bytes.length; i++) {
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length() == 1) {
                stringBuilder.append("0");
            }
            stringBuilder.append(temp);
        }
        return stringBuilder.toString();
    }
}
