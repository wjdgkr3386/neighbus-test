package com.neighbus.weather;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mobile/weather")
public class WeatherMobileRestController {

    private final WeatherService weatherService;

    @Autowired
    public WeatherMobileRestController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/current") // Path changed from /weather/current
    public WeatherDTO getCurrentWeather(@RequestParam("lat") double lat, 
                                        @RequestParam("lon") double lon) {
        return weatherService.getWeatherByLatLon(lat, lon);
    }
}
