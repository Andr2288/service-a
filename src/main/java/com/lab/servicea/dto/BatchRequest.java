package com.lab.servicea.dto;

/*
    @project   service-a
    @class     BatchRequest
    @version   1.0.0
    @since     22.10.2025 - 23:38
*/

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatchRequest {
    private String runId;
    private String channel;
    private List<SMSRequest> messages;
}
