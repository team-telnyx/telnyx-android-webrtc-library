/*
 *  Copyright 2017 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.telnyx.webrtc.lib;

/** Interface for receiving stats reports (see webrtc::RTCStatsCollectorCallback). */
public interface RTCStatsCollectorCallback {
  /** Called when the stats report is ready. */
  @CalledByNative public void onStatsDelivered(RTCStatsReport report);
}
