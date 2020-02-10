package com.pradprat.berapa.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.linecorp.bot.client.LineMessagingClient;
import com.linecorp.bot.client.LineSignatureValidator;
import com.linecorp.bot.model.ReplyMessage;
import com.linecorp.bot.model.event.MessageEvent;
import com.linecorp.bot.model.event.message.TextMessageContent;
import com.linecorp.bot.model.message.FlexMessage;
import com.linecorp.bot.model.message.TextMessage;
import com.linecorp.bot.model.message.flex.container.FlexContainer;
import com.linecorp.bot.model.objectmapper.ModelObjectMapper;
import com.pradprat.berapa.demo.utils.CurrencyFormatter;
import com.pradprat.berapa.demo.utils.Help;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

@RestController
public class Controller {
    CurrencyFormatter currencyFormatter = new CurrencyFormatter();
    Berapa berapa = new Berapa();
    Help help = new Help();


    @Autowired
    @Qualifier("lineMessagingClient")
    private LineMessagingClient lineMessagingClient;

    @Autowired
    @Qualifier("lineSignatureValidator")
    private LineSignatureValidator lineSignatureValidator;

    @RequestMapping(value="/webhook", method= RequestMethod.POST)
    public ResponseEntity<String> callback(
            @RequestHeader("X-Line-Signature") String xLineSignature,
            @RequestBody String eventsPayload)
    {
        try {
            if (!lineSignatureValidator.validateSignature(eventsPayload.getBytes(), xLineSignature)) {
                throw new RuntimeException("Invalid Signature Validation");
            }

            // parsing event
            ObjectMapper objectMapper = ModelObjectMapper.createNewObjectMapper();
            EventsModel eventsModel = objectMapper.readValue(eventsPayload, EventsModel.class);

            eventsModel.getEvents().forEach((event)->{
                if (event instanceof MessageEvent) {
                    MessageEvent messageEvent = (MessageEvent) event;
                    TextMessageContent textMessageContent = (TextMessageContent) messageEvent.getMessage();

                    if (textMessageContent.getText().toLowerCase().contains("help")) {
                        if (textMessageContent.getText().toLowerCase().contains("berapa")) {
                            replyText(messageEvent.getReplyToken(), help.help_berapa);
                        } else if (textMessageContent.getText().toLowerCase().contains("cashback")) {
                            replyText(messageEvent.getReplyToken(), help.help_cashback);
                        } else {
                            replyText(messageEvent.getReplyToken(), help.help);
                        }
                    }
                    if (textMessageContent.getText().toLowerCase().contains("berapa")) {
                        if (textMessageContent.getText().toLowerCase().contains("diskon") || textMessageContent.getText().toLowerCase().contains("pajak")) {
                            replyFlexBerapa(messageEvent.getReplyToken(), textMessageContent.getText());
                        } else if (textMessageContent.getText().toLowerCase().contains("cashback")) {
                            replyFlexCashback(messageEvent.getReplyToken(), textMessageContent.getText());
                        }
                    }
                    if (textMessageContent.getText().toLowerCase().equals("flex")) {
                        replyFlexBerapa(messageEvent.getReplyToken(), textMessageContent.getText());
                    } else {
                        replyText(messageEvent.getReplyToken(), textMessageContent.getText());
                    }
                }
            });

            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    private void reply(ReplyMessage replyMessage) {
        try {
            lineMessagingClient.replyMessage(replyMessage).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }


    private void replyText(String replyToken, String messageToUser) {
        TextMessage textMessage = new TextMessage(messageToUser);
        ReplyMessage replyMessage = new ReplyMessage(replyToken, textMessage);
        reply(replyMessage);
    }

    private void replyFlexBerapa(String replyToken, String message) {
        AtomicReference<String> price = new AtomicReference<>("");
        AtomicReference<String> discount = new AtomicReference<>("0%");
        AtomicReference<String> tax = new AtomicReference<>("0%");

        List<PriceItem> items = berapa.getFormattedItems(berapa.getItems(message));
        double final_price = berapa.getFinalPrice(message);
        items.forEach(priceItem -> {
            if (priceItem.getName().toLowerCase().equals("harga")) {
                price.set(priceItem.getFormattedNubmer());
            } else if (priceItem.getName().toLowerCase().equals("diskon")) {
                discount.set(priceItem.getFormattedNubmer());
            } else if (priceItem.getName().toLowerCase().equals("pajak")) {
                tax.set(priceItem.getFormattedNubmer());
            }
        });
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            String flexTemplate = IOUtils.toString(classLoader.getResourceAsStream("berapa_flex.json"));
            flexTemplate = String.format(flexTemplate,
                    price.get(), discount.get(), tax.get(), currencyFormatter.rupiah(final_price)
            );
            ObjectMapper objectMapper = ModelObjectMapper.createNewObjectMapper();
            FlexContainer flexContainer = objectMapper.readValue(flexTemplate, FlexContainer.class);
            ReplyMessage replyMessage = new ReplyMessage(replyToken, new FlexMessage("" + final_price, flexContainer));
            reply(replyMessage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void replyFlexCashback(String replyToken, String message) {
        ArrayList<String> formattedItem = new ArrayList<>();

        List<PriceItem> items = berapa.getFormattedItems(berapa.getItems(message));
        double final_price = berapa.getFinalCashbackPrice(message);
        double final_cashback = berapa.getFinalCashback(message);

        items.forEach(priceItem -> {
            if (priceItem.getName().toLowerCase().equals("harga")) {
                formattedItem.add(0, priceItem.getFormattedNubmer());
            } else if (priceItem.getName().toLowerCase().equals("cashback")) {
                formattedItem.add(1, priceItem.getFormattedNubmer());
            }
        });

        try {
            ClassLoader classLoader = getClass().getClassLoader();
            String flexTemplate = IOUtils.toString(classLoader.getResourceAsStream("cashback_flex.json"));
            flexTemplate = String.format(flexTemplate,
                    formattedItem.get(0), formattedItem.get(1), currencyFormatter.rupiah(final_cashback), currencyFormatter.rupiah(final_price)
            );
            ObjectMapper objectMapper = ModelObjectMapper.createNewObjectMapper();
            FlexContainer flexContainer = objectMapper.readValue(flexTemplate, FlexContainer.class);
            ReplyMessage replyMessage = new ReplyMessage(replyToken, new FlexMessage("" + final_price, flexContainer));
            reply(replyMessage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
