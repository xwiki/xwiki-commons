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

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xwiki.diff.Chunk;
import org.xwiki.diff.Delta;
import org.xwiki.diff.DiffException;
import org.xwiki.diff.Patch;
import org.xwiki.diff.PatchException;
import org.xwiki.diff.xml.XMLDiff;
import org.xwiki.diff.xml.XMLDiffConfiguration;
import org.xwiki.diff.xml.XMLDiffMarker;

/**
 * Base class for implementing an {@link XMLDiffMarker}.
 * 
 * @version $Id$
 * @since 11.6RC1
 */
public abstract class AbstractXMLDiffMarker implements XMLDiffMarker
{
    private static final String FUTURE_PARENT = "xwiki-xml-diff-marker-future-parent";

    private static final String TEXT_WRAPPER = "xwiki-xml-diff-marker-text-wrapper";

    @Inject
    private XMLDiff xmlDiff;

    @Override
    public boolean markDiff(Node left, Node right) throws DiffException
    {
        // Normalize in order to avoid false changes.
        left.normalize();
        right.normalize();

        // Compute the differences.
        Map<Node, Patch<?>> patches = this.xmlDiff.diff(left, right, new XMLDiffConfiguration());

        // Filter patches for relevant changes.
        patches = filterPatches(patches);

        // Determine the change blocks and keep only the patches that target nodes inside a marked change block.
        patches = markDiffBlocks(patches);

        // Apply in-line changes.
        applyPatches(patches);

        removeTextWrappers(left);

        return !patches.isEmpty();
    }

    //
    // Filtering
    //

    protected Map<Node, Patch<?>> filterPatches(Map<Node, Patch<?>> patches)
    {
        Map<Node, Patch<?>> acceptedPatches = new LinkedHashMap<>();
        for (Map.Entry<Node, Patch<?>> entry : patches.entrySet()) {
            if (acceptPatch(entry.getKey(), entry.getValue())) {
                acceptedPatches.put(entry.getKey(), entry.getValue());
            }
        }
        return acceptedPatches;
    }

    @SuppressWarnings("unchecked")
    protected boolean acceptPatch(Node node, Patch<?> patch)
    {
        if (node == null || patch.isEmpty()) {
            // Don't accept patches that target the root node or that are empty.
        } else if (node.getNodeType() == Node.TEXT_NODE) {
            return acceptPatch((Text) node, (Patch<Character>) patch);
        } else if (node.getNodeType() == Node.ELEMENT_NODE) {
            return acceptPatch((Element) node, (Patch<Node>) patch);
        } else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
            return acceptPatch((Attr) node, (Patch<Character>) patch);
        }

