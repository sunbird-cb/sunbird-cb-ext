package org.sunbird.portal.department.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.sunbird.common.model.Response;
import org.sunbird.core.exception.BadRequestException;
import org.sunbird.portal.department.PortalConstants;
import org.sunbird.portal.department.dto.UserDepartmentRole;
import org.sunbird.portal.department.model.DepartmentInfo;
import org.sunbird.portal.department.model.DeptPublicInfo;
import org.sunbird.portal.department.model.SearchUserInfo;
import org.sunbird.portal.department.model.UserDepartmentInfo;
import org.sunbird.portal.department.service.MdoPortalService;
import org.sunbird.portal.department.service.PortalService;
import org.sunbird.portal.department.service.SpvPortalService;

@RestController
public class PortalController {
    @Autowired
    PortalService portalService;

    @Autowired
    SpvPortalService spvPortalService;

    @Autowired
    MdoPortalService mdoPortalService;

    // ----------------- Public APIs --------------------
    @GetMapping("/portal/listDeptNames")
    public ResponseEntity<List<String>> getDeptNameList() {
        return new ResponseEntity<>(portalService.getDeptNameList(), HttpStatus.OK);
    }

    @GetMapping("/portal/getAllDept")
    public ResponseEntity<List<DeptPublicInfo>> getAllDepartment() {
        return new ResponseEntity<>(portalService.getAllDept(), HttpStatus.OK);
    }

    @GetMapping("/portal/deptSearch")
    public ResponseEntity<DeptPublicInfo> searchDepartment(@RequestParam(name = "friendlyName", required = true) String deptName) {
        return new ResponseEntity<>(portalService.searchDept(deptName), HttpStatus.OK);
    }

    // ----------------- END of Public APIs --------------------
    // ----------------- SPV APIs -----------------------
    @GetMapping("/portal/spv/isAdmin")
    public ResponseEntity<Boolean> isSpvAdmin(@RequestHeader("wid") String wid) {
        return new ResponseEntity<>(
                portalService.isAdmin(PortalConstants.SPV_DEPT_TYPE, PortalConstants.SPV_ROLE_NAME, wid),
                HttpStatus.OK);
    }

    @GetMapping("/portal/spv/mydepartment")
    public ResponseEntity<DepartmentInfo> getMyDepartments(@RequestHeader("wid") String wid,
                                                           @RequestParam(name = "allUsers", required = false) boolean isUserInfoRequired,
                                                           @RequestHeader("rootOrg") String rootOrg) throws Exception {
        validateUserAccess(PortalConstants.SPV_DEPT_TYPE, PortalConstants.SPV_ROLE_NAME, wid);
        return new ResponseEntity<>(spvPortalService.getMyDepartment(wid, isUserInfoRequired, rootOrg), HttpStatus.OK);
    }

    @PostMapping("/portal/spv/department")
    public ResponseEntity<DepartmentInfo> addDepartment(@RequestHeader("xAuthUser") String authUserToken, @RequestHeader("wid") String wid,
                                                        @RequestBody DepartmentInfo deptInfo, @RequestHeader("rootOrg") String rootOrg) throws Exception {
        validateUserAccess(PortalConstants.SPV_DEPT_TYPE, PortalConstants.SPV_ROLE_NAME, wid);
        return new ResponseEntity<>(spvPortalService.addDepartment(authUserToken, wid, deptInfo, rootOrg),
                HttpStatus.OK);
    }

    @PatchMapping("/portal/spv/department")
    public ResponseEntity<DepartmentInfo> updateDepartment(@RequestHeader("wid") String wid,
                                                           @RequestBody DepartmentInfo deptInfo, @RequestHeader("rootOrg") String rootOrg) throws Exception {
        validateUserAccess(PortalConstants.SPV_DEPT_TYPE, PortalConstants.SPV_ROLE_NAME, wid);
        return new ResponseEntity<>(spvPortalService.updateDepartment(deptInfo, rootOrg), HttpStatus.OK);
    }

    @GetMapping("/portal/spv/department")
    public ResponseEntity<List<DepartmentInfo>> getAllDepartments(@RequestHeader("wid") String wid,
                                                                  @RequestHeader("rootOrg") String rootOrg) throws Exception {
        validateUserAccess(PortalConstants.SPV_DEPT_TYPE, PortalConstants.SPV_ROLE_NAME, wid);
        return new ResponseEntity<>(spvPortalService.getAllDepartments(rootOrg), HttpStatus.OK);
    }

