package org;

import org.bouncycastle.asn1.ASN1ApplicationSpecific;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.ASN1Set;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CICSystemUtil {
    public static CardPersonalData readCardPersonalData(byte[] dg13) {
        CardPersonalData personalData = new CardPersonalData();

        try {
            ASN1ApplicationSpecific applicationSpecific = ASN1ApplicationSpecific.getInstance(dg13);
            ASN1Sequence asn1Content = ASN1Sequence.getInstance(applicationSpecific.getContents());
            ASN1Set asn1Set = ASN1Set.getInstance(asn1Content.getObjectAt(2));
            ASN1Encodable[] asn1SetArray = asn1Set.toArray();
            for (ASN1Encodable asn1Encodable : asn1SetArray) {
                ASN1Sequence asn1Sequence = ASN1Sequence.getInstance(asn1Encodable);
                if (asn1Sequence.size() > 0) {
                    Integer integer = getInteger(asn1Sequence, 0, 0);
                    if (integer == 1) {
                        personalData.setCardNumber(getString(asn1Sequence, 1, ""));
                    }
                    if (integer == 2) {
                        personalData.setFullName(getString(asn1Sequence, 1, ""));
                    }
                    if (integer == 3) {
                        personalData.setDateOfBirth(getString(asn1Sequence, 1, ""));
                    }
                    if (integer == 4) {
                        personalData.setSex(getString(asn1Sequence, 1, ""));
                    }
                    if (integer == 5) {
                        personalData.setNationality(getString(asn1Sequence, 1, ""));
                    }
                    if (integer == 6) {
                        personalData.setEthnicity(getString(asn1Sequence, 1, ""));
                    }
                    if (integer == 7) {
                        personalData.setReligion(getString(asn1Sequence, 1, ""));
                    }
                    if (integer == 8) {
                        personalData.setPlaceOfOrigin(getString(asn1Sequence, 1, ""));
                    }
                    if (integer == 9) {
                        personalData.setPlaceOfResidence(getString(asn1Sequence, 1, ""));
                    }
                    if (integer == 10) {
                        personalData.setPersonalIdentification(getString(asn1Sequence, 1, ""));
                    }
                    if (integer == 11) {
                        personalData.setDateOfIssue(getString(asn1Sequence, 1, ""));
                    }
                    if (integer == 12) {
                        personalData.setDateOfExpiry(getString(asn1Sequence, 1, ""));
                    }
                    if (integer == 13) {
                        if (asn1Sequence.size() > 1) {
                            ASN1Sequence sequence = ASN1Sequence.getInstance(asn1Sequence.getObjectAt(1));
                            personalData.setFatherName(getString(sequence, 0, ""));
                        }
                        if (asn1Sequence.size() > 2) {
                            ASN1Sequence sequence = ASN1Sequence.getInstance(asn1Sequence.getObjectAt(2));
                            personalData.setMotherName(getString(sequence, 0, ""));
                        }
                    }
                    if (integer == 14) {
                        if (asn1Sequence.size() > 1) {
                            personalData.setSpouseName(getString(asn1Sequence, 1, ""));
                        }
                    }
                    if (integer == 15) {
                        personalData.setOldCardNumber(getString(asn1Sequence, 1, ""));
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return personalData;
    }

    private static Integer getInteger(ASN1Sequence sequence, int index) {
        return getInteger(sequence, index, null);
    }

    private static Integer getInteger(ASN1Sequence sequence, int index, Integer defaultValue) {
        try {
            ASN1Integer integer = ASN1Integer.getInstance(sequence.getObjectAt(index));
            return integer.intValueExact();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private static String getString(ASN1Sequence sequence, int index) {
        return getString(sequence, index, null);
    }

    private static String getString(ASN1Sequence sequence, int index, String defaultValue) {
        try {
            return sequence.getObjectAt(index).toString();
        } catch (Exception e) {
            return defaultValue;
        }
    }

    public static byte[] inputStreamToByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        int bytesRead;
        byte[] data = new byte[1024]; // You can adjust the buffer size as needed

        while ((bytesRead = inputStream.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, bytesRead);
        }

        buffer.flush();
        return buffer.toByteArray();
    }
}
