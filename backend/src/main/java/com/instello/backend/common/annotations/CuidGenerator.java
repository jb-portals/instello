package com.instello.backend.common.annotations;



import io.github.thibaultmeyer.cuid.CUID;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;

import java.io.Serializable;

public class CuidGenerator implements IdentifierGenerator {

    @Override
    public Serializable generate(SharedSessionContractImplementor session, Object object) {
        return CUID.randomCUID2().toString();
    }
}
