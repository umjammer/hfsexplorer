/*-
 * Copyright (C) 2006-2009 Erik Larsson
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.catacombae.hfs;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.LinkedList;
import java.util.List;

import org.catacombae.hfs.types.hfscommon.CommonBTHeaderNode;
import org.catacombae.hfs.types.hfscommon.CommonBTHeaderRecord;
import org.catacombae.hfs.types.hfscommon.CommonBTIndexRecord;
import org.catacombae.hfs.types.hfscommon.CommonBTKey;
import org.catacombae.hfs.types.hfscommon.CommonBTKeyedNode;
import org.catacombae.hfs.types.hfscommon.CommonBTKeyedRecord;
import org.catacombae.hfs.types.hfscommon.CommonBTLeafRecord;
import org.catacombae.hfs.types.hfscommon.CommonBTNode;
import org.catacombae.hfs.types.hfscommon.CommonBTNodeDescriptor;
import org.catacombae.hfs.types.hfscommon.CommonBTNodeDescriptor.NodeType;
import org.catacombae.hfs.types.hfscommon.CommonHFSVolumeHeader;
import org.catacombae.io.Readable;
import org.catacombae.io.ReadableRandomAccessStream;
import org.catacombae.io.RuntimeIOException;

import static java.lang.System.getLogger;


/**
 * @author <a href="https://catacombae.org" target="_top">Erik Larsson</a>
 */
public abstract class BTreeFile<K extends CommonBTKey<K>, L extends CommonBTLeafRecord<K>> implements Limits {

    private static final Logger logger = getLogger(BTreeFile.class.getName());

    final HFSVolume vol;

    BTreeFile(HFSVolume vol) {
        this.vol = vol;
    }

    abstract class BTreeFileSession {

        final CommonHFSVolumeHeader header;
        final CommonBTNodeDescriptor btnd;
        final CommonBTHeaderRecord bthr;
        final ReadableRandomAccessStream btreeStream;

        public BTreeFileSession() {
            this.header = vol.getVolumeHeader();
//            header.print(System.err, "    ");
            this.btreeStream = getBTreeStream(header);

            this.btreeStream.seek(0);

            this.btnd = readNodeDescriptor(this.btreeStream);
//            this.btnd.print(System.err, "    ");
            if (btnd.getNodeType() != NodeType.HEADER) {
                throw new RuntimeIOException("Invalid node type for header node.");
            }

            this.bthr = readHeaderRecord(this.btreeStream);
//            this.bthr.print(System.err, "    ");
        }

        public final void close() {
            this.btreeStream.close();
        }

        protected abstract ReadableRandomAccessStream getBTreeStream(CommonHFSVolumeHeader header);
    }

    protected <R extends CommonBTKeyedRecord<K>> R findLEKey(CommonBTKeyedNode<R> indexNode, K searchKey) {
        //
        // Algorithm:
        //   input: Key searchKey
        //   variables: Key greatestMatchingKey
        //     For each n : records
        //     If n.key <= searchKey && n.key > greatestMatchingKey
        //       greatestMatchingKey = n.key
        //
        R largestMatchingRecord = null;

//        logger.log(Level.DEBUG, "findLEKey(): Entering loop...");
        for (R record : indexNode.getBTKeyedRecords()) {
            K recordKey = record.getKey();

//            logger.log(Level.DEBUG, "findLEKey():   Processing record");
//            if (recordKey instanceof CommonHFSExtentKey)
//                logger.log(Level.DEBUG, " with key " + getDebugString((CommonHFSExtentKey) recordKey));
//            logger.log(Level.DEBUG, "...");

            if (recordKey.compareTo(searchKey) <= 0 &&
                    (largestMatchingRecord == null ||
                            recordKey.compareTo(largestMatchingRecord.getKey()) > 0)) {

                largestMatchingRecord = record;
//                logger.log(Level.DEBUG, "match!");
            }
//            else
//                logger.log(Level.DEBUG, "no match.");
//            logger.log(Level.DEBUG, );
        }

//        logger.log(Level.DEBUG, "findLEKey(): Returning...");
        return largestMatchingRecord;
    }

