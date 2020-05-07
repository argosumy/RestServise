package com.example.course.model.services;


import com.example.course.model.converters.ParsJson;
import com.example.course.model.converters.ParseXmlDom;
import com.example.course.model.converters.TypeBank;
import com.example.course.model.converters.WordDoc;
import com.example.course.model.exchange.Exchange;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;


@Service
public class ExchangeRatesSearch implements ExchangeRatesSearchIn  {
    private static final Logger LOGGER = Logger.getLogger(ExchangeRatesSearch.class);
    private List<Exchange> listExchangePB = new ArrayList<>();
    private List<Exchange> listExchangeNBU = new ArrayList<>();
    private RestTemplate restTemplate = new RestTemplate();

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
    public String bestCurseWeek(String date,String cur) {
        Map <TypeBank.typeBank,List<Exchange>> mapBank = new HashMap<>();
        try {
            mapBank = searcExcange(date,cur);
        } catch (JSONException e) {
            LOGGER.error(e);
        } catch (IOException e) {
            LOGGER.error(e);
        }
        return "Лучший курс на прошедшей недели по банкам: " + bestCurs(mapBank);
    }

    @Override
    public String bestCurseDay(String date, String cur) {
            Map <TypeBank.typeBank,List<Exchange>> mapBank = new HashMap<>();
        try {
            mapBank = searcExcange(date,cur);
        } catch (JSONException e) {
            LOGGER.error(e);
        } catch (IOException e) {
            LOGGER.error(e);
        }

        return "Лучший курс на день по банкам:" +  bestCurs(mapBank);
    }

    @Override
    public Map<TypeBank.typeBank, List<Exchange>> searcExcange(String paramDate, String paramCur) throws JSONException, IOException {
        listExchangeNBU = null;
        listExchangePB = null;
        String curr ;
        String date = paramDate;
        CompletableFuture futurePb;
        CompletableFuture futureNbu;
        futureNbu = CompletableFuture.supplyAsync(()-> actionDayMonth(date,TypeBank.typeBank.NBU));
        futurePb = CompletableFuture.supplyAsync(() -> actionDayMonth(date,TypeBank.typeBank.PB));
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
        paramCur = paramCur.trim();
        if (paramCur.length()==3){
            curr = paramCur;
            List<Exchange>resultPB  = actionCurr(listExchangePB,curr);
            List<Exchange>resultNBU = actionCurr(listExchangeNBU,curr);
            new WordDoc(resultPB,date+"PB");
            new WordDoc(resultNBU,date+"NBU");
            listExchangePB = resultPB;
            listExchangeNBU = resultNBU;
        }
        else {
            new WordDoc(listExchangePB,date + "PB");
            new WordDoc(listExchangeNBU,date + "NBU");
        }
        Map<TypeBank.typeBank, List<Exchange>> bankListMap = new HashMap<>();
        bankListMap.put(TypeBank.typeBank.PB,listExchangePB);
        bankListMap.put(TypeBank.typeBank.NBU,listExchangeNBU);
        return bankListMap;
    }
    /**
     *Валидация даты
     */
    @Override
    public String validDate(String paramDate){
        String date = paramDate;
        SimpleDateFormat format = new SimpleDateFormat();
        Date docDate = null;
        if(date.length() > 7){
            format.applyPattern("dd.MM.yyyy");
        }
        else {
            format.applyPattern("MM.yyyy");
        }
        try {
            docDate= format.parse(date);
        } catch (ParseException e) {
            LOGGER.error(e);
            return "Неправильный формат даты. Допустимый формат dd.MM.yyyy или MM.yyyy";
        }
        if (docDate.after(new Date())) {
            System.out.println(docDate);
            return "Дата не может быть будущим по отношению к  " + LocalDate.now().format(DateTimeFormatter.ofPattern("dd MMMM yyyy "));
        }
        return date;
    }

    /**
     * Метод выбирает курсы всех валют с ресурса URL за определенный период date
     * @param date Временной параметр выборки формат "MM.yyyy" или "dd.MM.yyyy",если date is null выбирает за неделю
     * @return результат выборки в виде коллекции объектов Exchange
     * @throws JSONException
     */
    public List<Exchange> actionDayMonth(String date, TypeBank.typeBank bank){
        List<Exchange> exchangeList = new ArrayList<>();
        Exchange result;
        if(date!=null){
            //вборка за один день
            if(date.length() > 7){
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
                    date = i.format(format);
                    String url = creatURL(bank,date);
                    try {
                        future = completionService.submit(() -> exchange(url,bank));
                        listFuture.add(future);
                    } catch (Exception e) {
                        LOGGER.error(e);
                    }
                }
                for (int i = 0; i < listFuture.size();i++ ){
                    try {
                        exchangeList.add(completionService.take().get());
                    } catch (InterruptedException e) {
                        LOGGER.error(e);
                    } catch (ExecutionException e) {
                        LOGGER.error(e);
                    }
                }
                executor.shutdown();
            }
        }
        //выборка курсов за прошедшую неделю
        else{
            LocalDate dateNow = LocalDate.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            String url = null;
            for (LocalDate i = dateNow.minusWeeks(1);i.isBefore(dateNow);i=i.plusDays(1)){
                date = i.format(formatter);
                url = creatURL(bank,date);
                exchangeList.add(exchange(url,bank));
            }
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
                LOGGER.error(ex);
                }
        }
        else {
            ParseXmlDom parseXmlDom = new ParseXmlDom(resultJson);
            try {
                result = parseXmlDom.parserXmlDom();
            } catch (IOException e) {
                LOGGER.error(e);
            } catch (SAXException e) {
                LOGGER.error(e);
            } catch (ParserConfigurationException e) {
                LOGGER.error(e);
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
    public static List<Exchange> actionCurr(List<Exchange> arrayExchange, String curr) throws JSONException, IOException {
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
    public String bestCurs(Map <TypeBank.typeBank,List<Exchange>> mapBank){
        String bankSale = "PB";
        String bankBuy = "NBU";
        String dateSale = null;
        String dateBay = null;
        Float sale = Float.parseFloat(mapBank.get(TypeBank.typeBank.PB).get(0).getExchangeRate().get(0).getSaleRate());
        Float bay = Float.parseFloat(mapBank.get(TypeBank.typeBank.PB).get(0).getExchangeRate().get(0).getPurchaseRate());
        for (Map.Entry<TypeBank.typeBank,List<Exchange>> node:mapBank.entrySet()) {
            for (Exchange exchange: node.getValue()){
                System.out.println(exchange.toString());
             //   if (exchange.getExchangeRate().get(0).getCurrency().equals(param.get(1))){
                    for (int i = 0; i < exchange.getExchangeRate().size();i++){
                        if(sale > Float.parseFloat(exchange.getExchangeRate().get(i).getSaleRate())){
                            System.out.println("Ex"+exchange);
                            sale = Float.parseFloat(exchange.getExchangeRate().get(i).getSaleRate());
                            bankSale = exchange.getBank();
                            dateSale = exchange.getDate();
                        }
                        if(bay < Float.parseFloat(exchange.getExchangeRate().get(i).getPurchaseRate())){
                            bay = Float.parseFloat(exchange.getExchangeRate().get(i).getPurchaseRate());
                            bankSale = exchange.getBank();
                            dateBay = exchange.getDate();
                        }
                    }
               // }
            }
        }
        return "КУПИТЬ " + dateSale + " Bank Sale - " + bankSale
                + " sale - " + sale + " СДАТЬ:" + dateBay + " Bank Bay " + bankBuy + " bay " + bay;
    }

}
