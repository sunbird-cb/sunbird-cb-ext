package org.sunbird.cassandra.utils;

import com.datastax.driver.core.*;
import com.datastax.driver.core.querybuilder.*;
import com.datastax.driver.core.querybuilder.Select.Builder;
import com.datastax.driver.core.querybuilder.Select.Where;
import com.datastax.driver.core.querybuilder.Update.Assignments;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.sunbird.common.helper.cassandra.CassandraConnectionManager;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

@Component
public class CassandraOperationImpl implements CassandraOperation {

	private Logger logger = LoggerFactory.getLogger(getClass().getName());

	@Autowired
  	CassandraConnectionManager connectionManager;

	@Override
	public SBApiResponse insertRecord(String keyspaceName, String tableName, Map<String, Object> request) {
		SBApiResponse response = new SBApiResponse();
		String query = CassandraUtil.getPreparedStatement(keyspaceName, tableName, request);
		try {
			PreparedStatement statement = connectionManager.getSession(keyspaceName).prepare(query);
			BoundStatement boundStatement = new BoundStatement(statement);
			Iterator<Object> iterator = request.values().iterator();
			Object[] array = new Object[request.keySet().size()];
			int i = 0;
			while (iterator.hasNext()) {
				array[i++] = iterator.next();
			}
			connectionManager.getSession(keyspaceName).execute(boundStatement.bind(array));
			response.put(Constants.RESPONSE, Constants.SUCCESS);
		} catch (Exception e) {
			String errMsg = String.format("Exception occurred while inserting record to %s %s", tableName, e.getMessage());
			logger.error(errMsg);
			response.put(Constants.RESPONSE, Constants.FAILED);
			response.put(Constants.ERROR_MESSAGE, errMsg);
		}
		return response;
	}

	@Override
	public SBApiResponse insertBulkRecord(String keyspaceName, String tableName, List<Map<String, Object>> request) {
		SBApiResponse response = new SBApiResponse();
		try {
			BatchStatement batchStatement = new BatchStatement();
			for (Map<String, Object> requestMap : request) {
				String query = CassandraUtil.getPreparedStatement(keyspaceName, tableName, requestMap);
				PreparedStatement statement = connectionManager.getSession(keyspaceName).prepare(query);
				BoundStatement boundStatement = new BoundStatement(statement);
				Iterator<Object> iterator = requestMap.values().iterator();
				Object[] array = new Object[requestMap.size()];
				int i = 0;
				while (iterator.hasNext()) {
					array[i++] = iterator.next();
				}
				boundStatement.bind(array);
				batchStatement.add(boundStatement);
			}
			connectionManager.getSession(keyspaceName).execute(batchStatement);
			response.put(Constants.RESPONSE, Constants.SUCCESS);
		} catch (Exception e) {
			logger.error(String.format("Exception occurred while inserting bulk record to %s %s", tableName,
					e.getMessage()));
		}
		return response;
	}

	@Override
	public List<Map<String, Object>> getRecordsByProperties(String keyspaceName, String tableName,
			Map<String, Object> propertyMap, List<String> fields) {
		Select selectQuery = null;
		List<Map<String, Object>> response = new ArrayList<>();
		try {
			selectQuery = processQuery(keyspaceName, tableName, propertyMap, fields);
			ResultSet results = connectionManager.getSession(keyspaceName).execute(selectQuery);
			response = CassandraUtil.createResponse(results);

		} catch (Exception e) {
			logger.error(Constants.EXCEPTION_MSG_FETCH + tableName + " : " + e.getMessage(), e);
		}
		return response;
	}

	@Override
	public Map<String, Object> getRecordsByProperties(String keyspaceName, String tableName,
			Map<String, Object> propertyMap, List<String> fields, String key) {
		Select selectQuery = null;
		Map<String, Object> response = new HashMap<>();
		try {
			selectQuery = processQuery(keyspaceName, tableName, propertyMap, fields);
			ResultSet results = connectionManager.getSession(keyspaceName).execute(selectQuery);
			response = CassandraUtil.createResponse(results, key);

		} catch (Exception e) {
			logger.error(Constants.EXCEPTION_MSG_FETCH + tableName + " : " + e.getMessage(), e);
		}
		return response;
	}

