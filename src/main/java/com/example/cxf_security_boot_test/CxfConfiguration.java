package com.example.cxf_security_boot_test;

import java.util.Properties;

import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.apache.cxf.message.Message;
import org.apache.cxf.rt.security.SecurityConstants;
import org.apache.wss4j.common.crypto.Merlin;
import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.example.cxf_security_boot_test.server.ExampleServiceImpl;

import jakarta.xml.ws.Endpoint;

@Configuration
public class CxfConfiguration {

    @Autowired
    private Bus bus;

    @Bean
    public Endpoint secureEndpoint(ExampleServiceImpl serviceImpl) {
        EndpointImpl endpoint = new EndpointImpl(bus, serviceImpl);

        endpoint.getProperties().put(Message.MTOM_ENABLED, true);
        endpoint.getProperties().put(Message.SCHEMA_VALIDATION_ENABLED, true);

        Properties encryptProps = new Properties();
        encryptProps.put("org.apache.ws.security.crypto.provider", Merlin.class.getName());
        encryptProps.put(Merlin.PREFIX + Merlin.KEYSTORE_TYPE, "pkcs12");
        encryptProps.put(Merlin.PREFIX + Merlin.KEYSTORE_PASSWORD, "test");
        encryptProps.put(Merlin.PREFIX + Merlin.KEYSTORE_FILE, "test.p12");
        encryptProps.put(Merlin.PREFIX + Merlin.KEYSTORE_ALIAS, "test");
        endpoint.getProperties().put(SecurityConstants.ENCRYPT_PROPERTIES, encryptProps);
        endpoint.getProperties().put(SecurityConstants.SIGNATURE_PROPERTIES, encryptProps);
        endpoint.getProperties().put(SecurityConstants.CALLBACK_HANDLER,
            (javax.security.auth.callback.CallbackHandler) (callbacks) -> {
                for (int i = 0; i < callbacks.length; i++) {
                    if (callbacks[i] instanceof WSPasswordCallback) {
                        ((WSPasswordCallback) callbacks[i]).setPassword("test");
                    } else {
                        throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
                    }
                }
            });

        endpoint.publish("/ExampleService/ExamplePort");
        return endpoint;
    }
}
