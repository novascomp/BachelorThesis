package me.novascomp.flat.service;

import java.util.ArrayList;
import java.util.Collections;
import me.novascomp.utils.service.GeneralService;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import me.novascomp.flat.model.Flat;
import me.novascomp.flat.model.General;
import me.novascomp.flat.model.Organization;
import me.novascomp.flat.model.Token;
import me.novascomp.flat.model.User;
import me.novascomp.flat.repository.FlatRepository;
import me.novascomp.flat.rest.FlatController;
import me.novascomp.flat.service.business.rules.FlatCreateBusinessRule;
import me.novascomp.flat.service.business.rules.FlatUpdateBusinessRule;
import me.novascomp.flat.sort.SortByCommonShareSize;
import me.novascomp.flat.sort.SortByIdentifier;
import me.novascomp.flat.sort.SortBySize;
import me.novascomp.home.flat.uploader.NVHomeFlat;
import me.novascomp.utils.standalone.service.components.Requirement;
import me.novascomp.utils.standalone.attributes.Attribute;
import me.novascomp.utils.standalone.attributes.AttributeTag;
import me.novascomp.utils.standalone.service.exceptions.BadRequestException;
import me.novascomp.utils.standalone.service.exceptions.NotFoundException;
import me.novascomp.utils.standalone.service.exceptions.ServiceException;

@Service
public class FlatService extends GeneralService<Flat, FlatRepository, FlatCreateBusinessRule, FlatUpdateBusinessRule> {

    private final OrganizationService organizationService;
    private final UserService userService;

    @Autowired
    public FlatService(OrganizationService organizationService, UserService userService) {
        this.organizationService = organizationService;
        this.userService = userService;
    }

    public void deleteAllOrganizationFlats(String organizationId, MessageRecordNvmService messageRecordNvmService) throws ServiceException {
        List<Flat> flatsToRemove = organizationService.getAllOrganizationFlatsToRemove(organizationId);
        flatsToRemove.forEach((flat) -> {
            if (!FlatController.DEFAULT_FLAT_NAME.equals(flat.getIdentifier())) {
                delete(flat);
            }
        });

        messageRecordNvmService.deleteAllComponentsByCreatorKey(organizationId);
    }

    public List<Flat> findByTokenListIn(List<Token> tokenList) {
        return repository.findByTokenListIn(tokenList);
    }

    public Optional<Flat> findByIdentifierAndOrganization(String identifier, Organization organization) {
        return repository.findByIdentifierAndOrganization(identifier, organization);
    }

    public void createFlatComponentInNvmMicroservice(String organizationId, String flatIdentifier, MessageRecordNvmService messageRecordNvmService) throws ServiceException {
        messageRecordNvmService.createComponent(organizationId, flatIdentifier);
    }

    public void createFlatComponentInNvmMicroservice(String organizationId, List<String> flatIdentifiers, MessageRecordNvmService messageRecordNvmService) throws ServiceException {
        messageRecordNvmService.createComponentList(organizationId, flatIdentifiers);
    }

    public Page<NVHomeFlat> findByOrganization(Organization organization, String identifier, Pageable pageable) {
        Page<Flat> page = repository.findByOrganizationAndIdentifierNot(organization, identifier, PageRequest.of(0, Integer.MAX_VALUE));

        List<Flat> flats = new ArrayList<>();
        List<NVHomeFlat> nVHomeFlats = new ArrayList<>();
        flats.addAll(page.getContent());

        boolean flatIdentifiersInt = true;
        for (Flat flat : flats) {
            try {
                Integer.valueOf(flat.getIdentifier());
            } catch (NumberFormatException exception) {
                flatIdentifiersInt = false;
            }
        }
        if (pageable.getSort().toString().contains("commonShareSize")) {
            Collections.sort(flats, new SortByCommonShareSize());
        } else if (pageable.getSort().toString().contains("size")) {
            Collections.sort(flats, new SortBySize());
        } else if (pageable.getSort().toString().contains("identifier")) {
            if (flatIdentifiersInt) {
                Collections.sort(flats, new SortByIdentifier());
            } else {
            }
        }

        if (pageable.getSort().toString().contains("DESC")) {
            Collections.reverse(flats);
        }

        flats.stream().map((flat) -> {
            NVHomeFlat nVHomeFlat = new NVHomeFlat();
            nVHomeFlat.setFlatId(flat.getFlatId());
            nVHomeFlat.setIdentifier(flat.getIdentifier());
            nVHomeFlat.setSize(flat.getDetailList().get(0).getSize());
            nVHomeFlat.setCommonShareSize(flat.getDetailList().get(0).getCommonShareSize());
            return nVHomeFlat;
        }).forEachOrdered((nVHomeFlat) -> {
            nVHomeFlats.add(nVHomeFlat);
        });

        return (Page<NVHomeFlat>) getPage(nVHomeFlats, pageable);
    }

