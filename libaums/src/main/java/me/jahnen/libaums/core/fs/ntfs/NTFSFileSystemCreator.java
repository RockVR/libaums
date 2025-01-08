package me.jahnen.libaums.core.fs.ntfs;

import java.io.IOException;

import me.jahnen.libaums.core.driver.BlockDeviceDriver;
import me.jahnen.libaums.core.fs.FileSystem;
import me.jahnen.libaums.core.fs.FileSystemCreator;
import me.jahnen.libaums.core.partition.PartitionTableEntry;

public class NTFSFileSystemCreator implements FileSystemCreator {
    @Override
    public FileSystem read(PartitionTableEntry entry, BlockDeviceDriver blockDevice) throws IOException {
        return NTFSFileSystem.read(blockDevice);
    }
}
