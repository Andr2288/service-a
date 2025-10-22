package com.lab.servicea.service;

/*
    @project   service-a
    @class     SMSService
    @version   1.0.0
    @since     22.10.2025 - 23:37
*/

import com.lab.servicea.dto.SMSRequest;
import com.lab.servicea.model.SMS;
import com.lab.servicea.repository.SMSRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SMSService {

    private final SMSRepository smsRepository;
    private final Random random = new Random();

    private final String[] messageTemplates = {
            "Hello! How are you?",
            "Meeting at 3 PM today",
            "Please call me back",
            "Happy Birthday!",
            "Thank you for your help",
            "See you tomorrow",
            "Good morning!",
            "Have a great day!",
            "Reminder: Pay bills",
            "Check your email"
    };

    public List<SMS> generateSMS(int count) {
        List<SMS> smsList = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            SMS sms = new SMS();
            sms.setId(UUID.randomUUID().toString());
            sms.setSender(generatePhoneNumber());
            sms.setReceiver(generatePhoneNumber());
            sms.setText(messageTemplates[random.nextInt(messageTemplates.length)]);
            sms.setCreatedAt(LocalDateTime.now());

            smsList.add(sms);
        }

        return smsRepository.saveAll(smsList);
    }

    public Page<SMS> getAllSMS(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return smsRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public List<SMSRequest> convertToRequests(List<SMS> smsList) {
        List<SMSRequest> requests = new ArrayList<>();

        for (SMS sms : smsList) {
            SMSRequest request = new SMSRequest();
            request.setSender(sms.getSender());
            request.setReceiver(sms.getReceiver());
            request.setText(sms.getText());
            requests.add(request);
        }

        return requests;
    }

    private String generatePhoneNumber() {
        return "+380" + String.format("%09d", random.nextInt(1000000000));
    }
}
