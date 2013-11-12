package com.marklogic.mapreduce;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;

import com.marklogic.tree.ExpandedTree;

public class LargeBinaryDocument extends BinaryDocument {
    public static final Log LOG = LogFactory.getLog(
            LargeBinaryDocument.class);
    Path path;
    long offset;
    long size;
    long binaryOrigLen;
    Configuration conf;
    
    public LargeBinaryDocument() {
    }
    
    public LargeBinaryDocument(Configuration conf, Path forestDir, 
            ExpandedTree tree) {
        path = new Path(forestDir, tree.getPathToBinary());
        offset = tree.binaryOffset;
        size = tree.binarySize;
        binaryOrigLen = tree.binaryOrigLen;
        this.conf = conf;
    }
    
    @Override
    public void readFields(DataInput in) throws IOException {
        path = new Path(Text.readString(in));
        offset = in.readLong();
        size = in.readLong();
        binaryOrigLen = in.readLong();
    }

    @Override
    public void write(DataOutput out) throws IOException {
        Text.writeString(out, path.toString());
        out.writeLong(offset);
        out.writeLong(size);
        out.writeLong(binaryOrigLen);
    }
    
    @Override
    public byte[] getContentAsByteArray() {
        if (size > Integer.MAX_VALUE) {
            throw new ArrayIndexOutOfBoundsException("Array size = " + size);
        }
        FileSystem fs;
        try {
            fs = path.getFileSystem(conf);
            if (!fs.exists(path)) {
                throw new RuntimeException("File not found: " + path);
            }
            byte[] buf = new byte[(int) size];
            FSDataInputStream is = fs.open(path);
            for (int bytesRead = 0; bytesRead < size;) {
                bytesRead += is.read(buf, bytesRead, (int) size - bytesRead);
            }
            return buf;
        } catch (IOException e) {
            throw new RuntimeException("Error accessing file: " + path, e);
        }     
    }

    @Override
    public MarkLogicNode getContentAsMarkLogicNode() {
        throw new UnsupportedOperationException(
        "Cannot convert binary data to MarkLogicNode.");
    }

    @Override
    public Text getContentAsText() {
        throw new UnsupportedOperationException(
        "Cannot convert binary data to Text.");
    }

    @Override
    public ContentType getContentType() {
        return ContentType.BINARY;
    }

}