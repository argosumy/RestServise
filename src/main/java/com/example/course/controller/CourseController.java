package com.example.course.controller;

import com.example.course.model.services.ExchangeRatesSearch;
import com.example.course.model.services.ExchangeRatesSearchIn;
import org.json.JSONException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;


@RestController
public class CourseController {
    private final ExchangeRatesSearchIn service;

    public CourseController(ExchangeRatesSearch service) {
        this.service = service;
    }
/*Вид запроса
*http://localhost:8888//date=22.03.2020,USD
* */
    @GetMapping(value = "/date={param}")
    public String exchangeRateDay(@PathVariable List<String> param) throws JSONException, IOException {
            return service.searcExcange(param);
    }

    @GetMapping(value = "/month={param}")
    public String exchangeRateMonth(@PathVariable List<String> param) throws JSONException, IOException {
        return service.searcExcange(param);
    }



}
