package com.example.course.model.converters;

import com.example.course.model.exchange.Exchange;
import org.json.JSONException;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public interface BankParseIn {
    public Exchange parserXmlDom(String xmlDom) throws IOException, SAXException, ParserConfigurationException;
    public Exchange parseJson(String json) throws JSONException;
    public String creatURL(String date, String format, String url);
    public TypeBank.typeBank getTipeBank();
}
