package com.pradprat.berapa.demo;

public class PriceItem {
    private String name;
    private Long number;
    private String formattedNubmer;

    public PriceItem(String name, long number, String formattedNubmer) {
        this.name = name;
        this.number = number;
        this.formattedNubmer = formattedNubmer;

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getNumber() {
        return number;
    }

    public void setNumber(Long number) {
        this.number = number;
    }

    public String getFormattedNubmer() {
        return formattedNubmer;
    }

    public void setFormattedNubmer(String formattedNubmer) {
        this.formattedNubmer = formattedNubmer;
    }
}
