package com.education.flashEng.payload.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatisticBySpecificTimeResponse {
    private LocalDate date;
    private LocalTime time;
    private Long wordId;
    private String word;
    private String choosenAnswer;

    public StatisticBySpecificTimeResponse(LocalDateTime dateTime, Long wordId, String word, String choosenAnswer) {
        this.date = dateTime.toLocalDate();
        this.time = dateTime.toLocalTime();
        this.wordId = wordId;
        this.word = word;
        this.choosenAnswer = choosenAnswer;
    }
}
