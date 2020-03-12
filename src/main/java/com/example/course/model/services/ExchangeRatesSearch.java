package com.example.course.model.services;


import com.example.course.model.exchange.Exchange;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class ExchangeRatesSearch implements ExchangeRatesSearchIn  {
    public static String action(String date) throws JSONException {
        RestTemplate restTemplate = new RestTemplate();
        String resultJson = restTemplate.getForObject("https://api.privatbank.ua/p24api/exchange_rates?json&date="
                + date,String.class);
        String resultXML = restTemplate.getForObject("https://api.privatbank.ua/p24api/exchange_rates?xml&date="
                + date,String.class);
        System.out.println(resultJson);
        System.out.println(resultXML);
        JSONObject obj = new JSONObject(resultJson);

        Exchange exchange = new Exchange();



        System.out.println(exchange.toString());



        return resultJson;
    }
}
