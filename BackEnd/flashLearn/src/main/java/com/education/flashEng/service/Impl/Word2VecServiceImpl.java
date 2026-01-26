package com.education.flashEng.service.Impl;


import com.education.flashEng.service.Word2VecService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.deeplearning4j.models.embeddings.loader.WordVectorSerializer;
import org.deeplearning4j.models.embeddings.wordvectors.WordVectors;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Service
@Slf4j
public class Word2VecServiceImpl implements Word2VecService {

    private WordVectors wordVectors;

    @Value("${word2vec.model.path:classpath:GoogleNews-vectors-negative300.bin}")
    private String modelPath;

    private final ResourceLoader resourceLoader;

    public Word2VecServiceImpl(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void loadModel() {
        try {
            log.info("Bắt đầu tải Word2Vec model từ: {}", modelPath);
            
            File modelFile;
            if (modelPath.startsWith("classpath:")) {
                // Load from classpath
                Resource resource = resourceLoader.getResource(modelPath);
                if (!resource.exists()) {
                    throw new FileNotFoundException("Cannot find Word2Vec model file at: " + modelPath);
                }
                modelFile = resource.getFile();
            } else {
                // Load from file system
                modelFile = new File(modelPath);
                if (!modelFile.exists()) {
                    throw new FileNotFoundException("Cannot find Word2Vec model file at: " + modelPath);
                }
            }

            // Load binary Word2Vec model
            this.wordVectors = WordVectorSerializer.readWord2VecModel(modelFile);
            log.info("Word2Vec model has been loaded successfully from: {}", modelPath);

        } catch (Exception e) {
            log.error("Error loading Word2Vec model from path: {}", modelPath, e);
            throw new RuntimeException("Could not load Word2Vec model from path: " + modelPath, e);
        }
    }

    public List<String> findSimilarWords(List<String> words, int topN) {
        try {
            if (wordVectors == null) {
                throw new RuntimeException("Word2Vec model has not been loaded yet.");
            }

            List<INDArray> validVectors = new ArrayList<>();

            for (String word : words) {
                String normalizedWord = normalizeWord(word);
                if (wordVectors.hasWord(normalizedWord)) {
                    INDArray vec = wordVectors.getWordVectorMatrix(normalizedWord);
                    validVectors.add(vec);
                } else {
                    log.warn("The word '{}' does not exist in the Word2Vec model.", normalizedWord);
                }
            }

            if (validVectors.isEmpty()) {
                log.warn("No valid words found in the list.");
                return new ArrayList<>();
            }

            // Tính trung bình cộng vector
            INDArray meanVector = validVectors.get(0).dup();
            for (int i = 1; i < validVectors.size(); i++) {
                meanVector.addi(validVectors.get(i));
            }
            meanVector.divi(validVectors.size());

            // Lấy từ gần nhất với vector trung bình
            Collection<String> similarWords = wordVectors.wordsNearest(meanVector, topN);
            return new ArrayList<>(similarWords);

        } catch (Exception e) {
            log.error("Error finding similar words for '{}': {}", words, e.getMessage());
            return new ArrayList<>();
        }
    }

    private String normalizeWord(String word) {
        return word.toLowerCase().replaceAll("[^a-zA-Z]", "");
    }

    public boolean hasWord(String word) {
        if (wordVectors == null) return false;
        return wordVectors.hasWord(normalizeWord(word));
    }

}
