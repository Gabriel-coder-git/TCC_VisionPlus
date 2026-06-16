package com.Gabriel.API_Banco.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.util.Map;

@Service
public class TurnstileService {

    @Value("${turnstile.secret.key}")
    private String secretKey;

    private final RestTemplate restTemplate = new RestTemplate();

    public boolean validarToken(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        String url = "https://challenges.cloudflare.com/turnstile/v0/siteverify";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("secret", secretKey);
        body.add("response", token);

        HttpEntity<MultiValueMap<String, String>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(url, request, Map.class);

        if (response.getBody() == null) {
            return false;
        }

        Object success = response.getBody().get("success");

        return Boolean.TRUE.equals(success);
    }
}
