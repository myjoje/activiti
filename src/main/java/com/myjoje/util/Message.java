package com.myjoje.util;

import java.io.Serializable;

public class Message implements Serializable {

    private int code;
    private Object data;
    private String msg;

    private Message(int code, Object data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
    }

    // 成功，传入数据
    public static Message success() {
        return new Message(1, null, "请求成功");
    }

    public static Message success(int code, String msg) {
        return new Message(code, null, msg);
    }

    public static Message success(String msg) {
        return new Message(1, null, msg);
    }

    public static Message success(String msg, Object data) {
        return new Message(1, data, msg);
    }

    // 失败，传入描述信息
    public static Message error(String msg) {
        return new Message(-1, null, msg);
    }

    public static Message error(int code, String msg) {
        return new Message(code, null, msg);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
