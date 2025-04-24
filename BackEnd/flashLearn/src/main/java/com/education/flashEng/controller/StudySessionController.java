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
    public ResponseEntity<?> getStudyStatisticsByTime(@RequestParam (required = true) int page,
                                                      @RequestParam (required = true) int size) {
        ApiResponse<?> response = new ApiResponse<>(true,"Get Statistic Successfully",studySessionService.getUsersStudyStatisticByTime(page,size));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/word")
    public ResponseEntity<?> getStudyStatisticsByWord(@RequestParam (required = true) int page,
                                                      @RequestParam (required = true) int size) {
        ApiResponse<?> response = new ApiResponse<>(true,"Get Statistic Successfully",studySessionService.getUsersStudyStatisticByWord(page,size));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/time/{time}")
    public ResponseEntity<?> getStudyStatisticsBySpecificTime(@PathVariable LocalDate time,
                                                              @RequestParam (required = true) int page,
                                                              @RequestParam (required = true) int size) {
        ApiResponse<?> response = new ApiResponse<>(true,"Get Statistic Successfully",studySessionService.getUsersStudyStaticBySpecificTime(time,page,size));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/word/{wordId}")
    public ResponseEntity<?> getStudyStatisticsBySpecificWord(@PathVariable Long wordId,
                                                              @RequestParam (required = true) int page,
                                                              @RequestParam (required = true) int size) {
        ApiResponse<?> response = new ApiResponse<>(true,"Get Statistic Successfully",studySessionService.getUsersStudyStaticBySpecificWord(wordId,page,size));
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
