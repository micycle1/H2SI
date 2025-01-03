package com.github.micycle1.h2si;

import static java.lang.Math.PI;
import static java.lang.Math.sqrt;
import static net.jafama.FastMath.atan;
import static net.jafama.FastMath.atan2;
import static net.jafama.FastMath.cos;
import static net.jafama.FastMath.sin;

import org.apache.commons.numbers.complex.Complex;

import net.jafama.FastMath;

/**
 * The H2SI (<i>Hilbert square, super-importance</i>) perceptual colour space.
 *
 * <p>
 * This implementation is based on the paper "H2SI – A New Perceptual Colour
 * Space" by Michael Nölle, Martin Suda, and Winfried Boxleitner. The colour
 * space provides a metric that closely aligns with human colour perception
 * while maintaining mathematical properties suitable for algorithmic image
 * processing.
 * </p>
 *
 * <p>
 * The H2SI space represents colours using three complex numbers. These can be
 * seen as triplet states of a three-dimensional quantum system or a
 * three-dimensional normalised Hilbert space ℋ = 𝒞³.
 * </p>
 *
 * <p>
 * The implementation provides optimised methods for:
 * </p>
 * <ul>
 * <li>Converting between H2SI coordinates and their complex component
 * representation</li>
 * <li>Interpolating between colours in the H2SI space</li>
 * <li>Converting between H2SI and other colour spaces</li>
 * </ul>
 *
 * <p>
 * Performance Note:
 * </p>
 * The implementation uses optimised trigonometric calculations to minimise
 * computational overhead. The conversion between H2SI coordinates and complex
 * components has been optimised to use minimal trigonometric function calls
 * through the application of trigonometric identities.
 *
 * @author Michael Carleton
 */
public class H2SI {

	/**
	 * Converts HSI (hue, saturation, intensity) colour values into their H2SI
	 * colour space representation, comprising a complex number triplet.
	 *
	 * @param H The hue component, defined within the range [0, 2π].
	 * @param S The saturation component, defined within the range [0, 1].
	 * @param I The intensity component, defined within the range [0, 1].
	 * @return An array of three complex numbers that represent the H2SI value.
	 */
	public static Complex[] hsiToH2si(double H, double S, double I) {
		// First complex number: sqrt(1 - S/2) * exp(j * arctan(sqrt((1-I)/I)))
		Complex first = Complex.ofCartesian(sqrt(1 - S / 2), 0).multiply(Complex.I.multiply(atan(sqrt((1 - I) / I))).exp());

		// Second complex number: sqrt(S/2) * cos(2H) * exp(-jH)
		Complex second = Complex.ofCartesian(sqrt(S / 2) * cos(2 * H), 0).multiply(Complex.I.multiply(-H).exp());

		// Third complex number: sqrt(S/2) * sin(2H) * exp(jH)
		Complex third = Complex.ofCartesian(sqrt(S / 2) * sin(2 * H), 0).multiply(Complex.I.multiply(H).exp());

		return new Complex[] { first, second, third };
	}

	public static double[] h2sitoHSI(Complex... complexes) {
		if (complexes.length != 3) {
			throw new IllegalArgumentException("Three Complex numbers are required.");
		}
		Complex X1 = complexes[0];
		Complex X2 = complexes[1];
		Complex X3 = complexes[2];

		double S = 2 * (X2.abs() * X2.abs() + X3.abs() * X3.abs());
		double I = X1.getReal() * X1.getReal() / (1 - S / 2);
		double H = atan2(X3.getReal() + X2.getImaginary(), X2.getReal() + X3.getImaginary());

		// Adjust H to be in [0, 2π]
		if (H < 0) {
			H = (2 * PI) + H;
		}

		// Clamp values in case of slight overflow
		S = S > 1 ? 1 : S < 0 ? 0 : S;
		I = I > 1 ? 1 : I < 0 ? 0 : I;

		return new double[] { H, S, I };
	}

	/**
	 * Linearly interpolate between two H2SI colours.
	 * 
	 * @param h2siA
	 * @param h2siB
	 * @param t
	 * @return
	 */
	public static Complex[] interpolateH2si(Complex[] h2siA, Complex[] h2siB, double t) {
		Complex X1 = h2siA[0].multiply(1 - t).add(h2siB[0].multiply(t));
		Complex X2 = h2siA[1].multiply(1 - t).add(h2siB[1].multiply(t));
		Complex X3 = h2siA[2].multiply(1 - t).add(h2siB[2].multiply(t));
		return new Complex[] { X1, X2, X3 };
	}

