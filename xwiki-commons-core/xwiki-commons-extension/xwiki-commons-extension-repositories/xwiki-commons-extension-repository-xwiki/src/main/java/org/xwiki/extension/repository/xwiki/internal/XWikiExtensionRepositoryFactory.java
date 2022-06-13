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
package org.xwiki.extension.repository.xwiki.internal;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.xwiki.component.annotation.Component;
import org.xwiki.component.phase.Initializable;
import org.xwiki.component.phase.InitializationException;
import org.xwiki.extension.ExtensionLicenseManager;
import org.xwiki.extension.internal.ExtensionFactory;
import org.xwiki.extension.repository.AbstractExtensionRepositoryFactory;
import org.xwiki.extension.repository.ExtensionRepository;
import org.xwiki.extension.repository.ExtensionRepositoryDescriptor;
import org.xwiki.extension.repository.ExtensionRepositoryException;
import org.xwiki.extension.repository.http.internal.HttpClientFactory;

/**
 * @version $Id$
 * @since 4.0M1
 */
@Component
@Singleton
@Named("xwiki")
public class XWikiExtensionRepositoryFactory extends AbstractExtensionRepositoryFactory implements Initializable
{
    @Inject
    private ExtensionLicenseManager licenseManager;

    @Inject
    private HttpClientFactory httpClientFactory;

    @Inject
    private ExtensionFactory factory;

    private JAXBContext jaxbContext;

    @Override
    public void initialize() throws InitializationException
    {
        try {
            this.jaxbContext = JAXBContext.newInstance("org.xwiki.extension.repository.xwiki.model.jaxb");
        } catch (JAXBException e) {
            throw new InitializationException("Failed to create JAXB context", e);
        }
    }

    /**
     * JAXBContext is thread safe but Marshaller is not.
     * 
     * @return a new instance of Marshaller
     * @throws JAXBException if an error was encountered while creating the {@code Marshaller} object
     */
    public Marshaller createMarshaller() throws JAXBException
    {
        return this.jaxbContext.createMarshaller();
    }

    /**
     * JAXBContext is thread safe but Unmarshaller is not.
     * 
     * @return a new instance of Unmarshaller
     * @throws JAXBException if an error was encountered while creating the {@code Unmarshaller} object
     */
    public Unmarshaller createUnmarshaller() throws JAXBException
    {
        return this.jaxbContext.createUnmarshaller();
    }

    // ExtensionRepositoryFactory

    @Override
    public ExtensionRepository createRepository(ExtensionRepositoryDescriptor repositoryDescriptor)
        throws ExtensionRepositoryException
    {
        try {
            return new XWikiExtensionRepository(repositoryDescriptor, this, this.licenseManager, this.httpClientFactory,
                this.factory);
        } catch (Exception e) {
            throw new ExtensionRepositoryException("Failed to create repository [" + repositoryDescriptor + "]", e);
        }
    }
}
