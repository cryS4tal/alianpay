package com.ylli.api.pay.enums;

public enum Version {
    Default("1.0"), CNT("1.1");
    private String version;

    Version(String version) {
        this.version = version;
    }

    public String getVersion() {
        return version;
    }
}
