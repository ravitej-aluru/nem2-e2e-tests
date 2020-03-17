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

package io.nem.symbol.automation.account;

import cucumber.api.java.en.And;
import cucumber.api.java.en.When;
import io.nem.symbol.automation.common.BaseTest;
import io.nem.symbol.automationHelpers.common.TestContext;
import io.nem.symbol.automationHelpers.helper.AccountHelper;
import io.nem.symbol.automationHelpers.helper.AggregateHelper;
import io.nem.symbol.automationHelpers.helper.MultisigAccountHelper;
import io.nem.symbol.automationHelpers.helper.TransactionHelper;
import io.nem.symbol.sdk.model.account.Account;
import io.nem.symbol.sdk.model.account.MultisigAccountInfo;
import io.nem.symbol.sdk.model.account.PublicAccount;
import io.nem.symbol.sdk.model.transaction.*;

import java.util.*;

import static org.junit.Assert.assertFalse;

/** Edit multisignature contract. */
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

  private AggregateTransaction createAggregateTransaction(
      final boolean isBonded, final List<Transaction> innerTransaction) {
    final AggregateTransaction aggregateTransaction =
        isBonded
            ? new AggregateHelper(getTestContext())
                .createAggregateBondedTransaction(innerTransaction)
            : new AggregateHelper(getTestContext())
                .createAggregateCompleteTransaction(innerTransaction);
    return aggregateTransaction;
  }

  private void createModifyMultisigAccount(
      final String userName,
      final byte minimumApproval,
      final byte minimumRemoval,
      final List<List<String>> operationList) {
    final Account signerAccount = getUser(userName);
    final Account multiSigAccount =
        getTestContext().getScenarioContext().getContext(MULTISIG_ACCOUNT_INFO);
    final MultisigAccountModificationTransaction originalMultisigAccountModificationTransaction =
        getTestContext()
            .<MultisigAccountModificationTransaction>findTransaction(
                TransactionType.MULTISIG_ACCOUNT_MODIFICATION)
            .get();
    final List<PublicAccount> accountsAdditions =
        originalMultisigAccountModificationTransaction.getPublicKeyAdditions();
    final List<PublicAccount> accountsDeletions =
        originalMultisigAccountModificationTransaction.getPublicKeyDeletions();
    getTestContext().clearTransaction();
    boolean requireBondedTransaction =
        originalMultisigAccountModificationTransaction.getMinApprovalDelta() > 1;
    final List<PublicAccount> publicAccountsAddition = new ArrayList<>();
    final List<PublicAccount> publicAccountsDeletion = new ArrayList<>();
    for (List<String> entry : operationList) {
      final Account account = getUser(entry.get(0));
      if (entry.get(1).equalsIgnoreCase("add")) {
        publicAccountsAddition.add(account.getPublicAccount());
        accountsAdditions.add(account.getPublicAccount());
        requireBondedTransaction = true;
      } else if (entry.get(1).equalsIgnoreCase("remove")) {
        publicAccountsDeletion.add(account.getPublicAccount());
        accountsDeletions.add(account.getPublicAccount());
        accountsAdditions.removeIf(
            publicAccount -> publicAccount.equals(account.getPublicAccount()));
      }
    }

    final MultisigAccountModificationTransaction modifyMultisigAccountTransaction =
        multisigAccountHelper.createMultisigAccountModificationTransaction(
            minimumApproval, minimumRemoval, publicAccountsAddition, publicAccountsDeletion);
    final List<Transaction> innerTransactions =
        Arrays.asList(
            modifyMultisigAccountTransaction.toAggregate(multiSigAccount.getPublicAccount()));
    final AggregateTransaction aggregateTransaction =
        createAggregateTransaction(requireBondedTransaction, innerTransactions);
    final TransactionHelper transactionHelper = new TransactionHelper(getTestContext());
    transactionHelper.signTransaction(aggregateTransaction, signerAccount);
    final byte newApproval =
        (byte)
            (originalMultisigAccountModificationTransaction.getMinApprovalDelta()
                + minimumApproval);
    final byte newRemoval =
        (byte)
            (originalMultisigAccountModificationTransaction.getMinRemovalDelta() + minimumRemoval);
    final MultisigAccountModificationTransaction newMultisigAccountModificationTransaction =
        multisigAccountHelper.createMultisigAccountModificationTransaction(
            newApproval, newRemoval, accountsAdditions, accountsDeletions);
    getTestContext().addTransaction(newMultisigAccountModificationTransaction);
  }

  @And("^\"(\\w+)\" update the cosignatories of the multisignature:$")
  public void modifyCosignList(final String userName, final List<List<String>> operationList) {
    final byte minimumApproval = 0;
    final byte minimumRemoval = 0;
    createModifyMultisigAccount(
        userName, minimumApproval, minimumRemoval, removeHeader(operationList));
  }

  @When("^\"(\\w+)\" remove the last cosignatory of the multisignature:$")
  public void removeLastCosigner(final String userName, final List<List<String>> operationList) {
    final byte minimumApproval = -1;
    final byte minimumRemoval = -1;
    createModifyMultisigAccount(
        userName, minimumApproval, minimumRemoval, removeHeader(operationList));
  }

  @And("^\"(\\w+)\" accepted the transaction$")
  @When("^\"(\\w+)\" accepts the transaction$")
  public void cosignTransaction(final String cosigner) {
    final Account account = getUser(cosigner);
    final SignedTransaction signedTransaction = getTestContext().getSignedTransaction();
    final AccountHelper accountHelper = new AccountHelper(getTestContext());
    final AggregateTransaction aggregateTransaction =
        accountHelper.getAggregateBondedTransaction(
            PublicAccount.createFromPublicKey(
                signedTransaction.getSigner().getPublicKey().toHex(),
                getTestContext().getNetworkType()),
            signedTransaction);
    // TODO: update when bug is fix - https://github.com/nemtech/catapult-rest/issues/244
    //    accountHelper.getAggregateBondedTransaction(account.getPublicAccount(),
    // signedTransaction);
    final AggregateHelper aggregateHelper = new AggregateHelper(getTestContext());
    final String hash = aggregateTransaction.getTransactionInfo().get().getHash().get();
    int numOfCosigner = getTestContext().getScenarioContext().isContains(hash) ? getTestContext().getScenarioContext().getContext(hash) : 0;
    getTestContext().getScenarioContext().setContext(hash, numOfCosigner + 1);
    aggregateHelper.cosignAggregateBonded(account, aggregateTransaction);
  }

  @And("^(\\w+) become a regular account$")
  public void verifyNotMultisigAccount(final String userName) {
    waitForLastTransactionToComplete();
    final Account account = getUser(userName);
    final Optional<MultisigAccountInfo> multisigAccountInfoOptional =
        new AccountHelper(getTestContext()).getMultisigAccountNoThrow(account.getAddress());
    assertFalse(
        "Account " + account.getAddress().pretty() + " is still multisig.",
        multisigAccountInfoOptional.isPresent());
  }

  @And(
      "^(\\w+) created a contract to change approval by (-?\\d+) units and removal by (-?\\d+) units$")
  @When(
      "^(\\w+) creates a contract to change approval by (-?\\d+) units and removal by (-?\\d+) units$")
  public void publishMultisigSettingsUpdate(
      final String userName, final byte approvalDetla, final byte removalDelta) {
    createModifyMultisigAccount(userName, approvalDetla, removalDelta, new ArrayList<>());
  }
}
