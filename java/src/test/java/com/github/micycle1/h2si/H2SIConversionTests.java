package com.github.micycle1.h2si;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.ThreadLocalRandom;

import org.apache.commons.numbers.complex.Complex;
import org.junit.jupiter.api.Test;

class H2SIConversionTests {

	private static final double TOLERANCE = 1e-10;
	private static final int N = 100_000;

	/**
	 * Tests the round-trip conversion of random HSI values to 3-component H2SI
	 * complex numbers and back to HSI values.
	 */
	@Test
	void testRandomRoundTripComplexes() {
		for (int i = 0; i < N; i++) {
			double H = 2 * Math.PI * ThreadLocalRandom.current().nextDouble(); // Hue 0 to 2π
			double S = ThreadLocalRandom.current().nextDouble(); // Saturation 0 to 1
			double I = ThreadLocalRandom.current().nextDouble(); // Intensity 0 to 1

			Complex[] complexes = H2SI.hsiToH2si(H, S, I);
			double[] result = H2SI.h2sitoHSI(complexes);

			assertEquals(H, result[0], TOLERANCE, "Hue should be approximately equal");
			assertEquals(S, result[1], TOLERANCE, "Saturation should be approximately equal");
			assertEquals(I, result[2], TOLERANCE, "Intensity should be approximately equal");
		}
	}

	/**
	 * Tests the round-trip conversion of random HSI values to six-dimensional real
	 * colour space component arrays and back to HSI values.
	 */
	@Test
	void testRandomRoundTripComponents() {
		for (int i = 0; i < N; i++) {
			double H = 2 * Math.PI * ThreadLocalRandom.current().nextDouble(); // Hue 0 to 2π
			double S = ThreadLocalRandom.current().nextDouble(); // Saturation 0 to 1
			double I = ThreadLocalRandom.current().nextDouble(); // Intensity 0 to 1

			double[] components = H2SI.hsiToH2siReals(H, S, I);
			double[] result = H2SI.h2siRealsToHSI(components);

			assertEquals(H, result[0], TOLERANCE, "Hue should be approximately equal");
			assertEquals(S, result[1], TOLERANCE, "Saturation should be approximately equal");
			assertEquals(I, result[2], TOLERANCE, "Intensity should be approximately equal");
		}
	}

	/**
	 * Tests for equivalence between the H2SI space as expressed as a 3d complex,
	 * and 6d real.
	 */
	@Test
	void testComplexesRealsEquivalence() {
		for (int i = 0; i < N; i++) {
			double H = 2 * Math.PI * ThreadLocalRandom.current().nextDouble(); // Hue 0 to 2π
			double S = ThreadLocalRandom.current().nextDouble(); // Saturation 0 to 1
			double I = ThreadLocalRandom.current().nextDouble(); // Intensity 0 to 1

			Complex[] complexes = H2SI.hsiToH2si(H, S, I);
			double[] components = H2SI.hsiToH2siReals(H, S, I);

			assertEquals(complexes[0].getReal(), components[0], TOLERANCE);
			assertEquals(complexes[0].getImaginary(), components[1], TOLERANCE);
			assertEquals(complexes[1].getReal(), components[2], TOLERANCE);
			assertEquals(complexes[1].getImaginary(), components[3], TOLERANCE);
			assertEquals(complexes[2].getReal(), components[4], TOLERANCE);
			assertEquals(complexes[2].getImaginary(), components[5], TOLERANCE);

			double[] resultFromComplexes = H2SI.h2sitoHSI(complexes);
			double[] resultFromComponents = H2SI.h2siRealsToHSI(components);

			assertArrayEquals(resultFromComplexes, resultFromComponents, TOLERANCE, "Results from complexes and components should be equivalent");
		}
	}

	/**
	 * Test whether interpolating in complex and reals forms yields the same result.
	 */
	@Test
	void testInterpolationEquivalence() {
		for (int i = 0; i < N; i++) {
			double H = 2 * Math.PI * ThreadLocalRandom.current().nextDouble(); // Hue 0 to 2π
			double S = ThreadLocalRandom.current().nextDouble(); // Saturation 0 to 1
			double I = ThreadLocalRandom.current().nextDouble(); // Intensity 0 to 1

			double H2 = 2 * Math.PI * ThreadLocalRandom.current().nextDouble(); // Hue 0 to 2π
			double S2 = ThreadLocalRandom.current().nextDouble(); // Saturation 0 to 1
			double I2 = ThreadLocalRandom.current().nextDouble(); // Intensity 0 to 1

			Complex[] complexes = H2SI.hsiToH2si(H, S, I);
			double[] components = H2SI.hsiToH2siReals(H, S, I);
			Complex[] complexes2 = H2SI.hsiToH2si(H2, S2, I2);
			double[] components2 = H2SI.hsiToH2siReals(H2, S2, I2);

			double t = ThreadLocalRandom.current().nextDouble();

			Complex[] iComplexes = H2SI.interpolateH2si(complexes, complexes2, t);
			double[] iComponents = H2SI.interpolateH2siReals(components, components2, t);

			assertEquals(iComplexes[0].getReal(), iComponents[0], TOLERANCE);
			assertEquals(iComplexes[0].getImaginary(), iComponents[1], TOLERANCE);
			assertEquals(iComplexes[1].getImaginary(), iComponents[3], TOLERANCE);
			assertEquals(iComplexes[1].getReal(), iComponents[2], TOLERANCE);
			assertEquals(iComplexes[2].getReal(), iComponents[4], TOLERANCE);
			assertEquals(iComplexes[2].getImaginary(), iComponents[5], TOLERANCE);
		}
	}

}
