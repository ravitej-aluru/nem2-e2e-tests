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

package io.nem.automation.account;

import cucumber.api.java.en.And;
import cucumber.api.java.en.When;
import io.nem.automation.common.BaseTest;
import io.nem.automationHelpers.common.TestContext;
import io.nem.automationHelpers.helper.AccountHelper;
import io.nem.automationHelpers.helper.AggregateHelper;
import io.nem.automationHelpers.helper.MultisigAccountHelper;
import io.nem.automationHelpers.helper.TransactionHelper;
import io.nem.sdk.model.account.Account;
import io.nem.sdk.model.account.MultisigAccountInfo;
import io.nem.sdk.model.account.PublicAccount;
import io.nem.sdk.model.transaction.AggregateTransaction;
import io.nem.sdk.model.transaction.MultisigAccountModificationTransaction;
import io.nem.sdk.model.transaction.SignedTransaction;
import io.nem.sdk.model.transaction.Transaction;
import io.nem.sdk.model.transaction.TransactionType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;

/**
 * Edit multisignature contract.
 */
public class EditMultisignatureContract extends BaseTest {

    private final MultisigAccountHelper multisigAccountHelper;

    /**
     * Constructor.
     *
     * @param testContext Test context.
     */
    public EditMultisignatureContract(final TestContext testContext) {
        super(testContext);
        multisigAccountHelper = new MultisigAccountHelper(testContext);
    }

    private AggregateTransaction createAggregateTransaction(final boolean isBonded,
        final List<Transaction> innerTransaction) {
        final AggregateTransaction aggregateTransaction =
            isBonded ? new AggregateHelper(getTestContext())
                .createAggregateBondedTransaction(innerTransaction) :
                new AggregateHelper(getTestContext())
                    .createAggregateCompleteTransaction(innerTransaction);
        return aggregateTransaction;
    }

    private void createModifyMultisigAccount(
        final String userName,
        final byte minimumApproval,
        final byte minimumRemoval,
        final Map<String, String> operationList) {
        final Account signerAccount = getUser(userName);
        final Account multiSigAccount = getTestContext().getScenarioContext()
            .getContext(MULTISIG_ACCOUNT_INFO);

        final MultisigAccountModificationTransaction originalMultisigAccountModificationTransaction =
            getTestContext().<MultisigAccountModificationTransaction>findTransaction(
                TransactionType.MODIFY_MULTISIG_ACCOUNT).get();

        getTestContext().clearTransaction();
        boolean requireBondedTransaction =
            originalMultisigAccountModificationTransaction.getMinApprovalDelta() > 1;

        final List<PublicAccount> additions = new ArrayList<>();
        final List<PublicAccount> deletions = new ArrayList<>();

        for (Map.Entry<String, String> entry : operationList.entrySet()) {
            final Account account = getUser(entry.getKey());
            if ("ADD".equals(entry.getValue().toUpperCase())) {
                additions.add(account.getPublicAccount());
                requireBondedTransaction = true;
            } else if ("REMOVE".equals(entry.getValue().toUpperCase())
                && originalMultisigAccountModificationTransaction.getPublicKeyAdditions()
                .contains(account.getPublicAccount())) {
                deletions.add(account.getPublicAccount());
            }
        }

        final MultisigAccountModificationTransaction modifyMultisigAccountTransaction =
            multisigAccountHelper.createMultisigAccountModificationTransaction(
                minimumApproval, minimumRemoval, additions, deletions);
        final List<Transaction> innerTransactions = Arrays.asList(
            modifyMultisigAccountTransaction.toAggregate(
                multiSigAccount.getPublicAccount()));
        final AggregateTransaction aggregateTransaction = createAggregateTransaction(
            requireBondedTransaction, innerTransactions);
        final TransactionHelper transactionHelper = new TransactionHelper(getTestContext());
        transactionHelper.signTransaction(aggregateTransaction, signerAccount);
        final byte newApproval = (byte) (
            originalMultisigAccountModificationTransaction.getMinApprovalDelta() + minimumApproval);
        final byte newRemoval = (byte) (
            originalMultisigAccountModificationTransaction.getMinRemovalDelta() + minimumRemoval);
        final MultisigAccountModificationTransaction newMultisigAccountModificationTransaction =
            multisigAccountHelper
                .createMultisigAccountModificationTransaction(newApproval, newRemoval,
                    additions, deletions);
        getTestContext().addTransaction(newMultisigAccountModificationTransaction);
    }

    @And("^\"(\\w+)\" update the cosignatories of the multisignature:$")
    public void modifyCosignList(final String userName, final Map<String, String> operationList) {
        final byte minimumApproval = 0;
        final byte minimumRemoval = 0;
        createModifyMultisigAccount(userName, minimumApproval, minimumRemoval,
            removeHeader("cosignatory", operationList));
    }

    @When("^\"(\\w+)\" remove the last cosignatory of the multisignature:$")
    public void removeLastCosigner(final String userName, final Map<String, String> operationList) {
        final byte minimumApproval = -1;
        final byte minimumRemoval = -1;
        createModifyMultisigAccount(userName, minimumApproval, minimumRemoval,
            removeHeader("cosignatory", operationList));
    }

    @And("^\"(\\w+)\" accepted the transaction$")
    @When("^\"(\\w+)\" accepts the transaction$")
    public void cosignTransaction(final String cosigner) {
        final Account account = getUser(cosigner);
        final SignedTransaction signedTransaction = getTestContext().getSignedTransaction();
        final TransactionHelper transactionHelper = new TransactionHelper(getTestContext());
        final AggregateTransaction aggregateTransaction = transactionHelper
            .waitForBondedTransaction(signedTransaction);
        final AggregateHelper aggregateHelper = new AggregateHelper(getTestContext());
        aggregateHelper.cosignAggregateBonded(account, aggregateTransaction);
    }

    @And("^(\\w+) become a regular account$")
    public void verifyNotMultisigAccount(final String userName) {
        waitForLastTransactionToComplete();
        final Account account = getUser(userName);
        final Optional<MultisigAccountInfo> multisigAccountInfoOptional =
            new AccountHelper(getTestContext()).getMultisigAccountNoThrow(account.getAddress());
        Assertions.assertFalse(multisigAccountInfoOptional.isPresent(),
            "Account " + account.getAddress().pretty() + " is still multisig.");
    }

    @And("^(\\w+) created a contract to change approval by (-?\\d+) units and removal by (-?\\d+) units$")
    @When("^(\\w+) creates a contract to change approval by (-?\\d+) units and removal by (-?\\d+) units$")
    public void publishMultisigSettingsUpdate(final String userName, final byte approvalDetla,
        final byte removalDelta) {
        createModifyMultisigAccount(userName, approvalDetla, removalDelta, new HashMap<>());
    }
}
