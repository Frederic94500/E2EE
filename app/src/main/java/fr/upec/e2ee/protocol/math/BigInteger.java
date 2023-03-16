package fr.upec.e2ee.protocol.math;

import java.util.Objects;

public class BigInteger {
    /**
     * This mask is used to obtain the value of an int as if it were unsigned.
     */
    static final long LONG_MASK = 0xffffffffL;
    /**
     * This constant limits {@code mag.length} of BigIntegers to the supported
     * range.
     */
    private static final int MAX_MAG_LENGTH = Integer.MAX_VALUE / Integer.SIZE + 1; // (1 << 26)
    /**
     * The signum of this BigInteger: -1 for negative, 0 for zero, or
     * 1 for positive.  Note that the BigInteger zero <em>must</em> have
     * a signum of 0.  This is necessary to ensures that there is exactly one
     * representation for each BigInteger value.
     */
    final int signum;
    /**
     * The magnitude of this BigInteger, in <i>big-endian</i> order: the
     * zeroth element of this array is the most-significant int of the
     * magnitude.  The magnitude must be "minimal" in that the most-significant
     * int ({@code mag[0]}) must be non-zero.  This is necessary to
     * ensure that there is exactly one representation for each BigInteger
     * value.  Note that this implies that the BigInteger zero has a
     * zero-length mag array.
     */
    final int[] mag;
    /**
     * One plus the bitLength of this BigInteger. This is a stable variable.
     * (either value is acceptable).
     *
     * @see #bitLength()
     */
    private int bitLengthPlusOne;

    /**
     * Two plus the index of the lowest-order int in the magnitude of this
     * BigInteger that contains a nonzero int. This is a stable variable. The
     * least significant int has int-number 0, the next int in order of
     * increasing significance has int-number 1, and so forth.
     *
     * <p>Note: never used for a BigInteger with a magnitude of zero.
     *
     * @see #firstNonzeroIntNum()
     */
    private int firstNonzeroIntNumPlusTwo;

    /**
     * Translates the sign-magnitude representation of a BigInteger into a
     * BigInteger.  The sign is represented as an integer signum value: -1 for
     * negative, 0 for zero, or 1 for positive.  The magnitude is a sub-array of
     * a byte array in <i>big-endian</i> byte-order: the most significant byte
     * is the element at index {@code off}.  A zero value of the length
     * {@code len} is permissible, and will result in a BigInteger value of 0,
     * whether signum is -1, 0 or 1.  The {@code magnitude} array is assumed to
     * be unchanged for the duration of the constructor call.
     * <p>
     * An {@code IndexOutOfBoundsException} is thrown if the length of the array
     * {@code magnitude} is non-zero and either {@code off} is negative,
     * {@code len} is negative, or {@code off+len} is greater than the length of
     * {@code magnitude}.
     *
     * @param signum    signum of the number (-1 for negative, 0 for zero, 1
     *                  for positive).
     * @param magnitude big-endian binary representation of the magnitude of
     *                  the number.
     * @param off       the start offset of the binary representation.
     * @param len       the number of bytes to use.
     * @throws NumberFormatException     {@code signum} is not one of the three
     *                                   legal values (-1, 0, and 1), or {@code signum} is 0 and
     *                                   {@code magnitude} contains one or more non-zero bytes.
     * @throws IndexOutOfBoundsException if the provided array offset and
     *                                   length would cause an index into the byte array to be
     *                                   negative or greater than or equal to the array length.
     * @since 9
     */
    public BigInteger(int signum, byte[] magnitude, int off, int len) {
        if (signum < -1 || signum > 1) {
            throw (new NumberFormatException("Invalid signum value"));
        }
        Objects.checkFromIndexSize(off, len, magnitude.length);

        // stripLeadingZeroBytes() returns a zero length array if len == 0
        this.mag = stripLeadingZeroBytes(magnitude, off, len);

        if (this.mag.length == 0) {
            this.signum = 0;
        } else {
            if (signum == 0)
                throw (new NumberFormatException("signum-magnitude mismatch"));
            this.signum = signum;
        }
        if (mag.length >= MAX_MAG_LENGTH) {
            checkRange();
        }
    }

