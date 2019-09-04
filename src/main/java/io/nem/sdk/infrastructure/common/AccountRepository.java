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

import io.nem.sdk.model.account.AccountInfo;
import io.nem.sdk.model.account.Address;
import io.nem.sdk.model.account.MultisigAccountInfo;
import io.nem.sdk.model.account.PublicAccount;
import io.nem.sdk.model.transaction.AggregateTransaction;
import io.nem.sdk.model.transaction.Transaction;
import io.reactivex.Observable;

import java.util.List;

/** Account interface repository. */
public interface AccountRepository {
  /**
   * Gets an AccountInfo for an account.
   *
   * @param address Address
   * @return Observable of {@link AccountInfo}
   */
  Observable<AccountInfo> getAccountInfo(Address address);

  /**
   * Gets a MultisigAccountInfo for an account.
   *
   * @param address Address
   * @return Observable of {@link MultisigAccountInfo}
   */
  Observable<MultisigAccountInfo> getMultisigAccountInfo(Address address);

  /**
   * Gets an list of transactions for which an account is the sender or has sign the transaction. A
   * transaction is said to be aggregate bonded with respect to an account if there are missing
   * signatures.
   *
   * @param publicAccount PublicAccount
   * @return Observable of List<{@link Transaction}>
   */
  Observable<List<AggregateTransaction>> aggregateBondedTransactions(PublicAccount publicAccount);

  /**
   * Gets the list of transactions for which an account is the sender or receiver and which have not
   * yet been included in a block. Unconfirmed transactions are those transactions that have not yet
   * been included in a block. Unconfirmed transactions are not guaranteed to be included in any
   * block.
   *
   * @param publicAccount PublicAccount
   * @return Observable of List<{@link Transaction}>
   */
  Observable<List<Transaction>> unconfirmedTransactions(PublicAccount publicAccount);
}
