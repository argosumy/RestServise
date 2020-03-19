package com.example.course.controller;

import com.example.course.model.services.ExchangeRatesSearch;
import org.json.JSONException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class CourseController {
    @GetMapping(value = "/date={date}")
    public String exchangeRateDay(@PathVariable String date) throws JSONException {
            return ExchangeRatesSearch.searcExcange(date);
    }

    @GetMapping(value = "/month={month}")
    public String exchangeRateMonth(@PathVariable String month) throws JSONException {
        return ExchangeRatesSearch.searcExcange(month);
    }

}
