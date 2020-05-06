package com.supermap.blockchain.sdk;

import org.bouncycastle.asn1.x509.TBSCertList;
import org.bouncycastle.cert.X509CRLHolder;
import org.bouncycastle.openssl.PEMParser;
import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric_ca.sdk.exception.EnrollmentException;
import org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.StringReader;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.HashSet;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

public class SmCAImpTest {
    private final String networkConfigFile = this.getClass().getResource("/network-config-testC.yaml").getFile();
    private final SmChain smChain = SmChain.getChain("txchannel", new File(networkConfigFile));
    private SmCA smCA = smChain.getCa("OrgC");

    private static SmUser admin;
    private static SmUser user;
    private static String userKeyString;
    private static String userCertString;

    @Before
    public void enrollAdmin() throws EnrollmentException, InvalidArgumentException {
        Enrollment enrollment = smCA.enroll("admin", "adminpw");
        PrivateKey key = enrollment.getKey();

        admin = new SmUser();
        admin.setEnrollment(enrollment);
        admin.setName("admin");
        admin.setMspId("OrgC");
        Set<String> roles = new HashSet<>();
        roles.add("admin");
        admin.setRoles(roles);

        user = getSmUser();
//        userKeyString = Utils.encodeBase64(user.getEnrollment().getKey().getEncoded());
//        userCertString = user.getEnrollment().getCert();
    }

    @Test
    public void registerAndEnrollTest() throws Exception {
        String secret = smCA.register(user, admin);
        System.out.println(secret);
        Enrollment userEnrollment = smCA.enroll(user.getName(), secret);
        System.out.println(Utils.encodeBase64(userEnrollment.getKey().getEncoded()));
        System.out.println(userEnrollment.getCert());
    }

    @Test
    public void revokeTest() throws Exception {
        String result = smCA.revoke(user.getName(), "test revoke", admin);
        for (TBSCertList.CRLEntry entry : parseCRL(result)) {
            System.out.println(entry.toString());
        }
    }

    @Test
    public void reenrollTest() {
    }

    @Test
    public void getCRLTest() throws Exception {
        String crl = smCA.getCRL(admin, null, null, null, null);
        System.out.println(parseCRL(crl).length);
    }

    private SmUser getSmUser() {
        Set<String> roles = new HashSet<>();
        SmUser user = new SmUser();
        user.setName("sdk-user3");
        user.setAffiliation("affiliation");
        user.setRoles(roles);
        user.setMspId("OrgC");
        user.setEnrollSecret("password");
        return user;
    }

    private TBSCertList.CRLEntry[] parseCRL(String crl) throws Exception {
        Base64.Decoder b64dec = Base64.getDecoder();
        final byte[] decode = b64dec.decode(crl.getBytes(UTF_8));

        PEMParser pem = new PEMParser(new StringReader(new String(decode)));
        X509CRLHolder holder = (X509CRLHolder) pem.readObject();

        return holder.toASN1Structure().getRevokedCertificates();
    }
}