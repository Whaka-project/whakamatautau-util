package com.whaka.data.shuffle.pairwise;

import static com.whaka.util.UberStreams.*;
import static java.util.stream.Stream.*;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.IntStream;

import com.whaka.data.shuffle.pairwise.SequentialStrategy;
import com.whaka.util.function.MapStream;

public class StrategyPerformanceTest {

	public static void main(String[] args) {

		int[][] sizes = createSizes(2, 12, 9);
		test(SequentialStrategy.INSTANCE, sizes, 5)
			.forEach(StrategyPerformanceTest::printResult);
	}
	
	public static int[][] createSizes(int min, int max, int fill) {
		System.out.printf("Rows: %d-%d, fill: %d%n", min, max, fill);
		return IntStream.rangeClosed(min, max)
				.mapToObj(int[]::new)
				.peek(a->Arrays.fill(a, fill))
				.toArray(i->new int[i][]);
	}
	
	public static MapStream<int[], Long[]> test(Function<int[], int[][]> strategy, int[][] sizes, int invocation) {
		return stream(sizes).toMapStream(x -> x, x -> test(strategy, x, invocation));
	}
	
	public static Long[] test(Function<int[], int[][]> strategy, int[] size, int invocation) {
		Long times[] = new Long[invocation];
		for (int j = 0; j < times.length; j++) {
			long start = System.currentTimeMillis();
			strategy.apply(size);
			times[j] = System.currentTimeMillis() - start;
		}
		return times;
	}
	
	public static void printResult(int[] size, Long[] results) {
		double average = of(results).mapToLong(Long::longValue).average().getAsDouble();
		System.out.printf("%d: %.1f%n", size.length, average);
	}
}
