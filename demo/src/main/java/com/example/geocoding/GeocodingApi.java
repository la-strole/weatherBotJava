package com.example.geocoding;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.example.exceptions.AppErrorCheckedException;

/**
 * Retrieves city coordinates for the given city name.
 * 
 * The returned JSONArray must contain JSONObjects with the following keys:
 * - "cityName": String
 * - "country": String
 * - "state": String, Optional
 * - "lon": Double
 * - "lat": Double
 * 
 * @param cityName the name of the city
 * @return a JSONArray of JSONObjects containing city coordinates
 */
public abstract class GeocodingApi {

    Logger localLogger = Logger.getLogger(GeocodingApi.class.getName());

    public enum fields {
        CITY_NAME("cityName"),
        COUNTRY("country"),
        STATE("state"),
        LON("lon"),
        LAT("lat");

        private final String fieldName;

        fields(String fieldName) {
            this.fieldName = fieldName;
        }

        @Override
        public String toString() {
            return this.fieldName;
        }
    }

    public abstract JSONArray getCoordinates(String cityName) throws AppErrorCheckedException;

    boolean isJsonArrayValid(JSONArray jsonArray) {
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                jsonObject.getString(fields.CITY_NAME.toString());
                jsonObject.getString(fields.COUNTRY.toString());
                jsonObject.getDouble(fields.LON.toString());
                jsonObject.getDouble(fields.LAT.toString());
            } catch (JSONException e) {
                localLogger.log(Level.SEVERE, e.getMessage());
                return false;
            }
        }
        return true;
    }
}
