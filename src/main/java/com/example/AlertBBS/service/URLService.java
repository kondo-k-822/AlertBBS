package com.example.AlertBBS.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class URLService {

    // URL形式の検証
    public boolean Urlcheck(String url) {
        if (url != null) {
            // URL形式の検証
            String urlRegex = "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$";
            Pattern urlPattern = Pattern.compile(urlRegex);
            Matcher urlMatcher = urlPattern.matcher(url);
            if (!urlMatcher.matches()) {
                return false;
            }
        }
        return true;
    }

    public String getBBSEndTime(String url) {
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);

        Document doc = Jsoup.parse(response);
        Element div = doc.selectFirst("div.alert.alert-danger.h6.text-center");
        String text = div.text();
        // Assuming the date is in the format M/D and the time is in the format HH:MM
        Pattern pattern = Pattern.compile("\\b\\d{1,2}/\\d{1,2}\\b.*\\b\\d{2}:\\d{2}\\b");
        Matcher matcher = pattern.matcher(text);
        String timestampStr = "";
        if (matcher.find()) {
            timestampStr = matcher.group();
        }
        return timestampStr;
    }

}
