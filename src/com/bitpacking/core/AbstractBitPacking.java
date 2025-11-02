package com.bitpacking.core;

import com.bitpacking.utils.BitUtils;
 
 // base class for bit packing implementations
 
public abstract class AbstractBitPacking implements BitPacking {

    protected int[] compressedData;
    protected int bitsPerValue;
    protected int originalSize;

    @Override
    public int[] compress(int[] data) {
        if (data == null || data.length == 0) {
            throw new IllegalArgumentException("data cannot be null or empty");
        }

        // calculate bits needed
        this.bitsPerValue = BitUtils.bitsNeededForArray(data);
        this.originalSize = data.length;

        // delegate to subclass
        this.compressedData = performCompression(data, bitsPerValue);
        return this.compressedData;
    }

    @Override
    public int[] decompress(int[] compressed, int originalSize) {
        if (compressed == null) {
            throw new IllegalArgumentException("compressed data cannot be null");
        }
        return performDecompression(compressed, originalSize);
    }

    @Override
    public int get(int index) {
        if (compressedData == null) {
            throw new IllegalStateException("no data compressed yet");
        }
        if (index < 0 || index >= originalSize) {
            throw new IndexOutOfBoundsException("index out of bounds");
        }
        return performGet(index);
    }

    @Override
    public int getBitsPerValue() {
        return bitsPerValue;
    }

    @Override
    public double getCompressionRatio() {
        if (compressedData == null) return 0.0;
        return (double) originalSize / compressedData.length;
    }

    // subclasses implement these
    
    protected abstract int[] performCompression(int[] data, int bitsPerValue);
    protected abstract int[] performDecompression(int[] compressed, int originalSize);
    protected abstract int performGet(int index);
}
