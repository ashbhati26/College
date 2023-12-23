import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

public class Currency {
    private static final String API_KEY = "d88d3b2ab0ca1b52c8b4341c0cfa0918";
    private static final String API_URL = "https://open.er-api.com/v6/latest/";

    public static void main(String[] args) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

            System.out.print("Enter the source currency code: ");
            String fromCurrency = reader.readLine().toUpperCase();

            System.out.print("Enter the target currency code: ");
            String toCurrency = reader.readLine().toUpperCase();

            System.out.print("Enter the amount to convert: ");
            double amount = Double.parseDouble(reader.readLine());

            double exchangeRate = getExchangeRate(fromCurrency, toCurrency);
            double convertedAmount = amount * exchangeRate;

            System.out.println(amount + " " + fromCurrency + " is equal to " +
                    convertedAmount + " " + toCurrency + " (Exchange rate: " + exchangeRate + ")");
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    private static double getExchangeRate(String fromCurrency, String toCurrency) throws IOException {
        String urlStr = API_URL + URLEncoder.encode(fromCurrency, "UTF-8");
        URL url = new URL(urlStr);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            // Parse the JSON response to get the exchange rate
            String jsonResponse = response.toString();
            int startIndex = jsonResponse.indexOf("\"" + toCurrency + "\":") + toCurrency.length() + 4;
            int endIndex = jsonResponse.indexOf(",", startIndex);

            return Double.parseDouble(jsonResponse.substring(startIndex, endIndex));
        } finally {
            connection.disconnect();
        }
    }
}
