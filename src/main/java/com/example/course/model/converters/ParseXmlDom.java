package com.example.course.model.converters;

import com.example.course.model.exchange.Exchange;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ParseXmlDom {
    String xmlDom;

    public ParseXmlDom(String xmlDom) {
        this.xmlDom = xmlDom;
    }

    public Exchange parserXmlDom() throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(xmlDom);
        document.getDocumentElement().normalize();
        Exchange elBlogs = new Exchange();
        elBlogs.setDate(document.getDocumentElement().getAttribute("date"));
        elBlogs.setBank(document.getDocumentElement().getAttribute("bank"));
        elBlogs.setBaseCurrencyLit(document.getDocumentElement().getAttribute("BaseCurrencyLit"));
        List<Exchange.ExchangeRate> exchangeRateList = new ArrayList<>();
        NodeList nList = document.getElementsByTagName("exchangerate");
        for (int i = 0; i < nList.getLength(); i++) {
            Node nNode = nList.item(i);
            System.out.println(nNode.getNodeName());
            System.out.println(nNode.getNodeType());
            if (nNode.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) nNode;
                if (element.getAttribute("saleRate") != "") {
                    Exchange.ExchangeRate elExch = elBlogs.new ExchangeRate();
                    elExch.setBaseCurrency(element.getAttribute("baseCurrency"));
                    elExch.setCurrency(element.getAttribute("currency"));
                    elExch.setSaleRate(element.getAttribute("saleRate"));
                    elExch.setPurchaseRate(element.getAttribute("purchaseRate"));
                    exchangeRateList.add(elExch);
                    System.out.println(element.getAttribute("saleRate"));
                }
            }
        }
        elBlogs.setExchangeRate(exchangeRateList);
        return elBlogs;
    }
}
