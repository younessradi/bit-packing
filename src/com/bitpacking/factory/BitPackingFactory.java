package com.bitpacking.factory;

import com.bitpacking.core.BitPacking;
import com.bitpacking.core.CompressionType;
import com.bitpacking.impl.NonOverlappingBitPacking;
import com.bitpacking.impl.OverflowBitPacking;
import com.bitpacking.impl.OverlappingBitPacking;


  // factory for creating bit packing instances
 
public class BitPackingFactory {
    
    private BitPackingFactory() {}
    
    // create compressor by type
    public static BitPacking create(CompressionType type) {
        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }
        
        switch (type) {
            case OVERLAPPING:
                return new OverlappingBitPacking();
            case NON_OVERLAPPING:
                return new NonOverlappingBitPacking();
            case OVERFLOW:
                return new OverflowBitPacking();
            default:
                throw new IllegalArgumentException("unsupported type: " + type);
        }
    }
}
