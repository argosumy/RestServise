package com.example.course.model.services;


import com.example.course.model.converters.BankParseIn;
import com.example.course.model.converters.TypeBank;
import com.example.course.model.converters.WordDoc;
import com.example.course.model.converters.nbu.BankNBU;
import com.example.course.model.converters.privat.BankPrivat;
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
public class ExchangeRatesSearch  {
    private static final Logger LOGGER = Logger.getLogger(ExchangeRatesSearch.class);
    private List<BankParseIn> banks;
    private RestTemplate restTemplate = new RestTemplate();
    @Value("${url.nbu}")
    private String urlNbu;
    @Value("${url.pb}")
    private String urlPb;
    public ExchangeRatesSearch() {
        banks = new ArrayList<>();
        banks.add(new BankNBU());
        banks.add(new BankPrivat());
    }

    public Map<TypeBank.typeBank,List<Exchange>> searcExcangeBanks(String date, String xml_json, String paramCur){
        Map<TypeBank.typeBank,List<Exchange>> mapBanks = new HashMap<>();
        List<Exchange> exchangeList = new ArrayList<>();
            for (BankParseIn bank: banks){
                try {
                    exchangeList = searcExcange(date, xml_json, paramCur, bank);
                    mapBanks.put(bank.getTipeBank(),exchangeList);
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParserConfigurationException e) {
                    e.printStackTrace();
                } catch (SAXException e) {
                    e.printStackTrace();
                }
            }
        return mapBanks;
    }

    public List<Exchange> searcExcange(String date, String xml_json, String paramCur, BankParseIn bank) throws JSONException, IOException, ParserConfigurationException, SAXException {
        List<Exchange> listExchange = null;
        String curr ;
        listExchange = actionDayMonth(date,xml_json,bank);
        /*
        Проверка в запросе наличия условия по валюте
        и вывод соответствующего результата.
         */
        paramCur = paramCur.trim();
        if (paramCur.length()==3){
            curr = paramCur;
            List<Exchange>result  = actionCurr(listExchange,curr);
           // new WordDoc(result,date+"PB");
            listExchange = result;
        }
        else {
            //new WordDoc(listExchange,date + "PB");
        }
        return listExchange;
    }
    /**
     *Валидация даты
     */
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
    public List<Exchange> actionDayMonth(String date,String xml_Json, BankParseIn bank) throws ParserConfigurationException, SAXException, JSONException, IOException {
        List<Exchange> exchangeList = new ArrayList<>();
        Exchange result;
        if(date!=null){
            //вборка за один день
            if(date.length() > 7){
            System.out.println("Name " + Thread.currentThread().getName());
            result = exchange(date,xml_Json, bank);
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
                    String dateF = date;
                    try {
                        future = completionService.submit(() -> exchange(dateF,xml_Json,bank));
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
            ExecutorService executor = Executors.newFixedThreadPool(5);
            CompletionService<Exchange> completionService = new ExecutorCompletionService<>(executor);
            List<Future<Exchange>> listFuture = new ArrayList<>();
            Future<Exchange> future;
            DateTimeFormatter format = DateTimeFormatter.ofPattern("dd.MM.yyyy");
            LocalDate dateNow = LocalDate.now();
            for (LocalDate i = dateNow.minusWeeks(1);i.isBefore(dateNow);i=i.plusDays(1)) {
                date = i.format(format);
                String dateF = date;
                try {
                    future = completionService.submit(() -> exchange(dateF,xml_Json,bank));
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
        return exchangeList;
    }

    public Exchange exchange(String date, String format, BankParseIn bank) throws ParserConfigurationException, SAXException, IOException, JSONException {
        String bankUrl=null;
        if(bank instanceof BankPrivat){
            bankUrl = urlPb;
        }
        if (bank instanceof  BankNBU){
            bankUrl = urlNbu;
        }
        String url = bank.creatURL(date,format,bankUrl);
        String resultTemplate = restTemplate.getForObject(url, String.class);
        Exchange result = null;
        if(format.equals("xml")){
            result = bank.parserXmlDom(resultTemplate);
        }
        if(format.equals("json")){
            result = bank.parseJson(resultTemplate);
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
    /**
     * Выбирает лучший курс на прошедшей недели
     * */
    public String bestCurseWeek(String cur) {
        Map <TypeBank.typeBank,List<Exchange>> mapBank = new HashMap<>();
        mapBank = searcExcangeBanks(null,"json",cur);
        return "Лучший курс на прошедшей недели по банкам: " + bestCurs(mapBank);
    }

    public String bestCurseDay(String date,String xml_json , String cur) throws ParserConfigurationException, SAXException {
        Map <TypeBank.typeBank,List<Exchange>> mapBank = new HashMap<>();
        mapBank = searcExcangeBanks(date,xml_json,cur);
        return "Лучший курс на день по банкам:" +  bestCurs(mapBank);
    }

    public String bestCurs(Map <TypeBank.typeBank,List<Exchange>> mapBank){
        String dateSale = null;
        String dateBay = null;

        String bankSale = "PB";
        String bankBuy = "PB";
        Float sale = Float.parseFloat(mapBank.get(TypeBank.typeBank.PB).get(0).getExchangeRate().get(0).getSaleRate());
        Float bay = Float.parseFloat(mapBank.get(TypeBank.typeBank.PB).get(0).getExchangeRate().get(0).getPurchaseRate());
        for (Map.Entry<TypeBank.typeBank,List<Exchange>> node:mapBank.entrySet()) {
            for (Exchange exchange: node.getValue()){
                System.out.println(exchange.toString());
                    for (int i = 0; i < exchange.getExchangeRate().size();i++){
                        if(sale > Float.parseFloat(exchange.getExchangeRate().get(i).getSaleRate())){
                            System.out.println("Ex"+exchange);
                            sale = Float.parseFloat(exchange.getExchangeRate().get(i).getSaleRate());
                            bankSale = exchange.getBank();
                            dateSale = exchange.getDate();
                        }
                        if(bay < Float.parseFloat(exchange.getExchangeRate().get(i).getPurchaseRate())){
                            bay = Float.parseFloat(exchange.getExchangeRate().get(i).getPurchaseRate());
                            bankBuy = exchange.getBank();
                            dateBay = exchange.getDate();
                        }
                    }
            }
        }
        return "КУПИТЬ " + dateSale + " в банке  - " + bankSale
                + " по курсу - " + sale + " СДАТЬ:" + dateBay + " в банк " + bankBuy + " по курсу " + bay;
    }

}
