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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


@Service
public class ExchangeRatesSearch implements ExchangeRatesSearchIn  {
    public static String searcExcange(List<String> param) throws IOException, JSONException {
        String url ="https://api.privatbank.ua/p24api/exchange_rates?json&date=";
        String curr = null;
        Exchange result = null;
        String date = param.get(0);

        SimpleDateFormat format = new SimpleDateFormat();
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

        /*
        RestTemplate restTemplate = new RestTemplate();
        String resultJson = restTemplate.getForObject(url + date, String.class);
        */


        String resultJson = actionDayMonthCurr(date,url).toString();
        /*if (param.size()>1)  {
            curr = param.get(1);
            result  = actionDayCurr(resultJson,curr);
            new WordDoc(result,date);
            resultJson = result.toString();
        }
        else {
            ParsJson parsJson = new ParsJson(resultJson);
            result  = parsJson.parsJson();
            new WordDoc(result,date);
        }*/
        return resultJson;
    }

    public static List<Exchange> actionDayMonthCurr(String date, String url) throws JSONException {
        String dateCh = date;
        String str;
        Exchange result;
        List<Exchange> arrayExchange = new ArrayList<>();
        Boolean flag = false;
            if(date.length() > 7){
                flag = true;
            }
            else{
                dateCh = "01." + date;
            }
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

    public static Exchange actionDayCurr(String resultJson, String curr) throws JSONException, IOException {
        ParsJson parsJson = new ParsJson(resultJson);
        Exchange result  = parsJson.parsJson();
        for (Exchange.ExchangeRate exchange:result.getExchangeRate()) {
            if(exchange.getCurrency().equals(curr)){
                List<Exchange.ExchangeRate> listExchange = new ArrayList<>();
                listExchange.add(exchange);
                Exchange resultCopy = new Exchange();
                resultCopy.setBank(result.getBank());
                resultCopy.setDate(result.getDate());
                resultCopy.setBaseCurrencyLit(result.getBaseCurrencyLit());
                resultCopy.setExchangeRate(listExchange);
                return resultCopy;
            }
        }


     /*   String resultXML = restTemplate.getForObject("https://api.privatbank.ua/p24api/exchange_rates?xml&date="
                + date,String.class);*/
       // ParsJson parsJson = new ParsJson(resultJson);
       // Exchange result  = parsJson.parsJson();
       // new WordDoc(result,date);
        return null;
    }

}
