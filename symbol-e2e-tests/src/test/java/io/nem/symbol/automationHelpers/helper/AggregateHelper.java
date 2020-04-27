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

package io.nem.symbol.automationHelpers.helper;

import io.nem.symbol.automationHelpers.common.TestContext;
import io.nem.symbol.sdk.model.account.Account;
import io.nem.symbol.sdk.model.mosaic.Mosaic;
import io.nem.symbol.sdk.model.transaction.*;

import java.math.BigInteger;
import java.util.List;

/** Aggregate helper. */
public class AggregateHelper extends BaseHelper<AggregateHelper> {

  /**
   * Constructor.
   *
   * @param testContext Test context.
   */
  public AggregateHelper(final TestContext testContext) {
    super(testContext);
  }

  private HashLockTransaction createHashLockTransaction(
      final Mosaic mosaic, final BigInteger duration, final SignedTransaction signedTransaction) {
    final HashLockTransactionFactory hashLockTransactionFactory =
        HashLockTransactionFactory.create(
            testContext.getNetworkType(), mosaic, duration, signedTransaction);
    return buildTransaction(hashLockTransactionFactory);
  }

  /**
   * Creates a aggregate transaction with cosigners.
   *
   * @param innerTransaction List of inner transactions.
   * @return Aggregate transaction.
   */
  public AggregateTransaction createAggregateTransactionWithCosigners(
          final TransactionType transactionType,
      final List<Transaction> innerTransaction,
      final List<AggregateTransactionCosignature> cosignatures) {
    final AggregateTransactionFactory aggregateTransactionFactory =
        AggregateTransactionFactory.create(
            transactionType,
            testContext.getNetworkType(),
            innerTransaction,
            cosignatures);
    return buildTransaction(aggregateTransactionFactory);
  }

  /**
   * Creates a aggregate complete transaction.
   *
   * @param innerTransaction List of inner transactions.
   * @return Aggregate transaction.
   */
  public AggregateTransaction createAggregateCompleteTransaction(
      final List<Transaction> innerTransaction) {
    final AggregateTransactionFactory aggregateTransactionFactory =
        AggregateTransactionFactory.createComplete(testContext.getNetworkType(), innerTransaction);
    return buildTransaction(aggregateTransactionFactory);
  }

  /**
   * Creates a aggregate complete transaction.
   *
   * @param innerTransaction List of inner transactions.
   * @return Aggregate transaction.
   */
  public AggregateTransaction createAggregateBondedTransaction(
      final List<Transaction> innerTransaction) {
    final AggregateTransactionFactory aggregateTransactionFactory =
        AggregateTransactionFactory.createBonded(testContext.getNetworkType(), innerTransaction);
    return buildTransaction(aggregateTransactionFactory);
  }

  /**
   * Creates a lock fund transaction and announce it to the network and wait for confirmed status.
   *
   * @param account User account.
   * @param mosaic Mosaic to lock.
   * @param duration Duration to lock.
   * @param signedTransaction Signed transaction.
   * @return Signed transaction.
   */
  public SignedTransaction createLockFundsAndAnnounce(
      final Account account,
      final Mosaic mosaic,
      final BigInteger duration,
      final SignedTransaction signedTransaction) {
    return new TransactionHelper(testContext)
        .signAndAnnounceTransaction(
            account, () -> createHashLockTransaction(mosaic, duration, signedTransaction));
  }

  /**
   * Creates a lock fund transaction and announce it to the network and wait for confirmed status.
   *
   * @param account User account.
   * @param mosaic Mosaic to lock.
   * @param duration Duration to lock.
   * @param signedTransaction Signed transaction.
   * @return Lock funds transaction.
   */
  public HashLockTransaction submitHashLockTransactionAndWait(
      final Account account,
      final Mosaic mosaic,
      final BigInteger duration,
      final SignedTransaction signedTransaction) {
    return new TransactionHelper(testContext)
        .signAndAnnounceTransactionAndWait(
            account, () -> createHashLockTransaction(mosaic, duration, signedTransaction));
  }

  /**
   * Creates an aggregate complete transaction and announce it to the network.
   *
   * @param account User account.
   * @param innerTransaction List of inner transactions.
   * @return Signed transaction.
   */
  public SignedTransaction createAggregateCompleteAndAnnounce(
      final Account account, final List<Transaction> innerTransaction) {
    return new TransactionHelper(testContext)
        .signAndAnnounceTransaction(
            account, () -> createAggregateCompleteTransaction(innerTransaction));
  }

