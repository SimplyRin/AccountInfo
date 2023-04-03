/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.simplyrin.accountinfo.commonsio;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

public class IOUtils {

	/**
	 * The default buffer size ({@value}) to use in copy methods.
	 */
	public static final int DEFAULT_BUFFER_SIZE = 8192;

	/**
	 * Internal byte array buffer.
	 */
	private static final ThreadLocal<char[]> SKIP_CHAR_BUFFER = ThreadLocal.withInitial(IOUtils::charArray);

	/**
	 * Represents the end-of-file (or stream).
	 * @since 2.5 (made public)
	 */
	public static final int EOF = -1;

	/**
	 * Returns a new char array of size {@link #DEFAULT_BUFFER_SIZE}.
	 *
	 * @return a new char array of size {@link #DEFAULT_BUFFER_SIZE}.
	 * @since 2.9.0
	 */
	private static char[] charArray() {
		return charArray(DEFAULT_BUFFER_SIZE);
	}

	/**
	 * Returns a new char array of the given size.
	 *
	 * TODO Consider guarding or warning against large allocations...
	 *
	 * @param size array size.
	 * @return a new char array of the given size.
	 * @since 2.9.0
	 */
	private static char[] charArray(final int size) {
		return new char[size];
	}

	/**
	 * Gets the contents of an {@code InputStream} as a String
	 * using the specified character encoding.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * {@code BufferedInputStream}.
	 * </p>
	 *
	 * @param input the {@code InputStream} to read from
	 * @param charset the charset to use, null means platform default
	 * @return the requested String
	 * @throws NullPointerException if the input is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.3
	 */
	public static String toString(final InputStream input, final Charset charset) throws IOException {
		try (final StringBuilderWriter sw = new StringBuilderWriter()) {
			copy(input, sw, charset);
			return sw.toString();
		}
	}

	/**
	 * Copies bytes from an {@code InputStream} to chars on a
	 * {@code Writer} using the specified character encoding.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * {@code BufferedInputStream}.
	 * </p>
	 * <p>
	 * This method uses {@link InputStreamReader}.
	 * </p>
	 *
	 * @param input the {@code InputStream} to read from
	 * @param writer the {@code Writer} to write to
	 * @param inputCharset the charset to use for the input stream, null means platform default
	 * @throws NullPointerException if the input or output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.3
	 */
	public static void copy(final InputStream input, final Writer writer, final Charset inputCharset)
			throws IOException {
		final InputStreamReader reader = new InputStreamReader(input, toCharset(inputCharset));
		copy(reader, writer);
	}

	/**
	 * Copies chars from a {@code Reader} to a {@code Writer}.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * {@code BufferedReader}.
	 * <p>
	 * Large streams (over 2GB) will return a chars copied value of
	 * {@code -1} after the copy has completed since the correct
	 * number of chars cannot be returned as an int. For large streams
	 * use the {@code copyLarge(Reader, Writer)} method.
	 *
	 * @param reader the {@code Reader} to read.
	 * @param writer the {@code Writer} to write.
	 * @return the number of characters copied, or -1 if &gt; Integer.MAX_VALUE
	 * @throws NullPointerException if the input or output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 1.1
	 */
	public static int copy(final Reader reader, final Writer writer) throws IOException {
		final long count = copyLarge(reader, writer);
		if (count > Integer.MAX_VALUE) {
			return EOF;
		}
		return (int) count;
	}
    
	/**
	 * Copies chars from a large (over 2GB) {@code Reader} to a {@code Writer}.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * {@code BufferedReader}.
	 * <p>
	 * The buffer size is given by {@link #DEFAULT_BUFFER_SIZE}.
	 *
	 * @param reader the {@code Reader} to source.
	 * @param writer the {@code Writer} to target.
	 * @return the number of characters copied
	 * @throws NullPointerException if the input or output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 1.3
	 */
	public static long copyLarge(final Reader reader, final Writer writer) throws IOException {
		return copyLarge(reader, writer, getCharArray());
	}

	/**
	 * Copies chars from a large (over 2GB) {@code Reader} to a {@code Writer}.
	 * <p>
	 * This method uses the provided buffer, so there is no need to use a
	 * {@code BufferedReader}.
	 * <p>
	 *
	 * @param reader the {@code Reader} to source.
	 * @param writer the {@code Writer} to target.
	 * @param buffer the buffer to be used for the copy
	 * @return the number of characters copied
	 * @throws NullPointerException if the input or output is null
	 * @throws IOException          if an I/O error occurs
	 * @since 2.2
	 */
	public static long copyLarge(final Reader reader, final Writer writer, final char[] buffer) throws IOException {
		long count = 0;
		int n;
		while (EOF != (n = reader.read(buffer))) {
			writer.write(buffer, 0, n);
			count += n;
		}
		return count;
	}

	/**
	 * Copies bytes from an {@code InputStream} to chars on a
	 * {@code Writer} using the specified character encoding.
	 * <p>
	 * This method buffers the input internally, so there is no need to use a
	 * {@code BufferedInputStream}.
	 * </p>
	 * <p>
	 * Character encoding names can be found at
	 * <a href="http://www.iana.org/assignments/character-sets">IANA</a>.
	 * </p>
	 * <p>
	 * This method uses {@link InputStreamReader}.
	 * </p>
	 *
	 * @param input the {@code InputStream} to read from
	 * @param writer the {@code Writer} to write to
	 * @param inputCharsetName the name of the requested charset for the InputStream, null means platform default
	 * @throws NullPointerException                         if the input or output is null
	 * @throws IOException                                  if an I/O error occurs
	 * @throws java.nio.charset.UnsupportedCharsetException thrown instead of {@link java.io
	 *                                                      .UnsupportedEncodingException} in version 2.2 if the
	 *                                                      encoding is not supported.
	 * @since 1.1
	 */
    public static void copy(final InputStream input, final Writer writer, final String inputCharsetName)
    		throws IOException {
    	copy(input, writer, toCharset(inputCharsetName));
	}

	/**
	 * Gets the thread local char array.
	 *
	 * @return the thread local char array.
	 */
	static char[] getCharArray() {
		return SKIP_CHAR_BUFFER.get();
	}

	/**
	 * Returns a Charset for the named charset. If the name is null, return the default Charset.
	 *
	 * @param charsetName The name of the requested charset, may be null.
	 * @return a Charset for the named charset.
	 * @throws UnsupportedCharsetException If the named charset is unavailable (unchecked exception).
	 */
	public static Charset toCharset(final String charsetName) throws UnsupportedCharsetException {
		return charsetName == null ? Charset.defaultCharset() : Charset.forName(charsetName);
	}

	/**
	 * Returns the given Charset or the default Charset if the given Charset is null.
	 *
	 * @param charset
	 *            A charset or null.
	 * @return the given Charset or the default Charset if the given Charset is null
	 */
	public static Charset toCharset(final Charset charset) {
		return charset == null ? Charset.defaultCharset() : charset;
	}

}
