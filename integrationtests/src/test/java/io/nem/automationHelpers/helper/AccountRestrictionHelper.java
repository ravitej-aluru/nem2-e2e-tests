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
import io.nem.sdk.model.account.Account;
import io.nem.sdk.model.account.Address;
import io.nem.sdk.model.mosaic.MosaicId;
import io.nem.sdk.model.transaction.*;

import java.util.List;

/**
 * AccountRestriction helper.
 */
public class AccountRestrictionHelper {
	private final TestContext testContext;

	/**
	 * Constructor.
	 *
	 * @param testContext Test context.
	 */
	public AccountRestrictionHelper(final TestContext testContext) {
		this.testContext = testContext;
	}

	/**
	 * Create an account mosaic restriction
	 * @param accountRestrictionModificationType type of the modification
	 * @param mosaicId id of the mosaic to apply the restriction to
	 * @return An object of AccountRestrictionModification
	 */
	public AccountRestrictionModification createMosaicRestriction(
			final AccountRestrictionModificationType accountRestrictionModificationType,
																  final MosaicId mosaicId) {
		return AccountRestrictionModification.createForMosaic(accountRestrictionModificationType, mosaicId);
	}

	/**
	 * Create an account address restriction
	 * @param accountRestrictionModificationType type of the modification
	 * @param address address of the account to restrict
	 * @return AccountRestrictionModification object
	 */
	public AccountRestrictionModification createAddressRestriction(
			final AccountRestrictionModificationType accountRestrictionModificationType,
			final Address address) {
		return AccountRestrictionModification.createForAddress(accountRestrictionModificationType, address);
	}

	/**
	 *
	 * @param accountRestrictionModificationType
	 * @param transactionType
	 * @return
	 */
	public AccountRestrictionModification createTransactionTypeRestriction(
			final AccountRestrictionModificationType accountRestrictionModificationType,
			final TransactionType transactionType) {
		return AccountRestrictionModification.createForEntityType(accountRestrictionModificationType, transactionType);
	}

	/**
	 *
	 * @param account
	 * @param restrictionType
	 * @param modifications
	 * @return
	 */
	public AccountMosaicRestrictionModificationTransaction createAccountMosaicRestrictionTransactionAndWait(
			Account account,
			AccountRestrictionType restrictionType,
			List<AccountRestrictionModification<MosaicId>> modifications) {

		final TransactionHelper transactionHelper = new TransactionHelper(testContext);
		return transactionHelper.signAndAnnounceTransactionAndWait(
				account,
				() -> createAccountMosaicRestrictionTransaction(restrictionType, modifications));
	}

	/**
	 *
	 * @param account
	 * @param restrictionType
	 * @param modifications
	 * @return
	 */
	public AccountAddressRestrictionModificationTransaction createAccountAddressRestrictionTransactionAndWait(
			Account account,
			AccountRestrictionType restrictionType,
			List<AccountRestrictionModification<Address>> modifications) {

		final TransactionHelper transactionHelper = new TransactionHelper(testContext);
		return transactionHelper.signAndAnnounceTransactionAndWait(
				account,
				() -> createAccountAddressRestrictionTransaction(restrictionType, modifications));
	}

	/**
	 *
	 * @param account
	 * @param restrictionType
	 * @param modifications
	 * @return
	 */
	public AccountOperationRestrictionModificationTransaction createAccountTransactionTypeRestrictionTransactionAndWait(
			Account account,
			AccountRestrictionType restrictionType,
			List<AccountRestrictionModification<TransactionType>> modifications) {

		final TransactionHelper transactionHelper = new TransactionHelper(testContext);
		return transactionHelper.signAndAnnounceTransactionAndWait(
				account,
				() -> createAccountTransactionTypeRestrictionTransaction(restrictionType, modifications));
	}

	/**
	 *
	 * @param account
	 * @param restrictionType
	 * @param modifications
	 * @return
	 */
	public SignedTransaction createAccountMosaicRestrictionTransactionAndAnnounce(
			Account account, AccountRestrictionType restrictionType,
			List<AccountRestrictionModification<MosaicId>> modifications) {
		final TransactionHelper transactionHelper = new TransactionHelper(testContext);
		return transactionHelper.signAndAnnounceTransaction(
				account,
				() -> createAccountMosaicRestrictionTransaction(restrictionType, modifications));
	}

	/**
	 *
	 * @param account
	 * @param restrictionType
	 * @param modifications
	 * @return
	 */
	public SignedTransaction createAccountAddressRestrictionTransactionAndAnnounce(
			Account account, AccountRestrictionType restrictionType,
			List<AccountRestrictionModification<Address>> modifications) {
		final TransactionHelper transactionHelper = new TransactionHelper(testContext);
		return transactionHelper.signAndAnnounceTransaction(
				account,
				() -> createAccountAddressRestrictionTransaction(restrictionType, modifications));
	}

	/**
	 *
	 * @param account
	 * @param restrictionType
	 * @param modifications
	 * @return
	 */
	public SignedTransaction createAccountTransactionTypeRestrictionTransactionAndAnnounce(
			Account account, AccountRestrictionType restrictionType,
			List<AccountRestrictionModification<TransactionType>> modifications) {
		final TransactionHelper transactionHelper = new TransactionHelper(testContext);
		return transactionHelper.signAndAnnounceTransaction(
				account,
				() -> createAccountTransactionTypeRestrictionTransaction(restrictionType, modifications));
	}

	/**
	 *
	 * @param restrictionType
	 * @param modifications
	 * @return
	 */
	private AccountMosaicRestrictionModificationTransaction createAccountMosaicRestrictionTransaction(
			AccountRestrictionType restrictionType, List<AccountRestrictionModification<MosaicId>> modifications) {
		return AccountMosaicRestrictionModificationTransaction.create(
				TransactionHelper.getDefaultDeadline(),
				restrictionType,
				modifications,
				testContext.getNetworkType()
		);
	}

	/**
	 *
	 * @param restrictionType
	 * @param modifications
	 * @return
	 */
	private AccountAddressRestrictionModificationTransaction createAccountAddressRestrictionTransaction(
			AccountRestrictionType restrictionType, List<AccountRestrictionModification<Address>> modifications) {
		return AccountAddressRestrictionModificationTransaction.create(
				TransactionHelper.getDefaultDeadline(),
				restrictionType,
				modifications,
				testContext.getNetworkType()
		);
	}

	/**
	 *
	 * @param restrictionType
	 * @param modifications
	 * @return
	 */
	private AccountOperationRestrictionModificationTransaction createAccountTransactionTypeRestrictionTransaction(
			AccountRestrictionType restrictionType, List<AccountRestrictionModification<TransactionType>> modifications) {
		return AccountOperationRestrictionModificationTransaction.create(
				TransactionHelper.getDefaultDeadline(),
				restrictionType,
				modifications,
				testContext.getNetworkType()
		);
	}
}
