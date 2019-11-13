/**
 * ** Copyright (c) 2016-present, ** Jaguar0625, gimre, BloodyRookie, Tech Bureau, Corp. All rights
 * reserved. ** ** This file is part of Catapult. ** ** Catapult is free software: you can
 * redistribute it and/or modify ** it under the terms of the GNU Lesser General Public License as
 * published by ** the Free Software Foundation, either version 3 of the License, or ** (at your
 * option) any later version. ** ** Catapult is distributed in the hope that it will be useful, **
 * but WITHOUT ANY WARRANTY; without even the implied warranty of ** MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the ** GNU Lesser General Public License for more details. ** ** You
 * should have received a copy of the GNU Lesser General Public License ** along with Catapult. If
 * not, see <http://www.gnu.org/licenses/>.
 **/

package io.nem.sdk.infrastructure.directconnect.dataaccess.mappers;

import io.nem.sdk.model.transaction.Deadline;
import io.nem.sdk.model.transaction.TransactionStatusError;
import io.vertx.core.json.JsonObject;
import java.math.BigInteger;
import java.util.function.Function;

/** Transaction status error mapper */
public class TransactionStatusErrorMapper implements Function<JsonObject, TransactionStatusError> {

    /**
     * Create a transaction status object from json.
     *
     * @param jsonObject Json object.
     * @return Transaction status object.
     */
    public TransactionStatusError apply(final JsonObject jsonObject) {
        final JsonObject jsonStatusObject = jsonObject.getJsonObject("status");
        return new TransactionStatusError(
            null,
            jsonStatusObject.getString("hash"),
            TransactionStatusCode.rawValueOf(jsonStatusObject.getInteger("code")).toString(),
            new Deadline(BigInteger.valueOf(jsonStatusObject.getLong("deadline"))));
    }
}
