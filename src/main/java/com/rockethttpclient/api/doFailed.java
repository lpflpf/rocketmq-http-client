package com.rockethttpclient.api;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by lipengfeijs on 2016/12/16.
 */
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
