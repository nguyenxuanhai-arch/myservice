package com.example.myservice.modules.users.services.impl;

import com.example.myservice.modules.users.mapper.PermissionMapper;
import com.example.myservice.modules.users.resources.PermissionResource;
import com.example.myservice.modules.users.entities.Permission;
import com.example.myservice.modules.users.repositories.PermissionRepository;
import com.example.myservice.modules.users.requests.Permission.PermissionCreationRequest;
import com.example.myservice.modules.users.requests.Permission.PermissionUpdationRequest;
import com.example.myservice.modules.users.services.interfaces.PermissionServiceInterface;
import com.example.myservice.services.BaseService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class PermissionService extends BaseService implements PermissionServiceInterface {
    private final PermissionRepository permissionRepository;
    private final PermissionMapper permissionMapper;

    @Override
    public Permission create(PermissionCreationRequest request) {
        try {
            Permission payload = permissionMapper.tEntity(request);
            return permissionRepository.save(payload);
        } catch (Exception e) {
            throw new RuntimeException("Transaction failed" + e.getMessage());
        }
    }

    @Override
    public List<Permission> getAll(Map<String, String[]> parameters) {
        Sort sort = sortParam(parameters);

        Specification<Permission> specification = specificationParam(parameters);

        return permissionRepository.findAll(specification ,sort);
    }

    @Override
    public Permission update(Long id, PermissionUpdationRequest request) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Quyền người dùng không tồn tại với id: " + id));
        return permissionRepository.save(permission);
    }

    @Override
    public Page<Permission> paginate(Map<String, String[]> parameters) {
        int page = parameters.containsKey("page") ? Integer.parseInt(parameters.get("page")[0]) : 1;
        int perPage = parameters.containsKey("perPage") ? Integer.parseInt(parameters.get("perPage")[0]) : 20;
        Sort sort = sortParam(parameters);

        Specification<Permission> specification = specificationParam(parameters);

        Pageable pageable = PageRequest.of(page - 1, perPage, sort);
        return permissionRepository.findAll(specification ,pageable);
    }

    @Override
    public void delete(Long id) {
        permissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Quyền người dùng không tồn tại với id: " + id));
        permissionRepository.deleteById(id);
    }

    @Override
    public PermissionResource findById(Long id) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Quyền người dùng không tồn tại với id: " + id));
        return permissionMapper.tResource(permission);
    }
}
