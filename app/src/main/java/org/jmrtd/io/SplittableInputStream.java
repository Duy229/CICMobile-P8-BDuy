/*
 * JMRTD - A Java API for accessing machine readable travel documents.
 *
 * Copyright (C) 2006 - 2018  The JMRTD team
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
 *
 * $Id: SplittableInputStream.java 1808 2019-03-07 21:32:19Z martijno $
 */

package org.jmrtd.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * An input stream which will wrap another input stream (and yield the same bytes) and which can
 * spawn new fresh input stream copies (using {@link #getInputStream(int)})
 * (that also yield the same bytes).
 *
 * @author The JMRTD team (info@jmrtd.org)
 *
 * @version $Revision: 1808 $
 */
public class SplittableInputStream extends InputStream {

  private InputStreamBuffer inputStreamBuffer;
  private InputStreamBuffer.SubInputStream carrier;

  /**
   * Wraps an input stream so that copy streams can be split off.
   *
   * @param inputStream the original input stream
   * @param length the precise length of bytes that the original input stream provides
   */
  public SplittableInputStream(InputStream inputStream, int length) {
    this.inputStreamBuffer = new InputStreamBuffer(inputStream, length);
    this.carrier = inputStreamBuffer.getInputStream();
  }

  /**
   * Updates this stream's buffer based on some other stream's buffer.
   *
   * @param other the other stream
   */
  public void updateFrom(SplittableInputStream other) {
    inputStreamBuffer.updateFrom(other.inputStreamBuffer);
  }

  /**
   * Returns a copy of the inputstream positioned at <code>position</code>.
   *
   * @param position a position between <code>0</code> and {@link #getPosition()}
   *
   * @return a fresh input stream
   */
  public InputStream getInputStream(int position) {
    try {
      InputStream inputStream = inputStreamBuffer.getInputStream();
      long skippedBytes = 0L;
      while (skippedBytes < position) {
        skippedBytes += inputStream.skip(position - skippedBytes);
      }
      return inputStream;
    } catch (IOException ioe) {
      throw new IllegalStateException(ioe);
    }
  }

  /**
   * Returns the position within the input stream (the number of bytes read since this input stream was constructed).
   *
   * @return the position within this input stream
   */
  public int getPosition() {
    return carrier.getPosition();
  }

  /**
   * Reads the next byte of data from the input stream. The value byte is
   * returned as an <code>int</code> in the range <code>0</code> to
   * <code>255</code>. If no byte is available because the end of the stream
   * has been reached, the value <code>-1</code> is returned. This method
   * blocks until input data is available, the end of the stream is detected,
   * or an exception is thrown.
   *
   * @return     the next byte of data, or <code>-1</code> if the end of the
   *             stream is reached
   *
   * @throws IOException if an I/O error occurs
   */
  @Override
  public int read() throws IOException {
    return carrier.read();
  }

  /**
   * Skips over and discards <code>n</code> bytes of data from this input
   * stream. The <code>skip</code> method may, for a variety of reasons, end
   * up skipping over some smaller number of bytes, possibly <code>0</code>.
   * This may result from any of a number of conditions; reaching end of file
   * before <code>n</code> bytes have been skipped is only one possibility.
   * The actual number of bytes skipped is returned.  If <code>n</code> is
   * negative, no bytes are skipped.
   *
   * @param n the number of bytes to be skipped
   *
   * @return the actual number of bytes skipped
   *
   * @throws IOException if the stream does not support seek, or if some other I/O error occurs
   */
  @Override
  public long skip(long n) throws IOException {
    return carrier.skip(n);
  }

  /**
   * Returns an estimate of the number of bytes that can be read (or
   * skipped over) from this input stream without blocking by the next
   * invocation of a method for this input stream. The next invocation
   * might be the same thread or another thread.  A single read or skip of this
   * many bytes will not block, but may read or skip fewer bytes.
   *
   * @return an estimate of the number of bytes that can be read (or skipped
   *         over) from this input stream without blocking or <code>0</code> when
   *         it reaches the end of the input stream
   *
   * @throws IOException on error
   */
  @Override
  public int available() throws IOException {
    return carrier.available();
  }

  /**
   * Closes this input stream and releases any system resources associated
   * with the stream.
   *
   * @throws IOException on error
   */
  @Override
  public void close() throws IOException {
    carrier.close();
  }

  /**
   * Marks the current position in this input stream. A subsequent call to
   * the <code>reset</code> method repositions this stream at the last marked
   * position so that subsequent reads re-read the same bytes.
   *
   * <p>The <code>readlimit</code> arguments tells this input stream to
   * allow that many bytes to be read before the mark position gets
   * invalidated.</p>
   *
   * <p>The general contract of <code>mark</code> is that, if the method
   * <code>markSupported</code> returns <code>true</code>, the stream somehow
   * remembers all the bytes read after the call to <code>mark</code> and
   * stands ready to supply those same bytes again if and whenever the method
   * <code>reset</code> is called.  However, the stream is not required to
   * remember any data at all if more than <code>readlimit</code> bytes are
   * read from the stream before <code>reset</code> is called.</p>
   *
   * @param readlimit the maximum limit of bytes that can be read before the mark position becomes invalid
   *
   * @see InputStream#reset()
   */
  @Override
  public synchronized void mark(int readlimit) {
    carrier.mark(readlimit);
  }

  /**
   * Repositions this stream to the position at the time the
   * <code>mark</code> method was last called on this input stream.
   *
   * The general contract of <code>reset</code> is:
   *
   * <ul>
   *
   * <li> If the method <code>markSupported</code> returns
   * <code>true</code>, then:
   *
   *     <ul><li> If the method <code>mark</code> has not been called since
   *     the stream was created, or the number of bytes read from the stream
   *     since <code>mark</code> was last called is larger than the argument
   *     to <code>mark</code> at that last call, then an
   *     <code>IOException</code> might be thrown.
   *
   *     <li> If such an <code>IOException</code> is not thrown, then the
   *     stream is reset to a state such that all the bytes read since the
   *     most recent call to <code>mark</code> (or since the start of the
   *     file, if <code>mark</code> has not been called) will be resupplied
   *     to subsequent callers of the <code>read</code> method, followed by
   *     any bytes that otherwise would have been the next input data as of
   *     the time of the call to <code>reset</code>. </ul>
   *
   * <li> If the method <code>markSupported</code> returns
   * <code>false</code>, then:
   *
   *     <ul><li> The call to <code>reset</code> may throw an
   *     <code>IOException</code>.
   *
   *     <li> If an <code>IOException</code> is not thrown, then the stream
   *     is reset to a fixed state that depends on the particular type of the
   *     input stream and how it was created. The bytes that will be supplied
   *     to subsequent callers of the <code>read</code> method depend on the
   *     particular type of the input stream. </ul></ul>
   *
   * @throws IOException if this stream has not been marked or if the mark has been invalidated
   *
   * @see InputStream#mark(int)
   * @see IOException
   *
   * @throws IOException on error
   */
  @Override
  public synchronized void reset() throws IOException {
    carrier.reset();
  }

  /**
   * Tests if this input stream supports the <code>mark</code> and
   * <code>reset</code> methods. Whether or not <code>mark</code> and
   * <code>reset</code> are supported is an invariant property of a
   * particular input stream instance. The <code>markSupported</code> method
   * of <code>InputStream</code> returns <code>false</code>.
   *
   * @return <code>true</code> if this stream instance supports the mark
   *          and reset methods and <code>false</code> otherwise
   *
   * @see InputStream#mark(int)
   * @see InputStream#reset()
   */
  @Override
  public boolean markSupported() {
    return carrier.markSupported();
  }

  /**
   * Returns the length of the underlying buffer.
   *
   * @return the length of the underlying buffer
   */
  public int getLength() {
    return inputStreamBuffer.getLength();
  }

  /**
   * Returns the number of buffered bytes in the underlying buffer.
   *
   * @return the number of buffered bytes in the underlying buffer
   */
  public int getBytesBuffered() {
    return inputStreamBuffer.getBytesBuffered();
  }
}
