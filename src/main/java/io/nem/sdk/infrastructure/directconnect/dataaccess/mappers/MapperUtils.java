/**
 * ** Copyright (c) 2016-present,
 * ** Jaguar0625, gimre, BloodyRookie, Tech Bureau, Corp. All rights reserved.
 * **
 * ** This file is part of Catapult.
 * **
 * ** Catapult is free software: you can redistribute it and/or modify
 * ** it under the terms of the GNU Lesser General Public License as published by
 * ** the Free Software Foundation, either version 3 of the License, or
 * ** (at your option) any later version.
 * **
 * ** Catapult is distributed in the hope that it will be useful,
 * ** but WITHOUT ANY WARRANTY; without even the implied warranty of
 * ** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * ** GNU Lesser General Public License for more details.
 * **
 * ** You should have received a copy of the GNU Lesser General Public License
 * ** along with Catapult. If not, see <http://www.gnu.org/licenses/>.
 **/

package io.nem.sdk.infrastructure.directconnect.dataaccess.mappers;

import io.nem.sdk.model.mosaic.MosaicFlags;
import io.nem.sdk.model.receipt.ReceiptSource;
import io.nem.sdk.model.receipt.ReceiptType;
import io.nem.sdk.model.receipt.ResolutionEntry;
import io.nem.sdk.model.receipt.ResolutionStatement;
import io.vertx.core.json.JsonObject;

import java.math.BigInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Mapper utils.
 */
final class MapperUtils {
	/**
	 * Gets a Biginteger value from json.
	 *
	 * @param jsonObject Json object.
	 * @param name       Name of the property.
	 * @return BigInteger of the name.¬
	 */
	public static BigInteger extractBigInteger(final JsonObject jsonObject, final String name) {
		return BigInteger.valueOf(jsonObject.getLong(name));
	}

	/**
	 *
	 * @param receiptJsonObject
	 * @param getUnresolvedEntry
	 * @param getResolvedEntry
	 * @param receiptType
	 * @param <T>
	 * @return
	 */
	public static <T> ResolutionStatement<T> createResolutionStatement(final JsonObject receiptJsonObject,
																	   final Function<JsonObject, T> getUnresolvedEntry,
																	   final Function<JsonObject, T> getResolvedEntry,
																	   final ReceiptType receiptType) {
		return new ResolutionStatement<T>(
				extractBigInteger(receiptJsonObject, "height"),
				getUnresolvedEntry.apply(receiptJsonObject),
				receiptJsonObject.getJsonArray("resolutionEntries").stream()
						.map(
								entry -> {
									final JsonObject entryJsonObject = (JsonObject) entry;
									final JsonObject sourceJsonObject = entryJsonObject.getJsonObject("source");
									return new ResolutionEntry<>(
											getResolvedEntry.apply(entryJsonObject),
											new ReceiptSource(
													sourceJsonObject.getInteger("primaryId"),
													sourceJsonObject.getInteger("secondaryId")),
											receiptType);
								}).collect(Collectors.toList()));
	}
}
