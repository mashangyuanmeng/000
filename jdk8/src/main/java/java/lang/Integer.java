package java.lang;

import java.lang.annotation.Native;

/**
 * int 类型的包装类型
 * @author  Lee Boynton
 * @author  Arthur van Hoff
 * @author  Josh Bloch
 * @author  Joseph D. Darcy
 * @since JDK1.0
 */
public final class Integer extends Number implements Comparable<Integer> {
    /**
     * -2 的 31 次方
     * have, -2<sup>31</sup>.
     */
    @Native
    public static final int   MIN_VALUE = 0x80000000;

    /**
     * 2 的 31 次方-1
     * have, 2<sup>31</sup>-1.
     */
    @Native
    public static final int   MAX_VALUE = 0x7fffffff;

    /**
     * The {@code Class} instance representing the primitive type
     * {@code int}.
     *
     * @since   JDK1.1
     */
    @SuppressWarnings("unchecked")
    public static final Class<Integer>  TYPE = (Class<Integer>) Class.getPrimitiveClass("int");

    /**
     * All possible chars for representing a number as a String
     */
    final static char[] digits = {
        '0' , '1' , '2' , '3' , '4' , '5' ,
        '6' , '7' , '8' , '9' , 'a' , 'b' ,
        'c' , 'd' , 'e' , 'f' , 'g' , 'h' ,
        'i' , 'j' , 'k' , 'l' , 'm' , 'n' ,
        'o' , 'p' , 'q' , 'r' , 's' , 't' ,
        'u' , 'v' , 'w' , 'x' , 'y' , 'z'
    };

    /**
     *
     * @param   i       an integer to be converted to a string.
     * @param   radix   the radix to use in the string representation.
     * @return  a string representation of the argument in the specified radix.
     * @see     java.lang.Character#MAX_RADIX
     * @see     java.lang.Character#MIN_RADIX
     */
    public static String toString(int i, int radix) {
        if (radix < Character.MIN_RADIX || radix > Character.MAX_RADIX)
            radix = 10;

        /* Use the faster version */
        if (radix == 10) {
            return toString(i);
        }

        char buf[] = new char[33];
        boolean negative = (i < 0);
        int charPos = 32;

        if (!negative) {
            i = -i;
        }

        while (i <= -radix) {
            buf[charPos--] = digits[-(i % radix)];
            i = i / radix;
        }
        buf[charPos] = digits[-i];

        if (negative) {
            buf[--charPos] = '-';
        }

        return new String(buf, charPos, (33 - charPos));
    }

    /**
     *
     *
     * @param   i       an integer to be converted to an unsigned string.
     * @param   radix   the radix to use in the string representation.
     * @return  an unsigned string representation of the argument in the specified radix.
     * @see     #toString(int, int)
     * @since 1.8
     */
    public static String toUnsignedString(int i, int radix) {
        return Long.toUnsignedString(toUnsignedLong(i), radix);
    }

    /**
     *
     *
     * @param   i   an integer to be converted to a string.
     * @return  the string representation of the unsigned integer value
     *          represented by the argument in hexadecimal (base&nbsp;16).
     * @see #parseUnsignedInt(String, int)
     * @see #toUnsignedString(int, int)
     * @since   JDK1.0.2
     */
    public static String toHexString(int i) {
        return toUnsignedString0(i, 4);
    }

    /**
     *
     *
     * @param   i   an integer to be converted to a string.
     * @return  the string representation of the unsigned integer value
     *          represented by the argument in octal (base&nbsp;8).
     * @see #parseUnsignedInt(String, int)
     * @see #toUnsignedString(int, int)
     * @since   JDK1.0.2
     */
    public static String toOctalString(int i) {
        return toUnsignedString0(i, 3);
    }

    /**
     *
     *
     * @param   i   an integer to be converted to a string.
     * @return  the string representation of the unsigned integer value
     *          represented by the argument in binary (base&nbsp;2).
     * @see #parseUnsignedInt(String, int)
     * @see #toUnsignedString(int, int)
     * @since   JDK1.0.2
     */
    public static String toBinaryString(int i) {
        return toUnsignedString0(i, 1);
    }

