/*
 * $Id$
 *
 * Copyright (C) 2003-2015 JNode.org
 *
 * This library is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library; If not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
 
package org.jnode.fs.exfat;

import android.util.Log;

import java.io.EOFException;
import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.jnode.fs.FSFile;
import org.jnode.fs.spi.AbstractFSObject;

/**
 * @author Matthias Treydte &lt;waldheinz at gmail.com&gt;
 */
public class NodeFile extends AbstractFSObject implements FSFile {

    private final Node node;
    private long lastClusterToSkip = -1;
    private long lastCluster = -1;

    public NodeFile(ExFatFileSystem fs, Node node) {
        super(fs);

        this.node = node;
    }

    @Override
    public long getLength() {
        return this.node.getSize();
    }

    @Override
    public void setLength(long length) throws IOException {
        if (getLength() == length) return;

        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void read(long offset, ByteBuffer dest) throws IOException {
        final int remaining = dest.remaining();
        if (remaining == 0) return;

        if (offset + remaining > getLength()) {
            throw new EOFException();
        }

        final int bytesPerCluster = node.getSuperBlock().getBytesPerCluster();
        long startCluster = node.getStartCluster();
        int remaining2 = dest.remaining();

        // Skip to the cluster that corresponds to the requested offset
        long clustersToSkip = offset / bytesPerCluster;
        if(this.lastClusterToSkip < 0 || this.lastClusterToSkip > clustersToSkip) {
            for (int i = 0; i < clustersToSkip; i++) {
                startCluster = this.node.nextCluster(startCluster);

                if (Cluster.invalid(startCluster)) {
                    throw new IOException("invalid cluster");
                }
            }
        } else {
            startCluster = this.lastCluster;
            while (this.lastClusterToSkip < clustersToSkip) {
                startCluster = this.node.nextCluster(startCluster);
                if (Cluster.invalid(startCluster)) {
                    throw new IOException("invalid cluster");
                }
                this.lastClusterToSkip++;
            }
        }

        try {
            this.lastClusterToSkip = clustersToSkip;
            this.lastCluster = startCluster;
            if(dest.remaining() >= bytesPerCluster) {
                ByteBuffer allocate = ByteBuffer.allocate(bytesPerCluster);
                if(offset % bytesPerCluster != 0) {
                    allocate.rewind();
                    this.node.getSuperBlock().readCluster(allocate, startCluster);
                    int tmpOffset = (int) (offset % bytesPerCluster);
                    int tmpLength = Math.min(remaining2, bytesPerCluster - tmpOffset);
                    int position = dest.position();
                    System.arraycopy(allocate.array(), tmpOffset, dest.array(), position, tmpLength);
                    dest.position(position + tmpLength);
                    remaining2 -= tmpLength;
                    startCluster = node.nextCluster(startCluster);
                    if (remaining2 != 0 && Cluster.invalid(startCluster)) {
                        throw new IOException("invalid cluster");
                    }
                }
                // Read in the remaining data
                while (remaining2 > 0) {
                    int toRead = Math.min(bytesPerCluster, remaining2);
                    int position = dest.position();
                    allocate.rewind();
                    node.getSuperBlock().readCluster(allocate, startCluster);
                    System.arraycopy(allocate.array(), 0, dest.array(), position, toRead);
                    dest.position(position + toRead);
                    remaining2 -= toRead;
                    startCluster = this.node.nextCluster(startCluster);

                    if (remaining2 != 0 && Cluster.invalid(startCluster)) {
                        throw new IOException("invalid cluster");
                    }
                }
                return;
            }
            if (offset % bytesPerCluster != 0) {
                int position = dest.position();
                int tmpOffset = (int) (offset % bytesPerCluster);
                this.node.getSuperBlock().readCluster2(dest, startCluster, tmpOffset);
                startCluster = this.node.nextCluster(startCluster);
                if(bytesPerCluster - tmpOffset < remaining2) {
                    dest.position((position + bytesPerCluster) - tmpOffset);
                }
                if(dest.remaining() != 0 && Cluster.invalid(startCluster)) {
                    throw new IOException("invalid cluster");
                }
            }
            if(dest.remaining() < 0) {
                return;
            }
            this.node.getSuperBlock().readCluster(dest, startCluster);
            long nextCluster = this.node.nextCluster(startCluster);
            if(dest.remaining() != 0 && Cluster.invalid(nextCluster)) {
                throw new IOException("invalid cluster");
            }
        } catch (Exception e) {
            Log.e("NodeFile","NodeFile read error=" + e.toString());
        }


    }

    @Override
    public void write(long offset, ByteBuffer src) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void flush() throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
