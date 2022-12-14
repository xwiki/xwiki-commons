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
package org.xwiki.xml;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jdom2.input.DOMBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSParser;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xwiki.xml.internal.DefaultXMLReaderFactory;

/**
 * XML Utility methods.
 *
 * @version $Id$
 * @since 1.6M1
 */
public final class XMLUtils
{
    /**
     * A simple error logging helper to suppress expected error messages.
     * While extracting text excerpts via the {@link #extractXML} method
     * the extraction throws expected exceptions eg. in case a text node
     * is too long. The default ErrorListener writes these errors to the console.
     * However as these errors are expected under normal conditions
     * and are either handled in the {@link #extractXML} method
     * or passed up in the chain of commands it is ok to log these
     * at a much lower level, where users normally never see them.
     */
    private static class RelaxedErrorListener implements ErrorListener
    {
        private static final String STACK_TRACE_NOTE = "stack trace for information only";

        /**
         * Fatal errors are unexpected and are logged as warnings here.
         * They are not really fatal to XWiki but should be handled
         * up the command chain later, or maybe propagated to the UI level.
         *
         * @param exception the exception to be logged
         */
        @Override
        public void fatalError(TransformerException exception) throws TransformerException
        {
            LOGGER.warn("Fatal error from xml transformer: [{}]", ExceptionUtils.getRootCauseMessage(exception));
            LOGGER.trace(STACK_TRACE_NOTE, exception);
        }

        /**
         * Errors are expected and are only logged as debug.
         * These errors happen e.g. if a text node exceeds the expected
         * maximal length, which can happen in regular usage.
         *
         * @param exception the exception to be logged
         */
        @Override
        public void error(TransformerException exception) throws TransformerException
        {
            LOGGER.debug("Error [{}] from xml transformer", ExceptionUtils.getRootCauseMessage(exception));
            LOGGER.trace(STACK_TRACE_NOTE, exception);
        }

        /**
         * Warnings are logged at debug level.
         * They might be of concern for the developers but not the end users.
         *
         * @param exception the exception to be logged
         */
        @Override
        public void warning(TransformerException exception) throws TransformerException
        {
            LOGGER.debug("Warning [{}] from xml transformer", ExceptionUtils.getRootCauseMessage(exception));
            LOGGER.trace(STACK_TRACE_NOTE, exception);
        }
    }

    /** Logging helper object. */
    private static final Logger LOGGER = LoggerFactory.getLogger(XMLUtils.class);

    /** XML encoding of the "ampersand" character. */
    private static final String AMP = "&#38;";

    /** Regular expression recognizing XML-escaped "ampersand" characters. */
    private static final Pattern AMP_PATTERN = Pattern.compile("&(?:amp|#0*+38|#x0*+26);");

    /** XML encoding of the "single quote" character. */
    private static final String APOS = "&#39;";

    /** Regular expression recognizing XML-escaped "single quote" characters. */
    private static final Pattern APOS_PATTERN = Pattern.compile("&(?:apos|#0*+39|#x0*+27);");

    /** XML encoding of the "double quote" character. */
    private static final String QUOT = "&#34;";

    /** Regular expression recognizing XML-escaped "double quote" characters. */
    private static final Pattern QUOT_PATTERN = Pattern.compile("&(?:quot|#0*+34|#x0*+22);");

    /** XML encoding of the "left curly bracket". */
    private static final String LCURL = "&#123;";

    /** Regular expression recognizing XML-escaped "left curly bracket" characters. */
    private static final Pattern LCURL_PATTERN = Pattern.compile("&(?:#0*+123|#x0*+7[bB]);");

    /** XML encoding of the "less than" character. */
    private static final String LT = "&#60;";

    /** Regular expression recognizing XML-escaped "less than" characters. */
    private static final Pattern LT_PATTERN = Pattern.compile("&(?:lt|#0*+60|#x0*+3[cC]);");

    /** XML encoding of the "greater than" character. */
    private static final String GT = "&#62;";

    /** Regular expression recognizing XML-escaped "greater than" characters. */
    private static final Pattern GT_PATTERN = Pattern.compile("&(?:gt|#0*+62|#x0*+3[eE]);");

    private static final char[] ELEMENT_SYNTAX = new char[] {'<', '&'};

    /** Helper object for manipulating DOM Level 3 Load and Save APIs. */
    private static final DOMImplementationLS LS_IMPL;