    /**
     * Find records with keys <code>k</code> in the range
     * <code>minKeyInclusive</code> &lt;= <code>k</code> &lt;
     * <code>maxKeyExclusive</code> that exist in <code>keyedNode</code>.<br>
     * <p>
     * If no matching records are found, then the record with the largest key
     * that is less than <code>minKeyInclusive</code> (if any such record
     * exists) is returned in <code>result</code>. If no such record exists,
     * nothing is added to <code>result</code>.
     *
     * @param <R>             The type of the records that we operate on.
     * @param keyedNode       <b>(in)</b> The keyed node to search.
     * @param minKeyInclusive <b>(in)</b> The smallest key in the range (inclusive).
     * @param maxKeyExclusive <b>(in)</b> The largest key in the range (exclusive).
     * @param strict          <b>(in)</b> If <code>false</code>, then the record before the first
     *                        match is always included in the result. This is appropriate when
     *                        searching index nodes, but not for leaf nodes.
     * @return A {@link java.util.List} of records.
     */
    protected <R extends CommonBTKeyedRecord<K>> List<R> findLEKeys(
            CommonBTKeyedNode<R> keyedNode, K minKeyInclusive, K maxKeyExclusive, boolean strict) {
        LinkedList<R> result = new LinkedList<>();

        findLEKeys(keyedNode, minKeyInclusive, maxKeyExclusive, strict, result);

        return result;
    }

    /**
     * Find records with keys <code>k</code> in the range
     * <code>minKeyInclusive</code> &lt;= <code>k</code> &lt;
     * <code>maxKeyExclusive</code>) that exist in <code>keyedNode</code>.<br>
     * <p>
     * If no matching records are found, then the record with the largest key
     * that is less than <code>minKeyInclusive</code> (if any such record
     * exists) is returned in <code>result</code> and the function returns
     * <code>false</code>. If no such record exists, nothing is added to
     * <code>result</code> (and <code>false</code> is still returned).
     *
     * @param <R>             The type of the records that we operate on.
     * @param keyedNode       <b>(in)</b> The keyed node to search.
     * @param minKeyInclusive <b>(in)</b> The smallest key in the range (inclusive).
     * @param maxKeyExclusive <b>(in)</b> The largest key in the range (exclusive).
     * @param strict          <b>(in)</b> If <code>false</code>, then the record before the first
     *                        match is always included in the result. This is appropriate when
     *                        searching index nodes, but not for leaf nodes.
     * @param result          <b>(out)</b> A {@link java.util.LinkedList} that will receive the
     *                        matching keys.
     * @return <code>true</code> if at least one key matching the specified
     * conditions was found, and <code>false</code> otherwise.
     */
    protected <R extends CommonBTKeyedRecord<K>> boolean findLEKeys(
            CommonBTKeyedNode<R> keyedNode, K minKeyInclusive,
            K maxKeyExclusive, boolean strict, LinkedList<R> result) {
        boolean found = false;
        K largestLEKey = null;
        R largestLERecord = null;

        // TODO Iteration could be optimized to binary search since keys are
        //  (supposed to be) ordered.
        for (R record : keyedNode.getBTKeyedRecords()) {
            K key = record.getKey();

            if (key.compareTo(minKeyInclusive) < 0) {
                if (largestLEKey == null ||
                        key.compareTo(largestLEKey) > 0) {
                    largestLEKey = key;
                    largestLERecord = record;
                }
            } else if (key.compareTo(maxKeyExclusive) < 0) {
                if (result != null) {
                    result.addLast(record);
                }

                found = true;
            }
        }

        if (largestLEKey != null && (!found || !strict)) {
            if (result != null) {
                result.addFirst(largestLERecord);
            }
        }

        return found;
    }

    protected CommonBTHeaderNode createCommonBTHeaderNode(byte[] currentNodeData, int offset, int nodeSize) {
        return vol.createCommonBTHeaderNode(currentNodeData, offset, nodeSize);
    }

    protected abstract CommonBTKeyedNode<? extends CommonBTIndexRecord<K>>
    createIndexNode(byte[] nodeData, int offset, int nodeSize);

    protected abstract CommonBTKeyedNode<L> createLeafNode(byte[] nodeData, int offset, int nodeSize);

    protected CommonBTNodeDescriptor readNodeDescriptor(Readable rd) {
        return vol.readNodeDescriptor(rd);
    }

    protected CommonBTHeaderRecord readHeaderRecord(Readable rd) {
        return vol.readHeaderRecord(rd);
    }

    protected CommonBTNodeDescriptor createCommonBTNodeDescriptor(
            byte[] currentNodeData, int offset) {
        return vol.createCommonBTNodeDescriptor(currentNodeData, offset);
    }

    public HFSVolume getVolume() {
        return vol;
    }

    abstract BTreeFileSession openSession();

    /**
     * Returns the root node of the B-tree file. If it does not exist
     * <code>null</code> is returned. The B-tree file will have no meaningful
     * content if there is no root node.
     *
     * @return the B-tree root node of the B-tree file.
     */
    public CommonBTNode<?> getRootNode() {
        BTreeFileSession ses = openSession();

        try {
            long rootNode = ses.bthr.getRootNodeNumber();

            if (rootNode == 0) {
                // There is no index node, or other content. So the node we
                // seek does not exist. Return null.
                return null;
            } else if (rootNode < 0 || rootNode > UINT32_MAX) {
                throw new RuntimeException("Internal error - rootNode out of range: " + rootNode);
            } else {
                return getNode(rootNode, ses);
            }
        } finally {
            ses.close();
        }
    }

