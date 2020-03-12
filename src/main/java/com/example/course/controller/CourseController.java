package com.example.course.controller;

import com.example.course.model.services.ExchangeRatesSearch;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import org.json.JSONException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.text.ParseException;

@RestController
public class CourseController {
    @GetMapping(value = "/{date}")
    public void exchangeRateDay(@PathVariable String date) throws JSONException {
            ExchangeRatesSearch.action(date);
    }

}
