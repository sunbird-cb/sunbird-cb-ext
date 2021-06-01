package org.sunbird.workallocation.service;

import clojure.lang.Obj;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
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

    ObjectMapper mapper = new ObjectMapper();


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
            logger.info("user Ids : {}", mapper.writeValueAsString(userIds));
            Map<String, Object> usersMap = allocationService.getUserDetails(userIds);
            logger.info("user Map : {}", mapper.writeValueAsString(usersMap));
            if (!ObjectUtils.isEmpty(usersMap.get(workOrderDTO.getCreatedBy()))) {
                UserBasicInfo userBasicInfo = mapper.convertValue(usersMap.get(workOrderDTO.getCreatedBy()), UserBasicInfo.class);
                String name = userBasicInfo.getFirst_name() == null ? "" : userBasicInfo.getFirst_name() + " " + userBasicInfo.getLast_name() == null ? "" : userBasicInfo.getLast_name();
                workOrderDTO.setCreatedByName(name);
            }
            if (!ObjectUtils.isEmpty(usersMap.get(workOrderDTO.getUpdatedBy()))) {
                UserBasicInfo userBasicInfo = mapper.convertValue(usersMap.get(workOrderDTO.getUpdatedBy()), UserBasicInfo.class);
                String name = userBasicInfo.getFirst_name() == null ? "" : userBasicInfo.getFirst_name() + " " + userBasicInfo.getLast_name() == null ? "" : userBasicInfo.getLast_name();
                workOrderDTO.setUpdatedByName(name);
            }
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
            if (!ObjectUtils.isEmpty(usersMap.get(workAllocationDTOV2.getCreatedBy()))) {
                UserBasicInfo userBasicInfo = mapper.convertValue(usersMap.get(workAllocationDTOV2.getCreatedBy()), UserBasicInfo.class);
                String name = userBasicInfo.getFirst_name() == null ? "" : userBasicInfo.getFirst_name() + " " + userBasicInfo.getLast_name() == null ? "" : userBasicInfo.getLast_name();
                workAllocationDTOV2.setCreatedByName(name);
            }
            if (!ObjectUtils.isEmpty(usersMap.get(workAllocationDTOV2.getUpdatedBy()))) {
                UserBasicInfo userBasicInfo = mapper.convertValue(usersMap.get(workAllocationDTOV2.getUpdatedBy()), UserBasicInfo.class);
                String name = userBasicInfo.getFirst_name() == null ? "" : userBasicInfo.getFirst_name() + " " + userBasicInfo.getLast_name() == null ? "" : userBasicInfo.getLast_name();
                workAllocationDTOV2.setUpdatedByName(name);
            }
        } catch (IOException e) {
            logger.error("Error while fetching the user details", e);
        }
    }
}
