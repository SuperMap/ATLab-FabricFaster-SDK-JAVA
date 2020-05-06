package com.supermap.blockchain.sdk;

/**
 * Supermap 区块信息类
 */
public class SmBlockInfo {
    /**
     * 当前区块Hash
     */
    private String currentBlockHash;

    /**
     * 上一个区块Hash
     */
    private String previousBlockHash;

    public String getCurrentBlockHash() {
        return currentBlockHash;
    }

    void setCurrentBlockHash(String currentBlockHash) {
        this.currentBlockHash = currentBlockHash;
    }

    public String getPreviousBlockHash() {
        return previousBlockHash;
    }

    void setPreviousBlockHash(String previousBlockHash) {
        this.previousBlockHash = previousBlockHash;
    }
}
