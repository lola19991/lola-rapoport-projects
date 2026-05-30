package com.lola.cloudguard.rest;

import com.lola.cloudguard.domain.ScanResult;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class ScanStore {
    private final Map<UUID, ScanResult> scans = new ConcurrentHashMap<>();

    public ScanResult save(ScanResult result) {
        scans.put(result.id(), result);
        return result;
    }

    public Optional<ScanResult> find(UUID id) {
        return Optional.ofNullable(scans.get(id));
    }
}
