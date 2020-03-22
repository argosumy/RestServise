package com.example.course.controller;

import com.example.course.model.services.ExchangeRatesSearch;
import org.json.JSONException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;


@RestController
public class CourseController {
    @GetMapping(value = "/date={param}")
    public String exchangeRateDay(@PathVariable List<String> param) throws JSONException, IOException {
            return ExchangeRatesSearch.searcExcange(param);
    }

    @GetMapping(value = "/date={date}&currency={curr}")
    public String exchangeRateDay(@PathVariable String date, @PathVariable String curr) throws JSONException, IOException {
        return ExchangeRatesSearch.searcExcange(date, curr);
    }



}
