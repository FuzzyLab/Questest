package com.fuzzylabs.questest;

import java.util.List;

public class Response {
    private int respCode;
    private String respMsg;
    private User user;
    private List<Question> data;

    public void setRespCode(int respCode) {
        this.respCode = respCode;
    }

    public void setRespMsg(String respMsg) {
        this.respMsg = respMsg;
    }

    public void setData(List<Question> data) {
        this.data = data;
    }

    public int getRespCode() {
        return respCode;
    }

    public String getRespMsg() {
        return respMsg;
    }

    public List<Question> getData() {
        return data;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return user;
    }
}
