package com.supermap.blockchain.sdk;

import org.hyperledger.fabric.protos.peer.Query;
import org.hyperledger.fabric.sdk.BlockEvent;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.TransactionRequest;

import java.io.File;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Supermap 链码管理接口，用于安装、实例化、升级链码等。
 *
 * 链码即执行业务逻辑的“智能合约”，该接口主要用于管理链码，调用链
 * 码（即执行交易）请参考 SmTransaction 接口。
 */
public interface SmChaincode {

    /**
     * 安装链码
     * @param chaincodeName 链码名
     * @param chaincodeVersion 链码版本
     * @param chaincodePath 链码源码路径
     * @param type 链码开发语言，包括 java，node，go
     * @return 是否安装成功
     */
    boolean install(
            String chaincodeName,
            String chaincodeVersion,
            String chaincodePath,
            TransactionRequest.Type type
    );

    /**
     * 实例化链码
     * @param chaincodeName 链码名
     * @param chaincodeVersion 链码版本
     * @param chaincodePath 链码源码路径
     * @param chaincodeEndorsementPolicyFile 链码背书策略配置文件
     * @param type 链码开发语言
     * @return 交易时间集合
     */
    CompletableFuture<BlockEvent.TransactionEvent> instantiate(
            String chaincodeName,
            String chaincodeVersion,
            String chaincodePath,
            File chaincodeEndorsementPolicyFile,
            TransactionRequest.Type type
    );

    /**
     * 更新链码
     * @param chaincodeName 链码名
     * @param chaincodeVersion 链码版本
     * @param chaincodePath 链码源码路径
     * @param chaincodeEndorsementPolicyFile 链码背书策略配置文件
     * @param type 链码开发语言
     * @return 交易时间集合
     */
    CompletableFuture<BlockEvent.TransactionEvent> upgrade(
            String chaincodeName,
            String chaincodeVersion,
            String chaincodePath,
            File chaincodeEndorsementPolicyFile,
            TransactionRequest.Type type
    );

    /**
     * 获取某个节点已安装的链码列表
     * @param peer 节点
     * @return 链码信息集合
     */
    List<Query.ChaincodeInfo> listInstalled(Peer peer);

    /**
     * 获取某个节点已实例化的链码列表
     * @param peer 节点
     * @return 链码信息集合
     */
    List<Query.ChaincodeInfo> listInstantiated(Peer peer);

    // Fabric sdk java not support.
    // public String pack();
    // public String signpackage();
}
