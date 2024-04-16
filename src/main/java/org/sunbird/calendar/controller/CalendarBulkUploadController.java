package org.sunbird.calendar.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.sunbird.calendar.service.CalendarBulkUploadService;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

@RestController
@RequestMapping("/calendar")
public class CalendarBulkUploadController {

    @Autowired
    private CalendarBulkUploadService calendarBulkUploadService;

    private Logger log = LoggerFactory.getLogger(getClass().getName());

    @PostMapping("/v1/bulkupload")
    public ResponseEntity<?> bulkUploadCalendarEvent(@RequestParam(value = "file", required = true) MultipartFile multipartFile,
                                                     @RequestHeader(Constants.X_AUTH_USER_ORG_ID) String rootOrgId,
                                                     @RequestHeader(Constants.X_AUTH_USER_CHANNEL) String channel,
                                                     @RequestHeader(Constants.X_AUTH_TOKEN) String userAuthToken) throws UnsupportedEncodingException {
        log.info(String.format("bulkupload channel name:%s,OrgId:%s",
                URLDecoder.decode(channel, "UTF-8"), rootOrgId));
        SBApiResponse uploadResponse = calendarBulkUploadService.bulkUploadCalendarEvent(multipartFile, rootOrgId, userAuthToken, URLDecoder.decode(channel, "UTF-8"));
        return new ResponseEntity<>(uploadResponse, uploadResponse.getResponseCode());
    }
}
