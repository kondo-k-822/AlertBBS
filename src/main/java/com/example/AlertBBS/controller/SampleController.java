//package com.example.AlertBBS.controller;
//
//import com.example.AlertBBS.model.UrlTimestamp;
//import com.example.AlertBBS.repository.UrlTimestampRepository;
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.ModelAttribute;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestParam;
//import org.springframework.web.client.HttpClientErrorException;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.UUID;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//@Controller
//public class SampleController {
//
//    @Autowired
//    private UrlTimestampRepository urlTimestampRepository;
//
//    @ModelAttribute("token")
//    public String generateToken() {
//        return UUID.randomUUID().toString();
//    }
//
//    @GetMapping("/fetch-html")
//    public String fetchHtml() {
//        return "bbscheck";
//    }
//
//    @GetMapping("/sample")
//    public String sample() {
//        return "sample";
//    }
//
//    @PostMapping("/fetch-html-insert")
//    public String fetchHtmlInset(@RequestParam(required = false) String url, RedirectAttributes redirectAttributes, @ModelAttribute("token") String token) {
//        if (url != null) {
//            // URL形式の検証
//            String urlRegex = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$";
//            Pattern urlPattern = Pattern.compile(urlRegex);
//            Matcher urlMatcher = urlPattern.matcher(url);
//            if (!urlMatcher.matches()) {
//                redirectAttributes.addFlashAttribute("result", "Invalid URL format");
//                return "bbscheck";
//            }
//
//            try {
//                RestTemplate restTemplate = new RestTemplate();
//                String response = restTemplate.getForObject(url, String.class);
//
//                Document doc = Jsoup.parse(response);
//                Element div = doc.selectFirst("div.alert.alert-danger.h6.text-center");
//
//                if (div != null) {
//                    String text = div.text();
//                    // Assuming the date is in the format M/D and the time is in the format HH:MM
//                    Pattern pattern = Pattern.compile("\\b\\d{1,2}/\\d{1,2}\\b.*\\b\\d{2}:\\d{2}\\b");
//                    Matcher matcher = pattern.matcher(text);
//                    if (matcher.find()) {
//                        String timestampStr = matcher.group();
//                        redirectAttributes.addFlashAttribute("result", timestampStr);
//                        int currentYear = LocalDateTime.now().getYear();
//
//                        // 日付文字列に現在の年を追加
//                        String timestampStrWithYear = currentYear + "/" + timestampStr;
//                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/M/d HH:mm");
//
//                        // Parse the timestamp string to LocalDateTime
//                        LocalDateTime timestamp = LocalDateTime.parse(timestampStrWithYear, formatter);
//
//                        // Save to database
//                        UrlTimestamp urlTimestamp = new UrlTimestamp();
//                        urlTimestamp.setUrl(url);
//                        urlTimestamp.setEmail("TEST@gamail.com");
//                        urlTimestamp.setTime(timestamp); // Assuming current time as timestamp
//                        urlTimestampRepository.save(urlTimestamp);
//                    } else {
//                        redirectAttributes.addFlashAttribute("result", "Date and time not found");
//                    }
//                } else {
//                    redirectAttributes.addFlashAttribute("result", "既にスレッドは落ちています");
//                }
//            } catch (HttpClientErrorException e) {
//                redirectAttributes.addFlashAttribute("result", "Error URLを確認してください: " + e.getStatusCode() + " " + e.getStatusText());
//            } catch (Exception e) {
//                redirectAttributes.addFlashAttribute("result", "An unexpected error occurred: " + e.getMessage());
//            }
//        }
//        redirectAttributes.addFlashAttribute("token", token);
//        return "redirect:/fetch-html";
//    }
//}