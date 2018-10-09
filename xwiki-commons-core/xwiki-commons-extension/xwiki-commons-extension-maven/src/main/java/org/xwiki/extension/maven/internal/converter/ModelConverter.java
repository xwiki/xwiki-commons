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
package org.xwiki.extension.maven.internal.converter;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import javax.inject.Singleton;

import org.apache.maven.model.Model;
import org.xwiki.component.annotation.Component;
import org.xwiki.component.util.DefaultParameterizedType;
import org.xwiki.extension.Extension;
import org.xwiki.extension.maven.internal.MavenExtension;
import org.xwiki.extension.maven.internal.MavenUtils;
import org.xwiki.properties.converter.ConversionException;
import org.xwiki.properties.converter.Converter;

/**
 * Create an {@link Extension} from a Maven {@link Model}.
 * 
 * @version $Id$
 * @since 9.0RC1
 */
@Component
@Singleton
public class ModelConverter extends AbstractModelConverter<Model>
{
    /**
     * The role of the component.
     */
    public static final ParameterizedType ROLE = new DefaultParameterizedType(null, Converter.class, Model.class);

    @Override
    public <G> G convert(Type targetType, Object sourceValue)
    {
        if (targetType == Extension.class) {
            return (G) convertToExtension((Model) sourceValue);
        } else {
            throw new ConversionException(String.format("Unsupported target type [%s]", targetType));
        }
    }

    private MavenExtension convertToExtension(Model model)
    {
        String groupId = MavenUtils.resolveGroupId(model);
        String artifactId = model.getArtifactId();
        String type = MavenUtils.packagingToType(model.getPackaging());
        String version = MavenUtils.resolveVersion(model);

        return convertToExtension(model, groupId, artifactId, null, type, version);
    }
}
