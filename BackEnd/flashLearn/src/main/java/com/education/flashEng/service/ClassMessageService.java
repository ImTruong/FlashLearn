package com.education.flashEng.service;

import com.education.flashEng.payload.request.CreateClassMessageRequest;
import com.education.flashEng.payload.response.ClassMessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClassMessageService {

    boolean sendMessageToClass(CreateClassMessageRequest request);

    Page<ClassMessageResponse> getMessagesByClassId(Long classId, Pageable pageable);

    boolean deleteMessageById(String id);

    boolean deleteAllMessagesByClassId(Long classId);

}
