package com.framework.bemade.netfliex_code_gen.service;

import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class EmailService {

    public String getNetflixVerificationLink(String email2, String password2) throws MessagingException, IOException {
        Properties props = new Properties();
        props.put("mail.pop3.host", "pop.naver.com"); //원하는 메일 서버
        props.put("mail.pop3.port", "995");
        props.put("mail.pop3.ssl.enable", "true");
        props.put("mail.pop3.ssl.trust", "pop.naver.com");

        String email = "abc123@naver.com"; // 메일 주소
        String password = "1234"; // 메일 비밀번호

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email, password);
            }
        });

        Store store = session.getStore("pop3s");
        store.connect("pop.naver.com", email, password);

        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);

        Message[] allMessages = inbox.getMessages();
        String targetSubject = "회원님의 넷플릭스 임시 접속 코드";

        // 현재 시간에서 20분 이내의 메일만 필터링
        long currentTime = System.currentTimeMillis();
        long twentyMinutesAgo = currentTime - (20 * 60 * 1000); // 20분 = 1200000 밀리초
        
        List<Message> recentMessages = new ArrayList<>();
        for (Message msg : allMessages) {
            try {
                Date sentDate = msg.getSentDate();
                if (sentDate != null && sentDate.getTime() >= twentyMinutesAgo) {
                    recentMessages.add(msg);
                }
            } catch (MessagingException e) {
                // 날짜를 가져올 수 없는 경우 스킵
            }
        }

        // 최신 메일부터 검색
        for (int i = recentMessages.size() - 1; i >= 0; i--) {
            Message message = recentMessages.get(i);
            if (message.getSubject() != null && message.getSubject().contains(targetSubject)) {
                String link = extractLinkFromMessage(message);
                if (link != null && link.contains("netflix.com/account/travel/verify")) {
                    inbox.close(false);
                    store.close();
                    return link;
                }
            }
        }

        inbox.close(false);
        store.close();
        return null;
    }

    private String extractLinkFromMessage(Message message) throws MessagingException, IOException {
        Object content = message.getContent();
        String htmlContent = null;

        if (content instanceof String) {
            htmlContent = (String) content;
        } else if (content instanceof MimeMultipart) {
            MimeMultipart multipart = (MimeMultipart) content;
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (bodyPart.getContentType().contains("text/html")) {
                    htmlContent = (String) bodyPart.getContent();
                    break;
                }
            }
        }

        if (htmlContent != null) {
            // Jsoup을 사용하여 HTML 파싱
            Document doc = Jsoup.parse(htmlContent);
            String link = doc.select("a[href*='netflix.com/account/travel/verify']").attr("href");
            if (!link.isEmpty()) {
                return link;
            }

            // 정규식으로도 시도
            Pattern pattern = Pattern.compile("https://www\\.netflix\\.com/account/travel/verify\\?[^\\s\"']+");
            Matcher matcher = pattern.matcher(htmlContent);
            if (matcher.find()) {
                return matcher.group();
            }
        }

        return null;
    }

    public String getNetflixUpdateLink(String email2, String password2) throws MessagingException, IOException {
        Properties props = new Properties();
        props.put("mail.pop3.host", "pop.naver.com");
        props.put("mail.pop3.port", "995");
        props.put("mail.pop3.ssl.enable", "true");
        props.put("mail.pop3.ssl.trust", "pop.naver.com");

        String email = "alpush1024@naver.com";
        String password = "ZWENTBXB4Y8B";

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email, password);
            }
        });

        Store store = session.getStore("pop3s");
        store.connect("pop.naver.com", email, password);

        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);

        Message[] allMessages = inbox.getMessages();
        String targetSubject = "중요: 넷플릭스 이용 가구를 업데이트하는 방법";

        // 현재 시간에서 20분 이내의 메일만 필터링
        long currentTime = System.currentTimeMillis();
        long twentyMinutesAgo = currentTime - (20 * 60 * 1000); // 20분 = 1200000 밀리초
        
        List<Message> recentMessages = new ArrayList<>();
        for (Message msg : allMessages) {
            try {
                Date sentDate = msg.getSentDate();
                if (sentDate != null && sentDate.getTime() >= twentyMinutesAgo) {
                    recentMessages.add(msg);
                }
            } catch (MessagingException e) {
                // 날짜를 가져올 수 없는 경우 스킵
            }
        }

        // 최신 메일부터 검색
        for (int i = recentMessages.size() - 1; i >= 0; i--) {
            Message message = recentMessages.get(i);
            if (message.getSubject() != null && message.getSubject().contains(targetSubject)) {
                String link = extractUpdateLinkFromMessage(message);
                if (link != null && link.contains("netflix.com/account/update-primary-location")) {
                    inbox.close(false);
                    store.close();
                    return link;
                }
            }
        }

        inbox.close(false);
        store.close();
        return null;
    }

    private String extractUpdateLinkFromMessage(Message message) throws MessagingException, IOException {
        Object content = message.getContent();
        String htmlContent = null;

        if (content instanceof String) {
            htmlContent = (String) content;
        } else if (content instanceof MimeMultipart) {
            MimeMultipart multipart = (MimeMultipart) content;
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (bodyPart.getContentType().contains("text/html")) {
                    htmlContent = (String) bodyPart.getContent();
                    break;
                }
            }
        }

        if (htmlContent != null) {
            // Jsoup을 사용하여 HTML 파싱
            Document doc = Jsoup.parse(htmlContent);
            String link = doc.select("a[href*='netflix.com/account/update-primary-location']").attr("href");
            if (!link.isEmpty()) {
                return link;
            }

            // 정규식으로도 시도
            Pattern pattern = Pattern.compile("https://www\\.netflix\\.com/account/update-primary-location\\?[^\\s\"']+");
            Matcher matcher = pattern.matcher(htmlContent);
            if (matcher.find()) {
                return matcher.group();
            }
        }

        return null;
    }

    public String getNetflixLoginCode(String email2, String password2) throws MessagingException, IOException {
        Properties props = new Properties();
        props.put("mail.pop3.host", "pop.naver.com");
        props.put("mail.pop3.port", "995");
        props.put("mail.pop3.ssl.enable", "true");
        props.put("mail.pop3.ssl.trust", "pop.naver.com");

        String email = "alpush1024@naver.com";
        String password = "ZWENTBXB4Y8B";

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(email, password);
            }
        });

        Store store = session.getStore("pop3s");
        store.connect("pop.naver.com", email, password);

        Folder inbox = store.getFolder("INBOX");
        inbox.open(Folder.READ_ONLY);

        Message[] allMessages = inbox.getMessages();
        String targetSubject = "넷플릭스: 로그인 코드";

        // 현재 시간에서 20분 이내의 메일만 필터링
        long currentTime = System.currentTimeMillis();
        long twentyMinutesAgo = currentTime - (20 * 60 * 1000); // 20분 = 1200000 밀리초
        
        List<Message> recentMessages = new ArrayList<>();
        for (Message msg : allMessages) {
            try {
                Date sentDate = msg.getSentDate();
                if (sentDate != null && sentDate.getTime() >= twentyMinutesAgo) {
                    recentMessages.add(msg);
                }
            } catch (MessagingException e) {
                // 날짜를 가져올 수 없는 경우 스킵
            }
        }

        // 최신 메일부터 검색
        for (int i = recentMessages.size() - 1; i >= 0; i--) {
            Message message = recentMessages.get(i);
            if (message.getSubject() != null && message.getSubject().contains(targetSubject)) {
                String code = extractLoginCodeFromMessage(message);
                if (code != null && !code.isEmpty()) {
                    inbox.close(false);
                    store.close();
                    return code;
                }
            }
        }

        inbox.close(false);
        store.close();
        return null;
    }

    private String extractLoginCodeFromMessage(Message message) throws MessagingException, IOException {
        Object content = message.getContent();
        String htmlContent = null;

        if (content instanceof String) {
            htmlContent = (String) content;
        } else if (content instanceof MimeMultipart) {
            MimeMultipart multipart = (MimeMultipart) content;
            for (int i = 0; i < multipart.getCount(); i++) {
                BodyPart bodyPart = multipart.getBodyPart(i);
                if (bodyPart.getContentType().contains("text/html")) {
                    htmlContent = (String) bodyPart.getContent();
                    break;
                }
            }
        }

        if (htmlContent != null) {
            // Jsoup을 사용하여 HTML 파싱
            Document doc = Jsoup.parse(htmlContent);
            
            // 방법 1: 특정 스타일을 가진 td 태그에서 코드 찾기
            // font-size: 28px, letter-spacing: 6px를 가진 td 태그
            Elements tds = doc.select("td");
            for (Element td : tds) {
                String style = td.attr("style");
                if (style.contains("font-size: 28px") && style.contains("letter-spacing: 6px")) {
                    String text = td.text().trim();
                    // 숫자 4자리 코드인지 확인
                    if (text.matches("\\d{4}")) {
                        return text;
                    }
                }
            }

            // 방법 2: letter-spacing이 6px인 요소 찾기 (코드의 특징)
            Elements elements = doc.select("[style*='letter-spacing: 6px']");
            for (Element element : elements) {
                String text = element.text().trim();
                if (text.matches("\\d{4}")) {
                    return text;
                }
            }

            // 방법 3: 정규식으로 4자리 숫자 코드 찾기 (공백 제거 후)
            Pattern pattern = Pattern.compile("\\s*(\\d{4})\\s*");
            Matcher matcher = pattern.matcher(htmlContent);
            while (matcher.find()) {
                String code = matcher.group(1);
                // 주변 텍스트 확인 (코드를 입력하고 로그인하세요 근처)
                int start = Math.max(0, matcher.start() - 100);
                int end = Math.min(htmlContent.length(), matcher.end() + 100);
                String context = htmlContent.substring(start, end).toLowerCase();
                if (context.contains("코드를 입력") || context.contains("로그인")) {
                    return code;
                }
            }
        }

        return null;
    }
}