    /**
     * Convert the integer to an unsigned number.
     */
    private static String toUnsignedString0(int val, int shift) {
        // assert shift > 0 && shift <=5 : "Illegal shift value";
        int mag = Integer.SIZE - Integer.numberOfLeadingZeros(val);
        int chars = Math.max(((mag + (shift - 1)) / shift), 1);
        char[] buf = new char[chars];

        formatUnsignedInt(val, shift, buf, 0, chars);

        // Use special constructor which takes over "buf".
        return new String(buf, true);
    }

    /**
     * Format a long (treated as unsigned) into a character buffer.
     * @param val the unsigned int to format
     * @param shift the log2 of the base to format in (4 for hex, 3 for octal, 1 for binary)
     * @param buf the character buffer to write to
     * @param offset the offset in the destination buffer to start at
     * @param len the number of characters to write
     * @return the lowest character  location used
     */
     static int formatUnsignedInt(int val, int shift, char[] buf, int offset, int len) {
        int charPos = len;
        int radix = 1 << shift;
        int mask = radix - 1;
        do {
            buf[offset + --charPos] = Integer.digits[val & mask];
            val >>>= shift;
        } while (val != 0 && charPos > 0);

        return charPos;
    }

    final static char [] DigitTens = {
        '0', '0', '0', '0', '0', '0', '0', '0', '0', '0',
        '1', '1', '1', '1', '1', '1', '1', '1', '1', '1',
        '2', '2', '2', '2', '2', '2', '2', '2', '2', '2',
        '3', '3', '3', '3', '3', '3', '3', '3', '3', '3',
        '4', '4', '4', '4', '4', '4', '4', '4', '4', '4',
        '5', '5', '5', '5', '5', '5', '5', '5', '5', '5',
        '6', '6', '6', '6', '6', '6', '6', '6', '6', '6',
        '7', '7', '7', '7', '7', '7', '7', '7', '7', '7',
        '8', '8', '8', '8', '8', '8', '8', '8', '8', '8',
        '9', '9', '9', '9', '9', '9', '9', '9', '9', '9',
        } ;

    final static char [] DigitOnes = {
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        } ;

        // I use the "invariant division by multiplication" trick to
        // accelerate Integer.toString.  In particular we want to
        // avoid division by 10.
        //
        // The "trick" has roughly the same performance characteristics
        // as the "classic" Integer.toString code on a non-JIT VM.
        // The trick avoids .rem and .div calls but has a longer code
        // path and is thus dominated by dispatch overhead.  In the
        // JIT case the dispatch overhead doesn't exist and the
        // "trick" is considerably faster than the classic code.
        //
        // TODO-FIXME: convert (x * 52429) into the equiv shift-add
        // sequence.
        //
        // RE:  Division by Invariant Integers using Multiplication
        //      T Gralund, P Montgomery
        //      ACM PLDI 1994
        //

    /**
     * Returns a {@code String} object representing the
     * specified integer. The argument is converted to signed decimal
     * representation and returned as a string, exactly as if the
     * argument and radix 10 were given as arguments to the {@link
     * #toString(int, int)} method.
     *
     * @param   i   an integer to be converted.
     * @return  a string representation of the argument in base&nbsp;10.
     */
    public static String toString(int i) {
        if (i == Integer.MIN_VALUE)
            return "-2147483648";
        int size = (i < 0) ? stringSize(-i) + 1 : stringSize(i);
        char[] buf = new char[size];
        getChars(i, size, buf);
        return new String(buf, true);
    }

    /**
     * Returns a string representation of the argument as an unsigned
     * decimal value.
     *
     * The argument is converted to unsigned decimal representation
     * and returned as a string exactly as if the argument and radix
     * 10 were given as arguments to the {@link #toUnsignedString(int,
     * int)} method.
     *
     * @param   i  an integer to be converted to an unsigned string.
     * @return  an unsigned string representation of the argument.
     * @see     #toUnsignedString(int, int)
     * @since 1.8
     */
    public static String toUnsignedString(int i) {
        return Long.toString(toUnsignedLong(i));
    }

