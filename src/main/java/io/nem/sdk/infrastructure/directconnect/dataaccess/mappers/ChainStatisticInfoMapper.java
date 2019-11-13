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

import io.nem.sdk.model.blockchain.ChainStatisticInfo;
import io.vertx.core.json.JsonObject;
import java.math.BigInteger;
import java.util.function.Function;

/** Chain info mapper. */
public class ChainStatisticInfoMapper implements Function<JsonObject, ChainStatisticInfo> {
  /**
   * Converts a json object to block info.
   *
   * @param jsonObject Json object.
   * @return Chain info.
   */
  public ChainStatisticInfo apply(final JsonObject jsonObject) {
    final JsonObject chainStatisticJsonObject = jsonObject.getJsonObject("current");
    final BigInteger height = MapperUtils.extractBigInteger(chainStatisticJsonObject, "height");
    final BigInteger scoreHigh =
        MapperUtils.extractBigInteger(chainStatisticJsonObject, "scoreHigh");
    final BigInteger scoreLow = MapperUtils.extractBigInteger(chainStatisticJsonObject, "scoreLow");
    return ChainStatisticInfo.create(height, scoreHigh, scoreLow);
  }
}
