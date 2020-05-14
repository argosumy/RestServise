package com.example.course.model.converters.nbu;

import com.example.course.model.converters.BankParseIn;
import com.example.course.model.converters.TypeBank;
import com.example.course.model.exchange.Exchange;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;


public class BankNBU implements BankParseIn {
    private TypeBank.typeBank typeBank = TypeBank.typeBank.NBU;

    public BankNBU() {
    }

    @Override
    public TypeBank.typeBank getTipeBank() {
        return typeBank;
    }

    @Override
    public String creatURL(String date, String format, String nbuUrl) {
        String [] words = date.split("\\.");
        date = words[2]+words[1]+words[0];
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme("https").host(nbuUrl)
                .queryParam("date",date).query(format).build();
        return uriComponents.toUriString();
    }
    @Override
    public Exchange parserXmlDom(String xmlDom) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = factory.newDocumentBuilder();
        Document document = documentBuilder.parse(new InputSource(new StringReader(xmlDom)));
        document.getDocumentElement().normalize();

        Element root = document.getDocumentElement();
        NodeList nodeList = root.getChildNodes();

        Exchange exchanges = new Exchange();
        exchanges.setDate(root.getElementsByTagName("exchangedate").item(0).getTextContent());
        exchanges.setBank("NBU");
        exchanges.setBaseCurrencyLit("UAH");
        List<Exchange.ExchangeRate> exchangeList = new ArrayList<>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeName().equals("currency")){
                NodeList cur = node.getChildNodes();
                Exchange.ExchangeRate rate = exchanges.new ExchangeRate();
                for (int x = 0; x < cur.getLength(); x++) {
                    Node node1 = cur.item(x);
                    if (!node1.getNodeName().equals("#text")) {
                        if (node1.getNodeName().equals("cc")) {
                            rate.setCurrency(node1.getTextContent());
                            rate.setBaseCurrency("UAN");
                        }
                        if (node1.getNodeName().equals("rate")) {
                            rate.setSaleRate(node1.getTextContent());
                            rate.setPurchaseRate(node1.getTextContent());
                        }
                        if (x > 8) {
                            exchangeList.add(rate);
                        }
                    }
                }
            }
        }
        exchanges.setExchangeRate(exchangeList);
        return exchanges;
    }

    @Override
    public Exchange parseJson(String json) throws JSONException {
        JSONArray jsonArray = new JSONArray(json);
        Exchange exchange = new Exchange();
        exchange.setBank("NBU");
        exchange.setBaseCurrencyLit("UAN");
        List<Exchange.ExchangeRate> listExchange = new ArrayList<>();
        for(int i = 0; i < jsonArray.length();i++ ){
            Exchange.ExchangeRate exchangeRate = exchange.new ExchangeRate();
            JSONObject element = jsonArray.getJSONObject(i);
            if( i == 0){
                exchange.setDate(element.getString("exchangedate"));
            }
            exchangeRate.setCurrency(element.getString("cc"));
            exchangeRate.setSaleRate(element.getString("rate"));
            exchangeRate.setPurchaseRate(element.getString("rate"));
            exchangeRate.setBaseCurrency("UAN");
            listExchange.add(exchangeRate);
        }
        exchange.setExchangeRate(listExchange);
       return exchange;
    }

}
