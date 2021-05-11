package me.novascomp.utils.standalone.service.components;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import me.novascomp.utils.standalone.attributes.Attribute;
import me.novascomp.utils.standalone.attributes.AttributeTag;

public class Requirement<Rule extends BusinessRule> {

    private final List<Attribute> attributes;
    private final Map<Attribute, String> requiredValues;
    private final Map<String, Boolean> attributesStructureValidity;
    private final AttributeTag nTag;

    public Requirement(AttributeTag nTag) {
        attributes = new ArrayList<>();
        requiredValues = new HashMap();
        attributesStructureValidity = new HashMap();
        this.nTag = nTag;
        initAttributes();
    }

    public void setAttribute(Attribute attribute, String value) {
        if (attributes.contains(attribute)) {
            requiredValues.put(attribute, value);
        }
    }

    public Optional<String> getAttributeValue(Attribute attribute) {
        if (attributes.contains(attribute)) {
            return Optional.ofNullable(requiredValues.get(attribute));
        }
        return Optional.ofNullable(null);
    }

    public Map<String, Boolean> getAttributesStructureValidity() {
        return attributesStructureValidity;
    }

    public boolean areAttributesValid() {
        Iterator it = requiredValues.entrySet().iterator();
        boolean response = true;
        while (it.hasNext()) {
            Map.Entry<Attribute, String> pair = (Map.Entry) it.next();
            if (!pair.getKey().checkConstraints(pair.getValue()).isEmpty()) {
                attributesStructureValidity.put(pair.getKey().getAttributeName(), Boolean.FALSE);
                response = false;
            } else {
                attributesStructureValidity.put(pair.getKey().getAttributeName(), Boolean.TRUE);
            }
        }
        return response;
    }

    public boolean isValid(Rule rule) {
        return areAttributesValid() && rule.isValid();
    }

    public boolean isValid() {
        return false;
    }

    private void initAttributes() {
        for (Attribute attribute : Attribute.values()) {
            if (attribute.getTags().contains(nTag)) {
                attributes.add(attribute);
            }
        }
    }

    @Override
    public String toString() {
        return "Requirement{" + "requiredValues=" + requiredValues.toString() + ", nTag=" + nTag + '}';
    }

}
