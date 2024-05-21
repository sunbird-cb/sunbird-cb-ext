package org.sunbird.calendar.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.sunbird.calendar.service.CalendarBulkUploadService;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;

@RestController
@RequestMapping("/calendar")
public class CalendarBulkUploadController {

    @Autowired
    private CalendarBulkUploadService calendarBulkUploadService;

    private Logger log = LoggerFactory.getLogger(getClass().getName());

    @PostMapping("/v1/bulkUpload")
    public ResponseEntity<?> bulkUploadCalendarEvent(@RequestParam(value = "file", required = true) MultipartFile multipartFile,
                                                     @RequestHeader(Constants.X_AUTH_USER_ORG_ID) String rootOrgId,
                                                     @RequestHeader(Constants.X_AUTH_USER_CHANNEL) String channel,
                                                     @RequestHeader(Constants.X_AUTH_TOKEN) String userAuthToken) {
        try {
            log.info(String.format("bulkupload channel name:%s,OrgId:%s",
                    ProjectUtil.decodeUrl(channel), rootOrgId));
            SBApiResponse uploadResponse = calendarBulkUploadService.bulkUploadCalendarEvent(multipartFile, rootOrgId, userAuthToken, ProjectUtil.decodeUrl(channel));
            return new ResponseEntity<>(uploadResponse, uploadResponse.getResponseCode());
        } catch (Exception e) {
            log.error("Issue in calendar bulk Upload",e);
            SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_CALENDAR_EVENT_BULK_UPLOAD);
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg("Issue in bulk Upload when decoding the channel name.");
            response.setResponseCode(HttpStatus.BAD_REQUEST);
            return new ResponseEntity<>(response, response.getResponseCode());
        }
    }
}