    /**
     * Translates a byte sub-array containing the two's-complement binary
     * representation of a BigInteger into a BigInteger.  The sub-array is
     * specified via an offset into the array and a length.  The sub-array is
     * assumed to be in <i>big-endian</i> byte-order: the most significant
     * byte is the element at index {@code off}.  The {@code val} array is
     * assumed to be unchanged for the duration of the constructor call.
     * <p>
     * An {@code IndexOutOfBoundsException} is thrown if the length of the array
     * {@code val} is non-zero and either {@code off} is negative, {@code len}
     * is negative, or {@code off+len} is greater than the length of
     * {@code val}.
     *
     * @param val byte array containing a sub-array which is the big-endian
     *            two's-complement binary representation of a BigInteger.
     * @param off the start offset of the binary representation.
     * @param len the number of bytes to use.
     * @throws NumberFormatException     {@code val} is zero bytes long.
     * @throws IndexOutOfBoundsException if the provided array offset and
     *                                   length would cause an index into the byte array to be
     *                                   negative or greater than or equal to the array length.
     * @since 9
     */
    public BigInteger(byte[] val, int off, int len) {
        if (val.length == 0) {
            throw new NumberFormatException("Zero length BigInteger");
        }
        Objects.checkFromIndexSize(off, len, val.length);

        if (val[off] < 0) {
            mag = makePositive(val, off, len);
            signum = -1;
        } else {
            mag = stripLeadingZeroBytes(val, off, len);
            signum = (mag.length == 0 ? 0 : 1);
        }
        if (mag.length >= MAX_MAG_LENGTH) {
            checkRange();
        }
    }

    /**
     * Takes an array a representing a negative 2's-complement number and
     * returns the minimal (no leading zero bytes) unsigned whose value is -a.
     */
    private static int[] makePositive(byte a[], int off, int len) {
        int keep, k;
        int indexBound = off + len;

        // Find first non-sign (0xff) byte of input
        for (keep = off; keep < indexBound && a[keep] == -1; keep++)
            ;


        /* Allocate output array.  If all non-sign bytes are 0x00, we must
         * allocate space for one extra output byte. */
        for (k = keep; k < indexBound && a[k] == 0; k++)
            ;

        int extraByte = (k == indexBound) ? 1 : 0;
        int intLength = ((indexBound - keep + extraByte) + 3) >>> 2;
        int result[] = new int[intLength];

        /* Copy one's complement of input into output, leaving extra
         * byte (if it exists) == 0x00 */
        int b = indexBound - 1;
        for (int i = intLength - 1; i >= 0; i--) {
            result[i] = a[b--] & 0xff;
            int numBytesToTransfer = Math.min(3, b - keep + 1);
            if (numBytesToTransfer < 0)
                numBytesToTransfer = 0;
            for (int j = 8; j <= 8 * numBytesToTransfer; j += 8)
                result[i] |= ((a[b--] & 0xff) << j);

            // Mask indicates which bits must be complemented
            int mask = -1 >>> (8 * (3 - numBytesToTransfer));
            result[i] = ~result[i] & mask;
        }

        // Add one to one's complement to generate two's complement
        for (int i = result.length - 1; i >= 0; i--) {
            result[i] = (int) ((result[i] & LONG_MASK) + 1);
            if (result[i] != 0)
                break;
        }

        return result;
    }

    /**
     * Returns a copy of the input array stripped of any leading zero bytes.
     */
    private static int[] stripLeadingZeroBytes(byte a[], int off, int len) {
        int indexBound = off + len;
        int keep;

        // Find first nonzero byte
        for (keep = off; keep < indexBound && a[keep] == 0; keep++)
            ;

        // Allocate new array and copy relevant part of input array
        int intLength = ((indexBound - keep) + 3) >>> 2;
        int[] result = new int[intLength];
        int b = indexBound - 1;
        for (int i = intLength - 1; i >= 0; i--) {
            result[i] = a[b--] & 0xff;
            int bytesRemaining = b - keep + 1;
            int bytesToTransfer = Math.min(3, bytesRemaining);
            for (int j = 8; j <= (bytesToTransfer << 3); j += 8)
                result[i] |= ((a[b--] & 0xff) << j);
        }
        return result;
    }

    private static void reportOverflow() {
        throw new ArithmeticException("BigInteger would overflow supported range");
    }

