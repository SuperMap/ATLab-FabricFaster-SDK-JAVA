package com.supermap.blockchain.sdk;

import org.apache.commons.codec.binary.Hex;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.TransactionProposalRequest;

import java.io.ByteArrayInputStream;
import java.security.MessageDigest;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;

/**
 * 工具类
 */
class Utils {
    /**
     * 构造参数为字符串类型的交易提案请求
     * @param hfClient fabric 客户端
     * @param chaincodeName 链码名
     * @param functionName 方法名
     * @param args 参数
     * @return 交易提案请求
     */
    static TransactionProposalRequest getTransactionProposalRequest(HFClient hfClient, String chaincodeName, String functionName, String[] args) {
        TransactionProposalRequest transactionProposalRequest = hfClient.newTransactionProposalRequest();
        transactionProposalRequest.setFcn(functionName);
        transactionProposalRequest.setArgs(args);
        transactionProposalRequest.setChaincodeID(ChaincodeID.newBuilder().setName(chaincodeName).build());
        return transactionProposalRequest;
    }

    /**
     * 构造参数为字节数组类型的交易提案请求
     * @param hfClient fabric 客户端
     * @param chaincodeName 链码名
     * @param functionName 方法名
     * @param args 参数
     * @return 交易提案请求
     */
    static TransactionProposalRequest getTransactionProposalRequest(HFClient hfClient, String chaincodeName, String functionName, byte[][] args) {
        TransactionProposalRequest transactionProposalRequest = hfClient.newTransactionProposalRequest();
        transactionProposalRequest.setFcn(functionName);
        transactionProposalRequest.setArgBytes(args);
        transactionProposalRequest.setChaincodeID(ChaincodeID.newBuilder().setName(chaincodeName).build());
        return transactionProposalRequest;
    }

    /**
     * 计算特定字节数组唯一的Hash值
     * @param bytes 原文
     * @return Hash字符串
     */
    public static String getSHA256(byte[] bytes) {
        if (null == bytes) {
            return null;
        }
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.reset();
            messageDigest.update(bytes);
            return byte2Hex(messageDigest.digest());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 计算特定字符串唯一的Hash值
     * @param str 原文
     * @return Hash字符串
     */
    public static String getSHA256(String str) {
        return getSHA256(str.getBytes());
    }

    /**
     * 将字节数组编码为 Base64 字符串
     * @param bytes 原文
     * @return Base64 字符串
     */
    public static String encodeBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Base64 解码
     * @param str Base64字符串
     * @return 原文
     */
    public static byte[] decodeBase64(String str) {
        return Base64.getDecoder().decode(str);
    }

    /**
     * 将 Base64 字符串解码为原文字节数组
     * @param str Base64字符串
     * @return 原文
     */
    public static byte[] decodeBase64(byte[] bytes) {
        return Base64.getDecoder().decode(bytes);
    }

    /**
     * 根据PEM字符串获取 X509 证书
     * @param pemStr PEM 字符串
     * @return X509 证书
     * @throws CertificateException
     */
    public static X509Certificate getCertFromPem(String pemStr) throws CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(pemStr.getBytes()));

    }

    /**
     * 将十六进制字节码转码为普通字符串。Fabric中的一些值以十六进制字节码方式返回，可用该方法进行转码
     * 如： \260-\256D\323i:\311\343\321\267\212f\206\230]a\0331\2440\264T\243qW*9\a\367\374q
     * 转码后为： b02dae44d3693ac9e3d1b78a6686985d611b31a430b454a371572a3907f7fc71
     * @param bytes 十六进制字节码
     * @return 字符串
     */
    public static String getHexString(byte[] bytes) {
        return Hex.encodeHexString(bytes);
    }

    /**
     * 字节数组转十六进制
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String byte2Hex(byte[] bytes) {
        StringBuffer stringBuffer = new StringBuffer();
        String temp = null;
        for (int i = 0; i < bytes.length; i++) {
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length() == 1) {
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }
}
