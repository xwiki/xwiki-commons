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
package org.xwiki.store.blob;

/**
 * Container holding S3-compatible storage connection details for tests.

 * @param endpoint the S3 endpoint URL
 * @param bucketName the bucket name
 * @param accessKey the access key
 * @param secretKey the secret key
 * @param region the AWS region
 * @param pathStyleAccess whether to use path style access
 * @version $Id$
 * @since 17.10.0RC1
 */
public record BlobStoreContainer(String endpoint, String bucketName, String accessKey, String secretKey, String region,
                                 boolean pathStyleAccess)
{
}
