package com.gangbean.stockservice.controller;

import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class ProfileController {

    private final Environment environment;

    @GetMapping("/profile")
    public String profile() {
        List<String> profiles = Arrays.asList(environment.getActiveProfiles());
        List<String> prodProfiles = Arrays.asList("prod", "prod1", "prod2");
        return profiles.stream()
            .filter(prodProfiles::contains)
            .findAny()
            .orElse(profiles.isEmpty() ? "default" : profiles.get(0));
    }
}
