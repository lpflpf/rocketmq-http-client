package com.rockethttpclient.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class doFailed {
    private String code;
    private String info;

    @JsonProperty
    public String getCode() {
        return this.code;
    }

    @JsonProperty
    public String getInfo() {
        return this.info;
    }

    public doFailed(String code, String info){
        this.code = code;
        this.info = info;
    }
}
