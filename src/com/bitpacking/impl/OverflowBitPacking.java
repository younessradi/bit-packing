package com.bitpacking.impl;

import com.bitpacking.core.AbstractBitPacking;
import com.bitpacking.core.CompressionType;
import com.bitpacking.utils.BitUtils;

 
 // ** overflow bit packing ~ two-tier storage

 // ** main storage for small values , overflow area for outliers..
 
public class OverflowBitPacking extends AbstractBitPacking {
    
    private int[] overflowArea;
    private int overflowThreshold;
    private int mainBits;

    private int totalMainBits; // includes flag bit
    
    @Override
    public CompressionType getType() {
        return CompressionType.OVERFLOW;
    }
    
    @Override
    protected int[] performCompression(int[] data, int bitsPerValue) {
        // find optimal threshold
        OverflowStats stats = analyzeOverflow(data);
        
        this.overflowThreshold = stats.threshold;
        this.mainBits = stats.mainBits;
        
        // if no overflows,  use simple compression.
        if ( stats.overflowCount == 0) {
            this.totalMainBits = mainBits;
            this.overflowArea = new int[0];
            
            int totalBits = data.length * mainBits;
            int compressedSize = (totalBits + 31) / 32;

            int[] compressed = new int[compressedSize];
            
            for (int i = 0; i < data.length; i++) {
                int bitPosition = i * mainBits;

                BitUtils.writeBitsOverlapping( compressed, bitPosition, data[i], mainBits);
            }
            
            return compressed;
        }
        
        // overflow compression

        this.totalMainBits = mainBits + 1; // +1 for flag bit
        this.overflowArea = new int[stats.overflowCount];
        int overflowIndex =  0;
        
        // calculate sizes
        int mainStorageBits = data.length * totalMainBits;
        int mainStorageSize = (mainStorageBits + 31) / 32;
        int totalSize = mainStorageSize +  overflowArea.length;
        int[] compressed = new int[totalSize];
        
        // compress
        for (int i = 0; i < data.length; i++) {
            int value = data[i];
            int bitPosition = i * totalMainBits;
            
            if (value >= overflowThreshold) {
                // overflow: flag=1 ~ store index.
                int encoded = (1 << mainBits) | overflowIndex;
                BitUtils.writeBitsOverlapping(compressed, bitPosition, encoded, totalMainBits);
                overflowArea[overflowIndex++] = value;
            } else {
                // direct: flag=0 ~ store value
                int encoded = (0 << mainBits) | value;

                BitUtils.writeBitsOverlapping(compressed, bitPosition, encoded, totalMainBits);
            }
        }
        
        // copy overflow area to end
        System.arraycopy(overflowArea, 0, compressed, mainStorageSize, overflowArea.length);
        
        return compressed;
    }
    
    @Override
    protected int[] performDecompression(int[] compressed, int originalSize) {
        int[] decompressed = new int[originalSize];
        
        // no overflow? simple extraction
        if (overflowArea == null || overflowArea.length == 0) {
            for (int i = 0; i < originalSize; i++) {
                int bitPosition = i * totalMainBits;

                decompressed[i] = BitUtils.extractBitsOverlapping(compressed, bitPosition, totalMainBits);
            }
            return decompressed;
        }
        
        // with overflow
        int mainStorageBits = originalSize * totalMainBits;
        int mainStorageSize = (mainStorageBits + 31) / 32;
        
        // extract overflow area
        int overflowSize = compressed.length - mainStorageSize;
        int[] overflow = new int[overflowSize];
        System.arraycopy(compressed, mainStorageSize, overflow, 0, overflowSize);
        
        // decompress
        int mask = BitUtils.createMask(mainBits);
        
        for (int i = 0; i < originalSize; i++) {
            int bitPosition = i * totalMainBits;
            int encoded = BitUtils.extractBitsOverlapping(compressed, bitPosition, totalMainBits);
            
            int flag = (encoded >>> mainBits) & 1;
            int payload = encoded & mask;
            
            if (flag == 1) {
                // overflow value
                decompressed[i] = overflow[payload];
            } else {
                // direct value
                decompressed[i] = payload;
            }
        }
        
        return decompressed;
    }
    
    @Override
    protected int performGet(int index) {
        // no overflow? simple
        if (overflowArea.length == 0) {
            int bitPosition = index * totalMainBits;
            return BitUtils.extractBitsOverlapping(compressedData, bitPosition, totalMainBits);
        }
        
        // with overflow
        int mainStorageBits = originalSize * totalMainBits;

        int mainStorageSize = (mainStorageBits + 31) / 32;
        
        int bitPosition = index * totalMainBits;

        int encoded = BitUtils.extractBitsOverlapping(compressedData, bitPosition, totalMainBits);
        
        int flag = (encoded >>> mainBits ) & 1;
        int mask = BitUtils.createMask(mainBits);
        int payload = encoded & mask;
        
        if (flag == 1) {
            return compressedData[mainStorageSize + payload];
        } else {
            return payload;
        }
    }
    
    // find optimal overflow threshold
    private OverflowStats analyzeOverflow(int[] data) {
        int max = 0;
        for (int val : data) {
            if (val > max) max = val;
        }
        
        int maxBits = BitUtils.bitsNeeded(max);
        
        int bestThreshold = max;
        int bestMainBits = maxBits;
        int bestOverflowCount = 0;
        int bestTotalBits = data.length * maxBits;
        
        // assessing different thresholds ~ pick the one that saves bits. 
        for (int thresholdBits = Math.max(1, maxBits - 8); thresholdBits < maxBits;  thresholdBits++) {
            int threshold = (1 << thresholdBits);
            int overflowCount = 0;
            
            for (int val : data) {
                if (val >= threshold) overflowCount++;
            }
            
            int mainBits = thresholdBits;
            int indexBits = BitUtils.bitsNeeded(Math.max(1, overflowCount));
            
            if (indexBits > mainBits) continue;
            
            int mainStorageBits = data.length * (mainBits + 1); // +1 for flag
            int overflowStorageBits = overflowCount * 32;
            int totalBits = mainStorageBits + overflowStorageBits;
            
            if (totalBits < bestTotalBits) {
                bestTotalBits = totalBits;
                bestThreshold = threshold;

                bestMainBits = mainBits;
                bestOverflowCount = overflowCount;
            }
        }
        
        return new OverflowStats(bestThreshold , bestMainBits, bestOverflowCount);
    }
    
    // helper class
    private static class OverflowStats {
        final int threshold;
        final int mainBits;
        final int overflowCount;
        
        OverflowStats(int threshold, int mainBits, int overflowCount) {
            this.threshold = threshold;
            this.mainBits = mainBits;
            
            this.overflowCount = overflowCount;
        }
    }
}
