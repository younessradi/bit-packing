package com.bitpacking.benchmark;

import com.bitpacking.core.BitPacking;
import com.bitpacking.core.CompressionType;
import com.bitpacking.factory.BitPackingFactory;

import java.util.Random;

 
  // simple benchmarks for bit packing

public class Benchmark {
    

    private static final Random random = new Random(42);
    
    // benchmark single type

    public static BenchmarkResult benchmark(CompressionType type, int[] data) {
        BitPacking packer = BitPackingFactory.create(type);
        
        // time compression
        long startTime = System.nanoTime();
        int[] compressed = packer.compress(data);
        long compressTime = System.nanoTime() - startTime;
        
        // time decompression
        startTime = System.nanoTime();
        packer.decompress(compressed, data.length);
        long decompressTime = System.nanoTime() - startTime;
        
        // time random access  
        int accessCount = Math.min(100, data.length);
        startTime = System.nanoTime();
        for (int i = 0; i < accessCount; i++) {
            packer.get(random.nextInt(data.length));
        }
        long accessTime = (System.nanoTime() - startTime) / accessCount;
        
        return new BenchmarkResult(
            type,
            data.length,
            packer.getBitsPerValue(),
            compressTime,
            decompressTime,
            accessTime,
            compressed.length,
            packer.getCompressionRatio()
        );
    }
    
    // generate random data
    public static int[] generateRandom(int size, int maxValue) {
        int[] data = new int[size];
        for (int i = 0; i < size; i++) {
            data[i] = random.nextInt(maxValue + 1);
        }
        return data;
    }
    
    // generate sparse data {90% small, 10% large}
    public static int[] generateSparse(int size, int smallMax, int largeMin, int largeMax) {
        int[] data = new int[size];
        for (int i = 0; i < size; i++) {
            if (random.nextDouble() < 0.9) {
                data[i] = random.nextInt(smallMax + 1);
            } else {
                data[i] = largeMin + random.nextInt(largeMax - largeMin + 1);
            }
        }
        return data;
    }
    
    // print results
    public static void printResult(BenchmarkResult r) {
        System.out.println(" \n" + r.type);
        System.out.println("  compress :   " + formatTime(r.compressTimeNs));
        System.out.println("  decompress  : " + formatTime(r.decompressTimeNs));
        System.out.println("  access :     " + r.accessTimeNs + " ns");
        System.out.println("  ratio  :      " + String.format("%.2fx", r.ratio));
        System.out.println("  bits|value : " + r.bitsPerValue);
    }
    
    // calculate transmission break-even

    public static void printTransmissionAnalysis(BenchmarkResult r, double bandwidthMbps) {
        // bits saved by compression
        int bitsSaved = (r.dataSize - r.compressedSize) * 32;
        
        // time saved on transmission ~ in ms
        double bandwidthBitsPerMs = bandwidthMbps * 1000;

        double transmissionSavedMs = bitsSaved / bandwidthBitsPerMs;
        
        // compression overhead ~ ms
        double overheadMs = (r.compressTimeNs + r.decompressTimeNs) / 1_000_000.0;
        
        // break-even
        double breakEvenMs = transmissionSavedMs - overheadMs;
        
        System.out.println("\n" + r.type + " at " + bandwidthMbps + " Mbps:");
        System.out.println("  overhead:          " + String.format("%.2f ms", overheadMs));
        System.out.println("  transmission save: " + String.format("%.2f ms", transmissionSavedMs));
        System.out.println("  break-even:        " +
                (breakEvenMs > 0 ?
                        String.format("+%.2f ms saved", breakEvenMs) :
                        String.format("%.2f ms slower ~ not beneficial", breakEvenMs)));
    }
    
    private static String formatTime(long ns) {
        if (ns < 1_000) return ns + " ns";
        if (ns < 1_000_000) return String.format("%.2f μs", ns / 1_000.0);
        
        return String.format("%.2f ms", ns / 1_000_000.0);
    }
}
