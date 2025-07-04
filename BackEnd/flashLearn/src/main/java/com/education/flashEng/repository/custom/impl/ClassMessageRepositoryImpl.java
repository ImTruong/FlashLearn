package com.education.flashEng.repository.custom.impl;

import com.education.flashEng.payload.response.ClassMessageResponse;
import com.education.flashEng.repository.custom.ClassMessageRepositoryCustom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.ProjectionOperation;
import org.springframework.data.mongodb.core.aggregation.SortOperation;
import org.springframework.data.mongodb.core.query.Criteria;

import java.util.List;

@RequiredArgsConstructor
public class ClassMessageRepositoryImpl implements ClassMessageRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    public List<ClassMessageResponse> findMessagesByClassId(Long classId) {
        MatchOperation match = Aggregation.match(Criteria.where("class_id").is(classId));
        SortOperation sort = Aggregation.sort(Sort.by(Sort.Direction.DESC, "created_at"));
        ProjectionOperation project = Aggregation.project()
                .and("id").as("id")
                .and("sender_name").as("senderName")
                .and("sender_role").as("senderRole")
                .and("message").as("message")
                .and("reply_to_message_id").as("replyToMessageId")
                .and("created_at").as("createdAt")
                .and("deleted").as("deleted");

        Aggregation aggregation = Aggregation.newAggregation(match, sort, project);

        return mongoTemplate.aggregate(aggregation, "class_messages", ClassMessageResponse.class)
                .getMappedResults();
    }
}