    public Page<Organization> getUserOrganizationsByTokenHoldingPageable(String userUid, Pageable pageable) throws ServiceException {
        if (Optional.ofNullable(userUid).isEmpty()) {
            throw new BadRequestException("");
        }
        Optional<User> user = userService.findByUid(userUid);
        if (user.isPresent()) {
            return organizationService.getUserOrganizationByFlatTokenHoldingPageable(repository.findByTokenListIn(user.get().getTokenList()), pageable);
        }
        throw new NotFoundException("");
    }

    public Page<Flat> getUserFlatsByTokenHoldingPageable(String userUid, Pageable pageable) throws ServiceException {
        if (Optional.ofNullable(userUid).isEmpty()) {
            throw new BadRequestException("");
        }
        Optional<User> user = userService.findByUid(userUid);
        if (user.isPresent()) {
            return repository.findByTokenListIn(user.get().getTokenList(), pageable);
        }
        throw new NotFoundException("");
    }

    public List<Flat> getUserFlatsByTokenHolding(String userUid) throws ServiceException {
        if (Optional.ofNullable(userUid).isEmpty()) {
            throw new BadRequestException("");
        }
        List<Flat> userFlats = new ArrayList<>();
        Optional<User> user = userService.findByUid(userUid);
        if (user.isPresent()) {
            user.get().getTokenList().forEach((token) -> {
                userFlats.add(token.getFlat());
            });
            return userFlats;
        }
        throw new NotFoundException("");
    }

    public List<Flat> getUserFlatsByScope(String userUid, String requiredScope) throws ServiceException {
        if (Optional.ofNullable(userUid).isEmpty()) {
            throw new BadRequestException("");
        }
        List<Flat> userFlatsByScope = getUserFlatsByTokenHolding(userUid);
        List<Flat> userFlats = getUserFlatsByTokenHolding(userUid);
        Optional<User> user = userService.findByUid(userUid);
        if (user.isPresent()) {
            userFlats.forEach((flat) -> {
                flat.getTokenList().stream().filter((token) -> (Optional.ofNullable(token.getUserId()).isPresent())).filter((token) -> (token.getUserId().getUid().equals(userUid))).forEachOrdered((token) -> {
                    token.getScopeList().stream().filter((scope) -> (scope.getScope().equals(requiredScope))).forEachOrdered((_item) -> {
                        userFlatsByScope.add(flat);
                    });
                });
            });
            return userFlatsByScope;
        }
        throw new NotFoundException("");
    }

    @Override
    protected FlatCreateBusinessRule getBusinessRuleCrudCreate(Requirement<FlatCreateBusinessRule> requirement) {

        Optional<String> flatIdentifier = requirement.getAttributeValue(Attribute.FLAT_IDENTIFIER);
        Optional<String> organizationId = requirement.getAttributeValue(Attribute.ORGANIZATION_ID);

        boolean organizationIdOk = false;
        boolean foundInDatabase = false;

        boolean flatIdentifierFound = false;

        if (flatIdentifier.isPresent()) {
            flatIdentifierFound = repository.existsByIdentifier(flatIdentifier.get());
        }

        if (organizationId.isPresent()) {
            organizationIdOk = organizationService.existsById(organizationId.get());
        }

        if (flatIdentifierFound && organizationIdOk) {
            foundInDatabase = organizationContainsThisFlat(flatIdentifier, organizationId);
        }

        return new FlatCreateBusinessRule(organizationIdOk, foundInDatabase);
    }

