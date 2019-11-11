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

package io.nem.automation.transfer;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.nem.automation.common.BaseTest;
import io.nem.automationHelpers.common.TestContext;
import io.nem.automationHelpers.helper.*;
import io.nem.sdk.model.account.Account;
import io.nem.sdk.model.account.AccountInfo;
import io.nem.sdk.model.mosaic.Mosaic;
import io.nem.sdk.model.mosaic.MosaicFlags;
import io.nem.sdk.model.mosaic.MosaicId;
import io.nem.sdk.model.mosaic.MosaicInfo;
import io.nem.sdk.model.transaction.PlainMessage;
import io.nem.sdk.model.transaction.SignedTransaction;
import io.nem.sdk.model.transaction.TransferTransaction;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

/**
 * Send asset tests.
 */
public class SendAsset extends BaseTest {
	final TransferHelper transferHelper;

	/**
	 * Constructor.
	 *
	 * @param testContext Test context.
	 */
	public SendAsset(final TestContext testContext) {
		super(testContext);
		transferHelper = new TransferHelper(testContext);
	}

	@When("^(\\w+) sends (\\d+) asset \"(.*)\" to (\\w+)$")
	public void transferAsset(
			final String sender,
			final BigInteger amount,
			final String assetName,
			final String recipient) {
		final MosaicId mosaicId = resolveMosaicId(assetName);
		getTestContext().getLogger().LogInfo(String.format("transferAsset: sender = %s; " +
				"recipient: %s; mosaicId: %d; amount: %d", sender, recipient, mosaicId.getId(), amount));
		transferAssets(
				sender, recipient, Arrays.asList(new Mosaic(mosaicId, amount)), PlainMessage.Empty);
	}

	@When("^(\\w+) sends (-?\\d+) asset \"(\\w+)\" and (-?\\d+) asset \"(\\w+)\" to (.*)$")
	public void transferMultiAsset(
			final String sender,
			final BigInteger firstAmount,
			final String firstAssetName,
			final BigInteger secondAmount,
			final String secondAssetName,
			final String recipient) {
		final MosaicId firstMosaicId = resolveMosaicId(firstAssetName);
		final MosaicId secondMosaicId = resolveMosaicId(secondAssetName);
		transferAssets(
				sender,
				recipient,
				Arrays.asList(
						new Mosaic(firstMosaicId, firstAmount), new Mosaic(secondMosaicId, secondAmount)),
				PlainMessage.Empty);
	}

	@And("^(\\w+) should receive (\\d+) of asset \"(.*)\"$")
	public void verifyRecipientAsset(
			final String recipient, final int amount, final String assetName) {
		final AccountInfo recipientAccountInfo =
				getTestContext().getScenarioContext().getContext(recipient);
		final MosaicId mosaicId = resolveMosaicId(assetName);
		final Optional<Mosaic> initialMosaic =
				getMosaic(recipientAccountInfo, mosaicId);
		final long initialAmount =
				initialMosaic.isPresent() ? initialMosaic.get().getAmount().longValue() : 0;
		final AccountInfo recipientAccountInfoAfter =
				new AccountHelper(getTestContext()).getAccountInfo(recipientAccountInfo.getAddress());
		final Optional<Mosaic> mosaicAfter =
				getMosaic(recipientAccountInfoAfter, mosaicId);
		final long amountAfter = mosaicAfter.get().getAmount().longValue();
		final String errorMessage =
				"Recipient("
						+ recipientAccountInfoAfter.getAddress()
						+ ") did not receive Asset mosaic id:"
						+ mosaicId;
		getTestContext().getLogger().LogInfo("Recipient Account Info before: %s\n", recipientAccountInfo.toString());
		getTestContext().getLogger().LogInfo("Mosaic before: %s = %d\n\n", initialMosaic, initialAmount);
		getTestContext().getLogger().LogInfo("Recipient Account Info AFTER: %s\n", recipientAccountInfoAfter.toString());
		getTestContext().getLogger().LogInfo("Mosaic AFTER: %s = %d\n\n", mosaicAfter, amountAfter);
		assertEquals(errorMessage, true, mosaicAfter.isPresent());
		assertEquals(errorMessage, amount, amountAfter - initialAmount);
	}

	@And("^(\\w+) \"(.*)\" balance should decrease by (\\d+) units?$")
	public void verifySenderAsset(final String sender, final String assetName, final int amount) {
		final AccountInfo senderAccountInfo = getTestContext().getScenarioContext().getContext(sender);
		final MosaicId mosaicId = resolveMosaicId(assetName);
//		final MosaicInfo mosaicInfo = getTestContext().getScenarioContext().getContext(assetName);
		final Mosaic initialMosaic = getMosaic(senderAccountInfo, mosaicId).get();
		final AccountInfo recipientAccountInfoAfter =
				new AccountHelper(getTestContext()).getAccountInfo(senderAccountInfo.getAddress());
		final Mosaic mosaicAfter = getMosaic(recipientAccountInfoAfter, mosaicId).get();
		assertEquals(
				amount, initialMosaic.getAmount().longValue() - mosaicAfter.getAmount().longValue());
	}

	@And("^(\\w+) balance should remain intact$")
	public void VerifyAssetIntact(final String userName) {
		final AccountInfo accountInfo = getTestContext().getScenarioContext().getContext(userName);
		final AccountInfo accountInfoAfter =
				new AccountHelper(getTestContext()).getAccountInfo(accountInfo.getAddress());
		assertEquals(accountInfo.getMosaics().size(), accountInfoAfter.getMosaics().size());
		for (int i = 0; i < accountInfo.getMosaics().size(); ++i) {
			final Mosaic initial = accountInfo.getMosaics().get(i);
			final Mosaic after = accountInfoAfter.getMosaics().get(i);
			assertEquals(initial.getId().getIdAsLong(), after.getId().getIdAsLong());
			assertEquals(initial.getAmount().longValue(), after.getAmount().longValue());
		}
	}

