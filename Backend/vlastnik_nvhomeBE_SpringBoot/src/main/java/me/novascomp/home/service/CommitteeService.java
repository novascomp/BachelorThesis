package me.novascomp.home.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import me.novascomp.home.model.Committee;
import me.novascomp.home.model.General;
import me.novascomp.home.model.Member;
import me.novascomp.home.model.Organization;
import me.novascomp.home.repository.CommitteeRepository;
import me.novascomp.home.service.business.rules.CommitteeCreateBusinessRule;
import me.novascomp.home.service.business.rules.CommitteeUpdateBusinessRule;
import me.novascomp.utils.service.GeneralService;
import me.novascomp.utils.standalone.attributes.Attribute;
import me.novascomp.utils.standalone.attributes.AttributeTag;
import me.novascomp.utils.standalone.service.components.Requirement;
import me.novascomp.utils.standalone.service.exceptions.BadRequestException;
import me.novascomp.utils.standalone.service.exceptions.ConflictException;
import me.novascomp.utils.standalone.service.exceptions.NotFoundException;
import me.novascomp.utils.standalone.service.exceptions.ServiceException;

@Service
public class CommitteeService extends GeneralService<Committee, CommitteeRepository, CommitteeCreateBusinessRule, CommitteeUpdateBusinessRule> {

    private final OrganizationService organizationService;

    @Autowired
    public CommitteeService(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    public Committee addDefaultCommittee(Organization organization) throws ServiceException {

        String id = UUID.randomUUID().toString();
        Committee committee = new Committee(id);

        committee.setEmail(RegistrationService.DEFAULT_COMMITTEE_EMAIL);
        committee.setPhone(RegistrationService.DEFAULT_COMMITTEE_PHONE);
        committee.setOrganization(organization);

        General general = restUtils.getGeneral(id);
        committee.setGeneral(general);
        repository.save(committee);

        return committee;
    }

    @Override
    protected CommitteeCreateBusinessRule getBusinessRuleCrudCreate(Requirement<CommitteeCreateBusinessRule> requirement) {
        Optional<String> email = requirement.getAttributeValue(Attribute.EMAIL);
        Optional<String> phone = requirement.getAttributeValue(Attribute.PHONE_NUMBER);
        Optional<String> organizationId = requirement.getAttributeValue(Attribute.ORGANIZATION_ID);

        boolean foundInDatabase = false;

        Optional<Organization> organization = Optional.ofNullable(null);

        if (organizationId.isPresent()) {
            organization = organizationService.findById(organizationId.get());
        }

        if (organization.isPresent()) {
            foundInDatabase = !organization.get().getCommitteeList().isEmpty();
        }

        return new CommitteeCreateBusinessRule(foundInDatabase);
    }

    @Override
    public Requirement<CommitteeCreateBusinessRule> getRequirementCrudCreate() {
        return new Requirement<>(AttributeTag.NV_HOME_COMMITTEE_CREATE);
    }

    @Override
    protected Committee addModel(String id, Requirement<CommitteeCreateBusinessRule> requirement) {
        Committee committee = new Committee(id);
        fillInCommonCrudAttributes(requirement, committee);

        Optional<Organization> organization = organizationService.findById(requirement.getAttributeValue(Attribute.ORGANIZATION_ID).get());

        if (organization.isPresent()) {
            committee.setOrganization(organization.get());
        }

        General general = restUtils.getGeneral(id);
        committee.setGeneral(general);
        repository.save(committee);
        return committee;
    }

    @Override
    protected CommitteeUpdateBusinessRule getBusinessRuleCrudUpdate(Requirement<CommitteeUpdateBusinessRule> requirement) {
        Optional<String> committeeId = requirement.getAttributeValue(Attribute.COMMITTEE_ID);

        Optional<Committee> committeeById;

        boolean conflict = false;
        boolean idOk = false;

        if (committeeId.isPresent()) {
            committeeById = findById(committeeId.get());
        } else {
            committeeById = Optional.ofNullable(null);
        }

        if (committeeById.isPresent()) {
            idOk = true;
        }

        return new CommitteeUpdateBusinessRule(conflict, idOk);
    }

    @Override
    protected void mergeModel(Requirement<CommitteeUpdateBusinessRule> requirement) {
        Optional<Committee> committee = findById(requirement.getAttributeValue(Attribute.COMMITTEE_ID).get());
        if (committee.isPresent()) {
            fillInCommonCrudAttributes(requirement, committee.get());
            repository.save(committee.get());
        }
    }

    @Override
    public Requirement<CommitteeUpdateBusinessRule> getRequirementCrudUdate() {
        return new Requirement<>(AttributeTag.NV_HOME_COMMITTEE_UPDATE);
    }

    @Override
    public String getModelId(Committee committee) {
        return committee.getCommitteeId();
    }

    public Page<Member> getCommitteMembers(List<Committee> committees, Pageable pageable, MemberService memberService) {
        return memberService.findDistinctByCommitteeListIn(committees, pageable);
    }

    public void addMemberToCommittee(String committeeId, String memberId, MemberService memberService) throws ServiceException {

        if (Optional.ofNullable(committeeId).isEmpty() || Optional.ofNullable(memberId).isEmpty()) {
            throw new BadRequestException("");
        }

        Optional<Committee> committee = findById(committeeId);
        Optional<Member> member = memberService.findById(memberId);

        if (committee.isPresent() && member.isPresent()) {
            if (committee.get().getMemberList() == null) {
                committee.get().setMemberList(new ArrayList<>());
            }
            if (!committee.get().getMemberList().contains(member.get())) {
                committee.get().getMemberList().add(member.get());
                repository.save(committee.get());
            } else {
                throw new ConflictException("");
            }
        } else {
            throw new NotFoundException("");
        }
    }

    public void removeMemberFromCommittee(String committeeId, String memberId, MemberService memberService) {

        if (Optional.ofNullable(committeeId).isEmpty() || Optional.ofNullable(memberId).isEmpty()) {
            throw new BadRequestException("");
        }

        Optional<Committee> committee = findById(committeeId);
        Optional<Member> member = memberService.findById(memberId);

        if (committee.isPresent() && member.isPresent()) {
            if (committee.get().getMemberList().contains(member.get())) {
                committee.get().getMemberList().remove(member.get());
                repository.save(committee.get());
            } else {
                throw new NotFoundException("");
            }
        }
    }

    private void fillInCommonCrudAttributes(Requirement requirement, Committee committee) {
        committee.setEmail((String) requirement.getAttributeValue(Attribute.EMAIL).get());
        committee.setPhone((String) requirement.getAttributeValue(Attribute.PHONE_NUMBER).get());
    }
}
