package me.novascomp.flat.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import me.novascomp.utils.service.GeneralService;
import me.novascomp.flat.model.Detail;
import me.novascomp.flat.model.General;
import me.novascomp.flat.model.Resident;
import me.novascomp.flat.repository.ResidentRepository;
import me.novascomp.flat.service.business.rules.ResidentCreateBusinessRule;
import me.novascomp.flat.service.business.rules.ResidentUpdateBusinessRule;
import me.novascomp.utils.standalone.service.components.Requirement;
import me.novascomp.utils.standalone.attributes.Attribute;
import me.novascomp.utils.standalone.attributes.AttributeTag;
import me.novascomp.utils.standalone.service.exceptions.BadRequestException;
import me.novascomp.utils.standalone.service.exceptions.NotFoundException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@Service
public class ResidentService extends GeneralService<Resident, ResidentRepository, ResidentCreateBusinessRule, ResidentUpdateBusinessRule> {

    private final DetailService detailService;

    @Autowired
    public ResidentService(DetailService detailService) {
        this.detailService = detailService;
    }

    public Page<Resident> findDistinctByDetailListIn(List<Detail> detailList, Pageable pageable) {
        return repository.findDistinctByDetailListIn(detailList, pageable);
    }

    public void removeResidentFromAllDetails(String residentId) {
        Optional<String> residentIdOptional = Optional.ofNullable(residentId);
        Optional<Resident> residentById = null;

        if (residentIdOptional.isPresent()) {
            residentById = findById(residentId);
            if (residentById.isPresent()) {
                List<Resident> residents = new ArrayList<>();
                residents.add(residentById.get());
                List<String> detailIDs = new ArrayList<>();
                for (Detail detail : detailService.findByResidentListIn(residents, PageRequest.of(0, Integer.MAX_VALUE)).getContent()) {
                    detailIDs.add(detail.getDetailId());
                }

                for (String detailId : detailIDs) {
                    detailService.removeResidentFromDetail(detailId, residentById.get().getResidentId(), this);
                }
            } else {
                throw new NotFoundException("");
            }
        } else {
            throw new BadRequestException("");
        }
    }

    @Override
    protected ResidentCreateBusinessRule getBusinessRuleCrudCreate(Requirement requirement) {
        Optional<String> detailId = requirement.getAttributeValue(Attribute.DETAIL_ID);
        Optional<String> email = requirement.getAttributeValue(Attribute.EMAIL);
        Optional<String> phone = requirement.getAttributeValue(Attribute.PHONE_NUMBER);

        boolean foundInDatabase = false;
        boolean requiredFlatDetailFound = false;

        if (email.isPresent()) {
            foundInDatabase = repository.existsByEmail(email.get());
        }

        if (detailId.isPresent()) {
            requiredFlatDetailFound = detailService.findById(detailId.get()).isPresent();
        }

        if (requiredFlatDetailFound && email.isPresent() && phone.isPresent()) {
            foundInDatabase = checkMembersInFlatDetail(detailId.get(), email.get(), phone.get(), "");
        }

        return new ResidentCreateBusinessRule(foundInDatabase, requiredFlatDetailFound);
    }

    public boolean checkMembersInFlatDetail(String detailId, String email, String phone, String residentId) {
        Optional<Detail> detailById = detailService.findById(detailId);

        if (detailById.isPresent()) {
            Detail detail = detailById.get();
            for (Resident resident : detail.getResidentList()) {
                if (resident.getEmail().equals(email) || resident.getPhone().equals(phone)) {
                    return !resident.getResidentId().equals(residentId);
                }
            }
        }

        return false;
    }

    @Override
    public Requirement<ResidentCreateBusinessRule> getRequirementCrudCreate() {
        return new Requirement<>(AttributeTag.RESIDENT_CRUD_CREATE);
    }

    @Override
    protected Resident addModel(String id, Requirement<ResidentCreateBusinessRule> requirement) {
        Resident resident = new Resident(id);
        fillInCommonCrudAttributes(requirement, resident);

        General general = restUtils.getGeneral(id);
        resident.setGeneral(general);
        repository.save(resident);

        Optional<String> detailId = requirement.getAttributeValue(Attribute.DETAIL_ID);

        if (detailId.isPresent()) {
            detailService.addResidentToDetail(detailId.get(), id, this);
        }

        return resident;
    }

    @Override
    protected ResidentUpdateBusinessRule getBusinessRuleCrudUpdate(Requirement<ResidentUpdateBusinessRule> requirement) {
        Optional<String> residentId = requirement.getAttributeValue(Attribute.RESIDET_ID);
        Optional<String> email = requirement.getAttributeValue(Attribute.EMAIL);
        Optional<String> phone = requirement.getAttributeValue(Attribute.PHONE_NUMBER);

        Optional<Resident> residentById;

        boolean conflict = false;
        boolean idOk = false;

        if (residentId.isPresent()) {
            residentById = findById(residentId.get());
        } else {
            residentById = Optional.ofNullable(null);
        }

        if (residentId.isPresent()) {
            idOk = true;

            for (Detail detail : residentById.get().getDetailList()) {
                if (email.isPresent() && phone.isPresent()) {
                    conflict = checkMembersInFlatDetail(detail.getDetailId(), email.get(), phone.get(), residentId.get());
                    if (conflict) {
                        break;
                    }
                }
            }
        }

        return new ResidentUpdateBusinessRule(conflict, idOk);
    }

    @Override
    public Requirement<ResidentUpdateBusinessRule> getRequirementCrudUdate() {
        return new Requirement<>(AttributeTag.RESIDENT_CRUD_UPDATE);
    }

    @Override
    protected void mergeModel(Requirement<ResidentUpdateBusinessRule> requirement) {
        Optional<Resident> resident = findById(requirement.getAttributeValue(Attribute.RESIDET_ID).get());
        if (resident.isPresent()) {
            fillInCommonCrudAttributes(requirement, resident.get());
            repository.save(resident.get());
        }
    }

    @Override
    public String getModelId(Resident model) {
        return model.getResidentId();
    }

    private void fillInCommonCrudAttributes(Requirement requirement, Resident residnet) {
        residnet.setFirstName((String) requirement.getAttributeValue(Attribute.FIRST_NAME).get());
        residnet.setLastName((String) requirement.getAttributeValue(Attribute.LAST_NAME).get());
        residnet.setEmail((String) requirement.getAttributeValue(Attribute.EMAIL).get());
        residnet.setPhone((String) requirement.getAttributeValue(Attribute.PHONE_NUMBER).get());
        residnet.setDateOfBirth((String) requirement.getAttributeValue(Attribute.DATE_OF_BIRTH).get());
    }

}
