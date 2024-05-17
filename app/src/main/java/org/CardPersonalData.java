package org;

import com.fasterxml.jackson.annotation.JsonProperty;


public class CardPersonalData {
    @JsonProperty("mrz")
    private String mrz;

    @JsonProperty("faceImage")
    private byte[] faceImage;

    @JsonProperty("cardNumber")
    private String cardNumber;

    @JsonProperty("fullName")
    private String fullName;

    @JsonProperty("dateOfBirth")
    private String dateOfBirth;

    @JsonProperty("sex")
    private String sex;

    @JsonProperty("nationality")
    private String nationality;

    @JsonProperty("ethnicity")
    private String ethnicity;

    @JsonProperty("religion")
    private String religion;

    @JsonProperty("placeOfOrigin")
    private String placeOfOrigin;

    @JsonProperty("placeOfResidence")
    private String placeOfResidence;

    @JsonProperty("personalIdentification")
    private String personalIdentification;

    @JsonProperty("dateOfIssue")
    private String dateOfIssue;

    @JsonProperty("dateOfExpiry")
    private String dateOfExpiry;

    @JsonProperty("fatherName")
    private String fatherName;

    @JsonProperty("motherName")
    private String motherName;

    @JsonProperty("spouseName")
    private String spouseName;

    @JsonProperty("oldCardNumber")
    private String oldCardNumber;

    public String getMrz() {
        return mrz;
    }

    public void setMrz(String mrz) {
        this.mrz = mrz;
    }

    public byte[] getFaceImage() {
        return faceImage;
    }

    public void setFaceImage(byte[] faceImage) {
        this.faceImage = faceImage;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getEthnicity() {
        return ethnicity;
    }

    public void setEthnicity(String ethnicity) {
        this.ethnicity = ethnicity;
    }

    public String getReligion() {
        return religion;
    }

    public void setReligion(String religion) {
        this.religion = religion;
    }

    public String getPlaceOfOrigin() {
        return placeOfOrigin;
    }

    public void setPlaceOfOrigin(String placeOfOrigin) {
        this.placeOfOrigin = placeOfOrigin;
    }

    public String getPlaceOfResidence() {
        return placeOfResidence;
    }

    public void setPlaceOfResidence(String placeOfResidence) {
        this.placeOfResidence = placeOfResidence;
    }

    public String getPersonalIdentification() {
        return personalIdentification;
    }

    public void setPersonalIdentification(String personalIdentification) {
        this.personalIdentification = personalIdentification;
    }

    public String getDateOfIssue() {
        return dateOfIssue;
    }

    public void setDateOfIssue(String dateOfIssue) {
        this.dateOfIssue = dateOfIssue;
    }

    public String getDateOfExpiry() {
        return dateOfExpiry;
    }

    public void setDateOfExpiry(String dateOfExpiry) {
        this.dateOfExpiry = dateOfExpiry;
    }

    public String getFatherName() {
        return fatherName;
    }

    public void setFatherName(String fatherName) {
        this.fatherName = fatherName;
    }

    public String getMotherName() {
        return motherName;
    }

    public void setMotherName(String motherName) {
        this.motherName = motherName;
    }

    public String getSpouseName() {
        return spouseName;
    }

    public void setSpouseName(String spouseName) {
        this.spouseName = spouseName;
    }

    public String getOldCardNumber() {
        return oldCardNumber;
    }

    public void setOldCardNumber(String oldCardNumber) {
        this.oldCardNumber = oldCardNumber;
    }
}
