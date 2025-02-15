/*
 * JMRTD - A Java API for accessing machine readable travel documents.
 *
 * Copyright (C) 2006 - 2018  The JMRTD team
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 * $Id: MRZInfo.java 1813 2019-06-06 14:43:07Z martijno $
 */

package org.jmrtd.lds.icao;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import org.jmrtd.lds.AbstractLDSInfo;

import net.sf.scuba.data.Gender;

/**
 * Data structure for storing the MRZ information
 * as found in DG1. Based on ICAO Doc 9303 part 1 and 3.
 *
 * @author The JMRTD team (info@jmrtd.org)
 *
 * @version $Revision: 1813 $
 */
public class MRZInfo extends AbstractLDSInfo {

  private static final long serialVersionUID = 7054965914471297804L;

  /** Unspecified document type (do not use, choose ID1 or ID3). */
  public static final int DOC_TYPE_UNSPECIFIED = 0;

  /** ID1 document type for credit card sized identity cards. Specifies a 3-line MRZ, 30 characters wide. */
  public static final int DOC_TYPE_ID1 = 1;

  /** ID2 document type. Specifies a 2-line MRZ, 36 characters wide. */
  public static final int DOC_TYPE_ID2 = 2;

  /** ID3 document type for passport booklets. Specifies a 2-line MRZ, 44 characters wide. */
  public static final int DOC_TYPE_ID3 = 3;

  /** All valid characters in MRZ. */
  private static final String MRZ_CHARS = "<0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

  /** @deprecated to be replaced with documentCode */
  @Deprecated
  private int documentType;

  private String documentCode;
  private String issuingState;
  private String primaryIdentifier;
  private String secondaryIdentifier;
  private String nationality;
  private String documentNumber;
  private String dateOfBirth;
  private Gender gender;
  private String dateOfExpiry;
  private char documentNumberCheckDigit;
  private char dateOfBirthCheckDigit;
  private char dateOfExpiryCheckDigit;
  private char compositeCheckDigit;
  private String optionalData1; /* NOTE: holds personal number for some issuing states (e.g. NL), but is used to hold (part of) document number for others. */
  private String optionalData2;

  /**
   * Creates a new 2-line MRZ compliant with ICAO Doc 9303 part 1 vol 1.
   *
   * @param documentCode document code (1 or 2 digit, has to start with "P" or "V")
   * @param issuingState issuing state as 3 digit string
   * @param primaryIdentifier card holder last name
   * @param secondaryIdentifier card holder first name(s)
   * @param documentNumber document number
   * @param nationality nationality as 3 digit string
   * @param dateOfBirth date of birth
   * @param gender gender
   * @param dateOfExpiry date of expiry
   * @param personalNumber either empty, or a personal number of maximum length 14, or other optional data of exact length 15
   */
  public MRZInfo(String documentCode, String issuingState,
      String primaryIdentifier, String secondaryIdentifier,
      String documentNumber, String nationality, String dateOfBirth,
      Gender gender, String dateOfExpiry, String personalNumber) {
    if (documentCode == null || documentCode.length() < 1 || documentCode.length() > 2
        || !(documentCode.startsWith("P") || documentCode.startsWith("V"))) {
      throw new IllegalArgumentException("Wrong document code: " + documentCode);
    }
    this.documentType = getDocumentTypeFromDocumentCode(documentCode);
    this.documentCode = trimFillerChars(documentCode);
    this.issuingState = issuingState;
    this.primaryIdentifier = primaryIdentifier;
    this.secondaryIdentifier = secondaryIdentifier;
    this.documentNumber = trimFillerChars(documentNumber);
    this.nationality = nationality;
    this.dateOfBirth = dateOfBirth;
    this.gender = gender;
    this.dateOfExpiry = dateOfExpiry;
    if (personalNumber == null || equalsModuloFillerChars(personalNumber, "")) {
      /* optional data field is not used */
      this.optionalData1 = "";
    } else if (personalNumber.length() == 15) {
      /* it's either a personalNumber with check digit included, or some other optional data */
      this.optionalData1 = personalNumber;
    } else if (personalNumber.length() <= 14) {
      /* we'll assume it's a personalNumber without check digit, and we add the check digit ourselves */
      this.optionalData1 = mrzFormat(personalNumber, 14) + checkDigit(personalNumber, true);
    } else {
      throw new IllegalArgumentException("Wrong personal number: " + personalNumber);
    }
    checkDigit();
  }

