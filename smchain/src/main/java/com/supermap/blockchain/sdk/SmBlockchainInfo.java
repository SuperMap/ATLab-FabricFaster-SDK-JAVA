package com.supermap.blockchain.sdk;

/**
 * Supermap 区块链信息类，默认包含最新的区块信息
 */
public class SmBlockchainInfo extends SmBlockInfo {

    /**
     * 当前区块总高度
     */
    private long height;

    public long getHeight() {
        return height;
    }

    void setHeight(long height) {
        this.height = height;
    }
}
