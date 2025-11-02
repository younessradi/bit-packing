package com.bitpacking.impl;

import com.bitpacking.core.AbstractBitPacking;
import com.bitpacking.core.CompressionType;
import com.bitpacking.utils.BitUtils;


 // overlapping bit packing 
    //best space efficiency..{use all 32 bits}
 
public class OverlappingBitPacking extends AbstractBitPacking {
    
    @Override
    public CompressionType getType() {
        return CompressionType.OVERLAPPING;
    }
    
    @Override
    protected int[] performCompression(int[] data, int bitsPerValue) {
        // treat output as continuous bit stream.
        int totalBits = data.length * bitsPerValue;
        int compressedSize = ( totalBits + 31) / 32; // ceiling division
        int[] compressed =  new int[compressedSize];
        
        for (int i = 0; i < data.length; i++) {
            int bitPosition = i * bitsPerValue;
            BitUtils.writeBitsOverlapping(compressed,  bitPosition , data[i] , bitsPerValue);
        }
        
        return compressed;
    }
    
    @Override
    protected int[] performDecompression(int[] compressed, int originalSize) {
        int[] decompressed = new int[originalSize];
        
        for (int i = 0;  i < originalSize;   i++) {
            int bitPosition =  i * this.bitsPerValue;

            decompressed[i] =   BitUtils.extractBitsOverlapping(compressed, bitPosition, this.bitsPerValue);
        }
        
        return decompressed;
    }
    
    @Override
    protected int performGet(int index) {
        int bitPosition =  index * bitsPerValue;
        return BitUtils.extractBitsOverlapping(compressedData,  bitPosition, bitsPerValue);
    }
}