  /**
   * Creates a new 3-line MRZ compliant with ICAO Doc 9303 part 3 vol 1.
   *
   * @param documentCode document code (1 or 2 digit, has to start with "I", "C", or "A")
   * @param issuingState issuing state as 3 digit string
   * @param primaryIdentifier card holder last name
   * @param secondaryIdentifier card holder first name(s)
   * @param documentNumber document number
   * @param nationality nationality as 3 digit string
   * @param dateOfBirth date of birth in YYMMDD format
   * @param gender gender
   * @param dateOfExpiry date of expiry in YYMMDD format
   * @param optionalData1 optional data in line 1 of maximum length 15
   * @param optionalData2 optional data in line 2 of maximum length 11
   */
  public MRZInfo(String documentCode,
      String issuingState,
      String documentNumber,
      String optionalData1,
      String dateOfBirth,
      Gender gender,
      String dateOfExpiry,
      String nationality,
      String optionalData2,
      String primaryIdentifier,
      String secondaryIdentifier) {
    if (documentCode == null || documentCode.length() < 1 || documentCode.length() > 2
        || !(documentCode.startsWith("C") || documentCode.startsWith("I") || documentCode.startsWith("A"))) {
      throw new IllegalArgumentException("Wrong document code: " + documentCode);
    }

    this.documentType = getDocumentTypeFromDocumentCode(documentCode);
    this.documentCode = trimFillerChars(documentCode);
    this.issuingState = issuingState;
    this.primaryIdentifier = primaryIdentifier;
    this.secondaryIdentifier = secondaryIdentifier;
    this.documentNumber = trimFillerChars(documentNumber);
    this.nationality = nationality;
    this.dateOfBirth = dateOfBirth;
    this.gender = gender;
    this.dateOfExpiry = dateOfExpiry;
    if (optionalData1 == null || optionalData1.length() > 15) {
      throw new IllegalArgumentException("Wrong optional data 1: " + (optionalData1 == null ? "null" : "\"" + optionalData1 + "\""));
    }
    this.optionalData1 = optionalData1;
    this.optionalData2 = optionalData2;
    checkDigit();
  }

  /**
   * Creates a new MRZ based on an input stream.
   *
   * @param inputStream contains the contents (value) of DG1 (without the tag and length)
   * @param length the length of the MRZInfo structure
   */
  public MRZInfo(InputStream inputStream, int length) {
    try {
      readObject(inputStream, length);
    } catch (IOException ioe) {
      throw new IllegalArgumentException(ioe);
    }
  }

  /**
   * Creates a new MRZ based on the text input.
   * The text input may contain newlines, which will be ignored.
   *
   * @param str input text
   */
  public MRZInfo(String str) {
    if (str == null) {
      throw new IllegalArgumentException("Null string");
    }
    str = str.trim().replace("\n", "");
    try {
      readObject(new ByteArrayInputStream(str.getBytes("UTF-8")), str.length());
    } catch (UnsupportedEncodingException uee) {
      /* NOTE: never happens, UTF-8 is supported. */
      throw new IllegalStateException("Exception", uee);
    } catch (IOException ioe) {
      throw new IllegalArgumentException("Exception", ioe);
    }
  }

  /**
   * Reads the object value from a stream.
   *
   * @param inputStream the stream to read from
   * @param length the length of the value
   *
   * @throws IOException on error reading from the stream
   */
  private void readObject(InputStream inputStream, int length) throws IOException {
    DataInputStream dataIn = new DataInputStream(inputStream);

    /* line 1, pos 1 to 2, Document code */
    this.documentCode = readStringWithFillers(dataIn, 2);
    this.documentType = getDocumentTypeFromDocumentCode(this.documentCode);
    switch (length) {
      case 88:
        this.documentType = DOC_TYPE_ID3;
        break;
      case 90:
        this.documentType = DOC_TYPE_ID1;
        break;
      default:
        this.documentType = getDocumentTypeFromDocumentCode(this.documentCode);
        break;
    }
    if (this.documentType == DOC_TYPE_ID1) {
      /* line 1, pos 3 to 5 Issuing State or organization */
      this.issuingState = readCountry(dataIn);

      /* line 1, pos 6 to 14 Document number */
      this.documentNumber = readString(dataIn, 9);

      /* line 1, pos 15 Check digit */
      this.documentNumberCheckDigit = (char)dataIn.readUnsignedByte();

      /* line 1, pos 16 to 30, Optional data elements */
      this.optionalData1 = readStringWithFillers(dataIn, 15);

      if (documentNumberCheckDigit == '<' && !optionalData1.isEmpty()) {
        /* Interpret personal number as part of document number, see note j. */
        this.documentNumber += optionalData1.substring(0, optionalData1.length() - 1);
        this.documentNumberCheckDigit = optionalData1.charAt(optionalData1.length() - 1);
        this.optionalData1 = null;
      }
      this.documentNumber = trimFillerChars(this.documentNumber);

      /* line 2, pos 1 to 6, Date of birth */
      this.dateOfBirth = readDateOfBirth(dataIn);

      /* line 2, pos 7, Check digit */
      this.dateOfBirthCheckDigit = (char)dataIn.readUnsignedByte();

      /* line 2, pos 8, Sex */
      this.gender = readGender(dataIn);

      /* line 2, Pos 9 to 14, Date of expiry */
      this.dateOfExpiry = readDateOfExpiry(dataIn);

      /* line 2, pos 15, Check digit */
      this.dateOfExpiryCheckDigit = (char)dataIn.readUnsignedByte();

      /* line 2, pos 16 to 18, Nationality */
      this.nationality = readCountry(dataIn);

      /* line 2, pos 19 to 29, Optional data elements */
      this.optionalData2 = readString(dataIn, 11);

      /* line 2, pos 30, Overall check digit */
      this.compositeCheckDigit = (char)dataIn.readUnsignedByte();

      /* line 3 */
      readNameIdentifiers(readString(dataIn, 30));
    } else {
      /* Assume it's a ID3 document, i.e. 2-line MRZ. */

      /* line 1, pos 3 to 5 */
      this.issuingState = readCountry(dataIn);

      /* line 1, pos 6 to 44 */
      readNameIdentifiers(readString(dataIn, 39));

      /* line 2 */
      this.documentNumber = trimFillerChars(readString(dataIn, 9));
      this.documentNumberCheckDigit = (char)dataIn.readUnsignedByte();
      this.nationality = readCountry(dataIn);
      this.dateOfBirth = readDateOfBirth(dataIn);
      this.dateOfBirthCheckDigit = (char)dataIn.readUnsignedByte();
      this.gender = readGender(dataIn);
      this.dateOfExpiry = readDateOfExpiry(dataIn);
      this.dateOfExpiryCheckDigit = (char)dataIn.readUnsignedByte();
      String personalNumber = readStringWithFillers(dataIn, 14);
      char personalNumberCheckDigit = (char)dataIn.readUnsignedByte();
      this.optionalData1 = mrzFormat(personalNumber, 14) + personalNumberCheckDigit;
      this.compositeCheckDigit = (char)dataIn.readUnsignedByte();
    }
  }

