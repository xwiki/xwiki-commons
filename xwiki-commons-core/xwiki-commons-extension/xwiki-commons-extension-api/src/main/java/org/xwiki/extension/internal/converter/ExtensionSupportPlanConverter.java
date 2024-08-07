/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.extension.internal.converter;

import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.xwiki.component.annotation.Component;
import org.xwiki.configuration.ConversionException;
import org.xwiki.extension.ExtensionSupportPlan;
import org.xwiki.extension.ExtensionSupporter;
import org.xwiki.extension.internal.ExtensionFactory;
import org.xwiki.properties.converter.AbstractConverter;

/**
 * Convert an extension support plan from a String to an {@link ExtensionSupportPlan} object and the other way around.
 *
 * @version $Id$
 * @since 16.7.0RC1
 */
@Component
@Singleton
public class ExtensionSupportPlanConverter extends AbstractConverter<ExtensionSupportPlan>
{
    @Inject
    private ExtensionFactory factory;

    /**
     * @param values the values to convert
     * @return the list of {@link ExtensionSupportPlan}s created from the passed value
     * @throws MalformedURLException when failing to parse the URL
     */
    public static List<ExtensionSupportPlan> toExtensionSupportPlanList(Collection<?> values)
        throws MalformedURLException
    {
        return toExtensionSupportPlanList(values, null);
    }

    /**
     * @param values the values to convert
     * @param factory the factory used to de-duplicate instances
     * @return the list of {@link ExtensionSupportPlan}s created from the passed value
     * @throws MalformedURLException when failing to parse the URL
     */
    public static List<ExtensionSupportPlan> toExtensionSupportPlanList(Collection<?> values, ExtensionFactory factory)
        throws MalformedURLException
    {
        List<ExtensionSupportPlan> list = new ArrayList<>(values.size());

        for (Object value : values) {
            list.add(toExtensionSupportPlan(value, factory));
        }

        return list;
    }

    /**
     * @param value the value to convert
     * @return the {@link ExtensionSupportPlan} created from the passed value
     * @throws MalformedURLException when failing to parse the URL
     */
    public static ExtensionSupportPlan toExtensionSupportPlan(Object value) throws MalformedURLException
    {
        return toExtensionSupportPlan(value, null);
    }

    /**
     * @param value the value to convert
     * @param factory the factory used to de-deplicate instances
     * @return the {@link ExtensionSupportPlan} created from the passed value
     * @throws MalformedURLException when failing to parse the URL
     */
    public static ExtensionSupportPlan toExtensionSupportPlan(Object value, ExtensionFactory factory)
        throws MalformedURLException
    {
        if (value != null) {
            String valueString = value.toString();

            ExtensionConverterParser parser = new ExtensionConverterParser(valueString);

            String supporterName = parser.next(true);
            String supporterURL = parser.next(true);

            ExtensionSupporter suppoter = ExtensionFactory.getExtensionSupporter(factory, supporterName,
                StringUtils.isNoneEmpty(supporterURL) ? new URL(supporterURL) : null);

            String name = parser.next(true);
            String url = parser.next(true);
            boolean paying = BooleanUtils.toBoolean(parser.next(false));

            return ExtensionFactory.getExtensionSupportPlan(factory, suppoter, name, url != null ? new URL(url) : null,
                paying);
        }

        return null;
    }

    /**
     * @param value the {@link ExtensionSupportPlan} to serialize
     * @return the String version of an {@link ExtensionSupportPlan}
     */
    public static String toString(ExtensionSupportPlan value)
    {
        return ExtensionConverterParser.toString(value.getSupporter().getName(), value.getSupporter().getURL(),
            value.getName(), value.getURL(), value.isPaying());
    }

    /**
     * @param values the list of {@link ExtensionSupportPlan}s to serialize
     * @return the String version of an {@link ExtensionSupportPlan}s list
     */
    public static List<String> toStringList(Collection<ExtensionSupportPlan> values)
    {
        List<String> list = new ArrayList<>(values.size());

        for (ExtensionSupportPlan value : values) {
            list.add(toString(value));
        }

        return list;
    }

    @Override
    protected ExtensionSupportPlan convertToType(Type targetType, Object value)
    {
        try {
            return toExtensionSupportPlan(value, this.factory);
        } catch (MalformedURLException e) {
            throw new ConversionException("Failed to parse the URL of the extension supporter in [" + value + "]", e);
        }
    }

    @Override
    protected String convertToString(ExtensionSupportPlan value)
    {
        return toString(value);
    }
}
