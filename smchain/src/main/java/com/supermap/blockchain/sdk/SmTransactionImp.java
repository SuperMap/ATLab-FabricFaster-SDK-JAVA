package com.supermap.blockchain.sdk;

import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.CompletableFuture;

/**
 * 交易处理接口实现类
 */
class SmTransactionImp implements SmTransaction {
    private HFClient hfClient;
    private Channel channel;
    private Collection<ProposalResponse> successful = new LinkedList<>();
    private Collection<ProposalResponse> failed = new LinkedList<>();
    private Collection<ProposalResponse> proposalResponses = new LinkedList<>();

    public SmTransactionImp(HFClient hfClient, Channel channel) {
        this.hfClient = hfClient;
        this.channel = channel;
    };

    /**
     * 查询
     * @param chaincodeName 链码名称
     * @param functionName  方法名称
     * @param args  参数
     * @return  查询结果
     */
    @Override
    public String queryByString(String chaincodeName, String functionName, String[] args) {
        TransactionProposalRequest queryByChaincodeRequest = Utils.getTransactionProposalRequest(hfClient, chaincodeName, functionName, args);
        try {
            // 发送交易提案
            proposalResponses = channel.sendTransactionProposal(queryByChaincodeRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (proposalResponses == null) {
            return "No Response";
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (ProposalResponse res : proposalResponses) {
            try {
                if (stringBuilder.length() > 0) {
                    stringBuilder.append("-");
                }
                stringBuilder.append(new String(res.getChaincodeActionResponsePayload()));
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        }
        return stringBuilder.toString();
    }

    /**
     * 查询键为 byte[] 格式的值
     * @param chaincodeName 链码名称
     * @param functionName  方法名称
     * @param args  参数
     * @return  查询结果
     */
    @Override
    public byte[][] queryByByte(String chaincodeName, String functionName, byte[][] args) {
        TransactionProposalRequest queryByChaincodeRequest = Utils.getTransactionProposalRequest(hfClient, chaincodeName, functionName, args);
        try {
            // 发送交易提案
            proposalResponses = channel.sendTransactionProposal(queryByChaincodeRequest);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (proposalResponses == null) {
            return new byte[][]{"No Response".getBytes()};
        }

        // 将结果构造为 byte[][]
        ArrayList<byte[]> byteArrayList = new ArrayList<>();
        for (ProposalResponse res : proposalResponses) {
            try {
                byteArrayList.add(res.getChaincodeActionResponsePayload());
            } catch (InvalidArgumentException e) {
                e.printStackTrace();
            }
        }
        byte[][] bytes = byteArrayList.toArray(new byte[1][byteArrayList.size()]);

        return bytes;
    }

    /**
     * 执行链码
     * @param chaincodeName 链码名称
     * @param functionName  方法名称
     * @param args  参数
     * @return  执行结果
     */
    @Override
    public String invokeByString(String chaincodeName, String functionName, String[] args) {
        proposalResponses = null;
//        long startTime = System.currentTimeMillis();
        TransactionProposalRequest transactionProposalRequest = Utils.getTransactionProposalRequest(hfClient, chaincodeName, functionName, args);
//        long proposalTime = System.currentTimeMillis();
//        long waitEndoserTime = 0;
//        long judgeEndosermentTime = 0;
//        long ordererTime = 0;


        try {
            // 向所有背书节点发送交易，成功后返回要发往排序节点的提案
            proposalResponses = channel.sendTransactionProposal(transactionProposalRequest);
//            waitEndoserTime = System.currentTimeMillis();

            // 判断背书结果
            for (ProposalResponse response : proposalResponses) {
                if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
//                    System.out.printf("Successful transaction proposal response Txid: %s from peer %s \n", response.getTransactionID(), response.getPeer().getName());
                    successful.add(response);
                } else {
                    failed.add(response);
                }
            }

//            System.out.printf("Received %d transaction proposal responses. Successful+verified: %d . Failed: %d \n",
//                    proposalResponses.size(), successful.size(), failed.size());
            if (failed.size() > 0) {
                ProposalResponse firstTransactionProposalResponse = failed.iterator().next();
                return ("Not enough endorsers for invoke:" + failed.size() + " endorser error: " +
                        firstTransactionProposalResponse.getMessage() +
                        ". Was verified: " + firstTransactionProposalResponse.isVerified());
            }
//            judgeEndosermentTime = System.currentTimeMillis();

            // 向排序节点发送背书后的交易提案，成功后返回一个区块事件
            CompletableFuture<BlockEvent.TransactionEvent> completableFuture = channel.sendTransaction(proposalResponses);
//            ordererTime = System.currentTimeMillis();
//            System.out.println(completableFuture);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (proposalResponses == null) {
            return "No Response";
        }
//        System.out.println("构造提案时间：" + (proposalTime - startTime) + "ms");    // 暂时忽略不计
//        System.out.println("等待背书时间：" + (waitEndoserTime - proposalTime) + "ms");
//        System.out.println("确认背书规则时间" + (judgeEndosermentTime - waitEndoserTime) + "ms"); // 暂时忽略不计
//        System.out.println("等待排序时间：" + (ordererTime - judgeEndosermentTime) + "ms");

        // 获取返回结果
        StringBuilder stringBuilder = new StringBuilder();
        for (ProposalResponse res : proposalResponses) {
            stringBuilder.append(res.getMessage());
        }
        return stringBuilder.toString();
    }

    /**
     * 执行链码，参数为 byte[] 格式
     * @param chaincodeName 链码名称
     * @param functionName  方法名称
     * @param args  参数
     * @return  执行结果
     */
    @Override
    public String invokeByByte(String chaincodeName, String functionName, byte[][] args) {
        TransactionProposalRequest transactionProposalRequest = Utils.getTransactionProposalRequest(hfClient, chaincodeName, functionName, args);
        try {
            // 发送交易提案
            proposalResponses = channel.sendTransactionProposal(transactionProposalRequest);

            // 发送交易
            CompletableFuture<BlockEvent.TransactionEvent> completableFuture = channel.sendTransaction(proposalResponses);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (proposalResponses == null) {
            return "No Response";
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (ProposalResponse res : proposalResponses) {
            stringBuilder.append(res.getMessage());
        }
        return stringBuilder.toString();
    }
}
