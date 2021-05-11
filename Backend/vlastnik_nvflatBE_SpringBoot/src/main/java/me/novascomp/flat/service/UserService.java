package me.novascomp.flat.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import me.novascomp.utils.service.GeneralService;
import me.novascomp.flat.model.General;
import me.novascomp.flat.model.User;
import me.novascomp.flat.repository.UserRepository;
import me.novascomp.flat.service.business.rules.UserCreateBusinessRule;
import me.novascomp.flat.service.business.rules.UserUpdateBusinessRule;
import me.novascomp.utils.standalone.attributes.Attribute;
import me.novascomp.utils.standalone.attributes.AttributeTag;
import me.novascomp.utils.standalone.service.components.Requirement;
import me.novascomp.utils.standalone.service.components.GeneralCreateResponse;

@Service
public class UserService extends GeneralService<User, UserRepository, UserCreateBusinessRule, UserUpdateBusinessRule> {

    private final Map<String, Boolean> added;

    public UserService() {
        this.added = new HashMap();
    }

    public synchronized void registerUserToApp(Jwt principal) {
        String uid = getUserUidByPrincipal(principal);

        if (repository.findByUid(uid).isEmpty() && Optional.ofNullable(added.get(uid)).isEmpty()) {
            added.put(uid, true);
            Requirement<UserCreateBusinessRule> requirement = getRequirementCrudCreate();
            requirement.setAttribute(Attribute.USER_UID, uid);

            GeneralCreateResponse response = createModel(requirement);

            if (Optional.ofNullable(response).isPresent()) {
                if (response.isSuccessful()) {
                    LOG.log(Level.INFO, "New user added with uid: {0}", uid);
                }
            }
        }
    }

    public static String getUserUidByPrincipal(Jwt principal) {

        String uid = principal.getClaimAsString("uid");
        String cid = principal.getClaimAsString("cid");
        String userUid;

        if (uid == null) {
            userUid = cid;
        } else {
            userUid = uid;
        }

        return userUid;
    }

    public static List<String> getUserScopesPrincipal(Jwt principal) {
        return principal.getClaimAsStringList("scp");
    }

    public Optional<User> findByUid(String uid) {
        return repository.findByUid(uid);
    }

    @Override
    protected UserCreateBusinessRule getBusinessRuleCrudCreate(Requirement<UserCreateBusinessRule> requirement) {
        Optional<String> uid = requirement.getAttributeValue(Attribute.USER_UID);

        boolean foundInDatabase = false;

        if (uid.isPresent()) {
            foundInDatabase = repository.findByUid(uid.get()).isPresent();
        }

        return new UserCreateBusinessRule(foundInDatabase);
    }

    @Override
    protected User addModel(String id, Requirement<UserCreateBusinessRule> requirement) {
        User user = new User(id);
        fillInCommonCrudAttributes(requirement, user);
        General general = restUtils.getGeneral(id);
        user.setGeneral(general);
        repository.save(user);
        return user;
    }

    @Override
    public Requirement<UserCreateBusinessRule> getRequirementCrudCreate() {
        return new Requirement<>(AttributeTag.USER_CRUD_CREATE);
    }

    @Override
    protected UserUpdateBusinessRule getBusinessRuleCrudUpdate(Requirement<UserUpdateBusinessRule> requirement) {
        Optional<String> uid = requirement.getAttributeValue(Attribute.USER_UID);
        Optional<String> userId = requirement.getAttributeValue(Attribute.USER_ID);

        boolean idOk = false;
        boolean conflict = false;

        if (userId.isPresent()) {
            idOk = repository.existsById(userId.get());
        }

        if (uid.isPresent()) {
            conflict = repository.existsByUid(uid.get());
        }

        return new UserUpdateBusinessRule(conflict, idOk);
    }

    @Override
    protected void mergeModel(Requirement<UserUpdateBusinessRule> requirement) {
        Optional<User> user = findById(requirement.getAttributeValue(Attribute.USER_ID).get());
        if (user.isPresent()) {
            fillInCommonCrudAttributes(requirement, user.get());
            repository.save(user.get());
        }
    }

    @Override
    public Requirement<UserUpdateBusinessRule> getRequirementCrudUdate() {
        return new Requirement<>(AttributeTag.USER_CRUD_UPDATE);
    }

    @Override
    public String getModelId(User model) {
        return model.getUserId();
    }

    private void fillInCommonCrudAttributes(Requirement requirement, User user) {
        user.setUid((String) requirement.getAttributeValue(Attribute.USER_UID).get());
    }

}
