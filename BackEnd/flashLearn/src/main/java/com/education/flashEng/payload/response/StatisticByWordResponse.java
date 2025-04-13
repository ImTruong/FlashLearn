package com.education.flashEng.payload.response;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StatisticByWordResponse {
    private Long wordId;
    private Long count;
    private String word;
}
