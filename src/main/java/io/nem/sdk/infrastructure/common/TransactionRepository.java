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

import io.nem.sdk.model.transaction.*;
import io.reactivex.Observable;

/**
 * Transaction interface repository.
 */
public interface TransactionRepository {

	/**
	 * Gets a transaction for a given hash.
	 *
	 * @param transactionHash String
	 * @return Observable of {@link Transaction}
	 */
	Observable<Transaction> getTransaction(String transactionHash);

	/**
	 * Gets a transaction status for a transaction hash.
	 *
	 * @param transactionHash String
	 * @return Observable of {@link TransactionStatus}
	 */
	Observable<TransactionStatus> getTransactionStatus(String transactionHash);

	/**
	 * Send a signed transaction.
	 *
	 * @param signedTransaction SignedTransaction
	 * @return Observable of TransactionAnnounceResponse
	 */
	Observable<TransactionAnnounceResponse> announce(SignedTransaction signedTransaction);

	/**
	 * Send a signed transaction with missing signatures.
	 *
	 * @param signedTransaction SignedTransaction
	 * @return Observable of TransactionAnnounceResponse
	 */
	Observable<TransactionAnnounceResponse> announceAggregateBonded(SignedTransaction signedTransaction);

	/**
	 * Send a cosignature signed transaction of an already announced transaction.
	 *
	 * @param cosignatureSignedTransaction CosignatureSignedTransaction
	 * @return Observable of TransactionAnnounceResponse
	 */
	Observable<TransactionAnnounceResponse> announceAggregateBondedCosignature(CosignatureSignedTransaction cosignatureSignedTransaction);
}
