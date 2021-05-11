package me.novascomp.flat.service;

import java.util.List;
import me.novascomp.utils.service.GeneralService;
import java.util.Optional;
import me.novascomp.flat.config.ScopeEnum;
import me.novascomp.flat.model.General;
import me.novascomp.flat.model.Scope;
import me.novascomp.flat.model.Token;
import me.novascomp.flat.repository.ScopeRepository;
import me.novascomp.flat.service.business.rules.ScopeCreateBusinessRule;
import me.novascomp.flat.service.business.rules.ScopeUpdateBusinessRule;
import me.novascomp.utils.standalone.service.components.Requirement;
import me.novascomp.utils.standalone.attributes.Attribute;
import me.novascomp.utils.standalone.attributes.AttributeTag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ScopeService extends GeneralService<Scope, ScopeRepository, ScopeCreateBusinessRule, ScopeUpdateBusinessRule> {

    public ScopeEnum[] forbiddenScopesForToken() {
        return new ScopeEnum[]{ScopeEnum.SCOPE_READ_BY_FLAT_OWNER, ScopeEnum.SCOPE_READ_BY_FLAT_RESIDENT};
    }

    public Optional<Scope> findByScope(String scope) {
        return repository.findByScope(scope);
    }

    public Page<Scope> findDistinctByTokenListIn(List<Token> tokenList, Pageable pageable) {
        return repository.findDistinctByTokenListIn(tokenList, pageable);
    }

    @Override
    protected ScopeCreateBusinessRule getBusinessRuleCrudCreate(Requirement<ScopeCreateBusinessRule> requirement) {

        Optional<String> scope = requirement.getAttributeValue(Attribute.SCOPE);
        boolean foundInDatabase = false;

        if (scope.isPresent()) {
            foundInDatabase = repository.existsByScope(scope.get());
        }

        return new ScopeCreateBusinessRule(foundInDatabase);
    }

    @Override
    protected Scope addModel(String id, Requirement<ScopeCreateBusinessRule> requirement) {
        Scope scope = new Scope(id);
        fillInCommonCrudAttributes(requirement, scope);
        General general = restUtils.getGeneral(id);
        scope.setGeneral(general);
        repository.save(scope);
        return scope;
    }

    @Override
    public Requirement<ScopeCreateBusinessRule> getRequirementCrudCreate() {
        return new Requirement<>(AttributeTag.SCOPE_CRUD_CREATE);
    }

    @Override
    protected ScopeUpdateBusinessRule getBusinessRuleCrudUpdate(Requirement<ScopeUpdateBusinessRule> requirement) {

        Optional<String> scope = requirement.getAttributeValue(Attribute.SCOPE);
        Optional<String> scopeId = requirement.getAttributeValue(Attribute.SCOPE_ID);

        boolean idOk = false;
        boolean conflict = false;

        if (scopeId.isPresent()) {
            idOk = repository.existsById(scopeId.get());
        }

        if (scope.isPresent()) {
            conflict = repository.existsByScope(scope.get());
        }

        return new ScopeUpdateBusinessRule(conflict, idOk);
    }

    @Override
    protected void mergeModel(Requirement<ScopeUpdateBusinessRule> requirement) {
        Optional<Scope> scope = findById(requirement.getAttributeValue(Attribute.SCOPE_ID).get());
        if (scope.isPresent()) {
            fillInCommonCrudAttributes(requirement, scope.get());
            repository.save(scope.get());
        }
    }

    @Override
    public Requirement<ScopeUpdateBusinessRule> getRequirementCrudUdate() {
        return new Requirement<>(AttributeTag.SCOPE_CRUD_UPDATE);
    }

    @Override
    public String getModelId(Scope model) {
        return model.getScopeId();
    }

    private void fillInCommonCrudAttributes(Requirement requirement, Scope scope) {
        scope.setScope((String) requirement.getAttributeValue(Attribute.SCOPE).get());
    }
}
