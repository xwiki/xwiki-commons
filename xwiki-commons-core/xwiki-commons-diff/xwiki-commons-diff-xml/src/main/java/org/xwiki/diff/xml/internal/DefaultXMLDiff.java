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
package org.xwiki.diff.xml.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Singleton;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xwiki.component.annotation.Component;
import org.xwiki.diff.Chunk;
import org.xwiki.diff.Delta;
import org.xwiki.diff.DiffException;
import org.xwiki.diff.Patch;
import org.xwiki.diff.internal.ChangeDelta;
import org.xwiki.diff.internal.DefaultChunk;
import org.xwiki.diff.internal.DefaultPatch;
import org.xwiki.diff.internal.DeleteDelta;
import org.xwiki.diff.internal.InsertDelta;
import org.xwiki.diff.xml.StringSplitter;
import org.xwiki.diff.xml.XMLDiff;
import org.xwiki.diff.xml.XMLDiffConfiguration;

import com.github.difflib.DiffUtils;

/**
 * Default implementation of {@link XMLDiff}.
 * 
 * @version $Id$
 * @since 11.6RC1
 */
@Component
@Singleton
public class DefaultXMLDiff implements XMLDiff
{
    @Override
    public Map<Node, Patch<?>> diff(Node left, Node right, XMLDiffConfiguration config) throws DiffException
    {
        Map<Node, Patch<?>> patches = new LinkedHashMap<>();
        Patch<Node> rootPatch = new DefaultPatch<>();

        if (areSimilar(left, right)) {
            if (left != null) {
                patches.putAll(diffSimilarNodes(left, right, config));
            }
        } else if (left == null) {
            // Insert right.
            rootPatch.add(new InsertDelta<>(new DefaultChunk<>(-1, Collections.<Node>emptyList()),
                new DefaultChunk<>(XMLDiffUtils.getNodeIndex(right), Collections.singletonList(right))));
        } else if (right == null) {
            // Delete left.
            rootPatch.add(
                new DeleteDelta<>(new DefaultChunk<>(XMLDiffUtils.getNodeIndex(left), Collections.singletonList(left)),
                    new DefaultChunk<>(-1, Collections.<Node>emptyList())));
        } else {
            // Change left with right (when left and right are not similar).
            rootPatch.add(
                new ChangeDelta<>(new DefaultChunk<>(XMLDiffUtils.getNodeIndex(left), Collections.singletonList(left)),
                    new DefaultChunk<>(XMLDiffUtils.getNodeIndex(right), Collections.singletonList(right))));
        }

        if (!rootPatch.isEmpty()) {
            patches.put(null, rootPatch);
        }

        return patches;
    }

    private Map<Node, Patch<?>> diffSimilarNodes(Node left, Node right, XMLDiffConfiguration config)
        throws DiffException
    {
        Map<Node, Patch<?>> patches = new LinkedHashMap<>();

        if (left.getNodeValue() != null) {
            // These are value nodes (e.g. text, attributes, comments) so compute the value difference.
            if (!left.getNodeValue().equals(right.getNodeValue())) {
                StringSplitter splitter = config.getSplitterForNodeType(left.getNodeType());
                patches.put(left, diff(left.getNodeValue(), right.getNodeValue(), splitter));
            }
        } else {
            // Compute the difference between attributes.
            if (left.getAttributes() != null) {
                // Compute the difference between element attributes.
                extend(patches, diff(left.getAttributes(), right.getAttributes(), config), left);
            }

            // Compute the difference between child nodes. We do this only for non-value node types because depending on
            // the implementation some value nodes (attributes for instance) may have children.
            extend(patches, diff(left.getChildNodes(), right.getChildNodes(), config), left);
        }

        return patches;
    }

    @Override
    public Map<Node, Patch<?>> diff(NodeList left, NodeList right, XMLDiffConfiguration config) throws DiffException
    {
        return diff(XMLDiffUtils.asList(left), XMLDiffUtils.asList(right), config);
    }

    @Override
    public Map<Node, Patch<?>> diff(NamedNodeMap left, NamedNodeMap right, XMLDiffConfiguration config)
        throws DiffException
    {
        return diff(asMap(left), asMap(right), config);
    }

