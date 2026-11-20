package com.example.aiagent.llm;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;

@Component
public class OpenAiClient implements LlmClient {
    private static final String API_URL = "https://api.openai.com/v1/responses";

    private final String apiKey;
    private final String model;
    private final HttpClient httpClient;
    private final Gson gson;

    public OpenAiClient(
            @Value("${openai.api.key}") String apiKey,
            @Value("${openai.api.model}") String model) {
        this.apiKey = apiKey;
        this.model = model;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .build();
        this.gson = new Gson();
    }

    @Override
    public LlmResponse generate(List<ChatMessage> messages)
            throws IOException, InterruptedException {
        return generate(null, messages);
    }

    @Override
    public LlmResponse generate(String instructions, List<ChatMessage> messages)
            throws IOException, InterruptedException {
        if (messages == null || messages.isEmpty()) {
            throw new IllegalArgumentException("messages must contain at least one message");
        }

        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("model", model);
        if (instructions != null && !instructions.isBlank()) {
            requestBody.addProperty("instructions", instructions);
        }

        JsonObject textConfig = new JsonObject();
        requestBody.add("text", textConfig);

        JsonArray input = new JsonArray();
        for (ChatMessage chatMessage : messages) {
            if (chatMessage == null || chatMessage.role() == null || chatMessage.content() == null) {
                throw new IllegalArgumentException("messages cannot contain null values");
            }
            JsonObject message = new JsonObject();
            message.addProperty("role", chatMessage.role());
            message.addProperty("content", chatMessage.content());
            input.add(message);
        }
        requestBody.add("input", input);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(60))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(requestBody)))
                .build();

        HttpResponse<String> response = httpClient.send(
                request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("OpenAI API returned HTTP " + response.statusCode()
                    + ": " + response.body());
        }

        JsonObject responseJson;
        try {
            responseJson = JsonParser.parseString(response.body()).getAsJsonObject();
        } catch (RuntimeException exception) {
            throw new IOException("OpenAI API returned an invalid JSON response", exception);
        }

        JsonArray output = responseJson.getAsJsonArray("output");
        if (output == null) {
            throw new IOException("OpenAI API response did not contain output");
        }
        StringBuilder content = new StringBuilder();
        for (JsonElement element : output) {
            JsonObject item = element.getAsJsonObject();
            if (!item.has("type") || !"message".equals(item.get("type").getAsString())) {
                continue;
            }
            JsonArray parts = item.getAsJsonArray("content");
            if (parts == null) {
                continue;
            }
            for (JsonElement partElement : parts) {
                JsonObject part = partElement.getAsJsonObject();
                if (part.has("type") && "output_text".equals(part.get("type").getAsString())
                        && part.has("text")) {
                    content.append(part.get("text").getAsString());
                }
            }
        }

        JsonObject usage = responseJson.getAsJsonObject("usage");
        if (usage == null || !usage.has("input_tokens") || !usage.has("output_tokens")) {
            throw new IOException("OpenAI API response did not contain token usage");
        }
        return new LlmResponse(content.toString(),
                usage.get("input_tokens").getAsInt(),
                usage.get("output_tokens").getAsInt());
    }
}
