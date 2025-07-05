package com.education.flashEng.repository.custom.impl;

import com.education.flashEng.payload.response.ClassMessageResponse;
import com.education.flashEng.repository.custom.ClassMessageRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;

@RequiredArgsConstructor
public class ClassMessageRepositoryImpl implements ClassMessageRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public List<ClassMessageResponse> findMessagesByClassId(Long classId) {
        MatchOperation match = Aggregation.match(Criteria.where("class_id").is(classId));
        SortOperation sort = Aggregation.sort(Sort.by(Sort.Direction.DESC, "created_at"));

        // Join chính bảng class_messages để lấy message được reply
        LookupOperation lookupReply = LookupOperation.newLookup()
                .from("class_messages") // Join cùng collection
                .localField("reply_to_message_id") // Field hiện tại
                .foreignField("_id") // _id của message được trả lời
                .as("replyToMessage"); // Kết quả gán vào replyToMessage

        // Nếu không có replyToMessage thì vẫn để null (preserveNullAndEmptyArrays=true)
        UnwindOperation unwindReply = Aggregation.unwind("replyToMessage", true);

        ProjectionOperation project = Aggregation.project()
                .and("_id").as("id")
                .and("sender_name").as("senderName")
                .and("sender_role").as("senderRole")
                .and("message").as("message")
                .and("created_at").as("createdAt")
                .and("deleted").as("deleted")
                .and("replyToMessage").as("replyToMessage"); // Là subdocument

        Aggregation aggregation = Aggregation.newAggregation(
                match,
                sort,
                lookupReply,
                unwindReply,
                project
        );

        return mongoTemplate.aggregate(aggregation, "class_messages", ClassMessageResponse.class)
                .getMappedResults();
    }

}
