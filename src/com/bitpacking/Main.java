package com.bitpacking;

import com.bitpacking.benchmark.Benchmark;
import com.bitpacking.benchmark.BenchmarkResult;
import com.bitpacking.core.BitPacking;
import com.bitpacking.core.CompressionType;
import com.bitpacking.factory.BitPackingFactory;

import java.util.Arrays;


//  bit packing demo and benchmark


public class Main {

    public static void main(String[] args) {
        System.out.println("*** bit packing compression ***\n");

        // basic demo..
        basicDemo();

        // test all types
        testAllTypes();

        // benchmark
        runBenchmarks();
    }

    // basic usage example
    private static void basicDemo() {
        System.out.println("***** basic demo *****");

        int[] data = {1, 5, 12, 7, 3, 9, 15, 2};
        System.out.println("original : " + Arrays.toString(data));
        System.out.println("original size : " + (data.length * 32) +  " bits. \n");

        //  compress
        BitPacking packer = BitPackingFactory.create(CompressionType.OVERLAPPING);
        int[] compressed = packer.compress(data);

        System.out.println("compressed : " + compressed.length + " integers");
        System.out.println("bits per value : " + packer.getBitsPerValue() );
        System.out.println("compression ratio : " + String.format("%.2fx", packer.getCompressionRatio()));

        //  random access. {GET}..
        System.out.println("\n random access:");
        System.out.println("  data[0] = " + packer.get(0));
        System.out.println("  data[3] = " + packer.get(3));
        System.out.println("  data[7] = " + packer.get(7));

        // decompress.
        int[] decompressed = packer.decompress(compressed, data.length);
        System.out.println("\n decompressed: " + Arrays.toString(decompressed));
        System.out.println("match : " + Arrays.equals(data, decompressed));
    }

    // test all compression types
    private static void testAllTypes() {
        System.out.println("\n--- TEST ALL TYPES ---");

        int[] data = Benchmark.generateRandom(100, 4095);

        for (CompressionType type : CompressionType.values()) {
            BitPacking packer = BitPackingFactory.create(type);
            
            int[] compressed = packer.compress(data);
            int[] decompressed = packer.decompress(compressed, data.length);

            boolean correct = Arrays.equals(data, decompressed);
            System.out.println(type + ": " + (correct ? "OK" : "FAIL") +
                " (ratio : " + String.format("%.2fx", packer.getCompressionRatio()) + ")");
        }
    }

    // run benchmarks
    private static void runBenchmarks() {
        System.out.println("\n--- Benchmarks ---");

        // test different scenarios
        System.out.println("\n1. random data ~ 10k elements:");
        int[] randomData = Benchmark.generateRandom(10_000, 4095);
        benchmarkAll(randomData);

        System.out.println("\n2. sparse data ~ (10k elements , 90% small, 10% large):");
        int[] sparseData = Benchmark.generateSparse(10_000, 15, 1000, 10000);
        benchmarkAll(sparseData);

        // transmission analysis
        System.out.println("\n--- transmission analysis ---");
        System.out.println("(100k elements at different bandwidths)");
        int[] testData = Benchmark.generateRandom(100_000, 4095);

        for (CompressionType type : CompressionType.values()) {
            BenchmarkResult result = Benchmark.benchmark(type, testData);
            Benchmark.printTransmissionAnalysis(result, 100); // 100 Mbps
        }

        System.out.println("\n***=== summary ===***");
        System.out.println("OVERLAPPING  : best compression, slower access!");
        System.out.println("NON_OVERLAPPING: faster access, more space!");
        System.out.println("OVERFLOW     : best for sparse data!");
    }

    private static void benchmarkAll(int[] data) {
        for (CompressionType type : CompressionType.values()) {
            BenchmarkResult result = Benchmark.benchmark(type, data);

            Benchmark.printResult(result);
        }
    }
}
