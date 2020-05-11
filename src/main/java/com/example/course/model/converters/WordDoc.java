package com.example.course.model.converters;

import com.example.course.model.exchange.Exchange;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class WordDoc {
    public WordDoc() {
    }

    /**
     * Формирует массив байтов для отправки в файле
     *
     */
    public byte[] wordWrite(Map<TypeBank.typeBank,List<Exchange>> typeBankListMap) {
            XWPFDocument doc = new XWPFDocument();
            XWPFParagraph paragraph = doc.createParagraph();
            XWPFRun abzac = paragraph.createRun();
            for(Map.Entry entry: typeBankListMap.entrySet()) {
                List<Exchange> exchangeList = (List<Exchange>) entry.getValue();
                for (Exchange exchange : exchangeList) {
                    abzac.setText("ДАТА - " + exchange.getDate()
                            + " "
                            + "БАНК - "
                            + exchange.getBank()
                            + " "
                            + "ВАЛЮТА - "
                            + exchange.getBaseCurrencyLit());
                    abzac.addBreak();
                    for (Exchange.ExchangeRate exchangeRate : exchange.getExchangeRate()) {
                        abzac.setText("ВАЛЮТА - " + exchangeRate.getCurrency() + "   ");
                        abzac.setText("ПРОДАЖА - " + exchangeRate.getSaleRate() + "   ");
                        abzac.setText("ПОКУПКА - " + exchangeRate.getPurchaseRate());
                        abzac.addBreak();
                    }
                }
            }
        ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
        try {
            doc.write(byteArray);
        } catch (IOException e) {
            e.printStackTrace();
        }
      return byteArray.toByteArray();
    }

}
