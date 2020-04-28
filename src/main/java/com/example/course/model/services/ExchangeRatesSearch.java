package com.example.course.model.services;


import com.example.course.model.converters.ParsJson;
import com.example.course.model.converters.ParseXmlDom;
import com.example.course.model.converters.TypeBank;
import com.example.course.model.converters.WordDoc;
import com.example.course.model.exchange.Exchange;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
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
    private static final Logger LOGGER = Logger.getLogger(ExchangeRatesSearch.class);
    private List<Exchange> listExchangePB = new ArrayList<>();
    private List<Exchange> listExchangeNBU = new ArrayList<>();
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
        String curr = null;
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
        //?????????????????
      //  String url = creatURL(TypeBank.typeBank.PB,date);
      //  actionDayMonth(date,flag, TypeBank.typeBank.PB);
       // actionDayMonth(date,flag, TypeBank.typeBank.NBU);
        CompletableFuture futurePb;
        CompletableFuture futureNbu;
        Boolean flagB = flag;
        futureNbu = CompletableFuture.supplyAsync(()-> actionDayMonth(date,flagB,TypeBank.typeBank.NBU));
        futurePb = CompletableFuture.supplyAsync(() -> actionDayMonth(date,flagB, TypeBank.typeBank.PB));
        try{
        listExchangePB = (List<Exchange>) futurePb.get();
        listExchangeNBU = (List<Exchange>) futureNbu.get();
        }
        catch (InterruptedException | ExecutionException e){
            LOGGER.error("Ошибка при получении результатов асинхронного запроса", e);
        }

        /*
        Проверка в запросе наличия условия по валюте
        и вывод соответствующего результата.
         */

        if (param.size()>1)  {
            curr = param.get(1);
            List<Exchange>resultPB  = actionDayCurr(listExchangePB,curr);
            List<Exchange>resultNBU = actionDayCurr(listExchangeNBU,curr);
        //    String paramPB = date + "_" + TypeBank.typeBank.PB;
            new WordDoc(resultPB,date+"PB");
            new WordDoc(resultNBU,date+"NBU");
            listExchangePB = resultPB;
            listExchangeNBU = resultNBU;
        }
        else {
            new WordDoc(listExchangePB,date + "PB");
            new WordDoc(listExchangeNBU,date + "NBU");
        }
        return listExchangeNBU.toString();
    }

    /**
     * Метод выбирает курсы всех валют с ресурса URL за определенный период date
     * @param date Временной параметр выборки формат "MM.yyyy" или "dd.MM.yyyy"
     * @return результат выборки в виде коллекции объектов Exchange
     * @throws JSONException
     */
    public List<Exchange> actionDayMonth(String date, Boolean flag, TypeBank.typeBank bank){
        List<Exchange> exchangeList = new ArrayList<>();
        String dateCh = null;
        Exchange result;
        if(flag){
            System.out.println("Name " + Thread.currentThread().getName());
            String url = creatURL(bank,date);
            result = exchange(url, bank);
            exchangeList.add(result);
        }
        //выборка за месяц в нескольких потоках
        else {
            ExecutorService executor = Executors.newFixedThreadPool(5);
            CompletionService<Exchange> completionService = new ExecutorCompletionService<>(executor);
            List<Future<Exchange>> listFuture = new ArrayList<>();
            Future<Exchange> future;
            String dateParse = "01." + date;
            DateTimeFormatter format = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            LocalDate localDate = LocalDate.parse(dateParse,format);
            for (LocalDate i = localDate; i.isBefore(localDate.plusMonths(1)); i=i.plusDays(1)) {
                dateCh = i.format(format);
                System.out.println(bank +" "+ dateCh);
                String url = creatURL(bank,dateCh);
                try {
                    future = completionService.submit(() -> exchange(url,bank));
                    listFuture.add(future);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            for (int i = 0; i < listFuture.size();i++ ){
                try {
                    exchangeList.add(completionService.take().get());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
            }
            executor.shutdown();
        }
        return exchangeList;
    }

    public Exchange exchange(String url, TypeBank.typeBank bank)  {
        System.out.println("Exchange - " + Thread.currentThread().getName());
        String resultJson = restTemplate.getForObject(url, String.class);
        Exchange result = null;
        if(bank.equals(TypeBank.typeBank.PB)){
            try {
                ParsJson parsJson = new ParsJson(resultJson);
                result = parsJson.parsJson();
            }
            catch (JSONException ex){
                }
        }
        else {
            ParseXmlDom parseXmlDom = new ParseXmlDom(resultJson);
            try {
                result = parseXmlDom.parserXmlDom();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (SAXException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
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
                    break;
                }
            }
        }
        return arrayExchangeCurr;
    }

}
