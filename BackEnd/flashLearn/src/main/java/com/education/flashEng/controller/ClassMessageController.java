package com.education.flashEng.controller;

import com.education.flashEng.payload.request.CreateClassMessageRequest;
import com.education.flashEng.payload.response.ApiResponse;
import com.education.flashEng.payload.response.ClassMessageResponse;
import com.education.flashEng.service.ClassMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/class/message")
public class ClassMessageController {

    @Autowired
    private ClassMessageService classMessageService;

    @GetMapping("/{classId}")
    public ResponseEntity<?> getAllClassMessage(@PathVariable Long classId,
                                                Pageable pageable) {
        ApiResponse<?> response = new ApiResponse<>(true, "Messages Fetched Successfully",
                classMessageService.getMessagesByClassId(classId, pageable));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<?> sendMessageToClass(@RequestBody CreateClassMessageRequest request) {
        ApiResponse<ClassMessageResponse> response = new ApiResponse<>(
                classMessageService.sendMessageToClass(request), "Message Sent Successfully");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<?> deleteMessage(@PathVariable String messageId) {
        ApiResponse<String> response = new ApiResponse<>(classMessageService.deleteMessageById(messageId), "Message Deleted Successfully");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }


}
