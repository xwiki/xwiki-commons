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
package org.xwiki.tool.checkstyle;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.api.Configuration;

/**
 * Verify the file encoding rules defined in {@code checkstyle.xml} and {@code checkstyle-test.xml}.
 *
 * @version $Id$
 */
class FileEncodingRulesTest extends AbstractModuleTestSupport
{
    private static final String INVISIBLE_CHARACTER_MESSAGE =
        ": Invisible or ambiguous character: use a unicode escape instead.";

    private static final String PROPERTIES_ENCODING_MESSAGE =
        ": Properties files must be encoded in ISO-8859-1, not UTF-8.";

    private static final String ID = "id";

    @TempDir
    private Path tmpDir;

    @Test
    void invisibleCharactersInMainSources() throws Exception
    {
        verifyInvisibleCharacters("checkstyle.xml");
    }

    @Test
    void invisibleCharactersInTestSources() throws Exception
    {
        verifyInvisibleCharacters("checkstyle-test.xml");
    }

    @Test
    void iso88591PropertiesFile() throws Exception
    {
        Path file = this.tmpDir.resolve("ApplicationResources_fr.properties");
        Files.writeString(file, "key1=café à l'été :\nkey2=\\u0436\\u00E9\n", StandardCharsets.ISO_8859_1);

        verify(createConfiguration("checkstyle.xml", "propertiesEncoding"), file.toString());
    }

    @Test
    void utf8PropertiesFile() throws Exception
    {
        Path file = this.tmpDir.resolve("ApplicationResources_fr.properties");
        Files.writeString(file, "key1=ascii\nkey2=café\nkey3= :\n", StandardCharsets.UTF_8);

        verify(createConfiguration("checkstyle.xml", "propertiesEncoding"), file.toString(),
            "2" + PROPERTIES_ENCODING_MESSAGE, "3" + PROPERTIES_ENCODING_MESSAGE);
    }

    @Override
    public String getPackageLocation()
    {
        return "org/xwiki/tool/checkstyle/test/encoding";
    }

    private void verifyInvisibleCharacters(String configurationFile) throws Exception
    {
        verify(createConfiguration(configurationFile, "invisibleCharacter"),
            getPath("TestClassWithInvisibleCharacters.java"), "28" + INVISIBLE_CHARACTER_MESSAGE,
            "30" + INVISIBLE_CHARACTER_MESSAGE, "32" + INVISIBLE_CHARACTER_MESSAGE, "33" + INVISIBLE_CHARACTER_MESSAGE);
    }

    /**
     * @return a configuration holding only the module with the passed id, extracted from the passed Checkstyle
     *     configuration file, so that the test verifies the rule as it is actually shipped
     */
    private Configuration createConfiguration(String configurationFile, String moduleId) throws Exception
    {
        Configuration configuration = ConfigurationLoader.loadConfiguration(
            getClass().getClassLoader().getResource(configurationFile).toString(),
            new PropertiesExpander(new Properties()));
        for (Configuration child : configuration.getChildren()) {
            if (ArrayUtils.contains(child.getPropertyNames(), ID) && moduleId.equals(child.getProperty(ID))) {
                DefaultConfiguration rootConfiguration = new DefaultConfiguration("Checker");
                // Read the files as UTF-8, like the build does (project.build.sourceEncoding).
                rootConfiguration.addProperty("charset", StandardCharsets.UTF_8.name());
                rootConfiguration.addChild(child);
                return rootConfiguration;
            }
        }
        throw new IllegalStateException(
            String.format("No module with id [%s] in [%s]", moduleId, configurationFile));
    }
}
