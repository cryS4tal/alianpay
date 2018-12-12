package com.ylli.api.pay.model;

public class Response {

    public String code;

    public String message;

    public String sign;

    //返回data类型。
    public String type;

    //具体待定义.
    public Object data;

    public Response(String code, String message, Object data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public Response(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public Response() {
    }

    public Response(String code, String message, String sign, String type, Object data) {
        this.code = code;
        this.message = message;
        this.sign = sign;
        this.type = type;
        this.data = data;
    }

    public Response(String code, String message, String sign, Object data) {
        this.code = code;
        this.message = message;
        this.sign = sign;
        this.data = data;
    }
}
