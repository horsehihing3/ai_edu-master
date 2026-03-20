package com.edu.platform.controller;

import com.edu.platform.common.ResponseMessage;
import com.edu.platform.domain.SystemCode;
import com.edu.platform.dto.common.ApiResponse;
import com.edu.platform.service.SystemCodeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/codes")
@RequiredArgsConstructor
public class SystemCodeController {

    private final SystemCodeService systemCodeService;

    /** 코드 트리 조회 (프론트엔드 사용 - 활성 코드만) */
    @GetMapping("/tree")
    public ResponseEntity<ApiResponse<List<SystemCode>>> getTree(
            @RequestParam(required = false) String group) {
        return ResponseEntity.ok(ApiResponse.success(systemCodeService.getTree(group)));
    }

    /** 그룹별 코드 목록 (평면) */
    @GetMapping
    public ResponseEntity<ApiResponse<List<SystemCode>>> getByGroup(
            @RequestParam(required = false) String group) {
        return ResponseEntity.ok(ApiResponse.success(systemCodeService.getByGroup(group)));
    }

    /** 루트 코드 목록 */
    @GetMapping("/roots")
    public ResponseEntity<ApiResponse<List<SystemCode>>> getRoots(
            @RequestParam(required = false) String group) {
        return ResponseEntity.ok(ApiResponse.success(systemCodeService.getRoots(group)));
    }

    /** 자식 코드 목록 */
    @GetMapping("/{parentCodeId}/children")
    public ResponseEntity<ApiResponse<List<SystemCode>>> getChildren(
            @PathVariable Long parentCodeId) {
        return ResponseEntity.ok(ApiResponse.success(systemCodeService.getChildren(parentCodeId)));
    }

    /** 관리자: 전체 코드 조회 (비활성 포함) */
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<List<SystemCode>>> getAllForAdmin(
            @RequestParam(required = false) String group) {
        return ResponseEntity.ok(ApiResponse.success(systemCodeService.getAllForAdmin(group)));
    }

    /** 관리자: 코드 그룹 목록 */
    @GetMapping("/admin/groups")
    public ResponseEntity<ApiResponse<List<String>>> getGroups() {
        return ResponseEntity.ok(ApiResponse.success(systemCodeService.getGroups()));
    }

    /** 관리자: 코드 단건 조회 */
    @GetMapping("/admin/{codeId}")
    public ResponseEntity<ApiResponse<SystemCode>> getById(@PathVariable Long codeId) {
        return ResponseEntity.ok(ApiResponse.success(systemCodeService.getById(codeId)));
    }

    /** 관리자: 코드 생성 */
    @PostMapping("/admin")
    public ResponseEntity<ApiResponse<SystemCode>> create(@RequestBody SystemCode code) {
        SystemCode created = systemCodeService.create(code);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.CODE_CREATED, created));
    }

    /** 관리자: 코드 수정 */
    @PutMapping("/admin/{codeId}")
    public ResponseEntity<ApiResponse<SystemCode>> update(
            @PathVariable Long codeId,
            @RequestBody SystemCode code) {
        SystemCode updated = systemCodeService.update(codeId, code);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.CODE_UPDATED, updated));
    }

    /** 관리자: 활성/비활성 */
    @PatchMapping("/admin/{codeId}/active")
    public ResponseEntity<ApiResponse<Void>> toggleActive(
            @PathVariable Long codeId,
            @RequestBody Map<String, Boolean> body) {
        systemCodeService.toggleActive(codeId, Boolean.TRUE.equals(body.get("isActive")));
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.CODE_UPDATED));
    }

    /** 관리자: 코드 삭제 */
    @DeleteMapping("/admin/{codeId}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long codeId) {
        systemCodeService.delete(codeId);
        return ResponseEntity.ok(ApiResponse.success(ResponseMessage.CODE_DELETED));
    }
}