    private Map<String, Node> asMap(NamedNodeMap namedNodeMap)
    {
        Map<String, Node> map = new HashMap<>();
        for (int i = 0; i < namedNodeMap.getLength(); i++) {
            Node node = namedNodeMap.item(i);
            map.put(node.getNodeName(), node);
        }
        return map;
    }

    protected Patch<?> diff(String left, String right, StringSplitter splitter) throws DiffException
    {
        return new DefaultPatch<>(DiffUtils.diff(splitter.split(left), splitter.split(right)));
    }

    protected Map<Node, Patch<?>> diff(List<Node> left, List<Node> right, XMLDiffConfiguration config)
        throws DiffException
    {
        Map<Node, Patch<?>> patches = new LinkedHashMap<>();

        // First compute the difference between the direct children.
        Patch<Node> patch =
            new DefaultPatch<>(DiffUtils.diff(left, right, (alice, bob) -> this.areVerySimilar(alice, bob, config)));

        // Then compute the difference inside the child elements that are very similar (for all the descendants).
        Set<Node> leftModified = patch.stream().map(Delta::getPrevious).map(Chunk::getElements).flatMap(List::stream)
            .collect(Collectors.toSet());
        Set<Node> rightModified = patch.stream().map(Delta::getNext).map(Chunk::getElements).flatMap(List::stream)
            .collect(Collectors.toSet());
        List<Node> leftVerySimilar = new ArrayList<>(left);
        leftVerySimilar.removeAll(leftModified);
        List<Node> rightVerySimilar = new ArrayList<>(right);
        rightVerySimilar.removeAll(rightModified);
        assert leftVerySimilar.size() == rightVerySimilar.size();
        // We add the patches for the very similar nodes first because they don't change the node index at this level.
        for (int i = 0; i < leftVerySimilar.size(); i++) {
            patches.putAll(diff(leftVerySimilar.get(i), rightVerySimilar.get(i), config));
        }

        // This patch can change the index of the child nodes at this level (by adding and removing child nodes) so we
        // add it at the end, in order for it to be applied after the child nodes have been modified.
        addPatch(patch, patches, config);

        return patches;
    }

    protected Map<Node, Patch<?>> diff(Map<String, Node> left, Map<String, Node> right, XMLDiffConfiguration config)
        throws DiffException
    {
        Map<Node, Patch<?>> patches = new LinkedHashMap<>();
        Patch<Node> patch = new DefaultPatch<>();

        Set<String> deletedKeys = new HashSet<>(left.keySet());
        deletedKeys.removeAll(right.keySet());
        if (!deletedKeys.isEmpty()) {
            List<Node> deletedNodes = deletedKeys.stream().map(left::get).collect(Collectors.toList());
            patch.add(new DeleteDelta<>(new DefaultChunk<>(-1, deletedNodes),
                new DefaultChunk<>(-1, Collections.emptyList())));
        }

        Set<String> insertedKeys = new HashSet<>(right.keySet());
        insertedKeys.removeAll(left.keySet());
        if (!insertedKeys.isEmpty()) {
            List<Node> insertedNodes = insertedKeys.stream().map(right::get).collect(Collectors.toList());
            patch.add(new InsertDelta<>(new DefaultChunk<>(-1, Collections.emptyList()),
                new DefaultChunk<>(-1, insertedNodes)));
        }

        // Add the patch for the deleted / added attributes.
        addPatch(patch, patches, config);

        // Add the patches for the modified attributes.
        Set<String> keptKeys = new HashSet<>(left.keySet());
        keptKeys.retainAll(right.keySet());
        for (String keptKey : keptKeys) {
            patches.putAll(diff(left.get(keptKey), right.get(keptKey), config));
        }

        return patches;
    }

