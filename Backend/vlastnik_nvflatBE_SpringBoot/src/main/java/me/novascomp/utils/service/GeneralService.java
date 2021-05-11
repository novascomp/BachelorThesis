package me.novascomp.utils.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.http.HttpStatus;
import me.novascomp.utils.microservice.communication.MicroserviceConnectionException;
import me.novascomp.utils.repository.GeneralRepository;
import me.novascomp.utils.standalone.service.components.CrudCreateBusinessRule;
import me.novascomp.utils.standalone.service.components.CrudUpdateBusinessRule;
import me.novascomp.utils.standalone.service.components.Requirement;
import me.novascomp.utils.standalone.service.components.GeneralCreateResponse;
import me.novascomp.utils.standalone.service.components.GeneralUpdateResponse;
import me.novascomp.utils.rest.RestUtils;
import me.novascomp.utils.standalone.service.exceptions.BadRequestException;
import me.novascomp.utils.standalone.service.exceptions.ConflictException;
import me.novascomp.utils.standalone.service.exceptions.CreatedException;
import me.novascomp.utils.standalone.service.exceptions.ForbiddenException;
import me.novascomp.utils.standalone.service.exceptions.InternalException;
import me.novascomp.utils.standalone.service.exceptions.NotFoundException;
import me.novascomp.utils.standalone.service.exceptions.OKException;
import me.novascomp.utils.standalone.service.exceptions.ServiceException;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
public abstract class GeneralService<Model, Repository extends PagingAndSortingRepository<Model, String> & CrudRepository<Model, String> & GeneralRepository<Model, String>, BusinessRuleCrudCreate extends CrudCreateBusinessRule, BusinessRuleCrudUpdate extends CrudUpdateBusinessRule> {

    @Autowired
    protected Repository repository;

    @Autowired
    protected RestUtils restUtils;

    @Autowired
    protected ObjectMapper objectMapper;

    protected final Logger LOG = Logger.getLogger(this.getClass().getName());

    public Optional<Model> findById(String id) {
        return repository.findById(id);
    }

    public boolean existsById(String id) {
        return repository.existsById(id);
    }

    public void delete(Model model) {
        repository.delete(model);
    }

    public Page<Model> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public GeneralCreateResponse<Model, Requirement, BusinessRuleCrudCreate> createModel(Requirement<BusinessRuleCrudCreate> requirement) {
        String id = UUID.randomUUID().toString();
        BusinessRuleCrudCreate businessRule = getBusinessRuleCrudCreate(requirement);

        GeneralCreateResponse<Model, Requirement, BusinessRuleCrudCreate> response;
        if (requirement.isValid(businessRule)) {
            Model model = addModel(id, requirement);
            response = new GeneralCreateResponse<>(Optional.ofNullable(model), requirement, businessRule, true);
        } else {
            response = new GeneralCreateResponse<>(Optional.ofNullable(null), requirement, businessRule, false);
        }
        return response;
    }

    public GeneralUpdateResponse<Requirement, BusinessRuleCrudUpdate> updateModel(Requirement<BusinessRuleCrudUpdate> requirement) {
        BusinessRuleCrudUpdate businessRule = getBusinessRuleCrudUpdate(requirement);

        GeneralUpdateResponse<Requirement, BusinessRuleCrudUpdate> response;
        if (requirement.isValid(businessRule)) {
            mergeModel(requirement);
            response = new GeneralUpdateResponse(requirement, businessRule, true);
        } else {
            response = new GeneralUpdateResponse(requirement, businessRule, false);
        }
        return response;
    }

    protected abstract BusinessRuleCrudCreate getBusinessRuleCrudCreate(Requirement<BusinessRuleCrudCreate> requirement);

    protected abstract Model addModel(String id, Requirement<BusinessRuleCrudCreate> requirement);

    public abstract Requirement<BusinessRuleCrudCreate> getRequirementCrudCreate();

    protected abstract BusinessRuleCrudUpdate getBusinessRuleCrudUpdate(Requirement<BusinessRuleCrudUpdate> requirement);

    protected abstract void mergeModel(Requirement<BusinessRuleCrudUpdate> requirement);

    public abstract Requirement<BusinessRuleCrudUpdate> getRequirementCrudUdate();

    public abstract String getModelId(Model model);

    protected void httpStatusCodeToException(HttpStatus httpStatus) throws ServiceException, SecurityException {

        if (null != httpStatus) {
            switch (httpStatus) {
                case OK:
                    throw new OKException("");
                case CREATED:
                    throw new CreatedException("");
                case CONFLICT:
                    throw new ConflictException("");
                case FORBIDDEN:
                    throw new ForbiddenException("");
                case NOT_FOUND:
                    throw new NotFoundException("");
                case INTERNAL_SERVER_ERROR:
                    throw new InternalException("");
                case SERVICE_UNAVAILABLE:
                    throw new MicroserviceConnectionException("");
                case BAD_REQUEST:
                    throw new BadRequestException("");
                default:
                    throw new MicroserviceConnectionException("");
            }
        }
    }

    protected Page<?> getPage(List<?> list, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = (start + pageable.getPageSize()) > list.size() ? list.size() : (start + pageable.getPageSize());
        if (start > end) {
            start = end;
        }

        Page<?> page = new PageImpl<>(list.subList(start, end), pageable, list.size());
        return page;
    }

}