    /** Xerces configuration parameter for disabling fetching and checking XMLs against their DTD. */
    private static final String DISABLE_DTD_PARAM = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    /** Xerces configuration parameter for prevent DOCTYPE definition. */
    private static final String DISABLE_EXTERNAL_DOCTYPE_DECLARATION =
        "http://apache.org/xml/features/disallow-doctype-decl";

    /** Xerces configuration parameter for disabling inserting entities defined in external files. */
    private static final String DISABLE_EXTERNAL_PARAMETER_ENTITIES =
        "http://xml.org/sax/features/external-parameter-entities";

    /** Xerces configuration parameter for disabling inserting entities defined in external files. */
    private static final String DISABLE_EXTERNAL_GENERAL_ENTITIES =
        "http://xml.org/sax/features/external-general-entities";

    /** Helper to log expected errors at an appropriate level. */
    private static final ErrorListener RELAXED_ERROR_LISTENER = new RelaxedErrorListener();

    private static final String NEWLINE = "\n";

    private static final DefaultXMLReaderFactory XML_READER_FACTORY = new DefaultXMLReaderFactory();

    static {
        DOMImplementationLS implementation = null;
        try {
            implementation =
                (DOMImplementationLS) DOMImplementationRegistry.newInstance().getDOMImplementation("LS 3.0");
        } catch (Exception ex) {
            LOGGER.warn("Cannot initialize the XML Script Service: [{}]", ex.getMessage());
        }
        LS_IMPL = implementation;

        XML_READER_FACTORY.initialize();
    }

    /**
     * Private constructor since this is a utility class that shouldn't be instantiated (all methods are static).
     */
    private XMLUtils()
    {
        // Nothing to do
    }

    /**
     * Extracts a well-formed XML fragment from the given DOM tree.
     *
     * @param node the root of the DOM tree where the extraction takes place
     * @param start the index of the first character
     * @param length the maximum number of characters in text nodes to include in the returned fragment
     * @return a well-formed XML fragment starting at the given character index and having up to the specified length,
     *         summing only the characters in text nodes
     * @since 1.6M2
     */
    public static String extractXML(Node node, int start, int length)
    {
        ExtractHandler handler = null;
        try {
            handler = new ExtractHandler(start, length);
            Transformer xformer = XMLUtils.createTransformerFactory().newTransformer();
            xformer.setErrorListener(RELAXED_ERROR_LISTENER);
            xformer.transform(new DOMSource(node), new SAXResult(handler));
            return handler.getResult();
        } catch (Throwable t) {
            if (handler != null && handler.isFinished()) {
                return handler.getResult();
            } else {
                throw new RuntimeException("Failed to extract XML", t);
            }
        }
    }

    /**
     * XML comment does not support some characters inside its content but there is no official escaping/unescaping for
     * it so we made our own.
     * <ul>
     *   <li>1) Escape existing \</li>
     *   <li>2) Escape --</li>
     *   <li>3) Escape > or - at the start of the comment</li>
     *   <li>4) Escape { to prevent XWiki macro syntax</li>
     *   <li>5) Add {@code \} (unescaped as {@code ""}) at the end if the last char is {@code -}</li>
     * </ul>
     *
     * @param content the XML comment content to escape
     * @return the escaped content.
     * @since 1.9M2
     */
    public static String escapeXMLComment(String content)
    {
        StringBuilder str = new StringBuilder(content.length());

        char[] buff = content.toCharArray();

        // At the start of a comment, > isn't allowed.
        if (buff.length > 0 && buff[0] == '>') {
            str.append('\\');
        }

        // Initialize with '-', as "->" isn't allowed at the start of the comment. It is thus better to start with
        // an escape when the comment starts with '-'.
        char lastChar = '-';
        for (char c : buff) {
            if (c == '\\' || c == '{' || (c == '-' && lastChar == '-')) {
                str.append('\\');
            }

            str.append(c);
            lastChar = c;
        }

        if (lastChar == '-') {
            str.append('\\');
        }

        return str.toString();
    }

    /**
     * XML comment does not support some characters inside its content but there is no official escaping/unescaping for
     * it so we made our own.
     *
     * @param content the XML comment content to unescape
     * @return the unescaped content.
     * @see #escapeXMLComment(String)
     * @since 1.9M2
     */
    public static String unescapeXMLComment(String content)
    {
        StringBuffer str = new StringBuffer(content.length());

        char[] buff = content.toCharArray();
        boolean escaped = false;
        for (char c : buff) {
            if (!escaped && c == '\\') {
                escaped = true;
                continue;
            }

            str.append(c);
            escaped = false;
        }

        return str.toString();
    }