    /**
     * Two nodes are similar if and only if the following conditions are satisfied:
     * <ul>
     * <li>the two nodes are of the same type</li>
     * <li>the following string attributes are equal: nodeName, localName, namespaceURI, prefix; this is: they are both
     * null, or they have the same length and are character for character identical.</li>
     * </ul>
     * The difference from {@link Node#isEqualNode(Node)} is that we don't check the node value (e.g. for text nodes),
     * its child nodes and neither the attributes (for element nodes).
     * 
     * @param left the left side of the comparison
     * @param right the right side of the comparison
     * @return {@code true} if the given nodes are similar
     * @see Node#isEqualNode(Node)
     */
    protected boolean areSimilar(Node left, Node right)
    {
        return left == right || (left != null && right != null
            && new EqualsBuilder().append(left.getNodeType(), right.getNodeType())
                .append(left.getNodeName(), right.getNodeName()).append(left.getLocalName(), right.getLocalName())
                .append(left.getNamespaceURI(), right.getNamespaceURI()).append(left.getPrefix(), right.getPrefix())
                .isEquals());
    }

    protected boolean areSimilar(List<Node> left, List<Node> right)
    {
        if (left.size() != right.size()) {
            return false;
        }
        for (int i = 0; i < left.size(); i++) {
            if (!areSimilar(left.get(i), right.get(i))) {
                return false;
            }
        }
        return true;
    }

    protected boolean areVerySimilar(Node left, Node right, XMLDiffConfiguration config)
    {
        return areSimilar(left, right) && (left == null || getDiffPercentage(left.getTextContent(),
            right.getTextContent(), config) < config.getSimilarityThreshold());
    }

    private double getDiffPercentage(String left, String right, XMLDiffConfiguration config)
    {
        if (left != null && right != null) {
            // Use the text node splitter because the difference percentage is computed on the inner text.
            StringSplitter splitter = config.getSplitterForNodeType(Node.TEXT_NODE);
            List<Object> leftList = splitter.split(left);
            List<Object> rightList = splitter.split(right);
            if (leftList.equals(rightList)) {
                return 0;
            } else {
                // Note that the max length can't be zero because leftList and rightList are different.
                int maxLength = Math.max(leftList.size(), rightList.size());
                try {
                    Patch<?> patch = new DefaultPatch<>(DiffUtils.diff(leftList, rightList));
                    int levenshteinDistance =
                        patch.stream().map(delta -> Math.max(delta.getPrevious().size(), delta.getNext().size()))
                            .reduce(0, Integer::sum);
                    return (double) levenshteinDistance / maxLength;
                } catch (DiffException e) {
                    // This shouldn't happen. Let's assume the strings are completely different.
                    return 1;
                }
            }
        } else if (left == right) {
            // Both are null.
            return 0;
        } else {
            // One is null and the other is not.
            return 1;
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void extend(Map<Node, Patch<?>> left, Map<Node, Patch<?>> right, Node context)
    {
        for (Map.Entry<Node, Patch<?>> entry : right.entrySet()) {
            Node key = entry.getKey() != null ? entry.getKey() : context;
            Patch<?> patch = left.getOrDefault(key, new DefaultPatch<>());
            patch.addAll((List) entry.getValue());
            left.put(key, patch);
        }
    }

    private void addPatch(Patch<Node> patch, Map<Node, Patch<?>> patches, XMLDiffConfiguration config)
        throws DiffException
    {
        // Compute the lower level difference for nodes that are similar (within a change delta).
        List<Delta<Node>> deltasToRemove = new ArrayList<>();
        for (Delta<Node> delta : patch) {
            if (delta.getType() == Delta.Type.CHANGE
                && areSimilar(delta.getPrevious().getElements(), delta.getNext().getElements())) {
                deltasToRemove.add(delta);
                for (int i = 0; i < delta.getPrevious().size(); i++) {
                    Node left = delta.getPrevious().getElements().get(i);
                    Node right = delta.getNext().getElements().get(i);
                    extend(patches, diff(left, right, config), left);
                }
            }
        }

        patch.removeAll(deltasToRemove);
        if (!patch.isEmpty()) {
            // This patch can change the index of the child nodes (by adding and removing child nodes) so we add it at
            // the end, in order for it to be applied after the child nodes have been modified.
            patches.put(null, patch);
        }
    }
}
