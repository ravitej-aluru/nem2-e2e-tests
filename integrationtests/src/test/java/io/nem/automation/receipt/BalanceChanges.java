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

package io.nem.automation.receipt;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.nem.automation.common.BaseTest;
import io.nem.automationHelpers.common.TestContext;
import io.nem.automationHelpers.helper.BlockChainHelper;
import io.nem.sdk.model.mosaic.NetworkCurrencyMosaic;
import io.nem.sdk.model.receipt.BalanceChangeReceipt;
import io.nem.sdk.model.receipt.ReceiptType;
import io.nem.sdk.model.receipt.Statement;
import io.nem.sdk.model.transaction.AggregateTransaction;
import io.nem.sdk.model.transaction.LockFundsTransaction;
import io.nem.sdk.model.transaction.TransactionType;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class BalanceChanges extends BaseTest {
	private final String RECEIPT_TYPE = "receiptType";
	private final String RECEIPT_HEIGHT = "receiptHeight";

	public BalanceChanges(final TestContext testContext) {
		super(testContext);
	}

	private BalanceChangeReceipt getBalanceChange(final BigInteger height,
												  final ReceiptType receiptType) {
		final Statement statement = new BlockChainHelper(getTestContext()).getBlockReceipts(height);
		final Optional<BalanceChangeReceipt> balanceChangeReceipt =
				statement.getTransactionStatements().stream().map(s -> s.getReceipts()).flatMap(Collection::stream)
						.filter(receipt -> receipt.getType() == receiptType).findAny().map(f -> (BalanceChangeReceipt) f);
		assertTrue("Balance change was not found", balanceChangeReceipt.isPresent());
		return balanceChangeReceipt.get();
	}

	@When("^she checks if the contract has concluded$")
	public void definedValidEscrowContract() {
		final AggregateTransaction aggregateTransaction =
				getTestContext().<AggregateTransaction>findTransaction(TransactionType.AGGREGATE_BONDED).get();
		getTestContext().getScenarioContext().setContext(RECEIPT_TYPE, ReceiptType.LockHash_Completed);
		getTestContext().getScenarioContext().setContext(RECEIPT_HEIGHT, aggregateTransaction.getTransactionInfo().get().getHeight());
	}

	@When("^she checks if the locked mosaics for the previous transaction have been locked$")
	public void checksLockedValue() {

	}

	@And("^the lock expires$")
	public void lockExpires() {
		final LockFundsTransaction lockFundsTransaction =
				getTestContext().<LockFundsTransaction>findTransaction(TransactionType.LOCK).get();
		final BigInteger receiptHeight =
				lockFundsTransaction.getTransactionInfo().get().getHeight().add(lockFundsTransaction.getDuration());
		getTestContext().getScenarioContext().setContext(RECEIPT_HEIGHT, receiptHeight);
		getTestContext().getScenarioContext().setContext(RECEIPT_TYPE, ReceiptType.LockHash_Expired);
	}

	@When("^she checks if the lock has expired$")
	public void checkForLockExpired() {
		final LockFundsTransaction lockFundsTransaction =
				getTestContext().<LockFundsTransaction>findTransaction(TransactionType.LOCK).get();
		waitForBlockChainHeight(lockFundsTransaction.getTransactionInfo().get().getHeight().longValue());
	}

	@Then("(\\w+) should get (\\d+) \"cat.currency\" returned to her account")
	public void verifyLockHashReturnedReceipt(final String userName, final BigInteger amount) {
		final BigInteger receiptHeight = getTestContext().getScenarioContext().getContext(RECEIPT_HEIGHT);
		final ReceiptType receiptType = getTestContext().getScenarioContext().getContext(RECEIPT_TYPE);
		final BalanceChangeReceipt balanceChangeReceipt =
				getBalanceChange(receiptHeight, receiptType);
		final BigInteger actualAmount = NetworkCurrencyMosaic.createRelative(amount).getAmount();
		assertEquals("Receipt amount failed for " + receiptType + " @" + receiptHeight.longValue(),
				actualAmount.longValue(), balanceChangeReceipt.getAmount().longValue());
	}

	@Then("(\\w+) should have (\\d+) \"cat.currency\" sent from her account")
	public void verifyLockHashSentReceipt(final String userName, final BigInteger amount) {
		final LockFundsTransaction lockFundsTransaction =
				getTestContext().<LockFundsTransaction>findTransaction(TransactionType.LOCK).get();
		final BigInteger receiptHeight = lockFundsTransaction.getTransactionInfo().get().getHeight();
		final BalanceChangeReceipt balanceChangeReceipt =
				getBalanceChange(receiptHeight, ReceiptType.LockHash_Created);
		final BigInteger actualAmount = NetworkCurrencyMosaic.createRelative(amount).getAmount();
		assertEquals("Receipt amount failed for " + ReceiptType.LockHash_Created + " @" + receiptHeight.longValue(), actualAmount.longValue(),
				balanceChangeReceipt.getAmount().longValue());
	}
}