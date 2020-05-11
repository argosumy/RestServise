package com.example.course.model.exchange;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;
//@XmlRootElement
//@XmlAccessorType(XmlAccessType.NONE)
public class Exchange implements Comparable<Exchange>{
    private String date;
    private String bank;
    private String baseCurrencyLit;
//    @XmlElement
    private List<ExchangeRate> exchangeRate;

    public Exchange() {
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getBank() {
        return bank;
    }

    public void setBank(String bank) {
        this.bank = bank;
    }

    public String getBaseCurrencyLit() {
        return baseCurrencyLit;
    }

    public void setBaseCurrencyLit(String baseCurrencyLit) {
        this.baseCurrencyLit = baseCurrencyLit;
    }

    public List<ExchangeRate> getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(List<ExchangeRate> exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    @Override
    public int compareTo(Exchange o) {
        return this.getDate().compareTo(o.getDate());
    }

    public class ExchangeRate {

        private String baseCurrency;
        private String saleRate;
        private String purchaseRate;
        private String currency;

        public ExchangeRate() {
        }

        public String getBaseCurrency() {
            return baseCurrency;
        }

        public void setBaseCurrency(String baseCurrency) {
            this.baseCurrency = baseCurrency;
        }

        public String getSaleRate() {
            return saleRate;
        }

        public void setSaleRate(String saleRate) {
            this.saleRate = saleRate;
        }

        public String getPurchaseRate() {
            return purchaseRate;
        }

        public void setPurchaseRate(String purchaseRate) {
            this.purchaseRate = purchaseRate;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }

        @Override
        public String toString() {
            return "\nExchangeRate{" +
                    "baseCurrency='" + baseCurrency + '\'' +
                    ", saleRate=" + saleRate +
                    ", purchaseRate=" + purchaseRate +
                    ", currency='" + currency + '\'' +
                    '}';
        }
    }

    @Override
    public String toString() {
        return "\nExchange{" +
                "date='" + date + '\'' +
                ", bank='" + bank + '\'' +
                ", baseCurrencyLit='" + baseCurrencyLit + '\'' +
                ", exchangeRate=" + exchangeRate +
                '}';
    }
}
