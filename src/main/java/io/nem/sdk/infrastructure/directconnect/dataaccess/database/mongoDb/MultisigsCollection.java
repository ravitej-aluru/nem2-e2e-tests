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

package io.nem.sdk.infrastructure.directconnect.dataaccess.database.mongoDb;

import io.nem.sdk.infrastructure.common.CatapultContext;
import io.nem.sdk.infrastructure.directconnect.dataaccess.mappers.MultisigAccountInfoMapper;
import io.nem.sdk.model.account.MultisigAccountInfo;

import java.util.Optional;

/**
 * Multisigs collection.
 */
public class MultisigsCollection {
	/* Catapult context. */
	final CatapultContext context;
	/**
	 * Catapult collection.
	 */
	final private CatapultCollection<MultisigAccountInfo, MultisigAccountInfoMapper> catapultCollection;

	/**
	 * Constructor.
	 *
	 * @param context Catapult context.
	 */
	public MultisigsCollection(final CatapultContext context) {
		catapultCollection = new CatapultCollection<>(context.getCatapultMongoDbClient(), "Multisigs", MultisigAccountInfoMapper::new);
		this.context = context;
	}

	/**
	 * Get Multisig account info.
	 *
	 * @param address Account address.
	 * @return Multisig account info.
	 */
	public Optional<MultisigAccountInfo> findByAddress(final byte[] address) {
		final String keyName = "multisig.accountAddress";
		return catapultCollection.findOne(keyName, address, context.getDatabaseTimeoutInSeconds());
	}
}