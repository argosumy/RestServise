package com.example.course.controller;

import com.example.course.model.services.ExchangeRatesSearch;
import com.example.course.model.services.ExchangeRatesSearchIn;
import org.json.JSONException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.util.ArrayList;
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
    @GetMapping(value = "/date={date}/cur={cur}")
    public ResponseEntity<Object> exchangeRateDay(@PathVariable("date") String date,@PathVariable(value = "cur") String cur) throws JSONException, IOException {
        String answer = service.validDate(date);
        if(answer.length() > 10){
            return ResponseEntity.ok(answer);
        }
        return ResponseEntity.ok(service.searcExcange(date,cur));
    }
    //Лучший курс по банкам на указаную дату
    @GetMapping(value = "best/date={date}/cur={cur}")
    public String exchangeRateDayBest(@PathVariable String date, @PathVariable String cur) {
        String answer = service.validDate(date);
        if(answer.length() > 10){
            return answer;
        }
        return service.bestCurseDay(date,cur);
    }
  /*  @GetMapping(value = "best/week={param}")
    public String exchangeRateWeek(@PathVariable String param){
        System.out.println(param.length());
        if(param.length()==3){
        List<String> paramList = new ArrayList<>();
        paramList.add(null);
        paramList.add(param);
        return service.bestCurseWeek(paramList);
        }
        else {
            return "Введите валюту в формате USD";

    }*/







}
