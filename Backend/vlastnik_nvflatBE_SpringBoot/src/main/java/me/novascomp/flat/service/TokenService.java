package me.novascomp.flat.service;

import java.util.ArrayList;
import java.util.List;
import me.novascomp.utils.service.GeneralService;
import java.util.Optional;
import java.util.Random;
import me.novascomp.flat.config.ScopeEnum;
import me.novascomp.flat.model.Flat;
import me.novascomp.flat.model.General;
import me.novascomp.flat.model.Resident;
import me.novascomp.flat.model.Scope;
import me.novascomp.flat.model.Token;
import me.novascomp.flat.model.User;
import me.novascomp.flat.repository.TokenRepository;
import me.novascomp.flat.service.business.rules.TokenCreateBusinessRule;
import me.novascomp.flat.service.business.rules.TokenUpdateBusinessRule;
import me.novascomp.utils.standalone.service.components.Requirement;
import me.novascomp.utils.standalone.attributes.Attribute;
import me.novascomp.utils.standalone.attributes.AttributeTag;
import me.novascomp.utils.standalone.service.exceptions.BadRequestException;
import me.novascomp.utils.standalone.service.exceptions.ConflictException;
import me.novascomp.utils.standalone.service.exceptions.NotFoundException;
import me.novascomp.utils.standalone.service.exceptions.OKException;
import me.novascomp.utils.standalone.service.exceptions.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class TokenService extends GeneralService<Token, TokenRepository, TokenCreateBusinessRule, TokenUpdateBusinessRule> {

    private final UserService userService;
    private final ScopeService scopeService;
    private final FlatService flatService;
    private final ResidentService residentService;

    @Autowired
    public TokenService(UserService userService, ScopeService scopeService, FlatService flatService, ResidentService residentService) {
        this.userService = userService;
        this.scopeService = scopeService;
        this.flatService = flatService;
        this.residentService = residentService;
    }

    public List<Token> findByUserId(User userId) {
        return repository.findByUserId(userId);
    }

    public Page<Token> findByFlat(Flat flat, Pageable pageable) {
        return repository.findByFlat(flat, pageable);
    }

    public UserService getUserService() {
        return userService;
    }

    public ScopeService getScopeService() {
        return scopeService;
    }

    public ScopeEnum[] forbiddenPostScopesForToken() {
        return new ScopeEnum[]{ScopeEnum.SCOPE_FLAT_OWNER};
    }

    public String processTokenAddRequest(String key, Jwt principal) throws ServiceException {
        final Optional<Token> tokenRecord = findByKey(key);
        final Optional<User> user = getUserService().findByUid(UserService.getUserUidByPrincipal(principal));

        if (tokenRecord.isPresent() && user.isPresent()) {
            for (Token userToken : user.get().getTokenList()) {
                if (userToken.getFlat().getIdentifier().equals(tokenRecord.get().getFlat().getIdentifier())) {
                    if (userToken.getFlat().getOrganization().getOrganizationId().equals(tokenRecord.get().getFlat().getOrganization().getOrganizationId())) {
                        throw new ConflictException("");
                    }
                }
            }

            addUserToToken(tokenRecord.get().getTokenId(), user.get().getUserId());
            return tokenRecord.get().getFlatId();
        }

        throw new BadRequestException("");
    }

    public Optional<Token> findByKey(String key) {
        return repository.findByKey(key);
    }

    public List<Token> getUserTokens(String userUid) throws ServiceException {
        if (Optional.ofNullable(userUid).isEmpty()) {
            throw new BadRequestException("");
        }
        Optional<User> user = userService.findByUid(userUid);
        if (user.isPresent()) {
            return user.get().getTokenList();
        }
        throw new NotFoundException("");
    }

    public List<Token> allTokensAssociatedWithFlatId(String flatId) throws ServiceException {
        if (Optional.ofNullable(flatId).isEmpty()) {
            throw new BadRequestException("");
        }

        Optional<Flat> flat = flatService.findById(flatId);
        if (flat.isPresent()) {
            return flat.get().getTokenList();
        }

        throw new NotFoundException("");
    }

    public void addScopeToToken(String tokenId, String scopeId) throws ServiceException {

        if (Optional.ofNullable(tokenId).isEmpty() || Optional.ofNullable(scopeId).isEmpty()) {
            throw new BadRequestException("");
        }

        Optional<Token> token = this.findById(tokenId);
        Optional<Scope> scope = scopeService.findById(scopeId);

        if (token.isPresent() && scope.isPresent()) {
            if (Optional.ofNullable(token.get().getScopeList()).isEmpty()) {
                token.get().setScopeList(new ArrayList());
            }
            if (!token.get().getScopeList().contains(scope.get())) {
                token.get().getScopeList().add(scope.get());
                repository.save(token.get());
            } else {
                throw new OKException("");
            }
        } else {
            throw new NotFoundException("");
        }
    }

    public void removeScopeFromToken(String tokenId, String scopeId) throws ServiceException {

        if (Optional.ofNullable(tokenId).isEmpty() || Optional.ofNullable(scopeId).isEmpty()) {
            throw new BadRequestException("");
        }

        Optional<Token> token = this.findById(tokenId);
        Optional<Scope> scope = scopeService.findById(scopeId);

        if (token.isPresent() && scope.isPresent()) {
            if (token.get().getScopeList().contains(scope.get())) {
                token.get().getScopeList().remove(scope.get());
                repository.save(token.get());
            }
        } else {
            throw new NotFoundException("");
        }
    }

    public Page<Scope> getTokenByIdScopes(String tokenId, Pageable pageable) {
        List<Token> tokenList = new ArrayList<>();
        tokenList.add(findById(tokenId).get());
        return scopeService.findDistinctByTokenListIn(tokenList, pageable);
    }

    public Optional<Scope> getScopeIncludingToken(String tokenId, String scopeId) {

        if (Optional.ofNullable(tokenId).isEmpty() || Optional.ofNullable(scopeId).isEmpty()) {
            return Optional.ofNullable(null);
        }

        Optional<Token> token = this.findById(tokenId);

        if (token.isPresent()) {
            for (Scope scope : token.get().getScopeList()) {
                if (scope.getScopeId().equals(scopeId)) {
                    return Optional.ofNullable(scope);
                }
            }
        }
        return Optional.ofNullable(null);
    }

    public void addTokenToResident(String tokenId, String residentId) throws ServiceException {

        if (Optional.ofNullable(tokenId).isEmpty() || Optional.ofNullable(residentId).isEmpty()) {
            throw new BadRequestException("");
        }

        Optional<Token> token = findById(tokenId);
        Optional<Resident> resident = residentService.findById(residentId);

        if (token.isPresent() && resident.isPresent()) {
            if (!resident.get().getTokenList().contains(token.get())) {
                if (Optional.ofNullable(token.get().getResidentId()).isPresent()) {
                    throw new OKException("");
                }
                token.get().setResidentId(resident.get());
                repository.save(token.get());
            } else {
                throw new ConflictException("");
            }
        } else {
            throw new NotFoundException("");
        }
    }

    public void removeResidentFromToken(String tokenId, String residentId) throws ServiceException {

        if (Optional.ofNullable(tokenId).isEmpty() || Optional.ofNullable(residentId).isEmpty()) {
            throw new BadRequestException("");
        }

        Optional<Token> token = findById(tokenId);
        Optional<Resident> resident = residentService.findById(residentId);

        if (token.isPresent() && resident.isPresent()) {
            if (Optional.ofNullable(token.get().getResidentId()).isEmpty()) {
                throw new OKException("");
            }
            token.get().setResidentId(null);
            repository.save(token.get());
        } else {
            throw new NotFoundException("");
        }
    }

    public void addUserToToken(String tokenId, String userId) throws ServiceException {

        if (Optional.ofNullable(tokenId).isEmpty() || Optional.ofNullable(userId).isEmpty()) {
            throw new BadRequestException("");
        }

        Optional<Token> token = this.findById(tokenId);
        Optional<User> user = userService.findById(userId);

        if (token.isPresent() && user.isPresent()) {
            if (Optional.ofNullable(token.get().getUserId()).isPresent()) {
                if (token.get().getUserId().getUserId().equals(user.get().getUserId())) {
                    throw new OKException("");
                } else {
                    throw new ConflictException("");
                }
            }
            token.get().setUserId(user.get());
            repository.save(token.get());
        } else {
            throw new NotFoundException("");
        }

    }

    public void removeUserFromToken(String tokenId, String userId) throws ServiceException {

        if (tokenId.isEmpty()) {
            throw new BadRequestException("");
        }

        Optional<Token> token = this.findById(tokenId);
        Optional<User> user = userService.findById(userId);

        if (token.isPresent() && user.isPresent()) {
            if (Optional.ofNullable(token.get().getUserId()).isEmpty()) {
                throw new OKException("");
            }
            token.get().setUserId(null);
            repository.save(token.get());
        } else {
            throw new NotFoundException("");
        }
    }

    public void removeAllTokensContainingFlat(Optional<String> flatId) {
        Optional<Flat> flat = flatService.findById(flatId.get());

        if (flat.isPresent()) {
            flat.get().getTokenList().forEach((token) -> {
                this.delete(token);
            });
        }
    }

    public String generateTokenKey(String prefix) {

        String key = prefix;
        Random rd = new Random();
        final short keyLength = 30;
        final short dashPosition = 6;

        for (int i = (prefix.length() + 1); i <= keyLength; i++) {

            if (i % dashPosition == 0) {

                if (i < keyLength) {
                    key += "-";
                }
            } else if (i == 19) {
                for (int randomKey = 0; randomKey <= 4; randomKey++) {
                    key += rd.nextInt(9);
                }
                break;
            } else {
                char newChar;
                do {
                    newChar = (char) rd.nextInt(256);
                } while (!((newChar >= 'A' && newChar <= 'Z')
                        || (newChar >= '0' && newChar <= '9')));
                key += newChar;
            }
        }
        return key;
    }

    @Override
    protected TokenCreateBusinessRule getBusinessRuleCrudCreate(Requirement<TokenCreateBusinessRule> requirement) {

        Optional<String> key = requirement.getAttributeValue(Attribute.KEY);
        Optional<String> flatId = requirement.getAttributeValue(Attribute.FLAT_ID);

        boolean foundInDatabase = false;
        boolean flatIdOk = false;

        if (key.isPresent()) {
            foundInDatabase = repository.existsByKey(key.get());
        }

        if (flatId.isPresent()) {
            flatIdOk = flatService.existsById(flatId.get());
        }

        return new TokenCreateBusinessRule(flatIdOk, foundInDatabase);
    }

    @Override
    protected Token addModel(String id, Requirement<TokenCreateBusinessRule> requirement) {
        Token token = new Token(id);
        fillInCommonCrudAttributes(requirement, token);

        Optional<Flat> flat = flatService.findById(requirement.getAttributeValue(Attribute.FLAT_ID).get());

        if (flat.isPresent()) {
            token.setFlat(flat.get());
        }

        General general = restUtils.getGeneral(id);
        token.setGeneral(general);
        repository.save(token);
        return token;
    }

    @Override
    public Requirement<TokenCreateBusinessRule> getRequirementCrudCreate() {
        return new Requirement<>(AttributeTag.TOKEN_CRUD_CREATE);
    }

    @Override
    protected TokenUpdateBusinessRule getBusinessRuleCrudUpdate(Requirement<TokenUpdateBusinessRule> requirement) {

        Optional<String> key = requirement.getAttributeValue(Attribute.KEY);
        Optional<String> tokenId = requirement.getAttributeValue(Attribute.TOKEN_ID);
        Optional<String> flatId = requirement.getAttributeValue(Attribute.FLAT_ID);

        boolean idOk = false;
        boolean conflict = false;
        boolean flatIdOk = false;

        Optional<Token> tokenRecordById = Optional.ofNullable(null);
        Optional<Token> tokenRecordByKey = Optional.ofNullable(null);

        if (tokenId.isPresent()) {
            tokenRecordById = findById(tokenId.get());
            idOk = tokenRecordById.isPresent();
        }

        if (key.isPresent()) {
            tokenRecordByKey = repository.findByKey(key.get());
        }

        if (tokenId.isPresent() && key.isPresent()) {
            if (tokenRecordById.isPresent() && tokenRecordByKey.isPresent()) {
                if (tokenRecordById.get().equals(tokenRecordByKey.get())) {
                    conflict = true;
                }
            }
        }

        if (flatId.isPresent()) {
            flatIdOk = flatService.existsById(flatId.get());
        }

        return new TokenUpdateBusinessRule(flatIdOk, conflict, idOk);
    }

    @Override
    protected void mergeModel(Requirement<TokenUpdateBusinessRule> requirement) {
        Optional<Token> token = findById(requirement.getAttributeValue(Attribute.TOKEN_ID).get());
        Optional<Flat> flat = flatService.findById(requirement.getAttributeValue(Attribute.FLAT_ID).get());
        if (token.isPresent() && flat.isPresent()) {
            fillInCommonCrudAttributes(requirement, token.get());
            token.get().setFlat(flat.get());
            repository.save(token.get());
        }
    }

    @Override
    public Requirement<TokenUpdateBusinessRule> getRequirementCrudUdate() {
        return new Requirement<>(AttributeTag.TOKEN_CRUD_UPDATE);
    }

    @Override
    public String getModelId(Token model) {
        return model.getTokenId();
    }

    private void fillInCommonCrudAttributes(Requirement requirement, Token token) {
        token.setKey((String) requirement.getAttributeValue(Attribute.KEY).get());
    }

}
