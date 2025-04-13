package com.education.flashEng.service;

import com.education.flashEng.entity.StudySessionEntity;
import com.education.flashEng.payload.request.StudySessionRequest;
import com.education.flashEng.payload.response.StatisticBySpecificTimeResponse;
import com.education.flashEng.payload.response.StatisticBySpecificWordResponse;
import com.education.flashEng.payload.response.StatisticByTimeResponse;
import com.education.flashEng.payload.response.StatisticByWordResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public interface StudySessionService {
    boolean createStudySession(StudySessionRequest studySessionRequest);

    List<StatisticByTimeResponse> getUsersStudyStatisticByTime();

    List<StatisticByWordResponse> getUsersStudyStatisticByWord();

    List<StatisticBySpecificTimeResponse> getUsersStudyStaticBySpecificTime(LocalDate time);

    List<StatisticBySpecificWordResponse> getUsersStudyStaticBySpecificWord(Long wordId);

    LocalDateTime getReminderTimeBasedOnLevel(StudySessionEntity studySessionEntity, LocalDateTime startTime);
}
