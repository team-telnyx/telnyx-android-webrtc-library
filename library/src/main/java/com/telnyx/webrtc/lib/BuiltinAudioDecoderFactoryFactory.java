/*
 *  Copyright 2018 The WebRTC project authors. All Rights Reserved.
 *
 *  Use of this source code is governed by a BSD-style license
 *  that can be found in the LICENSE file in the root of the source
 *  tree. An additional intellectual property rights grant can be found
 *  in the file PATENTS.  All contributing project authors may
 *  be found in the AUTHORS file in the root of the source tree.
 */

package com.telnyx.webrtc.lib;

/**
 * Creates a native {@code webrtc::AudioDecoderFactory} with the builtin audio decoders.
 */
public class BuiltinAudioDecoderFactoryFactory implements AudioDecoderFactoryFactory {
  @Override
  public long createNativeAudioDecoderFactory() {
    return nativeCreateBuiltinAudioDecoderFactory();
  }

  private static native long nativeCreateBuiltinAudioDecoderFactory();
}