	@When("^(\\w+) tries to send (-?\\d+) asset \"(.*)\" to (.*)$")
	public void triesToTransferAsset(
			final String sender,
			final BigInteger amount,
			final String assetName,
			final String recipient) {
		final MosaicId mosaicId = resolveMosaicId(assetName);
		triesToTransferAssets(
				sender, recipient, Arrays.asList(new Mosaic(mosaicId, amount)), PlainMessage.Empty);
	}

	@When("^(\\w+) tries to send (-?\\d+) asset \"(.*)\" and (-?\\d+) asset \"(.*)\" to (.*)$")
	public void triesToTransferMultiAsset(
			final String sender,
			final BigInteger firstAmount,
			final String firstAssetName,
			final BigInteger secondAmount,
			final String secondAssetName,
			final String recipient) {
		final MosaicId firstMosaicId = resolveMosaicId(firstAssetName);
		final MosaicId secondMosaicId = resolveMosaicId(secondAssetName);
		triesToTransferAssets(
				sender,
				recipient,
				Arrays.asList(
						new Mosaic(firstMosaicId, firstAmount), new Mosaic(secondMosaicId, secondAmount)),
				PlainMessage.Empty);
	}

	@Given("^(\\w+) registers a non transferable asset which she transfer (\\d+) asset to (\\w+)$")
	public void createNonTransferableAsset(
			final String sender, final int amount, final String recipient) {
		final Account senderAccount = getUser(sender);
		final Account recipientAccount = getUser(recipient);
		final boolean supplyMutable = CommonHelper.getRandomNextBoolean();
		final boolean transferable = false;
		final int divisibility = CommonHelper.getRandomDivisibility();
		final BigInteger initialSupply = BigInteger.valueOf(20);
		final MosaicInfo mosaicInfo =
				new MosaicHelper(getTestContext())
						.createMosaic(senderAccount, MosaicFlags.create(supplyMutable, transferable),
								divisibility, initialSupply);
		final BigInteger transferAmount = BigInteger.valueOf(amount);
		final TransferHelper transferHelper = new TransferHelper(getTestContext());
		transferHelper.submitTransferAndWait(
				senderAccount,
				recipientAccount.getAddress(),
				Arrays.asList(new Mosaic(mosaicInfo.getMosaicId(), transferAmount)),
				PlainMessage.Empty);
		getTestContext().getScenarioContext().setContext(MOSAIC_INFO_KEY, mosaicInfo);
	}

	@When("^(\\w+) transfer (\\d+) asset to (\\w+)$")
	public void transferAsset(final String sender, final int amount, final String recipient) {
		final Account senderAccount = getUser(sender);
		final Account recipientAccount = getUser(recipient);
		final MosaicInfo mosaicInfo = getTestContext().getScenarioContext().getContext(MOSAIC_INFO_KEY);
		final SignedTransaction signedTransaction =
				new TransferHelper(getTestContext())
						.createTransferAndAnnounce(
								senderAccount,
								recipientAccount.getAddress(),
								Arrays.asList(new Mosaic(mosaicInfo.getMosaicId(), BigInteger.valueOf(amount))),
								PlainMessage.Empty);
		getTestContext().setSignedTransaction(signedTransaction);
	}

	@Given("^(\\w+) registers a transferable asset which she transfer asset to (\\w+)$")
	public void createTransferableAsset(final String sender, final String recipient) {
		final Account senderAccount = getUser(sender);
		final Account recipientAccount = getUser(recipient);
		final boolean supplyMutable = CommonHelper.getRandomNextBoolean();
		final boolean transferable = true;
		final int divisibility = CommonHelper.getRandomDivisibility();
		final BigInteger initialSupply = BigInteger.valueOf(20);
		final MosaicInfo mosaicInfo =
				new MosaicHelper(getTestContext())
						.createMosaic(
								getTestContext().getDefaultSignerAccount(),
								MosaicFlags.create(supplyMutable, transferable),
								divisibility,
								initialSupply);
		final BigInteger transferAmount = BigInteger.valueOf(10);
		final TransferHelper transferHelper = new TransferHelper(getTestContext());
		transferHelper.submitTransferAndWait(
				senderAccount,
				recipientAccount.getAddress(),
				Arrays.asList(new Mosaic(mosaicInfo.getMosaicId(), transferAmount)),
				PlainMessage.Empty);
		getTestContext().getScenarioContext().setContext(MOSAIC_INFO_KEY, mosaicInfo);
	}

	@Then("^(\\d+) asset transfered successfully$")
	public void TransferableAssetSucceed(final int amount) {
		final SignedTransaction signedTransaction = getTestContext().getSignedTransaction();
		TransferTransaction transferTransaction =
				new TransactionHelper(getTestContext()).getTransaction(signedTransaction.getHash());
		final MosaicInfo mosaicInfo = getTestContext().getScenarioContext().getContext(MOSAIC_INFO_KEY);
		assertEquals(
				mosaicInfo.getMosaicId().getIdAsLong(),
				transferTransaction.getMosaics().get(0).getId().getIdAsLong());
		assertEquals(amount, transferTransaction.getMosaics().get(0).getAmount().intValue());
	}

}
