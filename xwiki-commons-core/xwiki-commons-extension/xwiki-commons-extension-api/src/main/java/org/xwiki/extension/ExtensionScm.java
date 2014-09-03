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
package org.xwiki.extension;

/**
 * Provide informations related to extensions's Source Control Management.
 * 
 * @version $Id$
 * @since 6.3M1
 */
public interface ExtensionScm
{
    /**
     * Get the source control management system URL that describes the repository and how to connect to the repository.
     * For more information, see the <a href="http://maven.apache.org/scm/scm-url-format.html">URL format</a> and <a
     * href="http://maven.apache.org/scm/scms-overview.html">list of supported SCMs</a>. This connection is read-only. <br />
     * <b>Default value is</b>: parent value [+ path adjustment] + artifactId.
     * 
     * @return the connection
     */
    ExtensionScmConnection getConnection();

    /**
     * Get just like <code>connection</code>, but for developers, i.e. this scm connection will not be read only. <br />
     * <b>Default value is</b>: parent value [+ path adjustment] + artifactId.
     * 
     * @return the connection
     */
    ExtensionScmConnection getDeveloperConnection();

    /**
     * Get the tag of current code. By default, it's set to HEAD during development.
     * 
     * @return the tab name
     */
    String getTag();

    /**
     * Get the URL to the project's browsable SCM repository. <br />
     * <b>Default value is</b>: parent value [+ path adjustment] + artifactId.
     * 
     * @return the URL
     */
    String getUrl();
}
