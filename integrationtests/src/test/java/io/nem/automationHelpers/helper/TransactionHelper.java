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

package io.nem.automationHelpers.helper;

import io.nem.automationHelpers.common.TestContext;
import io.nem.core.utils.ExceptionUtils;
import io.nem.core.utils.RetryCommand;
import io.nem.sdk.infrastructure.common.TransactionRepository;
import io.nem.sdk.infrastructure.directconnect.dataaccess.dao.AccountsDao;
import io.nem.sdk.infrastructure.directconnect.dataaccess.dao.TransactionDao;
import io.nem.sdk.infrastructure.directconnect.dataaccess.database.mongoDb.PartialTransactionsCollection;
import io.nem.sdk.model.account.Account;
import io.nem.sdk.model.account.PublicAccount;
import io.nem.sdk.model.transaction.*;

import java.math.BigInteger;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Transaction helper.
 */
public class TransactionHelper {
	final TestContext testContext;

	/**
	 * Constructor.
	 *
	 * @param testContext Test context.
	 */
	public TransactionHelper(final TestContext testContext) {
		this.testContext = testContext;
	}

	/**
	 * Gets the default deadline.
	 *
	 * @return Default deadline.
	 */
	public static Deadline getDefaultDeadline() {
		return Deadline.create(2, ChronoUnit.HOURS);
	}

	/**
	 * Gets default max fee.
	 *
	 * @return Max fee.
	 */
	public static BigInteger getDefaultMaxFee() {
		return BigInteger.valueOf(0);
	}

	/**
	 * Signs the transaction.
	 *
	 * @param transaction    Transaction to sign.
	 * @param account        Account to sign the transaction.
	 * @param generationHash Generation hash
	 * @return Signed Transaction.
	 */
	public SignedTransaction signTransaction(
			final Transaction transaction, final Account account, final String generationHash) {
		final SignedTransaction signedTransaction = account.sign(transaction, generationHash);
		//testContext.addTransaction(transaction);
		testContext.setSignedTransaction(signedTransaction);
		return signedTransaction;
	}

	/**
	 * Signs the transaction.
	 *
	 * @param transaction Transaction to sign.
	 * @param account     Account to sign the transaction.
	 * @return Signed Transaction.
	 */
	public SignedTransaction signTransaction(final Transaction transaction, final Account account) {
		return signTransaction(
				transaction, account, testContext.getGenerationHash());
	}

	/**
	 * Get the transaction by the hash.
	 *
	 * @param hash Transaction hash.
	 * @param <T>  Transaction type.
	 * @return Transaction.
	 */
	public <T extends Transaction> T getTransaction(final String hash) {
		return ExceptionUtils.propagate(
				() ->
						(T)
								new TransactionDao(testContext.getCatapultContext())
										.getTransaction(hash)
										.toFuture()
										.get());
	}

	/**
	 * Gets the transaction status(failed, success)
	 *
	 * @param hash Transaction hash.
	 * @return Transaction status.
	 */
	public TransactionStatus getTransactionStatus(final String hash) {
		return ExceptionUtils.propagate(
				() ->
						new TransactionDao(testContext.getCatapultContext())
								.getTransactionStatus(hash)
								.toFuture()
								.get());
	}

	/**
	 * Gets the transaction status(failed, success) no throw
	 *
	 * @param hash Transaction hash.
	 * @return Transaction status.
	 */
	public TransactionStatus getTransactionStatusNoThrow(final String hash) {
		try {
			return getTransactionStatus(hash);
		}
		catch (Exception e) {
			return new TransactionStatus(
					"Failed",
					e.getMessage(),
					hash,
					Deadline.create(0, ChronoUnit.HOURS),
					BigInteger.valueOf(0));
		}
	}

	/**
	 * Waits for a transaction to complete.
	 *
	 * @param signedTransaction Signed transaction to wait for.
	 * @param <T>               Transaction type.
	 * @return Transaction of type T.
	 */
	public <T extends Transaction> T waitForTransactionToComplete(
			final SignedTransaction signedTransaction) {
		return waitForTransaction(signedTransaction, (final String hash) -> getTransaction(hash));
	}

	/**
	 * Waits for a transaction to show in the pt cache.
	 *
	 * @param signedTransaction Signed transaction to wait for.
	 * @return Transaction of type T.
	 */
	public AggregateTransaction waitForBondedTransaction(
			final SignedTransaction signedTransaction) {
		return waitForTransaction(signedTransaction, (final String hash) -> getBondedTransaction(hash));
	}

