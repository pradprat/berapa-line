package com.pradprat.berapa.demo;

public class PriceCalculator {

    public double addBerapaItem(double normal, PriceItem priceItem) {
        switch (priceItem.getName()) {
            case "diskon":
                return normal - (normal * priceItem.getNumber() / 100);
            case "pajak":
                return normal + (normal * priceItem.getNumber() / 100);
            case "cashback":
                return normal - (normal * priceItem.getNumber() / 100);
            case "harga":
                return normal + priceItem.getNumber();
            default:
                return 0;
        }
    }

    public double countCashback(double normal, PriceItem priceItem) {
        switch (priceItem.getName()) {
            case "cashback":
                return normal + (normal * priceItem.getNumber() / 100);
            case "harga":
                return normal + priceItem.getNumber();
            default:
                return 0;
        }
    }
}
