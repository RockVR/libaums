package me.jahnen.libaums.core.fs.ntfs;

import java.io.IOException;
import java.nio.ByteBuffer;

import me.jahnen.libaums.core.driver.BlockDeviceDriver;

public class NTFSVolume {
    private final BlockDeviceDriver blockDevice;
    private int clusterSize;
    private final BootRecord bootRecord;
    private MasterFileTable mftFileRecord;
    private FileRecord rootDirectory;

    public NTFSVolume(BlockDeviceDriver blockDevice, ByteBuffer byteBuffer) throws IOException, UnsupportedOperationException {
        if(byteBuffer == null) {
            byteBuffer = ByteBuffer.allocate(512);
            blockDevice.read(0L, byteBuffer);
        }
        if(checkFs(byteBuffer.array())) {
            this.blockDevice = blockDevice;
            BootRecord bootRecord = new BootRecord(byteBuffer.array());
            this.bootRecord = bootRecord;
            int size = bootRecord.getClusterSize();
            this.clusterSize = size;
            if(size > 0) {
                return;
            }
            this.clusterSize = 4096;
            return;
        }
        throw new UnsupportedOperationException("unsupported partition type");

    }

    public static boolean checkFs(byte[] bArr) {
        return new String(bArr, 3, 8).equals("NTFS    ");
    }

    public final BootRecord getBootRecord() {
        return this.bootRecord;
    }

    public int getClusterSize() {
        if(this.clusterSize == 0) {
            this.clusterSize = 4096;
        }
        return this.clusterSize;
    }

    public MasterFileTable getMFT() throws IOException {
        if (mftFileRecord == null) {
            final BootRecord bootRecord = getBootRecord();
            final int bytesPerFileRecord = bootRecord.getFileRecordSize();
            final int clusterSize = getClusterSize();

            final int nrClusters;
            if (bytesPerFileRecord < clusterSize) {
                nrClusters = 1;
            } else {
                nrClusters = (bytesPerFileRecord + clusterSize - 1) / clusterSize;
            }
            final byte[] data = new byte[nrClusters * clusterSize];
            readClusters(bootRecord.getMftLcn(), data, 0, nrClusters);
            mftFileRecord = new MasterFileTable(this, data, 0);
            mftFileRecord.checkIfValid();
        }
        return mftFileRecord;
    }

    public void readClusters(long firstCluster, byte[] dst, int dstOffset, int nrClusters) throws IOException{
        final int clusterSize = getClusterSize();
        final long clusterOffset = firstCluster * clusterSize;
        this.blockDevice.read(clusterOffset, ByteBuffer.wrap(dst, dstOffset, nrClusters * clusterSize));
    }

    public FileRecord getRootDirectory() throws IOException {
        if(this.rootDirectory == null) {
            final MasterFileTable mft = getMFT();
            rootDirectory = mft.getRecord(MasterFileTable.SystemFiles.ROOT);
        }
        return rootDirectory;
    }
}
