package com.example.cxf_security_boot_test.server;

import java.time.Instant;

import org.springframework.stereotype.Component;

@Component
public class StreamingFinishedTimestampHolder {
    private Instant lastStreamingFinished;

    public synchronized void reset() {
        this.lastStreamingFinished = null;
    }

    public synchronized Instant getLastStreamingFinished() {
        return lastStreamingFinished;
    }

    public synchronized void setLastStreamingFinished(Instant lastStreamingFinished) {
        this.lastStreamingFinished = lastStreamingFinished;
    }
}
