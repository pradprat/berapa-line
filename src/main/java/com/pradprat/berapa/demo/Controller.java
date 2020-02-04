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
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
public class Controller {

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

                    if (textMessageContent.getText().equals("hai")) {
                        replyText(messageEvent.getReplyToken(), "bacod");
                    }
                    if (textMessageContent.getText().contains("berapa")) {
                        replyFlexMessage(messageEvent.getReplyToken(), textMessageContent.getText());
//                        replyText(messageEvent.getReplyToken(), new Berapa().getFinalPrice(textMessageContent.getText()));
                    }
                    if (textMessageContent.getText().equals("flex")) {
                        replyFlexMessage(messageEvent.getReplyToken(), textMessageContent.getText());
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

    private void replyFlexMessage(String replyToken, String message) {
        Berapa berapa = new Berapa();
        List<PriceItem> items = berapa.getItems(message);
        double final_price = berapa.getFinalPrice(message);


        try {
            ClassLoader classLoader = getClass().getClassLoader();
            String flexTemplate = IOUtils.toString(classLoader.getResourceAsStream("berapa_flex.json"));

            flexTemplate = String.format(flexTemplate,
                    items.get(0).getNumber(), items.get(1).getNumber(), items.get(2).getNumber(), final_price
            );

            ObjectMapper objectMapper = ModelObjectMapper.createNewObjectMapper();
            FlexContainer flexContainer = objectMapper.readValue(flexTemplate, FlexContainer.class);

            ReplyMessage replyMessage = new ReplyMessage(replyToken, new FlexMessage("finalPrice", flexContainer));
            reply(replyMessage);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
