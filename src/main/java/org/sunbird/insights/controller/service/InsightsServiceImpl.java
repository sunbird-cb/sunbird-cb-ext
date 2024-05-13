package org.sunbird.insights.controller.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.sunbird.cache.RedisCacheMgr;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.common.util.ProjectUtil;
import java.math.BigDecimal;
import java.math.RoundingMode;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

import static org.sunbird.common.util.Constants.*;
@Service
public class InsightsServiceImpl implements InsightsService {

    private static final Logger log = LoggerFactory.getLogger(InsightsServiceImpl.class);
    @Autowired
    CbExtServerProperties serverProperties;

    @Autowired
    private RedisCacheMgr redisCacheMgr;

    @Autowired
    CassandraOperation cassandraOperation;

    @Autowired
    CbExtServerProperties extServerProperties;

    public SBApiResponse insights(Map<String, Object> requestBody,String userId) throws Exception {
        String [] labelsCertificates = {extServerProperties.getInsightsLabelCertificatesAcross(),extServerProperties.getInsightsLabelCertificatesYourDepartment()} ;
        String [] labelsLearningHours = {extServerProperties.getInsightsLabelLearningHoursYourDepartment(),extServerProperties.getInsightsLabelLearningHoursAcross()} ;
        HashMap<String, Object> request = (HashMap<String, Object>) requestBody.get(REQUEST) == null ? new HashMap<>() : (HashMap<String, Object>) requestBody.get(REQUEST);
        HashMap<String, Object> filter = ((HashMap<String, Object>) request.get(FILTERS)) == null ? new HashMap<>() : ((HashMap<String, Object>) request.get(FILTERS));
        ArrayList<String> organizations = (ArrayList<String>) (filter.get(ORGANISATIONS)) ==null ? new ArrayList<>() : (ArrayList<String>) (filter.get(ORGANISATIONS));
        ArrayList<String> keys = nudgeKeys(organizations);
        String[] fieldsArray = keys.toArray(new String[keys.size()]);
        ArrayList<String> certificateOrgs= new ArrayList<>();
        certificateOrgs.add("across");
        ArrayList<String> certificate_keys = nudgeKeys(certificateOrgs);
        String[] fieldsArray_certificates = certificate_keys.toArray(new String[certificate_keys.size()]);
        ArrayList<Object> nudges = new ArrayList<>();
        List<String> lhpLearningHours =  redisCacheMgr.hget(INSIGHTS_LEARNING_HOURS_REDIS_KEY, serverProperties.getRedisInsightIndex(),fieldsArray);
        List<String> lhpCertifications = redisCacheMgr.hget(INSIGHTS_CERTIFICATIONS_REDIS_KEY, serverProperties.getRedisInsightIndex(),fieldsArray_certificates);
        if(lhpLearningHours == null)
            lhpLearningHours = new ArrayList<>();
        if(lhpCertifications ==null)
            lhpCertifications = new ArrayList<>();
        populateIfNudgeExist(lhpLearningHours, nudges, INSIGHTS_TYPE_LEARNING_HOURS,organizations,labelsLearningHours);
        populateIfNudgeExist(lhpCertifications, nudges, INSIGHTS_TYPE_CERTIFICATE,Arrays.asList(fieldsArray_certificates),labelsCertificates);
        HashMap<String, Object> responseMap = new HashMap<>();
        responseMap.put(WEEKLY_CLAPS, populateIfClapsExist(userId) );
        responseMap.put(NUDGES, nudges);
        SBApiResponse response = ProjectUtil.createDefaultResponse(API_USER_INSIGHTS);
        response.getResult().put(RESPONSE, responseMap);
        return response;
    }
    private ArrayList<String> nudgeKeys(ArrayList<String> organizations ){
        ArrayList<String> keys = new ArrayList<>();
        for (String org : organizations) {
           // keys.add(org + COLON + LABEL);
            keys.add(org + COLON + YESTERDAY);
            keys.add(org + COLON + TODAY);
        }
        return  keys;
    }
    private Map<String, Object> populateIfClapsExist(String userId) {
        Map<String, Object> userRequest = new HashMap<>();
        userRequest.put(LEARNER_STATUS_USER_ID, userId);
        List<String> fields = new ArrayList<>();
        fields.add(LEARNER_STATUS_USER_ID);
        fields.add(TOTAL_CLAPS);
        fields.add(W1);
        fields.add(W2);
        fields.add(W3);
        fields.add(W4);
        List<Map<String, Object>>  result=  cassandraOperation.getRecordsByProperties(KEYSPACE_SUNBIRD,
                LEARNER_STATS, userRequest, fields);
        LocalDate[]  dates = populateDate();
        if (result ==null || result.size() < 1) {
            result = new ArrayList<>();
            HashMap m = new HashMap();
            result.add(m);
        }

            result.get(0).put("startDate", dates[0]);
            result.get(0).put("endDate", dates[1]);
            return result.get(0);


    }
    public void populateIfNudgeExist(List<String> data, ArrayList<Object> nudges, String type, List<String> organizations,String labels[]) {
        for (int i = 0, j = 0; i < data.size(); i += 2, j++) {
           // String label = data.get(i);
            double yesterday = StringUtils.isNotBlank(data.get(i)) ? Double.parseDouble(data.get(i)) : 0.0;
            double today = StringUtils.isNotBlank(data.get(i+1)) ? Double.parseDouble(data.get(i+1)) : 0.0;
            double change;
            if (yesterday != 0.0 || today != 0.0) {
                if (yesterday != 0.0) {
                    change = ((today - yesterday) / Math.abs(yesterday)) * 100;
                } else {
                    change = 100.0;
                }
            } else {
                change = 0.0;
            }
            HashMap<String, Object> nudge = new HashMap<>();
            nudge.put(PROGRESS, roundToTwoDecimals(change));
            nudge.put(GROWTH, change > 0 ? POSITIVE : NEGATIVE);

            if(organizations.size() > j) {
                String replacer = "";
                if(type.equals(INSIGHTS_TYPE_CERTIFICATE))
                    replacer = String.valueOf ((int)Math.round(today));
                else
                    replacer =  String.valueOf (roundToTwoDecimals(today));

                nudge.put(LABEL, organizations.get(j).equals("across") ? labels[1].replace("{0}",replacer) : labels[0].replace("{0}", replacer));
                nudge.put(ORG, organizations.get(j));
            }
            nudge.put(TYPE, type);
            nudges.add(nudge);
        }
    }

