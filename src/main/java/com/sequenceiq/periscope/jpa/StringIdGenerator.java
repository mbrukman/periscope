package com.sequenceiq.periscope.jpa;

import static org.apache.commons.lang3.StringUtils.deleteWhitespace;
import static org.apache.commons.lang3.StringUtils.substring;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Properties;

import org.hibernate.dialect.Dialect;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.id.IdentifierGeneratorHelper;
import org.hibernate.id.IntegralDataTypeHolder;
import org.hibernate.id.SequenceGenerator;
import org.hibernate.type.Type;

public class StringIdGenerator extends SequenceGenerator {

    public static final String FIELD_NAME = "fieldName";
    public static final String BASE_NAME = "baseName";
    private static final int MAX_LENGTH = 10;
    private String fieldName;
    private String baseName;

    @Override
    public void configure(Type type, Properties params, Dialect dialect) {
        super.configure(type, params, dialect);
        this.fieldName = params.getProperty(FIELD_NAME);
        this.baseName = params.getProperty(BASE_NAME);
    }

    @Override
    public Serializable generate(SessionImplementor session, Object obj) {
        Number sequence = (Number) super.generate(session, obj);
        String key;
        if (fieldName != null) {
            try {
                Field field = obj.getClass().getDeclaredField(fieldName);
                field.setAccessible(true);
                key = String.valueOf(field.get(obj));
            } catch (Exception e) {
                key = "";
            }
        } else {
            key = baseName;
        }
        return substring(deleteWhitespace(key.toLowerCase()), 0, MAX_LENGTH) + sequence;
    }

    @Override
    protected IntegralDataTypeHolder buildHolder() {
        return IdentifierGeneratorHelper.getIntegralDataTypeHolder(Long.class);
    }
}
