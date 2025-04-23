package com.example.cxf_security_boot_test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.Instant;
import java.util.Properties;
import java.util.function.Function;

import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.cxf.rt.security.SecurityConstants;
import org.apache.wss4j.common.crypto.Merlin;
import org.apache.wss4j.common.ext.WSPasswordCallback;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;

import com.example.cxf_security_boot_test.bindings.ExampleOperationRequestType;
import com.example.cxf_security_boot_test.bindings.ExampleOperationResponseType;
import com.example.cxf_security_boot_test.bindings.ExamplePortType;
import com.example.cxf_security_boot_test.bindings.ExampleService;
import com.example.cxf_security_boot_test.server.StreamingFinishedTimestampHolder;

import jakarta.xml.ws.BindingProvider;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
public class CxfAttachmentStreamingTest {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory
        .getLogger(CxfAttachmentStreamingTest.class);

    private static final String ENDPOINT_URL = "http://127.0.0.1:8081/services/ExampleService/ExamplePort";

    @Autowired
    private StreamingFinishedTimestampHolder streamingFinishedTimestampHolder;

    @BeforeEach
    public void beforeEach() {
        streamingFinishedTimestampHolder.reset();
    }

    @Test
    public void testStreamAttachmentNoWss() throws Exception {
        sendRequestUsingOperation((client) -> client.exampleOperationNoWss(new ExampleOperationRequestType()));
    }

    @Test
    public void testStreamAttachmentSignAndEncrypt() throws Exception {
        sendRequestUsingOperation((client) -> client.exampleOperationSignAndEncrypt(new ExampleOperationRequestType()));
    }

    @Test
    public void testStreamAttachmentSign() throws Exception {
        sendRequestUsingOperation((client) -> client.exampleOperationSign(new ExampleOperationRequestType()));
    }

    @Test
    public void testStreamAttachmentEncrypt() throws Exception {
        sendRequestUsingOperation((client) -> client.exampleOperationEncrypt(new ExampleOperationRequestType()));
    }

    public void sendRequestUsingOperation(Function<ExamplePortType, ExampleOperationResponseType> clientCallback)
        throws IOException {

        var service = new ExampleService();
        ExamplePortType port = service.getExamplePort();

        BindingProvider bp = (BindingProvider) port;
        bp.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, ENDPOINT_URL);

        Properties encryptProps = new Properties();
        encryptProps.put("org.apache.ws.security.crypto.provider", Merlin.class.getName());
        encryptProps.put(Merlin.PREFIX + Merlin.KEYSTORE_TYPE, "pkcs12");
        encryptProps.put(Merlin.PREFIX + Merlin.KEYSTORE_PASSWORD, "test");
        encryptProps.put(Merlin.PREFIX + Merlin.KEYSTORE_FILE, "test.p12");
        encryptProps.put(Merlin.PREFIX + Merlin.KEYSTORE_ALIAS, "test");
        bp.getRequestContext().put(SecurityConstants.ENCRYPT_PROPERTIES, encryptProps);
        bp.getRequestContext().put(SecurityConstants.SIGNATURE_PROPERTIES, encryptProps);
        bp.getRequestContext().put(SecurityConstants.CALLBACK_HANDLER,
            (javax.security.auth.callback.CallbackHandler) (callbacks) -> {
                for (int i = 0; i < callbacks.length; i++) {
                    if (callbacks[i] instanceof WSPasswordCallback) {
                        ((WSPasswordCallback) callbacks[i]).setPassword("test");
                    } else {
                        throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
                    }
                }
            });

        LOG.info("[Client] Sending request now...");
        ExampleOperationResponseType response = clientCallback.apply(port);
        Instant obtainedResponseInstant = Instant.now();
        LOG.info("[Client] Obtained response.");
        try (InputStreamReader isr = new InputStreamReader(response.getData().getInputStream());
            BufferedReader attachmentReader = new BufferedReader(isr)) {

            String read;
            while ((read = attachmentReader.readLine()) != null) {
                LOG.info("[Client] Read line: " + read);
            }
        }
        LOG.info("[Client] Done reading response.");

        Instant serverStreamingFinished = streamingFinishedTimestampHolder.getLastStreamingFinished();
        Assertions.assertNotNull(serverStreamingFinished, "The server should be done streaming by now.");
        String times = "Obtained Response: " + obtainedResponseInstant + ", Server Streaming Finished: "
            + serverStreamingFinished;
        LOG.info(times);
        Assertions.assertTrue(obtainedResponseInstant.isBefore(serverStreamingFinished),
            "The client only obtained the initial response body _after_ the server was done streaming."
                + " It should have been the other way around. " + times);
    }
}