	@Override
	public List<Map<String, Object>> searchByWhereClause(String keyspace, String tableName, List<String> fields,
			Date date) {
		Builder selectBuilder;
		if (CollectionUtils.isNotEmpty(fields)) {
			String[] dbFields = fields.toArray(new String[fields.size()]);
			selectBuilder = QueryBuilder.select(dbFields);
		} else {
			selectBuilder = QueryBuilder.select().all();
		}
		Select selectQuery = selectBuilder.from(keyspace, tableName);
		Where selectWhere = selectQuery.where();
		Clause completionpercentagegreaterthanzero = QueryBuilder.gt("completionpercentage", 0);
		selectWhere.and(completionpercentagegreaterthanzero);
		Clause completionpercentagelessthanhundred = QueryBuilder.lt("completionpercentage", 100);
		selectWhere.and(completionpercentagelessthanhundred);
		Clause lastAccessTimeNotNull = QueryBuilder.gt("last_access_time", 0);
		selectWhere.and(lastAccessTimeNotNull);
		selectQuery.allowFiltering();
		Clause lastAccessTime = QueryBuilder.lt("last_access_time", date);
		selectWhere.and(lastAccessTime);
		logger.debug("our query: " + selectQuery.getQueryString());
		ResultSet resultSet = connectionManager.getSession(keyspace).execute(selectQuery);
		return CassandraUtil.createResponse(resultSet);
	}

	@Override
	public Map<String, Object> getRecordsByPropertiesWithPagination(String keyspaceName, String tableName,
			Map<String, Object> propertyMap, List<String> fields, int limit, String updatedOn, String key) {
		Select selectQuery = null;
		Map<String, Object> response = new HashMap<>();
		try {
			selectQuery = processQuery(keyspaceName, tableName, propertyMap, fields);
			selectQuery.limit(limit);
			if (!StringUtils.isEmpty(updatedOn)) {
				selectQuery.where(QueryBuilder.lt("updatedon", UUID.fromString(updatedOn)));
			}
			ResultSet results = connectionManager.getSession(keyspaceName).execute(selectQuery);
			response = CassandraUtil.createResponse(results, key);
		} catch (Exception e) {
			logger.error(Constants.EXCEPTION_MSG_FETCH + tableName + " : " + e.getMessage(), e);
		}
		return response;
	}

	private Select processQuery(String keyspaceName, String tableName, Map<String, Object> propertyMap,
			List<String> fields) {
		Select selectQuery = null;

		Builder selectBuilder;
		if (CollectionUtils.isNotEmpty(fields)) {
			String[] dbFields = fields.toArray(new String[fields.size()]);
			selectBuilder = QueryBuilder.select(dbFields);
		} else {
			selectBuilder = QueryBuilder.select().all();
		}
		selectQuery = selectBuilder.from(keyspaceName, tableName);
		if (MapUtils.isNotEmpty(propertyMap)) {
			Where selectWhere = selectQuery.where();
			for (Entry<String, Object> entry : propertyMap.entrySet()) {
				if (entry.getValue() instanceof List) {
					List<Object> list = (List) entry.getValue();
					if (null != list) {
						Object[] propertyValues = list.toArray(new Object[list.size()]);
						Clause clause = QueryBuilder.in(entry.getKey(), propertyValues);
						selectWhere.and(clause);

					}
				} else {

					Clause clause = QueryBuilder.eq(entry.getKey(), entry.getValue());
					selectWhere.and(clause);

				}
				selectQuery.allowFiltering();
			}
		}
		return selectQuery;
	}

