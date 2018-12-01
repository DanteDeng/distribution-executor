package com.nature.distribution.model;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

/**
 * model基类
 */
public class BaseModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
