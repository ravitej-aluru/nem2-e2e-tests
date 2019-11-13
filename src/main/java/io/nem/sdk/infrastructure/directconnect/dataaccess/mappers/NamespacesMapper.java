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

import io.nem.sdk.model.account.Address;
import io.nem.sdk.model.account.PublicAccount;
import io.nem.sdk.model.mosaic.MosaicId;
import io.nem.sdk.model.namespace.AddressAlias;
import io.nem.sdk.model.namespace.Alias;
import io.nem.sdk.model.namespace.AliasType;
import io.nem.sdk.model.namespace.EmptyAlias;
import io.nem.sdk.model.namespace.MosaicAlias;
import io.nem.sdk.model.namespace.NamespaceId;
import io.nem.sdk.model.namespace.NamespaceInfo;
import io.nem.sdk.model.namespace.NamespaceRegistrationType;
import io.vertx.core.json.JsonObject;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/** Namespace mapper. */
public class NamespacesMapper implements Function<JsonObject, NamespaceInfo> {
  /**
   * Creates a namespace info object from json.
   *
   * @param jsonObject Json object.
   * @return namespace info.
   */
  public NamespaceInfo apply(final JsonObject jsonObject) {
    final JsonObject metaJsonObject = jsonObject.getJsonObject("meta");
    final boolean active = metaJsonObject.getBoolean("active");
    final Integer index = metaJsonObject.getInteger("index");
    final JsonObject namespaceJsonObject = jsonObject.getJsonObject("namespace");
    final String metaId = "";
    final NamespaceRegistrationType type =
            NamespaceRegistrationType.rawValueOf(namespaceJsonObject.getInteger("registrationType"));
    final Integer depth = namespaceJsonObject.getInteger("depth");
    final List<NamespaceId> levels = new ArrayList<>(depth);
    for (int i = 0; i < depth; i++) {
      levels.add(NamespaceId.createFromId(MapperUtils.extractBigInteger(namespaceJsonObject, "level" + i)));
    }
    final NamespaceId parentId =
            NamespaceId.createFromId(MapperUtils.extractBigInteger(namespaceJsonObject, "parentId"));
    final Address address =
        Address.createFromEncoded(namespaceJsonObject.getString("ownerAddress"));
    final PublicAccount owner =
        new PublicAccount(
            namespaceJsonObject.getString("ownerPublicKey"), address.getNetworkType());
    final BigInteger startHeight =
        MapperUtils.extractBigInteger(namespaceJsonObject, "startHeight");
    final BigInteger endHeight = MapperUtils.extractBigInteger(namespaceJsonObject, "endHeight");
    final Alias alias = getAlias(namespaceJsonObject);
    return new NamespaceInfo(
        active, index, metaId, type, depth, levels, parentId, owner, startHeight, endHeight, alias);
  }

  /**
   * Gets the alias if present
   *
   * @param jsonObject Json object.
   * @return Alias.
   */
  public Alias getAlias(final JsonObject jsonObject) {
    final JsonObject aliasObject = jsonObject.getJsonObject("alias");
    final AliasType aliasType = AliasType.rawValueOf(aliasObject.getInteger("type"));
    switch (aliasType) {
      case NONE:
        return new EmptyAlias();
      case ADDRESS:
        final Address address = Address.createFromEncoded(aliasObject.getString("address"));
        return new AddressAlias(address);
      case MOSAIC:
        final MosaicId mosaicId =
            new MosaicId(MapperUtils.extractBigInteger(aliasObject, "mosaicId"));
        return new MosaicAlias(mosaicId);
    }
    throw new IllegalStateException("Alias factory was not found.");
  }
}
