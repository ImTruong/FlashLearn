package com.education.flashEng.repository;

import com.education.flashEng.entity.ClassMessageEntity;
import com.education.flashEng.repository.custom.ClassMessageRepositoryCustom;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClassMessageRepository extends MongoRepository<ClassMessageEntity, String>, ClassMessageRepositoryCustom {

    Optional<ClassMessageEntity> findByClassIdAndId(Long classId, String id);

    Optional<ClassMessageEntity> findByIdAndSenderId(String id, Long senderId);

    Long deleteAllByClassId(Long classId);

}
