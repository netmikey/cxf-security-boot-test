
An example project that tests CXF/WSS4J features within Spring Boot with minimal overhead.

## The purpose

Our production project with SOAP-based APIs has grown a lot over the years. The complexity of the whole SOAP/WebService Security stack has grown to the point where it's really difficult to pinpoint a potential issue's cause, let alone provide the framework maintainers with a standalone working example that reproduces the issue.

That's why we ended up creating this minimalistic example project that is set to reproduce CXF/WSS4J related issues we encounter.

## Project layout

This should look familiar if you've used Gradle or Maven before, but just to make sure you find your way through the project:

- The minimalistic Spring Boot Application containing the example server implementation can be found in `src/main/java`.
- The test keystore as well as the example WSDL contract (containing the WS-Policies) can be found in `src/main/resources`.
- The actual unit tests along with the client implementation can be found in `src/test/java`.

## Building / Testing

This project's goal being to reproduce issues, it revolves around JUnit tests. We use Gradle as our build system. This project includes the Gradle Wrapper so in order to run the tests, place yourself into the project directory and run:

    ./gradlew test

## The tests

### `CxfAttachmentStreamingTest`

These tests send a request to the server. The server then returns a response that contains a binary attachment. The server is set up such that it takes some time to stream the attachment data. We expect chunked streaming to take place. That is: the client should obtain the response in chunks while the server is still streaming the attachment. That is checked by capturing the timestamps of when the client receives the initial response body and when the server finishes streaming. It is expected that the client starts receiving the response before the server finishes streaming.

Different tests in this class test this streaming hypothesis with different WebService Security Policies:

- without any security,
- with signing only,
- with encryption only,
- with signature and encryption.

In addition, the test class contains an `ENDPOINT_URL` constant. This can be used e.g. for conveniently placing an intercepting proxy to watch the wire, e.g. [tproxy](https://github.com/kevwan/tproxy) using `tproxy -p 8081 -r 127.0.0.1:8080 -t text` and setting `ENDPOINT_URL` to port `8081`.