package org.xwiki.javascript.importmap.internal.parser;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.xwiki.webjars.WebjarDescriptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Test of {@link JavascriptImportmapParser}.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
class JavascriptImportmapParserTest
{
    private final JavascriptImportmapParser parser = new JavascriptImportmapParser();

    @Test
    void parseMalformedJSON()
    {
        assertThrows(JavascriptImportmapException.class, () -> this.parser.parse("{\n"));
    }

    @Test
    void parseEmpty() throws Exception
    {
        var parsed = this.parser.parse("{}");
        assertEquals(Map.of(), parsed);
    }

    @Test
    void parseStringReference() throws Exception
    {
        var parsed = this.parser.parse("""
            {
                "moduleA": "org.xwiki.js/path.js",
                "moduleB": "org.xwiki.jsB/path2.js"
            }""");
        assertEquals(Map.of(
            "moduleA", new WebjarDescriptor("org.xwiki.js", "path.js"),
            "moduleB", new WebjarDescriptor("org.xwiki.jsB", "path2.js")
        ), parsed);
    }

    @Test
    void parseStringReferenceMalformed()
    {
        assertThrows(JavascriptImportmapException.class, () -> this.parser.parse("""
            {
                "moduleA": "org.xwiki.js"
            }"""));
    }

    @Test
    void parseWithParameters() throws Exception
    {
        var parsed = this.parser.parse("""
            {
              "moduleA": {
                "webjarId": "org.xwiki.js",
                "path": "path.js",
                "params": {
                  "key": "value"
                }
              }
            }""");
        assertEquals(Map.of(
            "moduleA", new WebjarDescriptor("org.xwiki.js", "path.js", Map.of("key", "value"))
        ), parsed);
    }

    @Test
    void parseWithMalformedParameters()
    {
        var exception = assertThrows(JavascriptImportmapException.class, () -> this.parser.parse("""
            {
              "moduleA": {
                "path": "path.js",
                "params": {
                  "key": "value"
                }
              }
            }"""));
        assertEquals("Malformed value for key [moduleA]", exception.getMessage());
    }

    @Test
    void parseWithNamespace() throws Exception
    {
        var parsed = this.parser.parse("""
            {
              "moduleA": {
                "webjarId": "org.xwiki.js",
                "path": "path.js",
                "namespace": "s1"
              }
            }""");
        assertEquals(Map.of(
            "moduleA", new WebjarDescriptor("org.xwiki.js", "s1","path.js")
        ), parsed);
    }

    @Test
    void parseWithNamespaceAndParameters() throws Exception
    {
        var parsed = this.parser.parse("""
            {
              "moduleA": {
                "webjarId": "org.xwiki.js",
                "path": "path.js",
                "namespace": "s1",
                "params": {
                  "key": "value"
                }
              }
            }""");
        assertEquals(Map.of(
            "moduleA", new WebjarDescriptor("org.xwiki.js", "s1", "path.js", Map.of("key", "value"))
        ), parsed);
    }
}