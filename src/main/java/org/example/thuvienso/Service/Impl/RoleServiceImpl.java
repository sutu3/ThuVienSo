package org.example.thuvienso.Service.Impl;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.example.thuvienso.Dto.Request.RoleRequest;
import org.example.thuvienso.Dto.Response.Role.RoleResponse;
import org.example.thuvienso.Exception.AppException;
import org.example.thuvienso.Exception.ErrorCode;
import org.example.thuvienso.Mapper.RoleMapper;
import org.example.thuvienso.Module.RoleEntity;
import org.example.thuvienso.Repo.RoleRepo;
import org.example.thuvienso.Service.RoleService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleServiceImpl implements RoleService {
    private final RoleRepo roleRepo;
    RoleMapper roleMapper;

    @Override
    public RoleResponse createRole(RoleRequest request) {
        RoleEntity role=roleMapper.toEntity(request);
        if(roleRepo.findByRoleName(request.getRoleName())==null){
            throw new AppException(ErrorCode.ROLE_NOT_FOUND);
        }
        role.setCreatedAt(LocalDateTime.now());
        role.setIsDeleted(false);
        return roleMapper.toResponse(role);
    }

    @Override
    public List<RoleResponse> getAllRole() {
        return roleRepo.findAll().stream()
                .map(roleMapper::toResponse)
                .collect(Collectors.toList());
    }
}
