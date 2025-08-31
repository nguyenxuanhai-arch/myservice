package com.example.myservice.modules.users.services.impl;

import com.example.myservice.modules.users.entities.Role;
import com.example.myservice.modules.users.repositories.RoleRepository;
import com.example.myservice.modules.users.requests.Role.StoreRequest;
import com.example.myservice.modules.users.requests.Role.UpdateRequest;
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
import java.util.Map;
import com.example.myservice.specifications.BaseSpecification;
import org.springframework.data.jpa.domain.Specification;

@Service
public class RoleService extends BaseService implements RoleServiceInterface {

    @Autowired
    private RoleRepository roleRepository;

    private static final Logger logger = LoggerFactory.getLogger(RoleService.class);


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
    @Transactional
    public Role create(StoreRequest request) {
        try {
            Role payload = Role.builder()
                    .name(request.getName())
                    .publish((request.getPublish()))
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
                .publish(request.getPublish())
                .build();

        return roleRepository.save(payload);
    }
}
