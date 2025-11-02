# Bit Packing Compression

Integer array compression achieving 2-8x compression with O(1) random access.

**Author:** Youness RADI (22311111)  
**Project:** Software Engineering 2025

## What It Does

Compresses integer arrays by using minimum required bits per value instead of full 32-bit representation. Maintains direct element access without full decompression.

## Build & Run
```bash
# Compile
javac -d bin src/com/bitpacking/**/*.java

# Run
java -cp bin com.bitpacking.Main
```

## Usage
```java
BitPacking packer = BitPackingFactory.create(CompressionType.OVERLAPPING);

int[] data = {1, 5, 12, 7, 3, 9, 15, 2};
int[] compressed = packer.compress(data);      // Compress
int value = packer.get(3);                      // random access, get in O(1)
int[] restored = packer.decompress(compressed, data.length); // decompress
```

## Three Algorithms

**OVERLAPPING** - Values span integer boundaries  
→ 2.67x compression, best space efficiency  
→ Use when bandwidth matters most

**NON_OVERLAPPING** - Values stay within boundaries  
→ 2.00x compression, simplest implementation  
→ Use for balanced performance

**OVERFLOW** - Two-tier storage (main + overflow area)  
→ 2.67x compression, optimal for sparse data  
→ Use when there is a very, very small outliers in the values 

## Performance (random data ~ 10k elements:, 12-bit values)

Typical ranges across multiple runs:

| Algorithm        | Compress  | Decompress | Random Access | Ratio |
|------------------|-----------|------------|---------------|-------|
| Overlapping      | 1.3-1.8ms | 0.8-1.2ms  | 1.2-2.3µs    | 2.67x |
| Non-Overlapping  | 1.1-1.6ms | 0.8-0.9ms  | 0.5-0.6µs    | 2.00x |
| Overflow         | 2.6-4.5ms | 0.5-0.9ms  | 0.3-0.6µs    | 2.67x |

*Compression ratios are constant. Absolute times vary by system load and JVM state.*

## Transmission Break-Even (100K elements at 100 Mbps)

All three algorithms provide **positive** break-even:
- Overlapping: saves 8-12ms per transmission
- Non-Overlapping: saves 4-6ms per transmission  
- Overflow: saves 0.7-3.6ms per transmission

**Conclusion:** Compression overhead is negligible compared to transmission time saved.

## Project Structure
```
src/com/bitpacking/
├── Main.java                      # Demo + benchmarks
├── core/
│   ├── BitPacking.java           # Interface
│   ├── CompressionType.java      # Enum: OVERLAPPING|NON_OVERLAPPING|OVERFLOW
│   └── AbstractBitPacking.java   # Template method base class
├── impl/
│   ├── OverlappingBitPacking.java
│   ├── NonOverlappingBitPacking.java
│   └── OverflowBitPacking.java
├── factory/
│   └── BitPackingFactory.java    # Factory pattern
├── utils/
│   └── BitUtils.java             # Bit manipulation primitives
└── benchmark/
    ├── Benchmark.java
    └── BenchmarkResult.java
```

## Key Features

✓ Three compression strategies with different trade-offs  
✓ O(1) random access without decompression  
✓ Factory pattern for algorithm selection  
✓ Template method for code reuse  
✓ Comprehensive benchmarking suite  
✓ Transmission break-even analysis

## Requirements

- Java 8+
- No external dependencies

## Documentation

See `report.pdf` for implementation details, algorithm analysis, and benchmarking methodology.

## Design Patterns Used

- **Factory Pattern**: Algorithm instantiation via `BitPackingFactory`
- **Strategy Pattern**: `BitPacking` interface with three implementations
- **Template Method**: `AbstractBitPacking` defines workflow, subclasses implement specifics

## Real-World Applications

This compression technique is used in:
- Apache Parquet (columnar storage)
- Protocol Buffers (data serialization)
