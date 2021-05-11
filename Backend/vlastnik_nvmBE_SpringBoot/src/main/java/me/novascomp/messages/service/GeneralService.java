package me.novascomp.messages.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.logging.Logger;
import me.novascomp.messages.config.NVFUtils;
import me.novascomp.messages.repository.GeneralRepository;
import me.novascomp.microservice.communication.MicroserviceConnectionException;
import me.novascomp.utils.standalone.service.exceptions.BadRequestException;
import me.novascomp.utils.standalone.service.exceptions.ConflictException;
import me.novascomp.utils.standalone.service.exceptions.CreatedException;
import me.novascomp.utils.standalone.service.exceptions.ForbiddenException;
import me.novascomp.utils.standalone.service.exceptions.InternalException;
import me.novascomp.utils.standalone.service.exceptions.NotFoundException;
import me.novascomp.utils.standalone.service.exceptions.OKException;
import me.novascomp.utils.standalone.service.exceptions.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Transactional(propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED)
public abstract class GeneralService<Model, Repository extends PagingAndSortingRepository<Model, String> & CrudRepository<Model, String> & GeneralRepository<Model, String>> {

    @Autowired
    protected Repository repository;

    @Autowired
    protected NVFUtils nvfUtils;

    @Autowired
    protected ObjectMapper objectMapper;

    protected static final Logger LOG = Logger.getLogger(GeneralService.class.getName());

    protected HttpStatus getStatusCode(HttpResponse<String> response) {
        int statusCode = response.statusCode();
        return HttpStatus.valueOf(statusCode);
    }

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

}
