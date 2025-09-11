package com.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.exceptions.AppErrorCheckedException;
import com.example.web_json_handlers.JsonHandler;

import io.github.cdimascio.dotenv.Dotenv;

/**
 * The GeminiApi class provides a method to fetch and rewrite weather
 * information
 * in a randomly selected literary style using the Gemini API.
 * <p>
 * This class contains a single public static method
 * {@link #getGeminiData(String)}
 * which takes a weather information string as input and returns the rewritten
 * weather information in a selected literary style.
 * </p>
 * <p>
 * The class uses environment variables to retrieve the API key required for
 * making requests to the Gemini API. If the API key is not found, an exception
 * is thrown.
 * </p>
 * <p>
 * The class also includes a private constructor to prevent instantiation.
 * </p>
 */
public class GeminiApi {

    static Logger logger = Logger.getLogger(GeminiApi.class.getName());
    static Random random = new Random();

    /**
     * Fetches and rewrites the given weather string in a randomly selected literary
     * style using the Gemini API.
     *
     * @param weatherString The weather information to be rewritten.
     * @param language      The language of the weather information.
     * @return The rewritten weather information in the selected literary style.
     * @throws AppErrorCheckedException If there is an error during the API call or
     *                                  processing the response.
     */
    public static String getGeminiData(final String weatherString, final String language)
            throws AppErrorCheckedException {
        final String apiKey = Dotenv.load().get("GEMINI_API_KEY");
        if (apiKey != null) {
            try {
                final Map<String, String> langMap = Map.of("en", "in american english language",
                        "ru", "in russian language",
                        "am", "in armenian language");
                final List<String> styles = new ArrayList<>();
                styles.add("Classic Realism");
                styles.add("Romanticism");
                styles.add("Symbolism");
                styles.add("Acmeism");
                styles.add("Futurism");
                styles.add("Socialist Realism");
                styles.add("Postmodernism");
                styles.add("Gothic Fiction");
                styles.add("Stream of Consciousness");
                styles.add("Minimalism");
                styles.add("Lyric Poetry");
                styles.add("Satire");
                styles.add("Philosophical Prose");
                styles.add("Historical Fiction");
                styles.add("Children's Literature");
                styles.add("Epistolary Style");
                styles.add("Magical Realism");
                styles.add("Dystopian Fiction");
                styles.add("Travel Writing");
                styles.add("Psychological Realism");
                final String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
                final Map<String, String> params = Map.of("key", apiKey);
                final JSONObject data = new JSONObject();
                final JSONObject parts = new JSONObject();
                final JSONArray partsContext = new JSONArray();
                final JSONObject text = new JSONObject();
                text.put("text", String.format("""
                        Please help me convert the following 3-hour weather forecast into a natural language day summary. 
                        You can use such text as examples, but you don't have to do it that way. 
                        You can sometimes make a table with the most convenient representation of the weather for today. 
                        Example: Today, in Paris, the potential for thunderstorms remains.
                        The chance of precipitation is high. However, the rainfall will be light. 
                        The temperature is predicted to vary between a pleasant 22°C and a refreshing 13°C.
                        The forecast high is lower and more aligned with what is typical in October than in September.
                        Sunrise was at 06:37 and sunset will be at 19:19; the daylight will last for 12h and 41min. 
                        OR something like this link in table: https://www.accuweather.com/en/fr/paris/623/weather-forecast/623?city=paris                                                                                                                               
                        Try to compose a typical one-paragraph weather forecast text from weather forecast data. 
                        You can use emoji and pictures. Use %s language. Here is the forecast: %s
                        """,
                        langMap.getOrDefault(language, "english"),
                        weatherString));
                partsContext.put(text);
                parts.put("parts", partsContext);
                final JSONArray contentsContext = new JSONArray();
                contentsContext.put(parts);
                data.put("contents", contentsContext);
                logger.log(Level.INFO, () -> String.format("request data: %s", data.toString()));
                final JSONArray result = JsonHandler.getJsonFromWeb(url, Optional.of(params), "POST", data.toString());
                // Parse the JSON response to get the generated text
                final JSONArray jsonArray = new JSONArray(result);
                final JSONObject firstObject = jsonArray.getJSONObject(0);
                final JSONArray candidates = firstObject.getJSONArray("candidates");
                final JSONObject firstCandidate = candidates.getJSONObject(0);
                final JSONObject content = firstCandidate.getJSONObject("content");
                final JSONArray respParts = content.getJSONArray("parts");
                final JSONObject firstPart = respParts.getJSONObject(0);
                return firstPart.getString("text");
            } catch (JSONException | UnsupportedOperationException | ClassCastException | NullPointerException
                    | IllegalArgumentException | IndexOutOfBoundsException e) {
                logger.log(Level.SEVERE, e::toString);
                throw new AppErrorCheckedException("Runtime Error.");
            }
        } else {
            logger.log(Level.SEVERE, "Gemini API key not found in environment variables.");
            throw new AppErrorCheckedException("Runtime Error.");
        }

    }

    private GeminiApi() {
        // Private constructor to hide the implicit public one
        throw new IllegalStateException("Utility class");
    }
}
