package com.example.course.model.services;


import com.example.course.model.converters.ParsJson;
import com.example.course.model.converters.WordDoc;
import com.example.course.model.exchange.Exchange;
import org.json.JSONException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;


@Service
public class ExchangeRatesSearch implements ExchangeRatesSearchIn  {
    public static String searcExcange(List<String> param) throws IOException, JSONException {
        String url ="https://api.privatbank.ua/p24api/exchange_rates?json&date=";
        String result = null;
        String curr = null;
        String date = param.get(0);
        SimpleDateFormat format = new SimpleDateFormat();
        format.applyPattern("dd.MM.yyyy");
        try {
            Date docDate= format.parse(date);
            if (docDate.after(new Date())){
                return "Дата не может быть больше текущей " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy "));
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "EROR1";
        }
        RestTemplate restTemplate = new RestTemplate();
        String resultJson = restTemplate.getForObject(url + date, String.class);
        if (param.size()>1)  {
            curr = param.get(1);
            return actionDayCurr(resultJson,curr);
        }

        return date + resultJson;
    }

    public static String searcExcange(String date, String curr) throws IOException, JSONException {
        String url ="https://api.privatbank.ua/p24api/exchange_rates?json&date=";
        String result = null;
        SimpleDateFormat format = new SimpleDateFormat();
        format.applyPattern("dd.MM.yyyy");
        try {
            Date docDate= format.parse(date);
            if (docDate.after(new Date())){
                return "Дата не может быть больше текущей " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy "));
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "EROR1";
        }
        RestTemplate restTemplate = new RestTemplate();
        String resultJson = restTemplate.getForObject(url + date, String.class);
        result = actionDayCurr(resultJson,curr).toString();
        return date + result;
    }

    public static String actionDayCurr(String resultJson, String curr) throws JSONException, IOException {
        ParsJson parsJson = new ParsJson(resultJson);
        Exchange result  = parsJson.parsJson();
        for (Exchange.ExchangeRate exchange:result.getExchangeRate()) {
            if(exchange.getCurrency().equals(curr)){
                return exchange.toString();
            }
        }
     /*   String resultXML = restTemplate.getForObject("https://api.privatbank.ua/p24api/exchange_rates?xml&date="
                + date,String.class);*/
       // ParsJson parsJson = new ParsJson(resultJson);
       // Exchange result  = parsJson.parsJson();
       // new WordDoc(result,date);
        return "НЕТ КУРСА НА ДАННУЮ ВАЛЮТУ";
    }

}
