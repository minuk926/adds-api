package kr.xit.ens.cmm;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import kr.xit.biz.cmm.service.ICmmEnsCacheService;
import kr.xit.biz.common.ApiConstants;
import kr.xit.biz.common.ApiConstants.SndngSeCode;
import kr.xit.biz.ens.model.cmm.CmmEnsRequestDTO;
import kr.xit.biz.ens.model.cmm.CmmEnsRlaybsnmDTO;
import kr.xit.biz.ens.model.kt.KtCommonDTO.ErrorMsg;
import kr.xit.biz.ens.model.kt.KtCommonDTO.KtMnsRequest;
import kr.xit.biz.kt.service.IBizKtMmsService;
import kr.xit.core.exception.BizRuntimeException;
import kr.xit.core.spring.util.ApiSpringUtils;
import kr.xit.core.spring.util.CoreSpringUtils;
import kr.xit.core.spring.util.MessageUtil;
import kr.xit.core.support.utils.DateUtils;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Base64Utils;

/**
 * <pre>
 * description : ENS 공통 method
 *
 * packageName : kr.xit.ens.cmm
 * fileName    : CmmNiceCiUtils
 * author      : limju
 * date        : 2023-09-19
 * ======================================================================
 * 변경일         변경자        변경 내용
 * ----------------------------------------------------------------------
 * 2023-09-19    limju       최초 생성
 *
 * </pre>
 */

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CmmEnsUtils {
    private static final MessageUtil messageUtil = CoreSpringUtils.getMessageUtil();
    private static final ICmmEnsCacheService cacheService = ApiSpringUtils.getCmmEnsCacheService();
    private static final IBizKtMmsService bizKtService = ApiSpringUtils.getBizKtMmsService();

    /**
     * 문서 중개자 인증 정보 조회
     * @param signguCode string
     * @param ffnlgCode String
     * @param seCode SndngSeCode 문서중개자 구분 코드
     * @return CmmEnsRlaybsnmDTO 문서중개자 정보
     */
    public static CmmEnsRlaybsnmDTO getRlaybsnmInfo(final String signguCode, final String ffnlgCode, final
    SndngSeCode seCode) {
        CmmEnsRequestDTO ensDTO = CmmEnsRequestDTO.builder()
            .signguCode(signguCode)
            .ffnlgCode(ffnlgCode)
            .profile(ApiConstants.PROFILE)
            .build();

        final CmmEnsRlaybsnmDTO dto = cacheService.getRlaybsnmInfoCache(ensDTO);
        if(ObjectUtils.isEmpty(dto))   throw BizRuntimeException.create(messageUtil.getMessage("fail.api.rlaybsnm.info"));

        // KT인 경우 토큰유효기간 check
        if(SndngSeCode.KT_BC.equals(seCode)){

            if(StringUtils.isNotEmpty(dto.getKtTokenExpiresIn())
                && DateUtils.getTodayAndNowTime(ApiConstants.FMT_DT_STD).compareTo(dto.getKtTokenExpiresIn()) < 0
                && ObjectUtils.isNotEmpty(dto.getKtAccessToken())
            )     return dto;

            // 유효기간이 경과된 경우 재발급
            bizKtService.requestToken(
                KtMnsRequest.builder()
                    .signguCode(signguCode)
                    .ffnlgCode(ffnlgCode)
                    .profile(ApiConstants.PROFILE)
                    .build()
            );
            return cacheService.getRlaybsnmInfoCache(ensDTO);
        }
        return dto;
    }

    /**
     * <pre>
     * parameter validation check
     * invalid parameter message -> String으로 BizRuntimeException throw
     * @param t T
     * </pre>
     */
    public static <T> void validate(T t) {
        Locale.setDefault(Locale.KOREA);
        final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        final Set<ConstraintViolation<T>> list = validator.validate(t);

        if (!list.isEmpty()) {
            throw BizRuntimeException.create(
                list.stream()
                    .map(row -> String.format("%s=%s", row.getPropertyPath(), row.getMessageTemplate()))
                    //.map(row -> String.format("%s=%s", row.getPropertyPath(), row.get()) ? row.getMessage(): row.getMessageTemplate()))
                    .toList().toString());
        }
    }

    /**
     * parameter validation check
     * @param t T
     * @return List<ErrorMsg>
     */
    public static <T> List<ErrorMsg> getValidateErrors(T t) {
        Locale.setDefault(Locale.KOREA);
        final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        final Set<ConstraintViolation<T>> list = validator.validate(t);

        if (!list.isEmpty()) {
            return list.stream()
                .map(row -> new ErrorMsg(String.format("%s=%s", row.getPropertyPath(), row.getMessageTemplate())))
                .toList();
        }
        return new ArrayList<>();
    }

    /**
     * length 길이의 UUID String return -> '-' remove
     * @param length
     * @return
     */
    public static String generateLengthUuid(int length) {
        final String allChars = UUID.randomUUID().toString().replace("-", "");
        final Random random = new Random();
        final char[] otp = new char[length];
        for (int i = 0; i < length; i++) {
            otp[i] = allChars.charAt(random.nextInt(allChars.length()));
        }
        return String.valueOf(otp);
    }

    /**
     * 공개키로 암호화를 수행
     *
     * @param publicKeyString String
     * @param symkeyRegInfo String
     * @return String
     */
    public static String encSymkeyRegInfo(String publicKeyString, String symkeyRegInfo)  {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            byte[] cipherEnc = Base64.getDecoder().decode(publicKeyString);
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(cipherEnc);
            java.security.PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] bytePlain = cipher.doFinal(symkeyRegInfo.getBytes());

            return Base64Utils.encodeToString(bytePlain);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | NoSuchPaddingException |
                 IllegalBlockSizeException | BadPaddingException | InvalidKeyException e){
            throw BizRuntimeException.create(e.getMessage());
        }
    }

    /**
     * sha256 암호화
     *
     * @param text String
     * @return String
     */
    public static String hexSha256(final String text) {
        final StringBuilder sbuf = new StringBuilder();

        try {
            final MessageDigest mDigest = MessageDigest.getInstance("SHA-256");
            mDigest.update(text.getBytes());

            final byte[] msgStr = mDigest.digest();

            for(final byte tmpStrByte : msgStr) {
                final String tmpEncTxt = Integer.toString((tmpStrByte & 0xff) + 0x100, 16)
                    .substring(1);

                sbuf.append(tmpEncTxt);
            }
        } catch (NoSuchAlgorithmException nae){
            throw BizRuntimeException.create(nae.getMessage());
        }
        return sbuf.toString();
    }


    /**
     * <pre>
     * AES 암호화 -> Base64 encoding return
     * -> Nice ci 데이타 암호화
     *
     * @param key String
     * @param iv String
     * @param planText String
     * @return String  Base64 encoding data
     * </pre>
     */
    public static String encodeAesData(final String key, final String iv, final String planText) {
        final SecretKey secureKey = new SecretKeySpec(key.getBytes(), "AES");
        try {
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secureKey, new IvParameterSpec(iv.getBytes()));

            final byte[] encData = cipher.doFinal(planText.trim().getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encData);

        }catch (NoSuchPaddingException | NoSuchAlgorithmException |
                InvalidAlgorithmParameterException | InvalidKeyException |
                IllegalBlockSizeException | BadPaddingException e){
            throw BizRuntimeException.create(e.getMessage());
        }
    }

    /**
     * <pre>
     * Hmac 무결성체크값(integrity_value) 생성
     * @param hmacKey String
     * @param message String
     * @return String
     * </pre>
     */
    public static String encodeHmacSha256(final String hmacKey, final String message) {
        try {
            final Mac mac = Mac.getInstance("HmacSHA256");
            final SecretKeySpec sks = new SecretKeySpec(hmacKey.getBytes(), "HmacSHA256");
            mac.init(sks);
            final byte[] hmac256 = mac.doFinal(message.getBytes());
            return Base64.getEncoder().encodeToString(hmac256);

        }catch (NoSuchAlgorithmException|InvalidKeyException e){
            throw BizRuntimeException.create(e.getMessage());
        }
    }

    /**
     * AES 복호화
     * @param encData String
     * @param key String
     * @param iv String
     * @return String
     */
    public static String decodeAesData(String encData, String key, String iv) {

        final byte[] respDataEnc = Base64.getDecoder().decode(encData.getBytes());
        final SecretKey secureKey = new SecretKeySpec(key.getBytes(), "AES");

        try {
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secureKey, new IvParameterSpec(iv.getBytes()));
            final byte[] decrypted = cipher.doFinal(respDataEnc);
            return new String(decrypted);

        }catch (NoSuchPaddingException | NoSuchAlgorithmException |
                InvalidAlgorithmParameterException | InvalidKeyException |
                IllegalBlockSizeException | BadPaddingException e){
            throw BizRuntimeException.create(e.getMessage());
        }
    }
}
