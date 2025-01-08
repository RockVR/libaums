package me.jahnen.libaums.core.fs.ntfs;

import java.io.IOException;

import me.jahnen.libaums.core.fs.ntfs.attribute.NTFSAttribute;
import me.jahnen.libaums.core.fs.ntfs.index.IndexEntry;

public class MasterFileTable extends FileRecord {
    /**
     * MFT indexes of system files
     */
    public static class SystemFiles {
        public static final int MFT = 0;
        public static final int MFTMIRR = 1;
        public static final int LOGFILE = 2;
        public static final int VOLUME = 3;
        public static final int ATTRDEF = 4;
        public static final int ROOT = 5;
        public static final int BITMAP = 6;
        public static final int BOOT = 7;
        public static final int BADCLUS = 8;
        public static final int SECURE = 9;
        public static final int UPCASE = 10;
        public static final int EXTEND = 11;
        public static final int RESERVED12 = 12;
        public static final int RESERVED13 = 13;
        public static final int RESERVED14 = 14;
        public static final int RESERVED15 = 15;
        public static final int FIRST_USER = 16;
    }

    private long mftLength;

    public MasterFileTable(NTFSVolume volume, byte[] buffer, int offset) throws IOException {
        super(volume, SystemFiles.MFT, buffer, offset);
    }

    public long getMftLength() {
        if (mftLength == 0) {
            // The MFT doesn't update the FileRecord file-size for itself, so fall back to check the size of the DATA
            // attribute.
            mftLength = getAttributeTotalSize(NTFSAttribute.Types.DATA, null);
        }

        return mftLength;
    }

    public byte[] readRecord(long index) throws IOException {
        final NTFSVolume volume = getVolume();
        final int bytesPerFileRecord = volume.getBootRecord().getFileRecordSize();
        final long offset = bytesPerFileRecord * index;

        // read the buffer
        final byte[] buffer = new byte[bytesPerFileRecord];
        readData(offset, buffer, 0, bytesPerFileRecord);
        return buffer;
    }

    public FileRecord getRecordUnchecked(long index) throws IOException {
        final NTFSVolume volume = getVolume();

        // read the buffer
        final byte[] buffer = readRecord(index);
        return new FileRecord(volume, index, buffer, 0);
    }

    public FileRecord getRecord(long index) throws IOException {
        final NTFSVolume volume = getVolume();
        final int bytesPerFileRecord = volume.getBootRecord().getFileRecordSize();
        final long offset = bytesPerFileRecord * index;

        if (offset + bytesPerFileRecord > getMftLength()) {
            throw new IOException("Attempt to read past the end of the MFT, offset: " + offset);
        }

        FileRecord fileRecord = getRecordUnchecked(index);
        fileRecord.checkIfValid();
        return fileRecord;
    }

    public FileRecord getIndexedFileRecord(IndexEntry indexEntry) throws IOException {
        return getRecord(indexEntry.getFileReferenceNumber());
    }
}
