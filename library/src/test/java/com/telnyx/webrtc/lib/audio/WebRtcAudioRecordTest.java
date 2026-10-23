package com.telnyx.webrtc.lib.audio;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Unit tests for the pacing math used by the recordingless branch of
 * AudioRecordThread (setUseAudioRecord(false), or a failed recorder
 * init). These run on the host JVM and cover the pure helper only:
 * Android AudioRecord behavior cannot be exercised here.
 */
public class WebRtcAudioRecordTest {
  private static final long NANOS_PER_MS = 1_000_000L;

  @Test
  public void parkZeroWhenExactlyOnDeadline() {
    // One full interval since the last tick -> no park needed.
    assertEquals(0L, WebRtcAudioRecord.silenceLoopParkNanos(0L, 10 * NANOS_PER_MS));
  }

  @Test
  public void parkRemainderWhenHalfwayToDeadline() {
    // Half an interval elapsed -> park the remaining half.
    assertEquals(5 * NANOS_PER_MS,
        WebRtcAudioRecord.silenceLoopParkNanos(0L, 5 * NANOS_PER_MS));
  }

  @Test
  public void parkNeverExceedsOneInterval() {
    // Clamped from above: even a stale tick never parks more than one
    // interval, so a stop is never delayed by more than ~10 ms.
    assertEquals(10 * NANOS_PER_MS,
        WebRtcAudioRecord.silenceLoopParkNanos(5 * NANOS_PER_MS, 0L));
  }

  @Test
  public void parkZeroWhenLongOverdue() {
    // Several intervals late (slow previous iteration): never tries to
    // catch up, and never parks a negative amount.
    assertEquals(0L, WebRtcAudioRecord.silenceLoopParkNanos(0L, 42 * NANOS_PER_MS));
  }
}