    public long getRootNodeNumber() {
        BTreeFileSession ses = openSession();

        try {
            long rootNodeNumber = ses.bthr.getRootNodeNumber();
            return rootNodeNumber;
        } finally {
            ses.close();
        }
    }

    CommonBTNode<?> getNode(long nodeNumber, BTreeFileSession ses) {
        final String METHOD = "getNode";
        int nodeSize = ses.bthr.getNodeSize();

        CommonBTNode<?> node;
        byte[] nodeData = new byte[nodeSize];
        try {
            ses.btreeStream.seek(nodeNumber * nodeSize);
            ses.btreeStream.readFully(nodeData);
        } catch (RuntimeException e) {
            logger.log(Level.DEBUG, "RuntimeException in " + METHOD + ". Printing additional information:");
            logger.log(Level.DEBUG, "  nodeNumber=" + nodeNumber);
            logger.log(Level.DEBUG, "  nodeSize=" + nodeSize);
            logger.log(Level.DEBUG, "  init.btreeStream.length()=" + ses.btreeStream.length());
            logger.log(Level.DEBUG, "  (currentNodeNumber * nodeSize)=" + (nodeNumber * nodeSize));
            throw e;
        }

        CommonBTNodeDescriptor nodeDescriptor = createCommonBTNodeDescriptor(nodeData, 0);
        node = switch (nodeDescriptor.getNodeType()) {
            case HEADER -> createCommonBTHeaderNode(nodeData, 0, nodeSize);
            case INDEX -> createIndexNode(nodeData, 0, nodeSize);
            case LEAF -> createLeafNode(nodeData, 0, nodeSize);
            default -> null;
        };

        return node;
    }

    /**
     * Returns the requested node in the B-tree file. If the requested node is
     * not a header, index or leaf node, <code>null</code> is returned because
     * they are the only ones that are implemented at the moment.<br>
     *
     * @param nodeNumber the node number of the requested node.
     * @return the requested node if it exists and has type header, index node
     * or leaf node, or <code>null</code> otherwise.
     */
    public CommonBTNode<?> getNode(long nodeNumber) {
        BTreeFileSession ses = openSession();
        try {
            return getNode(nodeNumber, ses);
        } finally {
            ses.close();
        }
    }

    /**
     * Get a record from the B* tree with the specified key.<br>
     * <p>
     * If none is found, the method returns <code>null</code>.<br>
     * Tis method should execute in <code>O(log n)</code> time, where
     * <code>n</code> is the number of elements in the tree.
     *
     * @param searchKey the key of the record that we are looking for.
     * @return the requested record, if any, or <code>null</code> if no such
     * record was found.
     */
    public L getRecord(K searchKey) {
        BTreeFileSession ses = openSession();

        try {
            int nodeSize = ses.bthr.getNodeSize();

            long currentNodeOffset = ses.bthr.getRootNodeNumber() * nodeSize;

            byte[] currentNodeData = new byte[nodeSize];
            ses.btreeStream.seek(currentNodeOffset);
            ses.btreeStream.readFully(currentNodeData);
            CommonBTNodeDescriptor nodeDescriptor = createCommonBTNodeDescriptor(currentNodeData, 0);

            // Search down through the layers of indices (O(log n) steps, where
            // n is the size of the tree)
            while (nodeDescriptor.getNodeType() == NodeType.INDEX) {
                CommonBTKeyedNode<? extends CommonBTIndexRecord<K>> currentNode =
                        createIndexNode(currentNodeData, 0, nodeSize);
                CommonBTIndexRecord<K> matchingRecord = findLEKey(currentNode, searchKey);

                if (matchingRecord == null) {
                    return null;
                }

                currentNodeOffset = matchingRecord.getIndex() * nodeSize;
                ses.btreeStream.seek(currentNodeOffset);
                ses.btreeStream.readFully(currentNodeData);
                nodeDescriptor = createCommonBTNodeDescriptor(currentNodeData, 0);
            }

            // Leaf node reached. Find record.
            if (nodeDescriptor.getNodeType() == NodeType.LEAF) {
                CommonBTKeyedNode<L> leaf = createLeafNode(currentNodeData, 0, nodeSize);

                for (L rec : leaf.getBTRecords()) {
                    if (rec.getKey().compareTo(searchKey) == 0) {
                        return rec;
                    }
                }

                return null;
            } else {
                throw new RuntimeException("Expected leaf node. Found other kind: " + nodeDescriptor.getNodeType());
            }
        } finally {
            ses.close();
        }
    }
}
