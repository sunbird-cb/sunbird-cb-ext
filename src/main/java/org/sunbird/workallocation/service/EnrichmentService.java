package org.sunbird.workallocation.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.sunbird.common.util.Constants;
import org.sunbird.workallocation.model.ChildNode;
import org.sunbird.workallocation.model.Role;
import org.sunbird.workallocation.model.WorkAllocationDTO;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EnrichmentService {
    private Logger logger = LoggerFactory.getLogger(EnrichmentService.class);

    /**
     *
     * @param updatedBy used id
     * @param workAllocation work allocation object
     * @param existingRecord existing object
     * @param type add or update
     */
    public void enrichDates(String updatedBy, WorkAllocationDTO workAllocation, WorkAllocationDTO existingRecord, String type) {
//        long currentMillis = System.currentTimeMillis();
//        workAllocation.setUpdatedAt(currentMillis);
//        workAllocation.setUpdatedBy(updatedBy);
//
//        if (Constants.ADD.equals(type)) {
//            if (!CollectionUtils.isEmpty(workAllocation.getActiveList())) {
//                workAllocation.getActiveList().forEach(role -> {
//                    role.setAddedAt(currentMillis);
//                    role.setUpdatedAt(currentMillis);
//                    role.setUpdatedBy(updatedBy);
//                });
//            }
//            if (!CollectionUtils.isEmpty(workAllocation.getArchivedList())) {
//                workAllocation.getArchivedList().forEach(role -> {
//                    role.setArchivedAt(currentMillis);
//                });
//            }
//        } else if (Constants.UPDATE.equals(type)) {
//            Map<String, Set<String>> exActiveRoleAndChildIds = new HashMap<>();
//            Map<String, Set<String>> exArchivedRoleAndChildIds = new HashMap<>();
//            if (!CollectionUtils.isEmpty(existingRecord.getActiveList())) {
//                for (Role exRole : existingRecord.getActiveList()) {
//                    exActiveRoleAndChildIds.put(exRole.getId(), exRole.getChildNodes().stream().map(ChildNode::getId).collect(Collectors.toSet()));
//                }
//            }
//            if (!CollectionUtils.isEmpty(existingRecord.getArchivedList())) {
//                for (Role exRole : existingRecord.getArchivedList()) {
//                    Set<String> exArchChildIds = exRole.getChildNodes().stream().filter(event -> !StringUtils.isEmpty(event.getId())).map(ChildNode::getId).collect(Collectors.toSet());
//                    if(!CollectionUtils.isEmpty(exArchChildIds))
//                    exArchivedRoleAndChildIds.put(exRole.getId(), exArchChildIds);
//                }
//            }
//            if (!CollectionUtils.isEmpty(workAllocation.getActiveList())) {
//                for (Role activeRole : workAllocation.getActiveList()) {
//                    if (CollectionUtils.isEmpty(exActiveRoleAndChildIds.keySet()) || !exActiveRoleAndChildIds.keySet().contains(activeRole.getId())) {
//                        activeRole.setAddedAt(currentMillis);
//                        activeRole.setUpdatedAt(currentMillis);
//                        activeRole.setUpdatedBy(updatedBy);
//                    } else {
//                        if (!CollectionUtils.isEmpty(activeRole.getChildNodes())) {
//                            Set<String> childIds = activeRole.getChildNodes().stream().map(ChildNode::getId).collect(Collectors.toSet());
//                            if (!(childIds.containsAll(exActiveRoleAndChildIds.get(activeRole.getId())) && exActiveRoleAndChildIds.get(activeRole.getId()).containsAll(childIds))) {
//                                activeRole.setUpdatedAt(currentMillis);
//                                activeRole.setUpdatedBy(updatedBy);
//                            }
//                        } else if (CollectionUtils.isEmpty(activeRole.getChildNodes()) && !CollectionUtils.isEmpty(exActiveRoleAndChildIds.get(activeRole.getId()))) {
//                            activeRole.setUpdatedAt(currentMillis);
//                            activeRole.setUpdatedBy(updatedBy);
//                        }
//                    }
//
//                }
//            }
//            if (!CollectionUtils.isEmpty(workAllocation.getArchivedList())) {
//                for (Role archivedRole : workAllocation.getArchivedList()) {
//                    if (CollectionUtils.isEmpty(exArchivedRoleAndChildIds.keySet()) || !exArchivedRoleAndChildIds.keySet().contains(archivedRole.getId())) {
//                        archivedRole.setArchivedAt(currentMillis);
//                    } else {
//                        if (!CollectionUtils.isEmpty(archivedRole.getChildNodes())) {
//                            Set<String> childIds = archivedRole.getChildNodes().stream().filter(entity -> !StringUtils.isEmpty(entity.getId())).map(ChildNode::getId).collect(Collectors.toSet());
//                            if (!(childIds.containsAll(exArchivedRoleAndChildIds.get(archivedRole.getId())) && exArchivedRoleAndChildIds.get(archivedRole.getId()).containsAll(childIds))) {
//                                archivedRole.setArchivedAt(currentMillis);
//                            }
//                        } else if (CollectionUtils.isEmpty(archivedRole.getChildNodes()) && !CollectionUtils.isEmpty(exArchivedRoleAndChildIds.get(archivedRole.getId()))) {
//                            archivedRole.setArchivedAt(currentMillis);
//                        }
//                    }
//                }
//            }
//
//        }

    }
}
