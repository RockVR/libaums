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

import me.jahnen.libaums.core.fs.ntfs.index.IndexEntry;

/**
 * @author vali
 * @author Ewout Prangsma (epr@users.sourceforge.net)
 */
public class NTFSEntry implements FSEntry {
    private FSObject cachedFSObject;
    private IndexEntry indexEntry;
    private FileRecord fileRecord;
    private final NTFSFileSystem fs;

    public NTFSEntry(NTFSFileSystem fs, IndexEntry indexEntry) {
        this.fs = fs;
        this.indexEntry = indexEntry;
    }

    public String getName() {
        if (indexEntry != null) {
            FileNameAttribute.Structure fileName = new FileNameAttribute.Structure(
                indexEntry, IndexEntry.CONTENT_OFFSET);
            return fileName.getFileName();
        } else if (fileRecord != null) {
            return fileRecord.getFileName();
        }
        return null;
    }

    public FSDirectory getParent() {
        // TODO Auto-generated method stub
        return null;
    }

    public long getCreated() throws IOException {
        if (getFileRecord().getStandardInformationAttribute() == null) {
            return 0;
        } else {
            return NTFSUTIL.filetimeToMillis(getFileRecord().getStandardInformationAttribute().getCreationTime());
        }
    }

    public long getLastModified() throws IOException {
        if (getFileRecord().getStandardInformationAttribute() == null) {
            return 0;
        } else {
            return NTFSUTIL.filetimeToMillis(getFileRecord().getStandardInformationAttribute().getModificationTime());
        }
    }

    public long getLastChanged() throws IOException {
        if (getFileRecord().getStandardInformationAttribute() == null) {
            return 0;
        } else {
            return NTFSUTIL.filetimeToMillis(getFileRecord().getStandardInformationAttribute().getMftChangeTime());
        }
    }

    public long getLastAccessed() throws IOException {
        if (getFileRecord().getStandardInformationAttribute() == null) {
            return 0;
        } else {
            return NTFSUTIL.filetimeToMillis(getFileRecord().getStandardInformationAttribute().getAccessTime());
        }
    }

    public boolean isFile() {
        if (indexEntry != null) {
            FileNameAttribute.Structure fileName = new FileNameAttribute.Structure(
                indexEntry, IndexEntry.CONTENT_OFFSET);
            return !fileName.isDirectory();
        } else {
            return !fileRecord.isDirectory();
        }
    }

    public boolean isDirectory() {
        if (indexEntry != null) {
            FileNameAttribute.Structure fileName = new FileNameAttribute.Structure(
                indexEntry, IndexEntry.CONTENT_OFFSET);
            return fileName.isDirectory();
        } else {
            return fileRecord.isDirectory();
        }
    }

    public void setName(String newName) {
        // TODO Auto-generated method stub

    }

    public void setCreated(long created) {
        // TODO: Implement write support.
    }

    public void setLastModified(long lastModified) {
        // TODO: Implement write support.
    }

    public void setLastAccessed(long lastAccessed) {
        // TODO: Implement write support.
    }

    public FSFile getFile() {
        if (this.isFile()) {
            if (cachedFSObject == null) {
                if (indexEntry != null) {
                    cachedFSObject = new NTFSFile(fs, indexEntry);
                } else {
                    cachedFSObject = new NTFSFile(fs, fileRecord);
                }
            }
            return (FSFile) cachedFSObject;
        } else {
            return null;
        }
    }

    public FSDirectory getDirectory() throws IOException {
        if (this.isDirectory()) {
            if (cachedFSObject == null) {
                if (fileRecord != null) {
                    cachedFSObject = new NTFSDirectory(fs, fileRecord);
                } else {
                    // XXX: Why can't this just use getFileRecord()?
                    cachedFSObject = new NTFSDirectory(fs, getFileRecord().getVolume().getMFT().getIndexedFileRecord(
                        indexEntry));
                }
            }
            return (FSDirectory) cachedFSObject;
        } else return null;
    }

    public boolean isValid() {
        // TODO Auto-generated method stub
        return true;
    }

    public FileRecord getFileRecord() throws IOException {
        if (fileRecord != null) {
            return fileRecord;
        }
        return indexEntry.getParentFileRecord().getVolume().getMFT().getIndexedFileRecord(indexEntry);
    }

    public IndexEntry getIndexEntry() {
        return indexEntry;
    }

    public boolean isDirty() throws IOException {
        return true;
    }

    @Override
    public String toString() {
        Object obj = indexEntry == null ? fileRecord : indexEntry;
        return super.toString() + '(' + obj + ')';
    }

}
