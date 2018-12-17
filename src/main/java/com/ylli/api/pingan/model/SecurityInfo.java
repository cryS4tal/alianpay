package com.ylli.api.pingan.model;

import java.io.Serializable;

/**
 * *********************************************************
 * Copyright © 2016 转折科技有限公司 Inc.All rights reserved.  *
 * ***********************************************************
 *
 * @Generator: IntelliJ IDEA
 * @Package: com.turn.bankgateway.bqp.dto
 * @Author: yangdf
 * @Date 2018/5/9 14:34
 * @Description: 渠道证书相关信息
 * @ModifyDetail
 * @ModifyDate
 */
public class SecurityInfo implements Serializable {

    private static final long serialVersionUID = -3190650595438740086L;
    /**
     * 证书路径
     **/
    private String certPath;
    /**
     * 证书密码
     **/
    private String certPwd;
    /**
     * MD5密钥
     **/
    private String secretKey;
    /**
     * 渠道公钥
     **/
    private String channelPubKey;
    /**
     * 转折公钥
     **/
    private String pubKey;
    /**
     * 转折私钥
     **/
    private String prvKey;
    /**
     * 渠道应用号
     */
    private String channelAppid;


    public String getChannelAppid() {
        return channelAppid;
    }

    public void setChannelAppid(String channelAppid) {
        this.channelAppid = channelAppid;
    }

    public String getCertPwd() {
        return certPwd;
    }

    public void setCertPwd(String certPwd) {
        this.certPwd = certPwd;
    }

    public String getCertPath() {
        return certPath;
    }

    public void setCertPath(String certPath) {
        this.certPath = certPath;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getChannelPubKey() {
        return channelPubKey;
    }

    public void setChannelPubKey(String channelPubKey) {
        this.channelPubKey = channelPubKey;
    }

    public String getPubKey() {
        return pubKey;
    }

    public void setPubKey(String pubKey) {
        this.pubKey = pubKey;
    }

    public String getPrvKey() {
        return prvKey;
    }

    public void setPrvKey(String prvKey) {
        this.prvKey = prvKey;
    }

    @Override
    public String toString() {
        return "SecurityInfo{" +
                "certPath='" + certPath + '\'' +
                ", secretKey='" + secretKey + '\'' +
                ", channelPubKey='" + channelPubKey + '\'' +
                ", pubKey='" + pubKey + '\'' +
                ", prvKey='" + prvKey + '\'' +
                '}';
    }
}
