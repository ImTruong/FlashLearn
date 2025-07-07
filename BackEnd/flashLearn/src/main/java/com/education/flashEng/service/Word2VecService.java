package com.education.flashEng.service;

import java.util.List;

public interface Word2VecService {

    void loadModel();
    List<String> findSimilarWords(List<String> words, int topN);
    boolean hasWord(String word);

}
