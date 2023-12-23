import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Scanner;

public class OpenAiChat {

    // Load configuration from the JSON file
    private static final JsonObject config = loadConfigFromJson("chatConfig.json");
    private final Scanner scanner;

    public OpenAiChat() {
        // Constructor logic here
        this.scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        try {
            OpenAiChat openAiChat = new OpenAiChat();

            System.out.println("ChatBot: " + getConfigValue("greeting"));
            openAiChat.startChat();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startChat() {
        while (true) {
            System.out.print("You: ");
            String userMessage = getUserInput();

            if ("exit".equalsIgnoreCase(userMessage)) {
                System.out.println("ChatBot: " + getConfigValue("exitMessage"));
                break;
            }

            String chatResponse = getChatResponse(userMessage);
            System.out.println("ChatBot: " + chatResponse);
        }
    }

    private String getUserInput() {
        return scanner.nextLine();
    }

    private String getChatResponse(String userMessage) {
        try {
            HttpClient client = HttpClient.newHttpClient();

            String prompt = "{\"prompt\": \"" + userMessage + "\", \"max_tokens\": 100}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(getConfigValue("apiEndpoint")))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + getConfigValue("apiKey"))
                    .POST(HttpRequest.BodyPublishers.ofString(prompt))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
                String chatResponse = jsonResponse.getAsJsonArray("choices").get(0).getAsJsonObject().get("text").getAsString();
                return chatResponse;
            } else {
                throw new RuntimeException("Failed to get a valid response from the API. Status code: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Error communicating with the API: " + e.getMessage());
        }
    }

    private static JsonObject loadConfigFromJson(String fileName) {
        try (FileReader reader = new FileReader(fileName)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (Exception e) {
            throw new RuntimeException("Error reading JSON file: " + e.getMessage());
        }
    }

    private static String getConfigValue(String key) {
        return config.getAsJsonPrimitive(key).getAsString();
    }
}
