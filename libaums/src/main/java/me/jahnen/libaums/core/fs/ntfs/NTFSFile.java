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

package me.jahnen.libaums.core.fs.ntfs;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Iterator;

import me.jahnen.libaums.core.fs.AbstractUsbFile;
import me.jahnen.libaums.core.fs.FSFile;
import me.jahnen.libaums.core.fs.UsbFile;
import me.jahnen.libaums.core.fs.ntfs.attribute.NTFSAttribute;
import me.jahnen.libaums.core.fs.ntfs.index.IndexEntry;
import me.jahnen.libaums.core.fs.ntfs.util.ByteBufferUtils;

/**
 * @author vali
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class NTFSFile extends AbstractUsbFile implements FSFile {
    private FileRecord fileRecord;
    private IndexEntry indexEntry;
    private UsbFile parent;
    private NTFSFileSystem fs;

    public NTFSFile(NTFSFileSystem fs, IndexEntry indexEntry) {
        this.fs = fs;
        this.indexEntry = indexEntry;
    }

    public NTFSFile(NTFSFileSystem fs, FileRecord fileRecord) {
        this.fs = fs;
        this.fileRecord = fileRecord;
    }

    public void setParent(UsbFile parent) {
        this.parent = parent;
    }

    @Override
    public long createdAt() {
        return getFileRecord().getStandardInformationAttribute().getCreationTime();
    }

    @Override
    public long lastModified() {
        return getFileRecord().getStandardInformationAttribute().getModificationTime();
    }

    @Override
    public long lastAccessed() {
        return getFileRecord().getStandardInformationAttribute().getAccessTime();
    }

    public FileRecord getFileRecord() {
        if(this.fileRecord == null) {
            try {
                this.fileRecord = this.indexEntry.getParentFileRecord().getVolume().getMFT().getIndexedFileRecord(this.indexEntry);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return this.fileRecord;
    }

    @Override
    public void close() {

    }

    public void read(long fileOffset, ByteBuffer destBuf) throws IOException {
        // TODO optimize it also to use ByteBuffer at lower level
        final ByteBufferUtils.ByteArray destBA = ByteBufferUtils.toByteArray(destBuf);
        final byte[] dest = destBA.toArray();
        getFileRecord().readData(fileOffset, dest, 0, dest.length);
        destBA.refreshByteBuffer();
    }

    @Override
    public void delete() throws IOException {
        throw new UnsupportedOperationException("delete unsupported");
    }

    @Override
    public boolean isRoot() {
        return false;
    }

    @Override
    public void write(long fileOffset, ByteBuffer src) throws IOException {
        throw new UnsupportedOperationException("write unsupported");
    }

    @Override
    public UsbFile createFile(String name) throws IOException {
        throw new UnsupportedOperationException("createFile unsupported");
    }

    @Override
    public void moveTo(UsbFile destination) throws IOException {

    }

    @Override
    public UsbFile createDirectory(String name) throws IOException {
        throw new UnsupportedOperationException("createDirectory unsupported");
    }

    @Override
    public void flush() throws IOException {
        throw new UnsupportedOperationException("unsupported operation");
    }

    @Override
    public long getLength() {
        Iterator<NTFSAttribute> attributes =
                getFileRecord().findAttributesByTypeAndName(NTFSAttribute.Types.DATA, null);

        if (!attributes.hasNext() && indexEntry != null) {
            // Fall back to the size stored in the index entry if the data attribute is not present (even possible??)
            FileNameAttribute.Structure fileName = new FileNameAttribute.Structure(
                    indexEntry, IndexEntry.CONTENT_OFFSET);
            return fileName.getRealSize();
        }

        return getFileRecord().getAttributeTotalSize(NTFSAttribute.Types.DATA, null);
    }

    @Override
    public void setLength(long newLength) throws UnsupportedOperationException {
        throw new UnsupportedOperationException("setLength currently not supported");
    }

    @Override
    public boolean isDirectory() {
        return false;
    }

    @Override
    public String getName() {
        return getFileRecord().getFileName();
    }

    @Override
    public UsbFile getParent() {
        return this.parent;
    }

    @Override
    public String[] list() throws IOException {
        throw new UnsupportedOperationException("list unsupported");
    }

    @Override
    public UsbFile[] listFiles() throws IOException {
        throw new UnsupportedOperationException("listFiles unsupported");
    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException("setName unsupported");
    }
}
