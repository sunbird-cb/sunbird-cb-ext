package org.sunbird.workallocation.service;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.sunbird.workallocation.model.UserBasicInfo;
import org.sunbird.workallocation.model.WorkAllocationDTOV2;
import org.sunbird.workallocation.model.WorkOrderDTO;
import org.sunbird.workallocation.util.WorkAllocationConstants;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class EnrichmentService {
	private Logger logger = LoggerFactory.getLogger(EnrichmentService.class);

	@Autowired
	private AllocationServiceV2 allocationServiceV2;

	ObjectMapper mapper = new ObjectMapper();

	public void enrichCopyWorkOrder(WorkOrderDTO workOrderDTO, String userId) {
		long currentMillis = System.currentTimeMillis();
		workOrderDTO.setStatus(WorkAllocationConstants.DRAFT_STATUS);
		workOrderDTO.setId(UUID.randomUUID().toString());
		workOrderDTO.setCreatedBy(userId);
		workOrderDTO.setCreatedAt(currentMillis);
		workOrderDTO.setUpdatedBy(userId);
		workOrderDTO.setUpdatedAt(currentMillis);
		workOrderDTO.setPublishedPdfLink(null);
		workOrderDTO.setSignedPdfLink(null);
		enrichUserNamesToWorkOrder(workOrderDTO);
	}

	private void enrichUserNamesToWorkAllocation(WorkAllocationDTOV2 workAllocationDTOV2) {
		Set<String> userIds = new HashSet<>();
		if (StringUtils.isEmpty(workAllocationDTOV2.getCreatedByName())) {
			userIds.add(workAllocationDTOV2.getCreatedBy());
		}
		userIds.add(workAllocationDTOV2.getUpdatedBy());
		try {
			Map<String, Object> usersMap = allocationServiceV2.getUsersResult(userIds);
			if (StringUtils.isEmpty(workAllocationDTOV2.getCreatedByName())
					&& !ObjectUtils.isEmpty(usersMap.get(workAllocationDTOV2.getCreatedBy()))) {
				UserBasicInfo userBasicInfo = mapper.convertValue(usersMap.get(workAllocationDTOV2.getCreatedBy()),
						UserBasicInfo.class);
				String firstName = userBasicInfo.getFirstName() == null ? "" : userBasicInfo.getFirstName();
				String lastName = userBasicInfo.getLastName() == null ? "" : userBasicInfo.getLastName();
				workAllocationDTOV2.setCreatedByName(firstName + " " + lastName);
			}
			if (!ObjectUtils.isEmpty(usersMap.get(workAllocationDTOV2.getUpdatedBy()))) {
				UserBasicInfo userBasicInfo = mapper.convertValue(usersMap.get(workAllocationDTOV2.getUpdatedBy()),
						UserBasicInfo.class);
				String firstName = userBasicInfo.getFirstName() == null ? "" : userBasicInfo.getFirstName();
				String lastName = userBasicInfo.getLastName() == null ? "" : userBasicInfo.getLastName();
				workAllocationDTOV2.setUpdatedByName(firstName + " " + lastName);
			}
		} catch (Exception e) {
			logger.error(
					String.format("Encountered an Exception while fetching the user details :  %s", e.getMessage()));
		}
	}

	private void enrichUserNamesToWorkOrder(WorkOrderDTO workOrderDTO) {
		Set<String> userIds = new HashSet<>();
		if (StringUtils.isEmpty(workOrderDTO.getCreatedByName())) {
			userIds.add(workOrderDTO.getCreatedBy());
		}
		userIds.add(workOrderDTO.getUpdatedBy());
		Map<String, Object> usersMap = allocationServiceV2.getUsersResult(userIds);
		if (StringUtils.isEmpty(workOrderDTO.getCreatedByName())
				&& !ObjectUtils.isEmpty(usersMap.get(workOrderDTO.getCreatedBy()))) {
			UserBasicInfo userBasicInfo = mapper.convertValue(usersMap.get(workOrderDTO.getCreatedBy()),
					UserBasicInfo.class);
			String firstName = userBasicInfo.getFirstName() == null ? "" : userBasicInfo.getFirstName();
			String lastName = userBasicInfo.getLastName() == null ? "" : userBasicInfo.getLastName();
			workOrderDTO.setCreatedByName(firstName + " " + lastName);
		}
		if (!ObjectUtils.isEmpty(usersMap.get(workOrderDTO.getUpdatedBy()))) {
			UserBasicInfo userBasicInfo = mapper.convertValue(usersMap.get(workOrderDTO.getUpdatedBy()),
					UserBasicInfo.class);
			String firstName = userBasicInfo.getFirstName() == null ? "" : userBasicInfo.getFirstName();
			String lastName = userBasicInfo.getLastName() == null ? "" : userBasicInfo.getLastName();
			workOrderDTO.setUpdatedByName(firstName + " " + lastName);
		}
	}

	public void enrichWorkAllocation(WorkAllocationDTOV2 workAllocationDTOV2, String userId) {
		long currentMillis = System.currentTimeMillis();
		if (StringUtils.isEmpty(workAllocationDTOV2.getCreatedBy())) {
			workAllocationDTOV2.setCreatedBy(userId);
			workAllocationDTOV2.setCreatedAt(currentMillis);
		}
		workAllocationDTOV2.setUpdatedBy(userId);
		workAllocationDTOV2.setUpdatedAt(currentMillis);
		enrichUserNamesToWorkAllocation(workAllocationDTOV2);
	}

	public void enrichWorkOrder(WorkOrderDTO workOrderDTO, String userId, String reqType) {
		long currentMillis = System.currentTimeMillis();
		if (WorkAllocationConstants.ADD.equals(reqType)) {
			workOrderDTO.setStatus(WorkAllocationConstants.DRAFT_STATUS);
			workOrderDTO.setId(UUID.randomUUID().toString());
			workOrderDTO.setCreatedBy(userId);
			workOrderDTO.setCreatedAt(currentMillis);
			workOrderDTO.setUserIds(null);
		}
		workOrderDTO.setUpdatedBy(userId);
		workOrderDTO.setUpdatedAt(currentMillis);
		enrichUserNamesToWorkOrder(workOrderDTO);
	}
}
