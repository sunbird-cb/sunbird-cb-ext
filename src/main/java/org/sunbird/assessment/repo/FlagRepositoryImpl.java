package org.sunbird.assessment.repo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;
import org.sunbird.core.logger.CbExtLogger;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FlagRepositoryImpl implements FlagRepository {

	public static final String RESULT = "result";
	private CbExtLogger logger = new CbExtLogger(getClass().getName());

	@Autowired
	CassandraOperation cassandraOperation;

	@Override
	public boolean addFlagDataToDB(String userId, Map<String, Object> request) {
		Map<String, Object> requestData = new HashMap<>();
		requestData.put(Constants.USER_ID, userId);
		requestData.put(Constants.CONTEXT_TYPE, request.get(Constants.CONTEXT_TYPE));
		requestData.put(Constants.CONTEXT_TYPE_ID, request.get(Constants.CONTEXT_TYPE_ID));
		requestData.put(Constants.ACQUIRED_CHANNEL, request.get(Constants.ACQUIRED_CHANNEL));
		requestData.put(Constants.ADDITIONAL_PARAMS, request.get(Constants.ADDITIONAL_PARAMS));
		requestData.put(Constants.CONTEXT_STATUS, Constants.DRAFT);
		requestData.put(Constants.CREATED_BY, userId);
		requestData.put(Constants.CREATED_AT, new Timestamp(new java.util.Date().getTime()));
		SBApiResponse resp = cassandraOperation.insertRecord(Constants.KEYSPACE_SUNBIRD,
				Constants.TABLE_OFFENSIVE_DATA_FLAGS, requestData);
		return resp.get(Constants.RESPONSE).equals(Constants.SUCCESS);
	}

	@Override
	public List<Map<String, Object>> getFlaggedData(String userId) {
		Map<String, Object> request = new HashMap<>();
		request.put(Constants.USER_ID, userId);
		List<Map<String, Object>> existingDataList = cassandraOperation.getRecordsByProperties(
				Constants.KEYSPACE_SUNBIRD, Constants.TABLE_OFFENSIVE_DATA_FLAGS, request, null);
		return existingDataList;
	}

	@Override
	public Boolean updateFlaggedData(String userId, Map<String, Object> request) {
		Map<String, Object> compositeKeys = new HashMap<>();
		compositeKeys.put(Constants.USER_ID, userId);
		compositeKeys.put(Constants.CONTEXT_TYPE, request.get(Constants.CONTEXT_TYPE));
		compositeKeys.put(Constants.CONTEXT_TYPE_ID, request.get(Constants.CONTEXT_TYPE_ID));
		Map<String, Object> fieldsToBeUpdated = new HashMap<>();
		fieldsToBeUpdated.put(Constants.CONTEXT_STATUS, request.get(Constants.CONTEXT_STATUS));
		if(!ObjectUtils.isEmpty(request.get(Constants.ADDITIONAL_PARAMS))) {
			fieldsToBeUpdated.put(Constants.ADDITIONAL_PARAMS, request.get(Constants.ADDITIONAL_PARAMS));
		}
		fieldsToBeUpdated.put(Constants.UPDATED_BY, userId);
		fieldsToBeUpdated.put(Constants.UPDATED_AT, new Timestamp(new java.util.Date().getTime()));
		cassandraOperation.updateRecord(Constants.KEYSPACE_SUNBIRD, Constants.TABLE_OFFENSIVE_DATA_FLAGS,
				fieldsToBeUpdated, compositeKeys);
		return true;
	}
}