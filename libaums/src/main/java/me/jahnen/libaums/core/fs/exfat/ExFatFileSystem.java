package me.jahnen.libaums.core.fs.exfat;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.nio.ByteBuffer;

import me.jahnen.libaums.core.driver.BlockDeviceDriver;
import me.jahnen.libaums.core.fs.FileSystem;
import me.jahnen.libaums.core.fs.FileSystemException;
import me.jahnen.libaums.core.fs.UsbFile;
import me.jahnen.libaums.core.partition.PartitionTypes;

public final class ExFatFileSystem implements FileSystem {
    private final ExFatSuperBlock sb;
    private final Node rootNode;
    private final BlockDeviceDriver device;
    private UsbFile rootDirectory;
    private final UpcaseTable upcase;
    private final String label;
    private final ClusterBitMap bitmap;

    public ExFatFileSystem(BlockDeviceDriver device, boolean readOnly, ExFatFileSystemType type) throws FileSystemException {
        if (device == null)
            throw new FileSystemException("Device cannot be null");

        this.device = device;

        try {
            sb = ExFatSuperBlock.read(this);
            rootNode = Node.createRoot(sb);
            final RootDirVisitor rootDirVis = new RootDirVisitor(sb);
            DirectoryParser.create(rootNode).parse(rootDirVis);

            if (rootDirVis.bitmap == null) {
                throw new FileSystemException("cluster bitmap not found");
            }

            if (rootDirVis.upcase == null) {
                throw new FileSystemException("upcase table not found");
            }

            this.upcase = rootDirVis.upcase;
            this.bitmap = rootDirVis.bitmap;
            this.label = rootDirVis.label;

        } catch (Exception e) {
            throw new FileSystemException(e);
        }
    }

    @Override
    public long getCapacity() {
        return -1;
    }

    @Override
    public long getFreeSpace() {
        return -1;
    }

    @Override
    public String getVolumeLabel() {
        return label;
    }

    public UpcaseTable getUpcase() {
        return upcase;
    }

    /**
     * Gets the super block.
     *
     * @return the super block.
     */
    public ExFatSuperBlock getSuperBlock() {
        return sb;
    }

    /**
     * Gets the cluster bitmap.
     *
     * @return the bitmap.
     */
    public ClusterBitMap getClusterBitmap() {
        return bitmap;
    }

    @NonNull
    @Override
    public UsbFile getRootDirectory() {
        return null;
    }

    @Override
    public int getChunkSize() {
        return 0;
    }

    @Override
    public int getType() {
        return PartitionTypes.NTFS_EXFAT;
    }

    @Override
    public long getOccupiedSpace() {
        return getCapacity() - getFreeSpace();
    }

    public static FileSystem read(BlockDeviceDriver blockDevice) throws IOException, UnsupportedOperationException {
        ByteBuffer buffer = ByteBuffer.allocate(512);
        blockDevice.read(0, buffer);
        buffer.flip();

        // TODO: Check exfat file system
        if (true)
            return new ExFatFileSystem(blockDevice, )
    }

    private static class RootDirVisitor implements DirectoryParser.Visitor {

        private final ExFatSuperBlock sb;
        private ClusterBitMap bitmap;
        private UpcaseTable upcase;
        private String label;

        private RootDirVisitor(ExFatSuperBlock sb) {
            this.sb = sb;
        }

        @Override
        public void foundLabel(String label) {
            this.label = label;
        }

        @Override
        public void foundBitmap(
                long startCluster, long size) throws IOException {

            if (this.bitmap != null) {
                throw new IOException("already had a bitmap");
            }

            this.bitmap = ClusterBitMap.read(this.sb, startCluster, size);
        }

        @Override
        public void foundUpcaseTable(DirectoryParser parser, long startCluster, long size,
                                     long checksum) throws IOException {

            if (this.upcase != null) {
                throw new IOException("already had an upcase table");
            }

            this.upcase = UpcaseTable.read(
                    this.sb, startCluster, size, checksum);

            /* the parser may use this table for file names to come */
            parser.setUpcase(this.upcase);
        }

        @Override
        public void foundNode(Node node, int index) {
            /* ignore */
        }

    }

}
