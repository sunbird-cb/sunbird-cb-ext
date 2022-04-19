package org.sunbird.cassandra.utils;

import org.sunbird.common.model.SBApiResponse;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author fathima
 * @desc this interface will hold functions for cassandra db interaction
 */
public interface CassandraOperation {

    /**
     * @param keyspaceName Keyspace name
     * @param tableName    Table name
     * @param request      Map<String,Object>(i.e map of column name and their value)
     * @return Response
     * @desc This method is used to insert record in cassandra db
     */

    public SBApiResponse insertRecord(String keyspaceName, String tableName, Map<String, Object> request);

    /**
     * Insert bulk data using batch
     *
     * @param keyspaceName String
     * @param tableName    String
     * @param request      List<Map<String, Object>>
     * @return SBApiResponse
     */
    public SBApiResponse insertBulkRecord(String keyspaceName, String tableName, List<Map<String, Object>> request);

    /**
     * Fetch records with specified columns (select all if null) for given column
     * map (name, value pairs).
     *
     * @param keyspaceName Keyspace name
     * @param tableName    Table name
     * @param propertyMap  Map describing columns to be used in where clause of select query.
     * @param fields       List of columns to be returned in each record
     * @return List consisting of fetched records
     */
    List<Map<String, Object>> getRecordsByProperties(String keyspaceName, String tableName,
                                                     Map<String, Object> propertyMap, List<String> fields);

    /**
     * @param keyspaceName Keyspace name
     * @param tableName    Table name
     * @param keyMap       Column map for composite primary key
     * @desc This method is used to delete record in cassandra db by their primary
     * composite key
     */
    public void deleteRecord(String keyspaceName, String tableName, Map<String, Object> keyMap);

    /**
     * Method to update the record on basis of composite primary key.
     *
     * @param keyspaceName     Keyspace name
     * @param tableName        Table name
     * @param updateAttributes Column map to be used in set clause of update query
     * @param compositeKey     Column map for composite primary key
     * @return Response consisting of update query status
     */
    Map<String, Object> updateRecord(String keyspaceName, String tableName, Map<String, Object> updateAttributes,
                                     Map<String, Object> compositeKey);

    /**
     * To get count of all records
     *
     * @param keyspace String
     * @param table    String
     * @return Long
     */
    public Long getRecordCount(String keyspace, String table);

    public Map<String, Object> getRecordsByProperties(String keyspaceName, String tableName,
                                                                        Map<String, Object> propertyMap, List<String> fields,String key);
    public Map<String, Object> getRecordsByPropertiesWithPagination(String keyspaceName, String tableName,
                                                                          Map<String, Object> propertyMap, List<String> fields, int limit, String updatedOn,String key);
    List<Map<String, Object>> searchByWhereClause(
            String keyspace,
            String tableName,
            List<String> fields,
            Date date);

}
