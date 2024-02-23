package org.sunbird.operationalreports.service;

/**
 * @author Deepak kumar Thakur
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.sunbird.cassandra.utils.CassandraOperation;
import org.sunbird.common.model.SBApiResponse;
import org.sunbird.common.util.CbExtServerProperties;
import org.sunbird.common.util.Constants;
import org.sunbird.user.service.UserUtilityService;

import java.util.*;

@Service
public class OprationalReportServiceImpl implements OperationalReportService {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    UserUtilityService userUtilService;

    @Autowired
    private CbExtServerProperties serverConfig;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private CassandraOperation cassandraOperation;

    public SBApiResponse grantAccessToMDOAdmin(String userOrgId, String authToken) {
        //Need to Implimented
        return null;
    }

}


