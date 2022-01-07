package com.trichain.specialbuddy.chat.model;



public class User {
    public String id;
    public String name;
    public String email;
    public String avata;
    public String phone;
    public String dob;
    public String gender;
    public String disability;
    public String diary;
    public double mLat;
    public double mLong;
    public Status status;
    public Message message;


    public User(){
        status = new Status();
        message = new Message();
        status.isOnline = false;
        status.timestamp = 0;
        message.idReceiver = "0";
        message.idSender = "0";
        message.text = "";
        message.timestamp = 0;
    }
}
