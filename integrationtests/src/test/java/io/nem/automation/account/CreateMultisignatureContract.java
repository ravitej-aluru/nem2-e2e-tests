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
import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.nem.automation.common.BaseTest;
import io.nem.automationHelpers.common.TestContext;
import io.nem.automationHelpers.helper.*;
import io.nem.core.utils.ConvertUtils;
import io.nem.sdk.infrastructure.directconnect.dataaccess.mappers.MapperUtils;
import io.nem.sdk.model.account.Account;
import io.nem.sdk.model.account.MultisigAccountInfo;
import io.nem.sdk.model.account.PublicAccount;
import io.nem.sdk.model.transaction.AggregateTransaction;
import io.nem.sdk.model.transaction.MultisigAccountModificationTransaction;
import io.nem.sdk.model.transaction.SignedTransaction;
import io.nem.sdk.model.transaction.TransactionType;

import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/** Create multisignature contract. */
public class CreateMultisignatureContract extends BaseTest {
  private final MultisigAccountHelper multisigAccountHelper;

  /**
   * Constructor.
   *
   * @param testContext Test context.
   */
  public CreateMultisignatureContract(final TestContext testContext) {
    super(testContext);
    multisigAccountHelper = new MultisigAccountHelper(testContext);
  }

  private List<Account> getCosignersForAccount(final Account account) {
    Optional<MultisigAccountInfo> multisigAccountInfoOptional =
        new AccountHelper(getTestContext()).getMultisigAccountNoThrow(account.getAddress());
    List<Account> cosigners = new Vector<>();
    if (multisigAccountInfoOptional.isPresent()) {
      cosigners =
          multisigAccountInfoOptional
              .get()
              .getCosignatories()
              .parallelStream()
              .map(
                  publicAccount -> {
                    final Account cosignerAccount =
                        getUserAccountFromContext(publicAccount.getAddress());
                    return getCosignersForAccount(cosignerAccount);
                  })
              .flatMap(Collection::stream)
              .collect(Collectors.toList());
    }
    if (cosigners.isEmpty()) {
      cosigners.add(account);
    }
    return cosigners;
  }

  private void createMultisigAccount(
      final String userName,
      final byte minimumApproval,
      final byte numberOfCosignatory,
      final String multisigAccountName,
      final byte minimumRemoval,
      final List<String> cosignatories) {
    final Account signerAccount = getUser(userName);
    final Account multiSigAccount = getUserWithCurrency(multisigAccountName);
    getTestContext()
        .getLogger()
        .LogError(
            "MultiSig account "
                + multisigAccountName
                + " public key:"
                + multiSigAccount.getPublicKey());
    final List<Account> cosignerAccounts = new Vector<>(cosignatories.size());
    final List<PublicAccount> accountsAdditions =
        cosignatories
            .parallelStream()
            .map(
                name -> {
                  final Account account = getUserWithCurrency(name);
                  getTestContext()
                      .getLogger()
                      .LogError(
                          "Cosigner account "
                              + name
                              + " public key:"
                              + account.getPublicKey()
                              + " address: "
                              + ConvertUtils.toHex(
                                  MapperUtils.fromAddressToByteBuffer(account.getAddress())
                                      .array()));
                  final List<Account> cosigners = getCosignersForAccount(account);
                  cosignerAccounts.addAll(cosigners);
                  return account.getPublicAccount();
                })
            .collect(Collectors.toList());
    final MultisigAccountModificationTransaction modifyMultisigAccountTransaction =
        multisigAccountHelper.createMultisigAccountModificationTransaction(
            minimumApproval, minimumRemoval, accountsAdditions, new ArrayList<>());
    getTestContext().addTransaction(modifyMultisigAccountTransaction);
    final AggregateTransaction aggregateTransaction =
        new AggregateHelper(getTestContext())
            .createAggregateBondedTransaction(
                Arrays.asList(
                    modifyMultisigAccountTransaction.toAggregate(
                        multiSigAccount.getPublicAccount())));
    final TransactionHelper transactionHelper = new TransactionHelper(getTestContext());
    transactionHelper.signTransaction(aggregateTransaction, multiSigAccount);
    getTestContext().getScenarioContext().setContext(COSIGNATORIES_LIST, cosignerAccounts);
    getTestContext().getScenarioContext().setContext(MULTISIG_ACCOUNT_INFO, multiSigAccount);
  }

