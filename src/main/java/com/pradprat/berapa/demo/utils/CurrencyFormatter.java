package com.pradprat.berapa.demo.utils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class CurrencyFormatter {
    public String rupiah(double harga) {

        DecimalFormat kursIndonesia = (DecimalFormat) DecimalFormat.getCurrencyInstance();
        DecimalFormatSymbols formatRp = new DecimalFormatSymbols();

        formatRp.setCurrencySymbol("Rp");
        formatRp.setMonetaryDecimalSeparator(',');
        formatRp.setGroupingSeparator('.');

        kursIndonesia.setDecimalFormatSymbols(formatRp);
        return kursIndonesia.format(harga);
    }
}
