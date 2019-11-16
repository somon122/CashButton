package com.worldtechpoints.cashbutton;

public class UserInfo {

    private String uId;
    private String userName;
    private String PhoneNumber;
    private String countryName;

    public UserInfo() {
    }

    public UserInfo(String uId, String userName, String phoneNumber, String countryName) {
        this.uId = uId;
        this.userName = userName;
        PhoneNumber = phoneNumber;
        this.countryName = countryName;
    }

    public String getuId() {
        return uId;
    }

    public void setuId(String uId) {
        this.uId = uId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public String getCountryName() {
        return countryName;
    }

    public void setCountryName(String countryName) {
        this.countryName = countryName;
    }
}
