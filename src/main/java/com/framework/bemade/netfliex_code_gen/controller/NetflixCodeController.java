package com.framework.bemade.netfliex_code_gen.controller;

import com.framework.bemade.netfliex_code_gen.service.EmailService;
import com.framework.bemade.netfliex_code_gen.service.NetflixCrawlerService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class NetflixCodeController {

    @Autowired
    private EmailService emailService;

    @Autowired
    private NetflixCrawlerService netflixCrawlerService;

    @PostMapping("/generate-code")
    public ResponseEntity<Map<String, Object>> generateNetflixCode() {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 1. POP3로 메일에서 넷플릭스 링크 추출
            String verificationLink = emailService.getNetflixVerificationLink(null, null);
            
            if (verificationLink == null) {
                response.put("success", false);
                response.put("message", "넷플릭스 임시 접속 코드 메일을 찾을 수 없습니다.");
                return ResponseEntity.ok(response);
            }

            // 2. 링크에서 접속 코드 추출
            List<String> codes = netflixCrawlerService.extractVerificationCodes(verificationLink);

            if (codes.isEmpty()) {
                response.put("success", false);
                response.put("message", "접속 코드를 찾을 수 없습니다. 링크: " + verificationLink);
                response.put("link", verificationLink);
                return ResponseEntity.ok(response);
            }

            response.put("success", true);
            response.put("codes", codes);
            response.put("link", verificationLink);
            response.put("message", "접속 코드를 성공적으로 가져왔습니다.");

            return ResponseEntity.ok(response);

        } catch (MessagingException e) {
            response.put("success", false);
            response.put("message", "메일 접속 오류: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "웹 크롤링 오류: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "오류 발생: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/add-device")
    public ResponseEntity<Map<String, Object>> addDevice() {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 1. POP3로 메일에서 넷플릭스 업데이트 링크 추출
            String updateLink = emailService.getNetflixUpdateLink(null, null);
            
            if (updateLink == null) {
                response.put("success", false);
                response.put("message", "넷플릭스 이용 가구 업데이트 메일을 찾을 수 없습니다.");
                return ResponseEntity.ok(response);
            }

            // 2. 링크에서 업데이트 버튼 클릭
            boolean success = netflixCrawlerService.clickUpdateButton(updateLink);

            if (success) {
                response.put("success", true);
                response.put("message", "디바이스 추가가 완료되었습니다.");
                response.put("link", updateLink);
            } else {
                response.put("success", false);
                response.put("message", "유효하지 않는 액션입니다. 디바이스에서 다시 요청해주세요.");
                response.put("link", updateLink);
            }

            return ResponseEntity.ok(response);

        } catch (MessagingException e) {
            response.put("success", false);
            response.put("message", "메일 접속 오류: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "웹 크롤링 오류: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "오류 발생: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    @PostMapping("/login-code")
    public ResponseEntity<Map<String, Object>> getLoginCode() {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 1. POP3로 메일에서 넷플릭스 로그인 코드 추출
            String loginCode = emailService.getNetflixLoginCode(null, null);
            
            if (loginCode == null || loginCode.isEmpty()) {
                response.put("success", false);
                response.put("message", "넷플릭스 로그인 코드 메일을 찾을 수 없습니다.");
                return ResponseEntity.ok(response);
            }

            response.put("success", true);
            response.put("code", loginCode);
            response.put("message", "로그인 코드를 성공적으로 가져왔습니다.");

            return ResponseEntity.ok(response);

        } catch (MessagingException e) {
            response.put("success", false);
            response.put("message", "메일 접속 오류: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        } catch (IOException e) {
            response.put("success", false);
            response.put("message", "메일 파싱 오류: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "오류 발생: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }
}
