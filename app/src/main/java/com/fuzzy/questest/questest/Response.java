package com.fuzzy.questest.questest;

public class Response {
    private int respCode;
    private String respMsg;
    private Object data;

    public Response() {
    }

    public Response(int respCode, String respMsg, Object data) {
        super();
        this.respCode = respCode;
        this.respMsg = respMsg;
        this.data = data;
    }

    public void setRespCode(int respCode) {
        this.respCode = respCode;
    }

    public void setRespMsg(String respMsg) {
        this.respMsg = respMsg;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public int getRespCode() {
        return respCode;
    }

    public String getRespMsg() {
        return respMsg;
    }

    public Object getData() {
        return data;
    }
}
