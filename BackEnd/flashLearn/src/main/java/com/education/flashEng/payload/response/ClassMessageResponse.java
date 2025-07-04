package com.education.flashEng.payload.response;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ClassMessageResponse {

    private String id;

    private String senderName;

    private String senderRole;

    private String message;

    private String replyToMessageId;

    private LocalDateTime createdAt;

    private boolean deleted;

}
