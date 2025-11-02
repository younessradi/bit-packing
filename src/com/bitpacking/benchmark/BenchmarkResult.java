package com.bitpacking.benchmark;

import com.bitpacking.core.CompressionType;

//  meant to holds benchmark results



 
public class BenchmarkResult {
    
    public final CompressionType type;
    public final int dataSize;
    public final int bitsPerValue;
    public final long compressTimeNs;
    public final long decompressTimeNs;
    public final long accessTimeNs;
    public final int compressedSize;
    public final double ratio;
    
    public BenchmarkResult(
            CompressionType type,
            int dataSize,
            int bitsPerValue,
            long compressTimeNs,
            long decompressTimeNs,
            long accessTimeNs,
            int compressedSize,
            double ratio) {
        this.type = type;
        this.dataSize = dataSize;
        this.bitsPerValue = bitsPerValue;
        this.compressTimeNs = compressTimeNs;
        this.decompressTimeNs = decompressTimeNs;
        this.accessTimeNs = accessTimeNs;
        this.compressedSize = compressedSize;
        this.ratio = ratio;
    }
}
