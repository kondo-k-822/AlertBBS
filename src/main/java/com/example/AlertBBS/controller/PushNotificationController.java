package com.example.AlertBBS.controller;

//import com.example.AlertBBS.model.UrlTimestamp;
//import com.example.AlertBBS.repository.UrlTimestampRepository;
import com.example.AlertBBS.service.EmailService;
import com.example.AlertBBS.service.URLService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/api/push")
public class PushNotificationController {
//    @Autowired
//    private UrlTimestampRepository urlTimestampRepository;
    @Autowired
    private EmailService emailService;
    @Autowired
    private URLService urlService;

    @GetMapping("/fetch-html")
    public String fetchHtml() {
        return "bbscheck";
    }

    @GetMapping("/testemail")
    public String sample() {
        return "testemail";
    }

    @PostMapping(value = "/fetch-html-insert")
    public String fetchHtmlInset(@RequestParam(required = false) String url, String email, RedirectAttributes redirectAttributes, Model model) {
        if (url != null) {
            // URL形式の検証
            if (!(urlService.Urlcheck(url))) {
                model.addAttribute("result", "Invalid URL format");
                return "bbscheck";
            }

            try {
                RestTemplate restTemplate = new RestTemplate();
                String response = restTemplate.getForObject(url, String.class);

                Document doc = Jsoup.parse(response);
                Element div = doc.selectFirst("div.alert.alert-danger.h6.text-center");

                if (div != null) {
                    String text = div.text();
                    // Assuming the date is in the format M/D and the time is in the format HH:MM
                    Pattern pattern = Pattern.compile("\\b\\d{1,2}/\\d{1,2}\\b.*\\b\\d{2}:\\d{2}\\b");
                    Matcher matcher = pattern.matcher(text);
                    if (matcher.find()) {
                        String timestampStr = matcher.group();
                        model.addAttribute("result", timestampStr);
                        int currentYear = LocalDateTime.now().getYear();

                        // 日付文字列に現在の年を追加
                        String timestampStrWithYear = currentYear + "/" + timestampStr;
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/M/d HH:mm");

                        // Parse the timestamp string to LocalDateTime
                        LocalDateTime timestamp = LocalDateTime.parse(timestampStrWithYear, formatter);

                        //メールアドレスが指定されている場合は、メールを送信
                        if (email != null) {
                            String mailText = "スレが落ちるのは" + timestampStr + "だよ！";
                            emailService.sendSimpleEmail(email, "KondoApp_AlertBBS", mailText);
                        }

                        // Save to database
//                        UrlTimestamp urlTimestamp = new UrlTimestamp();
//                        urlTimestamp.setUrl(url);
//                        urlTimestamp.setEmail(email);
//                        urlTimestamp.setTime(timestamp); // Assuming current time as timestamp
//                        urlTimestampRepository.save(urlTimestamp);
                    } else {
                        redirectAttributes.addFlashAttribute("result", "Date and time not found");
                    }
                } else {
                    redirectAttributes.addFlashAttribute("result", "既にスレッドは落ちています");
                    return "redirect:/api/push/fetch-html";
                }
            } catch (HttpClientErrorException e) {
                redirectAttributes.addFlashAttribute("result", "Error URLを確認してください: " + e.getStatusCode() + " " + e.getStatusText());
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("result", "An unexpected error occurred: " + e.getMessage());
            }
        }
        model.addAttribute("email", email);
        model.addAttribute("url", url);
        return "result";
    }

    @ResponseBody
    @PostMapping(value = "/get-endtime")
    public String getEndTime(@RequestParam(required = false) String url) {
        String result = "";
        if (url != null) {
            // URL形式の検証
            String urlRegex = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$";
            Pattern urlPattern = Pattern.compile(urlRegex);
            Matcher urlMatcher = urlPattern.matcher(url);

            try {
                RestTemplate restTemplate = new RestTemplate();
                String response = restTemplate.getForObject(url, String.class);

                Document doc = Jsoup.parse(response);
                Element div = doc.selectFirst("div.alert.alert-danger.h6.text-center");

                if (div != null) {
                    String text = div.text();
                    // Assuming the date is in the format M/D and the time is in the format HH:MM
                    Pattern pattern = Pattern.compile("\\b\\d{1,2}/\\d{1,2}\\b.*\\b\\d{2}:\\d{2}\\b");
                    Matcher matcher = pattern.matcher(text);
                    if (matcher.find()) {
                        String timestampStr = matcher.group();
                        result = timestampStr;
                    } else {
                        return "Date and time not found";
                    }
                } else {
                    return "既にスレッドは落ちています";
                }
            } catch (HttpClientErrorException e) {
                return "Error URLを確認してください";
            } catch (Exception e) {
                return  "An unexpected error occurred";
            }
        }
        return result;
    }

    @ResponseBody
    @PostMapping("/save-result")
    public Map<String, String> saveResult(@RequestBody Map<String, String> payload) {
        String result = payload.get("result");
        String email = payload.get("email");
        String url = payload.get("url");

        //現在の年を取得
        int currentYear = LocalDateTime.now().getYear();

        // 日付文字列に現在の年を追加
        String timestampStrWithYear = currentYear + "/" + result;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/M/d HH:mm");

        // Parse the timestamp string to LocalDateTime
        LocalDateTime timestamp = LocalDateTime.parse(timestampStrWithYear, formatter);
        emailService.scheduleEmail(email, "KondoApp_AlertBBS", "スレはまだ落ちてないよ！" + "定期的にスレを確認しよう! " + url, url, timestamp);

        // result 値を保存するか、他の必要な処理を行います
        System.out.println("Received result: " + timestamp);
        return Map.of("message", "Result saved successfully");
    }
}