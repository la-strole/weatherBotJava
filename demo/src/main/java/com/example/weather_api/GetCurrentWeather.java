package com.example.weather_api;

import com.example.exceptions.AppErrorCheckedException;

/**
 * Abstract class representing a service to get the current weather.
 */
abstract class GetCurrentWeather {

    /**
     * Retrieves the current weather for the specified longitude and latitude.
     *
     * @param lon the longitude of the location
     * @param lat the latitude of the location
     * @return a {@link ForecastItem} containing the current weather information
     * @throws AppErrorCheckedException if an error occurs while fetching the weather data
     */
    public abstract ForecastItem getCurrentWeather(Double lon, Double lat) throws AppErrorCheckedException;

}
