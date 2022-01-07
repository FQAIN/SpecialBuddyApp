package com.trichain.specialbuddy.chat.model;

import java.util.ArrayList;


public class ListFriend {
    private ArrayList<Friend> listFriend;

    public ArrayList<Friend> getListFriend() {
        return listFriend;
    }

    public ListFriend(){
        listFriend = new ArrayList<>();
    }

    public String getAvataById(String id){
        for(Friend friend: listFriend){
            if(id.equals(friend.id)){
                return friend.avata;
            }
        }
        return "";
    }

    public ArrayList<Friend> filterFriends(CharSequence s){
        ArrayList<Friend> newList= new ArrayList<>();
        for (Friend f:listFriend
             ) {
            if (f.name.contains(s.toString())||f.email.contains(s.toString())){
                newList.add(f);
            }
        }
        return newList;
    }

    public void setListFriend(ArrayList<Friend> listFriend) {
        this.listFriend = listFriend;
    }
}
