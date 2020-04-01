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
    }
    public Exchange parsJson()throws JSONException {
        JSONObject obj = new JSONObject(json);
        Exchange exchange = new Exchange();
        exchange.setDate(obj.getString("date"));
        exchange.setBank(obj.getString("bank"));
        exchange.setBaseCurrencyLit(obj.getString("baseCurrencyLit"));
        //System.out.println("Parser");
        JSONArray listEx = obj.getJSONArray("exchangeRate");
        List<Exchange.ExchangeRate> listExchange = new ArrayList<>();
        for (int i = 0; i < listEx.length(); i++) {
            Exchange.ExchangeRate exchangeRate = exchange.new ExchangeRate();
            JSONObject elListEx = listEx.getJSONObject(i);
            if(!elListEx.isNull("saleRate")) {
                exchangeRate.setSaleRate(elListEx.getString("saleRate"));
                exchangeRate.setPurchaseRate(elListEx.getString("purchaseRate"));
                exchangeRate.setCurrency(elListEx.getString("currency"));
                exchangeRate.setBaseCurrency(elListEx.getString("baseCurrency"));
            }
            else {
                continue;
            }
            listExchange.add(exchangeRate);
        }
        exchange.setExchangeRate(listExchange);
        System.out.println(exchange);
        return exchange;
    }
}
