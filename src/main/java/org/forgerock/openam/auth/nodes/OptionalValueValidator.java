package org.forgerock.openam.auth.nodes;

import com.sun.identity.sm.ServiceAttributeValidator;

import java.util.Set;

public class OptionalValueValidator implements ServiceAttributeValidator {

    public boolean validate(Set<String> values) {
        return true;
    }

}
