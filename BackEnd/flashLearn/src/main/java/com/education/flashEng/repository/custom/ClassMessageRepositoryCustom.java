package com.education.flashEng.repository.custom;

import com.education.flashEng.payload.response.ClassMessageResponse;

import java.util.List;

public interface ClassMessageRepositoryCustom {

    List<ClassMessageResponse> findMessagesByClassId(Long classId);

}
