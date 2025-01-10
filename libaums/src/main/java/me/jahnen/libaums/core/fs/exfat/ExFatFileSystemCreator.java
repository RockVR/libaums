package me.jahnen.libaums.core.fs.exfat;

import me.jahnen.libaums.core.driver.BlockDeviceDriver;
import me.jahnen.libaums.core.fs.FileSystem;
import me.jahnen.libaums.core.fs.FileSystemCreator;
import me.jahnen.libaums.core.partition.PartitionTableEntry;

public class ExFatFileSystemCreator implements FileSystemCreator {
    @Override
    public FileSystem read(PartitionTableEntry entry, BlockDeviceDriver blockDevice) {
        return ExFatFileSystem.read(blockDevice);
    }
}
