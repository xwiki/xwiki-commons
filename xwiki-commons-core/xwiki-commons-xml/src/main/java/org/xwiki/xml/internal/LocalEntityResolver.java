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
/*
 This class was copied from https://github.com/css4j/xml-dtd
 See https://github.com/css4j/xml-dtd/issues/7 for the reason why we copied it instead of having a dependency on it.

 Copyright (c) 1998-2022, Carlos Amengual.
 Originally Licensed under a BSD-style License but relicensed under LGPL for XWiki by Carlos Amengual.
 You can find the original license here:
 https://css4j.github.io/LICENSE.txt
 */
package org.xwiki.xml.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map.Entry;

import org.w3c.dom.DocumentType;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.EntityResolver2;
import org.xml.sax.ext.LexicalHandler;

/**
 * Implements EntityResolver2.
 * <p>
 * Has common W3C DTDs/entities built-in and loads others via the supplied
 * <code>SYSTEM</code> URL, provided that certain conditions are met:
 * </p>
 * <ul>
 * <li>URL protocol is <code>http</code>/<code>https</code>.</li>
 * <li>Either the mime type is valid for a DTD or entity, or the filename ends
 * with <code>.dtd</code>, <code>.ent</code> or <code>.mod</code>.</li>
 * <li>The whitelist is either disabled (no host added to it) or contains the
 * host from the URL.</li>
 * </ul>
 * <p>
 * If the whitelist was enabled (e.g. default constructor), any attempt to
 * download data from a remote URL not present in the whitelist is going to
 * produce an exception. You can use that to determine whether your documents
 * are referencing a DTD resource that is not bundled with this resolver.
 * </p>
 * <p>
 * If the constructor with a <code>false</code> argument was used, the whitelist
 * can still be enabled by adding a hostname via
 * {@link #addHostToWhiteList(String)}.
 * </p>
 * <p>
 * Although this resolver should protect you from most information leaks (see
 * <a href="https://owasp.org/www-community/attacks/Server_Side_Request_Forgery">SSRF
 * attacks</a>) and also from <code>jar:</code>
 * <a href="https://en.wikipedia.org/wiki/Zip_bomb">decompression bombs</a>, DoS
 * attacks based on entity expansion/recursion like the
 * <a href="https://en.wikipedia.org/wiki/Billion_laughs_attack">'billion laughs
 * attack'</a> may still be possible and should be prevented at the XML parser.
 * Be sure to use a properly configured, recent version of your parser.
 * </p>
 *
 * @author Carlos Amengual
 * @version $Id$
 */
public class LocalEntityResolver implements EntityResolver2
{
    private static final DTDLoader dtdLoader = createDTDLoader();

    private static final String XHTML1_TRA_PUBLICID = "-//W3C//DTD XHTML 1.0 Transitional//EN";

    private static final String XHTML1_TRA_SYSTEMID = "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd";

    // The map is sized to have room for one additional mapping
    // via registerSystemIdFilename
    private final HashMap<String, String> systemIdToFilename = new HashMap<String, String>(69, 0.4f);

    private final HashMap<String, String> systemIdToPublicId = new HashMap<String, String>(14);

    private ClassLoader loader = null;

    private HashSet<String> whitelist = null;

    /**
     * Construct a resolver with the whitelist enabled.
     */
    public LocalEntityResolver()
    {
        this(true);
    }