  /**
   * Writes the MRZ to an output stream.
   * This just outputs the MRZ characters, and does not add newlines.
   *
   * @param outputStream the output stream to write to
   */
  @Override
  public void writeObject(OutputStream outputStream) throws IOException {
    DataOutputStream dataOut = new DataOutputStream(outputStream);
    writeDocumentType(dataOut);
    if (documentType == DOC_TYPE_ID1) {
      /* Assume it's an ID1 document */

      /* top line */
      writeIssuingState(dataOut);
      if (documentNumber.length() > 9 && equalsModuloFillerChars(optionalData1, "")) {
        /*
         * If document number has more than 9 character, the 9 principal
         * character shall be shown in the MRZ in character positions 1 to 9.
         * They shall be followed by a filler character instead of a check
         * digit to indicate a truncated number. The remaining character of
         * the document number shall be shown at the beginning of the field
         * reserved of optional data element (character position 29 to 35 of
         * the lower machine readable line) followed by a check digit and a
         * filler character.
         *
         * Corresponds to Doc 9303 pt 3 vol 1 page V-10 (note j) (FIXED by Paulo Assumcao)
         *
         * Also see R3-p1_v2_sIV_0041 in Supplement to Doc 9303, release 11.
         */
        writeString(documentNumber.substring(0, 9), dataOut, 9);
        dataOut.write('<'); /* NOTE: instead of check digit */
        writeString(documentNumber.substring(9, documentNumber.length()) + documentNumberCheckDigit + "<", dataOut, 15);
      } else {
        writeString(documentNumber, dataOut, 9); /* FIXME: max size of field */
        dataOut.write(documentNumberCheckDigit);
        writeString(optionalData1, dataOut, 15); /* FIXME: max size of field */
      }

      /* middle line */
      writeDateOfBirth(dataOut);
      dataOut.write(dateOfBirthCheckDigit);
      writeGender(dataOut);
      writeDateOfExpiry(dataOut);
      dataOut.write(dateOfExpiryCheckDigit);
      writeNationality(dataOut);
      writeString(optionalData2, dataOut, 11);
      dataOut.write(compositeCheckDigit);

      /* bottom line */
      writeName(dataOut, 30);
    } else {
      /* Assume it's a ID3 document */

      /* top line */
      writeIssuingState(dataOut);
      writeName(dataOut, 39);

      /* bottom line */
      writeString(documentNumber, dataOut, 9);
      dataOut.write(documentNumberCheckDigit);
      writeNationality(dataOut);
      writeDateOfBirth(dataOut);
      dataOut.write(dateOfBirthCheckDigit);
      writeGender(dataOut);
      writeDateOfExpiry(dataOut);
      dataOut.write(dateOfExpiryCheckDigit);
      writeString(optionalData1, dataOut, 15); /* NOTE: already includes check digit */
      dataOut.write(compositeCheckDigit);
    }
  }

  /**
   * Returns the date of birth of the passport holder.
   *
   * @return date of birth
   */
  public String getDateOfBirth() {
    return dateOfBirth;
  }

  /**
   * Sets the date of birth.
   *
   * @param dateOfBirth new date of birth
   */
  public void setDateOfBirth(String dateOfBirth) {
    this.dateOfBirth = dateOfBirth;
    checkDigit();
  }

  /**
   * Returns the date of expiry.
   *
   * @return the date of expiry
   */
  public String getDateOfExpiry() {
    return dateOfExpiry;
  }

  /**
   * Sets the date of expiry.
   *
   * @param dateOfExpiry new date of expiry
   */
  public void setDateOfExpiry(String dateOfExpiry) {
    this.dateOfExpiry = dateOfExpiry;
    checkDigit();
  }

  /**
   * Returns the document number.
   *
   * @return document number
   */
  public String getDocumentNumber() {
    return documentNumber;
  }

  /**
   * Sets the document number.
   *
   * @param documentNumber new document number
   */
  public void setDocumentNumber(String documentNumber) {
    this.documentNumber = documentNumber.trim();
    checkDigit();
  }

  /**
   * Returns the document type.
   *
   * @return document type
   */
  public int getDocumentType() {
    return documentType;
  }

  /**
   * Returns the document type.
   *
   * @return document type
   */
  public String getDocumentCode() {
    return documentCode;
  }

  /**
   * Sets the document code.
   *
   * @param documentCode the new document code
   */
  public void setDocumentCode(String documentCode) {
    this.documentCode = documentCode;
    this.documentType = getDocumentTypeFromDocumentCode(documentCode);
    if (documentType == DOC_TYPE_ID1 && optionalData2 == null) {
      optionalData2 = "";
    }
    /* FIXME: need to adjust some other lengths if we go from ID1 to ID3 or back... */
  }

