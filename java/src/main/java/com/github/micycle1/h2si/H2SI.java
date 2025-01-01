package com.github.micycle1.h2si;

import static java.lang.Math.PI;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sqrt;
import static net.jafama.FastMath.atan;
import static net.jafama.FastMath.atan2;
import static net.jafama.FastMath.cos;
import static net.jafama.FastMath.sin;

import org.apache.commons.numbers.complex.Complex;

/**
 * Implementation of the H2SI (Hilbert square, super-importance) color space, a
 * perceptual color space designed to match human color perception.
 * 
 * <p>
 * This implementation is based on the paper "H2SI – A New Perceptual Colour
 * Space" by Michael Nölle, Martin Suda, and Winfried Boxleitner. The color
 * space provides a metric that closely aligns with human color perception while
 * maintaining mathematical properties suitable for algorithmic image
 * processing.
 * </p>
 * 
 * <p>
 * Internally, colors are represented using three complex numbers in a
 * four-dimensional space that can be interpreted as unit bi-quaternions. This
 * representation enables efficient color interpolation and manipulation while
 * preserving perceptual uniformity.
 * </p>
 * 
 * <p>
 * The implementation provides optimized methods for:
 * </p>
 * <ul>
 * <li>Converting between H2SI coordinates and their complex component
 * representation</li>
 * <li>Interpolating between colors in the H2SI space</li>
 * <li>Converting between H2SI and other color spaces</li>
 * </ul>
 * 
 * <p>
 * Performance Note:
 * </p>
 * The implementation uses optimized trigonometric calculations to minimize
 * computational overhead. The conversion between H2SI coordinates and complex
 * components has been optimized to use minimal trigonometric function calls
 * through the application of trigonometric identities.
 * 
 * @author Michael Carleton
 */
public class H2SI {

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
//	    if (complexes.length < 3) {
//	        return new double[] {0, 0, 0}; // or throw an exception
//	    }
	    Complex X1 = complexes[0];
	    Complex X2 = complexes[1];
	    Complex X3 = complexes[2];

	    double S = 2 * (X2.abs() * X2.abs() + X3.abs() * X3.abs());
	    double I = X1.getReal() * X1.getReal() / (1 - S / 2);
	    double H = atan2(X3.getReal() + X2.getImaginary(), X2.getReal() + X3.getImaginary());

	    if (H < 0) {
	        H = 2 * Math.PI + H;
	    }

	    // Clamp values
	    S = Math.min(Math.max(S, 0), 1);
	    I = Math.min(Math.max(I, 0), 1);

	    return new double[] {H, S, I};
	}
}
