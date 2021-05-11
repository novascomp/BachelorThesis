package me.novascomp.home.service;

import ares.vr.json.AresResponseChecker;
import ares.vr.json.Clen;
import ares.vr.json.Fosoba;
import ares.vr.json.VypisVR;
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
import me.novascomp.home.repository.MemberRepository;
import me.novascomp.home.service.business.rules.MemberCreateBusinessRule;
import me.novascomp.home.service.business.rules.MemberUpdateBusinessRule;
import me.novascomp.utils.service.GeneralService;
import me.novascomp.utils.standalone.attributes.Attribute;
import me.novascomp.utils.standalone.attributes.AttributeTag;
import me.novascomp.utils.standalone.service.components.Requirement;
import me.novascomp.utils.standalone.service.exceptions.BadRequestException;
import me.novascomp.utils.standalone.service.exceptions.ServiceException;

@Service
public class MemberService extends GeneralService<Member, MemberRepository, MemberCreateBusinessRule, MemberUpdateBusinessRule> {

    private final CommitteeService committeeService;

    @Autowired
    public MemberService(CommitteeService committeeService) {
        this.committeeService = committeeService;
    }

    public void addPersonFromAres(String ico, String committeeId, List<VypisVR> vypisVRList, RegistrationService registrationService) throws ServiceException {

        List<Fosoba> alreadyAdded = new ArrayList<>();
        for (VypisVR vypisVR : vypisVRList) {
            if (AresResponseChecker.checkStatutarniOrganClen(vypisVR)) {
                for (Clen clen : vypisVR.statutarniOrgan.clen) {
                    if (AresResponseChecker.checkStatutarniOrganClenFosoba(clen)) {
                        Fosoba fosoba = clen.fosoba;
                        if (alreadyAdded.contains(fosoba) == false) {
                            addFosoba(committeeId, fosoba);
                            alreadyAdded.add(fosoba);
                        }
                    }
                }
            }
        }
    }

    private void addFosoba(String committeeId, Fosoba fosoba) {
        String id = UUID.randomUUID().toString();
        Member member = new Member(id);

        if (AresResponseChecker.checkS(fosoba.jmeno)) {
            member.setFirstName(fosoba.jmeno.toUpperCase());
        } else {
            member.setFirstName(RegistrationService.DEFAULT_MEMBERS_FIRST_NAME);
        }

        if (AresResponseChecker.checkS(fosoba.prijmeni)) {
            member.setLastName(fosoba.prijmeni.toUpperCase());
        } else {
            member.setLastName(RegistrationService.DEFAULT_MEMBERS_LAST_NAME);
        }

        member.setEmail(RegistrationService.DEFAULT_MEMBERS_EMAIL);
        member.setPhone(RegistrationService.DEFAULT_MEMBERS_PHONE);

        if (AresResponseChecker.checkS(fosoba.datumNarozeni)) {
            member.setDateOfBirth(fosoba.datumNarozeni);
        } else {
            member.setDateOfBirth(RegistrationService.DEFAULT_MEMBERS_DATE_OF_BIRTH);
        }

        General general = restUtils.getGeneral(id);
        member.setGeneral(general);
        repository.save(member);
        committeeService.addMemberToCommittee(committeeId, id, this);
    }

    public Page<Member> findDistinctByCommitteeListIn(List<Committee> committeeList, Pageable pageable) {
        return repository.findDistinctByCommitteeListIn(committeeList, pageable);
    }

    @Override
    protected MemberCreateBusinessRule getBusinessRuleCrudCreate(Requirement<MemberCreateBusinessRule> requirement) {
        Optional<String> committeeId = requirement.getAttributeValue(Attribute.MEMBER_REQUIRED_COMMITTEE_ID_CRUD_CREATE);
        Optional<String> email = requirement.getAttributeValue(Attribute.EMAIL);
        Optional<String> phone = requirement.getAttributeValue(Attribute.PHONE_NUMBER);

        boolean foundInDatabase = false;
        boolean requiredCommitteeFound = false;

        if (committeeId.isPresent()) {
            requiredCommitteeFound = committeeService.existsById(committeeId.get());
        }

        if (requiredCommitteeFound && email.isPresent() && phone.isPresent()) {
            foundInDatabase = checkMembersInCommittee(committeeId.get(), email.get(), phone.get(), "");
        }

        return new MemberCreateBusinessRule(foundInDatabase, requiredCommitteeFound);
    }

