package com.framework.bemade.netfliex_code_gen.service;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Duration;
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

    /**
     * Selenium(headless Chrome)으로 URL 접속 후 "업데이트 확정" 버튼 클릭.
     * 다음 페이지에 "넷플릭스 이용 가구를 업데이트하셨습니다" 문구가 있으면 성공.
     */
    public boolean clickUpdateButton(String url) throws IOException {
        WebDriver driver = null;
        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless=new");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--disable-setuid-sandbox");
            options.addArguments("--window-size=1920,1080");
            options.addArguments(
                "user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"
            );

            driver = new ChromeDriver(options);
            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(5));
            driver.manage().timeouts().implicitlyWait(Duration.ZERO);

            driver.get(url);

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(5));

            // "업데이트 확정" 버튼: data-uia="set-primary-location-action"
            WebElement button = wait.until(ExpectedConditions.elementToBeClickable(
                By.cssSelector("button[data-uia='set-primary-location-action']")
            ));
            button.click();

            // 다음 페이지에서 "넷플릭스 이용 가구를 업데이트하셨습니다" 문구 대기
            wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//*[contains(., '" + "넷플릭스 이용 가구를 업데이트하셨습니다" + "')]")
            ));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception ignore) {
                }
            }
        }
    }
}
