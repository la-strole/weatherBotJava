package com.example.weather_api;

import com.example.exceptions.AppErrorCheckedException;

abstract class GetCurrentWeather {

    public abstract ForecastItem getCurrentWeather(Double lon, Double lat) throws AppErrorCheckedException;

}
