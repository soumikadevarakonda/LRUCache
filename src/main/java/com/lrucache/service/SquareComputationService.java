package com.lrucache.service;

/**
 * An implementation of ComputationService that computes the square of an Integer
 * after simulating an expensive 1-second delay.
 */
public class SquareComputationService implements ComputationService<Integer, Integer> {
    @Override
    public Integer compute(Integer key) throws Exception {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }
        // Simulate heavy computation delay
        Thread.sleep(1000);
        return key * key;
    }
}
