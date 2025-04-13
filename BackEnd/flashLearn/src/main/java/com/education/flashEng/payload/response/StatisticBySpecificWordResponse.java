package com.education.flashEng.payload.response;

import lombok.*;

import java.sql.Date;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatisticBySpecificWordResponse {
    Long wordId;
    String word;
    String choosenAnswer;
    LocalDate date;

    public StatisticBySpecificWordResponse(Long wordId, String word, String choosenAnswer, Date date) {
        this.wordId = wordId;
        this.word = word;
        this.choosenAnswer = choosenAnswer;
        this.date = date.toLocalDate();
    }
}
