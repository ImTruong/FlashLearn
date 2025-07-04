package com.education.flashEng.repository;

import com.education.flashEng.entity.ClassMessage;
import com.education.flashEng.payload.response.ClassMessageResponse;
import com.education.flashEng.repository.custom.ClassMessageRepositoryCustom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClassMessageRepository extends MongoRepository<ClassMessage, String>, ClassMessageRepositoryCustom {

    Optional<ClassMessage> findByClassIdAndId(Long classId, String id);

    Optional<ClassMessage> findByIdAndSenderId(String id, Long senderId);

}