	@Override
	public void deleteRecord(String keyspaceName, String tableName, Map<String, Object> compositeKeyMap) {
		Delete delete = null;
		try {
			delete = QueryBuilder.delete().from(keyspaceName, tableName);
			Delete.Where deleteWhere = delete.where();
			compositeKeyMap.entrySet().stream().forEach(x -> {
				Clause clause = QueryBuilder.eq(x.getKey(), x.getValue());
				deleteWhere.and(clause);
			});
			connectionManager.getSession(keyspaceName).execute(delete);
		} catch (Exception e) {
			logger.error(String.format("CassandraOperationImpl: deleteRecord by composite key. %s %s %s",
					Constants.EXCEPTION_MSG_DELETE, tableName, e.getMessage()));
			throw e;
		}
	}

	@Override
	public Map<String, Object> updateRecord(String keyspaceName, String tableName, Map<String, Object> updateAttributes,
			Map<String, Object> compositeKey) {
		Map<String, Object> response = new HashMap<>();
		Statement updateQuery = null;
		try {
			Session session = connectionManager.getSession(keyspaceName);
			Update update = QueryBuilder.update(keyspaceName, tableName);
			Assignments assignments = update.with();
			Update.Where where = update.where();
			updateAttributes.entrySet().stream().forEach(x -> {
				assignments.and(QueryBuilder.set(x.getKey(), x.getValue()));
			});
			compositeKey.entrySet().stream().forEach(x -> {
				where.and(QueryBuilder.eq(x.getKey(), x.getValue()));
			});
			updateQuery = where;
			session.execute(updateQuery);
			response.put(Constants.RESPONSE, Constants.SUCCESS);
		} catch (Exception e) {
			String errMsg = String.format("Exception occurred while updating record to %s %s", tableName, e.getMessage());
			logger.error(errMsg);
			response.put(Constants.RESPONSE, Constants.FAILED);
			response.put(Constants.ERROR_MESSAGE, errMsg);
			throw e;
		}
		return response;
	}

	@Override
	public Long getRecordCount(String keyspace, String table) {
		try {
			Select selectQuery = QueryBuilder.select().countAll().from(keyspace, table);
			Row row = connectionManager.getSession(keyspace).execute(selectQuery).one();
			return row.getLong(0);
		} catch (Exception e) {
			throw e;
		}
	}

	public void getAllRecords(String keyspace, String table, List<String> fields, String key,
			Map<String, Map<String, String>> objectInfo) {
		Select selectQuery = null;
		try {
			selectQuery = processQuery(keyspace, table, MapUtils.EMPTY_MAP, fields);
			ResultSet results = connectionManager.getSession(keyspace).execute(selectQuery);
			Map<String, String> columnsMapping = CassandraUtil.fetchColumnsMapping(results);
			Iterator<Row> rowIterator = results.iterator();
			rowIterator.forEachRemaining(row -> {
				Map<String, String> rowMap = new HashMap<>();
				columnsMapping.entrySet().stream().forEach(entry -> {
					rowMap.put(entry.getKey(), (String) row.getObject(entry.getValue()));
				});

				objectInfo.put((String) rowMap.get(key), rowMap);
			});
		} catch (Exception e) {
			logger.error(Constants.EXCEPTION_MSG_FETCH + table + " : " + e.getMessage(), e);
		}
	}

