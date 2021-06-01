package org.sunbird.workallocation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.sunbird.common.util.Constants;
import org.sunbird.workallocation.model.*;
import org.sunbird.workallocation.util.WorkAllocationConstants;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class EnrichmentService {
    private Logger logger = LoggerFactory.getLogger(EnrichmentService.class);

    @Autowired
    private AllocationService allocationService;

    public void enrichWorkOrder(WorkOrderDTO workOrderDTO, String userId) {
        long currentMillis = System.currentTimeMillis();
        if (StringUtils.isEmpty(workOrderDTO.getStatus()) || WorkAllocationConstants.DRAFT_STATUS.equals(workOrderDTO.getStatus())) {
            workOrderDTO.setStatus(WorkAllocationConstants.DRAFT_STATUS);
            workOrderDTO.setId(UUID.randomUUID().toString());
            workOrderDTO.setCreatedBy(userId);
            workOrderDTO.setCreatedAt(currentMillis);
            workOrderDTO.setUpdatedBy(userId);
            workOrderDTO.setUpdatedAt(currentMillis);
            workOrderDTO.setUserIds(null);
        }
        if (WorkAllocationConstants.PUBLISHED_STATUS.equals(workOrderDTO.getStatus())) {
            workOrderDTO.setUpdatedBy(userId);
            workOrderDTO.setUpdatedAt(currentMillis);
        }
        enrichUserNamesToWorkOrder(workOrderDTO);
    }

    public void enrichWorkAllocation(WorkAllocationDTOV2 workAllocationDTOV2, String userId) {
        long currentMillis = System.currentTimeMillis();
        if (StringUtils.isEmpty(workAllocationDTOV2.getCreatedBy())) {
            workAllocationDTOV2.setCreatedBy(userId);
            workAllocationDTOV2.setCreatedAt(currentMillis);
            workAllocationDTOV2.setUpdatedBy(userId);
            workAllocationDTOV2.setUpdatedAt(currentMillis);
        } else {
            workAllocationDTOV2.setUpdatedBy(userId);
            workAllocationDTOV2.setUpdatedAt(currentMillis);
        }
        enrichUserNamesToWorkAllocation(workAllocationDTOV2);
    }

    private void enrichUserNamesToWorkOrder(WorkOrderDTO workOrderDTO) {
        Set<String> userIds = new HashSet<>();
        userIds.add(workOrderDTO.getCreatedBy());
        userIds.add(workOrderDTO.getUpdatedBy());
        try {
            ObjectMapper mapper = new ObjectMapper();
            logger.info("user Ids : {}",mapper.writeValueAsString(userIds));
            Map<String, Object> usersMap = allocationService.getUserDetails(userIds);
            logger.info("user Map : {}",mapper.writeValueAsString(usersMap));
            Map<String, Object> createdByDetails = allocationService.extractUserDetails((Map<String, Object>) usersMap.get(workOrderDTO.getCreatedBy()));
            Map<String, Object> updatedByDetails = allocationService.extractUserDetails((Map<String, Object>) usersMap.get(workOrderDTO.getUpdatedBy()));
            String createdByName = (createdByDetails.get("first_name") == null ? "" : (String) createdByDetails.get("first_name")) + (createdByDetails.get("last_name") == null ? "" : (String) createdByDetails.get("last_name"));
            String updatedByName = (updatedByDetails.get("first_name") == null ? "" : (String) updatedByDetails.get("first_name")) + (updatedByDetails.get("last_name") == null ? "" : (String) updatedByDetails.get("last_name"));
            workOrderDTO.setCreatedByName(createdByName);
            workOrderDTO.setUpdatedByName(updatedByName);
        } catch (IOException e) {
            logger.error("Error while fetching the user details", e);
        }
    }
    private void enrichUserNamesToWorkAllocation(WorkAllocationDTOV2 workAllocationDTOV2) {
        Set<String> userIds = new HashSet<>();
        userIds.add(workAllocationDTOV2.getCreatedBy());
        userIds.add(workAllocationDTOV2.getUpdatedBy());
        try {
            Map<String, Object> usersMap = allocationService.getUserDetails(userIds);
            Map<String, Object> createdByDetails = allocationService.extractUserDetails((Map<String, Object>) usersMap.get(workAllocationDTOV2.getCreatedBy()));
            Map<String, Object> updatedByDetails = allocationService.extractUserDetails((Map<String, Object>) usersMap.get(workAllocationDTOV2.getUpdatedBy()));
            String createdByName = (createdByDetails.get("first_name") == null ? "" : (String) createdByDetails.get("first_name")) + (createdByDetails.get("last_name") == null ? "" : (String) createdByDetails.get("last_name"));
            String updatedByName = (updatedByDetails.get("first_name") == null ? "" : (String) updatedByDetails.get("first_name")) + (updatedByDetails.get("last_name") == null ? "" : (String) updatedByDetails.get("last_name"));
            workAllocationDTOV2.setCreatedByName(createdByName);
            workAllocationDTOV2.setUpdatedByName(updatedByName);
        } catch (IOException e) {
            logger.error("Error while fetching the user details", e);
        }
    }
}
