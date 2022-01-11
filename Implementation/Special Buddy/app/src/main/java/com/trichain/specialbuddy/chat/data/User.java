package com.trichain.specialbuddy.chat.data;



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
    public Message message;


    public User(){
        message = new Message();
        message.idReceiver = "0";
        message.idSender = "0";
        message.text = "";
        message.timestamp = 0;
    }
}