  /**
   * Returns the issuing state as a 3 letter code.
   *
   * @return the issuing state
   */
  public String getIssuingState() {
    return issuingState;
  }

  /**
   * Sets the issuing state.
   *
   * @param issuingState new issuing state
   */
  public void setIssuingState(String issuingState) {
    this.issuingState = issuingState;
    checkDigit();
  }

  /**
   * Returns the passport holder's last name.
   *
   * @return name
   */
  public String getPrimaryIdentifier() {
    return primaryIdentifier;
  }

  /**
   * Sets the passport holder's last name.
   *
   * @param primaryIdentifier new primary identifier
   */
  public void setPrimaryIdentifier(String primaryIdentifier) {
    this.primaryIdentifier = primaryIdentifier.trim();
    checkDigit();
  }

  /**
   * Returns the document holder's first names.
   *
   * @return the secondary identifier
   */
  public String getSecondaryIdentifier() {
    return secondaryIdentifier;
  }

  /**
   * Returns the document holder's first names.
   *
   * @return first names
   */
  public String[] getSecondaryIdentifierComponents() {
    return secondaryIdentifier.split(" |<");
  }

  /**
   * Sets the passport holder's first names.
   *
   * @param secondaryIdentifiers new secondary identifiers
   */
  public void setSecondaryIdentifierComponents(String[] secondaryIdentifiers) {
    if (secondaryIdentifiers == null) {
      this.secondaryIdentifier = null;
    } else {
      StringBuilder stringBuilder = new StringBuilder();
      for (int i = 0; i < secondaryIdentifiers.length; i++) {
        stringBuilder.append(secondaryIdentifiers[i]);
        if (i < secondaryIdentifiers.length - 1) {
          stringBuilder.append('<');
        }
      }
    }
    checkDigit();
  }

  /**
   * Sets the passport holder's first names.
   *
   * @param secondaryIdentifiers new secondary identifiers
   */
  public void setSecondaryIdentifiers(String secondaryIdentifiers) {
    readSecondaryIdentifiers(secondaryIdentifiers.trim());
    checkDigit();
  }

  /**
   * Returns the passport holder's nationality as a 3 digit code.
   *
   * @return a country
   */
  public String getNationality() {
    return nationality;
  }

  /**
   * Sets the passport holder's nationality.
   *
   * @param nationality new nationality
   */
  public void setNationality(String nationality) {
    this.nationality = nationality;
    checkDigit();
  }

  /**
   * Returns the personal number (if a personal number is encoded in optional data 1).
   *
   * @return personal number
   */
  public String getPersonalNumber() {
    if (optionalData1 == null) {
      return null;
    }
    if (optionalData1.length() > 14) {
      return trimFillerChars(optionalData1.substring(0, 14));
    } else {
      return trimFillerChars(optionalData1);
    }
  }

  /**
   * Sets the personal number.
   *
   * @param personalNumber new personal number
   */
  public void setPersonalNumber(String personalNumber) {
    if (personalNumber == null || personalNumber.length() > 14) {
      throw new IllegalArgumentException("Wrong personal number");
    }
    this.optionalData1 = mrzFormat(personalNumber, 14) + checkDigit(personalNumber, true);
  }

  /**
   * Returns the contents of the first optional data field for ID-1 and ID-3 style MRZs.
   *
   * @return optional data 1
   */
  public String getOptionalData1() {
    return optionalData1;
  }

  /**
   * Returns the contents of the second optional data field for ID-1 style MRZs.
   *
   * @return optional data 2
   */
  public String getOptionalData2() {
    return optionalData2;
  }

  /**
   * Sets the contents for the second optional data field for ID-1 style MRZs.
   *
   * @param optionalData2 optional data 2
   */
  public void setOptionalData2(String optionalData2) {
    this.optionalData2 = trimFillerChars(optionalData2);
    checkDigit();
  }

  /**
   * Returns the passport holder's gender.
   *
   * @return gender
   */
  public Gender getGender() {
    return gender;
  }

  /**
   * Sets the gender.
   *
   * @param gender new gender
   */
  public void setGender(Gender gender) {
    this.gender = gender;
    checkDigit();
  }

  /**
   * Creates a textual representation of this MRZ.
   * This is the 2 or 3 line representation
   * (depending on the document type) as it
   * appears in the document. All lines end in
   * a newline char.
   *
   * @return the MRZ as text
   *
   * @see Object#toString()
   */
  @Override
  public String toString() {
    try {
      String str = new String(getEncoded(), "UTF-8");
      switch(str.length()) {
        case 90: /* ID1 */
          return str.substring(0, 30) + "\n"
          + str.substring(30, 60) + "\n"
          + str.substring(60, 90) + "\n";
        case 88: /* ID3 */
          return str.substring(0, 44) + "\n"
          + str.substring(44, 88) + "\n";
        default:
          /* TODO: consider throwing an exception in this case. */
          return str;
      }
    } catch (UnsupportedEncodingException uee) {
      throw new IllegalStateException(uee);
    }
  }

  /**
   * Returns the hash code for this MRZ info.
   *
   * @return the hash code
   */
  @Override
  public int hashCode() {
    return 2 * toString().hashCode() + 53;
  }

  /**
   * Whether this MRZ info is identical to some other object.
   *
   * @param obj the other object
   *
   * @return a boolean
   */
  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (!(obj.getClass().equals(this.getClass()))) {
      return false;
    }

    MRZInfo other = (MRZInfo)obj;

