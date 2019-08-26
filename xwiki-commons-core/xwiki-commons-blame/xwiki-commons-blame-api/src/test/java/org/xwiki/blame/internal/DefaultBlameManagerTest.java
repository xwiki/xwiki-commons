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

package org.xwiki.blame.internal;

import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;

import org.junit.jupiter.api.Test;
import org.xwiki.blame.AnnotatedContent;
import org.xwiki.blame.AnnotatedElement;
import org.xwiki.test.junit5.mockito.ComponentTest;
import org.xwiki.test.junit5.mockito.InjectMockComponents;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.jmock.Expectations.same;
import static org.junit.Assert.assertThat;

@ComponentTest
public class DefaultBlameManagerTest
{
    /**
     * Small class simulating revision metadata;
     */
    class Revision
    {
        private final String rev;

        Revision(String rev)
        {
            this.rev = rev;
        }

        public String toString()
        {
            return rev;
        }
    }

    @InjectMockComponents
    private DefaultBlameManager blameManager;

    @Test
    public void blameNullRevision()
    {
        assertThat(blameManager.blame(null, null, null), nullValue());
        assertThat(blameManager.blame(null, null, Collections.<String>emptyList()), nullValue());
        assertThat(blameManager.blame(null, new Object(), null), nullValue());
    }

    @Test
    public void blame()
    {
        Revision rev1 = new Revision("rev1");
        Revision rev2 = new Revision("rev2");
        Revision rev3 = new Revision("rev3");

        AnnotatedContent<Revision, String> annotatedContent = blameManager.blame(null, rev3, Arrays.asList(
            "Jackdaws love my big sphinx of quartz.",
            "Cozy lummox gives smart squid who asks for job pen.",
            "The quick red fox",
            "jumps over",
            "the lazy dog"));

        assertThat(annotatedContent.isEntirelyAnnotated(), is(false));
        assertThat(annotatedContent.getOldestRevision(), same(rev3));

        annotatedContent = blameManager.blame(annotatedContent,
            rev2, Arrays.asList(
                "Cozy lummox gives smart squid who asks for job pen.",
                "The quick red fox",
                "jumps over the lazy dog"));

        assertThat(annotatedContent.isEntirelyAnnotated(), is(false));
        assertThat(annotatedContent.getOldestRevision(), same(rev2));

        annotatedContent = blameManager.blame(annotatedContent,
            rev1, Arrays.asList(
                "Cozy lummox gives smart squid who asks for job pen.",
                "The quick brown fox",
                "jumps over the lazy dog."));

        assertThat(annotatedContent.isEntirelyAnnotated(), is(false));
        assertThat(annotatedContent.getOldestRevision(), same(rev1));

        annotatedContent = blameManager.blame(annotatedContent, null, null);

        assertThat(annotatedContent.isEntirelyAnnotated(), is(true));
        assertThat(annotatedContent.getOldestRevision(), nullValue());

        Iterator<AnnotatedElement<Revision, String>> iter = annotatedContent.iterator();

        assertThat(iter.hasNext(), is(true));
        AnnotatedElement<Revision, String> annotatedElement = iter.next();
        assertThat(annotatedElement.getElement(), is("Jackdaws love my big sphinx of quartz."));
        assertThat(annotatedElement.getRevision(), same(rev3));

        assertThat(iter.hasNext(), is(true));
        annotatedElement = iter.next();
        assertThat(annotatedElement.getElement(), is("Cozy lummox gives smart squid who asks for job pen."));
        assertThat(annotatedElement.getRevision(), same(rev1));

        assertThat(iter.hasNext(), is(true));
        annotatedElement = iter.next();
        assertThat(annotatedElement.getElement(), is("The quick red fox"));
        assertThat(annotatedElement.getRevision(), same(rev2));

        assertThat(iter.hasNext(), is(true));
        annotatedElement = iter.next();
        assertThat(annotatedElement.getElement(), is("jumps over"));
        assertThat(annotatedElement.getRevision(), same(rev3));

        assertThat(iter.hasNext(), is(true));
        annotatedElement = iter.next();
        assertThat(annotatedElement.getElement(), is("the lazy dog"));
        assertThat(annotatedElement.getRevision(), same(rev3));
    }
}