    /**
     * Escapes all the XML special characters and a XWiki Syntax 2.0+ special character (i.e., <code>{</code>, to
     * protect against <code>{{/html}}</code>) in a {@code String}.
     * The escaping is done using numerical XML entities to allow the content to be used as an XML attribute value
     * or as an XML element text.
     * For instance, {@code <b>{{html}}$x{{/html}}</b>} will be escaped and can thus be put inside an XML attribute.
     * To illustrate, the value can be used in a div tag
     * <code>&lt;div&gt;&amp;#60;b&amp;#62;&amp;#123;&amp;#123;html}}$x&amp;#123;&amp;#123;/html}}&amp;#60;/b&amp;#62;
     * &lt;/div&gt;</code>
     * or in the attribute of an input tag
     * <code>&lt;input
     * value=&quot;&amp;#60;b&amp;#62;&amp;#123;&amp;#123;html}}$x&amp;#123;&amp;#123;/html}}&amp;#60;/b&amp;#62;&quot;
     * /&gt;</code>.
     * <p>
     * Specifically, escapes &lt;, &gt;, ", ', &amp; and {.
     * <p>
     * Note that is is preferable to use {@link #escapeAttributeValue(String)} when the content is used as
     * an XML tag attribute, and {@link #escapeElementText(String)} when the content is used as an XML text.
     *
     * @param content the text to escape, may be {@code null}. The content is converted to {@code String} using
     * {@link Objects#toString(Object, String)}, where the second parameter is {@code null}
     * @return a new escaped {@code String}, {@code null} if {@code null} input
     * @see #escapeAttributeValue(String)
     * @see #escapeElementText(String)
     * @deprecated since 12.8RC1, use {@link #escape(String)} instead
     */
    @Deprecated
    public static String escape(Object content)
    {
        return escape(Objects.toString(content, null));
    }

    /**
     * Escapes all the XML special characters and a XWiki Syntax 2.0+ special character (i.e., <code>{</code>, to
     * protect against {{/html}}) in a {@code String}.
     * The escaping is done using numerical XML entities to allow the content to be used as an XML attribute value
     * or as an XML element text.
     * For instance, {@code <b>{{html}}$x{{/html}}</b>} will be escaped and can thus be put inside as XML attribute.
     * To illustrate, the value can be used in a div tag
     * <code>&lt;div&gt;&amp;#60;b&amp;#62;&amp;#123;&amp;#123;html}}$x&amp;#123;&amp;#123;/html}}&amp;#60;/b&amp;#62;
     * &lt;/div&gt;</code>
     * or in the attribute of an input tag
     * <code>&lt;input
     * value=&quot;&amp;#60;b&amp;#62;&amp;#123;&amp;#123;html}}$x&amp;#123;&amp;#123;/html}}&amp;#60;/b&amp;#62;&quot;
     * /&gt;</code>.
     * <p>
     * Specifically, escapes &lt;, &gt;, ", ', &amp; and {.
     * <p>
     * Note that is is preferable to use {@link #escapeAttributeValue(String)} when the content is used as
     * an XML tag attribute, and {@link #escapeElementText(String)} when the content is used as an XML text.
     *
     * @param content the text to escape, may be {@code null}
     * @return a new escaped {@code String}, {@code null} if {@code null} input
     * @see #escapeAttributeValue(String)
     * @see #escapeElementText(String)
     * @since 12.8RC1
     * @since 12.6.3
     * @since 11.10.11
     */
    public static String escape(String content)
    {
        return escapeAttributeValue(content);
    }

    /**
     * Escapes all the XML special characters and a XWiki Syntax 2.0+ special character (i.e., <code>{</code>, to
     * protect against {{/html}}) in a {@code String}.
     * The escaping is done using numerical XML entities to allow the content to be used inside XML attributes.
     * For instance, {@code <b>{{html}}$x{{/html}}</b>} will be escaped and can thus be put inside an XML attribute.
     * To illustrate, the value can be used in the attribute of an input tag
     * <code>&lt;input
     * value=&quot;&amp;#60;b&amp;#62;&amp;#123;&amp;#123;html}}$x&amp;#123;&amp;#123;/html}}&amp;#60;/b&amp;#62;&quot;
     * /&gt;</code>.
     * <p>
     * Specifically, escapes &lt;, &gt;, ", ', &amp; and {.
     *
     * @param content the text to escape, may be {@code null}. The content is converted to {@code String} using
     * {@link String#valueOf(Object)} before escaping.
     * @return a new escaped {@code String}, {@code null} if {@code null} input
     * @deprecated since 12.8RC1, use {@link #escapeAttributeValue(String)} instead
     */
    @Deprecated
    public static String escapeAttributeValue(Object content)
    {
        if (content == null) {
            return null;
        }
        return escapeAttributeValue(String.valueOf(content));
    }

