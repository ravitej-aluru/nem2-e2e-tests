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

package io.nem.sdk.infrastructure.common;

import io.nem.sdk.model.account.Address;
import io.nem.sdk.model.mosaic.MosaicId;
import io.nem.sdk.model.namespace.NamespaceId;
import io.nem.sdk.model.namespace.NamespaceInfo;
import io.reactivex.Observable;

import java.util.List;

/**
 * Namespace interface repository.
 *
 * @since 1.0
 */
public interface NamespaceRepository {

	/**
	 * Gets the NamespaceInfo for a given namespaceId.
	 *
	 * @param namespaceId NamespaceId
	 * @return Observable of {@link NamespaceInfo}
	 */
	Observable<NamespaceInfo> getNamespace(NamespaceId namespaceId);

	/**
	 * Gets list of NamespaceInfo for an account.
	 *
	 * @param address Address
	 * @return Observable of List<{@link NamespaceInfo}>
	 */
	Observable<List<NamespaceInfo>> getNamespacesFromAccount(Address address);

	/**
	 * Gets the MosaicId from a MosaicAlias
	 *
	 * @param namespaceId - the namespaceId of the namespace
	 * @return Observable of <{@link MosaicId}>
	 */
	Observable<MosaicId> getLinkedMosaicId(NamespaceId namespaceId);

	/**
	 * Gets the Address from a AddressAlias
	 *
	 * @param namespaceId - the namespaceId of the namespace
	 * @return Observable of <{@link Address}>
	 */
	Observable<Address> getLinkedAddress(NamespaceId namespaceId);
}
