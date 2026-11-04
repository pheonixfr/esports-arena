package EsportsArena.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

@Service
public class EmailService {

    @Value("${google.script.url}")
    private String scriptUrl;

    @Value("${google.script.secret}")
    private String secret;

    @Value("${app.base-url}")
    private String baseUrl;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

    public void sendVerificationEmail(String recipient, String token) {

        String verificationLink =
                baseUrl + "/verify-email?token=" + token;

        String subject = "Verify your eSports Arena account";

        String body =
                "Welcome to eSports Arena!\n\n" +
                "Click the link below to verify your email address:\n\n" +
                verificationLink + "\n\n" +
                "This link expires in 30 minutes.\n\n" +
                "If you did not create this account, you can ignore this email.";

        String json = "{"
                + "\"secret\":\"" + escapeJson(secret) + "\","
                + "\"to\":\"" + escapeJson(recipient) + "\","
                + "\"subject\":\"" + escapeJson(subject) + "\","
                + "\"body\":\"" + escapeJson(body) + "\""
                + "}";

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(scriptUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            json, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200
                    || !response.body().contains("\"success\":true")) {
                throw new IllegalStateException(
                        "Email relay failed: " + response.statusCode()
                );
            }

        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Could not send verification email.", exception
            );
        }
    }

    private String escapeJson(String value) {
        return value.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}