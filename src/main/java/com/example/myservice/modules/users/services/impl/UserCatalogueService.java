package com.example.myservice.modules.users.services.impl;

import com.example.myservice.modules.users.entities.UserCatalogue;
import com.example.myservice.modules.users.repositories.UserCatalogueRepository;
import com.example.myservice.modules.users.requests.UserCatalogue.StoreRequest;
import com.example.myservice.modules.users.requests.UserCatalogue.UpdateRequest;
import com.example.myservice.modules.users.services.interfaces.UserCatalogueInterface;
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
public class UserCatalogueService extends BaseService implements UserCatalogueInterface {

    @Autowired
    private UserCatalogueRepository userCatalogueRepository;

    private static final Logger logger = LoggerFactory.getLogger(UserCatalogueService.class);


    @Override
    public Page<UserCatalogue> paginate(Map<String, String[]> parameters) {
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

       Specification<UserCatalogue> specification = Specification.where(
            BaseSpecification.<UserCatalogue>keyword(keyword, "name"))
               .and(BaseSpecification.<UserCatalogue>whereSpec(filterSimple)
                       .and(BaseSpecification.<UserCatalogue>complexWhereSpec(filterComplex)));

        Pageable pageable = PageRequest.of(page - 1, perPage, sort);
        return userCatalogueRepository.findAll(specification ,pageable);
    }

    @Override
    @Transactional
    public UserCatalogue create(StoreRequest request) {
        try {
            UserCatalogue payload = UserCatalogue.builder()
                    .name(request.getName())
                    .publish((request.getPublish()))
                    .build();
            return userCatalogueRepository.save(payload);
        } catch (Exception e) {
            throw new RuntimeException("Transaction failed" + e.getMessage());
        }
    }

    @Override
    @Transactional
    public UserCatalogue update(Long id, UpdateRequest request) {

        UserCatalogue userCatalogue = userCatalogueRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Nhóm thành viên không tồn tại"));

        UserCatalogue payload = userCatalogue.toBuilder()
                .name(request.getName())
                .publish(request.getPublish())
                .build();

        return userCatalogueRepository.save(payload);
    }

}
