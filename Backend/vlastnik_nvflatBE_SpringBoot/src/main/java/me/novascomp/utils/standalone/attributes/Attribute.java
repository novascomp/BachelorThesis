package me.novascomp.utils.standalone.attributes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

public enum Attribute {
    //USER
    USER_UID("uid", new AttributeTag[]{AttributeTag.USER_CRUD_CREATE, AttributeTag.USER_CRUD_UPDATE}, new AttributeConstraint[]{AttributeConstraint.MAX_LENGTH_30}),
    USER_ID("userId", new AttributeTag[]{AttributeTag.USER_CRUD_UPDATE}, new AttributeConstraint[]{AttributeConstraint.EXACT_LENGTH_36}),
    //TOKEN
    KEY("key", new AttributeTag[]{AttributeTag.TOKEN_CRUD_CREATE, AttributeTag.TOKEN_CRUD_UPDATE}, new AttributeConstraint[]{AttributeConstraint.TOKEN_KEY}),
    TOKEN_ID("tokenId", new AttributeTag[]{AttributeTag.TOKEN_CRUD_UPDATE}, new AttributeConstraint[]{AttributeConstraint.EXACT_LENGTH_36}),
    //SCOPE
    SCOPE("scope", new AttributeTag[]{AttributeTag.SCOPE_CRUD_CREATE, AttributeTag.SCOPE_CRUD_UPDATE}, new AttributeConstraint[]{AttributeConstraint.MAX_LENGTH_30}),
    SCOPE_ID("scopeId", new AttributeTag[]{AttributeTag.SCOPE_CRUD_UPDATE}, new AttributeConstraint[]{AttributeConstraint.EXACT_LENGTH_36}),
    //ORGANIZATION
    ICO("ico", new AttributeTag[]{AttributeTag.ORGANIZATION_CRUD_CREATE, AttributeTag.ORGANIZATION_CRUD_UPDATE}, new AttributeConstraint[]{AttributeConstraint.EXACT_LENGTH_8_DIGIT}),
    //FLAT
    FLAT_IDENTIFIER("identifier", new AttributeTag[]{AttributeTag.FLAT_CRUD_CREATE, AttributeTag.FLAT_CRUD_UPDATE}, new AttributeConstraint[]{AttributeConstraint.MAX_LENGTH_30}),
    ORGANIZATION_ID("organizationId", new AttributeTag[]{AttributeTag.ORGANIZATION_CRUD_CREATE, AttributeTag.ORGANIZATION_CRUD_UPDATE, AttributeTag.FLAT_CRUD_CREATE, AttributeTag.FLAT_CRUD_UPDATE}, new AttributeConstraint[]{AttributeConstraint.EXACT_LENGTH_36}),
    //DETAIL
    SIZE("size", new AttributeTag[]{AttributeTag.DETAIL_CRUD_CREATE, AttributeTag.DETAIL_CRUD_UPDATE}, new AttributeConstraint[]{AttributeConstraint.MAX_LENGTH_30}),
    COMMON_SHARE_SIZE("commonShareSize", new AttributeTag[]{AttributeTag.DETAIL_CRUD_CREATE, AttributeTag.DETAIL_CRUD_UPDATE}, new AttributeConstraint[]{AttributeConstraint.MAX_LENGTH_30}),
    FLAT_ID("flatId", new AttributeTag[]{AttributeTag.TOKEN_CRUD_CREATE, AttributeTag.TOKEN_CRUD_UPDATE, AttributeTag.FLAT_CRUD_UPDATE, AttributeTag.DETAIL_CRUD_CREATE, AttributeTag.DETAIL_CRUD_UPDATE}, new AttributeConstraint[]{AttributeConstraint.EXACT_LENGTH_36}),
    DETAIL_ID("detailId", new AttributeTag[]{AttributeTag.DETAIL_CRUD_UPDATE, AttributeTag.RESIDENT_CRUD_CREATE}, new AttributeConstraint[]{AttributeConstraint.EXACT_LENGTH_36}),
    //RESIDENT
    FIRST_NAME("firstName", new AttributeTag[]{AttributeTag.RESIDENT_CRUD_CREATE, AttributeTag.RESIDENT_CRUD_UPDATE}, new AttributeConstraint[]{AttributeConstraint.MAX_LENGTH_30}),
    LAST_NAME("lastName", new AttributeTag[]{AttributeTag.RESIDENT_CRUD_CREATE, AttributeTag.RESIDENT_CRUD_UPDATE}, new AttributeConstraint[]{AttributeConstraint.MAX_LENGTH_30}),
    EMAIL("email", new AttributeTag[]{AttributeTag.RESIDENT_CRUD_CREATE, AttributeTag.RESIDENT_CRUD_UPDATE}, new AttributeConstraint[]{AttributeConstraint.ENAIL_PATTERN, AttributeConstraint.MAX_LENGTH_250}),
    PHONE_NUMBER("phone", new AttributeTag[]{AttributeTag.RESIDENT_CRUD_CREATE, AttributeTag.RESIDENT_CRUD_UPDATE}, new AttributeConstraint[]{AttributeConstraint.PHONE_PATTERN, AttributeConstraint.MAX_LENGTH_30}),
    DATE_OF_BIRTH("dateOfBirth", new AttributeTag[]{AttributeTag.RESIDENT_CRUD_CREATE, AttributeTag.RESIDENT_CRUD_UPDATE}, new AttributeConstraint[]{}),
    RESIDET_ID("residentId", new AttributeTag[]{AttributeTag.RESIDENT_CRUD_UPDATE}, new AttributeConstraint[]{AttributeConstraint.EXACT_LENGTH_36}),
    //MESSAGE_NVM
    MESSAGE_RECORD_NVM_ID("messageRecordNvmId", new AttributeTag[]{AttributeTag.MESSAGE_RECORD_NVM_UPDATE}, new AttributeConstraint[]{AttributeConstraint.EXACT_LENGTH_36}),
    MESSAGE_ID_IN_NVM("idInNvm", new AttributeTag[]{AttributeTag.MESSAGE_RECORD_NVM_CREATE, AttributeTag.MESSAGE_RECORD_NVM_UPDATE}, new AttributeConstraint[]{AttributeConstraint.EXACT_LENGTH_36});

    private final String attributeName;
    private final ArrayList<AttributeTag> tags;
    private final ArrayList<AttributeConstraint> requiredConstraints;

    Attribute(String attributeName, AttributeTag[] tags, AttributeConstraint[] requiredConstraints) {
        this.attributeName = attributeName;
        this.tags = new ArrayList<>();
        this.requiredConstraints = new ArrayList<>();
        this.tags.addAll(Arrays.asList(tags));
        this.requiredConstraints.addAll(Arrays.asList(requiredConstraints));
    }

    public String getAttributeName() {
        return this.attributeName;
    }

    public ArrayList<AttributeTag> getTags() {
        return tags;
    }

    public ArrayList<AttributeConstraint> checkConstraints(String value) {

        Optional<String> optional = Optional.ofNullable(value);

        ArrayList<AttributeConstraint> listOfConstraints = new ArrayList<>();

        if (optional.isEmpty()) {
            listOfConstraints.add(AttributeConstraint.NULL);
        } else {
            requiredConstraints.stream().filter((constraint) -> (!constraint.checkPattern(value))).forEachOrdered((constraint) -> {
                listOfConstraints.add(constraint);
            });
        }

        return listOfConstraints;
    }

    @Override
    public String toString() {
        return "Attribute{" + "attributeName=" + attributeName + ", requiredConstraints=" + requiredConstraints.toString() + '}';
    }

}
