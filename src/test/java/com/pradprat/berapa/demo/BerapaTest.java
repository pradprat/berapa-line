package com.pradprat.berapa.demo;

import com.pradprat.berapa.demo.utils.CurrencyFormatter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Currency;

@SpringBootTest
class BerapaTest {
	Berapa berapa = new Berapa();
	CurrencyFormatter currencyFormatter = new CurrencyFormatter();

	@Test
	void testPrice() {
		ArrayList<PriceItem> items = new ArrayList<>();
		items.add(new PriceItem("harga",10000));
		items.add(new PriceItem("diskon",50));
		items.add(new PriceItem("diskon",50));
		items.add(new PriceItem("pajak",10));
		System.out.println(berapa.getPrice(items));
		assert (berapa.getPrice(items)==2750);
	}

	@Test
	void testgetItems(){
		String message = "berapa\n" +
				"harga 10000\n" +
				"diskon 50%\n" +
				"diskon 50%\n" +
				"pajak 10%";
		berapa.getItems(message).forEach(priceItem -> {
			System.out.println(priceItem.getName()+" "+priceItem.getNumber());
		});
	}

	@Test
	void testfinalPrice(){
		String message = "berapa\n" +
				"harga 10000\n" +
				"diskon 50%\n" +
				"diskon 50%\n" +
				"pajak 10%";
		System.out.println(berapa.getFinalPrice(message));
	}

}