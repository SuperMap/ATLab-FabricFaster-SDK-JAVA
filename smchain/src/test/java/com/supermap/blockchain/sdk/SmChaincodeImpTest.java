package com.supermap.blockchain.sdk;

import org.hyperledger.fabric.protos.peer.Query;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.TransactionRequest;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.supermap.blockchain.sdk.Utils.getHexString;

public class SmChaincodeImpTest {
    private static final String channelName = "txchannel";
    private final String networkConfigFile = this.getClass().getResource("/network-config-testB0.yaml").getFile();
    private final SmChain smChain = SmChain.getChain(channelName, new File(networkConfigFile));
    private static final String chaincodeName = "endorsercc";
    private static final String chaincodeVersion = "1.0";
    private static final String chaincodePath = "/home/cy/Documents/ATL/SuperMap/ATLab-Chaincodes/java/Common";
    private final String chaincodeEndorsementPolicyFile = this.getClass().getResource("/chaincode-endorsement-policy.yaml").getFile();

    @Test
    public void installTest() {
        boolean result = smChain.getChaincode().install(chaincodeName, chaincodeVersion, chaincodePath, TransactionRequest.Type.JAVA);
        Assert.assertTrue(result);
    }

    @Test
    public void instantiateTest() {
        CompletableFuture<BlockEvent.TransactionEvent> completableFuture = smChain.getChaincode().instantiate(chaincodeName, chaincodeVersion, chaincodePath, new File(chaincodeEndorsementPolicyFile), TransactionRequest.Type.JAVA);
        System.out.println(completableFuture.toString());
        Assert.assertTrue(completableFuture.isDone());
    }

    @Test
    public void upgradeTest() throws ExecutionException, InterruptedException {

        // 版本号自动递增
        String upgradeVersion = String.valueOf(getInstantiatedVersion(chaincodeName) + 1.0);
        boolean result = smChain.getChaincode().install(chaincodeName, upgradeVersion, chaincodePath, TransactionRequest.Type.JAVA);

        if (result) {
            CompletableFuture<BlockEvent.TransactionEvent> completableFuture = smChain.getChaincode().upgrade(chaincodeName, upgradeVersion, chaincodePath, new File(chaincodeEndorsementPolicyFile), TransactionRequest.Type.JAVA);

            // 处理交易返回事件
            completableFuture.thenApply(transactionEvent -> {
                Channel channel = smChain.getHFChannel();
                Peer peer = channel.getPeers().iterator().next();
                List<Query.ChaincodeInfo> list = smChain.getChaincode().listInstantiated(peer);
                for (Query.ChaincodeInfo info : list) {
                    if (chaincodeName.equals(info.getName())) {
                        Assert.assertTrue(upgradeVersion.equals(info.getVersion()));
                    }
                }
                return null;
            }).get();   // 等待事件处理完成
        }
    }

    @Test
    public void listInstalledTest() {
        Channel channel = smChain.getHFChannel();
        Peer peer = channel.getPeers().iterator().next();
        List<Query.ChaincodeInfo> list = smChain.getChaincode().listInstalled(peer);
        for (Query.ChaincodeInfo info : list) {
//            if (chaincodeName.equals(info.getName())) {
                System.out.println(info);
                System.out.println("name: " + info.getName());
                System.out.println("version: " + info.getVersion());
                System.out.println("id: " + getHexString(info.getId().toByteArray()));
                System.out.println();
//            }
        }
        Assert.assertTrue(list.size() > 0);
    }

    @Test
    public void listInstantiatedTest() {
        Channel channel = smChain.getHFChannel();
        Peer peer = channel.getPeers().iterator().next();
        List<Query.ChaincodeInfo> list = smChain.getChaincode().listInstantiated(peer);
        for (Query.ChaincodeInfo info : list) {
//            if (chaincodeName.equals(info.getName())) {
                System.out.println("name: " + info.getName());
                System.out.println("version: " + info.getVersion());
//            }
        }
        Assert.assertTrue(list.size() > 0);
    }

    @Test
    public void getInstantiatedVersionNull() {
        System.out.println(getInstantiatedVersion(null));
    }

    private double getInstantiatedVersion(String chaincodeName) {
        Channel channel = smChain.getHFChannel();
        Peer peer = channel.getPeers().iterator().next();
        List<Query.ChaincodeInfo> list = smChain.getChaincode().listInstantiated(peer);
        for (Query.ChaincodeInfo info : list) {
            if (chaincodeName.equals(info.getName())) {
                return Double.parseDouble(info.getVersion());
            }
        }
        return -1;
    }
}