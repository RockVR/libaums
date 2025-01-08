package me.jahnen.libaums.core.fs.ntfs;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import me.jahnen.libaums.core.driver.BlockDeviceDriver;
import me.jahnen.libaums.core.fs.FileSystem;
import me.jahnen.libaums.core.fs.UsbFile;
import me.jahnen.libaums.core.fs.ntfs.attribute.NTFSAttribute;
import me.jahnen.libaums.core.fs.ntfs.attribute.NTFSResidentAttribute;
import me.jahnen.libaums.core.partition.PartitionTypes;

public class NTFSFileSystem implements FileSystem {
    private NTFSVolume volume;
    private UsbFile rootDirectory;
    private FSEntry rootEntry;

    public NTFSFileSystem(BlockDeviceDriver blockDevice, ByteBuffer byteBuffer) throws IOException, UnsupportedOperationException {
        this.volume = new NTFSVolume(blockDevice, byteBuffer);
        if(this.rootEntry == null) {
            FSEntry root = new NTFSDirectory(this, this.volume.getRootDirectory()).getEntry(".");
            this.rootEntry = root;
            this.rootDirectory = (UsbFile)root.getDirectory();
        }
    }

    public static FileSystem read(BlockDeviceDriver blockDevice) throws IOException, UnsupportedOperationException {
        ByteBuffer buffer = ByteBuffer.allocate(512);
        blockDevice.read(0, buffer);
        buffer.flip();

        if(NTFSVolume.checkFs(buffer.array())) {
            return new NTFSFileSystem(blockDevice, buffer);
        }
        return null;
    }

    @Override
    public UsbFile getRootDirectory() {
        return this.rootDirectory;
    }

    @Override
    public String getVolumeLabel() {
        NTFSEntry n = null;
        try {
            n = (NTFSEntry) getRootEntry().getDirectory().getEntry("$Volume");
        } catch(Exception e) {
            e.printStackTrace();
        }
        if(n == null) {
            return "UsbStorage";
        }
        NTFSAttribute na = null;
        try {
            na = n.getFileRecord().findAttributeByType(NTFSAttribute.Types.VOLUME_NAME);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(na instanceof NTFSResidentAttribute) {
            NTFSResidentAttribute nra = (NTFSResidentAttribute) na;
            int length = nra.getAttributeLength();
            byte[] arr = new byte[length];
            if(length > 0) {
                nra.getData(nra.getAttributeOffset(), arr, 0, length);
                try {
                    return new String(arr, "UTF-16LE");
                } catch(UnsupportedEncodingException e) {
                    throw new IllegalStateException("UTF-16LE charset missing from JRE", e);
                }
            }
        }

        return "UsbStorage";
    }

    @Override
    public long getCapacity() {
        FileRecord bitmapRecord = null;
        try {
            bitmapRecord = volume.getMFT().getRecord(MasterFileTable.SystemFiles.BITMAP);
            long bitmapSize = bitmapRecord.getFileNameAttribute().getRealSize();
            return bitmapSize * 8 * volume.getClusterSize();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public long getFreeSpace() {
        try {
            FileRecord bitmapRecord = volume.getMFT().getRecord(MasterFileTable.SystemFiles.BITMAP);
            int bitmapSize = (int) bitmapRecord.getAttributeTotalSize(NTFSAttribute.Types.DATA, null);
            byte[] buffer = new byte[bitmapSize];
            bitmapRecord.readData(0, buffer, 0, buffer.length);
            int usedBlocks = 0;
            for (byte b : buffer) {
                for (int i = 0; i < 8; i++) {
                    if ((b & 0x1) != 0) {
                        usedBlocks++;
                    }
                    b >>= 1;
                }
            }

            long usedSpace = (long) usedBlocks * volume.getClusterSize();

            return getCapacity() - usedSpace;
        } catch(IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public long getOccupiedSpace() {
        return getCapacity() - getFreeSpace();
    }

    public FSEntry getRootEntry() {
        return this.rootEntry;
    }

    @Override
    public int getChunkSize() {
        return volume.getClusterSize();
    }

    @Override
    public int getType() {
        return PartitionTypes.NTFS_EXFAT;
    }
}
