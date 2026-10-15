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
import org.xwiki.store.blob.BlobStoreException;

import software.amazon.awssdk.awscore.exception.AwsErrorDetails;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Helpers for implementing {@link org.xwiki.store.blob.BlobWriteMode#CREATE_NEW} on S3 services.
 * <p>
 * The store checks that the object is absent (a {@code HEAD} request) before writing it, and adds a condition
 * ({@code If-None-Match: *}) to the write when the service supports it, which makes the check atomic. Not every
 * S3-compatible service supports conditions on every operation: some reject them with "501 Not Implemented", others
 * may ignore them, which is why the check is always performed.
 *
 * @version $Id$
 * @since 18.10.0RC1
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

    /**
     * The configuration property that controls the condition of uploads.
     */
    public static final String CONDITIONAL_WRITES_PROPERTY = "store.s3.conditionalWrites";

    /**
     * The configuration property that controls the conditions of copies.
     */
    public static final String CONDITIONAL_COPIES_PROPERTY = "store.s3.conditionalCopies";

    private static final int FORBIDDEN = 403;

    private static final int NOT_IMPLEMENTED = 501;

    private static final String NOT_IMPLEMENTED_CODE = "NotImplemented";

    private static final String BLOB_ALREADY_EXISTS = "Blob already exists";

    private S3ConditionalWriteSupport()
    {
        // Utility class.
    }

    /**
     * Check that no object exists at the given key. On its own, this check is not atomic: another writer can still
     * create the object between this check and the write, unless the write carries a condition that the service
     * enforces.
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
            String message = "Failed to check whether the blob at path [%s] already exists".formatted(blobPath);
            if (e instanceof S3Exception s3Exception && s3Exception.statusCode() == FORBIDDEN) {
                // AWS S3 answers 403 instead of 404 to a HEAD on a missing object without s3:ListBucket.
                message += ". The credentials need the permissions to read objects and to list the bucket "
                    + "(s3:GetObject and s3:ListBucket on AWS S3).";
            }
            throw new IOException(message, e);
        }

        throw new IOException(BLOB_ALREADY_EXISTS, new BlobAlreadyExistsException(blobPath));
    }

    /**
     * Translate an S3 error raised by a conditional upload ({@code If-None-Match} on {@code PutObject} or
     * {@code CompleteMultipartUpload}) into the exception expected by the blob store API.
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
            return new IOException(getNotImplementedMessage(blobPath, CONDITIONAL_WRITES_PROPERTY), e);
        }

        return new IOException(genericMessage, e);
    }

    /**
     * @param blobPath the blob that was being written
     * @param property the configuration property that controls the conditions of the rejected request
     * @return the message explaining that the S3 service does not support the conditions of the request and how to
     *     disable them
     */
    public static String getNotImplementedMessage(BlobPath blobPath, String property)
    {
        return ("The S3 service rejected the conditional request used to write the blob at path [%s] as not "
            + "implemented: it probably does not support conditional requests for this operation. Set [%s] to false "
            + "to send these requests without conditions (see the documentation of the property for what this gives "
            + "up).").formatted(blobPath, property);
    }

    /**
     * The upload helpers report the failures of their own requests as {@link IOException}: the existence check, and
     * the conditional {@code CompleteMultipartUpload}. This gives the exception a copy should throw for them.
     *
     * @param e the error raised while copying
     * @return the {@link BlobAlreadyExistsException} found as cause, an exception keeping the message that explains a
     *     rejected condition, or {@code null} if the error is not such an upload failure
     */
    public static BlobStoreException translateUploadFailure(Throwable e)
    {
        if (e instanceof IOException) {
            if (e.getCause() instanceof BlobStoreException cause) {
                return cause;
            }
            if (isNotImplemented(e.getCause())) {
                return new BlobStoreException(e.getMessage(), e);
            }
        }

        return null;
    }

    /**
     * @param e the error raised by the S3 client
     * @return {@code true} if the service answered that a precondition of the request failed (412)
     */
    public static boolean isPreconditionFailed(Throwable e)
    {
        return e instanceof S3Exception s3Exception && s3Exception.statusCode() == PRECONDITION_FAILED;
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
