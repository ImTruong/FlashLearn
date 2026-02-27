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
import java.io.FileOutputStream;
import java.io.InputStream;
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
            log.info("Đang khởi tạo Word2Vec với đường dẫn: {}", modelPath);
            File modelFile;

            if (modelPath.startsWith("classpath:")) {
                // TRƯỜNG HỢP: CHẠY LOCAL TRÊN IDE (File nằm trong resources)
                Resource resource = resourceLoader.getResource(modelPath);
                
                // Tạo file tạm vì DL4J bắt buộc cần đối tượng File (không đọc trực tiếp từ Stream được)
                log.info("Đang giải nén model từ classpath ra file tạm...");
                modelFile = File.createTempFile("word2vec_model_tmp", ".bin");
                modelFile.deleteOnExit();

                try (InputStream is = resource.getInputStream();
                    FileOutputStream os = new FileOutputStream(modelFile)) {
                    if (is == null) throw new FileNotFoundException("Không tìm thấy model trong classpath!");
                    byte[] buffer = new byte[1024 * 8];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                }
            } else {
                // TRƯỜNG HỢP: CHẠY TRÊN DOCKER (Đọc từ Volume mount ngoài)
                modelFile = new File(modelPath);
            }

            if (!modelFile.exists() || modelFile.length() == 0) {
                throw new FileNotFoundException("File model không tồn tại hoặc rỗng tại: " + modelFile.getAbsolutePath());
            }

            log.info("Bắt đầu đọc dữ liệu model từ file: {} (Dung lượng: {} bytes)", 
                    modelFile.getAbsolutePath(), modelFile.length());
            
            // Dùng hàm chuẩn của DL4J
            this.wordVectors = WordVectorSerializer.readWord2VecModel(modelFile);
            log.info("Word2Vec model đã được tải thành công!");

        } catch (Exception e) {
            log.error("LỖI TẢI MODEL: {}", e.getMessage());
            // Không quăng Exception ở đây để tránh làm sập cả Application nếu model lỗi
            // Nhưng bạn nên kiểm tra kĩ log
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
