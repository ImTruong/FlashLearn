package com.education.flashEng.controller;

import com.education.flashEng.payload.request.StudySessionRequest;
import com.education.flashEng.payload.response.ApiResponse;
import com.education.flashEng.service.StudySessionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/study")
public class StudySessionController {

    @Autowired
    private StudySessionService studySessionService;

    @PostMapping
    public ResponseEntity<?> createStudySession(@Valid @RequestBody StudySessionRequest studySessionRequest  ) {
        ApiResponse<String> response = new ApiResponse<>(studySessionService.createStudySession(studySessionRequest),"Study Session Created Successfully");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/time")
    public ResponseEntity<?> getStudyStatisticsByTime() {
        ApiResponse<?> response = new ApiResponse<>(true,"Get Statistic Successfully",studySessionService.getUsersStudyStatisticByTime());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/word")
    public ResponseEntity<?> getStudyStatisticsByWord() {
        ApiResponse<?> response = new ApiResponse<>(true,"Get Statistic Successfully",studySessionService.getUsersStudyStatisticByWord());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/time/{time}")
    public ResponseEntity<?> getStudyStatisticsBySpecificTime(@PathVariable LocalDate time) {
        ApiResponse<?> response = new ApiResponse<>(true,"Get Statistic Successfully",studySessionService.getUsersStudyStaticBySpecificTime(time));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/word/{wordId}")
    public ResponseEntity<?> getStudyStatisticsBySpecificWord(@PathVariable Long wordId) {
        ApiResponse<?> response = new ApiResponse<>(true,"Get Statistic Successfully",studySessionService.getUsersStudyStaticBySpecificWord(wordId));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
