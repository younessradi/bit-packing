package com.bitpacking.impl;

import com.bitpacking.core.AbstractBitPacking;
import com.bitpacking.core.CompressionType;
import com.bitpacking.utils.BitUtils;

//**
  // non-overlapping bit packing ~ values never span boundaries
 // faster random access, wastes some bits

public class NonOverlappingBitPacking extends AbstractBitPacking {
    
    private int valuesPerInt; // how many values fit in one integer
    
    @Override
    public CompressionType getType() {
        return CompressionType.NON_OVERLAPPING;
    }
    
    @Override
    protected int[] performCompression(int[] data, int bitsPerValue) {
        this.valuesPerInt = 32 / bitsPerValue;
        
        if (valuesPerInt == 0) {
            throw new IllegalStateException("values require more than 32 bits");
        }
        // calculate size
        int compressedSize = (data.length + valuesPerInt - 1) / valuesPerInt;
        int[] compressed = new int[compressedSize];
        
        for (int i = 0; i < data.length; i++) {
            int intIndex = i / valuesPerInt;
            int slotIndex = i % valuesPerInt;
            int bitOffset = slotIndex * bitsPerValue;
            
            BitUtils.writeBitsNonOverlapping(compressed, intIndex, bitOffset, data[i], bitsPerValue);
        }
        
        return compressed;
    }
    
    @Override
    protected int[] performDecompression(int[] compressed, int originalSize) {
        this.valuesPerInt = 32 / this.bitsPerValue;
        int[] decompressed = new int[originalSize];
        
        for (int i = 0; i < originalSize; i++) {
            int intIndex = i / valuesPerInt;
            int slotIndex = i % valuesPerInt;
            int bitOffset = slotIndex * this.bitsPerValue;
            
            decompressed[i] = BitUtils.extractBitsNonOverlapping(compressed, intIndex, bitOffset, this.bitsPerValue);
        }
        
        return decompressed;
    }
    
    @Override
    protected int performGet(int index) {
        if (valuesPerInt == 0) {
            valuesPerInt = 32 / bitsPerValue;
        }
        
        int intIndex = index / valuesPerInt;
        int slotIndex = index % valuesPerInt;
        int bitOffset = slotIndex * bitsPerValue;
        
        return BitUtils.extractBitsNonOverlapping(compressedData, intIndex, bitOffset, bitsPerValue);
    }
}
