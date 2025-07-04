package com.education.flashEng.service;

import com.education.flashEng.entity.ClassMessage;
import com.education.flashEng.payload.request.CreateClassMessageRequest;
import com.education.flashEng.payload.response.ClassMessageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ClassMessageService {

    boolean sendMessageToClass(CreateClassMessageRequest request);

    Page<ClassMessageResponse> getMessagesByClassId(Long classId, Pageable pageable);

    boolean deleteMessageById(String id);

}
