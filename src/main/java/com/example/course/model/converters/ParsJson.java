package com.example.course.model.converters;

import com.example.course.model.exchange.Exchange;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ParsJson {
    String json;

    public ParsJson(String json) throws JSONException {
        this.json = json;
        parsJson(json);
    }

    void parsJson(String json)throws JSONException {
        JSONObject obj = new JSONObject(json);
        Exchange exchange = new Exchange();
        exchange.setDate(obj.getString("date"));
        exchange.setBank(obj.getString("bank"));
        exchange.setBaseCurrencyLit(obj.getString("baseCurrencyLit"));
        System.out.println("Parser");
        System.out.println(exchange.toString());
        JSONArray listEx = obj.getJSONArray("exchangeRate");
        List<Exchange.ExchangeRate> listExchange = new ArrayList<>();
        Exchange.ExchangeRate exchangeRate = exchange.new ExchangeRate();
        for (int i = 0; i < listEx.length(); i++) {
            JSONObject elListEx = listEx.getJSONObject(i);
            if(!elListEx.isNull("currency")) {
                exchangeRate.setCurrency(elListEx.getString("currency"));
            }
            else exchangeRate.setCurrency(null);
            exchangeRate.setSaleRateNB(elListEx.getString("saleRateNB"));
            //exchangeRate.setCurrency(elListEx.getString(""));
            System.out.println(exchangeRate);
            listExchange.add(exchangeRate);
        }
        System.out.println(exchange.toString());

    }

}
