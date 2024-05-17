package org.service;

import android.os.Handler;
import android.util.Log;
import android.view.View;

import com.common.apiutil.TimeoutException;

import net.sf.scuba.smartcards.CardFileInputStream;
import net.sf.scuba.smartcards.CardService;
import net.sf.scuba.smartcards.CardServiceException;

import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jmrtd.PACEKeySpec;
import org.jmrtd.PassportService;
import org.jmrtd.Util;
import org.jmrtd.lds.CardAccessFile;
import org.jmrtd.lds.PACEInfo;
import org.jmrtd.lds.SecurityInfo;
import org.jmrtd.lds.icao.DG15File;
import org.jmrtd.protocol.AAResult;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.crypto.Cipher;

public class CardReaderService {
    private PassportService passportService;


    public void connectReader(CardService cardService) throws CardReaderException {
        try {
            passportService = new PassportService(cardService, 256, 1000, false, true);
        } catch (Exception e) {
            throw new CardReaderException(e.getMessage());
        }
    }

    public CardFileInputStream getInputStream(short fid) throws CardReaderException {
        try {
            return passportService.getInputStream(fid);
        } catch (CardServiceException e) {
            throw new CardReaderException(e.getMessage());
        }
    }

    public boolean checkAA(byte[] dg15) throws TimeoutException {
        try {
            Signature rsaAASignature = Signature.getInstance("SHA1WithRSA/ISO9796-2", new BouncyCastleProvider());

            DG15File dg15File = new DG15File(new ByteArrayInputStream(dg15));
            PublicKey dg15PublicKey = dg15File.getPublicKey();
            String publicKeyAlgorithm = dg15PublicKey.getAlgorithm();
            String digestAlgorithm = "SHA1";
            String signatureAlgorithm = "SHA1WithRSA/ISO9796-2";
            if (publicKeyAlgorithm.equals("EC")) {
                digestAlgorithm = "SHA256";
                signatureAlgorithm = "SHA256withECDSA";
            }
            SecureRandom secureRandom = new SecureRandom();
            byte[] challenge = new byte[8];
            secureRandom.nextBytes(challenge);

            AAResult aaResult = passportService.doAA(dg15PublicKey, digestAlgorithm, signatureAlgorithm, challenge);

            PublicKey aaPublicKey = aaResult.getPublicKey();
            byte[] aaResponse = aaResult.getResponse();
            if ("SHA256withECDSA".equals(aaResult.getSignatureAlgorithm())) {
                Signature ecdsaAASignature = Signature.getInstance("SHA256withECDSA");
                ecdsaAASignature.initVerify(aaPublicKey);

                int l = aaResponse.length / 2;
                BigInteger r = Util.os2i(aaResponse, 0, l);
                BigInteger s = Util.os2i(aaResponse, l, l);

                ecdsaAASignature.update(challenge);

                ASN1Sequence asn1Sequence = new DERSequence(new ASN1Encodable[]{new ASN1Integer(r), new ASN1Integer(s)});

                boolean success = ecdsaAASignature.verify(asn1Sequence.getEncoded());
                System.out.println("AA: " + success);
                return success;
            }
            if ("SHA1WithRSA/ISO9796-2".equals(aaResult.getSignatureAlgorithm())) {
                Cipher rsaAACipher = Cipher.getInstance("RSA/NONE/NoPadding");
                RSAPublicKey rsaPublicKey = (RSAPublicKey) aaPublicKey;
                rsaAACipher.init(Cipher.DECRYPT_MODE, rsaPublicKey);
                byte[] plaintext = rsaAACipher.doFinal(aaResponse);
                MessageDigest rsaAADigest = MessageDigest.getInstance("SHA1");
                byte[] m1 = Util.recoverMessage(rsaAADigest.getDigestLength(), plaintext);

                rsaAASignature.initVerify(aaPublicKey);
                rsaAASignature.update(m1);
                rsaAASignature.update(challenge);
                boolean success = rsaAASignature.verify(aaResponse);
                Log.i("checkAA", "checkAA: " + success);
                return success;
            }
        } catch (Exception e) {
            Log.e("checkAA", "checkAAException: ", e);
        }
        return false;
    }

    public void initSecureChannel(PACEKeySpec paceKey) throws TimeoutException {
        long startTime = System.currentTimeMillis();

        while (true) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - startTime > 15000) {
                throw new TimeoutException("Quá thời gian đọc dữ liệu CCCD");
            }

            try {
                if (passportService != null && !passportService.isOpen()) {
//                    passportService.open();
                    try {
                        CardFileInputStream cardFileInputStream = passportService.getInputStream(PassportService.EF_CARD_ACCESS);
                        CardAccessFile cardAccessFile = new CardAccessFile(cardFileInputStream);
                        Collection<SecurityInfo> securityInfoCollection = cardAccessFile.getSecurityInfos();
                        List<PACEInfo> paceInfoList = getPACEInfo(securityInfoCollection);
                        if (!paceInfoList.isEmpty()) {
                            for (PACEInfo paceInfo : paceInfoList) {
                                passportService.doPACE(paceKey, paceInfo.getObjectIdentifier(), PACEInfo.toParameterSpec(paceInfo.getParameterId()), paceInfo.getParameterId());
                                passportService.sendSelectApplet(true);
                                return;
                            }
                        }
                        break;
                    } catch (IOException e) {
                        passportService.close();
                    }
                }
            } catch (Exception e) {
                passportService.close();
            }
        }
    }

    private static List<PACEInfo> getPACEInfo(Collection<SecurityInfo> securityInfo) {
        List<PACEInfo> list = new ArrayList<>();
        for (SecurityInfo info : securityInfo) {
            if (info instanceof PACEInfo) {
                list.add((PACEInfo) info);
            }
        }
        return list;
    }

    public byte[] readBytes(CardFileInputStream inputStream) {
        try {
            byte[] dg = new byte[inputStream.getLength()];
            inputStream.read(dg);
            return dg;
        } catch (IOException e) {
            return null;
        }
    }
}
