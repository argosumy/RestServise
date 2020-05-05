package com.example.course.model.converters;

import com.example.course.model.exchange.Exchange;
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

public class ParseXmlDom {
    String xmlDom;

    public ParseXmlDom(String xmlDom) {
        this.xmlDom = xmlDom;
    }

    public Exchange parserXmlDom() throws IOException, SAXException, ParserConfigurationException {
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
                        if (node1.getNodeName().equals("txt")) {
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
}