    /**
     * Places characters representing the integer i into the
     * character array buf. The characters are placed into
     * the buffer backwards starting with the least significant
     * digit at the specified index (exclusive), and working
     * backwards from there.
     *
     * Will fail if i == Integer.MIN_VALUE
     */
    static void getChars(int i, int index, char[] buf) {
        int q, r;
        int charPos = index;
        char sign = 0;

        if (i < 0) {
            sign = '-';
            i = -i;
        }

        // Generate two digits per iteration
        while (i >= 65536) {
            q = i / 100;
        // really: r = i - (q * 100);
            r = i - ((q << 6) + (q << 5) + (q << 2));
            i = q;
            buf [--charPos] = DigitOnes[r];
            buf [--charPos] = DigitTens[r];
        }

        // Fall thru to fast mode for smaller numbers
        // assert(i <= 65536, i);
        for (;;) {
            q = (i * 52429) >>> (16+3);
            r = i - ((q << 3) + (q << 1));  // r = i-(q*10) ...
            buf [--charPos] = digits [r];
            i = q;
            if (i == 0) break;
        }
        if (sign != 0) {
            buf [--charPos] = sign;
        }
    }

    final static int [] sizeTable = { 9, 99, 999, 9999, 99999, 999999, 9999999,
                                      99999999, 999999999, Integer.MAX_VALUE };

    // Requires positive x
    static int stringSize(int x) {
        for (int i=0; ; i++)
            if (x <= sizeTable[i])
                return i+1;
    }

