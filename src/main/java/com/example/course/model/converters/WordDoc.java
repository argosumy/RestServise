package com.example.course.model.converters;

import com.example.course.model.exchange.Exchange;
import org.apache.poi.util.Units;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class WordDoc {
    public WordDoc(Exchange exchange, String param) throws IOException {
        wordWrite(exchange,param);
    }

    public void wordWrite(Exchange exchange, String param) {
        String fail = "exchange_" + param;
        try (FileOutputStream fos = new FileOutputStream(new File(fail + ".docx"))) {
            XWPFDocument doc = new XWPFDocument();
            XWPFParagraph paragraph = doc.createParagraph();
            XWPFRun abzac = paragraph.createRun();
            abzac.setText("БАНК - "
                    + exchange.getBank()
                    + " "
                    + "ВАЛЮТА - "
                    + exchange.getBaseCurrencyLit());
            abzac.addBreak();
            for (Exchange.ExchangeRate exchangeRate: exchange.getExchangeRate()){
                abzac.setText("ВАЛЮТА - " + exchangeRate.getCurrency() + "   ");
                abzac.setText("ПРОДАЖА - " + exchangeRate.getSaleRate() + "   ");
                abzac.setText("ПОКУПКА - " + exchangeRate.getPurchaseRate());
                abzac.addBreak();

            }
            doc.write(fos);
        } catch (IOException ex) {

        }
    }
     
}