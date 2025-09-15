package com.example.myservice.modules.users.services.impl;

import com.example.myservice.modules.users.entities.Permission;
import com.example.myservice.modules.users.entities.Role;
import com.example.myservice.modules.users.mapper.RoleMapper;
import com.example.myservice.modules.users.repositories.PermissionRepository;
import com.example.myservice.modules.users.repositories.RoleRepository;
import com.example.myservice.modules.users.requests.Role.RoleCreationRequest;
import com.example.myservice.modules.users.requests.Role.RoleUpdationRequest;
import com.example.myservice.modules.users.resources.RoleDetailsResource;
import com.example.myservice.modules.users.resources.RoleResource;
import com.example.myservice.modules.users.services.interfaces.RoleServiceInterface;
import com.example.myservice.services.BaseService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.data.jpa.domain.Specification;

@RequiredArgsConstructor
@Service
public class RoleService extends BaseService implements RoleServiceInterface {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;

    @Override
    public List<Role> getAll(Map<String, String[]> parameters) {
        Sort sort = sortParam(parameters);
        Specification<Role> specification = specificationParam(parameters);
        return roleRepository.findAll(specification ,sort);
    }

    @Override
    public Page<Role> paginate(Map<String, String[]> parameters) {
        int page = parameters.containsKey("page") ? Integer.parseInt(parameters.get("page")[0]) : 1;
        int perPage = parameters.containsKey("perPage") ? Integer.parseInt(parameters.get("perPage")[0]) : 20;
        Sort sort = sortParam(parameters);
        Specification<Role> specification = specificationParam(parameters);
        Pageable pageable = PageRequest.of(page - 1, perPage, sort);
        return roleRepository.findAll(specification ,pageable);
    }

    @Override
    public RoleDetailsResource findById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nhóm thành viên không tồn tại"));

        return roleMapper.tResourceDetails(role);
    }

    @Override
    @Transactional
    public Role create(RoleCreationRequest request) {
        try {
            Role payload = roleMapper.tEntity(request);
            return roleRepository.save(payload);
        } catch (Exception e) {
            throw new RuntimeException("Transaction failed" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Role update(Long id, RoleUpdationRequest request) {

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nhóm thành viên không tồn tại với id: " + id));
        return roleRepository.save(role);
    }

    @Override
    public void delete(Long id) {
        roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Quyền người dùng không tồn tại với id: " + id));
        roleRepository.deleteById(id);
    }

    @Override
    @Transactional
    public RoleResource updatePermissionsForRole(Long roleId, Set<Long> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new EntityNotFoundException("Role không tồn tại"));

        List<Permission> found = permissionRepository.findAllById(permissionIds);
        if (found.size() != permissionIds.size()) {
            Set<Long> foundIds = new HashSet<>();
            for (Permission p : found) foundIds.add(p.getId());
            Set<Long> missing = new HashSet<>(permissionIds);
            missing.removeAll(foundIds);
            throw new EntityNotFoundException("Permission không tồn tại: " + missing);
        }

        role.getPermissions().clear();
        role.getPermissions().addAll(found);

        Role saved = roleRepository.save(role);
        return roleMapper.tResource(saved);
    }
}
