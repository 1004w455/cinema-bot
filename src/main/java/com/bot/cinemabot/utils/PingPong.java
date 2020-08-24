package com.bot.cinemabot.utils;

import com.bot.cinemabot.model.cgv.CgvItem;
import com.bot.cinemabot.model.lotte.ProductItem;
import com.bot.cinemabot.model.megabox.MegaboxTicket;
import com.bot.cinemabot.service.CgvService;
import com.bot.cinemabot.service.LotteCinemaService;
import com.bot.cinemabot.service.MegaboxService;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by 1004w455 on 2018. 4. 16..
 */
@Slf4j
@Component
public class PingPong extends TelegramLongPollingBot {

//    @Autowired
//    private SimpMessagingTemplate template;

    @Autowired
    private CgvService cgvService;

    @Autowired
    private LotteCinemaService lotteCinemaService;

    @Autowired
    private MegaboxService megaboxService;

    @Value("${spring.bot.telegram.token}")
    private String token;

    @Value("${spring.bot.telegram.username}")
    private String username;

    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public String getBotUsername() {
        return username;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            SendMessage response = new SendMessage();
            Long chatId = message.getChatId();
            response.setChatId(chatId);
            String text = message.getText();
            String msg = null;
            if ("/list".equals(text)) {
                try {
                    String cgv = getCgvMovieTitles();
                    String lotte = getLotteMovieTitles();
                    String megabox = getMegaboxMovieTitles();
                    msg = Stream.of(cgv, lotte, megabox)
                            .filter(Objects::nonNull)
                            .collect(Collectors.joining("\n\n"));
                } catch (Exception e) {
                    response.setText(e.getMessage());
                    executeSend(response, chatId, text);
                    return;
                }
            } else if ("/ping".equals(text)) {
                msg = "pong";
            }

            if (Objects.nonNull(msg)) {
                response.setText(msg);
                executeSend(response, chatId, text);
            }
        }
    }

    private String getMegaboxMovieTitles() {
        List<MegaboxTicket> tickets = megaboxService.initOnePlusOneTickets();
        if (tickets.isEmpty()) return null;
        return "메가박스\n" + tickets
                .stream()
                .map(MegaboxTicket::getName)
                .collect(Collectors.joining("\n"));
    }

    private String getLotteMovieTitles() {
        List<ProductItem> tickets = lotteCinemaService.initOnePlusOneTickets();
        if (tickets.isEmpty()) return null;
        return "롯데시네마\n" + tickets
                .stream()
                .map(ProductItem::getDisplayItemName)
                .collect(Collectors.joining("\n"));
    }

    private String getCgvMovieTitles() throws IOException {
        List<CgvItem> tickets = cgvService.initOnePlusOneTickets();
        if (tickets.isEmpty()) return null;
        return "CGV\n" + tickets
                .stream()
                .map(CgvItem::getDescription)
                .collect(Collectors.joining("\n"));
    }

    private void executeSend(SendMessage response, Long chatId, String text) {
        try {
            execute(response);
            // template.convertAndSend("/topic/greetings", new Greeting(text));
            log.info("Sent message \"{}\" to {}", text, chatId);
        } catch (TelegramApiException e) {
            log.error("Failed to send message \"{}\" to {} due to error: {}", text, chatId, e.getMessage());
        }
    }

}
