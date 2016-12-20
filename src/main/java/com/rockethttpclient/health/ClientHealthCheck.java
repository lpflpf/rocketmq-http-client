package com.rockethttpclient.health;

import com.codahale.metrics.health.HealthCheck;

public class ClientHealthCheck extends HealthCheck {
    private final String nameServer;

    public ClientHealthCheck(String nameServer) {
        this.nameServer = nameServer;
    }

    @Override
    protected Result check() throws Exception {
        return Result.healthy();
    }
}