    public LocalDate[] populateDate(){
        LocalDate currentDate = LocalDate.now();
        LocalDate endDate = currentDate.with(TemporalAdjusters.next(DayOfWeek.SUNDAY));
        LocalDate startDate = endDate.minusWeeks(4).plusDays(1);
        LocalDate[] local = new LocalDate[2];
        local[0] = startDate;
        local[1] = endDate;
        return local;
    }
    public static double roundToTwoDecimals(double value) {
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public SBApiResponse readInsightsForOrganisation(Map<String, Object> requestBody, String userId) {
        SBApiResponse response = ProjectUtil.createDefaultResponse(Constants.API_MICRO_SITE_INSIGHTS);
        try {
            Map<String, Object> request = (Map<String, Object>) requestBody.get(REQUEST);
            if (MapUtils.isEmpty(request)) {
                response.getParams().setStatus(Constants.FAILED);
                response.put(MESSAGE, "Request is Missing");
                response.setResponseCode(HttpStatus.BAD_REQUEST);
                return response;
            }
            Map<String, Object> filter = ((Map<String, Object>) request.get(FILTERS));
            if (MapUtils.isEmpty(filter)) {
                response.getParams().setStatus(Constants.FAILED);
                response.put(MESSAGE, "Filter is Missing");
                response.setResponseCode(HttpStatus.BAD_REQUEST);
                return response;
            }
            List<String> organizations = (ArrayList<String>) (filter.get(ORGANISATIONS));
            if (CollectionUtils.isEmpty(organizations)) {
                response.getParams().setStatus(Constants.FAILED);
                response.put(MESSAGE, "Organization is Required");
                response.setResponseCode(HttpStatus.BAD_REQUEST);
                return response;
            }
            Map<String, Object> responseMap = new HashMap<>();
            Map<String, String> organisationInsideFields = serverProperties.getOrganisationInsightFields();
            Map<String, String> redisKeyForInsight = serverProperties.getOrganisationInsightRedisKeyMapping();
            List<Map<String, Object>> organisationDataMapList = new ArrayList<>();
            for (String organisationId: organizations) {
                Map<String, Object> organisationMap = new HashMap<>();
                List<Map<String, Object>> nudgesDataList = new ArrayList<>();
                for (Map.Entry<String,String> insightFields: organisationInsideFields.entrySet()) {
                    Map<String, Object> nudgesData = new HashMap<>();
                    nudgesData.put(Constants.ICON, insightFields.getValue());
                    populateNudgeForMicroSite(insightFields.getKey(), organisationId, serverProperties.getOrganisationInsightPropertyFields(),
                            redisKeyForInsight.get(insightFields.getKey()), nudgesData);
                    nudgesDataList.add(nudgesData);
                }
                organisationMap.put(Constants.ORG_ID, organisationId);
                organisationMap.put(Constants.DATA, nudgesDataList);
                organisationDataMapList.add(organisationMap);
            }
            responseMap.put(NUDGES, organisationDataMapList);
            response.getResult().put(RESPONSE, responseMap);
        } catch (Exception e) {
            log.error("Failed to get Insight Info for OrgId", e);
            response.getParams().setStatus(Constants.FAILED);
            response.getParams().setErrmsg(e.getMessage());
            response.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    public void populateNudgeForMicroSite(String label, String organisationId, Map<String, String> additionalCssFields, String redisKey, Map<String, Object> nudgesData) {
       List<String> redisData = redisCacheMgr.hget(redisKey, serverProperties.getRedisInsightIndex(), organisationId);
       if (CollectionUtils.isNotEmpty(redisData)) {
           nudgesData.put(Constants.LABEL, label);
           nudgesData.put(Constants.VALUE, redisData.get(0));
           nudgesData.putAll(additionalCssFields);
       } else {
           log.error("Not able to fetch Data from redis key for key: {} for organisation: {}", redisKey, organisationId);
       }
    }
}

