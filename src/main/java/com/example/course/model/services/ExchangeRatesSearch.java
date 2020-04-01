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
import java.util.concurrent.*;


@Service
public class ExchangeRatesSearch implements ExchangeRatesSearchIn  {
private List<Exchange> listExchange = new ArrayList<>();
private RestTemplate restTemplate = new RestTemplate();

    public ExchangeRatesSearch() {
    }

    @Override
    public String searcExcange(List<String> param) throws JSONException, IOException {
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
    public List<Exchange> actionDayMonth(String date, String url) throws JSONException {
        String dateCh = null;
        Exchange result;
        if(date.length() > 7){
            dateCh = date;
            System.out.println("Name " + Thread.currentThread().getName());
            result = exchange(url, dateCh);
            listExchange.add(result);
        }
        //выборка за месяц в нескольких потоках
        else {
           //List<CompletableFuture> futureList = new ArrayList<>(31);
            ExecutorService executor = Executors.newFixedThreadPool(5);
            CompletionService<Exchange> completionService = new ExecutorCompletionService<>(executor);
            Future<Exchange> future;
            for (int i = 1; i < 32; i++) {
                System.out.println(i);
                if (i < 10) {
                    dateCh = "0" + i + "." + date;
                } else {
                    dateCh = i + "." + date;
                }
                String finalDateCh = dateCh;
                try {
                    future = completionService.submit(() -> exchange(url,finalDateCh));
                    listExchange.add(future.get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
        return listExchange;
    }
  //  @Async("processExecutor")
    public Exchange exchange(String url, String dateCh)  {
        System.out.println(Thread.currentThread().getName());
        String resultJson = restTemplate.getForObject(url + dateCh, String.class);
        Exchange result = null;
        try {
            ParsJson parsJson = new ParsJson(resultJson);
            result = parsJson.parsJson();
        }
        catch (JSONException ex){

        }
        //listExchange.add(result);
        return result;
    }
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

}
