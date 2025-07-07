package com.education.flashEng.service.Impl;

import com.education.flashEng.entity.UserEntity;
import com.education.flashEng.service.StudySessionService;
import com.education.flashEng.service.UserService;
import com.education.flashEng.service.VocabularySuggestionService;
import com.education.flashEng.service.Word2VecService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VocabularySuggestionServiceImpl implements VocabularySuggestionService {

    @Autowired
    private Word2VecService word2VecService;

    @Autowired
    private StudySessionService studySessionService;

    @Autowired
    private UserService userService;

    private final static int MAX_SUGGESTIONS = 10;
    private final static int CLOSEST_WORDS = 10;

    public List<String> generateSuggestions(){
        UserEntity currentUser = userService.getUserFromSecurityContext();
        List<String> studyWords = studySessionService.getNClosestDistinctStudyWords(
                currentUser.getId(), CLOSEST_WORDS);
        if (studyWords.isEmpty())
            return word2VecService.findSimilarWords(List.of("hello"), MAX_SUGGESTIONS);
        else
            return word2VecService.findSimilarWords(studyWords, MAX_SUGGESTIONS);

    }

}
