package com.pradprat.berapa.demo;

public class PriceCalculator {

    public long addItem(Long normal, PriceItem priceItem){
        switch (priceItem.getName()){
            case "diskon":
                return normal - (normal*priceItem.getNumber()/100);
            case "pajak":
                return normal + (normal*priceItem.getNumber()/100);
            case "harga":
                return normal + priceItem.getNumber();
            default:
                return 0;
        }
    }
}
