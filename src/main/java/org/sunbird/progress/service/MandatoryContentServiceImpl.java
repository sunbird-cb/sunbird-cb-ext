package org.sunbird.progress.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.OutboundReqService;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.progress.cassandraRepo.MandatoryContentModel;
import org.sunbird.progress.cassandraRepo.MandatoryContentRepository;
import org.sunbird.progress.model.MandatoryContentInfo;
import org.sunbird.progress.model.MandatoryContentResponse;

import java.math.BigDecimal;
import java.util.*;

@Service
public class MandatoryContentServiceImpl implements MandatoryContentService {

    @Autowired
    private MandatoryContentRepository mandatoryContentRepository;

    @Autowired
    private OutboundReqService outboundReqService;

    @Autowired
    private CbExtServerProperties cbExtServerProperties;



    private CbExtLogger logger = new CbExtLogger(getClass().getName());


    @Override
    public MandatoryContentResponse getMandatoryContentStatusForUser(String rootOrg, String org, String userId) {
        MandatoryContentResponse response = new MandatoryContentResponse();
        List<MandatoryContentModel> contentList = mandatoryContentRepository.getMandatoryContentsInfo(rootOrg, org);
        if (CollectionUtils.isEmpty(contentList)) {
            logger.info("getMandatoryContentStatusForUser: There are no mandatory Content set in DB.");
            return response;
        }
        for (MandatoryContentModel content : contentList) {
            MandatoryContentInfo info = new MandatoryContentInfo();
            info.setRootOrg(rootOrg);
            info.setOrg(org);
            info.setContentType(content.getContentType());
            info.setMinProgressForCompletion(content.getMinProgressCheck());
            info.setBatchId(content.getBatchId());
            response.addContentInfo(content.getPrimaryKey().getContent_id(), info);
        }
        try {
            logger.info("getMandatoryContentStatusForUser: MandatoryCourse Details : " + new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(response));
        } catch (JsonProcessingException e) {
            logger.error(e);
        }
        enrichProgressDetails(response, userId);
        try {
            logger.info("getMandatoryContentStatusForUser: Ret Value is: " + new ObjectMapper().writer().withDefaultPrettyPrinter().writeValueAsString(response));
        } catch (JsonProcessingException e) {
            logger.error(e);
        }
        Iterator<MandatoryContentInfo> entries = response.getContentDetails().values().iterator();
        boolean isCompleted = false;
        while (entries.hasNext()) {
            MandatoryContentInfo entry = entries.next();
            if (entry.getUserProgress() < entry.getMinProgressForCompletion()) {
                response.setMandatoryCourseCompleted(false);
                isCompleted = false;
                break;
            } else {
                isCompleted = true;
            }
        }
        if (isCompleted) {
            response.setMandatoryCourseCompleted(true);
        }
        return response;
    }

    public void enrichProgressDetails(MandatoryContentResponse mandatoryContentInfo, String userId) {
        HashMap<String, Object> req;
        HashMap<String, Object> reqObj;
        List<String> fields = Arrays.asList("progressdetails");
        for (Map.Entry<String, MandatoryContentInfo> infoMap : mandatoryContentInfo.getContentDetails().entrySet()) {
            try {
                req = new HashMap<>();
                reqObj = new HashMap<>();
                reqObj.put("userId", userId);
                reqObj.put("courseId", infoMap.getKey());
                reqObj.put("batchId", infoMap.getValue().getBatchId());
                reqObj.put("fields", fields);
                req.put("request", reqObj);
                Map response = outboundReqService.fetchResultUsingPost(cbExtServerProperties.getCourseServiceHost()+cbExtServerProperties.getProgressReadEndPoint(), req);
                if (response.get("responseCode").equals("OK")) {
                    List<Object> result = (List<Object>) ((HashMap<String, Object>) response.get("result")).get("contentList");
                    if (!CollectionUtils.isEmpty(result)) {
                        Map<String, Object> content = (Map<String, Object>) result.stream().findFirst().get();
                        BigDecimal progress = new BigDecimal(content.get("completionPercentage").toString());
                        mandatoryContentInfo.getContentDetails().get(infoMap.getKey()).setUserProgress(progress.floatValue());
                    }
                }
            } catch (Exception ex) {
                logger.error(ex);
            }
        }
    }
}