    @GetMapping("/portal/spv/department/{dept_id}")
    public ResponseEntity<DepartmentInfo> getDepartmentById(@PathVariable("dept_id") Integer deptId,
                                                            @RequestParam(name = "allUsers", required = false) boolean isUserInfoRequired,
                                                            @RequestHeader("wid") String wid, @RequestHeader("rootOrg") String rootOrg) throws Exception {
        validateUserAccess(PortalConstants.SPV_DEPT_TYPE, PortalConstants.SPV_ROLE_NAME, wid);
        return new ResponseEntity<>(
                spvPortalService.getDepartmentById(deptId, isUserInfoRequired, rootOrg), HttpStatus.OK);
    }

    @PostMapping("/portal/spv/userrole")
    public ResponseEntity<UserDepartmentInfo> addUserRoleInDepartmentBySPV(@RequestBody UserDepartmentRole userDeptRole,
                                                                           @RequestHeader("rootOrg") String rootOrg, @RequestHeader("org") String org,
                                                                           @RequestHeader("wid") String wid) throws Exception {
        validateUserAccess(PortalConstants.SPV_DEPT_TYPE, PortalConstants.SPV_ROLE_NAME, wid);
        return new ResponseEntity<>(
                portalService.addUserRoleInDepartment(userDeptRole, wid, rootOrg, org), HttpStatus.OK);
    }

    @PatchMapping("/portal/spv/userrole")
    public ResponseEntity<UserDepartmentInfo> updateUserRoleInDepartmentBySPV(
            @RequestBody UserDepartmentRole userDeptRole, @RequestHeader("rootOrg") String rootOrg,
            @RequestHeader("org") String org, @RequestHeader("wid") String wid) throws Exception {
        validateUserAccess(PortalConstants.SPV_DEPT_TYPE, PortalConstants.SPV_ROLE_NAME, wid);
        return new ResponseEntity<>(
                portalService.updateUserRoleInDepartment(userDeptRole, wid, rootOrg, org), HttpStatus.OK);
    }

    @GetMapping("/portal/spv/role")
    public ResponseEntity<List<DepartmentInfo>> getSpvRoles(@RequestHeader("rootOrg") String rootOrg) {
        return new ResponseEntity<>(portalService.getAllDepartments(rootOrg), HttpStatus.OK);
    }

    @DeleteMapping("/portal/spv/{dept_id}")
    public ResponseEntity<Response> deleteDepartment(@PathVariable("dept_id") Integer deptId, @RequestHeader("wid") String wid) {
        validateUserAccess(PortalConstants.SPV_DEPT_TYPE, PortalConstants.SPV_ROLE_NAME, wid);
        return new ResponseEntity<>(spvPortalService.deleteDepartment(wid, deptId), HttpStatus.OK);
    }

    // ----------------- SPV APIs -----------------------

    // ----------------- FRAC APIs ----------------------
    @GetMapping("/portal/frac/mydepartment")
    public ResponseEntity<DepartmentInfo> getMyFracDepartment(@RequestHeader("wid") String wid,
                                                              @RequestParam(name = "allUsers", required = false) boolean isUserInfoRequired,
                                                              @RequestHeader("rootOrg") String rootOrg) throws Exception {
        if (!portalService.validateUserLogin(wid, PortalConstants.FRAC_ROLES, PortalConstants.MDO_DEPT_TYPE)) {
            throw new BadRequestException("User is not assigned with any FRAC related roles.");
        }
        return new ResponseEntity<>(mdoPortalService.getMyFracDepartment(wid, isUserInfoRequired, rootOrg), HttpStatus.OK);
    }
    // ----------------- END OF FRAC APIs ---------------

    // ----------------- MDO APIs -----------------------
    @GetMapping("/portal/mdo/isAdmin")
    public ResponseEntity<Boolean> isMdoAdmin(@RequestHeader("wid") String wid) {
        return new ResponseEntity<>(
                portalService.isAdmin(PortalConstants.MDO_DEPT_TYPE, PortalConstants.MDO_ROLE_NAME, wid),
                HttpStatus.OK);
    }

    @GetMapping("/portal/mdo/mydepartment")
    public ResponseEntity<DepartmentInfo> getMyMdoDepartment(@RequestHeader("wid") String wid,
                                                             @RequestParam(name = "allUsers", required = false) boolean isUserInfoRequired,
                                                             @RequestHeader("rootOrg") String rootOrg) throws Exception {
        if (!portalService.validateUserLogin(wid, PortalConstants.MDO_ROLES, PortalConstants.MDO_DEPT_TYPE)) {
            throw new BadRequestException("User is not assigned with any MDO related roles.");
        }
        return new ResponseEntity<>(mdoPortalService.getMyDepartment(wid, isUserInfoRequired, rootOrg), HttpStatus.OK);
    }

