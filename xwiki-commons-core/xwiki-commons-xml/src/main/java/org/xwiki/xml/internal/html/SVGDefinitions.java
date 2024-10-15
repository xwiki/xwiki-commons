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
 * Alternatively, at your choice, the contents of this file may be used under the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package org.xwiki.xml.internal.html;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.inject.Singleton;

import org.xwiki.component.annotation.Component;

/**
 * Provides SVG tag and attribute definitions with a focus on safe tags/attributes.
 * <p>
 * Unless otherwise noted, lists of elements and attributes are copied from DOMPurify by Cure53 and other contributors |
 * Released under the Apache license 2.0 and Mozilla Public License 2.0 -
 * <a href="https://github.com/cure53/DOMPurify/blob/main/LICENSE">LICENSE</a>.
 *
 * @version $Id$
 * @since 14.6RC1
 */
@Component(roles = SVGDefinitions.class)
@Singleton
// This file has lists of strings copied from a source, making them constants would complicate updating from
// upstream.
@SuppressWarnings("checkstyle:MultipleStringLiterals")
public class SVGDefinitions
{
    private final Set<String> safeTags;

    private final Set<String> filterTags;

    private final Set<String> allTags;

    private final Set<String> allowedAttributes;

    private final Set<String> commonHTMLElements;

    private final Set<String> htmlIntegrationPoints;

    /**
     * Default constructor.
     */
    public SVGDefinitions()
    {
        this.allowedAttributes = new HashSet<>(
            Arrays.asList("accent-height", "accumulate", "additive", "alignment-baseline", "ascent", "attributename",
                "attributetype", "azimuth", "basefrequency", "baseline-shift", "begin", "bias", "by", "class", "clip",
                "clippathunits", "clip-path", "clip-rule", "color", "color-interpolation",
                "color-interpolation-filters", "color-profile", "color-rendering", "cx", "cy", "d", "dx", "dy",
                "diffuseconstant", "direction", "display", "divisor", "dur", "edgemode", "elevation", "end", "fill",
                "fill-opacity", "fill-rule", "filter", "filterunits", "flood-color", "flood-opacity", "font-family",
                "font-size", "font-size-adjust", "font-stretch", "font-style", "font-variant", "font-weight", "fx",
                "fy", "g1", "g2", "glyph-name", "glyphref", "gradientunits", "gradienttransform", "height", "href",
                "id", "image-rendering", "in", "in2", "k", "k1", "k2", "k3", "k4", "kerning", "keypoints", "keysplines",
                "keytimes", "lang", "lengthadjust", "letter-spacing", "kernelmatrix", "kernelunitlength",
                "lighting-color", "local", "marker-end", "marker-mid", "marker-start", "markerheight", "markerunits",
                "markerwidth", "maskcontentunits", "maskunits", "max", "mask", "media", "method", "mode", "min", "name",
                "numoctaves", "offset", "operator", "opacity", "order", "orient", "orientation", "origin", "overflow",
                "paint-order", "path", "pathlength", "patterncontentunits", "patterntransform", "patternunits",
                "points", "preservealpha", "preserveaspectratio", "primitiveunits", "r", "rx", "ry", "radius", "refx",
                "refy", "repeatcount", "repeatdur", "restart", "result", "rotate", "scale", "seed", "shape-rendering",
                "specularconstant", "specularexponent", "spreadmethod", "startoffset", "stddeviation", "stitchtiles",
                "stop-color", "stop-opacity", "stroke-dasharray", "stroke-dashoffset", "stroke-linecap",
                "stroke-linejoin", "stroke-miterlimit", "stroke-opacity", "stroke", "stroke-width", "style",
                "surfacescale", "systemlanguage", "tabindex", "targetx", "targety", "transform", "transform-origin",
                "text-anchor", "text-decoration", "text-rendering", "textlength", "type", "u1", "u2", "unicode",
                "values", "viewbox", "visibility", "version", "vert-adv-y", "vert-origin-x", "vert-origin-y", "width",
                "word-spacing", "wrap", "writing-mode", "xchannelselector", "ychannelselector", "x", "x1", "x2",
                "xmlns", "y", "y1", "y2", "z", "zoomandpan"));

        this.safeTags = new HashSet<>(
            Arrays.asList("svg", "a", "altglyph", "altglyphdef", "altglyphitem", "animatecolor", "animatemotion",
                "animatetransform", "circle", "clippath", "defs", "desc", "ellipse", "filter", "font", "g", "glyph",
                "glyphref", "hkern", "image", "line", "lineargradient", "marker", "mask", "metadata", "mpath", "path",
                "pattern", "polygon", "polyline", "radialgradient", "rect", "stop", "style", "switch", "symbol", "text",
                "textpath", "title", "tref", "tspan", "view", "vkern"));

        this.filterTags = new HashSet<>(
            Arrays.asList("feBlend", "feColorMatrix", "feComponentTransfer", "feComposite", "feConvolveMatrix",
                "feDiffuseLighting", "feDisplacementMap", "feDistantLight", "feFlood", "feFuncA", "feFuncB", "feFuncG",
                "feFuncR", "feGaussianBlur", "feImage", "feMerge", "feMergeNode", "feMorphology", "feOffset",
                "fePointLight", "feSpecularLighting", "feSpotLight", "feTile", "feTurbulence"));

        this.allTags = new HashSet<>(
            Arrays.asList("animate", "color-profile", "cursor", "discard", "fedropshadow", "font-face",
                "font-face-format", "font-face-name", "font-face-src", "font-face-uri", "foreignobject", "hatch",
                "hatchpath", "mesh", "meshgradient", "meshpatch", "meshrow", "missing-glyph", "script", "set",
                "solidcolor", "unknown", "use"));

        this.allTags.addAll(this.filterTags);
        this.allTags.addAll(this.safeTags);

        this.commonHTMLElements = new HashSet<>(Arrays.asList("title", "style", "font", "a", "script"));

        this.htmlIntegrationPoints = new HashSet<>(Arrays.asList("foreignobject", "desc", "title", "annotation-xml"));
    }

    /**
     * @param attributeName the attribute to check
     * @return if the attribute is allowed, i.e., considered safe
     */
    public boolean isAllowedAttribute(String attributeName)
    {
        return this.allowedAttributes.contains(attributeName);
    }

    /**
     * @param tagName the name of the tag to check
     * @return if the tag is considered safe
     */
    public boolean isSafeTag(String tagName)
    {
        return this.safeTags.contains(tagName) || isFilterTag(tagName);
    }

    /**
     * @param tagName the name of the tag to check
     * @return if the tag is an SVG filter
     */
    public boolean isFilterTag(String tagName)
    {
        return this.filterTags.contains(tagName);
    }

    /**
     * @param tagName the name of the tag to check
     * @return if the tag is an SVG tag
     */
    public boolean isSVGTag(String tagName)
    {
        return this.allTags.contains(tagName);
    }

    /**
     * @param tagName the name of the tag to check
     * @return if the tag is both an HTML and an SVG tag
     */
    public boolean isCommonHTMLElement(String tagName)
    {
        return this.commonHTMLElements.contains(tagName);
    }

    /**
     * @param tagName the name of the tag to check
     * @return if the tag can contain HTML children
     */
    public boolean isHTMLIntegrationPoint(String tagName)
    {
        return this.htmlIntegrationPoints.contains(tagName);
    }
}
