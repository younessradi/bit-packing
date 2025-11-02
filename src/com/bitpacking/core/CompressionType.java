package com.bitpacking.core;

 // compression strategies
 
public enum CompressionType {
    OVERLAPPING,     // values can span integer boundaries  
    NON_OVERLAPPING,  // values stay within boundaries  
    OVERFLOW           // two-tier storage for sparse data
}
