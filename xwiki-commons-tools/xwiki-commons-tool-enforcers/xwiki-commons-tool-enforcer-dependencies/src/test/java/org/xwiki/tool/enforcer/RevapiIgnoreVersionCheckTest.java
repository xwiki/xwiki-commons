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
package org.xwiki.tool.enforcer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.maven.enforcer.rule.api.EnforcerRuleException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link RevapiIgnoreVersionCheck}.
 *
 * @version $Id$
 */
class RevapiIgnoreVersionCheckTest
{
    private static final String REMOVED_METHOD = """
        <item>
          <ignore>true</ignore>
          <code>java.method.removed</code>
          <old>method void org.xwiki.Foo::bar()</old>
        </item>
        """;

    @TempDir
    private Path directory;

    private RevapiIgnoreVersionCheck rule;

    @BeforeEach
    void setUp()
    {
        this.rule = new RevapiIgnoreVersionCheck();
        this.rule.setPreviousVersion("18.9.0");
    }

    @Test
    void withoutRevapiDifferences() throws Exception
    {
        this.rule.setPreviousVersion(null);
        setPom("<build><plugins><plugin><artifactId>maven-jar-plugin</artifactId></plugin></plugins></build>");

        assertDoesNotThrow(this.rule::execute);
    }

    @Test
    void withVersionOnTheGroup() throws Exception
    {
        setRevapiPom(group("<attachments><versions>18.10.0-rc-1</versions></attachments>", REMOVED_METHOD));

        assertDoesNotThrow(this.rule::execute);
    }

    @Test
    void withVersionOnTheItem() throws Exception
    {
        setRevapiPom(group("", item("<attachments><versions>18.10.0</versions></attachments>")));

        assertDoesNotThrow(this.rule::execute);
    }

    @Test
    void withBackportVersions() throws Exception
    {
        setRevapiPom(group("<attachments><versions>18.10.0-rc-1, 18.4.7,17.10.14</versions></attachments>",
            REMOVED_METHOD));

        assertDoesNotThrow(this.rule::execute);
    }

