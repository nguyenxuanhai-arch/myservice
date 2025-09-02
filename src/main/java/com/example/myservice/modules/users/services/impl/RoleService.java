package com.example.myservice.modules.users.services.impl;

import com.example.myservice.modules.users.entities.Permission;
import com.example.myservice.modules.users.entities.Role;
import com.example.myservice.modules.users.mapper.RoleMapper;
import com.example.myservice.modules.users.repositories.PermissionRepository;
import com.example.myservice.modules.users.repositories.RoleRepository;
import com.example.myservice.modules.users.requests.PermissionCreationRequest;
import com.example.myservice.modules.users.requests.Role.StoreRequest;
import com.example.myservice.modules.users.requests.Role.UpdateRequest;
import com.example.myservice.modules.users.resources.PermissionResource;
import com.example.myservice.modules.users.resources.RoleResource;
import com.example.myservice.modules.users.services.interfaces.RoleServiceInterface;
import com.example.myservice.services.BaseService;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.example.myservice.helps.FilterParameter;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.example.myservice.specifications.BaseSpecification;
import org.springframework.data.jpa.domain.Specification;

@Service
public class RoleService extends BaseService implements RoleServiceInterface {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    private static final Logger logger = LoggerFactory.getLogger(RoleService.class);
    @Autowired
    private RoleMapper roleMapper;


    @Override
    public Page<Role> paginate(Map<String, String[]> parameters) {
        int page = parameters.containsKey("page") ? Integer.parseInt(parameters.get("page")[0]) : 1;
        int perPage = parameters.containsKey("perPage") ? Integer.parseInt(parameters.get("perPage")[0]) : 20;
        String sortParam = parameters.containsKey("sort") ? parameters.get("sort")[0] : null;
        Sort sort = createSort(sortParam);

        String keyword = FilterParameter.filtertKeyword(parameters);
        Map<String, String> filterSimple = FilterParameter.filterSimple(parameters);
        Map<String, Map<String, String>> filterComplex = FilterParameter.filterComplex(parameters);

        logger.info("keyword" + keyword);
        logger.info("filterSimple: {}",filterSimple);
        logger.info("filterComple: {}" , filterComplex);

       Specification<Role> specification = Specification.where(
            BaseSpecification.<Role>keyword(keyword, "name"))
               .and(BaseSpecification.<Role>whereSpec(filterSimple)
                       .and(BaseSpecification.<Role>complexWhereSpec(filterComplex)));

        Pageable pageable = PageRequest.of(page - 1, perPage, sort);
        return roleRepository.findAll(specification ,pageable);
    }

    @Override
    public RoleResource findById(Long id) {
        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nhóm thành viên không tồn tại"));

        return roleMapper.tResource(role);
    }

    @Override
    @Transactional
    public Role create(StoreRequest request) {
        try {
            Role payload = Role.builder()
                    .name(request.getName())
                    .priority((request.getPriority()))
                    .build();
            return roleRepository.save(payload);
        } catch (Exception e) {
            throw new RuntimeException("Transaction failed" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public Role update(Long id, UpdateRequest request) {

        Role role = roleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nhóm thành viên không tồn tại"));

        Role payload = role.toBuilder()
                .name(request.getName())
                .priority(request.getPriority())
                .build();

        return roleRepository.save(payload);
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
        return roleMapper.toRoleResource(saved);
    }
}