        return false;
    }

    protected boolean acceptPatch(Text text, Patch<Character> patch)
    {
        Node parent = text.getParentNode();
        return parent != null && parent.getNodeType() == Node.ELEMENT_NODE && acceptChangesFor((Element) parent);
    }

    protected boolean acceptPatch(Element element, Patch<Node> patch)
    {
        if (!acceptChangesFor(element)) {
            return false;
        }

        List<Delta<Node>> attributesDeltas =
            patch.stream().filter(delta -> delta.getPrevious().getIndex() < 0).collect(Collectors.toList());
        if (attributesDeltas.size() == patch.size()) {
            // Only attribute changes.
            for (Delta<Node> delta : attributesDeltas) {
                boolean acceptAttributes =
                    delta.getPrevious().getElements().stream().anyMatch(node -> acceptChangesFor((Attr) node));
                acceptAttributes = acceptAttributes
                    || delta.getNext().getElements().stream().anyMatch(node -> acceptChangesFor((Attr) node));
                if (acceptAttributes) {
                    return true;
                }
            }
            return false;
        } else {
            // There are also child nodes changes.
            return true;
        }
    }

    protected boolean acceptPatch(Attr attribute, Patch<Character> patch)
    {
        return acceptChangesFor(attribute);
    }

    protected boolean acceptChangesFor(Element element)
    {
        return true;
    }

    protected boolean acceptChangesFor(Attr attribute)
    {
        return true;
    }

    //
    // Mark Change Blocks
    //

    protected Map<Node, Patch<?>> markDiffBlocks(Map<Node, Patch<?>> patches)
    {
        // Collect the change blocks and keep only the patches that target nodes inside these change blocks.
        Map<Node, Patch<?>> acceptedPatches = new LinkedHashMap<>();
        Set<Node> diffBlocks = new HashSet<>();
        for (Map.Entry<Node, Patch<?>> entry : patches.entrySet()) {
            Set<Node> patchDiffBlocks = getDiffBlocks(entry.getKey(), entry.getValue());
            if (!patchDiffBlocks.isEmpty() && !patchDiffBlocks.contains(null)) {
                acceptedPatches.put(entry.getKey(), entry.getValue());
                diffBlocks.addAll(patchDiffBlocks);
            }
        }

        // Mark the change blocks.
        diffBlocks.stream().forEach(node -> this.markDiffBlock((Element) node));

        // Take into account only the outer most change blocks.
        Set<Node> nestedDiffBlocks = diffBlocks.stream().filter(this::hasDiffBlockParent).collect(Collectors.toSet());
        nestedDiffBlocks.stream().forEach(node -> this.unmarkDiffBlock((Element) node));
        diffBlocks.removeAll(nestedDiffBlocks);

        return acceptedPatches;
    }

    @SuppressWarnings("unchecked")
    protected Set<Node> getDiffBlocks(Node node, Patch<?> patch)
    {
        Set<Node> diffBlocks = new HashSet<>();

        if (node.getNodeValue() != null) {
            // Text or attributes.
            diffBlocks.add(getDiffBlock(node));
        } else {
            Patch<Node> childrenPatch = (Patch<Node>) patch;
            List<Delta<Node>> childrenDeltas = childrenPatch.stream()
                .filter(delta -> delta.getPrevious().getIndex() >= 0).collect(Collectors.toList());
            if (childrenDeltas.size() < patch.size()) {
                // There are modified attributes.
                diffBlocks.add(getDiffBlock(node));
            } else {
                // There are only child node changes. Determine the change block for each modified child node.
                for (Delta<Node> delta : childrenPatch) {
                    diffBlocks.addAll(getDiffBlocks(node, delta.getPrevious().getElements()));
                    diffBlocks.addAll(getDiffBlocks(node, delta.getNext().getElements()));
                }
            }
        }

        return diffBlocks;
    }

    private Set<Node> getDiffBlocks(Node parent, List<Node> children)
    {
        return children.stream().map(child -> this.getDiffBlock(parent, child)).collect(Collectors.toSet());
    }

    private Node getDiffBlock(Node parent, Node child)
    {
        // The child node might not be attached to the specified parent yet (it will after we apply the patch).
        if (child.getParentNode() != parent) {
            // We need this information in order to be able to ignore nested change blocks.
            child.setUserData(FUTURE_PARENT, parent, null);
        }

        return getDiffBlock(child);
    }

    protected Node getDiffBlock(Node node)
    {
        Node diffBlock = node;
        if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
            diffBlock = ((Attr) node).getOwnerElement();
        }

        while (diffBlock != null
            && (diffBlock.getNodeType() != Node.ELEMENT_NODE || !acceptAsDiffBlock((Element) diffBlock))) {
            diffBlock = getParentNode(diffBlock);
        }

        return diffBlock;
    }

    protected abstract boolean acceptAsDiffBlock(Element element);

    protected abstract void markDiffBlock(Element element);

    protected abstract void unmarkDiffBlock(Element element);

    protected abstract boolean isMarkedAsDiffBlock(Element element);

    private boolean hasDiffBlockParent(Node node)
    {
        Node parent = getParentNode(node);
        while (parent != null
            && (parent.getNodeType() != Node.ELEMENT_NODE || !isMarkedAsDiffBlock((Element) parent))) {
            parent = getParentNode(parent);
        }
        return parent != null && parent.getNodeType() == Node.ELEMENT_NODE;
    }

    private Node getParentNode(Node node)
    {
        if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
            return ((Attr) node).getOwnerElement();
        } else {
            Node futureParent = (Node) node.getUserData(FUTURE_PARENT);
            return futureParent != null ? futureParent : node.getParentNode();
        }
    }

    //
    // Mark In-line changes
    //

    protected void applyPatches(Map<Node, Patch<?>> patches) throws PatchException
    {
        for (Map.Entry<Node, Patch<?>> entry : patches.entrySet()) {
            this.applyPatch(entry.getKey(), entry.getValue());
        }
    }

    @SuppressWarnings("unchecked")
    protected void applyPatch(Node node, Patch<?> patch) throws PatchException
    {
        if (node.getNodeValue() != null) {
            if (node.getNodeType() == Node.TEXT_NODE) {
                applyPatch((Text) node, (Patch<Character>) patch);
            } else if (node.getNodeType() == Node.ATTRIBUTE_NODE) {
                applyPatch((Attr) node, (Patch<Character>) patch);
            }
        } else if (node.getNodeType() == Node.ELEMENT_NODE) {
            applyPatch((Element) node, (Patch<Node>) patch);
        }
    }

    protected void applyPatch(Text textLeft, Patch<Character> patch) throws PatchException
    {
        Text textRight = (Text) getOrCreateRightNode(textLeft);
        textRight.setNodeValue(applyPatch(textLeft.getNodeValue(), patch));

        Element parentLeft = (Element) textLeft.getParentNode();
        if (supportsInlineMarkerElements(parentLeft)) {
            markTextValueChange(textLeft, patch, true);
            markTextValueChange(textRight, patch, false);
        } else {
            markElementModified(parentLeft, true);
            markElementModified((Element) textRight.getParentNode(), false);
        }
    }

    protected boolean supportsInlineMarkerElements(Element parent)
    {
        return true;
    }

    protected void markTextValueChange(Node text, Patch<Character> patch, boolean left)
    {
        Document document = text.getOwnerDocument();
        // We use a wrapper because we don't want to change the node index of the next siblings when the text node is
        // split in multiple parts (in order to show the parts that were deleter or inserted).
        Element wrapper = document.createElement(getInlineMarkerElementName());
        wrapper.setAttribute(TEXT_WRAPPER, "true");
        String textValue = text.getNodeValue();
        int lastIndex = 0;
        for (Delta<Character> delta : patch) {
            Chunk<Character> chunk = left ? delta.getPrevious() : delta.getNext();
            if (chunk.size() > 0) {
                wrapper.appendChild(document.createTextNode(textValue.substring(lastIndex, chunk.getIndex())));
                Element marker = (Element) document.createElement(getInlineMarkerElementName());
                marker.appendChild(document.createTextNode(toString(chunk.getElements())));
                wrapper.appendChild(marker);
                markElementModified(marker, left);
                lastIndex = chunk.getLastIndex() + 1;
            }
        }
        wrapper.appendChild(document.createTextNode(textValue.substring(lastIndex)));
        text.getParentNode().replaceChild(wrapper, text);
    }

    protected abstract String getInlineMarkerElementName();

    protected void applyPatch(Attr attributeLeft, Patch<Character> patch) throws PatchException
    {
        Attr attributeRight = (Attr) getOrCreateRightNode(attributeLeft);
        attributeRight.setValue(applyPatch(attributeLeft.getValue(), patch));
        markElementModified(attributeLeft.getOwnerElement(), true);
        markElementModified(attributeRight.getOwnerElement(), false);
    }

    private String applyPatch(String string, Patch<Character> patch) throws PatchException
    {
        List<Character> chars = string.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        return toString(patch.apply(chars));
    }

    private String toString(List<Character> characters)
    {
        StringBuilder stringBuilder = new StringBuilder();
        characters.forEach(stringBuilder::append);
        return stringBuilder.toString();
    }

    protected void applyPatch(Element parent, Patch<Node> patch)
    {
        List<Delta<Node>> attributesDeltas =
            patch.stream().filter(delta -> delta.getPrevious().getIndex() < 0).collect(Collectors.toList());
        if (!attributesDeltas.isEmpty()) {
            applyAttributesPatch(parent, attributesDeltas);
        }

        List<Delta<Node>> childrenDeltas =
            patch.stream().filter(delta -> delta.getPrevious().getIndex() >= 0).collect(Collectors.toList());
        if (!childrenDeltas.isEmpty()) {
            applyChildrenPatch(parent, childrenDeltas);
        }
    }

    protected void applyAttributesPatch(Element elementLeft, List<Delta<Node>> deltas)
    {
        Element elementRight = (Element) getOrCreateRightNode(elementLeft);
        for (Delta<Node> delta : deltas) {
            for (Node attr : delta.getPrevious().getElements()) {
                elementRight.removeAttribute(attr.getNodeName());
            }
            for (Node attr : delta.getNext().getElements()) {
                elementRight.setAttribute(attr.getNodeName(), attr.getNodeValue());
            }
        }
        markElementModified(elementLeft, true);
        markElementModified(elementRight, false);
    }

    protected void applyChildrenPatch(Element parentLeft, List<Delta<Node>> deltas)
    {
        // We have two cases:
        // * the given parent is inside a change block so we need to duplicate the change block and apply the changes
        // * the given parent is not inside a change block which means the modified child nodes are themselves change
        // blocks and thus we need to add the inserted child nodes and mark both deleted and inserted child nodes
        // accordingly.
        Element parentRight = isInsideDiffBlock(parentLeft) ? (Element) getOrCreateRightNode(parentLeft) : parentLeft;

        ListIterator<Delta<Node>> deltaIterator = deltas.listIterator(deltas.size());
        while (deltaIterator.hasPrevious()) {
            Delta<Node> delta = deltaIterator.previous();
            for (Node node : delta.getPrevious().getElements()) {
                markNodeModified(node, true);
                if (parentRight != parentLeft) {
                    parentRight.removeChild(parentRight.getChildNodes().item(delta.getPrevious().getIndex()));
                }
            }
            int insertIndex = delta.getPrevious().getIndex();
            if (parentRight == parentLeft) {
                insertIndex += delta.getPrevious().size();
            }
            Node referenceChild = null;
            if (insertIndex < parentRight.getChildNodes().getLength()) {
                referenceChild = parentRight.getChildNodes().item(insertIndex);
            }
            for (Node node : delta.getNext().getElements()) {
                Node nodeToInsert = node.cloneNode(true);
                parentRight.getOwnerDocument().adoptNode(nodeToInsert);
                parentRight.insertBefore(nodeToInsert, referenceChild);
                markNodeModified(nodeToInsert, false);
            }
        }
    }

    private boolean isInsideDiffBlock(Element element)
    {
        return isMarkedAsDiffBlock(element) || hasDiffBlockParent(element);
    }

    protected void markNodeModified(Node node, boolean deleted)
    {
        Element element = null;
        if (node.getNodeType() == Node.ELEMENT_NODE) {
            element = (Element) node;
        } else if (node.getNodeType() == Node.TEXT_NODE) {
            Element parent = (Element) node.getParentNode();
            if ((node.getPreviousSibling() == null && node.getNextSibling() == null)
                || !supportsInlineMarkerElements(parent)) {
                element = parent;
            } else {
                element = node.getOwnerDocument().createElement(getInlineMarkerElementName());
                parent.insertBefore(element, node);
                element.appendChild(node);
            }
        }

        if (element != null) {
            markElementModified(element, deleted);
        }
    }

    protected abstract void markElementModified(Element element, boolean deleted);

    protected abstract Node getOrCreateRightNode(Node left);

    private void removeTextWrappers(Node node)
    {
        XPath xpath = XPathFactory.newInstance().newXPath();
        String expression = "//" + getInlineMarkerElementName() + "[@" + TEXT_WRAPPER + "]";
        try {
            XMLDiffUtils.asList((NodeList) xpath.compile(expression).evaluate(node, XPathConstants.NODESET)).stream()
                .forEach(this::replaceWithChildren);
        } catch (XPathExpressionException e) {
            // This shouldn't happen but in case it does the text wrappers shouldn't case any problems (they just make
            // the HTML bigger).
        }
    }

    private void replaceWithChildren(Node parent)
    {
        while (parent.getFirstChild() != null) {
            parent.getParentNode().insertBefore(parent.getFirstChild(), parent);
        }
        parent.getParentNode().removeChild(parent);
    }
}
