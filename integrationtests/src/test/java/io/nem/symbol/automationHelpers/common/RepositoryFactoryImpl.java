/*
 * Copyright (c) 2016-present,
 * Jaguar0625, gimre, BloodyRookie, Tech Bureau, Corp. All rights reserved.
 *
 * This file is part of Catapult.
 *
 * Catapult is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Catapult is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Catapult.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.nem.symbol.automationHelpers.common;

import io.nem.symbol.automationHelpers.config.ConfigFileReader;
import io.nem.symbol.core.crypto.PublicKey;
import io.nem.symbol.core.utils.ExceptionUtils;
import io.nem.symbol.sdk.api.RepositoryFactory;
import io.nem.symbol.sdk.infrastructure.common.CatapultContext;
import io.nem.symbol.sdk.infrastructure.directconnect.DirectConnectRepositoryFactoryImpl;
import io.nem.symbol.sdk.infrastructure.directconnect.dataaccess.common.DataAccessContext;
import io.nem.symbol.sdk.infrastructure.directconnect.dataaccess.database.mongoDb.BlocksCollection;
import io.nem.symbol.sdk.infrastructure.directconnect.dataaccess.database.mongoDb.FullBlockInfo;
import io.nem.symbol.sdk.infrastructure.directconnect.network.CatapultNodeContext;
import io.nem.symbol.sdk.infrastructure.vertx.RepositoryFactoryVertxImpl;
import io.nem.symbol.sdk.model.account.Account;

import java.util.HashMap;
import java.util.Map;

/** Repository factory */
public class RepositoryFactoryImpl {

  private static final Map<RepositoryFactoryType, RepositoryFactory> hashMap = new HashMap<>();
  final ConfigFileReader configFileReader;

  RepositoryFactoryImpl(final ConfigFileReader configFileReader) {
    this.configFileReader = configFileReader;
  }

  /**
   * Creates the repository factory object.
   *
   * @return Repository factory.
   */
  public RepositoryFactory create() {
    final RepositoryFactoryType repositoryFactoryType = configFileReader.getRepositoryFactoryType();
    return hashMap.computeIfAbsent(repositoryFactoryType, this::createRepositoryFactory);
  }

  private RepositoryFactory createRepositoryFactory(
      final RepositoryFactoryType repositoryFactoryType) {
    if (RepositoryFactoryType.DIRECT == repositoryFactoryType) {
      return createDirectRepositoryFactory();
    }

    if (RepositoryFactoryType.VERTX == repositoryFactoryType) {
      return createVertxRepositoryFactory();
    }

    throw new UnsupportedOperationException("Repository factory type not implemented.");
  }

  private RepositoryFactory createDirectRepositoryFactory() {
    final DataAccessContext dataAccessContext =
        new DataAccessContext(
            configFileReader.getMongodbHost(), configFileReader.getMongodbPort(), 0 /* timeout */);
    FullBlockInfo firstBlock =
        ExceptionUtils.propagate(() -> new BlocksCollection(dataAccessContext).find(1).get());

    final PublicKey apiServerPublicKey =
        PublicKey.fromHexString(configFileReader.getApiServerPublicKey());

    final String automationPrivateKey = configFileReader.getAutomationPrivateKey();
    final Account account =
        automationPrivateKey == null
            ? Account.generateNewAccount(firstBlock.getNetworkType())
            : Account.createFromPrivateKey(automationPrivateKey, firstBlock.getNetworkType());
    final CatapultNodeContext apiNodeContext =
        new CatapultNodeContext(
            apiServerPublicKey,
            account.getKeyPair(),
            configFileReader.getApiHost(),
            configFileReader.getApiPort(),
            configFileReader.getSocketTimeoutInMilliseconds());
    final CatapultContext catapultContext = new CatapultContext(apiNodeContext, dataAccessContext);
    return new DirectConnectRepositoryFactoryImpl(catapultContext);
  }

  private RepositoryFactory createVertxRepositoryFactory() {
    final String nodeUrl = configFileReader.getRestGatewayUrl();
    return new RepositoryFactoryVertxImpl(nodeUrl);
  }
}
