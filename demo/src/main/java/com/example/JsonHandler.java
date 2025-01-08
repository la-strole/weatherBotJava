package com.example;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.example.exceptions.AppErrorCheckedException;


/**
 * The JsonHandler class provides utility methods for building URIs, retrieving JSON strings from
 * web APIs, and parsing JSON data into JSONArrays.
 * 
 * <p>
 * It includes the following methods:
 * <ul>
 * <li>{@code buildURI(String baseUrl, Map<String, String> params)}: Builds a URI from the given
 * base URL and parameters.</li>
 * <li>{@code getJsonStringFromWeb(String url, Map<String, String> urlParams)}: Retrieves a JSON
 * string from a web API using the provided URL and URL parameters.</li>
 * <li>{@code getJsonFromWeb(String url, Optional<Map<String, String>> params)}: Fetches JSON data
 * from a specified URL and returns it as a JSONArray.</li>
 * </ul>
 * </p>
 */
class JsonHandler {

    private static final Logger logger = Logger.getLogger(JsonHandler.class.getName());

    /**
     * Builds a URI from the given base URL and parameters.
     *
     * @param baseUrl the base URL to which the parameters will be appended
     * @param params a map of query parameters to be included in the URI
     * @return a URI constructed from the base URL and the provided parameters
     * @throws AppErrorCheckedException if the given base URL is invalid
     */
    private static URI buildURI(String baseUrl, Map<String, String> params)
            throws AppErrorCheckedException {
        try {
            StringBuilder uriBuilder = new StringBuilder(baseUrl);
            if (!params.isEmpty()) {
                uriBuilder.append("?");
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    String encodedKey = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
                    String encodedValue =
                            URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8);
                    uriBuilder.append(encodedKey).append("=").append(encodedValue).append("&");
                }
                // Remove the last '&'
                uriBuilder.setLength(uriBuilder.length() - 1);
            }
            return URI.create(uriBuilder.toString());
        } catch (NullPointerException | IllegalStateException | IndexOutOfBoundsException
                | IllegalArgumentException e) {
            logger.severe("buildURI:" + e);
            throw new AppErrorCheckedException("JsonHandler:buidURI: Runtime Error");
        }
    }

    /**
     * Retrieves a JSON string from a web API using the provided URL and URL parameters.
     *
     * @param url The URL of the web API.
     * @param urlParams A map of URL parameters to be included in the request.
     * @return The JSON response as a string.
     * @throws AppErrorCheckedException If there is an error during the request or the response
     *         status is not 200 or if request interrupted.
     */
    private static String getJsonStringFromWeb(String url, Map<String, String> urlParams)
            throws AppErrorCheckedException {
        try (HttpClient client =
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();) {
            HttpRequest request = HttpRequest.newBuilder().uri(buildURI(url, urlParams))
                    .header("Accept", "application/json").build();
            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                logger.severe(String.format(
                        "getJsonStringFromWeb: Failed to get response from the API. Status code: %d Request: %s",
                        response.statusCode(), request.uri().toString()));
                throw new AppErrorCheckedException(
                        "JsonHandler:getJsnStringFromWeb: Runtime Error");
            } else {
                return response.body();
            }
        } catch (IllegalArgumentException | IOException | SecurityException e) {
            logger.severe("getJsnStringFromWeb:" + e);
            throw new AppErrorCheckedException("JsonHandler:getJsnStringFromWeb: Runtime Error");
        } catch (InterruptedException e2) {
            Thread.currentThread().interrupt();
            logger.severe("getJsonStringFromWeb: Request interrupted: " + e2);
            throw new AppErrorCheckedException(
                    "JsonHandler:getJsnStringFromWeb: Request interrupted");
        }
    }

    /**
     * Fetches JSON data from a given URL and returns it as a JSONArray.
     * If the fetched JSON data is a JSONObject, it will be wrapped in a JSONArray.
     *
     * @param url The URL to fetch the JSON data from.
     * @param params Optional URL parameters to include in the request.
     * @return A JSONArray containing the fetched JSON data.
     * @throws AppErrorCheckedException If there is an error during the JSON fetching or parsing process.
     */
    public static JSONArray getJsonFromWeb(String url, Optional<Map<String, String>> params)
            throws AppErrorCheckedException {
        JSONArray result = new JSONArray();
        Map<String, String> urlParams = params.orElseGet(HashMap::new);
        String jsonString = getJsonStringFromWeb(url, urlParams);
        // Check if jsonString is JSONArray.
        try {
            result = new JSONArray(jsonString);
        } catch (JSONException e) {
            // Check if jsonString is JSONObject and packing it to JSONArray.
            try {
                JSONObject object = new JSONObject(jsonString);
                result.put(object);
            } catch (JSONException e2) {
                logger.severe("getJsonFromWeb:" + e);
                throw new AppErrorCheckedException("JsonHandler:getJsonFromWeb: Rntime Error");
            }
        }
        return result;
    }
}


