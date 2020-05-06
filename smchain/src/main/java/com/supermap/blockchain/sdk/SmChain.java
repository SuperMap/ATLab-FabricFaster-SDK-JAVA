package com.supermap.blockchain.sdk;


import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.NetworkConfig;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import java.io.File;

/**
 * SmChain 客户端，基于 HFClient 封装。是操作区块链的主入口
 */
public class SmChain {
    private HFClient hfClient;
    private Channel channel;
    private NetworkConfig networkConfig;

    /**
     * 通过 yaml 配置文件实例化
     * @param networkConfigFile 网络配置文件
     */
    private SmChain(String channelName, File networkConfigFile) {
        try {
            networkConfig = NetworkConfig.fromYamlFile(networkConfigFile);
            hfClient = HFClient.createNewInstance();
            hfClient.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
            hfClient.setUserContext(networkConfig.getPeerAdmin());
            channel = hfClient.loadChannelFromConfig(channelName, networkConfig);
            channel.initialize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Channel getHFChannel() {
        return channel;
    }

    public static SmChain getChain(String channelName, File networkConfigFile) {
        return new SmChain(channelName, networkConfigFile);
    }

    public SmTransaction getTransaction() {
        return new SmTransactionImp(hfClient, channel);
    }

    public SmChaincode getChaincode() {

        return new SmChaincodeImp(hfClient, channel);
    }

    public SmChannel getChannel() {
        return new SmChannelImp(hfClient, channel);
    }

    public SmCA getCa(String OrgName) {
        return new SmCAImp(networkConfig, OrgName);
    }
}