    /**
     * Escapes all the XML special characters and a XWiki Syntax 2.0+ special character (i.e., <code>{</code>, to
     * protect against {{/html}}) in a {@code String}.
     * The escaping is done using numerical XML entities to allow the content to be used inside XML attributes.
     * For instance, {@code <b>{{html}}$x{{/html}}</b>} will be escaped and can thus be put inside an XML attribute.
     * To illustrate, the value can be used in the attribute of an input tag
     * <code>&lt;input
     * value=&quot;&amp;#60;b&amp;#62;&amp;#123;&amp;#123;html}}$x&amp;#123;&amp;#123;/html}}&amp;#60;/b&amp;#62;&quot;
     * /&gt;</code>.
     * <p>
     * Specifically, escapes &lt;, &gt;, ", ', &amp; and {.
     *
     * @param content the text to escape, may be {@code null}
     * @return a new escaped {@code String}, {@code null} if {@code null} input
     * @since 12.8RC1
     * @since 12.6.3
     * @since 11.10.11
     */
    public static String escapeAttributeValue(String content)
    {
        if (content == null) {
            return null;
        }
        StringBuilder result = new StringBuilder((int) (content.length() * 1.1));
        int length = content.length();
        char c;
        for (int i = 0; i < length; ++i) {
            c = content.charAt(i);
            switch (c) {
                case '&':
                    result.append(AMP);
                    break;
                case '\'':
                    result.append(APOS);
                    break;
                case '"':
                    result.append(QUOT);
                    break;
                case '<':
                    result.append(LT);
                    break;
                case '>':
                    result.append(GT);
                    break;
                case '{':
                    // Not needed from XML point of view but escaping xwiki/2.x macro syntax helps avoid countless
                    // security problems easily
                    result.append(LCURL);
                    break;
                default:
                    result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * Escapes XML special characters in a {@code String} using numerical XML entities, so that the resulting string
     * can safely be used as an XML element text value.
     * For instance, {@code Jim & John} will be escaped and can thus be put inside an XML tag, such as the {@code p}
     * tag, as in {@code <p>Jim &amp; John</p>}.
     * Specifically, escapes &lt; to {@code &lt;}, and &amp; to {@code &amp;}.
     * <p>
     * Since 13.10.9, 14.4.4 and 14.7RC1 the character { is also escaped.
     *
     * @param content the text to escape, may be {@code null}.
     * @return a new escaped {@code String}, {@code null} if {@code null} input
     * @since 12.8RC1
     * @since 12.6.3
     * @since 11.10.11
     */
    public static String escapeElementText(String content)
    {
        if (content == null) {
            return null;
        }
        // Initializes a string builder with an initial capacity 1.1 times greater than the initial content to account
        // for special character substitutions.
        int contentLength = content.length();
        StringBuilder result = new StringBuilder((int) (contentLength * 1.1));
        for (int i = 0; i < contentLength; ++i) {
            char c = content.charAt(i);
            switch (c) {
                case '&':
                    result.append(AMP);
                    break;
                case '<':
                    result.append(LT);
                    break;
                case '{':
                    // Not needed from XML point of view but escaping xwiki/2.x macro syntax helps avoid countless
                    // security problems easily
                    result.append(LCURL);
                    break;
                default:
                    result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * Same logic as {@link #escapeElementText(String)} but only indicate if there is something to escape.
     * 
     * @param content the content to parse
     * @return true if the passed content contains content that can be interpreted as XML syntax
     * @see #escapeElementText(String)
     * @since 12.10
     * @since 12.6.5
     */
    public static boolean containsElementText(CharSequence content)
    {
        return StringUtils.containsAny(content, ELEMENT_SYNTAX);
    }

    /**
     * Escapes the XML special characters in a <code>String</code> using numerical XML entities, so that the resulting
     * string can safely be used as an XML text node. Specifically, escapes &lt;, &gt;, and &amp;.
     *
     * @param content the text to escape, may be {@code null}. The content is converted to {@code String} using
     * {@link String#valueOf(Object)} before escaping.
     * @return a new escaped {@code String}, {@code null} if {@code null} input
     * @deprecated since 12.8RC1, use {@link #escapeElementText(String)} instead.
     */
    @Deprecated
    public static String escapeElementContent(Object content)
    {
        if (content == null) {
            return null;
        }
        String str = String.valueOf(content);
        StringBuilder result = new StringBuilder((int) (str.length() * 1.1));
        int length = str.length();
        char c;
        for (int i = 0; i < length; ++i) {
            c = str.charAt(i);
            switch (c) {
                case '&':
                    result.append(AMP);
                    break;
                case '<':
                    result.append(LT);
                    break;
                case '>':
                    result.append(GT);
                    break;
                default:
                    result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * Unescape encoded special XML characters. Only &gt;, &lt; &amp;, ", ' and { are unescaped, since they are the only
     * ones that affect the resulting markup.
     *
     * @param content the text to decode, may be {@code null}. The content is converted to {@code String} using
     * {@link String#valueOf(Object)} before escaping
     * @return unescaped content, {@code null} if {@code null} input
     * @deprecated since 12.8RC1, use {@link org.apache.commons.text.StringEscapeUtils#unescapeXml(String)} instead
     */
    @Deprecated
    public static String unescape(Object content)
    {
        if (content == null) {
            return null;
        }
        String str = String.valueOf(content);

        str = APOS_PATTERN.matcher(str).replaceAll("'");
        str = QUOT_PATTERN.matcher(str).replaceAll("\"");
        str = LT_PATTERN.matcher(str).replaceAll("<");
        str = GT_PATTERN.matcher(str).replaceAll(">");
        str = AMP_PATTERN.matcher(str).replaceAll("&");
        str = LCURL_PATTERN.matcher(str).replaceAll("{");

        return str;
    }

    /**
     * Construct a new (empty) DOM Document and return it.
     *
     * @return an empty DOM Document
     */
    public static Document createDOMDocument()
    {
        try {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        } catch (ParserConfigurationException ex) {
            LOGGER.error("Cannot create DOM Documents", ex);
            return null;
        }
    }

    /**
     * Parse a DOM Document from a source.
     *
     * @param source the source input to parse
     * @return the equivalent DOM Document, or {@code null} if the parsing failed.
     */
    public static Document parse(LSInput source)
    {
        try {
            LSParser p = LS_IMPL.createLSParser(DOMImplementationLS.MODE_SYNCHRONOUS, null);
            // Disable validation, since this takes a lot of time and causes unneeded network traffic
            p.getDomConfig().setParameter("validate", false);
            if (p.getDomConfig().canSetParameter(DISABLE_DTD_PARAM, false)) {
                p.getDomConfig().setParameter(DISABLE_DTD_PARAM, false);
            }

            // Avoid XML eXternal Entity injection (XXE)
            if (p.getDomConfig().canSetParameter(DISABLE_EXTERNAL_DOCTYPE_DECLARATION, false)) {
                p.getDomConfig().setParameter(DISABLE_EXTERNAL_DOCTYPE_DECLARATION, false);
            }
            if (p.getDomConfig().canSetParameter(DISABLE_EXTERNAL_PARAMETER_ENTITIES, false)) {
                p.getDomConfig().setParameter(DISABLE_EXTERNAL_PARAMETER_ENTITIES, false);
            }
            if (p.getDomConfig().canSetParameter(DISABLE_EXTERNAL_GENERAL_ENTITIES, false)) {
                p.getDomConfig().setParameter(DISABLE_EXTERNAL_GENERAL_ENTITIES, false);
            }
            return p.parse(source);
        } catch (Exception ex) {
            LOGGER.warn("Cannot parse XML document: [{}]", ex.getMessage());
            return null;
        }
    }

    /**
     * Serialize a DOM Node into a string, including the XML declaration at the start.
     *
     * @param node the node to export
     * @return the serialized node, or an empty string if the serialization fails
     */
    public static String serialize(Node node)
    {
        return serialize(node, true);
    }

    /**
     * Serialize a DOM Node into a string, with an optional XML declaration at the start.
     *
     * @param node the node to export
     * @param withXmlDeclaration whether to output the XML declaration or not
     * @return the serialized node, or an empty string if the serialization fails or the node is {@code null}
     */
    public static String serialize(Node node, boolean withXmlDeclaration)
    {
        if (node == null) {
            return "";
        }
        try {
            LSOutput output = LS_IMPL.createLSOutput();
            StringWriter result = new StringWriter();
            output.setCharacterStream(result);
            LSSerializer serializer = LS_IMPL.createLSSerializer();
            serializer.getDomConfig().setParameter("xml-declaration", withXmlDeclaration);
            serializer.setNewLine(NEWLINE);
            String encoding = "UTF-8";
            if (node instanceof Document) {
                encoding = ((Document) node).getXmlEncoding();
            } else if (node.getOwnerDocument() != null) {
                encoding = node.getOwnerDocument().getXmlEncoding();
            }
            output.setEncoding(encoding);
            serializer.write(node, output);
            return result.toString();
        } catch (Exception ex) {
            LOGGER.warn("Failed to serialize node to XML String: [{}]", ex.getMessage());
            return "";
        }
    }

    /**
     * Apply an XSLT transformation to a Document.
     *
     * @param xml the document to transform
     * @param xslt the stylesheet to apply
     * @return the transformation result, or {@code null} if an error occurs or {@code null} xml or xslt input
     */
    public static String transform(Source xml, Source xslt)
    {
        if (xml != null && xslt != null) {
            try {
                StringWriter output = new StringWriter();
                Result result = new StreamResult(output);
                Source safeXMLSource = createSafeSource(xml);
                XMLUtils.createTransformerFactory().newTransformer(xslt).transform(safeXMLSource, result);
                return output.toString();
            } catch (Exception ex) {
                LOGGER.warn("Failed to apply XSLT transformation: [{}]", ex.getMessage());
            }
        }
        return null;
    }

    /**
     * Parse and pretty print a XML content.
     *
     * @param content the XML content to format
     * @return the formatted version of the passed XML content
     * @throws TransformerFactoryConfigurationError when failing to create a
     *             {@link TransformerFactoryConfigurationError}
     * @throws TransformerException when failing to transform the content
     * @since 5.2M1
     */
    public static String formatXMLContent(String content) throws TransformerFactoryConfigurationError,
        TransformerException
    {
        // TODO: Deprecate and legacify this method and introduce a new signature that doesn't throw any exception
        // Or better, also change XMLUtils.parse() to throw an exception and throw that exception here. Introduce an
        // XWiki exception to not be dependent of the XML parsing implementation used.
        if (content == null) {
            return null;
        }
        LSInput input = LS_IMPL.createLSInput();
        input.setCharacterStream(new StringReader(content));
        Format format = Format.getPrettyFormat();
        format.setLineSeparator(NEWLINE);
        Document document = XMLUtils.parse(input);
        if (document == null) {
            throw new TransformerException(String.format("Failed to pretty print XML content [%s]", content));
        }
        DOMBuilder builder = new DOMBuilder();
        return new XMLOutputter(format).outputString(builder.build(document));
    }

    private static Source createSafeSource(Source originalSource) throws ParserConfigurationException, SAXException
    {
        Source safeSource;
        if (originalSource instanceof StreamSource) {
            StreamSource stream = (StreamSource) originalSource;
            InputSource inputSource;
            if (stream.getReader() == null) {
                inputSource = new InputSource(stream.getInputStream());
            } else {
                inputSource = new InputSource(stream.getReader());
            }
            inputSource.setPublicId(stream.getPublicId());
            inputSource.setSystemId(originalSource.getSystemId());
            safeSource = new SAXSource(XML_READER_FACTORY.createXMLReader(), inputSource);
        } else if (originalSource instanceof SAXSource) {
            SAXSource originalSAXSource = (SAXSource) originalSource;
            safeSource = new SAXSource(XML_READER_FACTORY.createXMLReader(), originalSAXSource.getInputSource());
            safeSource.setSystemId(originalSAXSource.getSystemId());
        } else {
            // We don't handle this type of source by using our local resolver but it's still safe since we use the
            // FEATURE_SECURE_PROCESSING feature in createTransformerFactory().
            safeSource = originalSource;
        }
        return safeSource;
    }

    private static TransformerFactory createTransformerFactory() throws TransformerConfigurationException
    {
        TransformerFactory tf = TransformerFactory.newInstance();
        tf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        return tf;
    }
}