    @Override
    protected Flat addModel(String id, Requirement<FlatCreateBusinessRule> requirement) {
        Flat flat = new Flat(id);
        fillInCommonCrudAttributes(requirement, flat);

        Optional<Organization> organization = organizationService.findById(requirement.getAttributeValue(Attribute.ORGANIZATION_ID).get());

        if (organization.isPresent()) {
            flat.setOrganization(organization.get());
        }

        General general = restUtils.getGeneral(id);
        flat.setGeneral(general);
        repository.save(flat);
        return flat;
    }

    @Override
    public Requirement<FlatCreateBusinessRule> getRequirementCrudCreate() {
        return new Requirement<>(AttributeTag.FLAT_CRUD_CREATE);
    }

    @Override
    protected FlatUpdateBusinessRule getBusinessRuleCrudUpdate(Requirement<FlatUpdateBusinessRule> requirement) {

        Optional<String> flatIdentifier = requirement.getAttributeValue(Attribute.FLAT_IDENTIFIER);
        Optional<String> organizationId = requirement.getAttributeValue(Attribute.ORGANIZATION_ID);
        Optional<String> flatId = requirement.getAttributeValue(Attribute.FLAT_ID);

        boolean organizationIdOk = false;
        boolean flatIdentifierFound = false;
        boolean idOk = false;
        boolean conflict = false;

        if (flatId.isPresent()) {
            idOk = repository.existsById(flatId.get());
        }

        if (flatIdentifier.isPresent()) {
            flatIdentifierFound = repository.existsByIdentifier(flatIdentifier.get());
        }

        if (organizationId.isPresent()) {
            organizationIdOk = organizationService.existsById(organizationId.get());
        } else {
            if (flatId.isPresent()) {
                Optional<Flat> flat = repository.findById(flatId.get());
                if (flat.isPresent()) {
                    if (Optional.ofNullable(flat.get().getOrganization()).isPresent()) {
                        organizationIdOk = organizationService.existsById(flat.get().getOrganization().getOrganizationId());
                    }
                }
            }
        }

        if (flatIdentifierFound && organizationIdOk) {
            conflict = organizationContainsThisFlat(flatIdentifier, organizationId);
        }

        return new FlatUpdateBusinessRule(organizationIdOk, conflict, idOk);
    }

    @Override
    protected void mergeModel(Requirement<FlatUpdateBusinessRule> requirement) {
        Optional<Flat> flat = findById(requirement.getAttributeValue(Attribute.FLAT_ID).get());
        Optional<Organization> organizaton = organizationService.findById(requirement.getAttributeValue(Attribute.ORGANIZATION_ID).get());
        if (flat.isPresent() && organizaton.isPresent()) {
            fillInCommonCrudAttributes(requirement, flat.get());
            flat.get().setOrganization(organizaton.get());
            repository.save(flat.get());
        }
    }

    @Override
    public Requirement<FlatUpdateBusinessRule> getRequirementCrudUdate() {
        return new Requirement<>(AttributeTag.FLAT_CRUD_UPDATE);
    }

    @Override
    public String getModelId(Flat model) {
        return model.getFlatId();
    }

    private void fillInCommonCrudAttributes(Requirement requirement, Flat flat) {
        flat.setIdentifier((String) requirement.getAttributeValue(Attribute.FLAT_IDENTIFIER).get());
    }

    public boolean organizationContainsThisFlat(Optional<String> flatIdentifier, Optional<String> organizationId) {
        Optional<Organization> organization = organizationService.findById(organizationId.get());
        if (organization.isPresent()) {
            Optional<List<Flat>> organizationFlats = Optional.ofNullable(organization.get().getFlatList());
            if (organizationFlats.isPresent()) {
                if (organizationFlats.get().stream().anyMatch((flat) -> (flat.getIdentifier().equals(flatIdentifier.get())))) {
                    return true;
                }
            }
        }
        return false;
    }
}
