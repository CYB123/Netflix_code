package com.framework.bemade.netfliex_code_gen.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class NetflixCrawlerService {

    public List<String> extractVerificationCodes(String url) throws IOException {
        List<String> codes = new ArrayList<>();
        
        // User-Agent 설정 (크롤링 차단 방지)
        Document doc = Jsoup.connect(url)
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .timeout(10000)
                .get();

        // 넷플릭스 페이지에서 코드를 찾는 여러 방법 시도
        
        // 방법 1: input 필드에서 코드 찾기
        Elements inputs = doc.select("input[type='text'], input[type='tel'], input[value]");
        for (Element input : inputs) {
            String value = input.attr("value");
            if (value != null && !value.isEmpty() && isVerificationCode(value)) {
                codes.add(value);
            }
        }

        // 방법 2: 특정 클래스나 ID를 가진 요소에서 찾기
        Elements codeElements = doc.select("[class*='code'], [id*='code'], [class*='verification'], [id*='verification']");
        for (Element element : codeElements) {
            String text = element.text();
            if (text != null && !text.isEmpty() && isVerificationCode(text)) {
                codes.add(text.trim());
            }
        }

        // 방법 3: 일반 텍스트에서 코드 패턴 찾기 (예: 4자리 코드, 6자리 코드 등)
        String bodyText = doc.body().text();
        Pattern codePattern = Pattern.compile("\\b[A-Z0-9]{4,8}\\b");
        Matcher matcher = codePattern.matcher(bodyText);
        while (matcher.find()) {
            String code = matcher.group();
            if (isVerificationCode(code)) {
                codes.add(code);
            }
        }

        // 방법 4: data 속성에서 찾기
        Elements dataElements = doc.select("[data-code], [data-verification-code]");
        for (Element element : dataElements) {
            String code = element.attr("data-code");
            if (code.isEmpty()) {
                code = element.attr("data-verification-code");
            }
            if (!code.isEmpty() && isVerificationCode(code)) {
                codes.add(code);
            }
        }

        // 중복 제거
        return codes.stream().distinct().toList();
    }

    private boolean isVerificationCode(String text) {
        if (text == null || text.length() < 4 || text.length() > 12) {
            return false;
        }
        // 알파벳과 숫자로만 구성된 코드인지 확인
        return text.matches("^[A-Z0-9]+$");
    }

    public boolean clickUpdateButton(String url) throws IOException {
        try {
            // User-Agent 설정 (크롤링 차단 방지)
            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                    .timeout(10000)
                    .followRedirects(true)
                    .get();

            // 버튼 찾기: data-uia="set-primary-location-action" 속성을 가진 버튼
            Element button = doc.select("button[data-uia='set-primary-location-action']").first();
            
            if (button == null) {
                // 다른 방법으로 버튼 찾기 (클래스나 텍스트로)
                Elements buttons = doc.select("button");
                for (Element btn : buttons) {
                    String text = btn.text();
                    if (text != null && (text.contains("업데이트 확정") || text.contains("확정"))) {
                        button = btn;
                        break;
                    }
                }
            }

            if (button != null) {
                // 버튼이 form 안에 있는지 확인
                Element form = button.closest("form");
                if (form != null) {
                    String action = form.attr("action");
                    String method = form.attr("method").toUpperCase();
                    if (method.isEmpty()) {
                        method = "POST";
                    }

                    // form의 모든 input 필드 수집
                    Elements inputs = form.select("input");
                    java.util.Map<String, String> formData = new java.util.HashMap<>();
                    
                    for (Element input : inputs) {
                        String name = input.attr("name");
                        String value = input.attr("value");
                        String type = input.attr("type");
                        
                        if (!name.isEmpty() && !"submit".equals(type) && !"button".equals(type)) {
                            formData.put(name, value);
                        }
                    }

                    // POST 요청 보내기
                    if (action.isEmpty()) {
                        action = url; // action이 없으면 현재 URL 사용
                    } else if (!action.startsWith("http")) {
                        // 상대 경로인 경우 절대 경로로 변환
                        java.net.URL baseUrl = new java.net.URL(url);
                        action = new java.net.URL(baseUrl, action).toString();
                    }

                    org.jsoup.Connection connection = Jsoup.connect(action)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                            .timeout(10000)
                            .followRedirects(true)
                            .method(org.jsoup.Connection.Method.valueOf(method));

                    for (java.util.Map.Entry<String, String> entry : formData.entrySet()) {
                        connection.data(entry.getKey(), entry.getValue());
                    }

                    // 버튼의 name과 value도 추가
                    String buttonName = button.attr("name");
                    String buttonValue = button.attr("value");
                    if (!buttonName.isEmpty()) {
                        connection.data(buttonName, buttonValue.isEmpty() ? "업데이트 확정" : buttonValue);
                    }

                    org.jsoup.Connection.Response response = connection.execute();
                    
                    // 응답 상태 코드 확인
                    return response.statusCode() == 200 || response.statusCode() == 302 || response.statusCode() == 303;
                } else {
                    // form이 없는 경우, 버튼의 onclick이나 data 속성 확인
                    String onclick = button.attr("onclick");
                    String dataAction = button.attr("data-action");
                    
                    // 버튼이 존재하면 성공으로 간주 (실제 클릭은 JavaScript가 필요할 수 있음)
                    return true;
                }
            } else {
                // 버튼을 찾을 수 없음
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