  private int getMultisigAccountLevelDepth(final PublicAccount account) {
    final MultisigAccountInfo multisigAccountInfo;
    int level = 0;

    try {
      multisigAccountInfo =
          new AccountHelper(getTestContext()).getMultisigAccount(account.getAddress());
    } catch (final Exception e) {
      return level;
    }
    final List<PublicAccount> cosigners = multisigAccountInfo.getCosignatories();
    for (final PublicAccount cosigner : cosigners) {
      final int cosignerLevel = getMultisigAccountLevelDepth(cosigner) + 1;
      if (cosignerLevel > level) {
        level = cosignerLevel;
      }
    }
    return level;
  }

  @Given(
      "^(\\w+) defined a (-?\\d+) of (-?\\d+) multisignature contract called \"(.*)\" with (-?\\d+) required for removal with cosignatories:$")
  public void definedMultiSignatureContract(
      final String userName,
      final byte minimumApproval,
      final byte numberOfCosignatory,
      final String multisigAccountName,
      final byte minimumRemoval,
      final List<String> cosignatories) {
    createMultisigAccount(
        userName,
        minimumApproval,
        numberOfCosignatory,
        multisigAccountName,
        minimumRemoval,
        cosignatories.subList(1, cosignatories.size()));
  }

  private List<String> generateCosignerList(final byte numberOfCosignatory) {
    final List<String> cosignatories = new ArrayList<>(numberOfCosignatory);
    for (int i = 0; i < numberOfCosignatory; i++) {
      cosignatories.add("cosigner" + i);
    }
    return cosignatories;
  }

  @Given("^(\\w+) tries to define a (-?\\d+) of (-?\\d+) multisignature contract called \"(.*)\"$")
  public void definedMultiSignatureContractWithCosigners(
      final String userName,
      final byte minimumApproval,
      final byte numberOfCosignatory,
      final String multisigAccountName) {
    final List<String> cosignatories = generateCosignerList(numberOfCosignatory);
    final byte minimumRemoval = 1;
    createMultisigAccount(
        userName,
        minimumApproval,
        numberOfCosignatory,
        multisigAccountName,
        minimumRemoval,
        cosignatories);
  }

  @And("^(\\w+) published the bonded contract")
  @When("^(\\w+) publishes the bonded contract")
  public void publishBondedTransaction(final String userName) {
    final Account account = getUser(AUTOMATION_USER_ALICE);
    final SignedTransaction signedTransaction = getTestContext().getSignedTransaction();
    final BigInteger duration = BigInteger.valueOf(10);
    final AggregateHelper aggregateHelper = new AggregateHelper(getTestContext());
    aggregateHelper.submitLockFundForBondedTransaction(account, signedTransaction, duration);
    final TransactionHelper transactionHelper = new TransactionHelper(getTestContext());
    transactionHelper.announceAggregateBonded(signedTransaction);
    getTestContext().setSignedTransaction(signedTransaction);
  }

  @And("^(\\w+) published the contract")
  @When("^(\\w+) publishes the contract")
  public void publishTransaction(final String userName) {
    final SignedTransaction signedTransaction = getTestContext().getSignedTransaction();
    final TransactionHelper transactionHelper = new TransactionHelper(getTestContext());
    transactionHelper.announceTransaction(signedTransaction);
  }

  @And("^all the required cosignatories sign the transaction$")
  public void cosignMultiSignatureAccount() {
    final List<Account> cosignatories =
        getTestContext().getScenarioContext().getContext(COSIGNATORIES_LIST);
    final SignedTransaction signedTransaction = getTestContext().getSignedTransaction();
    final AccountHelper accountHelper = new AccountHelper(getTestContext());
    final AggregateTransaction aggregateTransaction =
        accountHelper.getAggregateBondedTransaction(signedTransaction);
    final AggregateHelper aggregateHelper = new AggregateHelper(getTestContext());
    cosignatories.forEach(
        (final Account account) -> {
          List<AggregateTransaction> transactions =
              getTestContext()
                  .getRepositoryFactory()
                  .createAccountRepository()
                  .aggregateBondedTransactions(account.getPublicAccount())
                  .blockingFirst();
          aggregateHelper.cosignAggregateBonded(account, aggregateTransaction);
        });
  }

