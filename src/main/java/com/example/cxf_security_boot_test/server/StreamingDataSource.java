package com.example.cxf_security_boot_test.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.time.Instant;
import java.util.concurrent.Executors;

import jakarta.activation.DataSource;

public class StreamingDataSource implements DataSource {
    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(StreamingDataSource.class);

    private final PipedInputStream pipedIn;

    private final PipedOutputStream pipedOut;

    public StreamingDataSource(StreamingFinishedTimestampHolder streamingFinishedTimestampHolder) {

        pipedIn = new PipedInputStream();
        try {
            pipedOut = new PipedOutputStream(pipedIn);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            try (PrintWriter pw = new PrintWriter(pipedOut)) {
                for (int i = 0; i < 15; i++) {
                    LOG.info("[Server DataSource] Printing 1kb block # " + i + " ...");
                    pw.append("" + i + "\n")
                        .append("a".repeat(1024))
                        .append("\n");
                    pw.flush();
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            streamingFinishedTimestampHolder.setLastStreamingFinished(Instant.now());
        });
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return pipedIn;
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new UnsupportedOperationException("DataHandler should not query the output stream");
    }

    @Override
    public String getContentType() {
        return "application/octet-stream";
    }

    @Override
    public String getName() {
        return "dataStream";
    }
}