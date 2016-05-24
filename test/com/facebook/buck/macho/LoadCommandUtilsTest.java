/*
 * Copyright 2016-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.facebook.buck.macho;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import com.google.common.base.Function;
import com.google.common.io.BaseEncoding;
import com.google.common.primitives.UnsignedInteger;

import org.junit.Test;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class LoadCommandUtilsTest {
  @Test
  public void testEnumeratingLoadCommands() throws Exception {
    MachoMagicInfo info = new MachoMagicInfo(UnsignedInteger.fromIntBits(0xFEEDFACF));
    byte[] header = MachoHeaderTestData.getBigEndian64Bit();
    header[19] = 2;   // ncmds
    header[23] = 16;  // sizeofcmds

    byte[] commandBytes = BaseEncoding.base16().decode(
        "000000AA00000008" +      // command 1 is AA, size 8
        "000000BB00000008");      // command 2 is BB, size 8

    ByteBuffer buffer = ByteBuffer
        .allocate(MachoHeaderTestData.getBigEndian64Bit().length + commandBytes.length)
        .put(header)
        .put(commandBytes)
        .order(ByteOrder.BIG_ENDIAN);

    final List<LoadCommand> result = new ArrayList<>();
    buffer.position(0);
    LoadCommandUtils.enumerateLoadCommandsInFile(
        buffer,
        info,
        new Function<LoadCommand, Boolean>() {
          @Override
          public Boolean apply(LoadCommand input) {
            result.add(input);
            return Boolean.TRUE;
          }
        });

    assertThat(result.size(), equalTo(2));

    assertThat(result.get(0).getCmd(), equalTo(UnsignedInteger.fromIntBits(0xAA)));
    assertThat(result.get(0).getCmdsize(), equalTo(UnsignedInteger.fromIntBits(0x08)));

    assertThat(result.get(1).getCmd(), equalTo(UnsignedInteger.fromIntBits(0xBB)));
    assertThat(result.get(1).getCmdsize(), equalTo(UnsignedInteger.fromIntBits(0x08)));
  }
}