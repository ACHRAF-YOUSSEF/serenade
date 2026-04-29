package com.serenade.backend.domain.rating;

import com.serenade.backend.domain.rating.dto.RatingRequest;
import com.serenade.backend.domain.rating.dto.RatingResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    private final RatingService service;

    public RatingController(RatingService service) {
        this.service = service;
    }

    @PostMapping
    public RatingResponse rate(@Valid @RequestBody RatingRequest req, Authentication auth) {
        return service.upsert(UUID.fromString(auth.getName()), req);
    }
}