    /**
     * Construct a resolver with the whitelist enabled or disabled according to
     * <code>enableWhitelist</code>.
     *
     * @param enableWhitelist can be <code>false</code> to allow connecting to any
     *                        host to retrieve DTDs or entities, or
     *                        <code>true</code> to enable the (empty) whitelist so
     *                        no network connections are to be allowed until a host
     *                        is added to it.
     */
    public LocalEntityResolver(boolean enableWhitelist)
    {
        super();

        systemIdToFilename.put("https://www.w3.org/TR/html5/entities.dtd",
            "/xhtml5.ent");
        systemIdToFilename.put("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd",
            "/xhtml1-strict.dtd");
        systemIdToFilename.put("http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd",
            "/xhtml1-transitional.dtd");
        systemIdToFilename.put("http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd",
            "/xhtml11.dtd");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml11.dtd",
            "/xhtml11.dtd");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-lat1.ent",
            "/xhtml5.ent");
        systemIdToFilename.put("http://www.w3.org/TR/xhtml11/DTD/xhtml-lat1.ent",
            "/xhtml5.ent");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-symbol.ent",
            "/xhtml-symbol.ent");
        systemIdToFilename.put("http://www.w3.org/TR/xhtml11/DTD/xhtml-symbol.ent",
            "/xhtml-symbol.ent");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-special.ent",
            "/xhtml-special.ent");
        systemIdToFilename.put("http://www.w3.org/TR/xhtml11/DTD/xhtml-special.ent",
            "/xhtml-special.ent");

        // XHTML 1.1 modules
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-inlstyle-1.mod",
            "/xhtml-inlstyle-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml11-model-1.mod",
            "/xhtml11-model-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-datatypes-1.mod",
            "/xhtml-datatypes-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-framework-1.mod",
            "/xhtml-framework-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-text-1.mod",
            "/xhtml-text-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-hypertext-1.mod",
            "/xhtml-hypertext-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-list-1.mod",
            "/xhtml-list-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-edit-1.mod",
            "/xhtml-edit-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-bdo-1.mod",
            "/xhtml-bdo-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-ruby-1.mod",
            "/xhtml-ruby-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-pres-1.mod",
            "/xhtml-pres-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-link-1.mod",
            "/xhtml-link-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-meta-1.mod",
            "/xhtml-meta-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-base-1.mod",
            "/xhtml-base-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-script-1.mod",
            "/xhtml-script-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-style-1.mod",
            "/xhtml-style-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-image-1.mod",
            "/xhtml-image-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-csismap-1.mod",
            "/xhtml-csismap-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-ssismap-1.mod",
            "/xhtml-ssismap-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-param-1.mod",
            "/xhtml-param-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-object-1.mod",
            "/xhtml-object-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-table-1.mod",
            "/xhtml-table-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-form-1.mod",
            "/xhtml-form-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-legacy-1.mod",
            "/xhtml-legacy-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-struct-1.mod",
            "/xhtml-struct-1.mod");

        // Other common DTDs
        systemIdToFilename.put("http://www.w3.org/TR/xhtml1/DTD/xhtml1-frameset.dtd",
            "/xhtml1-frameset.dtd");
        systemIdToFilename.put("http://www.w3.org/TR/xhtml-basic/xhtml-basic11.dtd",
            "/xhtml-basic11.dtd");
        systemIdToFilename.put("http://www.w3.org/TR/html4/strict.dtd",
            "/html4-strict.dtd");
        systemIdToFilename.put("http://www.w3.org/TR/html4/loose.dtd",
            "/html4-loose.dtd");
        systemIdToFilename.put("http://www.w3.org/TR/html4/frameset.dtd",
            "/html4-frameset.dtd");
        systemIdToFilename.put("http://www.w3.org/Math/DTD/mathml2/mathml2.dtd",
            "/mathml2.dtd");
        systemIdToFilename.put("http://www.w3.org/Math/DTD/mathml1/mathml.dtd",
            "/mathml.dtd");
        systemIdToFilename.put("http://www.w3.org/2002/04/xhtml-math-svg/xhtml-math-svg.dtd",
            "/xhtml-math-svg.dtd");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-inlstruct-1.mod",
            "/xhtml-inlstruct-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-inlphras-1.mod",
            "/xhtml-inlphras-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-blkstruct-1.mod",
            "/xhtml-blkstruct-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-blkphras-1.mod",
            "/xhtml-blkphras-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-applet-1.mod",
            "/xhtml-applet-1.dtd");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-blkpres-1.mod",
            "/xhtml-blkpres-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-basic-form-1.mod",
            "/xhtml-basic-form-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-basic-table-1.mod",
            "/xhtml-basic-table-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-frames-1.mod",
            "/xhtml-frames-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-target-1.mod",
            "/xhtml-target-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-iframe-1.mod",
            "/xhtml-iframe-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-events-1.mod",
            "/xhtml-events-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-nameident-1.mod",
            "/xhtml-nameident-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-legacy-redecl-1.mod",
            "/xhtml-legacy-redecl-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-inlpres-1.mod",
            "/xhtml-inlpres-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-arch-1.mod",
            "/xhtml-arch-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-notations-1.mod",
            "/xhtml-notations-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-qname-1.mod",
            "/xhtml-qname-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-attribs-1.mod",
            "/xhtml-attribs-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-charent-1.mod",
            "/xhtml-charent-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-basic11-model-1.mod",
            "/xhtml-basic11-model-1.mod");
        systemIdToFilename.put("http://www.w3.org/MarkUp/DTD/xhtml-inputmode-1.mod",
            "/xhtml-inputmode-1.mod");
        systemIdToFilename.put("http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd",
            "/svg11.dtd");
        systemIdToFilename.put("http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd",
            "/svg10.dtd");
        //
        systemIdToPublicId.put("http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd",
            "-//W3C//DTD XHTML 1.0 Strict//EN");
        systemIdToPublicId.put("http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd",
            "-//W3C//DTD XHTML 1.0 Transitional//EN");
        systemIdToPublicId.put("http://www.w3.org/MarkUp/DTD/xhtml11.dtd",
            "-//W3C//DTD XHTML 1.1//EN");
        systemIdToPublicId.put("http://www.w3.org/TR/xhtml11/DTD/xhtml-lat1.ent",
            "-//W3C//ENTITIES Latin 1 for XHTML//EN");
        systemIdToPublicId.put("http://www.w3.org/TR/xhtml11/DTD/xhtml-symbol.ent",
            "-//W3C//ENTITIES Symbols for XHTML//EN");
        systemIdToPublicId.put("http://www.w3.org/TR/xhtml11/DTD/xhtml-special.ent",
            "-//W3C//ENTITIES Special for XHTML//EN");
        systemIdToPublicId.put("http://www.w3.org/TR/html4/strict.dtd",
            "-//W3C//DTD HTML 4.01//EN");
        systemIdToPublicId.put("http://www.w3.org/TR/html4/loose.dtd",
            "-//W3C//DTD HTML 4.01 Transitional//EN");
        systemIdToPublicId.put("http://www.w3.org/TR/html4/frameset.dtd",
            "-//W3C//DTD HTML 4.01 Frameset//EN");
        systemIdToPublicId.put("http://www.w3.org/Math/DTD/mathml2/mathml2.dtd",
            "-//W3C//DTD MathML 2.0//EN");
        systemIdToPublicId.put("http://www.w3.org/Math/DTD/mathml1/mathml.dtd", "math");
        systemIdToPublicId.put("http://www.w3.org/2002/04/xhtml-math-svg/xhtml-math-svg.dtd",
            "-//W3C//DTD XHTML 1.1 plus MathML 2.0 plus SVG 1.1//EN");
        systemIdToPublicId.put("http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd",
            "-//W3C//DTD SVG 1.1//EN");
        systemIdToPublicId.put("http://www.w3.org/TR/2001/REC-SVG-20010904/DTD/svg10.dtd",
            "-//W3C//DTD SVG 1.0//EN");

        if (enableWhitelist) {
            whitelist = new HashSet<>(1);
        }
    }

    /**
     * Add the given host to a whitelist for remote DTD fetching.
     * <p>
     * If the whitelist is enabled, only http or https URLs will be allowed.
     * </p>
     *
     * @param fqdn
     *            the fully qualified domain name to add to the whitelist.
     */
    public void addHostToWhiteList(String fqdn)
    {
        if (fqdn != null) {
            if (whitelist == null) {
                whitelist = new HashSet<String>(4);
            }
            whitelist.add(fqdn.toLowerCase(Locale.ROOT));
        }
    }

    /**
     * Allows applications to provide an external subset for documents that don't
     * explicitly define one.
     * <p>
     * Documents with {@code DOCTYPE} declarations that omit an external subset can
     * thus augment the declarations available for validation, entity processing,
     * and attribute processing (normalization, defaulting, and reporting types
     * including {@code ID}). This augmentation is reported through the
     * {@link LexicalHandler#startDTD startDTD()} method as if the document text had
     * originally included the external subset; this callback is made before any
     * internal subset data or errors are reported.
     * </p>
     * <p>
     * This method can also be used with documents that have no {@code DOCTYPE}
     * declaration. When the root element is encountered but no {@code DOCTYPE}
     * declaration has been seen, this method is invoked. If it returns a value for
     * the external subset, that root element is declared to be the root element,
     * giving the effect of splicing a {@code DOCTYPE} declaration at the end the
     * prolog of a document that could not otherwise be valid. The sequence of
     * parser callbacks in that case logically resembles this:
     * </p>
     *
     * <pre>
     * ... comments and PIs from the prolog (as usual)
     * startDTD ("rootName", source.getPublicId (), source.getSystemId ());
     * startEntity ("[dtd]");
     * ... declarations, comments, and PIs from the external subset
     * endEntity ("[dtd]");
     * endDTD ();
     * ... then the rest of the document (as usual)
     * startElement (..., "rootName", ...);
     * </pre>
     *
     * <p>
     * Note that the {@code InputSource} gets no further resolution. Also, this
     * method will never be used by a (non-validating) processor that is not
     * including external parameter entities.
     * </p>
     * <p>
     * Uses for this method include facilitating data validation when interoperating
     * with XML processors that would always require undesirable network accesses
     * for external entities, or which for other reasons adopt a "no DTDs" policy.
     * </p>
     * <p>
     * <strong>Warning:</strong> returning an external subset modifies the input
     * document. By providing definitions for general entities, it can make a
     * malformed document appear to be well formed.
     * </p>
     *
     * @param name    Identifies the document root element. This name comes from a
     *                {@code DOCTYPE} declaration (where available) or from the
     *                actual root element.
     * @param baseURI The document's base URI, serving as an additional hint for
     *                selecting the external subset. This is always an absolute URI,
     *                unless it is {@code null} because the {@code XMLReader} was
     *                given an {@code InputSource} without one.
     *
     * @return an {@code InputSource} object describing the new external subset to
     *         be used by the parser. If no specific subset could be determined, an
     *         input source describing the HTML5 entities is returned.
     *
     * @throws SAXException        if either the provided arguments or the input
     *                             source were invalid or not allowed.
     * @throws java.io.IOException if an I/O problem was found while loading the
     *                             input source.
     */
    @Override
    public InputSource getExternalSubset(String name, String baseURI) throws SAXException, IOException
    {
        InputSource is = findExternalSubset(name, baseURI);
        if (is == null) {
            // Give the HTML5 entities as a fallback
            String fname = systemIdToFilename.get("https://www.w3.org/TR/html5/entities.dtd");
            Reader re = dtdLoader.loadDTDfromClasspath(loader, fname);
            if (re != null) {
                is = new InputSource(re);
            } else {
                throw new IOException("Could not find resource: " + fname);
            }
        }
        return is;
    }

    private InputSource findExternalSubset(String name, String baseURI) throws SAXException, IOException
    {
        InputSource is;
        if ("html".equalsIgnoreCase(name)) {
            is = resolveEntity("[dtd]", XHTML1_TRA_PUBLICID, baseURI, XHTML1_TRA_SYSTEMID);
            is.setPublicId(null);
            is.setSystemId(null);
        } else {
            is = null;
        }
        return is;
    }

    /**
     * Register an internal classpath filename to retrieve a DTD {@code SystemId}.
     *
     * @param systemId the {@code SystemId}.
     * @param filename the internal filename. Must point to a resource with
     *                 {@code UTF-8} encoding.
     * @return {@code true} if the new {@code SystemId} was successfully registered,
     *         {@code false} if it was already registered.
     * @throws IllegalArgumentException if the {@code filename} is considered
     *                                  invalid by {@link #isInvalidPath(String)}.
     */
    protected boolean registerSystemIdFilename(String systemId, String filename)
    {
        if (filename == null || systemId == null) {
            throw new NullPointerException("Null SystemId or filename.");
        }
        if (isInvalidPath(filename)) {
            throw new IllegalArgumentException("Bad DTD filename.");
        }
        String ret;
        synchronized (systemIdToFilename) {
            ret = systemIdToFilename.putIfAbsent(systemId, filename);
        }
        return ret == null;
    }

    /**
     * Allows applications to map references to external entities into input
     * sources.
     * <p>
     * This method is only called for external entities which have been properly
     * declared. It provides more flexibility than the
     * {@link org.xml.sax.EntityResolver EntityResolver} interface, supporting
     * implementations of more complex catalogue schemes such as the one defined by
     * the
     * <a href= "http://www.oasis-open.org/committees/entity/spec-2001-08-06.html"
     * >OASIS XML Catalogs</a> specification.
     * </p>
     * <p>
     * Parsers configured to use this resolver method will call it to determine the
     * input source to use for any external entity being included because of a
     * reference in the XML text. That excludes the document entity, and any
     * external entity returned by {@link #getExternalSubset getExternalSubset()}.
     * When a (non-validating) processor is configured not to include a class of
     * entities (parameter or general) through use of feature flags, this method is
     * not invoked for such entities.
     * </p>
     * <p>
     * If no valid input source could be determined, this method will throw a
     * {@code SAXException} instead of returning {@code null} as other
     * implementations would do. If you have to retrieve a DTD which is not directly
     * provided by this resolver, you need to whitelist the host using
     * {@link #addHostToWhiteList(String)} first. Make sure that either the systemId
     * URL ends with a valid extension, or that the retrieved URL was served with a
     * valid DTD media type.
     * </p>
     * <p>
     * Note that the entity naming scheme used here is the same one used in the
     * {@link org.xml.sax.ext.LexicalHandler LexicalHandler}, or in the
     * {@link org.xml.sax.ContentHandler#skippedEntity
     * ContentHandler.skippedEntity()} method.
     * </p>
     *
     * @param name     Identifies the external entity being resolved. Either
     *                 "{@code [dtd]}" for the external subset, or a name starting
     *                 with "{@code %}" to indicate a parameter entity, or else the
     *                 name of a general entity. This is never {@code null} when
     *                 invoked by a SAX2 parser.
     * @param publicId The public identifier of the external entity being referenced
     *                 (normalized as required by the XML specification), or
     *                 {@code null} if none was supplied.
     * @param baseURI  The URI with respect to which relative systemIDs are
     *                 interpreted. This is always an absolute URI, unless it is
     *                 {@code null} (likely because the {@code XMLReader} was given
     *                 an {@code InputSource} without one). This URI is defined by
     *                 the XML specification to be the one associated with the
     *                 "{@literal <}" starting the relevant declaration.
     * @param systemId The system identifier of the external entity being
     *                 referenced; either a relative or absolute URI.
     *
     * @return an {@code InputSource} object describing the new input source to be
     *         used by the parser. This implementation never returns {@code null} if
     *         {@code systemId} is non-{@code null}.
     *
     * @throws SAXException        if either the provided arguments or the input
     *                             source were invalid or not allowed.
     * @throws java.io.IOException if an I/O problem was found while forming the URL
     *                             to the input source, or when connecting to it.
     */
    @Override
    public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId)
        throws SAXException, IOException
    {
        if (publicId == null) {
            publicId = systemIdToPublicId.get(systemId);
        } else if (systemId == null) {
            systemId = getSystemIdFromPublicId(publicId);
        }
        String fname = systemIdToFilename.get(systemId);

        InputSource isrc = null;
        if (fname != null) {
            Reader re = dtdLoader.loadDTDfromClasspath(loader, fname);
            if (re != null) {
                isrc = new InputSource(re);
                isrc.setPublicId(publicId);
                if (systemId != null) {
                    isrc.setSystemId(systemId);
                }
            } else {
                throw new SAXException("Could not find resource: " + fname);
            }
        } else if (systemId != null) {
            URL enturl;
            if (baseURI != null) {
                URL base = new URL(baseURI);
                enturl = new URL(base, systemId);
            } else {
                enturl = new URL(systemId);
            }
            if (isInvalidProtocol(enturl.getProtocol())) {
                throw new SAXException("Invalid url protocol: " + enturl.getProtocol());
            }
            if (isWhitelistEnabled() && !isWhitelistedHost(enturl.getHost())) {
                throw new SAXException(
                    "Whitelist is enabled, and attempted to retrieve data from " + enturl.toExternalForm());
            }

            boolean invalidPath = isInvalidPath(enturl.getPath());
            String charset = "UTF-8";
            URLConnection con = openConnection(enturl);
            connect(con);
            String conType = con.getContentType();
            if (conType != null) {
                int sepidx = conType.indexOf(';');
                if (sepidx != -1 && sepidx < conType.length()) {
                    conType = conType.substring(0, sepidx);
                    charset = AgentUtil.findCharset(conType, sepidx + 1);
                }
            }

            if (invalidPath && !isValidContentType(conType)) {
                // Disconnect
                if (con instanceof HttpURLConnection) {
                    ((HttpURLConnection) con).disconnect();
                }
                String msg = enturl.toExternalForm();
                if (conType != null) {
                    // Sanitize untrusted content-type by removing control characters
                    // ('Other, Control' unicode category).
                    conType = conType.replaceAll("\\p{Cc}", "*CTRL*");
                    msg = "URL served with invalid type (" + conType + "): " + msg;
                } else {
                    msg = "URL served with invalid type: " + msg;
                }
                throw new SAXException(msg);
            }

            isrc = new InputSource();
            isrc.setSystemId(enturl.toExternalForm());
            if (publicId != null) {
                isrc.setPublicId(publicId);
            }
            isrc.setEncoding(charset);
            InputStream is = con.getInputStream();
            isrc.setCharacterStream(new InputStreamReader(is, charset));
        } else {
            isrc = findExternalSubset(name, baseURI);
            // 'isrc' can be null safely: there is no SystemId URL to connect to
        }

        return isrc;
    }

    private String getSystemIdFromPublicId(String publicId)
    {
        Iterator<Entry<String, String>> it = systemIdToPublicId.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, String> entry = it.next();
            if (publicId.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Determine if the given path is considered invalid for a DTD.
     * <p>
     * To be valid, must end with {@code .dtd}, {@code .ent} or {@code .mod}.
     * </p>
     *
     * @param path the path to check.
     * @return {@code true} if the path is invalid for a DTD, {@code false} otherwise.
     */
    protected boolean isInvalidPath(String path)
    {
        int len = path.length();
        String ext;
        return len < 5 || (!(ext = path.substring(len - 4)).equalsIgnoreCase(".dtd") && !ext.equalsIgnoreCase(".ent")
            && !ext.equalsIgnoreCase(".mod"));
    }

    /**
     * Is the whitelist enabled ?
     *
     * @return <code>true</code> if the whitelist is enabled.
     */
    protected boolean isWhitelistEnabled()
    {
        return whitelist != null;
    }

    /**
     * Is the given protocol not supported by this resolver ?
     * <p>
     * Only {@code http} and {@code https} are valid.
     * </p>
     *
     * @param protocol the protocol.
     * @return <code>true</code> if this resolver considers the given protocol invalid.
     */
    protected boolean isInvalidProtocol(String protocol)
    {
        return !protocol.equals("http") && !protocol.equals("https");
    }

    /**
     * Is the given host whitelisted ?
     *
     * @param host
     *            the host to test.
     * @return <code>true</code> if the given host is whitelisted.
     */
    protected boolean isWhitelistedHost(String host)
    {
        return whitelist.contains(host.toLowerCase(Locale.ROOT));
    }

    /**
     * Open a connection to the given URL.
     *
     * @param url the URL to connect to.
     * @return the connection.
     * @throws IOException if an I/O error happened opening the connection.
     */
    protected URLConnection openConnection(URL url) throws IOException
    {
        return url.openConnection();
    }

    /**
     * Connect the given <code>URLConnection</code>.
     *
     * @param con
     *            the <code>URLConnection</code>.
     * @throws IOException
     *             if a problem happened connecting.
     */
    protected void connect(final URLConnection con) throws IOException
    {
        con.setConnectTimeout(60000);
        dtdLoader.connect(con);
    }

    /**
     * Is the given string a valid DTD/entity content-type ?
     *
     * @param conType
     *            the content-type.
     * @return <code>true</code> if it is a valid DTD/entity content-type
     */
    protected boolean isValidContentType(String conType)
    {
        return conType != null
            && (conType.equals("application/xml-dtd") || conType.equals("text/xml-external-parsed-entity")
            || conType.equals("application/xml-external-parsed-entity"));
    }

    /**
     * Allow the application to resolve external entities.
     *
     * <p>
     * The parser will call this method before opening any external entity except
     * the top-level document entity. Such entities include the external DTD subset
     * and external parameter entities referenced within the DTD (in either case,
     * only if the parser reads external parameter entities), and external general
     * entities referenced within the document element (if the parser reads external
     * general entities). The application may request that the parser locate the
     * entity itself, that it use an alternative URI, or that it use data provided
     * by the application (as a character or byte input stream).
     * </p>
     * <p>
     * If no valid input source could be determined, this method will throw a
     * {@code SAXException} instead of returning {@code null} as other
     * implementations would do. If you have to retrieve a DTD which is not directly
     * provided by this resolver, you need to whitelist the host using
     * {@link #addHostToWhiteList(String)} first. Make sure that either the systemId
     * URL ends with a valid extension, or that the retrieved URL was served with a
     * valid DTD media type.
     * </p>
     *
     * @param publicId The public identifier of the external entity being
     *                 referenced, or {@code null} if none was supplied.
     * @param systemId The system identifier of the external entity being
     *                 referenced.
     * @return an {@code InputSource} object describing the new input source. This
     *         implementation never returns {@code null} if {@code systemId} is
     *         non-{@code null}.
     * @throws SAXException        if either the provided arguments or the input
     *                             source were invalid or not allowed.
     * @throws java.io.IOException if an I/O problem was found while forming the URL
     *                             to the input source, or when connecting to it.
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException
    {
        return resolveEntity(null, publicId, null, systemId);
    }

    /**
     * Resolve external entities according to the given {@code DocumentType}.
     * <p>
     * If no valid input source could be determined, this method will throw a
     * {@code SAXException} instead of returning {@code null} as other
     * implementations would do. If you have to retrieve a DTD which is not directly
     * provided by this resolver, you need to whitelist the host using
     * {@link #addHostToWhiteList(String)} first. Make sure that either the systemId
     * URL ends with a valid extension, or that the retrieved URL was served with a
     * valid DTD media type.
     * </p>
     *
     * @param dtDecl the {@code DocumentType}.
     * @return an {@code InputSource} object describing the new input source.
     * @throws SAXException        if either the provided arguments or the input
     *                             source were invalid or not allowed.
     * @throws java.io.IOException if an I/O problem was found while forming the URL
     *                             to the input source, or when connecting to it.
     */
    public InputSource resolveEntity(DocumentType dtDecl) throws SAXException, IOException
    {
        return resolveEntity(dtDecl.getName(), dtDecl.getPublicId(), dtDecl.getBaseURI(), dtDecl.getSystemId());
    }

    /**
     * Set the class loader to be used to read the built-in DTDs.
     *
     * @param loader the class loader.
     */
    public void setClassLoader(ClassLoader loader)
    {
        this.loader = loader;
    }

    private static DTDLoader createDTDLoader()
    {
        DTDLoader loader;
        try {
            Class<?> cl = Class.forName("io.sf.carte.doc.xml.dtd.SMDTDLoader");
            Constructor<?> ctor = cl.getConstructor();
            loader = (DTDLoader) ctor.newInstance();
        } catch (Exception e) {
            loader = new SimpleDTDLoader();
        }
        return loader;
    }

    abstract static class DTDLoader
    {
        abstract void connect(URLConnection con) throws IOException;

        abstract Reader loadDTDfromClasspath(ClassLoader loader, String dtdFilename);
    }

    /**
     * Load DTDs without a Security Manager.
     */
    private static class SimpleDTDLoader extends DTDLoader
    {
        @Override
        void connect(final URLConnection con) throws IOException
        {
            con.connect();
        }

        @Override
        Reader loadDTDfromClasspath(final ClassLoader loader, final String dtdFilename)
        {
            InputStream is;
            if (loader != null) {
                is = loader.getResourceAsStream(dtdFilename);
            } else {
                is = LocalEntityResolver.class.getResourceAsStream(dtdFilename);
            }
            if (is == null) {
                is = ClassLoader.getSystemResourceAsStream(dtdFilename);
            }

            Reader re = null;
            if (is != null) {
                re = new InputStreamReader(is, StandardCharsets.UTF_8);
            }
            return re;
        }
    }
}
