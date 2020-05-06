package com.supermap.blockchain.sdk;

import org.hyperledger.fabric.protos.peer.Query;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Supermap 链码管理接口实现类
 */
class SmChaincodeImp implements SmChaincode {

    Logger logger = Logger.getLogger(SmChaincodeImp.class.getName());

    private HFClient hfClient;
    private Channel channel;

    private Collection<ProposalResponse> successful = new LinkedList<>();
    private Collection<ProposalResponse> failed = new LinkedList<>();
    private Collection<ProposalResponse> proposalResponses = new LinkedList<>();

    public SmChaincodeImp(HFClient hfClient, Channel channel) {
        this.hfClient = hfClient;
        this.channel = channel;
    }

    @Override
    public boolean install(
            String chaincodeName,
            String chaincodeVersion,
            String chaincodePath,
            TransactionRequest.Type type
    ) {
        // 构造链码安装提案请求
        try {
            InstallProposalRequest installProposalRequest = hfClient.newInstallProposalRequest();
            installProposalRequest.setChaincodeID(getChaincodeID(chaincodeName, chaincodeVersion, chaincodePath, type));
            installProposalRequest.setChaincodeLanguage(type);
            installProposalRequest.setChaincodeSourceLocation(new File(chaincodePath));

            clear();

            // 向通道中的节点发送链码安装提案请求
            proposalResponses = hfClient.sendInstallProposal(installProposalRequest, channel.getPeers());

            // 等待接收响应
            for (ProposalResponse response : proposalResponses) {
                if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
                    successful.add(response);
                } else {
                    failed.add(response);
                }
            }

            if (failed.size() == 0) {
                return true;
            } else {
                // TODO: 链码安装失败节点的提示信息
                logger.warning("Not enough endorsers for install: " + successful.size() + ".  " + failed.iterator().next().getMessage());
                return false;
            }
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        } catch (ProposalException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public CompletableFuture<BlockEvent.TransactionEvent> instantiate(
            String chaincodeName,
            String chaincodeVersion,
            String chaincodePath,
            File chaincodeEndorsementPolicyFile,
            TransactionRequest.Type type
    ) {
        CompletableFuture<BlockEvent.TransactionEvent> result = null;
        ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
        Channel.NOfEvents nOfEvents = null;
        try {
            chaincodeEndorsementPolicy.fromYamlFile(chaincodeEndorsementPolicyFile);

            // 构造链码实例化提案请求
            InstantiateProposalRequest instantiateProposalRequest = hfClient.newInstantiationProposalRequest();
            instantiateProposalRequest.setChaincodeLanguage(type);
            instantiateProposalRequest.setChaincodeID(getChaincodeID(chaincodeName, chaincodeVersion, chaincodePath, type));
            instantiateProposalRequest.setFcn("init");
            instantiateProposalRequest.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);
            instantiateProposalRequest.setProposalWaitTime(300000L);
            instantiateProposalRequest.setArgs("");
            Map<String, byte[]> tm = new HashMap<>();
            tm.put("HyperLedgerFabric", "InstantiateProposalRequest:JavaSDK".getBytes(UTF_8));
            tm.put("method", "InstantiateProposalRequest".getBytes(UTF_8));
            instantiateProposalRequest.setTransientMap(tm);

            clear();

            Collection<Peer> peers = channel.getPeers();
            // 向通道中的节点发送实例化提案请求
            proposalResponses = channel.sendInstantiationProposal(instantiateProposalRequest);

            for (ProposalResponse response : proposalResponses) {
                if (response.isVerified() && response.getStatus().equals(ProposalResponse.Status.SUCCESS)) {
                    successful.add(response);
                } else {
                    failed.add(response);
                }
            }

            if (failed.size() > 0) {
                for (ProposalResponse fail : failed) {
                    logger.warning("Not enough endorsers for instantiate: " + successful.size() + " endorser failed with " + fail.getMessage() + ", on peer " + fail.getPeer());
                }
                ProposalResponse first = failed.iterator().next();
                logger.warning("Not enough endorsers for instantiate: " + successful.size() + " endorser failed with " + first.getMessage() + ". Was verified: " + first.isVerified());
            }

            nOfEvents = Channel.NOfEvents.createNofEvents();
            if (!channel.getPeers(EnumSet.of(Peer.PeerRole.EVENT_SOURCE)).isEmpty()) {
                nOfEvents.addPeers(channel.getPeers(EnumSet.of(Peer.PeerRole.EVENT_SOURCE)));
            }
            if (!channel.getEventHubs().isEmpty()) {
                nOfEvents.addEventHubs(channel.getEventHubs());
            }

            result = channel.sendTransaction(successful, Channel.TransactionOptions.createTransactionOptions()
                    .userContext(hfClient.getUserContext())
                    .shuffleOrders(false)
                    .orderers(channel.getOrderers())
                    .nOfEvents(nOfEvents)
            );
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ChaincodeEndorsementPolicyParseException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        } catch (ProposalException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public CompletableFuture<BlockEvent.TransactionEvent> upgrade(
            String chaincodeName,
            String chaincodeVersion,
            String chaincodePath,
            File chaincodeEndorsementPolicyFile,
            TransactionRequest.Type type
    ) {
        CompletableFuture<BlockEvent.TransactionEvent> result = null;
        ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
        Channel.NOfEvents nOfEvents = null;
        try {

            chaincodeEndorsementPolicy.fromYamlFile(chaincodeEndorsementPolicyFile);

            UpgradeProposalRequest upgradeProposalRequest = hfClient.newUpgradeProposalRequest();
            upgradeProposalRequest.setChaincodeLanguage(type);
            upgradeProposalRequest.setChaincodeID(getChaincodeID(chaincodeName, chaincodeVersion, chaincodePath, type));
            upgradeProposalRequest.setFcn("init");
            upgradeProposalRequest.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);
            upgradeProposalRequest.setProposalWaitTime(300000L);
            upgradeProposalRequest.setArgs("");

            Map<String, byte[]> tm = new HashMap<>();
            tm.put("HyperLedgerFabric", "UpgradeProposalRequest:JavaSDK".getBytes(UTF_8));
            tm.put("method", "UpgradeProposalRequest".getBytes(UTF_8));
            upgradeProposalRequest.setTransientMap(tm);

            clear();

            Collection<Peer> peers = channel.getPeers();
            // 向通道中的节点发送实例化提案请求
            proposalResponses = channel.sendUpgradeProposal(upgradeProposalRequest);

            for (ProposalResponse response : proposalResponses) {
                if (response.isVerified() && response.getStatus().equals(ProposalResponse.Status.SUCCESS)) {
                    successful.add(response);
                } else {
                    failed.add(response);
                }
            }

            if (failed.size() > 0) {
                for (ProposalResponse fail : failed) {
                    logger.warning("Not enough endorsers for upgrade: " + successful.size() + " endorser failed with " + fail.getMessage() + ", on peer " + fail.getPeer());
                }
                ProposalResponse first = failed.iterator().next();
                logger.warning("Not enough endorsers for upgrade: " + successful.size() + " endorser failed with " + first.getMessage() + ". Was verified: " + first.isVerified());
            }

            nOfEvents = Channel.NOfEvents.createNofEvents();
            if (!channel.getPeers(EnumSet.of(Peer.PeerRole.EVENT_SOURCE)).isEmpty()) {
                nOfEvents.addPeers(channel.getPeers(EnumSet.of(Peer.PeerRole.EVENT_SOURCE)));
            }
            if (!channel.getEventHubs().isEmpty()) {
                nOfEvents.addEventHubs(channel.getEventHubs());
            }

            result = channel.sendTransaction(successful, Channel.TransactionOptions.createTransactionOptions()
                    .userContext(hfClient.getUserContext())
                    .shuffleOrders(false)
                    .orderers(channel.getOrderers())
                    .nOfEvents(nOfEvents)
            );


        } catch (IOException e) {
            e.printStackTrace();
        } catch (ChaincodeEndorsementPolicyParseException e) {
            e.printStackTrace();
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        } catch (ProposalException e) {
            e.printStackTrace();
        }

        return result;
    }

    @Override
    public List<Query.ChaincodeInfo> listInstalled(Peer peer) {
        List<Query.ChaincodeInfo> chaincodeInfos = null;
        try {
            chaincodeInfos = hfClient.queryInstalledChaincodes(peer);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        } catch (ProposalException e) {
            e.printStackTrace();
        }

        return chaincodeInfos;
    }

    @Override
    public List<Query.ChaincodeInfo> listInstantiated(Peer peer) {
        List<Query.ChaincodeInfo> chaincodeInfos = null;
        try {
            chaincodeInfos = channel.queryInstantiatedChaincodes(peer);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        } catch (ProposalException e) {
            e.printStackTrace();
        }
        return chaincodeInfos;
    }

    private ChaincodeID getChaincodeID(
            String chaincodeName,
            String chaincodeVersion,
            String chaincodePath,
            TransactionRequest.Type type
    ) {
        ChaincodeID chaincodeID = null;
        switch (type) {
            case JAVA:
                chaincodeID = ChaincodeID.newBuilder()
                        .setName(chaincodeName)
                        .setVersion(chaincodeVersion)
                        .build();
                break;
            case NODE:
            case GO_LANG:
                chaincodeID = ChaincodeID.newBuilder()
                        .setName(chaincodeName)
                        .setPath(chaincodePath)
                        .setVersion(chaincodeVersion)
                        .build();
                break;
            default:
                break;
        }
        return chaincodeID;
    }

    private void clear() {
        proposalResponses.clear();
        successful.clear();
        failed.clear();
    }
}
