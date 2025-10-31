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

import org.junit.jupiter.api.extension.ExtendWith;
import org.xwiki.component.manager.ComponentManager;
import org.xwiki.store.blob.internal.S3BlobStore;
import org.xwiki.store.blob.internal.S3BlobStoreFactory;
import org.xwiki.store.blob.internal.S3ClientManager;
import org.xwiki.store.blob.internal.S3CopyOperations;
import org.xwiki.store.blob.internal.S3DeleteOperations;
import org.xwiki.test.annotation.ComponentList;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectComponentManager;

/**
 * Integration tests for {@link S3BlobStore} using MinIO container.
 *
 * @version $Id$
 * @since 17.10.0RC1
 */
@ComponentTest
@ExtendWith(BlobStoreExtension.class)
@ComponentList({
    S3BlobStoreFactory.class,
    S3BlobStore.class,
    S3ClientManager.class,
    S3CopyOperations.class,
    S3DeleteOperations.class
})
class S3BlobStoreIT extends AbstractBlobStoreIT
{
    @InjectBlobStoreContainer
    private BlobStoreContainer container;

    @InjectComponentManager
    private ComponentManager componentManager;

    @Override
    protected BlobStore createBlobStore(String name) throws Exception
    {
        S3BlobStoreProperties props = new S3BlobStoreProperties();
        props.setBucket(this.container.bucketName());
        props.setKeyPrefix(name);
        // Set multipart sizes to 5MB to reduce storage requirements
        props.setMultipartUploadPartSize(5L * 1024 * 1024);
        props.setMultipartCopyPartSize(5L * 1024 * 1024);

        // Get the factory from the component manager as we cannot inject it directly because the S3 configuration is
        // initialized too late. Injecting a provider isn't supported by the test framework.
        BlobStoreFactory factory = this.componentManager.getInstance(BlobStoreFactory.class, "s3");
        return factory.create(name, props);
    }
}
