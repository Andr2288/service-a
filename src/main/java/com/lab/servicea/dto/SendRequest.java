package com.lab.servicea.dto;

/*
    @project   service-a
    @class     SendRequest
    @version   1.0.0
    @since     22.10.2025 - 23:38
*/

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SendRequest {
    private Integer count;
    private String channel; // SYNC_HTTP, ASYNC_HTTP, KAFKA
}