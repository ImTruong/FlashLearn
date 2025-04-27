package com.education.flashEng.service;

import com.education.flashEng.entity.SetEntity;
import com.education.flashEng.payload.request.CreateSetRequest;
import com.education.flashEng.payload.request.UpdateSetRequest;
import com.education.flashEng.payload.response.SetResponse;

import java.util.List;

public interface SetService {
    SetResponse createSet(CreateSetRequest createSetRequest);
    List<SetResponse> getOwnPublicAndPrivateSet(int page,int size);
    List<SetResponse> getPrivateSet();
    List<SetResponse> getSetByClassID(Long classID,int page, int size);
    boolean updateSet(UpdateSetRequest updateSetRequest);
    boolean deleteSetById(Long setID);
    List<SetResponse> getRecentSet(int page,int size);
    List<SetResponse> findSetByName(Long classId, String name,int page, int size);

    List<SetResponse> getPublicSet(int page,int size);
    boolean updateSetPrivacyToPrivateByEntity(SetEntity setEntity);
}