    return
        ((documentCode == null && other.documentCode == null) || documentCode !=  null && documentCode.equals(other.documentCode))
        && ((issuingState == null && other.issuingState == null) || issuingState != null && issuingState.equals(other.issuingState))
        && ((primaryIdentifier == null && other.primaryIdentifier == null) || primaryIdentifier != null && primaryIdentifier.equals(other.primaryIdentifier))
        && ((secondaryIdentifier == null && other.secondaryIdentifier == null) || equalsModuloFillerChars(secondaryIdentifier, other.secondaryIdentifier))
        && ((nationality == null && other.nationality == null) || nationality != null && nationality.equals(other.nationality))
        && ((documentNumber == null && other.documentNumber == null) || documentNumber != null && documentNumber.equals(other.documentNumber))
        && ((optionalData1 == null && other.optionalData1 == null) || optionalData1 != null && optionalData1.equals(other.optionalData1) || getPersonalNumber().equals(other.getPersonalNumber()))
        && ((dateOfBirth == null && other.dateOfBirth == null) || dateOfBirth != null && dateOfBirth.equals(other.dateOfBirth))
        && ((gender == null && other.gender == null) || gender != null && gender.equals(other.gender))
        && ((dateOfExpiry == null && other.dateOfExpiry == null) || dateOfExpiry != null && dateOfExpiry.equals(other.dateOfExpiry))
        && ((optionalData2 == null && other.optionalData2 == null) || optionalData2 != null && equalsModuloFillerChars(optionalData2, other.optionalData2));
  }

  /**
   * Computes the 7-3-1 check digit for part of the MRZ.
   *
   * @param str a part of the MRZ.
   *
   * @return the resulting check digit (in '0' - '9')
   */
  public static char checkDigit(String str) {
    return checkDigit(str, false);
  }

  /* ONLY PRIVATE METHODS BELOW */

  /**
   * Sets the name identifiers (primary and secondary identifier) based on
   * the name in the MRZ.
   *
   * @param mrzNameString the name field as it occurs in the MRZ
   */
  private void readNameIdentifiers(String mrzNameString) {
    int delimIndex = mrzNameString.indexOf("<<");
    if (delimIndex < 0) {
      /* Only a primary identifier. */
      primaryIdentifier = trimFillerChars(mrzNameString);
      this.secondaryIdentifier = ""; /* FIXME: Or would null be a better value in case there is no secondary identifier? -- MO */
      return;
    }
    primaryIdentifier = trimFillerChars(mrzNameString.substring(0, delimIndex));
    String rest = mrzNameString.substring(mrzNameString.indexOf("<<") + 2);
    readSecondaryIdentifiers(rest);
  }

  /**
   * Sets the secondary identifier.
   *
   * @param secondaryIdentifier the new secondary identifier
   */
  private void readSecondaryIdentifiers(String secondaryIdentifier) {
    this.secondaryIdentifier = secondaryIdentifier;
  }

  /**
   * Writes a MRZ string to a stream, optionally formatting the MRZ string.
   *
   * @param string the string to write
   * @param dataOutputStream the stream to write to
   * @param width the width of the MRZ field (the string will be augmented with trailing fillers)
   *
   * @throws IOException on error writing to the stream
   */
  private void writeString(String string, DataOutputStream dataOutputStream, int width) throws IOException {
    dataOutputStream.write(mrzFormat(string, width).getBytes("UTF-8"));
  }

  /**
   * Writes the issuing state to an stream.
   *
   * @param dataOutputStream the stream to write to
   *
   * @throws IOException on error writing to the stream
   */
  private void writeIssuingState(DataOutputStream dataOutputStream) throws IOException {
    dataOutputStream.write(issuingState.getBytes("UTF-8"));
  }

  /**
   * Writes the date of expiry to a stream.
   *
   * @param dateOutputStream the stream to write to
   *
   * @throws IOException on error writing to the stream
   */
  private void writeDateOfExpiry(DataOutputStream dateOutputStream) throws IOException {
    dateOutputStream.write(dateOfExpiry.getBytes("UTF-8"));
  }

  /**
   * Writes the gender to a stream.
   *
   * @param dataOutputStream the stream to write to
   *
   * @throws IOException on error writing to the stream
   */
  private void writeGender(DataOutputStream dataOutputStream) throws IOException {
    dataOutputStream.write(genderToString(gender).getBytes("UTF-8"));
  }

  /**
   * Writes the data of birth to a stream.
   *
   * @param dataOutputStream the stream to write to
   *
   * @throws IOException on error writing to the stream
   */
  private void writeDateOfBirth(DataOutputStream dataOutputStream) throws IOException {
    dataOutputStream.write(dateOfBirth.getBytes("UTF-8"));
  }

  /**
   * Writes the nationality to a stream.
   *
   * @param dataOutputStream the stream to write to
   *
   * @throws IOException on error writing to the stream
   */
  private void writeNationality(DataOutputStream dataOutputStream) throws IOException {
    dataOutputStream.write(nationality.getBytes("UTF-8"));
  }

  /**
   * Writes the name to a stream.
   *
   * @param dataOutputStream the stream to write to
   * @param width the width of the field
   *
   * @throws IOException on error writing to the stream
   */
  private void writeName(DataOutputStream dataOutputStream, int width) throws IOException {
    dataOutputStream.write(nameToString(primaryIdentifier, secondaryIdentifier, width).getBytes("UTF-8"));
  }

  /**
   * Write the document type to a stream.
   *
   * @param dataOutputStream the stream to write to
   *
   * @throws IOException on error writing to the stream
   */
  private void writeDocumentType(DataOutputStream dataOutputStream) throws IOException {
    writeString(documentCode, dataOutputStream, 2);
  }

  /**
   * Converts a gender to a string to be used in an MRZ.
   *
   * @param gender the gender
   *
   * @return a string to be used in an MRZ
   */
  private static String genderToString(Gender gender) {
    switch (gender) {
      case MALE:
        return "M";
      case FEMALE:
        return "F";
      default:
        return "<";
    }
  }

  /**
   * Converts the name (primary and secondary identifier) to a single MRZ formatted name
   * field of the given length.
   *
   * @param primaryIdentifier the primary identifier part of the name
   * @param secondaryIdentifier the secondary identifier part of the name
   * @param width the width of the resulting MRZ formatted string
   *
   * @return the string containing the MRZ formatted name field
   */
  private static String nameToString(String primaryIdentifier, String secondaryIdentifier, int width) {
    String[] primaryComponents = primaryIdentifier.split(" |<");
    String[] secondaryComponents = secondaryIdentifier == null || secondaryIdentifier.trim().isEmpty() ? new String[0] : secondaryIdentifier.split(" |<");

    StringBuilder name = new StringBuilder();
    boolean isFirstPrimaryComponent = true;
    for (String primaryComponent: primaryComponents) {
      if (isFirstPrimaryComponent) {
        isFirstPrimaryComponent = false;
      } else {
        name.append('<');
      }
      name.append(primaryComponent);
    }

    if (secondaryIdentifier != null && !secondaryIdentifier.trim().isEmpty()) {
      name.append("<<");
      boolean isFirstSecondaryComponent = true;
      for (String secondaryComponent: secondaryComponents) {
        if (isFirstSecondaryComponent) {
          isFirstSecondaryComponent = false;
        } else {
          name.append('<');
        }
        name.append(secondaryComponent);
      }
    }

    return mrzFormat(name.toString(), width);
  }

  /**
   * Reads a string including fillers.
   *
   * @param inputStream the stream to read from
   * @param count the length of the field
   *
   * @return the string
   *
   * @throws IOException on error reading from the stream
   */
  private String readStringWithFillers(DataInputStream inputStream, int count) throws IOException {
    return trimFillerChars(readString(inputStream, count));
  }

  /**
   * Reads the issuing state as a three letter string.
   *
   * @param inputStream the stream to read from
   *
   * @return a string of length 3 containing an abbreviation
   *         of the issuing state or organization
   *
   * @throws IOException error reading from the stream
   */
  private String readCountry(DataInputStream inputStream) throws IOException {
    return readString(inputStream, 3);
  }

  /**
   * Reads the 1 letter gender information.
   *
   * @param inputStream input source
   *
   * @return the gender of the passport holder
   *
   * @throws IOException if something goes wrong
   */
  private Gender readGender(DataInputStream inputStream) throws IOException {
    String genderStr = readString(inputStream, 1);
    if ("M".equalsIgnoreCase(genderStr)) {
      return Gender.MALE;
    }
    if ("F".equalsIgnoreCase(genderStr)) {
      return Gender.FEMALE;
    }
    return Gender.UNKNOWN;
  }

  /**
   * Reads the date of birth of the passport holder.
   * As only the rightmost two digits are stored,
   * the assumption that this is a date in the recent
   * past is made.
   *
   * @param inputStream the stream to read from
   *
   * @return the date of birth
   *
   * @throws IOException if something goes wrong
   * @throws NumberFormatException if a data could not be constructed
   */
  private String readDateOfBirth(DataInputStream inputStream) throws IOException, NumberFormatException {
    return readString(inputStream, 6);
  }

  /**
   * Reads the date of expiry of this document.
   * As only the rightmost two digits are stored,
   * the assumption that this is a date in the near
   * future is made.
   *
   * @param inputStream the stream to read from
   *
   * @return the date of expiry
   *
   * @throws IOException if something goes wrong
   * @throws NumberFormatException if a date could not be constructed
   */
  private String readDateOfExpiry(DataInputStream inputStream) throws IOException {
    return readString(inputStream, 6);
  }

  /**
   * Reads a fixed length string from a stream.
   *
   * @param inputStream the stream to read from
   * @param count the fixed length
   *
   * @return the string that was read
   *
   * @throws IOException on error reading from the stream
   */
  private String readString(DataInputStream inputStream, int count) throws IOException {
    byte[] data = new byte[count];
    inputStream.readFully(data);
    return new String(data).trim();
  }

  /**
   * Reformats the input string such that it
   * only contains ['A'-'Z'], ['0'-'9'], '<' characters
   * by replacing other characters with '<'.
   * Also extends to the given length by adding '<' to the right.
   *
   * @param str the input string
   * @param width the (minimal) width of the result
   *
   * @return the reformatted string
   */
  private static String mrzFormat(String str, int width) {
    if (str == null) {
      return "";
    }
    if (str.length() > width) {
      throw new IllegalArgumentException("Argument too wide (" + str.length() + " > " + width + ")");
    }
    str = str.toUpperCase().trim();
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      if (MRZ_CHARS.indexOf(c) == -1) {
        result.append('<');
      } else {
        result.append(c);
      }
    }
    while (result.length() < width) {
      result.append("<");
    }
    return result.toString();
  }

  /**
   * Tests equality of two MRZ string while ignoring extra filler characters.
   *
   * @param str1 an MRZ string
   * @param str2 another MRZ string
   *
   * @return a boolean indicating whether the strings are equal modulo filler characters
   */
  public static boolean equalsModuloFillerChars(String str1, String str2) {
    if (str1 == str2) {
      return true;
    }
    if (str1 == null) {
      str1 = "";
    }
    if (str2 == null) {
      str2 = "";
    }

    int length = Math.max(str1.length(), str2.length());
    return mrzFormat(str1, length).equals(mrzFormat(str2, length));
  }

  /**
   * Determines the document type based on the document code (the first two characters of the MRZ).
   *
   * ICAO Doc 9303 part 3 vol 1 defines MRTDs with 3-line MRZs,
   * in this case the document code starts with "A", "C", or "I"
   * according to note j to Section 6.6 (page V-9).
   *
   * ICAO Doc 9303 part 2 defines MRVs with 2-line MRZs,
   * in this case the document code starts with "V".
   *
   * ICAO Doc 9303 part 1 vol 1 defines MRPs with 2-line MRZs,
   * in this case the document code starts with "P"
   * according to Section 9.6 (page IV-15).
   *
   * @param documentCode a two letter code
   *
   * @return a document type, one of {@link #DOC_TYPE_ID1}, {@link #DOC_TYPE_ID2},
   * 			{@link #DOC_TYPE_ID3}, or {@link #DOC_TYPE_UNSPECIFIED}
   */
  private static int getDocumentTypeFromDocumentCode(String documentCode) {
    if (documentCode == null || documentCode.length() < 1 || documentCode.length() > 2) {
      throw new IllegalArgumentException("Was expecting 1 or 2 digit document code, got " + documentCode);
    }
    if (documentCode.startsWith("A")
        || documentCode.startsWith("C")
        || documentCode.startsWith("I")) {
      /* MRTD according to ICAO Doc 9303 part 3 vol 1 */
      return DOC_TYPE_ID1;
    } else if (documentCode.startsWith("V")) {
      /* MRV according to ICAO Doc 9303 part 2 */
      return DOC_TYPE_ID1;
    } else if (documentCode.startsWith("P")) {
      /* MRP according to ICAO Doc 9303 part 1 vol 1 */
      return DOC_TYPE_ID3;
    }
    return DOC_TYPE_UNSPECIFIED;
  }

  /**
   * Replaces '<' with ' ' and trims leading and trailing whitespace.
   *
   * @param str the string to read from
   *
   * @return a trimmed string
   */
  private static String trimFillerChars(String str) {
    byte[] chars = str.trim().getBytes();
    for (int i = 0; i < chars.length; i++) {
      if (chars[i] == '<') {
        chars[i] = ' ';
      }
    }
    return (new String(chars)).trim();
  }

  /**
   * Updates the check digit fields for document number,
   * date of birth, date of expiry, and composite.
   */
  private void checkDigit() {
    this.documentNumberCheckDigit = checkDigit(documentNumber);
    this.dateOfBirthCheckDigit = checkDigit(dateOfBirth);
    this.dateOfExpiryCheckDigit = checkDigit(dateOfExpiry);

    if (optionalData1.length() < 15) {
      String personalNumber = mrzFormat(optionalData1, 14);
      char personalNumberCheckDigit = checkDigit(mrzFormat(optionalData1, 14), true); /* FIXME: Uses '<' over '0'. Where specified? */
      optionalData1 = personalNumber + personalNumberCheckDigit;
    }

    this.compositeCheckDigit = checkDigit(getComposite(documentType));
  }

  /**
   * Returns the composite part over which the composite check digit is computed.
   *
   * @param documentType the type of document, either {@code DOC_TYPE_ID1} or {@code DOC_TYPE_ID3}
   *
   * @return a string with the composite part
   */
  private String getComposite(int documentType) {
    StringBuilder composite = new StringBuilder();
    if (documentType == DOC_TYPE_ID1) {
      /*
       * Based on 6.6 in Part V of Doc 9303 Part 3 Vol 1.
       * Composite check digit in position 30 is computed over:
       *
       * Upper line:
       * 6-30, i.e., documentNumber, documentNumberCheckDigit, personalNumber(15)
       *
       * Middle line:
       * 1-7, i.e., dateOfBirth, dateOfBirthCheckDigit
       * 9-15, i.e., dateOfExpiry, dateOfExpiryCheckDigit
       * 19-29, i.e., optionalData2(11)
       */
      int documentNumberLength = documentNumber.length();
      if (documentNumberLength <= 9) {
        composite.append(mrzFormat(documentNumber, 9));
        composite.append(documentNumberCheckDigit);
        composite.append(mrzFormat(optionalData1, 15));
      } else {
        /* Document number, first 9 characters. */
        composite.append(documentNumber.substring(0, 9));
        composite.append("<"); /* Filler instead of check digit. */

        /* Remainder of document number. */
        String documentNumberRemainder = documentNumber.substring(9);
        composite.append(documentNumberRemainder);
        composite.append(documentNumberCheckDigit);

        /* Remainder of optional data 1 (removing any prefix). */
        String optionalData1Remainder = mrzFormat(optionalData1, 15).substring(documentNumberRemainder.length() + 1);
        composite.append(mrzFormat(optionalData1Remainder, optionalData1Remainder.length()));
      }
      composite.append(dateOfBirth);
      composite.append(dateOfBirthCheckDigit);
      composite.append(dateOfExpiry);
      composite.append(dateOfExpiryCheckDigit);
      composite.append(mrzFormat(optionalData2, 11));
    } else {
      /* Must be ID3. */
      /* Composite check digit lower line: 1-10, 14-20, 22-43. */
      composite.append(documentNumber);
      composite.append(documentNumberCheckDigit);
      composite.append(dateOfBirth);
      composite.append(dateOfBirthCheckDigit);
      composite.append(dateOfExpiry);
      composite.append(dateOfExpiryCheckDigit);
      composite.append(mrzFormat(optionalData1, 15));
    }

    return composite.toString();
  }

  /**
   * Computes the 7-3-1 check digit for part of the MRZ.
   * If {@code preferFillerOverZero} is {@code true} then '<' will be
   * returned on check digit 0.
   *
   * @param str a part of the MRZ
   * @param preferFillerOverZero a boolean indicating whether fillers should be preferred
   *
   * @return the resulting check digit (in '0' - '9', '<')
   */
  private static char checkDigit(String str, boolean preferFillerOverZero) {
    try {
      byte[] chars = str == null ? new byte[] { } : str.getBytes("UTF-8");
      int[] weights = { 7, 3, 1 };
      int result = 0;
      for (int i = 0; i < chars.length; i++) {
        result = (result + weights[i % 3] * decodeMRZDigit(chars[i])) % 10;
      }
      String checkDigitString = Integer.toString(result);
      if (checkDigitString.length() != 1) {
        throw new IllegalStateException("Error in computing check digit."); /* NOTE: Never happens. */
      }
      char checkDigit = (char)checkDigitString.getBytes("UTF-8")[0];
      if (preferFillerOverZero && checkDigit == '0') {
        checkDigit = '<';
      }
      return checkDigit;
    } catch (NumberFormatException nfe) {
      /* NOTE: never happens. */
      throw new IllegalStateException("Error in computing check digit", nfe);
    } catch (UnsupportedEncodingException usee) {
      /* NOTE: never happens. */
      throw new IllegalStateException("Error in computing check digit", usee);
    } catch (Exception e) {
      throw new IllegalArgumentException("Error in computing check digit", e);
    }
  }

  /**
   * Looks up the numerical value for MRZ characters. In order to be able
   * to compute check digits.
   *
   * @param ch a character from the MRZ.
   *
   * @return the numerical value of the character.
   *
   * @throws NumberFormatException if <code>ch</code> is not a valid MRZ
   *                               character.
   */
  private static int decodeMRZDigit(byte ch) {
    switch (ch) {
      case '<':
      case '0':
        return 0;
      case '1':
        return 1;
      case '2':
        return 2;
      case '3':
        return 3;
      case '4':
        return 4;
      case '5':
        return 5;
      case '6':
        return 6;
      case '7':
        return 7;
      case '8':
        return 8;
      case '9':
        return 9;
      case 'a':
      case 'A':
        return 10;
      case 'b':
      case 'B':
        return 11;
      case 'c':
      case 'C':
        return 12;
      case 'd':
      case 'D':
        return 13;
      case 'e':
      case 'E':
        return 14;
      case 'f':
      case 'F':
        return 15;
      case 'g':
      case 'G':
        return 16;
      case 'h':
      case 'H':
        return 17;
      case 'i':
      case 'I':
        return 18;
      case 'j':
      case 'J':
        return 19;
      case 'k':
      case 'K':
        return 20;
      case 'l':
      case 'L':
        return 21;
      case 'm':
      case 'M':
        return 22;
      case 'n':
      case 'N':
        return 23;
      case 'o':
      case 'O':
        return 24;
      case 'p':
      case 'P':
        return 25;
      case 'q':
      case 'Q':
        return 26;
      case 'r':
      case 'R':
        return 27;
      case 's':
      case 'S':
        return 28;
      case 't':
      case 'T':
        return 29;
      case 'u':
      case 'U':
        return 30;
      case 'v':
      case 'V':
        return 31;
      case 'w':
      case 'W':
        return 32;
      case 'x':
      case 'X':
        return 33;
      case 'y':
      case 'Y':
        return 34;
      case 'z':
      case 'Z':
        return 35;
      default:
        throw new NumberFormatException("Could not decode MRZ character " + ch + " ('" + Character.toString((char) ch) + "')");
    }
  }

  public String toString2() {
    return "MRZInfo{" +
            "documentType=" + documentType +
            ", documentCode='" + documentCode + '\'' +
            ", issuingState='" + issuingState + '\'' +
            ", primaryIdentifier='" + primaryIdentifier + '\'' +
            ", secondaryIdentifier='" + secondaryIdentifier + '\'' +
            ", nationality='" + nationality + '\'' +
            ", documentNumber='" + documentNumber + '\'' +
            ", dateOfBirth='" + dateOfBirth + '\'' +
            ", gender=" + gender +
            ", dateOfExpiry='" + dateOfExpiry + '\'' +
            ", documentNumberCheckDigit=" + documentNumberCheckDigit +
            ", dateOfBirthCheckDigit=" + dateOfBirthCheckDigit +
            ", dateOfExpiryCheckDigit=" + dateOfExpiryCheckDigit +
            ", compositeCheckDigit=" + compositeCheckDigit +
            ", optionalData1='" + optionalData1 + '\'' +
            ", optionalData2='" + optionalData2 + '\'' +
            '}';
  }
}
