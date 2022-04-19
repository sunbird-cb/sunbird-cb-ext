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
import org.springframework.stereotype.Component;
import org.sunbird.common.helper.cassandra.CassandraConnectionManager;
import org.sunbird.common.helper.cassandra.CassandraConnectionMngrFactory;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.Constants;

import java.util.*;
import java.util.Map.Entry;

@Component
public class CassandraOperationImpl implements CassandraOperation {

    private Logger logger = LoggerFactory.getLogger(getClass().getName());
    protected CassandraConnectionManager connectionManager = CassandraConnectionMngrFactory.getInstance();

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
            logger.error(
                    String.format("Exception occurred while inserting record to %s %s", tableName, e.getMessage()));
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
                                                            Map<String, Object> propertyMap, List<String> fields,String key) {
        Select selectQuery = null;
        Map<String, Object> response = new HashMap<>();
        try {
            selectQuery = processQuery(keyspaceName, tableName, propertyMap, fields);
            ResultSet results = connectionManager.getSession(keyspaceName).execute(selectQuery);
            response = CassandraUtil.createResponse(results,key);

        } catch (Exception e) {
            logger.error(Constants.EXCEPTION_MSG_FETCH + tableName + " : " + e.getMessage(), e);
        }
        return response;
    }

    @Override
    public List<Map<String, Object>> searchByWhereClause(
            String keyspace,
            String tableName,
            List<String> fields,
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
        Clause completionpercentagelessthanhundred= QueryBuilder.lt("completionpercentage", 100);
        selectWhere.and(completionpercentagelessthanhundred);
        Clause lastAccessTimeNotNull= QueryBuilder.gt("last_access_time", 0);
        selectWhere.and(lastAccessTimeNotNull);
        selectQuery.allowFiltering();
        Clause lastAccessTime= QueryBuilder.lt("last_access_time", date);
        selectWhere.and(lastAccessTime);
        logger.debug("our query: "+selectQuery.getQueryString());
        ResultSet resultSet = connectionManager.getSession(keyspace).execute(selectQuery);
        return CassandraUtil.createResponse(resultSet);
    }

    @Override
    public Map<String, Object> getRecordsByPropertiesWithPagination(String keyspaceName, String tableName,
                                                                          Map<String, Object> propertyMap, List<String> fields, int limit, String updatedOn,String key) {
        Select selectQuery = null;
        Map<String, Object> response = new HashMap<>();
        try {
            selectQuery = processQuery(keyspaceName, tableName, propertyMap, fields);
            selectQuery.limit(limit);
            if (!StringUtils.isEmpty(updatedOn)) {
                selectQuery.where(QueryBuilder.lt("updatedon", UUID.fromString(updatedOn)));
            }
            ResultSet results = connectionManager.getSession(keyspaceName).execute(selectQuery);
            response = CassandraUtil.createResponse(results,key);
        } catch (Exception e) {
            logger.error(Constants.EXCEPTION_MSG_FETCH + tableName + " : " + e.getMessage(), e);
        }
        return response;
    }

    private Select processQuery(String keyspaceName, String tableName,
                                Map<String, Object> propertyMap, List<String> fields) {
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
            logger.error(e.getMessage());
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

}
