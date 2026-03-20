package com.edu.platform.service;

import com.edu.platform.domain.SystemCode;
import com.edu.platform.exception.BusinessException;
import com.edu.platform.exception.ErrorCode;
import com.edu.platform.mapper.SystemCodeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SystemCodeService {

    private final SystemCodeMapper systemCodeMapper;

    /** 특정 그룹 전체 코드 (활성) */
    @Transactional(readOnly = true)
    public List<SystemCode> getByGroup(String codeGroup) {
        return systemCodeMapper.findAll(codeGroup, null, true);
    }

    /** 루트(최상위) 코드 목록 */
    @Transactional(readOnly = true)
    public List<SystemCode> getRoots(String codeGroup) {
        return systemCodeMapper.findRoots(codeGroup, true);
    }

    /** 특정 부모의 자식 코드 목록 */
    @Transactional(readOnly = true)
    public List<SystemCode> getChildren(Long parentCodeId) {
        return systemCodeMapper.findChildren(parentCodeId, true);
    }

    /** 그룹별 계층 트리 (루트 → children 포함) */
    @Transactional(readOnly = true)
    public List<SystemCode> getTree(String codeGroup) {
        List<SystemCode> all = systemCodeMapper.findAll(codeGroup, null, true);
        Map<Long, SystemCode> map = all.stream()
                .collect(Collectors.toMap(SystemCode::getCodeId, c -> c));

        List<SystemCode> roots = all.stream()
                .filter(c -> c.getParentCodeId() == null)
                .collect(Collectors.toList());

        for (SystemCode code : all) {
            if (code.getParentCodeId() != null) {
                SystemCode parent = map.get(code.getParentCodeId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new java.util.ArrayList<>());
                    }
                    parent.getChildren().add(code);
                }
            }
        }
        return roots;
    }

    /** 관리자용 - 전체(비활성 포함) */
    @Transactional(readOnly = true)
    public List<SystemCode> getAllForAdmin(String codeGroup) {
        return systemCodeMapper.findAll(codeGroup, null, false);
    }

    /** 사용 가능한 코드 그룹 목록 */
    @Transactional(readOnly = true)
    public List<String> getGroups() {
        return systemCodeMapper.findDistinctGroups();
    }

    /** 단건 조회 */
    @Transactional(readOnly = true)
    public SystemCode getById(Long codeId) {
        return systemCodeMapper.findById(codeId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CODE_NOT_FOUND));
    }

    /** 코드 생성 */
    @Transactional
    public SystemCode create(SystemCode code) {
        long dup = systemCodeMapper.countByGroupAndValue(code.getCodeGroup(), code.getCodeValue(), null);
        if (dup > 0) {
            throw new BusinessException(ErrorCode.CODE_ALREADY_EXISTS);
        }
        if (code.getSortOrder() == null) code.setSortOrder(0);
        if (code.getIsActive() == null) code.setIsActive(true);
        systemCodeMapper.insert(code);
        return code;
    }

    /** 코드 수정 (codeName, parentCodeId, sortOrder, description만 변경 가능) */
    @Transactional
    public SystemCode update(Long codeId, SystemCode req) {
        SystemCode existing = getById(codeId);
        existing.setCodeName(req.getCodeName());
        existing.setParentCodeId(req.getParentCodeId());
        existing.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : existing.getSortOrder());
        existing.setDescription(req.getDescription());
        systemCodeMapper.update(existing);
        return existing;
    }

    /** 활성/비활성 토글 */
    @Transactional
    public void toggleActive(Long codeId, boolean isActive) {
        getById(codeId); // 존재 확인
        systemCodeMapper.updateActive(codeId, isActive);
    }

    /** 코드 삭제 (자식 코드 없을 때만) */
    @Transactional
    public void delete(Long codeId) {
        getById(codeId);
        List<SystemCode> children = systemCodeMapper.findChildren(codeId, false);
        if (!children.isEmpty()) {
            throw new BusinessException(ErrorCode.CODE_HAS_CHILDREN);
        }
        systemCodeMapper.delete(codeId);
    }
}
