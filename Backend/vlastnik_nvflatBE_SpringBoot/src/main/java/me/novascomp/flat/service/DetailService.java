package me.novascomp.flat.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import me.novascomp.utils.service.GeneralService;
import me.novascomp.flat.model.Detail;
import me.novascomp.flat.model.Flat;
import me.novascomp.flat.model.General;
import me.novascomp.flat.model.MessageRecordNvm;
import me.novascomp.flat.model.Resident;
import me.novascomp.flat.model.User;
import me.novascomp.flat.repository.DetailRepository;
import me.novascomp.flat.service.business.rules.DetailCreateBusinessRule;
import me.novascomp.flat.service.business.rules.DetailUpdateBusinessRule;
import me.novascomp.utils.standalone.service.components.Requirement;
import me.novascomp.utils.standalone.attributes.Attribute;
import me.novascomp.utils.standalone.attributes.AttributeTag;
import me.novascomp.utils.standalone.service.exceptions.BadRequestException;
import me.novascomp.utils.standalone.service.exceptions.ConflictException;
import me.novascomp.utils.standalone.service.exceptions.NotFoundException;
import me.novascomp.utils.standalone.service.exceptions.ServiceException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DetailService extends GeneralService<Detail, DetailRepository, DetailCreateBusinessRule, DetailUpdateBusinessRule> {

    private final FlatService flatService;
    private final UserService userService;

    @Autowired
    public DetailService(FlatService flatService, UserService userService) {
        this.flatService = flatService;
        this.userService = userService;
    }

    public Page<Detail> findByFlat(Flat flat, Pageable pageable) {
        return repository.findByFlat(flat, pageable);
    }

    public Optional<Detail> findByFlat(Flat flat) {
        return repository.findByFlat(flat);
    }

    public Page<Detail> findByMessageRecordNvmListIn(List<MessageRecordNvm> messageRecordNvmList, Pageable pageable) {
        return repository.findByMessageRecordNvmListIn(messageRecordNvmList, pageable);
    }

    public List<Detail> findDistinctByMessageRecordNvmListIn(List<MessageRecordNvm> messageRecordNvmList) {
        return repository.findDistinctByMessageRecordNvmListIn(messageRecordNvmList);
    }

    public List<Detail> findByResidentListIn(List<Resident> residentList) {
        return repository.findByResidentListIn(residentList);
    }

    public Page<Detail> findByResidentListIn(List<Resident> residentList, Pageable pageable) {
        return repository.findByResidentListIn(residentList, pageable);
    }

    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public List<String> getUserDetailsByTokenHolding(TokenService tokenService, String userUid) throws ServiceException {
        if (Optional.ofNullable(userUid).isEmpty()) {
            throw new BadRequestException("");
        }
        List<String> userDetails = new ArrayList<>();
        Optional<User> user = userService.findByUid(userUid);

        for (Flat flat : flatService.findByTokenListIn(tokenService.findByUserId(user.get()))) {
            userDetails.add(repository.findByFlat(flat).get().getDetailId());
        }

        return userDetails;
    }

    public void addMessageToDetail(String detailId, String messageId, MessageRecordNvmService messageRecordNvmService) throws ServiceException {

        if (Optional.ofNullable(detailId).isEmpty() || Optional.ofNullable(messageId).isEmpty()) {
            throw new BadRequestException("");
        }

        Optional<Detail> detail = findById(detailId);
        Optional<MessageRecordNvm> messageRecordNvm = messageRecordNvmService.findById(messageId);

        if (detail.isPresent() && messageRecordNvm.isPresent()) {
            if (!detail.get().getMessageRecordNvmList().contains(messageRecordNvm.get())) {
                detail.get().getMessageRecordNvmList().add(messageRecordNvm.get());
                repository.save(detail.get());
            } else {
                throw new ConflictException("");
            }
        } else {
            throw new NotFoundException("");
        }
    }

    public void addResidentToDetail(String detailId, String residentId, ResidentService residentService) throws ServiceException {

        if (Optional.ofNullable(detailId).isEmpty() || Optional.ofNullable(residentId).isEmpty()) {
            throw new BadRequestException("");
        }

        Optional<Detail> detail = findById(detailId);
        Optional<Resident> resident = residentService.findById(residentId);

        if (detail.isPresent() && resident.isPresent()) {
            if (!detail.get().getResidentList().contains(resident.get())) {
                detail.get().getResidentList().add(resident.get());
                repository.save(detail.get());
            } else {
                throw new ConflictException("");
            }
        } else {
            throw new NotFoundException("");
        }
    }

    public boolean removeResidentFromDetail(String detailId, String residentId, ResidentService residentService) {

        if (Optional.ofNullable(detailId).isEmpty() || Optional.ofNullable(residentId).isEmpty()) {
            throw new BadRequestException("");
        }

        Optional<Detail> detail = findById(detailId);
        Optional<Resident> resident = residentService.findById(residentId);
        List<Resident> residents = new ArrayList<>();

        if (detail.isPresent() && resident.isPresent()) {
            if (detail.get().getResidentList().contains(resident.get())) {
                detail.get().getResidentList().remove(resident.get());
                repository.save(detail.get());

                residents.add(residentService.findById(residentId).get());
                if (repository.findByResidentListIn(residents, PageRequest.of(0, Integer.MAX_VALUE)).getTotalElements() == 0) {
                    residentService.delete(residents.get(0));
                    return true;
                }
                return false;
            }
        }

        throw new NotFoundException("");
    }

    public boolean removeMessageFromDetail(String detailId, String messageId, MessageRecordNvmService messageRecordNvmService) {

        if (Optional.ofNullable(detailId).isEmpty() || Optional.ofNullable(messageId).isEmpty()) {
            throw new BadRequestException("");
        }

        Optional<Detail> detail = findById(detailId);
        Optional<MessageRecordNvm> message = messageRecordNvmService.findById(messageId);
        List<MessageRecordNvm> messages = new ArrayList<>();

        if (detail.isPresent() && message.isPresent()) {
            if (detail.get().getMessageRecordNvmList().contains(message.get())) {
                detail.get().getMessageRecordNvmList().remove(message.get());
                repository.save(detail.get());

                messages.add(messageRecordNvmService.findById(messageId).get());
                if (repository.findByMessageRecordNvmListIn(messages, PageRequest.of(0, Integer.MAX_VALUE)).getTotalElements() == 0) {
                    messageRecordNvmService.delete(message.get());
                    return true;
                }
                return false;
            }
        }

        throw new NotFoundException("");
    }

    public FlatService getFlatService() {
        return flatService;
    }

    @Override
    protected DetailCreateBusinessRule getBusinessRuleCrudCreate(Requirement<DetailCreateBusinessRule> requirement) {

        Optional<String> flatId = requirement.getAttributeValue(Attribute.FLAT_ID);

        boolean foundInDatabase = false;
        boolean flatIdOk = false;

        if (flatId.isPresent()) {
            Optional<Flat> flat = flatService.findById(flatId.get());
            if (flat.isPresent()) {
                flatIdOk = true;
                if (Optional.ofNullable(flat.get().getDetailList()).isPresent()) {
                    if (!flat.get().getDetailList().isEmpty()) {
                        foundInDatabase = true;
                    }
                }
            }
        }
        return new DetailCreateBusinessRule(flatIdOk, foundInDatabase);
    }

    @Override
    protected Detail addModel(String id, Requirement<DetailCreateBusinessRule> requirement) {
        Detail detail = new Detail(id);
        fillInCommonCrudAttributes(requirement, detail);

        Optional<Flat> flat = flatService.findById(requirement.getAttributeValue(Attribute.FLAT_ID).get());

        if (flat.isPresent()) {
            detail.setFlat(flat.get());
        }

        General general = restUtils.getGeneral(id);
        detail.setGeneral(general);
        repository.save(detail);
        return detail;
    }

    @Override
    public Requirement<DetailCreateBusinessRule> getRequirementCrudCreate() {
        return new Requirement<>(AttributeTag.DETAIL_CRUD_CREATE);
    }

    @Override
    protected DetailUpdateBusinessRule getBusinessRuleCrudUpdate(Requirement<DetailUpdateBusinessRule> requirement) {

        Optional<String> detailId = requirement.getAttributeValue(Attribute.DETAIL_ID);
        Optional<String> flatId = requirement.getAttributeValue(Attribute.FLAT_ID);

        boolean idOk = false;
        boolean conflict = false;
        boolean flatIdOk = false;

        if (detailId.isPresent()) {
            idOk = this.existsById(detailId.get());
        }

        if (flatId.isPresent()) {
            Optional<Flat> flat = flatService.findById(flatId.get());
            if (flat.isPresent()) {
                flatIdOk = true;
                if (!flat.get().getDetailList().isEmpty()) {
                    for (Detail detail : flat.get().getDetailList()) {
                        if (!detail.getDetailId().equals(detailId.get())) {
                            conflict = true;
                        }
                    }
                }
            }
        }

        return new DetailUpdateBusinessRule(flatIdOk, conflict, idOk);
    }

    @Override
    protected void mergeModel(Requirement<DetailUpdateBusinessRule> requirement) {
        Optional<Detail> detail = findById(requirement.getAttributeValue(Attribute.DETAIL_ID).get());
        Optional<Flat> flat = flatService.findById(requirement.getAttributeValue(Attribute.FLAT_ID).get());
        if (detail.isPresent() && flat.isPresent()) {
            fillInCommonCrudAttributes(requirement, detail.get());
            detail.get().setFlat(flat.get());
            repository.save(detail.get());
        }
    }

    @Override
    public Requirement<DetailUpdateBusinessRule> getRequirementCrudUdate() {
        return new Requirement<>(AttributeTag.DETAIL_CRUD_UPDATE);
    }

    @Override
    public String getModelId(Detail model) {
        return model.getDetailId();
    }

    private void fillInCommonCrudAttributes(Requirement requirement, Detail detail) {
        detail.setSize((String) requirement.getAttributeValue(Attribute.SIZE).get());
        detail.setCommonShareSize((String) requirement.getAttributeValue(Attribute.COMMON_SHARE_SIZE).get());
    }
}
