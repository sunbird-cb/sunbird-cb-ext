package org.sunbird.calendar.service;

import org.springframework.web.multipart.MultipartFile;
import org.sunbird.common.model.SBApiResponse;

public interface CalendarBulkUploadService {
    SBApiResponse bulkUploadCalendarEvent(MultipartFile multipartFile, String rootOrgId, String userAuthToken, String channelName);

    void initiateCalendarEventBulkUploadProcess(String value);
}
