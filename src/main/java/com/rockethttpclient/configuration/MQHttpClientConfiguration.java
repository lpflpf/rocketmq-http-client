package com.rockethttpclient.configuration;

import io.dropwizard.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class MQHttpClientConfiguration extends Configuration {

    @JsonProperty
    public String getServerName() {
        return serverName;
    }

    @JsonProperty
    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    @NotEmpty
    private String serverName;

}