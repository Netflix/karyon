package com.netflix.karyon.admin;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.junit.Test;

public class CompletableFutureTest {
    @Test
    public void test() {
        CompletableFuture<Integer> cf1 = CompletableFuture.completedFuture(1);
        CompletableFuture<Integer> cf2 = CompletableFuture.completedFuture(2);
        CompletableFuture<Integer> cf3 = CompletableFuture.completedFuture(3);
        
        List<CompletableFuture<Integer>> cfs = Arrays.asList(cf1, cf2, cf3);
        
        List<Integer> result = cfs
            .stream()
            .map((f) -> f.join())
            .collect(Collectors.toList());
        
        System.out.println(result);
    }
}
