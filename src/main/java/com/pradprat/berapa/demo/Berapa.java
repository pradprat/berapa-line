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
            itemSplit[1] = itemSplit[1].replace(".", "");
            items.add(new PriceItem(itemSplit[0], Integer.parseInt(itemSplit[1]), userText));
        });
        return items;
    }

    public double getFinalPrice(String message) {
        double totalPrice = 0;
        ArrayList<PriceItem> items = new ArrayList<>();
        items.addAll(getItems(message));
        for (int i = 0; i < items.size(); i++) {
            totalPrice = priceCalculator.addBerapaItem(totalPrice, items.get(i));
        }
        return totalPrice;
    }

    public double getFinalCashbackPrice(String message) {
        double totalCashbackPrice = 0;
        ArrayList<PriceItem> items = new ArrayList<>();
        items.addAll(getItems(message));
        for (int i = 0; i < items.size(); i++) {
            totalCashbackPrice = priceCalculator.addBerapaItem(totalCashbackPrice, items.get(i));
        }
        return totalCashbackPrice;
    }

    public double getFinalCashback(String message) {
        final double[] totalCashback = {0};
        ArrayList<PriceItem> items = new ArrayList<>();
        items.addAll(getItems(message));
        for (int i = 0; i < items.size(); i++) {
            totalCashback[0] = priceCalculator.countCashback(totalCashback[0], items.get(i));
        }
        items.forEach(priceItem -> {
            if (priceItem.getName().equals("harga")) {
                totalCashback[0] = totalCashback[0] - priceItem.getNumber();
            }
        });

        return totalCashback[0];
    }

    public List<PriceItem> getFormattedItems(List<PriceItem> items) {
        String[] temp = {"", ""};
        items.forEach(priceItem -> {
            if (priceItem.getName().equals("harga")) {
                priceItem.setFormattedNubmer(currencyFormatter.rupiah(priceItem.getNumber()));
            } else if (priceItem.getName().equals("diskon")) {
                if (!priceItem.getFormattedNubmer().contains("%")) {
                    priceItem.setFormattedNubmer(priceItem.getFormattedNubmer() + "%");
                }
                if (temp[0].equals("diskon")) {
                    temp[1] = temp[1] + "+";
                } else {
                    temp[0] = "";
                    temp[1] = "";
                }
                priceItem.setFormattedNubmer(temp[1] + priceItem.getFormattedNubmer());
                temp[1] = temp[1] + priceItem.getFormattedNubmer();
                temp[0] = "diskon";
            } else if (priceItem.getName().equals("pajak")) {
                if (!priceItem.getFormattedNubmer().contains("%")) {
                    priceItem.setFormattedNubmer(priceItem.getFormattedNubmer() + "%");
                }
                if (temp[0].equals("pajak")) {
                    temp[1] = temp[1] + "+";
                } else {
                    temp[0] = "";
                    temp[1] = "";
                }
                priceItem.setFormattedNubmer(temp[1] + priceItem.getFormattedNubmer());
                temp[1] = temp[1] + priceItem.getFormattedNubmer();
                temp[0] = "pajak";
            } else if (priceItem.getName().equals("cashback")) {
                if (!priceItem.getFormattedNubmer().contains("%")) {
                    priceItem.setFormattedNubmer(priceItem.getFormattedNubmer() + "%");
                }
                if (temp[0].equals("pajak")) {
                    temp[1] = temp[1] + "+";
                } else {
                    temp[0] = "";
                    temp[1] = "";
                }
                priceItem.setFormattedNubmer(temp[1] + priceItem.getFormattedNubmer());
                temp[1] = temp[1] + priceItem.getFormattedNubmer();
                temp[0] = "pajak";
            }
        });
        return items;
    }

}
