package com.education.flashEng.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateClassMessageRequest {

    @NotNull(message = "Class ID is required")
    private Long classId;

    @NotBlank(message = "message cannot be empty")
    private String message;

    private String replyToMessageId;

}
