package com.supermap.blockchain.sdk;

import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.NetworkConfig;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric_ca.sdk.HFCAClient;
import org.hyperledger.fabric_ca.sdk.RegistrationRequest;
import org.hyperledger.fabric_ca.sdk.exception.EnrollmentException;
import org.hyperledger.fabric_ca.sdk.exception.GenerateCRLException;
import org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric_ca.sdk.exception.RevocationException;

import java.util.Collection;
import java.util.Date;

/**
 * Supermap CA 接口实现类
 */
class SmCAImp implements SmCA {
    private HFCAClient hfcaClient = null;
    private User admin = null;

    SmCAImp(NetworkConfig networkConfig, String OrgName) {
        try {
            Collection<NetworkConfig.OrgInfo> orgInfos = networkConfig.getOrganizationInfos();
            hfcaClient = HFCAClient.createNewInstance(networkConfig.getOrganizationInfo(OrgName).getCertificateAuthorities().get(0));

            admin = networkConfig.getPeerAdmin(OrgName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String register(SmUser user, SmUser adminUser) throws Exception {
        RegistrationRequest registrationRequest = new RegistrationRequest(user.getName());
        if (null != user.getEnrollSecret()) {
            registrationRequest.setSecret(user.getEnrollSecret());
        }
        String secret = hfcaClient.register(registrationRequest, adminUser);
        return secret;
    }

    @Override
    public Enrollment enroll(String userName, String secret) throws EnrollmentException, InvalidArgumentException {
        Enrollment enrollment = hfcaClient.enroll(userName, secret);
        enrollment.getKey().toString();
        return enrollment;
    }

    @Override
    public Enrollment reenroll(SmUser user) throws EnrollmentException, InvalidArgumentException {
//        return hfcaClient.reenroll(user, req);
        return null;
    }

    @Override
    public String revoke(String userName, String reason, SmUser adminUser) throws InvalidArgumentException, RevocationException {
        String result = hfcaClient.revoke(adminUser, userName, reason, true);
        return result;
    }

    @Override
    public String revoke(Enrollment enrollment, String reason, SmUser adminUser) throws InvalidArgumentException, RevocationException {
//        String result = hfcaClient.revoke(adminUser, smEnrollment, reason, true);
//        return result;
        return null;
    }

    @Override
    public String getCRL(User adminUser, Date revokedBefore, Date revokedAfter, Date expireBefore, Date expireAfter) throws GenerateCRLException, InvalidArgumentException {
        return hfcaClient.generateCRL(adminUser, revokedBefore, revokedAfter, expireBefore, expireAfter);
    }
}
