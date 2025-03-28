/*
 *  Copyright 2019 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.telnyx.webrtc.lib;

/**
 * Implementations of this interface can create a native {@code webrtc::NetEqFactory}.
 */
public interface NetEqFactoryFactory {
  /**
   * Returns a pointer to a {@code webrtc::NetEqFactory}. The caller takes ownership.
   */
  long createNativeNetEqFactory();
}
