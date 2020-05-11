package com.example.course.model.converters.privat;

import com.example.course.model.converters.BankParseIn;
import com.example.course.model.converters.TypeBank;
import com.example.course.model.exchange.Exchange;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BankPrivat implements BankParseIn {
    private TypeBank.typeBank typeBank = TypeBank.typeBank.PB;

    public BankPrivat() {
            }

    @Override
    public TypeBank.typeBank getTipeBank() {
        return typeBank;
    }

    @Override
    public String creatURL(String date,String format) {
        String url = "https://api.privatbank.ua/p24api/exchange_rates?" + format + "&date=" + date;
        return url;
    }

    @Override
    public Exchange parserXmlDom(String xmlDom) throws IOException, SAXException, ParserConfigurationException {
        return null;
    }

    @Override
    public Exchange parseJson(String json) throws JSONException {
        JSONObject obj = new JSONObject(json);
        Exchange exchange = new Exchange();
        exchange.setDate(obj.getString("date"));
        exchange.setBank(obj.getString("bank"));
        exchange.setBaseCurrencyLit(obj.getString("baseCurrencyLit"));
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
        return exchange;
    }


}
