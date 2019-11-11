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

import io.nem.catapult.builders.*;
import io.nem.core.utils.ConvertUtils;
import io.nem.core.utils.MapperUtils;
import io.nem.core.utils.StringEncoder;
import io.nem.sdk.model.account.Address;
import io.nem.sdk.model.account.PublicAccount;
import io.nem.sdk.model.account.UnresolvedAddress;
import io.nem.sdk.model.blockchain.NetworkType;
import io.nem.sdk.model.mosaic.Mosaic;
import io.nem.sdk.model.mosaic.MosaicId;
import io.nem.sdk.model.mosaic.UnresolvedMosaicId;
import io.nem.sdk.model.namespace.NamespaceId;
import io.nem.sdk.model.transaction.TransactionType;
import org.apache.commons.codec.binary.Base32;
import org.apache.commons.lang3.Validate;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.stream.Collectors;

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