	public void getAllRecordsWithPagination(String keyspace, String table, List<String> fields, String key,
			Map<String, Map<String, String>> objectInfo) {
		long startTime = System.currentTimeMillis();
		Select selectQuery = processQuery(keyspace, table, MapUtils.EMPTY_MAP, fields);

		int n = 0;
		PagingState pageStates = null;
		Map<Integer, PagingState> stringMap = new HashMap<Integer, PagingState>();
		do {
			Statement select = selectQuery.setFetchSize(100).setPagingState(pageStates);
			ResultSet resultSet = connectionManager.getSession(keyspace).execute(select);
			pageStates = resultSet.getExecutionInfo().getPagingState();
			stringMap.put(++n, pageStates);
		} while (pageStates != null);

		Iterator<Entry<Integer, PagingState>> pageIterator = stringMap.entrySet().iterator();
		while (pageIterator.hasNext()) {
			Entry<Integer, PagingState> pageEntry = pageIterator.next();
			Statement selectq = selectQuery.setPagingState(pageEntry.getValue());
			ResultSet resultSet = connectionManager.getSession(keyspace).execute(selectq);
			Map<String, String> columnsMapping = CassandraUtil.fetchColumnsMapping(resultSet);

			Iterator<Row> rowIterator = resultSet.iterator();
			rowIterator.forEachRemaining(row -> {
				Map<String, String> rowMap = new HashMap<>();
				columnsMapping.entrySet().stream().forEach(entry -> {
					rowMap.put(entry.getKey(), (String) row.getObject(entry.getValue()));
				});

				objectInfo.put((String) rowMap.get(key), rowMap);
			});
		}
		logger.info(String.format("Competed Oeration in %s seconds",
				TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime)));
	}

	@Override
	public List<Map<String, Object>> getRecordsWithInClause(String keyspaceName, String tableName, List<Map<String, Object>> propertyMaps, List<String> fields) {
		Select.Where selectQuery = null;
		List<Map<String, Object>> response = new ArrayList<>();
		try {
			if (CollectionUtils.isNotEmpty(fields)) {
				selectQuery = QueryBuilder.select(fields.toArray(new String[fields.size()])).from(keyspaceName, tableName).where();
			} else {
				selectQuery = QueryBuilder.select().all().from(keyspaceName, tableName).where();
			}
			List<Object> values = new ArrayList<>();
			String key = null;
			for (Map<String, Object> propertyMap : propertyMaps) {
				for (Map.Entry<String, Object> entry : propertyMap.entrySet()) {
					key = entry.getKey();
					if (entry.getValue() instanceof List) {
						values.addAll((List<String>) entry.getValue());
					} else {
						values.add(entry.getValue());
					}
				}
				selectQuery.and(QueryBuilder.in(key, values.toArray()));
			}
			ResultSet results = connectionManager.getSession(keyspaceName).execute(selectQuery);
			response = CassandraUtil.createResponse(results);
		} catch (Exception e) {
			logger.error(Constants.EXCEPTION_MSG_FETCH + tableName + " : " + e.getMessage(), e);
		}
		return response;
	}

	/**
	 * Fetch records with specified columns (select all if null) for given column
	 * map (name, value pairs).
	 *
	 * @param keyspaceName Keyspace name
	 * @param tableName    Table name
	 * @param propertyMap  Map describing columns to be used in where clause of
	 *                     select query.
	 * @param fields       List of columns to be returned in each record
	 * @return List consisting of fetched records
	 */
	public List<Map<String, Object>> getRecordsByPropertiesWithoutFiltering(String keyspaceName, String tableName,
			Map<String, Object> propertyMap, List<String> fields) {
		return getRecordsByPropertiesWithoutFiltering(keyspaceName, tableName, propertyMap, fields, null);
	}

	@Override
	public List<Map<String, Object>> getRecordsByPropertiesWithoutFiltering(String keyspaceName, String tableName,
			Map<String, Object> propertyMap, List<String> fields, Integer limit) {
		Select selectQuery = null;
		List<Map<String, Object>> response = new ArrayList<>();
		try {
			selectQuery = processQueryWithoutFiltering(keyspaceName, tableName, propertyMap, fields);
			if (limit != null) {
				selectQuery = selectQuery.limit(limit);
			}
			ResultSet results = connectionManager.getSession(keyspaceName).execute(selectQuery);
			response = CassandraUtil.createResponse(results);

		} catch (Exception e) {
			logger.error(Constants.EXCEPTION_MSG_FETCH + tableName + " : " + e.getMessage(), e);
		}
		return response;
	}

	private Select processQueryWithoutFiltering(String keyspaceName, String tableName, Map<String, Object> propertyMap,
			List<String> fields) {
		Select selectQuery = null;
		Builder selectBuilder;
		if (CollectionUtils.isNotEmpty(fields)) {
			String[] dbFields = fields.toArray(new String[fields.size()]);
			selectBuilder = QueryBuilder.select(dbFields);
		} else {
			selectBuilder = QueryBuilder.select().all();
		}
		selectQuery = selectBuilder.from(keyspaceName, tableName);
		if (MapUtils.isNotEmpty(propertyMap)) {
			Where selectWhere = selectQuery.where();
			for (Entry<String, Object> entry : propertyMap.entrySet()) {
				if (entry.getValue() instanceof List) {
					List<Object> list = (List) entry.getValue();
					if (null != list) {
						Object[] propertyValues = list.toArray(new Object[list.size()]);
						Clause clause = QueryBuilder.in(entry.getKey(), propertyValues);
						selectWhere.and(clause);
					}
				} else {
					Clause clause = QueryBuilder.eq(entry.getKey(), entry.getValue());
					selectWhere.and(clause);
				}
			}
		}
		return selectQuery;
	}

	public Map<String, Object> getRecordsByPropertiesByKey(String keyspaceName, String tableName,
			Map<String, Object> propertyMap, List<String> fields, String key) {
		Select selectQuery = null;
		Map<String, Object> response = new HashMap<>();
		try {
			selectQuery = processQueryWithoutFiltering(keyspaceName, tableName, propertyMap, fields);
			ResultSet results = connectionManager.getSession(keyspaceName).execute(selectQuery);
			response = CassandraUtil.createResponse(results, key);
		} catch (Exception e) {
			logger.error(Constants.EXCEPTION_MSG_FETCH + tableName + " : " + e.getMessage(), e);
		}
		return response;
	}

	@Override
	public List<Map<String, Object>> getKarmaPointsRecordsByPropertiesWithPaginationList(String keyspaceName, String tableName,
																						 Map<String, Object> propertyMap, List<String> fields, int limit, Date updatedOn, String key,Date limitDate) {
		Select selectQuery = null;
		List<Map<String, Object>> response = new ArrayList<>();
		try {
			selectQuery = processQueryWithoutFiltering(keyspaceName, tableName, propertyMap, fields);
			selectQuery.limit(limit);
			selectQuery.where(QueryBuilder.lt(Constants.DB_COLUMN_CREDIT_DATE, updatedOn));
			if(limitDate != null)
			selectQuery.where(QueryBuilder.gt(Constants.DB_COLUMN_CREDIT_DATE, limitDate));

			selectQuery.orderBy(QueryBuilder.desc(Constants.DB_COLUMN_CREDIT_DATE));
			ResultSet results = connectionManager.getSession(keyspaceName).execute(selectQuery);
			response = CassandraUtil.createResponse(results);
		} catch (Exception e) {
			logger.error(Constants.EXCEPTION_MSG_FETCH + tableName + " : " + e.getMessage(), e);
		}
		return response;
	}
	public Long getRecordCountWithUserId(String keyspace, String tableName, String userId,Date limitDate) {
		try {
			Select selectQuery = QueryBuilder.select().countAll().from(keyspace, tableName);
			selectQuery.where(QueryBuilder.eq(Constants.USER_ID, userId));
			selectQuery.where(QueryBuilder.gt(Constants.DB_COLUMN_CREDIT_DATE, limitDate));
			Row row = connectionManager.getSession(keyspace).execute(selectQuery).one();
			return row.getLong(0);
		} catch (Exception e) {
			logger.error(Constants.EXCEPTION_MSG_FETCH + tableName + " : " + e.getMessage(), e);
			throw e;
		}
	}

	@Override
	public Map<String,Object> getRecordByIdentifierWithPage(String keyspaceName, String tableName,
															Map<String,Object> key, List<String> fields, String pageString, int limit) {
		long startTime = System.currentTimeMillis();
		Map<String,Object> response = new HashMap<>();
		try {
			Session session = connectionManager.getSession(keyspaceName);
			Builder selectBuilder;
			if (CollectionUtils.isNotEmpty(fields)) {
				selectBuilder = QueryBuilder.select(fields.toArray(new String[fields.size()]));
			} else {
				selectBuilder = QueryBuilder.select().all();
			}
			Select selectQuery = selectBuilder.from(keyspaceName, tableName);
			if (MapUtils.isNotEmpty(key)) {
				Where selectWhere = selectQuery.where();
				for (Entry<String, Object> entry : key.entrySet()) {
					if (entry.getValue() instanceof List) {
						List<Object> list = (List) entry.getValue();
						if (null != list) {
							Object[] propertyValues = list.toArray(new Object[list.size()]);
							Clause clause = QueryBuilder.in(entry.getKey(), propertyValues);
							selectWhere.and(clause);
						}
					} else {
						Clause clause = QueryBuilder.eq(entry.getKey(), entry.getValue());
						selectWhere.and(clause);
					}
				}
			}
			if (StringUtils.isNotBlank(pageString)) {
				selectQuery.setPagingState(PagingState.fromString(pageString));
			}
			selectQuery.setFetchSize(limit);
			ResultSet results = session.execute(selectQuery);
			List<Map<String, Object>> responseList = new ArrayList<>();
			Map<String, String> columnsMapping = CassandraUtil.fetchColumnsMapping(results);
			int remaining = results.getAvailableWithoutFetching();
			Iterator<Row> rowIterator = results.iterator();
			while(rowIterator.hasNext()) {
				Row row = rowIterator.next();
				Map<String, Object> rowMap = new HashMap<>();
				columnsMapping.entrySet().stream()
						.forEach(entry -> rowMap.put(entry.getKey(), row.getObject(entry.getValue())));
				responseList.add(rowMap);
				remaining--;
				if (remaining == 0 || responseList.size() >= limit) {
					break;
				}
			}
			response.put(Constants.RESPONSE, responseList);
			if (results.getExecutionInfo().getPagingState() != null) {
				response.put(Constants.PAGE_ID, results.getExecutionInfo().getPagingState().toString());
			}
		} catch (Exception e) {
			logger.error(Constants.EXCEPTION_MSG_FETCH + tableName + " : " + e.getMessage(), e);
		}
		return response;
	}

	@Override
	public Long getCountOfRecordByIdentifier(String keyspaceName, String tableName, Map<String,Object> key, String field) {
		long startTime = System.currentTimeMillis();
		List<Map<String,Object>>  response = new ArrayList<>();
		Long count = 0L;
		try {
			if (MapUtils.isEmpty(key)) {
				throw new IllegalArgumentException("Key parameter cannot be null");
			}
			Session session = connectionManager.getSession(keyspaceName);
			Builder selectBuilder;
			selectBuilder = QueryBuilder.select().count(field);
			Select selectQuery = selectBuilder.from(keyspaceName, tableName);
			if (MapUtils.isNotEmpty(key)) {
				Where selectWhere = selectQuery.where();
				for (Entry<String, Object> entry : key.entrySet()) {
					if (entry.getValue() instanceof List) {
						List<Object> list = (List) entry.getValue();
						if (null != list) {
							Object[] propertyValues = list.toArray(new Object[list.size()]);
							Clause clause = QueryBuilder.in(entry.getKey(), propertyValues);
							selectWhere.and(clause);
						}
					} else {
						Clause clause = QueryBuilder.eq(entry.getKey(), entry.getValue());
						selectWhere.and(clause);
					}
				}
			}
			ResultSet results = session.execute(selectQuery);
			response = CassandraUtil.createResponse(results);
			count = ((Long)((Map<String,Object>)response.get(0)).get("system.count(" + field.toLowerCase() + ")"));
		} catch (Exception e) {
			logger.error(Constants.EXCEPTION_MSG_FETCH + tableName + " : " + e.getMessage(), e);

		}
		return count;
	}
}