    /**
     * Package private method to return bit length for an integer.
     */
    static int bitLengthForInt(int n) {
        return 32 - Integer.numberOfLeadingZeros(n);
    }

    /**
     * Throws an {@code ArithmeticException} if the {@code BigInteger} would be
     * out of the supported range.
     *
     * @throws ArithmeticException if {@code this} exceeds the supported range.
     */
    private void checkRange() {
        if (mag.length > MAX_MAG_LENGTH || mag.length == MAX_MAG_LENGTH && mag[0] < 0) {
            reportOverflow();
        }
    }

    /**
     * Returns a byte array containing the two's-complement
     * representation of this BigInteger.  The byte array will be in
     * <i>big-endian</i> byte-order: the most significant byte is in
     * the zeroth element.  The array will contain the minimum number
     * of bytes required to represent this BigInteger, including at
     * least one sign bit, which is {@code (ceil((this.bitLength() +
     * 1)/8))}.
     *
     * @return a byte array containing the two's-complement representation of
     * this BigInteger.
     */
    public byte[] toByteArray() {
        int byteLen = bitLength() / 8 + 1;
        byte[] byteArray = new byte[byteLen];

        for (int i = byteLen - 1, bytesCopied = 4, nextInt = 0, intIndex = 0; i >= 0; i--) {
            if (bytesCopied == 4) {
                nextInt = getInt(intIndex++);
                bytesCopied = 1;
            } else {
                nextInt >>>= 8;
                bytesCopied++;
            }
            byteArray[i] = (byte) nextInt;
        }
        return byteArray;
    }

    /**
     * Returns the number of bits in the minimal two's-complement
     * representation of this BigInteger, <em>excluding</em> a sign bit.
     * For positive BigIntegers, this is equivalent to the number of bits in
     * the ordinary binary representation.  For zero this method returns
     * {@code 0}.  (Computes {@code (ceil(log2(this < 0 ? -this : this+1)))}.)
     *
     * @return number of bits in the minimal two's-complement
     * representation of this BigInteger, <em>excluding</em> a sign bit.
     */
    public int bitLength() {
        int n = bitLengthPlusOne - 1;
        if (n == -1) { // bitLength not initialized yet
            int[] m = mag;
            int len = m.length;
            if (len == 0) {
                n = 0; // offset by one to initialize
            } else {
                // Calculate the bit length of the magnitude
                int magBitLength = ((len - 1) << 5) + bitLengthForInt(mag[0]);
                if (signum < 0) {
                    // Check if magnitude is a power of two
                    boolean pow2 = (Integer.bitCount(mag[0]) == 1);
                    for (int i = 1; i < len && pow2; i++)
                        pow2 = (mag[i] == 0);

                    n = (pow2 ? magBitLength - 1 : magBitLength);
                } else {
                    n = magBitLength;
                }
            }
            bitLengthPlusOne = n + 1;
        }
        return n;
    }

    /**
     * Returns the specified int of the little-endian two's complement
     * representation (int 0 is the least significant).  The int number can
     * be arbitrarily high (values are logically preceded by infinitely many
     * sign ints).
     */
    private int getInt(int n) {
        if (n < 0)
            return 0;
        if (n >= mag.length)
            return signInt();

        int magInt = mag[mag.length - n - 1];

        return (signum >= 0 ? magInt :
                (n <= firstNonzeroIntNum() ? -magInt : ~magInt));
    }

    /* Returns an int of sign bits */
    private int signInt() {
        return signum < 0 ? -1 : 0;
    }

    /**
     * Returns the index of the int that contains the first nonzero int in the
     * little-endian binary representation of the magnitude (int 0 is the
     * least significant). If the magnitude is zero, return value is undefined.
     *
     * <p>Note: never used for a BigInteger with a magnitude of zero.
     */
    private int firstNonzeroIntNum() {
        int fn = firstNonzeroIntNumPlusTwo - 2;
        if (fn == -2) { // firstNonzeroIntNum not initialized yet
            // Search for the first nonzero int
            int i;
            int mlen = mag.length;
            for (i = mlen - 1; i >= 0 && mag[i] == 0; i--)
                ;
            fn = mlen - i - 1;
            firstNonzeroIntNumPlusTwo = fn + 2; // offset by two to initialize
        }
        return fn;
    }
}
