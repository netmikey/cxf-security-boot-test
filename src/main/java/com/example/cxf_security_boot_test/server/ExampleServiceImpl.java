package com.example.cxf_security_boot_test.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.cxf_security_boot_test.bindings.ExampleOperationRequestType;
import com.example.cxf_security_boot_test.bindings.ExampleOperationResponseType;
import com.example.cxf_security_boot_test.bindings.ExamplePortType;

import jakarta.activation.DataHandler;
import jakarta.jws.WebService;
import jakarta.xml.ws.BindingType;

@Service
@WebService(
    name = "ExamplePortType",
    portName = "ExamplePort",
    serviceName = "ExampleService",
    targetNamespace = "http://example.org/v1",
    wsdlLocation = "com/example/cxf_security_boot_test/wsdl/ExampleService.wsdl",
    endpointInterface = "com.example.cxf_security_boot_test.bindings.ExamplePortType")
@BindingType("http://schemas.xmlsoap.org/wsdl/soap/http")
public class ExampleServiceImpl implements ExamplePortType {

    @Autowired
    private StreamingFinishedTimestampHolder streamingFinishedTimestampHolder;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ExampleServiceImpl.class);

    @Override
    public ExampleOperationResponseType exampleOperationSignAndEncrypt(ExampleOperationRequestType requestMessagePart) {
        return doExampleOperation();
    }

    @Override
    public ExampleOperationResponseType exampleOperationNoWss(ExampleOperationRequestType requestMessagePart) {
        return doExampleOperation();
    }

    @Override
    public ExampleOperationResponseType exampleOperationEncrypt(ExampleOperationRequestType requestMessagePart) {
        return doExampleOperation();
    }

    @Override
    public ExampleOperationResponseType exampleOperationSign(ExampleOperationRequestType requestMessagePart) {
        return doExampleOperation();
    }

    private ExampleOperationResponseType doExampleOperation() {
        LOG.info("[Server] Processing request...");
        ExampleOperationResponseType response = new ExampleOperationResponseType();
        response.setData(new DataHandler(new StreamingDataSource(streamingFinishedTimestampHolder)));
        LOG.info("[Server] Returning response");
        return response;
    }

}
