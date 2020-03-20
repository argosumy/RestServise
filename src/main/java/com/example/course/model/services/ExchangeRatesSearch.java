package com.example.course.model.services;


import com.example.course.model.converters.ParsJson;
import com.example.course.model.converters.WordDoc;
import com.example.course.model.exchange.Exchange;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;


@Service
public class ExchangeRatesSearch implements ExchangeRatesSearchIn  {
    public static String searcExcange(String date){
        String url ="https://api.privatbank.ua/p24api/exchange_rates?json&date=";
        String result = null;
        try {
            result = actionDay(date,url);
        } catch (JSONException | IOException e) {
            e.printStackTrace();
        }
        return date + result;
    }

    public static String actionDay(String date, String url) throws JSONException, IOException {
        RestTemplate restTemplate = new RestTemplate();
        String resultJson = restTemplate.getForObject(url + date, String.class);
     /*   String resultXML = restTemplate.getForObject("https://api.privatbank.ua/p24api/exchange_rates?xml&date="
                + date,String.class);*/
        JSONObject root = new JSONObject(resultJson);
        ParsJson parsJson = new ParsJson(resultJson);
        Exchange result  = parsJson.parsJson();
        new WordDoc(result,date);
        return result.toString();
    }
}