    @PatchMapping("/portal/mdo/department")
    public ResponseEntity<DepartmentInfo> updateMdoDepartment(@RequestHeader("wid") String wid,
                                                              @RequestBody DepartmentInfo deptInfo, @RequestHeader("rootOrg") String rootOrg) throws Exception {
        validateUserAccess(PortalConstants.MDO_DEPT_TYPE, PortalConstants.MDO_ROLE_NAME, wid, deptInfo.getId());
        return new ResponseEntity<>(mdoPortalService.updateDepartment(deptInfo, rootOrg), HttpStatus.OK);
    }

    @PostMapping("/portal/mdo/userrole")
    public ResponseEntity<UserDepartmentInfo> addUserRoleInDepartmentByMDO(@RequestBody UserDepartmentRole userDeptRole,
                                                                           @RequestHeader("rootOrg") String rootOrg, @RequestHeader("org") String org,
                                                                           @RequestHeader("wid") String wid) throws Exception {
        validateUserAccess(PortalConstants.MDO_DEPT_TYPE, PortalConstants.MDO_ROLE_NAME, wid, userDeptRole.getDeptId());
        return new ResponseEntity<>(
                portalService.addUserRoleInDepartment(userDeptRole, wid, rootOrg, org), HttpStatus.OK);
    }

    @PatchMapping("/portal/mdo/userrole")
    public ResponseEntity<UserDepartmentInfo> updateUserRoleInDepartmentByMDO(
            @RequestBody UserDepartmentRole userDeptRole, @RequestHeader("rootOrg") String rootOrg,
            @RequestHeader("org") String org, @RequestHeader("wid") String wid) throws Exception {
        validateUserAccess(PortalConstants.MDO_DEPT_TYPE, PortalConstants.MDO_ROLE_NAME, wid, userDeptRole.getDeptId());
        return new ResponseEntity<>(
                portalService.updateUserRoleInDepartment(userDeptRole, wid, rootOrg, org), HttpStatus.OK);
    }

    // ----------------- MDO APIs -----------------------

    // ----------------- CBP APIs -----------------------
    @GetMapping("/portal/cbp/isAdmin")
    public ResponseEntity<Boolean> isCBPAdmin(@RequestHeader("wid") String wid) {
        return new ResponseEntity<>(portalService.validateCBPUserLogin(wid), HttpStatus.OK);
    }

    @GetMapping("/portal/cbp/mydepartment")
    public ResponseEntity<DepartmentInfo> getMyCbpDepartment(@RequestHeader("wid") String wid,
                                                             @RequestHeader("rootOrg") String rootOrg) throws Exception {
        validateCBPUserAccess(wid);
        return new ResponseEntity<>(portalService.getMyCbpDepartment(wid, rootOrg), HttpStatus.OK);
    }

    @PatchMapping("/portal/cbp/department")
    public ResponseEntity<DepartmentInfo> updateCbpDepartment(@RequestHeader("wid") String wid,
                                                              @RequestBody DepartmentInfo deptInfo, @RequestHeader("rootOrg") String rootOrg) throws Exception {
        validateUserAccess(PortalConstants.CBP_DEPT_TYPE, PortalConstants.CBP_ROLE_NAME, wid);
        return new ResponseEntity<>(mdoPortalService.updateDepartment(deptInfo, rootOrg), HttpStatus.OK);
    }

    @PostMapping("/portal/cbp/userrole")
    public ResponseEntity<UserDepartmentInfo> addUserRoleInDepartmentByCBP(@RequestBody UserDepartmentRole userDeptRole,
                                                                           @RequestHeader("rootOrg") String rootOrg, @RequestHeader("org") String org,
                                                                           @RequestHeader("wid") String wid) throws Exception {
        validateUserAccess(PortalConstants.CBP_DEPT_TYPE, PortalConstants.CBP_ROLE_NAME, wid, userDeptRole.getDeptId());
        return new ResponseEntity<>(
                portalService.addUserRoleInDepartment(userDeptRole, wid, rootOrg, org), HttpStatus.OK);
    }

    @PatchMapping("/portal/cbp/userrole")
    public ResponseEntity<UserDepartmentInfo> updateUserRoleInDepartmentByCBP(
            @RequestBody UserDepartmentRole userDeptRole, @RequestHeader("rootOrg") String rootOrg,
            @RequestHeader("org") String org, @RequestHeader("wid") String wid) throws Exception {
        validateUserAccess(PortalConstants.CBP_DEPT_TYPE, PortalConstants.CBP_ROLE_NAME, wid, userDeptRole.getDeptId());
        return new ResponseEntity<>(
                portalService.updateUserRoleInDepartment(userDeptRole, wid, rootOrg, org), HttpStatus.OK);
    }

