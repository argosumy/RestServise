package com.example.course.model.exchange;

import java.util.List;

public class Exchange {
    private String date;
    private String bank;
    private String baseCurrencyLit;
    private List<ExchangeRate> exchangeRate = null;

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


    class ExchangeRate {

        private String baseCurrency;
        private Float saleRateNB;
        private Float purchaseRateNB;
        private String currency;

        public String getBaseCurrency() {
            return baseCurrency;
        }

        public void setBaseCurrency(String baseCurrency) {
            this.baseCurrency = baseCurrency;
        }

        public Float getSaleRateNB() {
            return saleRateNB;
        }

        public void setSaleRateNB(Float saleRateNB) {
            this.saleRateNB = saleRateNB;
        }

        public Float getPurchaseRateNB() {
            return purchaseRateNB;
        }

        public void setPurchaseRateNB(Float purchaseRateNB) {
            this.purchaseRateNB = purchaseRateNB;
        }

        public String getCurrency() {
            return currency;
        }

        public void setCurrency(String currency) {
            this.currency = currency;
        }
    }

}