    private boolean checkMembersInCommittee(String committeeId, String email, String phone, String memberId) {
        Optional<Committee> committeeById = committeeService.findById(committeeId);

        if (committeeById.isPresent()) {
            Committee committee = committeeById.get();
            for (Member member : committee.getMemberList()) {
                if (member.getEmail().equals(email) || member.getPhone().equals(phone)) {
                    return !member.getMemberId().equals(memberId);
                }
            }
        }

        return false;
    }

    @Override
    public Requirement<MemberCreateBusinessRule> getRequirementCrudCreate() {
        return new Requirement<>(AttributeTag.NV_HOME_COMMITTEE_MEMBER_CREATE);
    }

    @Override
    protected Member addModel(String id, Requirement<MemberCreateBusinessRule> requirement) {
        Member member = new Member(id);
        fillInCommonCrudAttributes(requirement, member);

        General general = restUtils.getGeneral(id);
        member.setGeneral(general);
        repository.save(member);

        Optional<String> committeeId = requirement.getAttributeValue(Attribute.MEMBER_REQUIRED_COMMITTEE_ID_CRUD_CREATE);

        if (committeeId.isPresent()) {
            committeeService.addMemberToCommittee(committeeId.get(), id, this);
        }

        return member;
    }

    @Override
    protected MemberUpdateBusinessRule getBusinessRuleCrudUpdate(Requirement<MemberUpdateBusinessRule> requirement) {
        Optional<String> memberId = requirement.getAttributeValue(Attribute.MEMBER_ID);
        Optional<String> email = requirement.getAttributeValue(Attribute.EMAIL);
        Optional<String> phone = requirement.getAttributeValue(Attribute.PHONE_NUMBER);

        Optional<Member> memberById;

        boolean conflict = false;
        boolean idOk = false;

        if (memberId.isPresent()) {
            memberById = findById(memberId.get());
        } else {
            memberById = Optional.ofNullable(null);
        }

        if (memberById.isPresent()) {
            idOk = true;

            for (Committee committee : memberById.get().getCommitteeList()) {
                if (email.isPresent() && phone.isPresent()) {
                    conflict = checkMembersInCommittee(committee.getCommitteeId(), email.get(), phone.get(), memberId.get());
                    if (conflict) {
                        break;
                    }
                }
            }
        }

        return new MemberUpdateBusinessRule(conflict, idOk);
    }

    @Override
    public Requirement<MemberUpdateBusinessRule> getRequirementCrudUdate() {
        return new Requirement<>(AttributeTag.NV_HOME_COMMITTEE_MEMBER_UPDATE);
    }

    @Override
    protected void mergeModel(Requirement<MemberUpdateBusinessRule> requirement) {
        Optional<Member> member = findById(requirement.getAttributeValue(Attribute.MEMBER_ID).get());
        if (member.isPresent()) {
            fillInCommonCrudAttributes(requirement, member.get());
            repository.save(member.get());
        }
    }

    @Override
    public String getModelId(Member model) {
        return model.getMemberId();
    }

    public void removeMemberFromCommittee(String memberId) {
        Optional<String> memberIdOptional = Optional.ofNullable(memberId);
        Optional<Member> memberById = null;

        if (memberIdOptional.isPresent()) {
            memberById = findById(memberId);
            if (memberById.isPresent()) {
                for (Committee committee : memberById.get().getCommitteeList()) {
                    committeeService.removeMemberFromCommittee(committee.getCommitteeId(), memberById.get().getMemberId(), this);
                }
            } else {
                throw new BadRequestException("");
            }
        } else {
            throw new BadRequestException("");
        }
    }

    private void fillInCommonCrudAttributes(Requirement requirement, Member member) {
        member.setFirstName((String) requirement.getAttributeValue(Attribute.FIRST_NAME).get());
        member.setLastName((String) requirement.getAttributeValue(Attribute.LAST_NAME).get());
        member.setEmail((String) requirement.getAttributeValue(Attribute.EMAIL).get());
        member.setPhone((String) requirement.getAttributeValue(Attribute.PHONE_NUMBER).get());
        member.setDateOfBirth((String) requirement.getAttributeValue(Attribute.DATE_OF_BIRTH).get());
    }

}
