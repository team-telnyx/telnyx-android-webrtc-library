/*
 *  Copyright (c) 2017 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.telnyx.webrtc.lib;

public class LibvpxVp9Decoder extends WrappedNativeVideoDecoder {
  @Override
  public long createNative(long webrtcEnvRef) {
    return nativeCreateDecoder();
  }

  static native long nativeCreateDecoder();

  static native boolean nativeIsSupported();
}
