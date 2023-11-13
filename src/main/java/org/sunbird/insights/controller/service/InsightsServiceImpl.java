package org.sunbird.insights.controller.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.sunbird.cache.RedisCacheMgr;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.ProjectUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static org.sunbird.common.util.Constants.*;
@Service
public class InsightsServiceImpl implements InsightsService {

    @Autowired
    CbExtServerProperties serverProperties;

    @Autowired
    private RedisCacheMgr redisCacheMgr;

    public SBApiResponse insights(Map<String, Object> requestBody) throws Exception {

        HashMap<String, Object> request = (HashMap<String, Object>) requestBody.get(REQUEST);
        HashMap<String, Object> filter = ((HashMap<String, Object>) request.get(FILTERS));
        ArrayList<String> organizations = (ArrayList<String>) (filter.get(ORGANISATIONS));

        ArrayList<Object> nudges = new ArrayList<>();
        Map<String, String> lhpLearningHours = redisCacheMgr.hgetAll(INSIGHTS_LEARNING_HOURS_REDIS_KEY, serverProperties.getRedisInsightIndex());
        Map<String, String> lhpCertifications = redisCacheMgr.hgetAll(INSIGHTS_CERTIFICATIONS_REDIS_KEY, serverProperties.getRedisInsightIndex());
        for (String org : organizations) {
            populateIfNudgeExist(lhpLearningHours, org, nudges, INSIGHTS_TYPE_LEARNING_HOURS);
            populateIfNudgeExist(lhpCertifications, org, nudges, INSIGHTS_TYPE_CERTIFICATE);
        }
        ArrayList<Object> weeklyClaps = new ArrayList<>();
        populateIfClapsExist(weeklyClaps);
        HashMap<String, Object> responseMap = new HashMap<>();
        responseMap.put(WEEKLY_CLAPS, weeklyClaps);
        responseMap.put(NUDGES, nudges);
        SBApiResponse response = ProjectUtil.createDefaultResponse(API_USER_INSIGHTS);
        response.getResult().put(RESPONSE, responseMap);
        return response;
    }

    private void populateIfNudgeExist(Map<String, String> data, String org, ArrayList<Object> nudges, String type) {
        String label = data.get(org + COLON + LABEL);
        if (StringUtils.isNotBlank(label)) {
            HashMap<String, Object> nudge = new HashMap<>();
            double yesterday = Integer.parseInt(data.get(org + COLON + YESTERDAY));
            double today = Integer.parseInt(data.get(org + COLON + TODAY));
            double change = ((today - yesterday) / yesterday) * 100;
            nudge.put(PROGRESS, change);
            nudge.put(GROWTH, change > 0 ? POSITIVE : NEGATIVE);
            nudge.put(LABEL, label);
            nudge.put(ORG, org);
            nudge.put(TYPE, type);
            nudges.add(nudge);
        }
    }

    private void populateIfClapsExist(ArrayList<Object> weeklyClaps) {
        for (int i = 0; i < NUM_WEEKS; i++) {
            HashMap<String, Object> weeklyClap = new HashMap<>();
            weeklyClap.put(WEEK, i);
            weeklyClap.put(ACHIEVED, new Random().nextInt(2));
            weeklyClaps.add(weeklyClap);
        }
    }
}
