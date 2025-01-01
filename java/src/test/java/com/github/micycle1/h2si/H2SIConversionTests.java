package com.github.micycle1.h2si;

import static org.junit.jupiter.api.Assertions.*;

import org.apache.commons.numbers.complex.Complex;
import org.junit.jupiter.api.Test;
import java.util.concurrent.ThreadLocalRandom;


class H2SIConversionTests {
	
	private static final double TOLERANCE = 1e-10;
    
    @Test
    void testRandomRoundTripHSItoH2SItoHSI() {
        for (int i = 0; i < 10_000; i++) {
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
}
