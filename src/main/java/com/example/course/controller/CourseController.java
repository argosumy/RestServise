package com.example.course.controller;

import com.example.course.model.services.ExchangeRatesSearch;
import com.example.course.model.services.ExchangeRatesSearchIn;
import org.json.JSONException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

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
    @GetMapping(value = "/date={param}")
    public String exchangeRateDay(@PathVariable List<String> param) throws JSONException, IOException {
        String answer = service.validDate(param);
        if(answer.length() > 10){
            return answer;
        }
        return service.searcExcange(param).toString();
    }
    //Лучший курс по банкам на указаную дату
    @GetMapping(value = "best/date={param}")
    public String exchangeRateDayBest(@PathVariable List<String> param) {
        String answer = service.validDate(param);
        if(answer.length() > 10){
            return answer;
        }
        else {
            if((answer.length() < 10)||(param.size() < 2 )){
                return "Не верный формат запроса. Допустимый формат dd.MM.yyyy,USD";
            }
        }
        return service.bestCurseDay(param);
    }
    @GetMapping(value = "best/week={param}")
    public String exchangeRateWeek(@PathVariable String param){
        List<String> paramList = new ArrayList<>();
        paramList.add(null);
        paramList.add(param);
        return service.bestCurseWeek(paramList);
    }







}