  @And("^(\\w+) account is convert to multisig$")
  public void verifyMultiSignatureAccount(final String accountName) {
    waitForLastTransactionToComplete();
    final Account multisigAccount = getUser(accountName);
    final MultisigAccountInfo multisigAccountInfo =
        new AccountHelper(getTestContext())
            .getMultisigAccountWithRetry(multisigAccount.getAddress());
    final MultisigAccountModificationTransaction modifyMultisigAccountTransaction =
        getTestContext()
            .<MultisigAccountModificationTransaction>findTransaction(
                TransactionType.MULTISIG_ACCOUNT_MODIFICATION)
            .get();
    final String errorMessage =
        "Multisig Account " + multisigAccount.getAddress().pretty() + " failed to verification.";
    assertEquals(errorMessage, true, multisigAccountInfo.isMultisig());
    assertEquals(
        errorMessage,
        modifyMultisigAccountTransaction.getMinApprovalDelta(),
        multisigAccountInfo.getMinApproval());
    assertEquals(
        errorMessage,
        modifyMultisigAccountTransaction.getMinRemovalDelta(),
        multisigAccountInfo.getMinRemoval());
    for (final PublicAccount publicAccount :
        modifyMultisigAccountTransaction.getPublicKeyAdditions()) {
      assertEquals(
          errorMessage, true, multisigAccountInfo.getCosignatories().contains(publicAccount));
    }
    for (final PublicAccount publicAccount :
        modifyMultisigAccountTransaction.getPublicKeyDeletions()) {
      assertEquals(
          errorMessage, false, multisigAccountInfo.getCosignatories().contains(publicAccount));
    }
  }

  @And("^(\\w+) multisignature contract should be updated$")
  public void verifyUpdatedMultiSignatureAccount(final String userName) {
    verifyMultiSignatureAccount(userName);
  }

  @Given(
      "^(\\w+) created a (-?\\d+) of (-?\\d+) multisignature contract called \"(.*)\" with (-?\\d+) required for removal with "
          + "cosignatories:$")
  public void createMultiSignatureContract(
      final String userName,
      final byte minimumApproval,
      final byte numberOfCosignatory,
      final String multisigAccountName,
      final byte minimumRemoval,
      final List<String> cosignatories) {
    createMultisigAccount(
        userName,
        minimumApproval,
        numberOfCosignatory,
        multisigAccountName,
        minimumRemoval,
        cosignatories.subList(1, cosignatories.size()));
    publishBondedTransaction(userName);
    cosignMultiSignatureAccount();
    final AggregateTransaction aggregateTransaction = waitForLastTransactionToComplete();
    getTestContext().addTransaction(aggregateTransaction);
  }

  @Given("^(\\w+) created a (-?\\d+) of (-?\\d+) multisignature contract called \"(.*)\"$")
  public void createMultiSignatureContractWithCosigners(
      final String userName,
      final byte minimumApproval,
      final byte numberOfCosignatory,
      final String multisigAccountName) {
    final byte minimumRemoval = 1;
    final List<String> cosignatories = generateCosignerList(numberOfCosignatory);
    createMultisigAccount(
        userName,
        minimumApproval,
        numberOfCosignatory,
        multisigAccountName,
        minimumRemoval,
        cosignatories);
    publishBondedTransaction(userName);
    cosignMultiSignatureAccount();
    final AggregateTransaction aggregateTransaction = waitForLastTransactionToComplete();
    getTestContext().addTransaction(aggregateTransaction);
  }

  @Given("^(\\w+) is cosignatory of (\\d+) multisignature contracts$")
  public void addToMultiSignatureContracts(final String userName, final int numberOfContracts) {
    final String signer = AUTOMATION_USER_ALICE;
    final byte minimumApproval = 1;
    final byte minimumRemoval = 1;
    final byte numberOfCosignatory = 1;
    final String multisigAccountName = "harry";
    final Runnable runnable =
        () -> {
          createMultisigAccount(
              signer,
              minimumApproval,
              numberOfCosignatory,
              multisigAccountName,
              minimumRemoval,
              Arrays.asList(userName));
          publishBondedTransaction(signer);
          cosignMultiSignatureAccount();
        };
    CommonHelper.executeInParallel(runnable, numberOfContracts, 8 * BLOCK_CREATION_TIME_IN_SECONDS);
  }

  @Then("^the multisignature contract should become a (\\d+) level multisignature contract$")
  public void verifyContractLevel(final int level) {
    waitForLastTransactionToComplete();
    final Account multisigAccount =
        getTestContext().getScenarioContext().getContext(MULTISIG_ACCOUNT_INFO);
    assertEquals(
        "Multisig account level did not match.",
        level,
        getMultisigAccountLevelDepth(multisigAccount.getPublicAccount()));
  }
}
