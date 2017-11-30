package com.example.sherifhussien.phonalyzer;

/**
 * Created by sherifhussien on 11/26/17.
 */

public class Message {
    private String message;
    private String uuid;
    private boolean sender;

    public Message(String message,boolean sender){
        this.message=message;
        this.sender=sender;
    }

    public Message(String message,String uuid,boolean sender){
        this.message=message;
        this.uuid=uuid;
        this.sender=sender;
    }

    public String getMessage() {
        return message;
    }

    public String getUuid() {
        return uuid;
    }

    public boolean isSender() {
        return sender;
    }

    public String toString(){
        return message+" "+uuid+" "+sender;
    }
}
