package com.education.flashEng.service.Impl;

import com.education.flashEng.entity.ClassMemberEntity;
import com.education.flashEng.entity.ClassMessage;
import com.education.flashEng.entity.UserEntity;
import com.education.flashEng.payload.request.CreateClassMessageRequest;
import com.education.flashEng.payload.response.ClassMessageResponse;
import com.education.flashEng.repository.ClassMessageRepository;
import com.education.flashEng.service.ClassMemberService;
import com.education.flashEng.service.ClassMessageService;
import com.education.flashEng.service.UserService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ClassMessageServiceImpl implements ClassMessageService {

    @Autowired
    private ClassMessageRepository classMessageRepository;

    @Autowired
    private ClassMemberService classMemberService;

    @Autowired
    private UserService userService;

    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    @Override
    public boolean sendMessageToClass(CreateClassMessageRequest request) {
        UserEntity currentUser = userService.getUserFromSecurityContext();
        Long classId = request.getClassId();
        ClassMemberEntity classMember = classMemberService.getClassMemberByClassIdAndUserId(classId, currentUser.getId());
        if(request.getReplyToMessageId()!=null)
            classMessageRepository.findByClassIdAndId(classId, request.getReplyToMessageId())
                .orElseThrow(() -> new IllegalArgumentException("Reply message not found"));
        ClassMessage classMessage = ClassMessage.builder()
                .classId(classId)
                .senderId(currentUser.getId())
                .senderName(currentUser.getFullName())
                .senderRole(classMember.getRoleClassEntity().getName())
                .message(request.getMessage())
                .replyToMessageId(request.getReplyToMessageId())
                .createdAt(LocalDateTime.now())
                .build();
        classMessageRepository.save(classMessage);
        return true;
    }

    @Override
    public Page<ClassMessageResponse> getMessagesByClassId(Long classId, Pageable pageable) {
        UserEntity currentUser = userService.getUserFromSecurityContext();
        classMemberService.getClassMemberByClassIdAndUserId(classId, currentUser.getId());
        List<ClassMessageResponse> messages = classMessageRepository.findMessagesByClassId(classId);
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), messages.size());
        List<ClassMessageResponse> paginatedMessages = messages.subList(start, end);
        return new PageImpl<>(paginatedMessages, pageable, messages.size());
    }

    @Transactional
    @Override
    public boolean deleteMessageById(String id) {
        UserEntity currentUser = userService.getUserFromSecurityContext();
        ClassMessage classMessage = classMessageRepository.findByIdAndSenderId(id, currentUser.getId())
                .orElseThrow(() -> new IllegalArgumentException("You do not have permission to delete this message"));
        classMessage.setDeleted(true);
        classMessageRepository.save(classMessage);
        return true;
    }
}