	public static double[] hsiToH2siReals(double H, double S, double I) {
		// Performance note: This optimized version uses:
		// - 4 sqrt calls: sqrt(1-S/2), sqrt((1-I)/I), sqrt(1+x*x), sqrt(S/2)
		// - 1 cos call: cos(H)
		// - 1 sin call: sin(H)
		// Compared to original version with:
		// - 3 sqrt calls: sqrt(1-S/2), sqrt((1-I)/I), sqrt(S/2)
		// - 1 atan call: atan(sqrt((1-I)/I))
		// - 5 cos calls: cos(angle1), cos(2H), cos(-H), cos(H), cos(H)
		// - 4 sin calls: sin(angle1), sin(-H), sin(H), sin(H)

		// Calculate components for first complex number
		double sqrtTerm1 = sqrt(1 - S / 2);
		double sqrtTerm2 = sqrt(S / 2);

		// For first complex number
		// Instead of computing angle1 = atan(sqrt((1-I)/I)) and then cos/sin of that
		// angle,
		// we can directly compute the cos and sin terms:
		// If angle1 = atan(x), then:
		// cos(angle1) = 1/sqrt(1 + x²)
		// sin(angle1) = x/sqrt(1 + x²)
		double x = sqrt((1 - I) / I);
		double denom = sqrt(1 + x * x); // = sqrt(1/I)
		double first_real = sqrtTerm1 / denom;
		double first_imag = sqrtTerm1 * x / denom;

		// For second and third complex numbers
		// Pre-calculate H terms (only need one sin and cos call)
		double cosH = FastMath.cos(H);
		double sinH = FastMath.sin(H);

		// Use double angle formulas:
		// cos(2H) = cos²(H) - sin²(H)
		// sin(2H) = 2sin(H)cos(H)
		double cos2H = cosH * cosH - sinH * sinH;
		double sin2H = 2 * sinH * cosH;

		// For second complex number:
		// cos(-H) = cos(H)
		// sin(-H) = -sin(H)
		double second_real = sqrtTerm2 * cos2H * cosH;
		double second_imag = -sqrtTerm2 * cos2H * sinH;

		// For third complex number:
		double third_real = sqrtTerm2 * sin2H * cosH;
		double third_imag = sqrtTerm2 * sin2H * sinH;

		return new double[] { first_real, first_imag, second_real, second_imag, third_real, third_imag };
	}

	/**
	 * Converts a 6-dimensional real H2SI color space vector into HSI (hue,
	 * saturation, intensity) components. This method expects an array of six
	 * elements representing the real and imaginary parts of the three complex
	 * components of the H2SI color space.
	 *
	 * @param components A double array of length 6 where:
	 *                   <ul>
	 *                   <li>components[0] and components[1] represent the real and
	 *                   imaginary parts of X1,</li>
	 *                   <li>components[2] and components[3] represent the real and
	 *                   imaginary parts of X2,</li>
	 *                   <li>components[4] and components[5] represent the real and
	 *                   imaginary parts of X3.</li>
	 *                   </ul>
	 * @return A double array containing the HSI components: - [0]: Hue (H),
	 *         normalized between 0 and 2π. - [1]: Saturation (S), clamped between 0
	 *         and 1. - [2]: Intensity (I), clamped between 0 and 1.
	 */
	public static double[] h2siRealsToHSI(double[] components) {
		if (components.length != 6) {
			throw new IllegalArgumentException("Expected 6 components.");
		}

		// Extract components
		double x1_real = components[0];
//        double x1_imag = components[1]; // unused
		double x2_real = components[2], x2_imag = components[3];
		double x3_real = components[4], x3_imag = components[5];

		// Calculate absolute values squared
		double x2_abs_sq = x2_real * x2_real + x2_imag * x2_imag;
		double x3_abs_sq = x3_real * x3_real + x3_imag * x3_imag;

		// Calculate S, I, and H
		double S = 2 * (x2_abs_sq + x3_abs_sq);
		double I = (x1_real * x1_real) / (1 - S / 2);
		double H = FastMath.atan2(x3_real + x2_imag, x2_real + x3_imag);

		// Adjust H to be in [0, 2π]
		if (H < 0) {
			H = (2 * PI) + H;
		}

		// Clamp values in case of slight overflow
		S = S > 1 ? 1 : S < 0 ? 0 : S;
		I = I > 1 ? 1 : I < 0 ? 0 : I;

		return new double[] { H, S, I };
	}

	public static double[] interpolateH2siReals(double[] realsA, double[] realsB, double t) {
		double[] result = new double[6];
		double oneMinusT = 1.0 - t;

		// Linear interpolation for each component
		for (int i = 0; i < 6; i++) {
			result[i] = realsA[i] * oneMinusT + realsB[i] * t;
		}

		return result;
	}

	public static double[] rgbToHSI(double r, double g, double b) {
		double x = Math.min(Math.min(r, g), b);
		double y = Math.max(Math.max(r, g), b);
		double I = (x + y) / 2.0;
		double S = y - x;
		double c1 = r - (g + b) / 2.0;
		double c2 = (sqrt(3) * (b - g)) / 2.0;
		double H = atan2(c2, c1);
		if (H < 0) {
			H = (2 * PI) + H;
		}

		return new double[] { H, S, I };
	}
	
	public static void main(String[] args) {
		   double[] hsi = rgbToHSI(1, 0, 0);
	        System.out.println("H: " + hsi[0] + ", S: " + hsi[1] + ", I: " + hsi[2]);
	}
}
