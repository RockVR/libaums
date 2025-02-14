//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package org.jnode.fs.exfat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.jnode.fs.spi.AbstractFSObject;

public final class ExFatSuperBlock extends AbstractFSObject {
    static final boolean $assertionsDisabled = false;
    private static final String OEM_NAME = "EXFAT   ";
    private static final int SIZE = 512;
    private static final int m_iGroupClusterCount = 65536;
    private static final int m_iGroupClusterMark0 = -65536;
    private static final int m_iGroupClusterMark1 = 65535;
    private static final int m_iGroupClusterShiftBit = 16;
    private byte blockBits;
    private long blockCount;
    private long blockStart;
    private byte blocksPerClusterBits;
    private long clusterBlockStart;
    private long clusterCount;
    private final DeviceAccess da;
    private long fatBlockCount;
    private long fatBlockStart;
    private byte fsVersionMajor;
    private byte fsVersionMinor;
    private ByteBuffer[] m_clusterTable = null;
    private byte percentInUse;
    private long rootDirCluster;
    private int volumeSerial;
    private short volumeState;

    public ExFatSuperBlock(ExFatFileSystem var1) {
        super(var1);
        this.da = new DeviceAccess(var1.getApi());
    }

    public static ExFatSuperBlock read(ExFatFileSystem var0) throws IOException {
        ByteBuffer var1 = ByteBuffer.allocate(512);
        var1.order(ByteOrder.LITTLE_ENDIAN);
        var0.getApi().read(0L, var1);
        byte[] var2 = new byte[8];
        var1.position(3);
        var1.get(var2);
        if ("EXFAT   ".equals(new String(var2))) {
            if ((var1.get(110) & 255) == 1) {
                if ((var1.get(111) & 255) == 128) {
                    if ((var1.get(510) & 255) == 85 && (var1.get(511) & 255) == 170) {
                        ExFatSuperBlock var3 = new ExFatSuperBlock(var0);
                        var3.blockStart = var1.getLong(64);
                        var3.blockCount = var1.getLong(72);
                        var3.fatBlockStart = (long)var1.getInt(80);
                        var3.fatBlockCount = (long)var1.getInt(84);
                        var3.clusterBlockStart = (long)var1.getInt(88);
                        var3.clusterCount = (long)var1.getInt(92);
                        var3.rootDirCluster = (long)var1.getInt(96);
                        var3.volumeSerial = var1.getInt(100);
                        var3.fsVersionMinor = var1.get(104);
                        var3.fsVersionMajor = var1.get(105);
                        var3.volumeState = var1.getShort(106);
                        var3.blockBits = var1.get(108);
                        var3.blocksPerClusterBits = var1.get(109);
                        var3.percentInUse = var1.get(112);
                        var3.m_clusterTable = new ByteBuffer[(int)((var3.clusterCount >> 16) + 1L)];
                        if (var3.fsVersionMajor == 1) {
                            if (var3.fsVersionMinor == 0) {
                                return var3;
                            } else {
                                throw new IOException("unsupported version minor " + var3.fsVersionMinor);
                            }
                        } else {
                            throw new IOException("unsupported version major " + var3.fsVersionMajor);
                        }
                    } else {
                        throw new IOException("missing boot sector signature");
                    }
                } else {
                    throw new IOException("invalid drive number");
                }
            } else {
                throw new IOException("invalid FAT count");
            }
        } else {
            throw new IOException("OEM name mismatch");
        }
    }

    long GetCluster(long var1) {
        int var3 = (int)(var1 >> 16);
        ByteBuffer[] var8 = this.m_clusterTable;
        if (var8[var3] == null) {
            label30: {
                boolean var10001;
                long var6;
                try {
                    var8[var3] = ByteBuffer.allocate(262144);
                    this.m_clusterTable[var3].order(ByteOrder.LITTLE_ENDIAN);
                    this.m_clusterTable[var3].rewind();
                    var6 = this.blockToOffset(this.getFatBlockStart());
                } catch (Exception var10) {
                    var10001 = false;
                    break label30;
                }

                long var4 = (long)((var3 << 16) * 4);

                try {
                    this.da.dev.read(var6 + var4, this.m_clusterTable[var3]);
                    this.m_clusterTable[var3].rewind();
                } catch (Exception var9) {
                    var10001 = false;
                }
            }
        }

        var8 = this.m_clusterTable;
        return var8[var3] != null ? (long)var8[var3].getInt((int)((var1 & 65535L) * 4L)) & 4294967295L : 0L;
    }

    public long blockToOffset(long var1) {
        return var1 << this.blockBits;
    }

    public long clusterToBlock(long var1) throws IOException {
        Cluster.checkValid(var1);
        return this.clusterBlockStart + (var1 - 2L << this.blocksPerClusterBits);
    }

    public long clusterToOffset(long var1) throws IOException {
        return this.blockToOffset(this.clusterToBlock(var1));
    }

    public long getBlockCount() {
        return this.blockCount;
    }

    public int getBlockSize() {
        return 1 << this.blockBits;
    }

    public long getBlockStart() {
        return this.blockStart;
    }

    public int getBlocksPerCluster() {
        return 1 << this.blocksPerClusterBits;
    }

    public int getBytesPerCluster() {
        return this.getBlockSize() << this.blocksPerClusterBits;
    }

    public long getClusterBlockStart() {
        return this.clusterBlockStart;
    }

    public long getClusterCount() {
        return this.clusterCount;
    }

    public DeviceAccess getDeviceAccess() {
        return this.da;
    }

    public long getFatBlockCount() {
        return this.fatBlockCount;
    }

    public long getFatBlockStart() {
        return this.fatBlockStart;
    }

    public byte getFsVersionMajor() {
        return this.fsVersionMajor;
    }

    public byte getFsVersionMinor() {
        return this.fsVersionMinor;
    }

    public byte getPercentInUse() {
        return this.percentInUse;
    }

    public long getRootDirCluster() {
        return this.rootDirCluster;
    }

    public int getVolumeSerial() {
        return this.volumeSerial;
    }

    public short getVolumeState() {
        return this.volumeState;
    }

    public void readCluster(ByteBuffer var1, long var2) throws IOException {
        this.da.read(var1, this.clusterToOffset(var2));
    }

    public void readCluster2(ByteBuffer var1, long var2, long var4) throws IOException {
        this.da.read(var1, this.clusterToOffset(var2) + var4);
    }
}
