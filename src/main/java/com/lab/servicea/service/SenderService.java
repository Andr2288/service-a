package com.lab.servicea.service;

/*
    @project   service-a
    @class     SenderService
    @version   1.0.0
    @since     22.10.2025 - 23:36
*/

import com.lab.servicea.dto.BatchRequest;
import com.lab.servicea.dto.SMSRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SenderService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final KafkaTemplate<String, BatchRequest> kafkaTemplate;
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);

    @Value("${service-b.url}")
    private String serviceBUrl;

    private static final String KAFKA_TOPIC = "sms-topic";

    public String sendSyncHTTP(List<SMSRequest> messages) {
        String runId = UUID.randomUUID().toString();

        BatchRequest request = new BatchRequest();
        request.setRunId(runId);
        request.setChannel("SYNC_HTTP");
        request.setMessages(messages);

        try {
            restTemplate.postForObject(serviceBUrl + "/api/messages/batch", request, String.class);
            log.info("Sent {} messages via SYNC_HTTP with runId: {}", messages.size(), runId);
        } catch (Exception e) {
            log.error("Error sending via SYNC_HTTP: {}", e.getMessage());
        }

        return runId;
    }

    public String sendAsyncHTTP(List<SMSRequest> messages) {
        String runId = UUID.randomUUID().toString();

        int batchSize = 1000;
        List<List<SMSRequest>> batches = splitIntoBatches(messages, batchSize);

        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (List<SMSRequest> batch : batches) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                BatchRequest request = new BatchRequest();
                request.setRunId(runId);
                request.setChannel("ASYNC_HTTP");
                request.setMessages(batch);

                try {
                    restTemplate.postForObject(serviceBUrl + "/api/messages/batch", request, String.class);
                    log.info("Sent batch of {} messages via ASYNC_HTTP", batch.size());
                } catch (Exception e) {
                    log.error("Error sending batch via ASYNC_HTTP: {}", e.getMessage());
                }
            }, executorService);

            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        log.info("All batches sent via ASYNC_HTTP with runId: {}", runId);

        return runId;
    }

    public String sendViaKafka(List<SMSRequest> messages) {
        String runId = UUID.randomUUID().toString();

        int batchSize = 1000;
        List<List<SMSRequest>> batches = splitIntoBatches(messages, batchSize);

        for (List<SMSRequest> batch : batches) {
            BatchRequest request = new BatchRequest();
            request.setRunId(runId);
            request.setChannel("KAFKA");
            request.setMessages(batch);

            kafkaTemplate.send(KAFKA_TOPIC, runId, request);
            log.info("Sent batch of {} messages to Kafka", batch.size());
        }

        log.info("All batches sent to Kafka with runId: {}", runId);
        return runId;
    }

    private List<List<SMSRequest>> splitIntoBatches(List<SMSRequest> messages, int batchSize) {
        List<List<SMSRequest>> batches = new ArrayList<>();

        for (int i = 0; i < messages.size(); i += batchSize) {
            int end = Math.min(i + batchSize, messages.size());
            batches.add(new ArrayList<>(messages.subList(i, end)));
        }

        return batches;
    }
}