    @GetMapping("/portal/cbp/searchUser/deptId/{dept_id}/role/{role_name}/userlike/{username}")
    public ResponseEntity<List<SearchUserInfo>> searchUserForRole(@PathVariable("dept_id") Integer deptId,
                                                                  @PathVariable("role_name") String roleName, @PathVariable("username") String userName) throws Exception {
        return new ResponseEntity<>(portalService.searchUserForRole(deptId, roleName, userName), HttpStatus.OK);
    }
    // ----------------- END OF CBP APIs -----------------------

    // ----------------- CBC APIs -----------------------
    @GetMapping("/portal/cbc/isAdmin")
    public ResponseEntity<Boolean> isCBCAdmin(@RequestHeader("wid") String wid) {
        return new ResponseEntity<>(
                portalService.isAdmin(PortalConstants.CBC_DEPT_TYPE, PortalConstants.CBC_ROLE_NAME, wid),
                HttpStatus.OK);
    }

    @GetMapping("/portal/cbc/mydepartment")
    public ResponseEntity<DepartmentInfo> getMyCBCDepartment(@RequestHeader("wid") String wid,
                                                             @RequestParam(name = "allUsers", required = false) boolean isUserInfoRequired,
                                                             @RequestHeader("rootOrg") String rootOrg) throws Exception {
        validateUserLoginForDepartment(wid, PortalConstants.CBC_DEPT_TYPE);
        return new ResponseEntity<>(mdoPortalService.getMyCBCDepartment(wid, isUserInfoRequired, rootOrg), HttpStatus.OK);
    }

    @PatchMapping("/portal/cbc/department")
    public ResponseEntity<DepartmentInfo> updateCBCDepartment(@RequestHeader("wid") String wid,
                                                              @RequestBody DepartmentInfo deptInfo, @RequestHeader("rootOrg") String rootOrg) throws Exception {
        validateUserAccess(PortalConstants.CBC_DEPT_TYPE, PortalConstants.CBC_ROLE_NAME, wid, deptInfo.getId());
        return new ResponseEntity<>(mdoPortalService.updateDepartment(deptInfo, rootOrg), HttpStatus.OK);
    }

    @PostMapping("/portal/cbc/userrole")
    public ResponseEntity<UserDepartmentInfo> addUserRoleInDepartmentByCBC(@RequestBody UserDepartmentRole userDeptRole,
                                                                           @RequestHeader("rootOrg") String rootOrg, @RequestHeader("org") String org,
                                                                           @RequestHeader("wid") String wid) throws Exception {
        validateUserAccess(PortalConstants.CBC_DEPT_TYPE, PortalConstants.CBC_ROLE_NAME, wid, userDeptRole.getDeptId());
        return new ResponseEntity<>(
                portalService.addUserRoleInDepartment(userDeptRole, wid, rootOrg, org), HttpStatus.OK);
    }

    @PatchMapping("/portal/cbc/userrole")
    public ResponseEntity<UserDepartmentInfo> updateUserRoleInDepartmentByCBC(
            @RequestBody UserDepartmentRole userDeptRole, @RequestHeader("rootOrg") String rootOrg,
            @RequestHeader("org") String org, @RequestHeader("wid") String wid) throws Exception {
        validateUserAccess(PortalConstants.CBC_DEPT_TYPE, PortalConstants.CBC_ROLE_NAME, wid, userDeptRole.getDeptId());
        return new ResponseEntity<>(
                portalService.updateUserRoleInDepartment(userDeptRole, wid, rootOrg, org), HttpStatus.OK);
    }

    @GetMapping("/portal/cbc/department")
    public ResponseEntity<List<DepartmentInfo>> getAllDepartmentsForCBC(@RequestHeader("wid") String wid,
                                                                        @RequestHeader("rootOrg") String rootOrg) throws Exception {
        validateUserLoginForDepartment(wid, PortalConstants.CBC_DEPT_TYPE);
        return new ResponseEntity<>(spvPortalService.getAllDepartments(rootOrg), HttpStatus.OK);
    }

