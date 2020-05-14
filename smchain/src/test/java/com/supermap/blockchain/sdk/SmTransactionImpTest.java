package com.supermap.blockchain.sdk;

import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Vector;

public class SmTransactionImpTest {
    private static final String channelName = "txchannel";
    private static final String chaincodeName = "endorsercc";
    private File networkFile = new File(this.getClass().getResource("/network-config-testB0.yaml").getPath());
    private SmChain smChain;

    public SmTransactionImpTest() {
        smChain = SmChain.getChain(channelName, networkFile);
    }

    @Test
    public void testQuery() {
        try {
            long startTime = System.currentTimeMillis();
            String result = smChain.getTransaction().queryByString(
                    chaincodeName,
                    "GetRecordByKey",
                    new String[]{"key1", "key2", "key3", "key4", "4"}   // 最后一个数字表示该请求中包含多少交易
            );
            System.out.println("result:" + result);
            long endTime = System.currentTimeMillis();
            System.out.println("总时间：" + (endTime - startTime) + "ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testInvoke() {
        try {
            long startTime = System.currentTimeMillis();
            String result = smChain.getTransaction().invokeByString(
                    chaincodeName,
                    "PutRecord",
                    new String[]{"key3", "value3", "1"}
            );
            System.out.println(result);
            long endTime = System.currentTimeMillis();
            System.out.println("总时间：" + (endTime - startTime) + "ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testPutManyRecord() {
        long startTime = System.currentTimeMillis();
        System.out.println(startTime);
        int loop = 2000;
        for (int i = 0; i < loop; i++) {
            String key = "key";
            try {
                String result = smChain.getTransaction().invokeByString(
                        chaincodeName,
                        "PutRecord",
                        new String[]{key + i, "value" + i}
                );
//                System.out.println(i + ": " + result);
//                Assert.assertNotEquals("", result);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        long endTime = System.currentTimeMillis();
        System.out.println(endTime);
        System.out.println("Time: " + (endTime - startTime) / 1000 + " ,TPS: " + 1000.0 * loop / (endTime - startTime));
    }

    @Test
    public void testGetManyRecord() {
        long startTime = System.currentTimeMillis();
        int loop = 2000;
        for (int i = 0; i < loop; i++) {
            String key = "key" + i;
            try {
                String result = smChain.getTransaction().queryByString(
                        chaincodeName,
                        "GetRecordByKey",
                        new String[]{key}
                );
                System.out.println(key + ": " + result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        long endTime = System.currentTimeMillis();
        System.out.println("Time: " + (endTime - startTime) + "ms ,TPS: " + 1000.0 * loop / (endTime - startTime));
    }

    @Test
    public void testGetRecordByRange() {
        long startTime = System.currentTimeMillis();
        // Query by lexical order
        String startKey = "k100";
        String endKey = "k1009999";
        try {
            byte[][] result = smChain.getTransaction().queryByByte(
                    chaincodeName,
                    "GetRecordByKeyRange",
                    new byte[][]{startKey.getBytes(), endKey.getBytes()}
            );
            for (byte[] res : result) {
                System.out.println(startKey + ": " + new String(res));
            }
            Assert.assertNotEquals("", result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Time: " + (endTime - startTime) / 1000);
    }

    @Test
    public void testHistory() {
        try {
            String result = smChain.getTransaction().queryByString(
                    chaincodeName,
                    "GetHistoryByKey",
                    new String[]{"historyKey"}
            );
            System.out.println("result:" + result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static int TX_COUNT = 1000;

    @Test
    public void multipleThreadInvoke() throws InterruptedException {
        int tNum = 128;
        long startTime = System.currentTimeMillis();
        Vector<Thread> threads = new Vector<>();
        for (int i = 0; i < tNum; i++) {
            Thread thread = new Thread(new ThreadInvoke("k" + i));
            thread.start();
            threads.add(thread);
        }

        for (Thread t : threads) {
            t.join();
        }
        long totalTime = (System.currentTimeMillis() - startTime) / 1000L;
        System.out.println("总耗时：" + totalTime);
        System.out.println("TPS：" + tNum * TX_COUNT / totalTime);

        System.out.println("main exit");

    }

    class ThreadInvoke implements Runnable{
        private String key;
        public ThreadInvoke(String key) {
            this.key = key;
        }

        @Override
        public void run() {
            try {
                for (int i = 0; i < TX_COUNT; i++) {
                    smChain.getTransaction().invokeByString(
                            chaincodeName,
                            "GetRecord",
                            new String[]{key + i, "value" + i}
                    );
                    if (i % 500 == 0) {
                        System.out.println(key + "第" + i + "次输出");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}