package me.novascomp.flat.config;

import java.util.logging.Level;
import java.util.logging.Logger;
import me.novascomp.flat.service.ScopeService;
import me.novascomp.utils.standalone.attributes.Attribute;
import me.novascomp.utils.standalone.attributes.AttributeTag;
import me.novascomp.utils.standalone.service.components.GeneralCreateResponse;
import me.novascomp.utils.standalone.service.components.Requirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DatabaseInit {

    private final ScopeService scopeService;
    private static final Logger LOG = Logger.getLogger(DatabaseInit.class.getName());

    @Autowired
    public DatabaseInit(ScopeService scopeService) {
        this.scopeService = scopeService;
        addDefaultScopes();
    }

    public void addDefaultScopes() {
        for (ScopeEnum scopeEnum : ScopeEnum.values()) {
            addScope(scopeEnum);
        }

    }

    private void addScope(ScopeEnum scopeEnum) {
        Requirement requirement = new Requirement(AttributeTag.SCOPE_CRUD_CREATE);
        requirement.setAttribute(Attribute.SCOPE, scopeEnum.getScopeName());
        GeneralCreateResponse createResponse = scopeService.createModel(requirement);
        if (createResponse.isSuccessful()) {
            LOG.log(Level.INFO, "Scope added: {0}", scopeEnum.getScopeName());
        }
    }

}
