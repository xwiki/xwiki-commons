package org.xwiki.filter.internal.converter;

import java.lang.reflect.Type;
import java.util.Map;

import javax.inject.Singleton;

import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.filter.FilterEventParameters;
import org.xwiki.properties.converter.AbstractConverter;
import org.xwiki.properties.converter.ConversionException;

/**
 * Converter that converts a value into an {@link FilterEventParameters} object.
 * 
 * @version $Id$
 * @since 5.2M2
 */
// TODO: add real syntax support (only convert from empty String to empty Map right now)
@Component
@Singleton
public class FilterElementParametersConverter extends AbstractConverter<FilterEventParameters>
{
    @Override
    protected FilterEventParameters convertToType(Type type, Object value)
    {
        if (value == null) {
            return null;
        }

        FilterEventParameters parameters;

        if (value instanceof FilterEventParameters) {
            parameters = (FilterEventParameters) value;
        } else if (value instanceof Map) {
            parameters = new FilterEventParameters();
            parameters.putAll((Map) value);
        } else {
            String parametersString = value.toString().trim();

            if (StringUtils.isEmpty(parametersString)) {
                parameters = FilterEventParameters.EMPTY;
            } else {
                throw new ConversionException("Only able to convert empty string to ["
                    + FilterEventParameters.class.getName() + "]");
            }
        }

        return parameters;
    }

    @Override
    protected String convertToString(FilterEventParameters value)
    {
        throw new ConversionException("Conversion from [" + FilterEventParameters.class.getName() + "] to ["
            + String.class.getName() + "] is not supported");
    }
}
