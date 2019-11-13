/*
 * Copyright 2019 NEM
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nem.sdk.infrastructure;

import io.nem.sdk.model.account.Address;
import java.nio.ByteBuffer;
import org.apache.commons.codec.binary.Base32;

/**
 * Utility class used to serialize/deserialize catbuffer values.
 */
public class SerializationExtUtils {

    /**
     * Private constructor.
     */
    private SerializationExtUtils() {
    }

    public static ByteBuffer fromAddressToByteBuffer(final Address address) {
        return ByteBuffer.wrap(new Base32().decode((address.plain())));
    }

}
