package com.education.flashEng.entity;

import jakarta.persistence.Id;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@AllArgsConstructor
@Document(collection = "class_messages")
public class ClassMessageEntity {

    @Id
    private ObjectId id;

    @Field("class_id")
    private Long classId;

    @Field("sender_id")
    private Long senderId;

    @Field("sender_name")
    private String senderName;

    @Field("sender_role")
    private String senderRole;

    @Field("message")
    private String message;

    @Field("reply_to_message_id")
    private ObjectId replyToMessageId;

    @Field("created_at")
    private LocalDateTime createdAt;

    @Field("deleted")
    private boolean deleted = false;

    public ClassMessageEntity() {
        this.createdAt = LocalDateTime.now();
    }
}
