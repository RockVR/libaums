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

import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import me.jahnen.libaums.core.fs.AbstractUsbFile;
import me.jahnen.libaums.core.fs.FSDirectory;
import me.jahnen.libaums.core.fs.FSEntry;
import me.jahnen.libaums.core.fs.FSFile;
import me.jahnen.libaums.core.fs.UsbFile;
import me.jahnen.libaums.core.fs.ntfs.index.NTFSIndex;

/**
 * A NTFS directory.
 *
 * @author vali
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 * @author Luke Quinane
 */
public class NTFSDirectory extends AbstractUsbFile implements FSDirectory {
    private static final Logger log = Logger.getLogger(NTFSDirectory.class);
    private final NTFSIndex index;
    private final FileRecord fileRecord;
    private UsbFile parent;
    private final NTFSFileSystem fs;
    private final String id;

    public NTFSDirectory(NTFSFileSystem fs, FileRecord record) throws IOException {
        this.fs = fs;
        this.fileRecord = record;
        this.index = new NTFSIndex(record, "$I30");
        id = Long.toString(record.getReferenceNumber());
    }

    public void setParent(UsbFile parent) {
        this.parent = parent;
    }

    private List<UsbFile> scanFiles() throws IOException {
        Iterator<FSEntry> entries = iterator();
        List<UsbFile> files = new ArrayList<>();

        while(entries.hasNext()) {
            NTFSEntry n = (NTFSEntry)entries.next();
            String name = n.getName();
            if(name != null && (name.startsWith("$") || name.equals(".") || name.equals("..")))
                continue;

            if(n.isDirectory()) {
                FSDirectory dir = n.getDirectory();
                if(dir instanceof NTFSDirectory) {
                    ((NTFSDirectory) dir).setParent(this);
                }
                files.add((UsbFile) dir);
            } else if(n.isFile()) {
                FSFile file = n.getFile();
                if(file instanceof NTFSFile) {
                    ((NTFSFile) file).setParent(this);
                }
                files.add((UsbFile) file);
            }
        }

        return files;
    }

    @Override
    public FSEntry getEntry(String name ){
        Iterator<FSEntry> entries = iterator();
        while(entries.hasNext()) {
            NTFSEntry ntfsEntry = (NTFSEntry) entries.next();
            if(ntfsEntry.getName().equals(name)) {
                return ntfsEntry;
            }
        }
        return null;
    }

    @Override
    public long lastModified() {
        return this.fileRecord.getStandardInformationAttribute().getModificationTime();
    }

    @Override
    public long lastAccessed() {
        return this.fileRecord.getStandardInformationAttribute().getAccessTime();
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

    @Override
    public String[] list() throws IOException {
        List<UsbFile> files = scanFiles();
        String[] strArr = new String[files.size()];
        for(int i = 0; i < files.size(); i++) {
            strArr[i] = files.get(i).getName();
        }
        return strArr;
    }

    @Override
    public UsbFile[] listFiles() throws IOException {
        List<UsbFile> files = scanFiles();
        return (UsbFile[]) files.toArray(new UsbFile[0]);
    }

    @Override
    public void moveTo(UsbFile file) throws IOException {
        throw new UnsupportedOperationException("moveTo unsupported");
    }

    @Override
    public void delete() throws IOException {
        throw new UnsupportedOperationException("delete unsupported");
    }

    @Override
    public boolean isRoot() {
        return parent == null;
    }

    @Override
    public long createdAt() {
        return this.fileRecord.getStandardInformationAttribute().getCreationTime();
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public UsbFile createDirectory(String name) throws IOException {
        throw new UnsupportedOperationException("createFile unsupported");
    }

    @Override
    public void write(long offset, ByteBuffer src) throws IOException  {
        throw new UnsupportedOperationException("write unsupported");
    }

    @Override
    public void read(long offset, ByteBuffer dst) throws IOException {
        throw new UnsupportedOperationException("read unsupported");
    }

    @Override
    public UsbFile createFile(String name) throws IOException {
        throw new UnsupportedOperationException("createFile unsupported");
    }

    @Override
    public void flush() throws IOException {
    }

    public Iterator<FSEntry> iterator() {
        return new DirectoryEntryIterator(fs, index);
    }

    @Override
    public long getLength() {
        return 0L;
    }

    @Override
    public String getName() {
        if(this.parent == null) {
            return this.fs.getVolumeLabel();
        }
        return this.fileRecord.getFileName();
    }

    @Override
    public UsbFile getParent() {
        return this.parent;
    }

    @Override
    public void setName(String name) {
        throw new UnsupportedOperationException("setName unsupported");
    }

    @Override
    public void setLength(long value) {
        throw new UnsupportedOperationException("setLength unsupported");
    }
}
