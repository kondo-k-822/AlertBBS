package com.example.AlertBBS.service;

import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private String text;

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @PostConstruct
    public void init() {
        // スケジューラの初期化処理
        scheduler.schedule(() -> {
            logger.info("Scheduler initialized");
            System.out.println("Scheduler initialized");
        }, 0, TimeUnit.SECONDS);
    }

    public void sendSimpleEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        mailSender.send(message);
    }

    public void scheduleEmail(String to, String subject, String text, String url, LocalDateTime sendTime) {
        long delay = TimeUnit.MINUTES.toMillis(60);
        logger.info("scheduleEmail method executed");
        this.text = text;
        if (delay > 0) {
            scheduler.schedule(() -> {
                logger.info("Scheduled task executed");
                sendSimpleEmail(to, subject, text);
                try {
                    RestTemplate restTemplate = new RestTemplate();
                    String response = restTemplate.getForObject(url, String.class);
                    Document doc = Jsoup.parse(response);
                    Element div = doc.selectFirst("div.alert.alert-danger.h6.text-center");
                    if (div == null) {
                        logger.info("Element not found, stopping rescheduling");
                        scheduler.shutdown();
                    } else {
                        String newText = div.text();
                        Pattern pattern = Pattern.compile("\\b\\d{1,2}/\\d{1,2}\\b.*\\b\\d{2}:\\d{2}\\b");
                        Matcher matcher = pattern.matcher(newText);
                        if (matcher.find()) {
                            String timestampStr = matcher.group();
                            int currentYear = LocalDateTime.now().getYear();
                            String timestampStrWithYear = currentYear + "/" + timestampStr;
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/M/d HH:mm");
                            LocalDateTime newSendTime = LocalDateTime.parse(timestampStrWithYear, formatter).minusHours(1);
                            long newDelay = Duration.between(LocalDateTime.now(), newSendTime).toMillis();
                            if (newDelay > 0) {
                                scheduleEmail(to, subject, this.text + "\n" + newText, url, newSendTime);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("Error during scheduled task execution", e);
                    e.printStackTrace();
                }
            }, delay, TimeUnit.MILLISECONDS);
        } else {
            throw new IllegalArgumentException("Adjusted send time must be in the future");
        }
    }

    public void scheduleEmailOneHourBeforeAndCheck(String to, String subject, String url) {
        try {
            RestTemplate restTemplate = new RestTemplate();
            String response = restTemplate.getForObject(url, String.class);
            Document doc = Jsoup.parse(response);
            Element div = doc.selectFirst("div.alert.alert-danger.h6.text-center");

            if (div != null) {
                String text = div.text();
                Pattern pattern = Pattern.compile("\\b\\d{1,2}/\\d{1,2}\\b.*\\b\\d{2}:\\d{2}\\b");
                Matcher matcher = pattern.matcher(text);
                if (matcher.find()) {
                    String timestampStr = matcher.group();
                    int currentYear = LocalDateTime.now().getYear();
                    String timestampStrWithYear = currentYear + "/" + timestampStr;
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/M/d HH:mm");
                    LocalDateTime sendTime = LocalDateTime.parse(timestampStrWithYear, formatter).minusHours(1);
                    long delay = Duration.between(LocalDateTime.now(), sendTime).toMillis();

                    if (delay > 0) {
                        scheduler.schedule(() -> {
                            try {
                                String newResponse = restTemplate.getForObject(url, String.class);
                                Document newDoc = Jsoup.parse(newResponse);
                                Element newDiv = newDoc.selectFirst("div.alert.alert-danger.h6.text-center");

                                if (newDiv != null) {
                                    String newText = newDiv.text();
                                    Matcher newMatcher = pattern.matcher(newText);
                                    if (newMatcher.find()) {
                                        String newTimestampStr = newMatcher.group();
                                        String newTimestampStrWithYear = currentYear + "/" + newTimestampStr;
                                        LocalDateTime newSendTime = LocalDateTime.parse(newTimestampStrWithYear, formatter);

                                        if (newSendTime.equals(sendTime.plusHours(1))) {
                                            sendSimpleEmail(to, subject, "This is a reminder email.");
                                        } else {
                                            scheduleEmailOneHourBeforeAndCheck(to, subject, url);
                                        }
                                    }
                                }
                            } catch (Exception e) {
                                logger.error("Error during scheduled task execution", e);
                                e.printStackTrace();
                            }
                        }, delay, TimeUnit.MILLISECONDS);
                    } else {
                        throw new IllegalArgumentException("Adjusted send time must be in the future");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error during scheduled task execution", e);
            e.printStackTrace();
        }
    }
}