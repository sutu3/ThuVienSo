package org.example.thuvienso.Service;

import org.example.thuvienso.Dto.Request.RoleRequest;
import org.example.thuvienso.Dto.Response.Role.RoleResponse;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface RoleService {
    RoleResponse createRole(RoleRequest request);
    List<RoleResponse> getAllRole();

}
