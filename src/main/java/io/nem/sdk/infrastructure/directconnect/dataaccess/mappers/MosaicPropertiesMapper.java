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

import io.nem.sdk.model.mosaic.MosaicProperties;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.math.BigInteger;
import java.util.function.Function;

/**
 * Mosaic properties mapper.
 */
public class MosaicPropertiesMapper implements Function<JsonObject, MosaicProperties> {
	/**
	 * Gets a flag value from a long.
	 *
	 * @param flag  Flags value.
	 * @param index Flag position.
	 * @return Flag value.
	 */
	boolean getFlag(final long flag, final int index) {
		return ((flag >>> index) & 1) == 1;
	}

  /**
   * Converts a json object to mosaic properties.
   *
   * @param jsonObject Json Object.
   * @return Mosaic properties.
   */
  @Override
  public MosaicProperties apply(final JsonObject jsonObject) {
    final JsonObject mosaicProperties = jsonObject.containsKey("properties") ? jsonObject.getJsonObject("properties") : jsonObject;
    final Long flags = mosaicProperties.getLong("flags");
    final int divisibility = mosaicProperties.getInteger("divisibility");
    final Long duration = mosaicProperties.getLong("duration");
    return MosaicProperties.create(
        getFlag(flags, 0),
        getFlag(flags, 1),
            divisibility,
        BigInteger.valueOf(duration));
  }
}
