package com.serenade.backend.domain.sync;

import com.serenade.backend.domain.sync.dto.ChangesResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/changes")
public class ChangesController {

    private final ChangesService service;

    public ChangesController(ChangesService service) {
        this.service = service;
    }

    @GetMapping
    public ChangesResponse changes(
            Authentication auth,
            @RequestParam(defaultValue = "1970-01-01T00:00:00Z") Instant since,
            @RequestParam(defaultValue = "100") int limit
    ) {
        int boundedLimit = Math.clamp(limit, 1, 500);
        return service.changes(UUID.fromString(auth.getName()), since, boundedLimit);
    }
}
