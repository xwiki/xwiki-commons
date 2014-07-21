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

package org.xwiki.crypto.signer;

import java.security.GeneralSecurityException;

import org.xwiki.component.annotation.Role;
import org.xwiki.crypto.signer.param.CMSSignedDataGeneratorParameters;
import org.xwiki.stability.Unstable;

/**
 * Generate SignedData according to RFC 3852.
 *
 * @version $Id$
 * @since 6.0M1
 */
@Role
@Unstable
public interface CMSSignedDataGenerator
{
    /**
     * Generate the SignedData.
     *
     * @param data the data to be signed.
     * @param parameters the parameters for generation.
     * @return an ASN.1 SignedData sequence according to RFC 3852 with an empty EncapsulatedContentInfo.
     * @throws GeneralSecurityException on error.
     */
    byte[] generate(byte[] data, CMSSignedDataGeneratorParameters parameters) throws GeneralSecurityException;

    /**
     * Generate the SignedData.
     *
     * @param data the data to be signed.
     * @param parameters the parameters for generation.
     * @param embedData when true, the signed data is embedded into the SignedData.
     * @return an ASN.1 SignedData sequence according to RFC 3852.
     * @throws java.security.GeneralSecurityException on error.
     */
    byte[] generate(byte[] data, CMSSignedDataGeneratorParameters parameters, boolean embedData)
        throws GeneralSecurityException;
}
