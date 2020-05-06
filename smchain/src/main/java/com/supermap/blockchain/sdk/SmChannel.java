package com.supermap.blockchain.sdk;

import org.hyperledger.fabric.sdk.Peer;

import java.util.Set;

/**
 * Supermap 通道管理接口，用于获取通道区块信息，查询特定节点所加入的通道等。
 *
 * Fabric 中一个通道中可以加入多个组织形成一个“联盟”，每个通道拥有一套账本，
 * 加入不同通道的同一个节点，会在本地保存两份区块链账本，但是两套账本互相隔离。
 */
public interface SmChannel {

    /**
     * 获取区块链信息
     * @return Supermap 区块链信息
     */
    SmBlockchainInfo getBlockchainInfo();

    /**
     * 根据区块链号获取指定节点上的区块信息
     * @param peer 节点
     * @param number 区块号
     * @return Supermap区块信息
     */
    SmBlockInfo getBlockInfoByNumber(Peer peer, long number);

    /**
     * 获取指定节点所加入的通道
     * @param peer 节点
     * @return 通道名列表
     */
    Set<String> listChannelOfPeerJoined(Peer peer);
}
