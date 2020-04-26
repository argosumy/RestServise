package com.example.course.model.services;


import com.example.course.model.converters.ParsJson;
import com.example.course.model.converters.TypeBank;
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
//private SimpleDateFormat format = new SimpleDateFormat();

    public ExchangeRatesSearch() {
    }
    public String creatURL(TypeBank.typeBank bank, String date){
        //date 21.10.2019
        String url;
        if(bank == TypeBank.typeBank.PB){
            url = "https://api.privatbank.ua/p24api/exchange_rates?json&date=" + date;
            return url;
        }
        if(bank == TypeBank.typeBank.NBU){
            String [] words = date.split("\\.");
            date = words[2]+words[1]+words[0];
            url = "https://bank.gov.ua/NBUStatService/v1/statdirectory/exchange?date=" + date;//20191021&xml
            return url;
        }
        return null;
    }
    @Override
    public String searcExcange(List<String> param) throws JSONException, IOException {
      //  String url = "https://api.privatbank.ua/p24api/exchange_rates?json&date=";
        String curr = null;
        List<Exchange> result = null;
        String date = param.get(0);
        SimpleDateFormat format = new SimpleDateFormat();
        Date docDate;
        Boolean flag = true;
        /*
        Проверка даты на соответствие настоящему или прошедшему времени
         */
        if(date.length() > 7){
            format.applyPattern("dd.MM.yyyy");
        }
        else {
            format.applyPattern("MM.yyyy");
            flag = false;
        }
        try {
            docDate= format.parse(date);
            if (docDate.after(new Date())){
                System.out.println(docDate);
                return "Дата не может быть будущим по отношению к  " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy "));
            }
        } catch (ParseException e) {
            e.printStackTrace();
            return "EROR1";
        }
        String url = creatURL(TypeBank.typeBank.PB,date);
        List<Exchange> resultExchange = actionDayMonth(date,flag);

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
     * @return результат выборки в виде коллекции объектов Exchange
     * @throws JSONException
     */
    public List<Exchange> actionDayMonth(String date, Boolean flag) throws JSONException {
        String dateCh = null;
        Exchange result;
        if(flag){
            //dateCh = date;
            System.out.println("Name " + Thread.currentThread().getName());
            String url = creatURL(TypeBank.typeBank.PB,date);
            result = exchange(url);

            listExchange.add(result);
        }
        //выборка за месяц в нескольких потоках
        else {
           //List<CompletableFuture> futureList = new ArrayList<>(31);
            ExecutorService executor = Executors.newFixedThreadPool(5);
            CompletionService<Exchange> completionService = new ExecutorCompletionService<>(executor);
            List<Future<Exchange>> listFuture = new ArrayList<>();
            Future<Exchange> future;
            for (int i = 1; i < 32; i++) {
                if (i < 10) {
                    dateCh = "0" + i + "." + date;
                } else {
                    dateCh = i + "." + date;
                }
//                String finalDateCh = dateCh;
                String url = creatURL(TypeBank.typeBank.PB,dateCh);
                try {
                    future = completionService.submit(() -> exchange(url));
                    listFuture.add(future);
                    //listExchange.add(future.get());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            System.out.println("ADD START");
            for (int i = 0; i < listFuture.size();i++ ){
                try {
                    listExchange.add(completionService.take().get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executor.shutdown();
        }
        return listExchange;
    }

    public Exchange exchange(String url)  {
        System.out.println("Exchange - " + Thread.currentThread().getName());
        String resultJson = restTemplate.getForObject(url, String.class);
        Exchange result = null;
        try {
            ParsJson parsJson = new ParsJson(resultJson);
            result = parsJson.parsJson();
        }
        catch (JSONException ex){

        }
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
