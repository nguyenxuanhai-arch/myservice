package com.example.myservice.modules.users.services.impl;

import com.example.myservice.helps.FilterParameter;
import com.example.myservice.modules.users.entities.Permission;
import com.example.myservice.modules.users.repositories.PermissionRepository;
import com.example.myservice.modules.users.requests.Permission.PermissionCreationRequest;
import com.example.myservice.modules.users.requests.Permission.PermissionUpdationRequest;
import com.example.myservice.modules.users.services.interfaces.PermissionServiceInterface;
import com.example.myservice.services.BaseService;
import com.example.myservice.specifications.BaseSpecification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class PermissionService extends BaseService implements PermissionServiceInterface {
    private final PermissionRepository permissionRepository;

    @Override
    public Permission create(PermissionCreationRequest request) {
        try {
            Permission payload = Permission.builder()
                    .name(request.getName())
                    .description(request.getDescription())
                    .build();
            return permissionRepository.save(payload);
        } catch (Exception e) {
            throw new RuntimeException("Transaction failed" + e.getMessage());
        }
    }

    @Override
    public Permission update(Long id, PermissionUpdationRequest request) {
        Permission permission = permissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Quyền người dùng không tồn tại với id: " + id));
        Permission payload = permission.toBuilder()
                .name(request.getName())
                .description(request.getDescription())
                .build();
        return permissionRepository.save(payload);
    }

    @Override
    public Page<Permission> paginate(Map<String, String[]> parameters) {
        int page = parameters.containsKey("page") ? Integer.parseInt(parameters.get("page")[0]) : 1;
        int perPage = parameters.containsKey("perPage") ? Integer.parseInt(parameters.get("perPage")[0]) : 20;
        String sortParam = parameters.containsKey("sort") ? parameters.get("sort")[0] : null;
        Sort sort = createSort(sortParam);

        String keyword = FilterParameter.filtertKeyword(parameters);
        Map<String, String> filterSimple = FilterParameter.filterSimple(parameters);
        Map<String, Map<String, String>> filterComplex = FilterParameter.filterComplex(parameters);


        Specification<Permission> specification = Specification.where(
                        BaseSpecification.<Permission>keyword(keyword, "name"))
                .and(BaseSpecification.<Permission>whereSpec(filterSimple)
                        .and(BaseSpecification.<Permission>complexWhereSpec(filterComplex)));

        Pageable pageable = PageRequest.of(page - 1, perPage, sort);
        return permissionRepository.findAll(specification ,pageable);
    }

    @Override
    public void delete(Long id) {
        permissionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Quyền người dùng không tồn tại với id: " + id));
        permissionRepository.deleteById(id);
    }
}