	/**
	 * Waits for a specific transaction.
	 *
	 * @param signedTransaction Signed transaction to wait for.
	 * @param getTransaction    Function to get the transaction.
	 * @param <T>               Transaction type.
	 * @return Transaction if found.
	 */
	public <T extends Transaction> T waitForTransaction(final SignedTransaction signedTransaction,
														final Function<String, T> getTransaction) {
		final int retries = 2;
		final int waitTimeInMilliseconds = 500;
		testContext.getLogger().LogInfo("Start waiting for tx hash: ", signedTransaction.toString());
		return new RetryCommand<T>(retries, waitTimeInMilliseconds, Optional.empty())
				.run(
						(final RetryCommand<T> retryCommand) -> {
							try {
								return getTransaction.apply(signedTransaction.getHash());
							}
							catch (final IllegalArgumentException e) {
								testContext.getLogger().LogException(e);
								final TransactionStatus transactionStatus =
										getTransactionStatusNoThrow(signedTransaction.getHash());
								if (!transactionStatus.getStatus().equalsIgnoreCase("SUCCESS")) {
									testContext.getLogger().LogInfo("Status is success for hash: " + signedTransaction.toString());
									// Transaction was not found.
									retryCommand.cancelRetry();
								}
								throw new RuntimeException(
										"txStatus: "
												+ transactionStatus.getStatus()
												+ " statusGroup: "
												+ transactionStatus.getGroup()
												+ " "
												+ signedTransaction.toString(),
										e);
							}
						});
	}


	/**
	 * Announce a signed transaction.
	 *
	 * @param signedTransaction Signed transaction.
	 */
	public void announceTransaction(final SignedTransaction signedTransaction) {
		final TransactionRepository transactionRepository =
				new TransactionDao(testContext.getCatapultContext());
		testContext.getLogger().LogInfo("Announce tx : " + signedTransaction.toString());
		ExceptionUtils.propagate(
				() -> transactionRepository.announce(signedTransaction).toFuture().get());
	}

	/**
	 * Announce an aggregate bonded transaction.
	 *
	 * @param signedTransaction Signed transaction.
	 */
	public void announceAggregateBonded(final SignedTransaction signedTransaction) {
		final TransactionRepository transactionRepository =
				new TransactionDao(testContext.getCatapultContext());
		testContext.getLogger().LogInfo("Announce bonded tx : " + signedTransaction.toString());
		ExceptionUtils.propagate(
				() -> transactionRepository.announceAggregateBonded(signedTransaction).toFuture().get());
	}

	/**
	 * Announce a cosignature signed transaction.
	 *
	 * @param signedTransaction Signed transaction.
	 */
	public void announceAggregateBondedCosignature(
			final CosignatureSignedTransaction signedTransaction) {
		final TransactionRepository transactionRepository =
				new TransactionDao(testContext.getCatapultContext());
		testContext.getLogger().LogInfo("Announce aggregate bonded cosignature tx : " + signedTransaction.toString());
		ExceptionUtils.propagate(
				() ->
						transactionRepository
								.announceAggregateBondedCosignature(signedTransaction)
								.toFuture()
								.get());
	}

	/**
	 * Sign and announce transaction.
	 *
	 * @param transaction Transaction to sign.
	 * @param signer      Signer of the transaction.
	 * @return Signed transaction.
	 */
	public SignedTransaction signAndAnnounceTransaction(
			final Transaction transaction, final Account signer) {
		final SignedTransaction signedTransaction = signTransaction(transaction, signer);
		announceTransaction(signedTransaction);
		return signedTransaction;
	}

	/**
	 * Sign and announce transaction.
	 *
	 * @param signer              Signer of the transaction.
	 * @param transactionSupplier Transaction supplier.
	 * @return Signed transaction.
	 */
	public <T extends Transaction> SignedTransaction signAndAnnounceTransaction(
			final Account signer, final Supplier<T> transactionSupplier) {
		final T transaction = transactionSupplier.get();
		return signAndAnnounceTransaction(transaction, signer);
	}

	/**
	 * Sign and announce transaction to the network. Wait for the transaction to complete.
	 *
	 * @param signer              Signer of the transaction.
	 * @param transactionSupplier Transaction supplier.
	 * @return Transaction.
	 */
	public <T extends Transaction> T signAndAnnounceTransactionAndWait(
			final Account signer, final Supplier<T> transactionSupplier) {
		final SignedTransaction signedTransaction =
				signAndAnnounceTransaction(signer, transactionSupplier);
		final T transaction = waitForTransactionToComplete(signedTransaction);
		testContext.addTransaction(transaction);
		return transaction;
	}

	/**
	 * Gets aggregate bonded transactions for an account.
	 *
	 * @param publicAccount Public account.
	 * @return List of aggregate transaction.
	 */
	public List<AggregateTransaction> getAggregateBondedTransactions(
			final PublicAccount publicAccount) {
		return ExceptionUtils.propagate(
				() ->
						new AccountsDao(testContext.getCatapultContext())
								.aggregateBondedTransactions(publicAccount)
								.toFuture()
								.get());
	}

	/**
	 * Gets a bonded transaction from the pt cache.
	 *
	 * @param hash Transaction hash.
	 * @return Aggregate transaction.
	 */
	public AggregateTransaction getBondedTransaction(final String hash) {
		final Optional<Transaction> optionalTransaction =
				new PartialTransactionsCollection(testContext.getCatapultContext().getDataAccessContext()).findByHash(hash);
		if (optionalTransaction.isPresent()) {
			return (AggregateTransaction) optionalTransaction.get();
		}
		throw new IllegalArgumentException("Transaction hash " + hash + " not found.");
	}
}
