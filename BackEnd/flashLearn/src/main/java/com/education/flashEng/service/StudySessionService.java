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

    List<StatisticByTimeResponse> getUsersStudyStatisticByTime(int page, int size);

    List<StatisticByWordResponse> getUsersStudyStatisticByWord(int page, int size);

    List<StatisticBySpecificTimeResponse> getUsersStudyStaticBySpecificTime(LocalDate time, int page, int size);

    List<StatisticBySpecificWordResponse> getUsersStudyStaticBySpecificWord(Long wordId, int page, int size);

    LocalDateTime getReminderTimeBasedOnLevel(StudySessionEntity studySessionEntity, LocalDateTime startTime);
}
