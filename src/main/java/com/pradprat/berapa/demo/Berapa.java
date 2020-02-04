package com.pradprat.berapa.demo;

import com.pradprat.berapa.demo.utils.CurrencyFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Berapa {
    private PriceCalculator priceCalculator = new PriceCalculator();
    private CurrencyFormatter currencyFormatter = new CurrencyFormatter();


    public List<PriceItem> getItems(String message){
        ArrayList<PriceItem> items = new ArrayList<>();
        ArrayList<String> itemArrayStrings = new ArrayList<>(Arrays.asList(message.split("\n")));
        itemArrayStrings.remove(0); //remove "berapa"
        itemArrayStrings.forEach(item->{
            String[] itemSplit = item.split(" ");
            String userText = itemSplit[1];
            itemSplit[1] = itemSplit[1].replace("%", "");
            items.add(new PriceItem(itemSplit[0], Integer.parseInt(itemSplit[1]), userText));
        });
        return items;
    }

    public double getFinalPrice(String message) {
        long totalPrice = 0;
        ArrayList<PriceItem> items = new ArrayList<>();
        items.addAll(getItems(message));
        for (int i = 0; i < items.size(); i++) {
            totalPrice = priceCalculator.addItem(totalPrice, items.get(i));
        }
        return totalPrice;
    }
}
