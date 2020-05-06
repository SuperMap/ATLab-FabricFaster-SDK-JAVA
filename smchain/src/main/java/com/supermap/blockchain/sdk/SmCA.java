package com.supermap.blockchain.sdk;

import org.hyperledger.fabric.sdk.Enrollment;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric_ca.sdk.exception.EnrollmentException;
import org.hyperledger.fabric_ca.sdk.exception.GenerateCRLException;
import org.hyperledger.fabric_ca.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric_ca.sdk.exception.RevocationException;

import java.util.Date;

/**
 * Supermap CA 接口，用于管理联盟链中证书生成，注销等
 *
 * 证书的管理实际由 CA 节点完成，客户端的作用是向 CA 发送请求，CA 会将处理
 * 结果返回到客户端，获取到的结果请根据实际情况进行处理。比如注册用户后可以得
 * 到证书字符串，请自行保存至合适位置。
 */
public interface SmCA {
    /**
     * 注册用户。用户注册成功后会返回一个密码，该密码可自定义，也可以在 user 参
     * 数中自定义，如未自定义则会返回一个随机值。请妥善保管该密码，改密码是用于生
     * 成用户证书、密钥的唯一凭证。另外，同一用户名只能注册一次，如果证书丢失，且
     * 无法找到注册密码，则只能用其他用户名重新注册。
     * @param user 注册用户
     * @param adminUser 管理员用户
     * @return 注册密码，用于获取密钥，请妥善保管该密码
     * @throws Exception
     */
    String register(SmUser user, SmUser adminUser) throws Exception;

    /**
     * 登记用户，注册完成后可以登记用户，登记之后可获得用户密钥
     * @param userName 用户名
     * @param secret 注册密码，该密码在注册用户时生成
     * @return 登记信息
     * @throws EnrollmentException
     * @throws InvalidArgumentException
     */
    Enrollment enroll(String userName, String secret) throws EnrollmentException, InvalidArgumentException;

    /**
     * 重登记注销登记的用户
     * @param user 重注册用户
     * @return 登记信息
     * @throws EnrollmentException
     * @throws InvalidArgumentException
     */
    Enrollment reenroll(SmUser user) throws EnrollmentException, InvalidArgumentException;

    /**
     * 注销用户，注销后该用户名永久失效
     * @param userName 用户名
     * @param reason 注销原因
     * @param adminUser 管理员用户
     * @return CRL
     * @throws InvalidArgumentException
     * @throws RevocationException
     */
    String revoke(String userName, String reason, SmUser adminUser) throws InvalidArgumentException, RevocationException;

    /**
     * 撤销用户的证书，即冻结该用户账户，该用户重登记后可继续使用
     * @param enrollment 登记信息
     * @param reason 撤销原因
     * @param adminUser 管理员用户
     * @return CRL
     * @throws InvalidArgumentException
     * @throws RevocationException
     */
    String revoke(Enrollment enrollment ,String reason, SmUser adminUser) throws InvalidArgumentException, RevocationException;

    /**
     * 获取 CRL（Certificate Revocation List，证书撤销列表），
     * 该列表中记录了已失效的证书，一般来说，在验证一个证书有效性的时候，
     * 需要先查看该证书是否在次列表之中。该列表需定期从 CA 获取更新。
     * @param adminUser 管理员
     * @param revokedBefore 该日期之前撤销
     * @param revokedAfter 该日期之后撤销
     * @param expireBefore 该日期之前过期
     * @param expireAfter 该日期之前过期
     * @return
     * @throws GenerateCRLException
     * @throws InvalidArgumentException
     */
    String getCRL(User adminUser, Date revokedBefore, Date revokedAfter, Date expireBefore, Date expireAfter) throws GenerateCRLException, InvalidArgumentException;
}
