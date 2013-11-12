package com.marklogic.mapreduce;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import com.marklogic.tree.ExpandedTree;
import com.marklogic.tree.NodeKind;

public abstract class UnpackedDocument implements MarkLogicDocument {
    public static final Log LOG = LogFactory.getLog(
            UnpackedDocument.class);
    
    public static UnpackedDocument createDocument(Configuration conf,
            Path forestDir, ExpandedTree tree, String uri) {
        byte rootNodeKind = tree.rootNodeKind();
        switch (rootNodeKind) {
            case NodeKind.BINARY:
                if (tree.binaryData == null) {
                    return new LargeBinaryDocument(conf, forestDir, tree);
                } else {
                    return new RegularBinaryDocument(tree);
                }
            case NodeKind.ELEM:
            case NodeKind.TEXT:
                return new DOMDocument(tree);     
            default:
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Skipping unsupported node kind "
                            + rootNodeKind + " (" + uri + ")");
                }
                return null;
        }
    }
}