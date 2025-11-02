package com.bitpacking.core;

 
 // bit packing compression interface
 
public interface BitPacking {
    
    // compress integer array
    int[] compress(int[] data);
    
    // decompress back to original
    int[] decompress(int[] compressed, int originalSize);
    
    // get value at index without full decompression
    int get(int index);
    
    // bits used per value
    int getBitsPerValue();
    
    // compression ratio
    double getCompressionRatio();
    
    // compression type
    CompressionType getType();
}
