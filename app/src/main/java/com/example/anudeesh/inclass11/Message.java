package com.example.anudeesh.inclass11;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Anudeesh on 11/14/2016.
 */
public class Message implements Serializable {
    String text, type, fname, lname, time, uid;

    public Message(String text, String type, String fname, String lname, String time, String uid) {
        this.text = text;
        this.type = type;
        this.fname = fname;
        this.lname = lname;
        this.time = time;
        this.uid = uid;
    }

    public Message() {

    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getLname() {
        return lname;
    }

    public void setLname(String lname) {
        this.lname = lname;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Map<String, Object> toMap() {
        HashMap<String,Object> result = new HashMap<>();
        result.put("uid",uid);
        result.put("fname",fname);
        result.put("lname",lname);
        result.put("text",text);
        result.put("type",type);
        result.put("time",time);

        return result;
    }

    @Override
    public String toString() {
        return "Message{" +
                "text='" + text + '\'' +
                ", type='" + type + '\'' +
                ", fname='" + fname + '\'' +
                ", lname='" + lname + '\'' +
                ", time='" + time + '\'' +
                ", uid='" + uid + '\'' +
                '}';
    }
}
