package org.sunbird.portal.department.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.sunbird.common.model.SunbirdApiResp;
import org.sunbird.common.model.SunbirdApiRespContent;
import org.sunbird.common.model.SunbirdApiResultResponse;
import org.sunbird.common.service.OutboundRequestHandlerServiceImpl;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.core.logger.CbExtLogger;
import org.sunbird.portal.department.model.DeptPublicInfo;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PortalServiceImpl implements PortalService {

	private CbExtLogger logger = new CbExtLogger(getClass().getName());
	private ObjectMapper mapper = new ObjectMapper();

	@Autowired
	CbExtServerProperties serverConfig;

	@Autowired
	OutboundRequestHandlerServiceImpl outboundRequestHandlerService;

	@Override
	public List<String> getDeptNameList() {
		try {
			List<String> orgNames = new ArrayList<>();
			int count = 0;
			int iterateCount = 0;
			do {
				// request body
				Map<String, Object> requestMap = new HashMap<>();
				requestMap.put(Constants.OFFSET, iterateCount);
				requestMap.put(Constants.LIMIT, 100);
				requestMap.put(Constants.FIELDS,
						new ArrayList<>(Arrays.asList(Constants.CHANNEL, Constants.IS_MDO, Constants.IS_CBP)));
				Map<String, Object> deptFilterMap = new HashMap<>();
				deptFilterMap.put(Constants.IS_TENANT, Boolean.TRUE);
				deptFilterMap.put(Constants.STATUS, 1);
				requestMap.put(Constants.FILTERS,deptFilterMap);

				String serviceURL = serverConfig.getSbUrl() + serverConfig.getSbOrgSearchPath();
				Map<String, Object> requestBody = new HashMap<>();
				requestBody.put(Constants.REQUEST, requestMap);
				SunbirdApiResp orgResponse = mapper.convertValue(
						outboundRequestHandlerService.fetchResultUsingPost(serviceURL, requestBody), SunbirdApiResp.class);
				SunbirdApiResultResponse resultResp = orgResponse.getResult().getResponse();
				count = resultResp.getCount();
				iterateCount = iterateCount + resultResp.getContent().size();
				for (SunbirdApiRespContent content : resultResp.getContent()) {
					// return orgname only if cbp or mdo
					if ((!ObjectUtils.isEmpty(content.getIsMdo()) && content.getIsMdo())
							|| (!ObjectUtils.isEmpty(content.getIsCbp()) && content.getIsCbp())) {
						orgNames.add(content.getChannel());
					}
				}
			} while (count != iterateCount);
			return orgNames;
		} catch (Exception e) {
			logger.info("Exception occurred in getDeptNameList");
			logger.error(e);
		}
		return Collections.emptyList();
	}

	@Override
	public List<DeptPublicInfo> getAllDept() throws UnsupportedOperationException {
		UnsupportedOperationException ex = new UnsupportedOperationException(
				"/portal/getAllDept API is not implemented.");
		logger.error(ex);
		throw ex;
	}

	@Override
	public DeptPublicInfo searchDept(String deptName) throws UnsupportedOperationException {
		UnsupportedOperationException ex = new UnsupportedOperationException(
				"/portal/getAllDept API is not implemented.");
		logger.error(ex);
		throw ex;
	}

	@Override
	public List<Map<String,String>> getDeptNameListByAdmin() {
		try {
			List<Map<String,String>> orgDetailsList = new ArrayList<>();
			int count = 0;
			int iterateCount = 0;
			do {
				// request body
				Map<String, Object> requestMap = new HashMap<>();
				requestMap.put(Constants.OFFSET, iterateCount);
				requestMap.put(Constants.LIMIT, 100);
				requestMap.put(Constants.FIELDS,
						new ArrayList<>(Arrays.asList(Constants.CHANNEL, Constants.IS_MDO, Constants.IS_CBP, Constants.ID)));
				Map<String, Object> filtersMap = new HashMap<>();
				filtersMap.put(Constants.IS_TENANT, Boolean.TRUE);
				filtersMap.put(Constants.STATUS, 1);
				requestMap.put(Constants.FILTERS, filtersMap);
				String serviceURL = serverConfig.getSbUrl() + serverConfig.getSbOrgSearchPath();
				Map<String, Object> request = new HashMap<>();
				requestMap.put(Constants.REQUEST, request);
				SunbirdApiResp orgResponse = mapper.convertValue(
						outboundRequestHandlerService.fetchResultUsingPost(serviceURL, request), SunbirdApiResp.class);
				SunbirdApiResultResponse resultResp = orgResponse.getResult().getResponse();
				count = resultResp.getCount();
				iterateCount = iterateCount + resultResp.getContent().size();
				for (SunbirdApiRespContent content : resultResp.getContent()) {
					// return orgname only if cbp or mdo
					if ((!ObjectUtils.isEmpty(content.getIsMdo()) && content.getIsMdo())
							|| (!ObjectUtils.isEmpty(content.getIsCbp()) && content.getIsCbp())) {
								Map<String, String> orgMap = new HashMap<>();
								orgMap.put(Constants.CHANNEL, content.getChannel());
								orgMap.put(Constants.ORG_ID, content.getId());
								orgDetailsList.add(orgMap);
					}
				}
			} while (count != iterateCount);
			return orgDetailsList;
		} catch (Exception e) {
			logger.info("Exception occurred in getDeptNameList");
			logger.error(e);
		}
		return Collections.emptyList();
	}
}
