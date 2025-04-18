package com.education.flashEng.payload.response;

import lombok.*;

import java.sql.Date;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatisticByTimeResponse {
    private LocalDate date;
    private long numberOfWords;

    public StatisticByTimeResponse(Date date, Long numberOfWords) {
        this.date = date.toLocalDate();  // Chuyển từ Date sang LocalDate
        this.numberOfWords = numberOfWords;
    }
}