  /**
   * Submits a lock fund transaction for a aggregate bonded transaction.
   *
   * @param account Signer account.
   * @param signedTransaction Signed bonded transaction.
   * @param duration Duration for the lock funds.
   * @return Lock funds transaction.
   */
  public HashLockTransaction submitLockFundForBondedTransaction(
      final Account account, final SignedTransaction signedTransaction, final BigInteger duration) {
    final Mosaic mosaicToLock =
        testContext.getNetworkCurrency().createRelative(BigInteger.valueOf(10));
    return submitHashLockTransactionAndWait(account, mosaicToLock, duration, signedTransaction);
  }

  /**
   * Creates an aggregate bonded transaction and announce it to the network.
   *
   * @param account User account.
   * @param innerTransaction List of inner transactions.
   * @return Signed transaction.
   */
  public SignedTransaction createAggregateBondedAndAnnounce(
      final Account account, final List<Transaction> innerTransaction) {
    final TransactionHelper transactionHelper = new TransactionHelper(testContext);
    final AggregateTransaction aggregateTransaction =
        createAggregateBondedTransaction(innerTransaction);
    final SignedTransaction signedTransaction =
        transactionHelper.signTransaction(aggregateTransaction, account);
    final BigInteger duration = BigInteger.valueOf(5);
    submitLockFundForBondedTransaction(account, signedTransaction, duration);
    transactionHelper.announceAggregateBonded(signedTransaction);
    testContext.addTransaction(aggregateTransaction);
    return signedTransaction;
  }

  /**
   * Creates an aggregate complete transaction and announce it to the network and wait for confirmed
   * status.
   *
   * @param account User account.
   * @param innerTransaction List of transactions.
   * @return Mosaic supply change transaction.
   */
  public AggregateTransaction submitAggregateCompleteAndWait(
      final Account account, final List<Transaction> innerTransaction) {
    return new TransactionHelper(testContext)
        .signAndAnnounceTransactionAndWait(
            account, () -> createAggregateCompleteTransaction(innerTransaction));
  }

  /**
   * Creates an aggregate bonded transaction and announce it to the network and wait for confirmed
   * status.
   *
   * @param account User account.
   * @param innerTransaction List of transactions.
   * @return Mosaic supply change transaction.
   */
  public AggregateTransaction submitAggregateBondedAndWait(
      final Account account, final List<Transaction> innerTransaction) {
    final SignedTransaction signedTransaction =
        createAggregateBondedAndAnnounce(account, innerTransaction);
    return new TransactionHelper(testContext).waitForTransactionToComplete(signedTransaction);
  }

  /**
   * Cosign a aggregate bonded transaction.
   *
   * @param account Account to cosign.
   * @param aggregateTransaction Aggregate transaction.
   */
  public void cosignAggregateBonded(
      final Account account, final AggregateTransaction aggregateTransaction) {
    final CosignatureTransaction cosignatureTransaction =
        CosignatureTransaction.create(aggregateTransaction);
    final CosignatureSignedTransaction cosignatureSignedTransaction =
        account.signCosignatureTransaction(cosignatureTransaction);
    final TransactionHelper transactionHelper = new TransactionHelper(testContext);
    transactionHelper.announceAggregateBondedCosignature(cosignatureSignedTransaction);
    testContext.getLogger().LogInfo("Cosign bonded with account: " + account.getPublicKey());
  }

  /**
   * Sign aggregate transaction with cosigners.
   *
   * @param aggregateTransaction Aggregate transaction.
   * @param initiatorAccount Initiator account.
   * @param cosigners List of cosigners.
   * @return Signed aggregate transaction.
   */
  public SignedTransaction signTransactionWithCosigners(
      final AggregateTransaction aggregateTransaction,
      final Account initiatorAccount,
      final List<Account> cosigners) {
    aggregateTransaction.signTransactionWithCosigners(
        initiatorAccount, cosigners, testContext.getGenerationHash());
    this.withDeadline(() -> aggregateTransaction.getDeadline());
    final AggregateTransaction aggregateTransactionUpdate =
        createAggregateTransactionWithCosigners(aggregateTransaction.getType(),
            aggregateTransaction.getInnerTransactions(), aggregateTransaction.getCosignatures());
    testContext.addTransaction(aggregateTransactionUpdate);
    final SignedTransaction signedTransaction =
        aggregateTransactionUpdate.signTransactionWithCosigners(
                initiatorAccount, cosigners, testContext.getGenerationHash());
    testContext.setSignedTransaction(signedTransaction);

//    aggregateTransaction.addCosigners(initiatorAccount, cosigners, testContext.getGenerationHash());
    return signedTransaction;
  }
}
