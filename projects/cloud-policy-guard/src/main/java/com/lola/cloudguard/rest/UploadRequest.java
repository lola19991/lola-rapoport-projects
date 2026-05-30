package com.lola.cloudguard.rest;

public record UploadRequest(
        String content,
        String filename,
        String source
) {
}
