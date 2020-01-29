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
            item = item.replace("%", "");
            String[] itemSplit = item.split(" ");
            items.add(new PriceItem(itemSplit[0],Integer.parseInt(itemSplit[1])));
//            System.out.println(item);
        });
//        System.out.println(items.get(0).getNumber());
        return items;
    }

    public long getPrice(List<PriceItem> items){
        long totalPrice = 0;
        for(int i=0; i<items.size();i++){
            totalPrice = priceCalculator.addItem(totalPrice, items.get(i));
        }
        return totalPrice;
    }

    public String getFinalPrice(String message){
        ArrayList<PriceItem> items = new ArrayList<>();
        items.addAll(getItems(message));
        return currencyFormatter.rupiah(getPrice(items));
    }
}
