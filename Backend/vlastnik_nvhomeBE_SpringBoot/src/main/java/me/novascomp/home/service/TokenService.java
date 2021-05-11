package me.novascomp.home.service;

import java.util.List;
import me.novascomp.utils.service.GeneralService;
import java.util.Optional;
import java.util.Random;
import me.novascomp.home.model.General;
import me.novascomp.home.model.Token;
import me.novascomp.home.model.User;
import me.novascomp.home.repository.TokenRepository;
import me.novascomp.home.service.business.rules.TokenCreateBusinessRule;
import me.novascomp.home.service.business.rules.TokenUpdateBusinessRule;
import me.novascomp.home.model.Organization;
import me.novascomp.home.flat.uploader.NVHomeFlat;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

@Service
public class TokenService extends GeneralService<Token, TokenRepository, TokenCreateBusinessRule, TokenUpdateBusinessRule> {

    private final UserService userService;
    private final OrganizationService organizationService;

    @Autowired
    public TokenService(UserService userService, OrganizationService organizationService) {
        this.userService = userService;
        this.organizationService = organizationService;
    }

    public Page<Token> findByOrganization(Organization organization, Pageable pageable) {
        return repository.findByOrganization(organization, pageable);
    }

    public UserService getUserService() {
        return userService;
    }

    public String processTokenAddRequest(Token token, RegistrationService registrationService, Jwt principal) throws ServiceException {

        if (!Attribute.KEY.checkConstraints(token.getKey()).isEmpty()) {
            throw new BadRequestException("");
        }

        if (repository.existsByKey(token.getKey())) {
            RegistrationService.maxSVJCountCheck(principal, userService);
            final Optional<Token> tokenRecord = findByKey(token.getKey());
            final Optional<User> user = getUserService().findByUid(UserService.getUserUidByPrincipal(principal));

            if (tokenRecord.isPresent() && user.isPresent()) {
                user.get().getTokenList().stream().filter((userToken) -> (userToken.getOrganization().getOrganizationId().equals(tokenRecord.get().getOrganization().getOrganizationId()))).forEachOrdered((_item) -> {
                    throw new ConflictException("");
                });

                addUserToToken(tokenRecord.get().getTokenId(), user.get().getUserId());
                NVHomeFlat homeFlat = registrationService.getDefaultFlat(tokenRecord.get().getOrganization().getOrganizationId()).get();
                String defaultFlatToken = registrationService.getDefaultFlatToken(tokenRecord.get().getOrganization().getOrganizationId(), homeFlat.getFlatId(), tokenRecord.get().getKey());
                registrationService.addUserToDefaultFlat(principal, defaultFlatToken);
                return tokenRecord.get().getOrganization().getOrganizationId();
            }
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

    public void deleteOrganizationToken(Token token, String userUid, RegistrationService registrationService) throws ServiceException {
        if (findByOrganization(token.getOrganization(), PageRequest.of(0, Integer.MAX_VALUE)).getTotalElements() == 1) {
            throw new BadRequestException("");
        }
        if (Optional.ofNullable(token.getUserId()).isPresent()) {
            if (token.getUserId().getUid().equals(userUid)) {
                throw new BadRequestException("");
            }
        }

        delete(token);
        registrationService.deleteDefaulFlatToken(token.getOrganization().getOrganizationId(), token.getKey());
    }

    public boolean checkOrganizationTokenOwnerByOrganizationId(String userUid, String organizationId) {
        return getUserTokens(userUid).stream().anyMatch((token) -> (token.getOrganization().getOrganizationId().contains(organizationId)));
    }

    public boolean checkOrganizationTokenOwnerByTokenId(String userUid, String tokenId) {
        return getUserTokens(userUid).stream().anyMatch((token) -> (token.getTokenId().contains(tokenId)));
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

//    public void removeAllTokensContainingOrganization(Optional<String> organizationId) {
//        Optional<Flat> flat = flatService.findById(flatId.get());
//
//        if (flat.isPresent()) {
//            flat.get().getTokenList().forEach((token) -> {
//                this.delete(token);
//            });
//        }
//    }
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
        Optional<String> organizationId = requirement.getAttributeValue(Attribute.ORGANIZATION_ID);

        boolean foundInDatabase = false;
        boolean organizationIdOk = false;

        if (key.isPresent()) {
            foundInDatabase = repository.existsByKey(key.get());
        }

        if (organizationId.isPresent()) {
            organizationIdOk = organizationService.existsById(organizationId.get());
        }

        return new TokenCreateBusinessRule(organizationIdOk, foundInDatabase);
    }

    @Override
    protected Token addModel(String id, Requirement<TokenCreateBusinessRule> requirement) {
        Token token = new Token(id);
        fillInCommonCrudAttributes(requirement, token);

        Optional<Organization> organization = organizationService.findById(requirement.getAttributeValue(Attribute.ORGANIZATION_ID).get());

        if (organization.isPresent()) {
            token.setOrganization(organization.get());
        }

        General general = restUtils.getGeneral(id);
        token.setGeneral(general);
        repository.save(token);
        return token;
    }

    @Override
    public Requirement<TokenCreateBusinessRule> getRequirementCrudCreate() {
        return new Requirement<>(AttributeTag.NV_HOME_TOKEN_CRUD_CREATE);
    }

    @Override
    protected TokenUpdateBusinessRule getBusinessRuleCrudUpdate(Requirement<TokenUpdateBusinessRule> requirement) {

        Optional<String> key = requirement.getAttributeValue(Attribute.KEY);
        Optional<String> tokenId = requirement.getAttributeValue(Attribute.TOKEN_ID);
        Optional<String> organizationId = requirement.getAttributeValue(Attribute.ORGANIZATION_ID);

        boolean idOk = false;
        boolean conflict = false;
        boolean organizationIdOk = false;

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

        if (organizationId.isPresent()) {
            organizationIdOk = organizationService.existsById(organizationId.get());
        }

        return new TokenUpdateBusinessRule(organizationIdOk, conflict, idOk);
    }

    @Override
    protected void mergeModel(Requirement<TokenUpdateBusinessRule> requirement) {
        Optional<Token> token = findById(requirement.getAttributeValue(Attribute.TOKEN_ID).get());
        Optional<Organization> organization = organizationService.findById(requirement.getAttributeValue(Attribute.ORGANIZATION_ID).get());
        if (token.isPresent() && organization.isPresent()) {
            fillInCommonCrudAttributes(requirement, token.get());
            token.get().setOrganization(organization.get());
            repository.save(token.get());
        }
    }

    @Override
    public Requirement<TokenUpdateBusinessRule> getRequirementCrudUdate() {
        return new Requirement<>(AttributeTag.NV_HOME_TOKEN_CRUD_UPDATE);
    }

    @Override
    public String getModelId(Token model) {
        return model.getTokenId();
    }

    private void fillInCommonCrudAttributes(Requirement requirement, Token token) {
        token.setKey((String) requirement.getAttributeValue(Attribute.KEY).get());
    }

}
