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
import io.nem.sdk.model.account.MultisigAccountInfo;
import io.nem.sdk.model.account.PublicAccount;
import io.nem.sdk.model.blockchain.NetworkType;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Multisigs account mapper.
 */
public class MultisigAccountInfoMapper implements Function<JsonObject, MultisigAccountInfo> {
	/**
	 * Gets public accounts from json.
	 *
	 * @param jsonObject  Json object.
	 * @param keyName     Key name.
	 * @param networkType Network type.
	 * @return Public account.
	 */
	private List<PublicAccount> getPublicAccounts(final JsonObject jsonObject, final String keyName, final NetworkType networkType) {
		return jsonObject.getJsonArray(keyName).stream().map(s -> new PublicAccount(jsonObject.getString("account"), networkType)).collect(
				Collectors.toList());
	}

  /**
   * Create a multisig account info object from json.
   *
   * @param jsonObject Json object.
   * @return Multisig account info.
   */
  public MultisigAccountInfo apply(final JsonObject jsonObject) {
    final JsonObject multisigJsonObject = jsonObject.getJsonObject("multisig");
    final Address address =
        Address.createFromEncoded(multisigJsonObject.getString("accountAddress"));
    final PublicAccount account =
        new PublicAccount(multisigJsonObject.getString("account"), address.getNetworkType());
    final byte minApproval = multisigJsonObject.getInteger("minApproval").byteValue();
    final byte minRemoval = multisigJsonObject.getInteger("minRemoval").byteValue();
    final List<PublicAccount> cosignatories =
        getPublicAccounts(multisigJsonObject, "cosignatories", address.getNetworkType());
    final List<PublicAccount> multisigAccounts =
        getPublicAccounts(multisigJsonObject, "multisigAccounts", address.getNetworkType());
    return new MultisigAccountInfo(
        account, minApproval, minRemoval, cosignatories, multisigAccounts);
  }
}
