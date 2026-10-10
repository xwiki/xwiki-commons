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
package org.xwiki.store.blob.internal;

import java.io.IOException;

import org.xwiki.store.blob.BlobAlreadyExistsException;
import org.xwiki.store.blob.BlobPath;

import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Helpers for implementing {@link org.xwiki.store.blob.BlobWriteMode#CREATE_NEW} on S3 services.
 * <p>
 * The reliable way to create an object only if it does not exist yet is a conditional write ({@code If-None-Match:
 * *}). Many S3-compatible services do not implement conditional writes. For those, the store can be configured to
 * fall back to a non-atomic check (a {@code HEAD} request) followed by an unconditional write.
 *
 * @version $Id$
 * @since 18.9.0RC1
 */
public final class S3ConditionalWriteSupport
{
    /**
     * The HTTP status code returned by S3 when a precondition fails.
     */
    public static final int PRECONDITION_FAILED = 412;

    /**
     * The value of the {@code If-None-Match} header used to require that the object does not exist.
     */
    public static final String WILDCARD = "*";

    private static final int NOT_IMPLEMENTED = 501;

    private static final String NOT_IMPLEMENTED_CODE = "NotImplemented";

    private static final String BLOB_ALREADY_EXISTS = "Blob already exists";

    private S3ConditionalWriteSupport()
    {
        // Utility class.
    }

    /**
     * Check that no object exists at the given key. This is the non-atomic fallback used when conditional writes are
     * disabled: another writer can still create the object between this check and the write.
     *
     * @param s3Client the S3 client
     * @param bucketName the bucket
     * @param s3Key the key of the object that is about to be written
     * @param blobPath the blob path, for error reporting
     * @throws IOException wrapping a {@link BlobAlreadyExistsException} if the object exists, or describing the
     *     failure if the check could not be performed
     */
    public static void assertAbsent(S3Client s3Client, String bucketName, String s3Key, BlobPath blobPath)
        throws IOException
    {
        try {
            s3Client.headObject(HeadObjectRequest.builder().bucket(bucketName).key(s3Key).build());
        } catch (NoSuchKeyException e) {
            // This is what we want.
            return;
        } catch (Exception e) {
            throw new IOException("Failed to check whether the blob at path [%s] already exists".formatted(blobPath),
                e);
        }

        throw new IOException(BLOB_ALREADY_EXISTS, new BlobAlreadyExistsException(blobPath));
    }

    /**
     * Translate an S3 error raised by a conditional write into the exception expected by the blob store API.
     *
     * @param e the error raised by the S3 client
     * @param blobPath the blob path, for error reporting
     * @param genericMessage the message to use when the error is not related to the write condition
     * @return the exception to throw
     */
    public static IOException translateConditionalWriteFailure(S3Exception e, BlobPath blobPath,
        String genericMessage)
    {
        if (e.statusCode() == PRECONDITION_FAILED) {
            return new IOException(BLOB_ALREADY_EXISTS, new BlobAlreadyExistsException(blobPath, e));
        }

        if (isNotImplemented(e)) {
            return new IOException(getNotImplementedMessage(blobPath), e);
        }

        return new IOException(genericMessage, e);
    }

    /**
     * @param blobPath the blob that was being written
     * @return the message explaining that the S3 service does not support conditional requests and how to disable
     *     them
     */
    public static String getNotImplementedMessage(BlobPath blobPath)
    {
        return ("The S3 service rejected the conditional request used to write the blob at path [%s]. The service "
            + "probably does not support conditional requests (If-None-Match, x-amz-copy-source-if-match). Set "
            + "[store.s3.conditionalWrites] to false to use a non-atomic existence check instead.").formatted(blobPath);
    }

    /**
     * @param e the error raised by the S3 client
     * @return {@code true} if the service answered that the request (usually one of its headers) is not implemented
     */
    public static boolean isNotImplemented(Throwable e)
    {
        return e instanceof S3Exception s3Exception && isNotImplemented(s3Exception);
    }

    private static boolean isNotImplemented(S3Exception e)
    {
        if (e.statusCode() == NOT_IMPLEMENTED) {
            return true;
        }

        AwsErrorDetails details = e.awsErrorDetails();
        return details != null && NOT_IMPLEMENTED_CODE.equals(details.errorCode());
    }
}
