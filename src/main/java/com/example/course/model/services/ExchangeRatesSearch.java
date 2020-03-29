package com.example.course.model.services;


import com.example.course.model.converters.ParsJson;
import com.example.course.model.converters.WordDoc;
import com.example.course.model.exchange.Exchange;
import org.json.JSONException;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;


@Service
public class ExchangeRatesSearch implements ExchangeRatesSearchIn  {


    public static String searcExcange(List<String> param) throws IOException, JSONException {
        String url ="https://api.privatbank.ua/p24api/exchange_rates?json&date=";
        String curr = null;
        List<Exchange> result = null;
        String date = param.get(0);
        SimpleDateFormat format = new SimpleDateFormat();
        /*
        Проверка даты на соответствие настоящему или прошедшему времени
         */
        if(date.length() > 7){
            format.applyPattern("dd.MM.yyyy");
        }
        else {
            format.applyPattern("MM.yyyy");
        }
        try {
            Date docDate= format.parse(date);
            if (docDate.after(new Date())){
                System.out.println(docDate);
                return "Дата не может быть больше текущей " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy "));
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "EROR1";
        }

        List<Exchange> resultExchange = actionDayMonth(date,url);
        /*
        Проверка в запросе наличия условия по валюте
        и вывод соответствующего результата.
         */
        if (param.size()>1)  {
            curr = param.get(1);
            result  = actionDayCurr(resultExchange,curr);
            new WordDoc(result,date);
            resultExchange = result;
        }
        else {
            new WordDoc(resultExchange,date);
        }
        return resultExchange.toString();
    }

    /**
     * Метод выбирает курсы всех валют с ресурса URL за определенный период date
     * @param date Временной параметр выборки формат "MM.yyyy" или "dd.MM.yyyy"
     * @param url Ресурс курсов валют
     * @return результат выборки в виде коллекции объектов Exchange
     * @throws JSONException
     */
    public static List<Exchange> actionDayMonth(String date, String url) throws JSONException {
        String dateCh = null;
        Exchange result;
        List<Exchange> arrayExchange = new ArrayList<>();
        Boolean flag = false;
        if(date.length() > 7){
            dateCh = date;
            flag = true;
        }
        else dateCh = "01." + date;

        int i = 1;
        do {
            RestTemplate restTemplate = new RestTemplate();
            String resultJson = restTemplate.getForObject(url + dateCh, String.class);
            ParsJson parsJson = new ParsJson(resultJson);
            result  = parsJson.parsJson();
            if((flag == false) && (result.getDate().contains(date))){
                arrayExchange.add(result);
                if ( i < 9){
                    i++;
                    dateCh = "0" + i +"." + date;
                }
                else {
                    i++;
                    dateCh = i + "." + date;
                }
            }
            else {
                if(flag == true){
                    arrayExchange.add(result);
                }
                flag = true;
            }
        }
        while (!flag);
        return arrayExchange;
    }
/*
    public static List<Exchange> arrayExchange(String url, String dateCh, List<Exchange> array) throws JSONException {
        RestTemplate restTemplate = new RestTemplate();
        String resultJson = restTemplate.getForObject(url + dateCh, String.class);
        ParsJson parsJson = new ParsJson(resultJson);
        Exchange result  = parsJson.parsJson();
        array.add(result);
        return array;
    }*/
    /**
     * Метод выбирает из коллекции валют курс валюты curr
     * @param arrayExchange коллекция курсов всех валют
     * @param curr название валюты example: "USD"
     * @return
     * @throws JSONException
     * @throws IOException
     */
    public static List<Exchange> actionDayCurr(List<Exchange> arrayExchange, String curr) throws JSONException, IOException {
        List<Exchange> arrayExchangeCurr = new ArrayList<>();
        for (Exchange result:arrayExchange) {
            for (Exchange.ExchangeRate exchange : result.getExchangeRate()) {
                if (exchange.getCurrency().equals(curr)) {
                    List<Exchange.ExchangeRate> listExchange = new ArrayList<>();
                    listExchange.add(exchange);
                    Exchange resultCopy = new Exchange();
                    resultCopy.setBank(result.getBank());
                    resultCopy.setDate(result.getDate());
                    resultCopy.setBaseCurrencyLit(result.getBaseCurrencyLit());
                    resultCopy.setExchangeRate(listExchange);
                    arrayExchangeCurr.add(resultCopy);
                }
            }
        }
        return arrayExchangeCurr;
    }

    //public Exchange

}
