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
import io.nem.sdk.model.mosaic.MosaicInfo;
import io.nem.sdk.model.transaction.*;

import java.util.ArrayList;
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
     * Generates the AccountRestrictionType enum value from given restriction operation (allow/block)
     * and a given restricted item type (asset/address/transaction type)
     *
     * @param restrictionOperation restriction operation - allow or block
     * @param restrictedItem       restricted item type - asset or address or transaction type
     * @return AccountRestrictionType object
     */
    public AccountRestrictionType getAccountRestrictionType(final String restrictionOperation,
                                                            final String restrictedItem) {
        String accountRestrictionTypeString = "";
        switch (restrictionOperation.toUpperCase()) {
            case "ALLOWS":
                accountRestrictionTypeString = "ALLOW_INCOMING_";
                break;
            case "BLOCKS":
                accountRestrictionTypeString = "BLOCK_";
                break;
        }

        switch (restrictedItem.toUpperCase()) {

            case "ADDRESS":
            case "ADDRESSES":
                accountRestrictionTypeString += "ADDRESS";
                break;
            case "ASSET":
            case "ASSETS":
                accountRestrictionTypeString += "MOSAIC";
                break;
            case "TRANSACTION TYPE":
            case "TRANSACTION TYPES":
                accountRestrictionTypeString += "TRANSACTION_TYPE";
                break;
        }
        return AccountRestrictionType.valueOf(accountRestrictionTypeString);
    }

    public void removeAppropriateModificationTransactionAndWait(final String restrictedItem,
                                                                final List<Object> restrictedItems,
                                                                final Account signerAccount,
                                                                final AccountRestrictionType accountRestrictionType) {
        abcdefg(restrictedItem, restrictedItems, signerAccount, accountRestrictionType,
				AccountRestrictionModificationAction.REMOVE, true);
    }

    public void addAppropriateModificationTransactionAndWait(final String restrictedItem,
                                                             final List<Object> restrictedItems,
                                                             final Account signerAccount,
                                                             final AccountRestrictionType accountRestrictionType) {
        abcdefg(restrictedItem, restrictedItems, signerAccount, accountRestrictionType,
				AccountRestrictionModificationAction.ADD, true);
    }

    public void removeAppropriateModificationTransactionAndAnnounce(final String restrictedItem,
                                                                    final List<Object> restrictedItems,
                                                                    final Account signerAccount,
                                                                    final AccountRestrictionType accountRestrictionType) {
        abcdefg(restrictedItem, restrictedItems, signerAccount, accountRestrictionType,
				AccountRestrictionModificationAction.REMOVE, false);
    }

    public void addAppropriateModificationTransactionAndAnnounce(final String restrictedItem,
                                                                 final List<Object> restrictedItems,
                                                                 final Account signerAccount,
                                                                 final AccountRestrictionType accountRestrictionType) {
        abcdefg(restrictedItem, restrictedItems, signerAccount, accountRestrictionType,
				AccountRestrictionModificationAction.ADD, false);
    }

    private void abcdefg(final String restrictedItem, final List<Object> restrictedItems, final Account signerAccount,
                         final AccountRestrictionType accountRestrictionType,
                         final AccountRestrictionModificationAction accountRestrictionModificationAction,
                         final Boolean waitForTransaction) {
        switch (restrictedItem.toUpperCase()) {
            case "ASSET":
            case "ASSETS":
                List<AccountRestrictionModification<MosaicId>> assetModifications = new ArrayList<>();
                restrictedItems.forEach(asset -> {
                    MosaicInfo mosaicInfo = testContext.getScenarioContext().getContext(asset.toString());
                    assetModifications.add(createMosaicRestriction(
                            accountRestrictionModificationAction, mosaicInfo.getMosaicId()));
                });
                if (waitForTransaction) {
                    createAccountMosaicRestrictionTransactionAndWait(
                            signerAccount, accountRestrictionType, assetModifications);
                } else {
                    createAccountMosaicRestrictionTransactionAndAnnounce(
                            signerAccount, accountRestrictionType, assetModifications);
                }

                break;
            case "ADDRESS":
            case "ADDRESSES":
                List<AccountRestrictionModification<Address>> addressModifications = new ArrayList<>();
                restrictedItems.forEach(address -> {
                    addressModifications.add(createAddressRestriction(
                            accountRestrictionModificationAction, ((Address)address)));
                });
                if (waitForTransaction) {
                    createAccountAddressRestrictionTransactionAndWait(
                            signerAccount, accountRestrictionType, addressModifications);
                } else {
                    createAccountAddressRestrictionTransactionAndAnnounce(
                            signerAccount, accountRestrictionType, addressModifications);
                }
                break;
            case "TRANSACTION TYPE":
            case "TRANSACTION TYPES":
                List<AccountRestrictionModification<TransactionType>> operationModifications = new ArrayList<>();
                restrictedItems.forEach(transactionType -> {
                    TransactionType transactionTypeInfo = testContext.getScenarioContext().getContext(
                    		transactionType.toString());
                    operationModifications.add(createTransactionTypeRestriction(
                            accountRestrictionModificationAction, transactionTypeInfo));
                });
                if (waitForTransaction) {
                    createAccountTransactionTypeRestrictionTransactionAndWait(
                            signerAccount, accountRestrictionType, operationModifications);
                } else {
                    createAccountTransactionTypeRestrictionTransactionAndAnnounce(
                            signerAccount, accountRestrictionType, operationModifications);
                }
                break;
        }
    }

	/**
	 * Create an account mosaic restriction
	 * @param accountRestrictionModificationAction type of the modification
	 * @param mosaicId id of the mosaic to apply the restriction to
	 * @return An object of AccountRestrictionModification
	 */
	public AccountRestrictionModification createMosaicRestriction(
			final AccountRestrictionModificationAction accountRestrictionModificationAction,
																  final MosaicId mosaicId) {
		return AccountRestrictionModification.createForMosaic(accountRestrictionModificationAction, mosaicId);
	}

	/**
	 * Create an account address restriction
	 * @param accountRestrictionModificationAction type of the modification
	 * @param address address of the account to restrict
	 * @return AccountRestrictionModification object
	 */
	public AccountRestrictionModification createAddressRestriction(
			final AccountRestrictionModificationAction accountRestrictionModificationAction,
			final Address address) {
		return AccountRestrictionModification.createForAddress(accountRestrictionModificationAction, address);
	}

	/**
	 *
	 * @param accountRestrictionModificationAction
	 * @param transactionType
	 * @return
	 */
	public AccountRestrictionModification createTransactionTypeRestriction(
			final AccountRestrictionModificationAction accountRestrictionModificationAction,
			final TransactionType transactionType) {
		return AccountRestrictionModification.createForTransactionType(accountRestrictionModificationAction, transactionType);
	}

	/**
	 *
	 * @param account
	 * @param restrictionType
	 * @param modifications
	 * @return
	 */
	public AccountMosaicRestrictionTransaction createAccountMosaicRestrictionTransactionAndWait(
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
	public AccountAddressRestrictionTransaction createAccountAddressRestrictionTransactionAndWait(
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
	public AccountOperationRestrictionTransaction createAccountTransactionTypeRestrictionTransactionAndWait(
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
	private AccountMosaicRestrictionTransaction createAccountMosaicRestrictionTransaction(
			AccountRestrictionType restrictionType, List<AccountRestrictionModification<MosaicId>> modifications) {
		return AccountMosaicRestrictionTransactionFactory.create(
				testContext.getNetworkType(),
				restrictionType,
				modifications
				).build();
	}

	/**
	 *
	 * @param restrictionType
	 * @param modifications
	 * @return
	 */
	private AccountAddressRestrictionTransaction createAccountAddressRestrictionTransaction(
			AccountRestrictionType restrictionType, List<AccountRestrictionModification<Address>> modifications) {
		return AccountAddressRestrictionTransactionFactory.create(
				testContext.getNetworkType(),
				restrictionType,
				modifications
				).build();
	}

	/**
	 *
	 * @param restrictionType
	 * @param modifications
	 * @return
	 */
	private AccountOperationRestrictionTransaction createAccountTransactionTypeRestrictionTransaction(
			AccountRestrictionType restrictionType, List<AccountRestrictionModification<TransactionType>> modifications) {
		return AccountOperationRestrictionTransactionFactory.create(
				testContext.getNetworkType(),
				restrictionType,
				modifications
				).build();
	}
}
