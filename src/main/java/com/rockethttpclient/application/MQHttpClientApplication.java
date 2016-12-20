package com.rockethttpclient.application;

import com.rockethttpclient.configuration.MQHttpClientConfiguration;
import com.rockethttpclient.health.ClientHealthCheck;
import com.rockethttpclient.resources.MessageResource;
import io.dropwizard.Application;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;



public class MQHttpClientApplication extends Application<MQHttpClientConfiguration> {
    public static void main(String[] args) throws Exception {
        new MQHttpClientApplication().run(args);
    }

    @Override
    public void initialize(Bootstrap<MQHttpClientConfiguration> bootstrap) {
        // nothing to do yet
    }

    @Override
    public void run(MQHttpClientConfiguration configuration,
                    Environment environment) {
        final MessageResource resource = new MessageResource(configuration.getServerName());

        environment.jersey().register(resource);
        environment.healthChecks().register("mq", new ClientHealthCheck(configuration.getServerName()));
    }

}