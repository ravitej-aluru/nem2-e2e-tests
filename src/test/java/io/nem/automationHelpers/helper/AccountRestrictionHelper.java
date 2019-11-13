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
import io.nem.sdk.model.account.UnresolvedAddress;
import io.nem.sdk.model.mosaic.MosaicId;
import io.nem.sdk.model.mosaic.UnresolvedMosaicId;
import io.nem.sdk.model.transaction.AccountAddressRestrictionTransaction;
import io.nem.sdk.model.transaction.AccountAddressRestrictionTransactionFactory;
import io.nem.sdk.model.transaction.AccountMosaicRestrictionTransaction;
import io.nem.sdk.model.transaction.AccountMosaicRestrictionTransactionFactory;
import io.nem.sdk.model.transaction.AccountOperationRestrictionTransaction;
import io.nem.sdk.model.transaction.AccountOperationRestrictionTransactionFactory;
import io.nem.sdk.model.transaction.AccountRestrictionType;
import io.nem.sdk.model.transaction.SignedTransaction;
import io.nem.sdk.model.transaction.TransactionType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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
     * Generates the AccountRestrictionType enum value from given restriction operation
     * (allow/block) and a given restricted item type (asset/address/transaction type)
     *
     * @param restrictionOperation restriction operation - allow or block
     * @param restrictedItem restricted item type - asset or address or transaction type
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
        createAppropriateRestrictionTransaction(restrictedItem, restrictedItems, signerAccount,
            accountRestrictionType,
            false, true);
    }

    public void addAppropriateModificationTransactionAndWait(final String restrictedItem,
        final List<Object> restrictedItems,
        final Account signerAccount,
        final AccountRestrictionType accountRestrictionType) {
        createAppropriateRestrictionTransaction(restrictedItem, restrictedItems, signerAccount,
            accountRestrictionType,
            true, true);
    }

    public void removeAppropriateModificationTransactionAndAnnounce(final String restrictedItem,
        final List<Object> restrictedItems,
        final Account signerAccount,
        final AccountRestrictionType accountRestrictionType) {
        createAppropriateRestrictionTransaction(restrictedItem, restrictedItems, signerAccount,
            accountRestrictionType,
            false, false);
    }

    public void addAppropriateModificationTransactionAndAnnounce(final String restrictedItem,
        final List<Object> restrictedItems,
        final Account signerAccount,
        final AccountRestrictionType accountRestrictionType) {
        createAppropriateRestrictionTransaction(restrictedItem, restrictedItems, signerAccount,
            accountRestrictionType,
            true, false);
    }

    private void createAppropriateRestrictionTransaction(final String restrictedItem,
        final List<Object> restrictedItems, final Account signerAccount,
        final AccountRestrictionType accountRestrictionType,
        final boolean add,
        final Boolean waitForTransaction) {
        switch (restrictedItem.toUpperCase()) {
            case "ASSET":
            case "ASSETS":
                List<UnresolvedMosaicId> assetModifications = restrictedItems.parallelStream()
                    .map(asset -> (MosaicId) asset).collect(Collectors.toList());

                testContext.getLogger().LogInfo("assetModifications = %s",
                    Arrays.toString(assetModifications.toArray()));

                if (waitForTransaction) {
                    createAccountMosaicRestrictionTransactionAndWait(
                        signerAccount, accountRestrictionType,
                        add ? assetModifications : Collections.emptyList(),
                        !add ? assetModifications : Collections.emptyList());
                } else {
                    createAccountMosaicRestrictionTransactionAndAnnounce(
                        signerAccount, accountRestrictionType,
                        add ? assetModifications : Collections.emptyList(),
                        !add ? assetModifications : Collections.emptyList());
                }
                break;
            case "ADDRESS":
            case "ADDRESSES":
                List<UnresolvedAddress> addressModifications = restrictedItems.parallelStream()
                    .map(address -> (Address) address).collect(Collectors.toList());
                if (waitForTransaction) {
                    createAccountAddressRestrictionTransactionAndWait(
                        signerAccount, accountRestrictionType,
                        add ? addressModifications : Collections.emptyList(),
                        !add ? addressModifications : Collections.emptyList());
                } else {
                    createAccountAddressRestrictionTransactionAndAnnounce(
                        signerAccount, accountRestrictionType,
                        add ? addressModifications : Collections.emptyList(),
                        !add ? addressModifications : Collections.emptyList());

                }
                break;
            case "TRANSACTION TYPE":
            case "TRANSACTION TYPES":
                List<TransactionType> operationModifications = restrictedItems
                    .parallelStream().<TransactionType>map(
                        transactionType -> testContext.getScenarioContext()
                            .getContext(
                                transactionType.toString())).collect(Collectors.toList());

                if (waitForTransaction) {
                    createAccountTransactionTypeRestrictionTransactionAndWait(
                        signerAccount, accountRestrictionType,
                        add ? operationModifications : Collections.emptyList(),
                        !add ? operationModifications : Collections.emptyList());

                } else {
                    createAccountTransactionTypeRestrictionTransactionAndAnnounce(
                        signerAccount, accountRestrictionType,
                        add ? operationModifications : Collections.emptyList(),
                        !add ? operationModifications : Collections.emptyList());
                }
                break;
        }
    }

    /**
     *
     */
    public AccountMosaicRestrictionTransaction createAccountMosaicRestrictionTransactionAndWait(
        Account account,
        AccountRestrictionType restrictionType,
        List<UnresolvedMosaicId> additions, List<UnresolvedMosaicId> deletions) {

        final TransactionHelper transactionHelper = new TransactionHelper(testContext);
        return transactionHelper.signAndAnnounceTransactionAndWait(
            account,
            () -> createAccountMosaicRestrictionTransaction(restrictionType, additions, deletions));
    }

    /**
     *
     */
    public AccountAddressRestrictionTransaction createAccountAddressRestrictionTransactionAndWait(
        Account account,
        AccountRestrictionType restrictionType,
        List<UnresolvedAddress> additions, List<UnresolvedAddress> deletions) {

        final TransactionHelper transactionHelper = new TransactionHelper(testContext);
        return transactionHelper.signAndAnnounceTransactionAndWait(
            account,
            () -> createAccountAddressRestrictionTransaction(restrictionType, additions,
                deletions));
    }

    /**
     *
     */
    public AccountOperationRestrictionTransaction createAccountTransactionTypeRestrictionTransactionAndWait(
        Account account,
        AccountRestrictionType restrictionType,
        List<TransactionType> additions, List<TransactionType> deletions) {

        final TransactionHelper transactionHelper = new TransactionHelper(testContext);
        return transactionHelper.signAndAnnounceTransactionAndWait(
            account,
            () -> createAccountTransactionTypeRestrictionTransaction(restrictionType,
                additions, deletions));
    }

    /**
     *
     */
    public SignedTransaction createAccountMosaicRestrictionTransactionAndAnnounce(
        Account account, AccountRestrictionType restrictionType,
        List<UnresolvedMosaicId> additions, List<UnresolvedMosaicId> deletions) {
        final TransactionHelper transactionHelper = new TransactionHelper(testContext);
        return transactionHelper.signAndAnnounceTransaction(
            account,
            () -> createAccountMosaicRestrictionTransaction(restrictionType, additions, deletions));
    }

    /**
     *
     */
    public SignedTransaction createAccountAddressRestrictionTransactionAndAnnounce(
        Account account, AccountRestrictionType restrictionType,
        List<UnresolvedAddress> additions, List<UnresolvedAddress> deletions) {
        final TransactionHelper transactionHelper = new TransactionHelper(testContext);
        return transactionHelper.signAndAnnounceTransaction(
            account,
            () -> createAccountAddressRestrictionTransaction(restrictionType, additions,
                deletions));
    }

    /**
     *
     */
    public SignedTransaction createAccountTransactionTypeRestrictionTransactionAndAnnounce(
        Account account, AccountRestrictionType restrictionType,
        List<TransactionType> additions, List<TransactionType> deletions) {
        final TransactionHelper transactionHelper = new TransactionHelper(testContext);
        return transactionHelper.signAndAnnounceTransaction(
            account,
            () -> createAccountTransactionTypeRestrictionTransaction(restrictionType,
                additions, deletions));
    }

    /**
     *
     */
    private AccountMosaicRestrictionTransaction createAccountMosaicRestrictionTransaction(
        AccountRestrictionType restrictionType,
        List<UnresolvedMosaicId> additions, List<UnresolvedMosaicId> deletions) {
        return AccountMosaicRestrictionTransactionFactory.create(
            testContext.getNetworkType(),
            restrictionType,
            additions, deletions
        ).build();
    }

    /**
     *
     */
    private AccountAddressRestrictionTransaction createAccountAddressRestrictionTransaction(
        AccountRestrictionType restrictionType,
        List<UnresolvedAddress> additions, List<UnresolvedAddress> deletions) {
        return AccountAddressRestrictionTransactionFactory.create(
            testContext.getNetworkType(),
            restrictionType,
            additions, deletions
        ).build();
    }

    private AccountOperationRestrictionTransaction createAccountTransactionTypeRestrictionTransaction(
        AccountRestrictionType restrictionType, List<TransactionType> additions,
        List<TransactionType> deletions) {
        return AccountOperationRestrictionTransactionFactory.create(
            testContext.getNetworkType(),
            restrictionType,
            additions, deletions
        ).build();
    }
}