    /**
     * Parses the string argument as a signed integer in the radix
     * specified by the second argument. The characters in the string
     * must all be digits of the specified radix (as determined by
     * whether {@link java.lang.Character#digit(char, int)} returns a
     * nonnegative value), except that the first character may be an
     * ASCII minus sign {@code '-'} ({@code '\u005Cu002D'}) to
     * indicate a negative value or an ASCII plus sign {@code '+'}
     * ({@code '\u005Cu002B'}) to indicate a positive value. The
     * resulting integer value is returned.
     *
     * <p>An exception of type {@code NumberFormatException} is
     * thrown if any of the following situations occurs:
     * <ul>
     * <li>The first argument is {@code null} or is a string of
     * length zero.
     *
     * <li>The radix is either smaller than
     * {@link java.lang.Character#MIN_RADIX} or
     * larger than {@link java.lang.Character#MAX_RADIX}.
     *
     * <li>Any character of the string is not a digit of the specified
     * radix, except that the first character may be a minus sign
     * {@code '-'} ({@code '\u005Cu002D'}) or plus sign
     * {@code '+'} ({@code '\u005Cu002B'}) provided that the
     * string is longer than length 1.
     *
     * <li>The value represented by the string is not a value of type
     * {@code int}.
     * </ul>
     *
     * <p>Examples:
     * <blockquote><pre>
     * parseInt("0", 10) returns 0
     * parseInt("473", 10) returns 473
     * parseInt("+42", 10) returns 42
     * parseInt("-0", 10) returns 0
     * parseInt("-FF", 16) returns -255
     * parseInt("1100110", 2) returns 102
     * parseInt("2147483647", 10) returns 2147483647
     * parseInt("-2147483648", 10) returns -2147483648
     * parseInt("2147483648", 10) throws a NumberFormatException
     * parseInt("99", 8) throws a NumberFormatException
     * parseInt("Kona", 10) throws a NumberFormatException
     * parseInt("Kona", 27) returns 411787
     * </pre></blockquote>
     *
     * @param      s   the {@code String} containing the integer
     *                  representation to be parsed
     * @param      radix   the radix to be used while parsing {@code s}.
     * @return     the integer represented by the string argument in the
     *             specified radix.
     * @exception  NumberFormatException if the {@code String}
     *             does not contain a parsable {@code int}.
     */
    public static int parseInt(String s, int radix)
                throws NumberFormatException
    {
        /*
         * WARNING: This method may be invoked early during VM initialization
         * before IntegerCache is initialized. Care must be taken to not use
         * the valueOf method.
         */

        if (s == null) {
            throw new NumberFormatException("null");
        }

        if (radix < Character.MIN_RADIX) {
            throw new NumberFormatException("radix " + radix +
                                            " less than Character.MIN_RADIX");
        }

        if (radix > Character.MAX_RADIX) {
            throw new NumberFormatException("radix " + radix +
                                            " greater than Character.MAX_RADIX");
        }

        int result = 0;
        boolean negative = false;
        int i = 0, len = s.length();
        int limit = -Integer.MAX_VALUE;
        int multmin;
        int digit;

        if (len > 0) {
            char firstChar = s.charAt(0);
            if (firstChar < '0') { // Possible leading "+" or "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Integer.MIN_VALUE;
                } else if (firstChar != '+')
                    throw NumberFormatException.forInputString(s);

                if (len == 1) // Cannot have lone "+" or "-"
                    throw NumberFormatException.forInputString(s);
                i++;
            }
            multmin = limit / radix;
            while (i < len) {
                // Accumulating negatively avoids surprises near MAX_VALUE
                digit = Character.digit(s.charAt(i++),radix);
                if (digit < 0) {
                    throw NumberFormatException.forInputString(s);
                }
                if (result < multmin) {
                    throw NumberFormatException.forInputString(s);
                }
                result *= radix;
                if (result < limit + digit) {
                    throw NumberFormatException.forInputString(s);
                }
                result -= digit;
            }
        } else {
            throw NumberFormatException.forInputString(s);
        }
        return negative ? result : -result;
    }

    /**
     * Parses the string argument as a signed decimal integer. The
     * characters in the string must all be decimal digits, except
     * that the first character may be an ASCII minus sign {@code '-'}
     * ({@code '\u005Cu002D'}) to indicate a negative value or an
     * ASCII plus sign {@code '+'} ({@code '\u005Cu002B'}) to
     * indicate a positive value. The resulting integer value is
     * returned, exactly as if the argument and the radix 10 were
     * given as arguments to the {@link #parseInt(java.lang.String,
     * int)} method.
     *
     * @param s    a {@code String} containing the {@code int}
     *             representation to be parsed
     * @return     the integer value represented by the argument in decimal.
     * @exception  NumberFormatException  if the string does not contain a
     *               parsable integer.
     */
    public static int parseInt(String s) throws NumberFormatException {
        return parseInt(s,10);
    }

    /**
     *
     * @param      s   the {@code String} containing the unsigned integer
     *                  representation to be parsed
     * @param      radix   the radix to be used while parsing {@code s}.
     * @return     the integer represented by the string argument in the
     *             specified radix.
     * @throws     NumberFormatException if the {@code String}
     *             does not contain a parsable {@code int}.
     * @since 1.8
     */
    public static int parseUnsignedInt(String s, int radix)
                throws NumberFormatException {
        if (s == null)  {
            throw new NumberFormatException("null");
        }

        int len = s.length();
        if (len > 0) {
            char firstChar = s.charAt(0);
            if (firstChar == '-') {
                throw new
                    NumberFormatException(String.format("Illegal leading minus sign " +
                                                       "on unsigned string %s.", s));
            } else {
                if (len <= 5 || // Integer.MAX_VALUE in Character.MAX_RADIX is 6 digits
                    (radix == 10 && len <= 9) ) { // Integer.MAX_VALUE in base 10 is 10 digits
                    return parseInt(s, radix);
                } else {
                    long ell = Long.parseLong(s, radix);
                    if ((ell & 0xffff_ffff_0000_0000L) == 0) {
                        return (int) ell;
                    } else {
                        throw new
                            NumberFormatException(String.format("String value %s exceeds " +
                                                                "range of unsigned int.", s));
                    }
                }
            }
        } else {
            throw NumberFormatException.forInputString(s);
        }
    }

    /**
     *
     *
     * @param s   a {@code String} containing the unsigned {@code int}
     *            representation to be parsed
     * @return    the unsigned integer value represented by the argument in decimal.
     * @throws    NumberFormatException  if the string does not contain a
     *            parsable unsigned integer.
     * @since 1.8
     */
    public static int parseUnsignedInt(String s) throws NumberFormatException {
        return parseUnsignedInt(s, 10);
    }

    /**
     *
     * @param      s   the string to be parsed.
     * @param      radix the radix to be used in interpreting {@code s}
     * @return     an {@code Integer} object holding the value
     *             represented by the string argument in the specified
     *             radix.
     * @exception NumberFormatException if the {@code String}
     *            does not contain a parsable {@code int}.
     */
    public static Integer valueOf(String s, int radix) throws NumberFormatException {
        return Integer.valueOf(parseInt(s,radix));
    }

    /**
     *
     * @param      s   the string to be parsed.
     * @return     an {@code Integer} object holding the value
     *             represented by the string argument.
     * @exception  NumberFormatException  if the string cannot be parsed
     *             as an integer.
     */
    public static Integer valueOf(String s) throws NumberFormatException {
        return Integer.valueOf(parseInt(s, 10));
    }

    /**
     *
     * Integer内部类，默认用于缓存 -128~127 的 int 类型的数字；
     * 也可通过 JVM 参数进行设置缓存的范围，比如：-XX:AutoBoxCacheMax=300 『即缓存范围为-128~300』
     *
     */
    private static class IntegerCache {
        static final int low = -128;
        static final int high;
        static final Integer cache[];

        static {
            // high value may be configured by property
            int h = 127;
            String integerCacheHighPropValue =
                sun.misc.VM.getSavedProperty("java.lang.Integer.IntegerCache.high");
            if (integerCacheHighPropValue != null) {
                try {
                    int i = parseInt(integerCacheHighPropValue);
                    // 获取设置值与 127这两者的最大值
                    i = Math.max(i, 127);
                    // Maximum array size is Integer.MAX_VALUE
                    h = Math.min(i, Integer.MAX_VALUE - (-low) -1);
                } catch( NumberFormatException nfe) {
                    // If the property cannot be parsed into an int, ignore it.
                }
            }
            high = h;
            cache = new Integer[(high - low) + 1];
            int j = low;
            for(int k = 0; k < cache.length; k++) {
                cache[k] = new Integer(j++);
            }
            // range [-128, 127] must be interned (JLS7 5.1.7)
            assert IntegerCache.high >= 127;
        }
        private IntegerCache() {}
    }

    /**
     *
     * @param  i an {@code int} value.
     * @return an {@code Integer} instance representing {@code i}.
     * @since  1.5
     */
    public static Integer valueOf(int i) {
        if (i >= IntegerCache.low && i <= IntegerCache.high)
            return IntegerCache.cache[i + (-IntegerCache.low)];
        return new Integer(i);
    }

    /**
     * The value of the {@code Integer}.
     *
     * @serial
     */
    private final int value;

    /**
     * Constructs a newly allocated {@code Integer} object that
     * represents the specified {@code int} value.
     *
     * @param   value   the value to be represented by the
     *                  {@code Integer} object.
     */
    public Integer(int value) {
        this.value = value;
    }

    /**
     * Constructs a newly allocated {@code Integer} object that
     * represents the {@code int} value indicated by the
     * {@code String} parameter. The string is converted to an
     * {@code int} value in exactly the manner used by the
     * {@code parseInt} method for radix 10.
     *
     * @param      s   the {@code String} to be converted to an
     *                 {@code Integer}.
     * @exception  NumberFormatException  if the {@code String} does not
     *               contain a parsable integer.
     * @see        java.lang.Integer#parseInt(java.lang.String, int)
     */
    public Integer(String s) throws NumberFormatException {
        this.value = parseInt(s, 10);
    }

    /**
     * Returns the value of this {@code Integer} as a {@code byte}
     * after a narrowing primitive conversion.
     * @jls 5.1.3 Narrowing Primitive Conversions
     */
    public byte byteValue() {
        return (byte)value;
    }

    /**
     * Returns the value of this {@code Integer} as a {@code short}
     * after a narrowing primitive conversion.
     * @jls 5.1.3 Narrowing Primitive Conversions
     */
    public short shortValue() {
        return (short)value;
    }

    /**
     * Returns the value of this {@code Integer} as an
     * {@code int}.
     */
    public int intValue() {
        return value;
    }

    /**
     * Returns the value of this {@code Integer} as a {@code long}
     * after a widening primitive conversion.
     * @jls 5.1.2 Widening Primitive Conversions
     * @see Integer#toUnsignedLong(int)
     */
    public long longValue() {
        return (long)value;
    }

    /**
     * Returns the value of this {@code Integer} as a {@code float}
     * after a widening primitive conversion.
     * @jls 5.1.2 Widening Primitive Conversions
     */
    public float floatValue() {
        return (float)value;
    }

    /**
     * Returns the value of this {@code Integer} as a {@code double}
     * after a widening primitive conversion.
     * @jls 5.1.2 Widening Primitive Conversions
     */
    public double doubleValue() {
        return (double)value;
    }

    /**
     * Returns a {@code String} object representing this
     * {@code Integer}'s value. The value is converted to signed
     * decimal representation and returned as a string, exactly as if
     * the integer value were given as an argument to the {@link
     * java.lang.Integer#toString(int)} method.
     *
     * @return  a string representation of the value of this object in
     *          base&nbsp;10.
     */
    public String toString() {
        return toString(value);
    }

    /**
     * Returns a hash code for this {@code Integer}.
     *
     * @return  a hash code value for this object, equal to the
     *          primitive {@code int} value represented by this
     *          {@code Integer} object.
     */
    @Override
    public int hashCode() {
        return Integer.hashCode(value);
    }

    /**
     * Integer 的 hashCode 就是其本身
     *
     * @param value the value to hash
     * @since 1.8
     *
     * @return a hash code value for a {@code int} value.
     */
    public static int hashCode(int value) {
        return value;
    }

    /**
     * equals 比对的是值
     *
     * @param   obj   the object to compare with.
     * @return  {@code true} if the objects are the same;
     *          {@code false} otherwise.
     */
    public boolean equals(Object obj) {
        if (obj instanceof Integer) {
            return value == ((Integer)obj).intValue();
        }
        return false;
    }

    /**
     *
     *
     * @param   nm   property name.
     * @return  the {@code Integer} value of the property.
     * @throws  SecurityException for the same reasons as
     *          {@link System#getProperty(String) System.getProperty}
     * @see     java.lang.System#getProperty(java.lang.String)
     * @see     java.lang.System#getProperty(java.lang.String, java.lang.String)
     */
    public static Integer getInteger(String nm) {
        return getInteger(nm, null);
    }

    /**
     *
     *
     * @param   nm   property name.
     * @param   val   default value.
     * @return  the {@code Integer} value of the property.
     * @throws  SecurityException for the same reasons as
     *          {@link System#getProperty(String) System.getProperty}
     * @see     java.lang.System#getProperty(java.lang.String)
     * @see     java.lang.System#getProperty(java.lang.String, java.lang.String)
     */
    public static Integer getInteger(String nm, int val) {
        Integer result = getInteger(nm, null);
        return (result == null) ? Integer.valueOf(val) : result;
    }

    /**
     *
     *
     * @param   nm   property name.
     * @param   val   default value.
     * @return  the {@code Integer} value of the property.
     * @throws  SecurityException for the same reasons as
     *          {@link System#getProperty(String) System.getProperty}
     * @see     System#getProperty(java.lang.String)
     * @see     System#getProperty(java.lang.String, java.lang.String)
     */
    public static Integer getInteger(String nm, Integer val) {
        String v = null;
        try {
            v = System.getProperty(nm);
        } catch (IllegalArgumentException | NullPointerException e) {
        }
        if (v != null) {
            try {
                return Integer.decode(v);
            } catch (NumberFormatException e) {
            }
        }
        return val;
    }

    /**
     *
     *
     * @param     nm the {@code String} to decode.
     * @return    an {@code Integer} object holding the {@code int}
     *             value represented by {@code nm}
     * @exception NumberFormatException  if the {@code String} does not
     *            contain a parsable integer.
     * @see java.lang.Integer#parseInt(java.lang.String, int)
     */
    public static Integer decode(String nm) throws NumberFormatException {
        int radix = 10;
        int index = 0;
        boolean negative = false;
        Integer result;

        if (nm.length() == 0)
            throw new NumberFormatException("Zero length string");
        char firstChar = nm.charAt(0);
        // Handle sign, if present
        if (firstChar == '-') {
            negative = true;
            index++;
        } else if (firstChar == '+')
            index++;

        // Handle radix specifier, if present
        if (nm.startsWith("0x", index) || nm.startsWith("0X", index)) {
            index += 2;
            radix = 16;
        }
        else if (nm.startsWith("#", index)) {
            index ++;
            radix = 16;
        }
        else if (nm.startsWith("0", index) && nm.length() > 1 + index) {
            index ++;
            radix = 8;
        }

        if (nm.startsWith("-", index) || nm.startsWith("+", index))
            throw new NumberFormatException("Sign character in wrong position");

        try {
            result = Integer.valueOf(nm.substring(index), radix);
            result = negative ? Integer.valueOf(-result.intValue()) : result;
        } catch (NumberFormatException e) {
            // If number is Integer.MIN_VALUE, we'll end up here. The next line
            // handles this case, and causes any genuine format error to be
            // rethrown.
            String constant = negative ? ("-" + nm.substring(index))
                                       : nm.substring(index);
            result = Integer.valueOf(constant, radix);
        }
        return result;
    }

    /**
     *
     * @param   anotherInteger   the {@code Integer} to be compared.
     * @return  the value {@code 0} if this {@code Integer} is
     *          equal to the argument {@code Integer}; a value less than
     *          {@code 0} if this {@code Integer} is numerically less
     *          than the argument {@code Integer}; and a value greater
     *          than {@code 0} if this {@code Integer} is numerically
     *           greater than the argument {@code Integer} (signed
     *           comparison).
     * @since   1.2
     */
    public int compareTo(Integer anotherInteger) {
        return compare(this.value, anotherInteger.value);
    }

    /**
     *
     * @param  x the first {@code int} to compare
     * @param  y the second {@code int} to compare
     * @return the value {@code 0} if {@code x == y};
     *         a value less than {@code 0} if {@code x < y}; and
     *         a value greater than {@code 0} if {@code x > y}
     * @since 1.7
     */
    public static int compare(int x, int y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    /**
     *
     * @param  x the first {@code int} to compare
     * @param  y the second {@code int} to compare
     * @return the value {@code 0} if {@code x == y}; a value less
     *         than {@code 0} if {@code x < y} as unsigned values; and
     *         a value greater than {@code 0} if {@code x > y} as
     *         unsigned values
     * @since 1.8
     */
    public static int compareUnsigned(int x, int y) {
        return compare(x + MIN_VALUE, y + MIN_VALUE);
    }

    /**
     *
     * @param  x the value to convert to an unsigned {@code long}
     * @return the argument converted to {@code long} by an unsigned
     *         conversion
     * @since 1.8
     */
    public static long toUnsignedLong(int x) {
        return ((long) x) & 0xffffffffL;
    }

    /**
     *
     * @param dividend the value to be divided
     * @param divisor the value doing the dividing
     * @return the unsigned quotient of the first argument divided by
     * the second argument
     * @see #remainderUnsigned
     * @since 1.8
     */
    public static int divideUnsigned(int dividend, int divisor) {
        // In lieu of tricky code, for now just use long arithmetic.
        return (int)(toUnsignedLong(dividend) / toUnsignedLong(divisor));
    }

    /**
     *
     * @param dividend the value to be divided
     * @param divisor the value doing the dividing
     * @return the unsigned remainder of the first argument divided by
     * the second argument
     * @see #divideUnsigned
     * @since 1.8
     */
    public static int remainderUnsigned(int dividend, int divisor) {
        // In lieu of tricky code, for now just use long arithmetic.
        return (int)(toUnsignedLong(dividend) % toUnsignedLong(divisor));
    }


    // Bit twiddling

    /**
     * The number of bits used to represent an {@code int} value in two's
     * complement binary form.
     *
     * @since 1.5
     */
    @Native public static final int SIZE = 32;

    /**
     * The number of bytes used to represent a {@code int} value in two's
     * complement binary form.
     *
     * @since 1.8
     */
    public static final int BYTES = SIZE / Byte.SIZE;

    /**
     *
     * @param i the value whose highest one bit is to be computed
     * @return an {@code int} value with a single one-bit, in the position
     *     of the highest-order one-bit in the specified value, or zero if
     *     the specified value is itself equal to zero.
     * @since 1.5
     */
    public static int highestOneBit(int i) {
        // HD, Figure 3-1
        i |= (i >>  1);
        i |= (i >>  2);
        i |= (i >>  4);
        i |= (i >>  8);
        i |= (i >> 16);
        return i - (i >>> 1);
    }

    /**
     *
     * @param i the value whose lowest one bit is to be computed
     * @return an {@code int} value with a single one-bit, in the position
     *     of the lowest-order one-bit in the specified value, or zero if
     *     the specified value is itself equal to zero.
     * @since 1.5
     */
    public static int lowestOneBit(int i) {
        // HD, Section 2-1
        return i & -i;
    }

    /**
     *
     *
     * @param i the value whose number of leading zeros is to be computed
     * @return the number of zero bits preceding the highest-order
     *     ("leftmost") one-bit in the two's complement binary representation
     *     of the specified {@code int} value, or 32 if the value
     *     is equal to zero.
     * @since 1.5
     */
    public static int numberOfLeadingZeros(int i) {
        // HD, Figure 5-6
        if (i == 0)
            return 32;
        int n = 1;
        if (i >>> 16 == 0) { n += 16; i <<= 16; }
        if (i >>> 24 == 0) { n +=  8; i <<=  8; }
        if (i >>> 28 == 0) { n +=  4; i <<=  4; }
        if (i >>> 30 == 0) { n +=  2; i <<=  2; }
        n -= i >>> 31;
        return n;
    }

    /**
     *
     * @param i the value whose number of trailing zeros is to be computed
     * @return the number of zero bits following the lowest-order ("rightmost")
     *     one-bit in the two's complement binary representation of the
     *     specified {@code int} value, or 32 if the value is equal
     *     to zero.
     * @since 1.5
     */
    public static int numberOfTrailingZeros(int i) {
        // HD, Figure 5-14
        int y;
        if (i == 0) return 32;
        int n = 31;
        y = i <<16; if (y != 0) { n = n -16; i = y; }
        y = i << 8; if (y != 0) { n = n - 8; i = y; }
        y = i << 4; if (y != 0) { n = n - 4; i = y; }
        y = i << 2; if (y != 0) { n = n - 2; i = y; }
        return n - ((i << 1) >>> 31);
    }

    /**
     * Returns the number of one-bits in the two's complement binary
     * representation of the specified {@code int} value.  This function is
     * sometimes referred to as the <i>population count</i>.
     *
     * @param i the value whose bits are to be counted
     * @return the number of one-bits in the two's complement binary
     *     representation of the specified {@code int} value.
     * @since 1.5
     */
    public static int bitCount(int i) {
        // HD, Figure 5-2
        i = i - ((i >>> 1) & 0x55555555);
        i = (i & 0x33333333) + ((i >>> 2) & 0x33333333);
        i = (i + (i >>> 4)) & 0x0f0f0f0f;
        i = i + (i >>> 8);
        i = i + (i >>> 16);
        return i & 0x3f;
    }

    /**
     *
     *
     * @param i the value whose bits are to be rotated left
     * @param distance the number of bit positions to rotate left
     * @return the value obtained by rotating the two's complement binary
     *     representation of the specified {@code int} value left by the
     *     specified number of bits.
     * @since 1.5
     */
    public static int rotateLeft(int i, int distance) {
        return (i << distance) | (i >>> -distance);
    }

    /**
     *
     * @param i the value whose bits are to be rotated right
     * @param distance the number of bit positions to rotate right
     * @return the value obtained by rotating the two's complement binary
     *     representation of the specified {@code int} value right by the
     *     specified number of bits.
     * @since 1.5
     */
    public static int rotateRight(int i, int distance) {
        return (i >>> distance) | (i << -distance);
    }

    /**
     *
     * @param i the value to be reversed
     * @return the value obtained by reversing order of the bits in the
     *     specified {@code int} value.
     * @since 1.5
     */
    public static int reverse(int i) {
        // HD, Figure 7-1
        i = (i & 0x55555555) << 1 | (i >>> 1) & 0x55555555;
        i = (i & 0x33333333) << 2 | (i >>> 2) & 0x33333333;
        i = (i & 0x0f0f0f0f) << 4 | (i >>> 4) & 0x0f0f0f0f;
        i = (i << 24) | ((i & 0xff00) << 8) |
            ((i >>> 8) & 0xff00) | (i >>> 24);
        return i;
    }

    /**
     *
     * @param i the value whose signum is to be computed
     * @return the signum function of the specified {@code int} value.
     * @since 1.5
     */
    public static int signum(int i) {
        // HD, Section 2-7
        return (i >> 31) | (-i >>> 31);
    }

    /**
     *
     * @param i the value whose bytes are to be reversed
     * @return the value obtained by reversing the bytes in the specified
     *     {@code int} value.
     * @since 1.5
     */
    public static int reverseBytes(int i) {
        return ((i >>> 24)           ) |
               ((i >>   8) &   0xFF00) |
               ((i <<   8) & 0xFF0000) |
               ((i << 24));
    }

    /**
     *
     * @param a the first operand
     * @param b the second operand
     * @return the sum of {@code a} and {@code b}
     * @see java.util.function.BinaryOperator
     * @since 1.8
     */
    public static int sum(int a, int b) {
        return a + b;
    }

    /**
     *
     * @param a the first operand
     * @param b the second operand
     * @return the greater of {@code a} and {@code b}
     * @see java.util.function.BinaryOperator
     * @since 1.8
     */
    public static int max(int a, int b) {
        return Math.max(a, b);
    }

    /**
     *
     * @param a the first operand
     * @param b the second operand
     * @return the smaller of {@code a} and {@code b}
     * @see java.util.function.BinaryOperator
     * @since 1.8
     */
    public static int min(int a, int b) {
        return Math.min(a, b);
    }

    /** use serialVersionUID from JDK 1.0.2 for interoperability */
    @Native private static final long serialVersionUID = 1360826667806852920L;
}