    @Test
    void withoutVersion() throws Exception
    {
        setRevapiPom(group("<justification>Unstable API</justification>", REMOVED_METHOD));

        EnforcerRuleException exception = assertThrows(EnforcerRuleException.class, this.rule::execute);

        assertTrue(exception.getMessage().contains(
            "- java.method.removed old [method void org.xwiki.Foo::bar()]: no [versions] attachment"));
        // The message tells what to add, with the version of the current branch.
        assertTrue(exception.getMessage().contains("<versions>18.10.0</versions>"));
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        // Only versions already released: the ignore is stale.
        "18.9.0,18.4.7   | none of the versions [18.9.0,18.4.7] is still to be released",
        // A version after the one of the current branch.
        "18.11.0         | none of the versions [18.11.0] is still to be released",
        "18.10.0-SNAPSHOT | [18.10.0-SNAPSHOT] is not a release version"
    })
    void withRejectedVersions(String versions, String expectedError) throws Exception
    {
        setRevapiPom(group("<attachments><versions>" + versions + "</versions></attachments>", REMOVED_METHOD));

        EnforcerRuleException exception = assertThrows(EnforcerRuleException.class, this.rule::execute);

        assertTrue(exception.getMessage().contains(expectedError), exception.getMessage());
    }

    @Test
    void itemVersionTakesPrecedenceOverTheGroupOne() throws Exception
    {
        setRevapiPom(group("<attachments><versions>18.10.0</versions></attachments>",
            item("<attachments><versions>18.4.7</versions></attachments>")));

        EnforcerRuleException exception = assertThrows(EnforcerRuleException.class, this.rule::execute);

        assertTrue(exception.getMessage().contains("none of the versions [18.4.7] is still to be released"));
    }

    @Test
    void reportsEveryInvalidDifference() throws Exception
    {
        setRevapiPom(group("<attachments><versions>18.10.0</versions></attachments>", REMOVED_METHOD)
            + group("", """
                <item>
                  <code>java.class.removed</code>
                  <old>class org.xwiki.Foo</old>
                </item>
                <item>
                  <code>java.method.addedToInterface</code>
                  <new>method void org.xwiki.Bar::baz()</new>
                </item>
                """));

        EnforcerRuleException exception = assertThrows(EnforcerRuleException.class, this.rule::execute);

        assertFalse(exception.getMessage().contains("org.xwiki.Foo::bar()"));
        assertTrue(exception.getMessage().contains("- java.class.removed old [class org.xwiki.Foo]"));
        assertTrue(
            exception.getMessage().contains("- java.method.addedToInterface new [method void org.xwiki.Bar::baz()]"));
    }

    @Test
    void withCustomAttachmentKey() throws Exception
    {
        this.rule.setAttachmentKey("since");
        setRevapiPom(group("<attachments><since>18.10.0</since></attachments>", REMOVED_METHOD));

        assertDoesNotThrow(this.rule::execute);
    }

    @Test
    void checksPluginManagementAndProfiles() throws Exception
    {
        String configuration = configuration(group("", REMOVED_METHOD));
        setPom("<build><pluginManagement><plugins>" + configuration + "</plugins></pluginManagement></build>"
            + "<profiles><profile><id>test</id><build><plugins>" + configuration
            + "</plugins></build></profile></profiles>");

        EnforcerRuleException exception = assertThrows(EnforcerRuleException.class, this.rule::execute);

        assertEquals(2, exception.getMessage().split("no \\[versions\\] attachment").length - 1);
    }

    @Test
    void withoutPreviousVersion() throws Exception
    {
        this.rule.setPreviousVersion(null);
        setRevapiPom(group("<attachments><versions>18.10.0</versions></attachments>", REMOVED_METHOD));

        EnforcerRuleException exception = assertThrows(EnforcerRuleException.class, this.rule::execute);

        assertEquals("The [previousVersion] parameter of the rule is mandatory but got [null].",
            exception.getMessage());
    }

    @Test
    void withUndefinedPreviousVersionProperty() throws Exception
    {
        this.rule.setPreviousVersion("${xwiki.compatibility.previous.version}");
        setRevapiPom(group("<attachments><versions>18.10.0</versions></attachments>", REMOVED_METHOD));

        EnforcerRuleException exception = assertThrows(EnforcerRuleException.class, this.rule::execute);

        assertEquals("The [previousVersion] parameter of the rule is mandatory but got "
            + "[${xwiki.compatibility.previous.version}].", exception.getMessage());
    }

    private static String group(String groupProperties, String items)
    {
        return "<revapi.differences>" + groupProperties + "<differences>" + items
            + "</differences></revapi.differences>";
    }

    private static String item(String itemProperties)
    {
        return REMOVED_METHOD.replace("</item>", itemProperties + "</item>");
    }

    private static String configuration(String groups)
    {
        return "<plugin><groupId>org.revapi</groupId><artifactId>revapi-maven-plugin</artifactId>"
            + "<configuration><analysisConfiguration>" + groups + "</analysisConfiguration></configuration></plugin>";
    }

    private void setRevapiPom(String groups) throws IOException
    {
        setPom("<build><plugins>" + configuration(groups) + "</plugins></build>");
    }

    private void setPom(String content) throws IOException
    {
        Path pom = this.directory.resolve("pom.xml");
        Files.writeString(pom, "<project><modelVersion>4.0.0</modelVersion><groupId>org.xwiki.commons</groupId>"
            + "<artifactId>test</artifactId><version>18.10.0-SNAPSHOT</version>" + content + "</project>");

        MavenProject project = new MavenProject();
        project.setVersion("18.10.0-SNAPSHOT");
        project.setFile(pom.toFile());
        this.rule.project = project;
    }
}