    @GetMapping("/portal/cbc/department/{dept_id}")
    public ResponseEntity<DepartmentInfo> getDepartmentByIdForCBC(@PathVariable("dept_id") Integer deptId,
                                                                  @RequestParam(name = "allUsers", required = false) boolean isUserInfoRequired,
                                                                  @RequestHeader("wid") String wid, @RequestHeader("rootOrg") String rootOrg) throws Exception {
        validateUserAccess(PortalConstants.CBC_DEPT_TYPE, PortalConstants.CBC_ROLE_NAME, wid);
        return new ResponseEntity<>(
                spvPortalService.getDepartmentById(deptId, isUserInfoRequired, rootOrg), HttpStatus.OK);
    }

    // ----------------- CBC APIs Ends-----------------------

    @GetMapping("/portal/department/{dept_id}")
    public ResponseEntity<DepartmentInfo> getDepartmentById(@PathVariable("dept_id") Integer deptId,
                                                            @RequestParam(name = "allUsers", required = false) boolean isUserInfoRequired,
                                                            @RequestHeader("rootOrg") String rootOrg) {
        return new ResponseEntity<>(portalService.getDepartmentById(deptId, isUserInfoRequired, rootOrg),
                HttpStatus.OK);
    }

    @GetMapping("/portal/mydepartment")
    public ResponseEntity<DepartmentInfo> getMyDepartmentDetails(@RequestHeader("wid") String userId,
                                                                 @RequestParam(name = "allUsers", required = false) boolean isUserInfoRequired) throws Exception {
        return new ResponseEntity<>(portalService.getMyDepartmentDetails(userId, isUserInfoRequired),
                HttpStatus.OK);
    }

    @PatchMapping("/portal/department")
    public ResponseEntity<DepartmentInfo> updateDepartment(@RequestBody DepartmentInfo deptInfo,
                                                           @RequestHeader("rootOrg") String rootOrg) throws Exception {
        return new ResponseEntity<>(portalService.updateDepartment(deptInfo, rootOrg), HttpStatus.OK);
    }

    @PostMapping("/portal/userrole")
    public ResponseEntity<UserDepartmentInfo> updateUserRoleInDepartment(
            @Valid @RequestBody UserDepartmentRole userDeptRole, @RequestHeader("rootOrg") String rootOrg,
            @RequestHeader("org") String org, @RequestHeader("wid") String wid) throws Exception {
        return new ResponseEntity<>(
                portalService.addUserRoleInDepartment(userDeptRole, wid, rootOrg, org), HttpStatus.OK);
    }

//	@GetMapping("/portal/user/{user_id}/departmentRoles")
//	public ResponseEntity<List<UserDepartmentInfo>> getUserDepartments(@PathVariable("user_id") String userId) {
//		return new ResponseEntity<List<UserDepartmentInfo>>(portalService.getUserDepartments(userId), HttpStatus.OK);
//	}

    @GetMapping("/portal/department/{dept_id}/user/{user_id}/isAdmin")
    public ResponseEntity<Boolean> checkAdminPrivilage(@PathVariable("dept_id") Integer deptId,
                                                       @PathVariable("user_id") String userId, @RequestHeader("rootOrg") String rootOrg) throws Exception {
        return new ResponseEntity<>(portalService.checkAdminPrivilage(deptId, userId, rootOrg), HttpStatus.OK);
    }

//	@GetMapping("/portal/{dept_key}/isAdmin")
//	public ResponseEntity<Boolean> checkMDOAdminPrivilage(@PathVariable("dept_key") String deptKey,
//			@RequestHeader("wid") String wid) throws Exception {
//		return new ResponseEntity<Boolean>(portalService.checkMdoAdminPrivilage(deptKey, wid), HttpStatus.OK);
//	}

    private void validateUserAccess(String deptType, String roleName, String wid) {
        validateUserAccess(deptType, roleName, wid, -1);
    }

    private void validateUserAccess(String deptType, String roleName, String wid, Integer deptId) throws BadRequestException {
        if (!portalService.isAdmin(deptType, roleName, wid)) {
            throw new BadRequestException("User is not assigned with Role: '" + roleName + "'.");
        }
    }

    private void validateCBPUserAccess(String userId) throws BadRequestException {
        if (!portalService.validateCBPUserLogin(userId)) {
            throw new BadRequestException("User is not assigned with any CBP related roles.");
        }
    }

    private void validateUserLoginForDepartment(String userId, String departmentType) throws BadRequestException {
        if (!portalService.validateUserLoginForDepartment(userId, departmentType)) {
            throw new BadRequestException("User is not assigned with any " + departmentType + " related roles.");
        }
    }

    @GetMapping("/portal/isUserActive")
    public ResponseEntity<Boolean> isUserActive(@RequestHeader("userId") String userId) {
        return new ResponseEntity<>(portalService.isUserActive(userId), HttpStatus.OK);
    }

}