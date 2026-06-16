package com.Gabriel.API_Banco.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class EmailHttpService {

    @Value("${resend.api.key}")
    private String resendApiKey;

    @Value("${email.from}")
    private String emailFrom;

    private final RestTemplate restTemplate = new RestTemplate();

    public void enviarRecuperacaoSenha(String para, String link, String senhaTemporaria) {

        String url = "https://api.resend.com/emails";

        String textoEmail =
                "Olá!\n\n" +
                        "Recebemos uma solicitação de recuperação de senha para sua conta na VisionPlus+.\n\n" +
                        "Sua senha temporária é:\n\n" +
                        senhaTemporaria + "\n\n" +
                        "Use essa senha para acessar sua conta.\n\n" +
                        "Link de acesso:\n" +
                        link + "\n\n" +
                        "Caso você não tenha solicitado essa recuperação, ignore este e-mail.";

        String bodyJson = """
                {
                  "from": "%s",
                  "to": ["%s"],
                  "subject": "Recuperação de senha - VisionPlus+",
                  "text": "%s"
                }
                """.formatted(
                emailFrom,
                para,
                textoEmail
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"")
                        .replace("\n", "\\n")
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(resendApiKey);

        HttpEntity<String> request = new HttpEntity<>(bodyJson, headers);

        ResponseEntity<String> response =
                restTemplate.postForEntity(url, request, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Erro ao enviar e-mail via Resend: " + response.getBody());
        }
    }
}
