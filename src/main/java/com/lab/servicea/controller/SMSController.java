package com.lab.servicea.controller;

/*
    @project   service-a
    @class     SMSController
    @version   1.0.0
    @since     22.10.2025 - 23:37
*/

import com.lab.servicea.dto.SendRequest;
import com.lab.servicea.dto.SMSRequest;
import com.lab.servicea.model.SMS;
import com.lab.servicea.service.SMSService;
import com.lab.servicea.service.SenderService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/sms")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class SMSController {

    private final SMSService smsService;
    private final SenderService senderService;
    private final RestTemplate restTemplate;

    @Value("${service-b.url}")
    private String serviceBUrl;

    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generateSMS(@RequestBody Map<String, Integer> request) {
        int count = request.get("count");

        if (count < 1 || count > 100000) {
            return ResponseEntity.badRequest().body(Map.of("error", "Count must be between 1 and 100000"));
        }

        List<SMS> generatedSMS = smsService.generateSMS(count);

        Map<String, Object> response = new HashMap<>();
        response.put("count", generatedSMS.size());
        response.put("message", "SMS generated successfully");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/list")
    public ResponseEntity<Page<SMS>> listSMS(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<SMS> smsPage = smsService.getAllSMS(page, size);
        return ResponseEntity.ok(smsPage);
    }

    @PostMapping("/send")
    public ResponseEntity<Map<String, String>> sendSMS(@RequestBody SendRequest request) {
        Page<SMS> smsPage = smsService.getAllSMS(0, request.getCount());
        List<SMS> smsList = smsPage.getContent();

        if (smsList.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "No SMS to send. Please generate first."));
        }

        List<SMSRequest> smsRequests = smsService.convertToRequests(smsList);

        String runId;

        switch (request.getChannel().toUpperCase()) {
            case "SYNC_HTTP":
                runId = senderService.sendSyncHTTP(smsRequests);
                break;
            case "ASYNC_HTTP":
                runId = senderService.sendAsyncHTTP(smsRequests);
                break;
            case "KAFKA":
                runId = senderService.sendViaKafka(smsRequests);
                break;
            default:
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid channel"));
        }

        Map<String, String> response = new HashMap<>();
        response.put("runId", runId);
        response.put("channel", request.getChannel());
        response.put("message", "Sending started");

        return ResponseEntity.ok(response);
    }

    @GetMapping("/metrics/{runId}")
    public ResponseEntity<Map<String, Object>> getMetrics(@PathVariable String runId) {
        try {
            String url = serviceBUrl + "/api/metrics/summary?runId=" + runId;
            Map<String, Object> metrics = restTemplate.getForObject(url, Map.class);
            return ResponseEntity.ok(metrics);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Could not fetch metrics");
            return ResponseEntity.ok(error);
        }
    }

    @GetMapping("/results")
    public ResponseEntity<List<Map<String, Object>>> getResults() {
        try {
            String url = serviceBUrl + "/api/results";
            List<Map<String, Object>> results = restTemplate.getForObject(url, List.class);
            return ResponseEntity.ok(results);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of());
        }
    }
}