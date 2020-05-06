package com.supermap.blockchain.sdk;

/**
 * 交易处理接口，用于写入、查询记录，并提供了 String 和 Byte 两种格式。
 *
 * 交易过程由链码进行处理，所以执行交易时，需要指明所用链码名以及链码中的方法名，
 * 另外还要传入链码方法所需的参数。链码、链码名、链码方法参数请查询链码。
 *
 * 链码是根据不用的业务需求所开发的“智能合约”，不同应用的链码也会不同，所谓“交易”，
 * 也就是调用链码执行特定业务逻辑的过程，所以执行“交易”需要根据链码设定的“规则”进行。
 */
public interface SmTransaction {
    /**
     * 查询交易，该操作不会改变链上数据
     * @param chaincodeName 链码名
     * @param functionName 方法名
     * @param args 方法参数
     * @return 查询结果字符串
     */
    String queryByString(String chaincodeName, String functionName, String[] args);

    /**
     * 以字节方式查询交易，该操作不会改变链上数据
     * @param chaincodeName 链码名
     * @param functionName 方法名
     * @param args 方法参数
     * @return 查询结果字节数组
     */
    byte[][] queryByByte(String chaincodeName, String functionName, byte[][] args);

    /**
     * 执行交易，该操作会向链上写入数据
     * @param chaincodeName 链码名
     * @param functionName 方法名
     * @param args 方法参数
     * @return 执行结果字符串
     */
    String invokeByString(String chaincodeName, String functionName, String[] args);

    /**
     * 以字节方式执行交易，该操作会向链上写入数据
     * @param chaincodeName 链码名
     * @param functionName 方法名
     * @param args 方法参数
     * @return 执行结果字符串
     */
    String invokeByByte(String chaincodeName, String functionName, byte[][] args);
}
