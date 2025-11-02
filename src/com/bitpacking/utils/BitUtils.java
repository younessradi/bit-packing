package com.bitpacking.utils;

// all bit manipulation related
 
public final class BitUtils {
    
    private BitUtils() {}
    
    // how many bits needed to represent this value
    public static int bitsNeeded(int value) {
        if (value < 0) throw new IllegalArgumentException("no negative value support yet..");
        if (value == 0) return 1;
        return 32 - Integer.numberOfLeadingZeros(value);
    }
    
    // how many bits needed for max value in array
    public static int bitsNeededForArray(int[] data) {
        int max = 0;

        for (int val : data) {
            if (val < 0) throw new IllegalArgumentException("no negative value support yet..");
            if (val > max) max = val;
        }
        return bitsNeeded(max);
    }
    
    // create mask with n bits set to 1
    public static int createMask(int bits) {
        if (bits == 32) return -1;
        return (1 << bits) - 1;
    }
    
    // extract bits that can span two integers ~ overlap
    public static int extractBitsOverlapping(int[] data, int bitPosition, int bitsPerValue) {
        int intIndex = bitPosition / 32;
        int bitOffset = bitPosition % 32;
        int bitsAvailable = 32 - bitOffset;
        
        if (bitsAvailable >= bitsPerValue) {
            // all bits in one integer
            return (data[intIndex] >>> bitOffset) & createMask(bitsPerValue);
        } else {

            // spans two integers
            int lowBits = (data[intIndex] >>> bitOffset) & createMask(bitsAvailable);
            int highBits = data[intIndex + 1] & createMask(bitsPerValue - bitsAvailable);
            return lowBits | (highBits << bitsAvailable);
        }
    }
    
    // write bits that can span two integers (overlapping)
    public static void writeBitsOverlapping(int[] data, int bitPosition, int value, int bitsPerValue) {
        int intIndex = bitPosition / 32;
        int bitOffset = bitPosition % 32;
        int bitsAvailable = 32 - bitOffset;
        int mask = createMask(bitsPerValue);
        value &= mask;
        
        if (bitsAvailable >= bitsPerValue) {
            // all bits in one integer
            int clearMask = ~(mask << bitOffset);
            data[intIndex] = (data[intIndex] & clearMask) | (value << bitOffset);
        } else {
            // spans two integers
            int lowMask = createMask(bitsAvailable);
            int highMask = createMask(bitsPerValue - bitsAvailable);
            
            int clearLowMask = ~(lowMask << bitOffset);
            data[intIndex] = (data[intIndex] & clearLowMask) | ((value & lowMask) << bitOffset);
            
            int highBits = value >>> bitsAvailable;
            data[intIndex + 1] = (data[intIndex + 1] & ~highMask) | (highBits & highMask);
        }
    }
    
    // extract bits within single integer ~ non-overlapping
    public static int extractBitsNonOverlapping(int[] data, int intIndex, int bitOffset, int bitsPerValue) {
        return (data[intIndex] >>> bitOffset) & createMask(bitsPerValue);
    }
    
    // write bits within single integer ~ non-overlapping
    public static void writeBitsNonOverlapping(int[] data, int intIndex, int bitOffset, int value, int bitsPerValue) {
        int mask = createMask(bitsPerValue);
        value &= mask;
        int clearMask = ~(mask << bitOffset);
        data[intIndex] = (data[intIndex] & clearMask) | (value << bitOffset);
    }
}
