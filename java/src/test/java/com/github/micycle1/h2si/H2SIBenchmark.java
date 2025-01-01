package com.github.micycle1.h2si;

import org.apache.commons.numbers.complex.Complex;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.annotations.Scope;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Benchmarks the performance of H2SI round trip conversion between the 3d
 * complex representation vs optimised 6d real representation.
 * 
 * Include <code>jmh:benchmark</code> in the Maven goals to run the benchmark.
 */
@State(Scope.Thread)
public class H2SIBenchmark {

	private static final int N = 100_000;
	private final double[][] hsiValues = new double[N][3];

	@Setup(Level.Iteration)
	public void generateRandomHSIs() {
		for (int i = 0; i < N; i++) {
			hsiValues[i][0] = 2 * Math.PI * ThreadLocalRandom.current().nextDouble();
			hsiValues[i][1] = ThreadLocalRandom.current().nextDouble();
			hsiValues[i][2] = ThreadLocalRandom.current().nextDouble();
		}
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 5)
	@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
	@Fork(2)
	@Threads(1)
	public void testRoundTripComplexes(Blackhole bh) {
		for (double[] hsi : hsiValues) {
			Complex[] complexes = H2SI.hsiToH2si(hsi[0], hsi[1], hsi[2]);
			double[] result = H2SI.h2sitoHSI(complexes);
			bh.consume(result);
		}
	}

	@Benchmark
	@BenchmarkMode(Mode.AverageTime)
	@OutputTimeUnit(TimeUnit.MILLISECONDS)
	@Warmup(iterations = 5)
	@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
	@Fork(2)
	@Threads(1)
	public void testRoundTripComponents(Blackhole bh) {
		for (double[] hsi : hsiValues) {
			double[] components = H2SI.hsiToH2siComponents(hsi[0], hsi[1], hsi[2]);
			double[] result = H2SI.h2siComponentsToHSI(components);
			bh.consume(result);
		}
	}
}