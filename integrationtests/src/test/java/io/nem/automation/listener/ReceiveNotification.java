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

package io.nem.automation.listener;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.nem.automation.common.BaseTest;
import io.nem.automationHelpers.common.TestContext;
import io.nem.automationHelpers.helper.AggregateHelper;
import io.nem.sdk.api.Listener;
import io.nem.sdk.model.account.*;
import io.nem.sdk.model.blockchain.BlockInfo;
import io.nem.sdk.model.transaction.CosignatureSignedTransaction;
import io.nem.sdk.model.transaction.SignedTransaction;
import io.nem.sdk.model.transaction.Transaction;
import io.nem.sdk.model.transaction.TransactionStatusError;
import io.reactivex.Observable;

import java.util.HashMap;
import java.util.List;
import java.util.function.BiFunction;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ReceiveNotification extends BaseTest {

  final String BLOCK_INFO_NAME = "blockInfo";
  final String OBSERVABLE_NAME = "observable";

  public ReceiveNotification(final TestContext testContext) {
    super(testContext);
  }

  private String getObservableName(final String userName) {
    return userName + OBSERVABLE_NAME;
  }

  private <T> void setObservable(
      final String userName, final BiFunction<Address, Listener, Observable<T>> supplier) {
    final Account account = getUser(userName);
    final Listener listener = openListener();
    setObservable(userName, supplier.apply(account.getAddress(), listener));
  }

  private <T extends Transaction> void setTransactionObservable(
      final String userName, final BiFunction<Address, Listener, Observable<T>> supplier) {
    setObservable(userName, supplier);
  }

  private void setHashObservable(
      final String userName, final BiFunction<Address, Listener, Observable<String>> supplier) {
    setObservable(userName, supplier);
  }

  private void setCosignerObservable(
      final String userName,
      final BiFunction<Address, Listener, Observable<CosignatureSignedTransaction>> supplier) {
    setObservable(userName, supplier);
  }

  private <T> Observable<T> getObservable(final String userName) {
    return getTestContext().getScenarioContext().getContext(getObservableName(userName));
  }

  private <T> void setObservable(final String userName, final Observable<T> observable) {
    getTestContext().getScenarioContext().setContext(getObservableName(userName), observable);
  }

  private <T extends Transaction> Observable<T> getTransactionObservable(final String userName) {
    return getObservable(userName);
  }

  private Observable<CosignatureSignedTransaction> getCosignTransactionObservable(
      final String userName) {
    return getObservable(userName);
  }

  private Observable<String> getHashObservable(final String userName) {
    return getObservable(userName);
  }

  private Observable<TransactionStatusError> getErrorObservable(final String userName) {
    return getObservable(userName);
  }

  @Given("^(\\w+) is register to receive notification from the blockchain$")
  public void registerListener(final String username) {
    openListener();
  }

  @When("^(\\w+) waits for a next block$")
  public void waitForNextBlock(final String username) {
    Listener listener = getListener();
    final BlockInfo blockInfo = listener.newBlock().take(1).blockingFirst();
    getTestContext().getScenarioContext().setContext(BLOCK_INFO_NAME, blockInfo);
  }

  @Then("^(\\w+) should receive a block notification$")
  public void verifyBlock(final String username) {
    final BlockInfo blockInfo = getTestContext().getScenarioContext().getContext(BLOCK_INFO_NAME);
    final BlockInfo blockInfoDb =
        getTestContext()
            .getRepositoryFactory()
            .createBlockRepository()
            .getBlockByHeight(blockInfo.getHeight())
            .blockingFirst();
    assertEquals(
        "Block previous hash didn't match",
        blockInfo.getPreviousBlockHash(),
        blockInfoDb.getPreviousBlockHash());
    assertEquals("Block hash didn't match", blockInfo.getHash(), blockInfoDb.getHash());
    assertEquals(
        "Block generation hash didn't match",
        blockInfo.getGenerationHash(),
        blockInfoDb.getGenerationHash());
    assertEquals(
        "Block state hash didn't match", blockInfo.getStateHash(), blockInfoDb.getStateHash());
    assertEquals(
        "Block signature didn't match", blockInfo.getSignature(), blockInfoDb.getSignature());
  }

  @Given("^(\\w+) register to receive unconfirmed transaction notification$")
  public void registerUnconfirmedListener(final String username) {
    setTransactionObservable(username, (address, listener) -> listener.unconfirmedAdded(address));
  }

  @Then("^(\\w+) should receive a transaction notification")
  public void listenForTransactionNotification(final String userName) {
    final String hash = getTestContext().getSignedTransaction().getHash();
    final Transaction transaction =
        getTransactionObservable(userName)
            .filter(t -> t.getTransactionInfo().get().getHash().get().equalsIgnoreCase(hash))
            .blockingFirst();
    assertTrue("The correct message was not received for hash " + hash, transaction != null);
  }

  @When("^(\\w+) should receive a notification that the cosigner have signed the transaction")
  @Then("^(\\w+) should receive a remove notification")
  public void listenForRemoveNotification(final String userName) {
    final String hash = getTestContext().getSignedTransaction().getHash();
    final boolean found =
        !getHashObservable(userName).filter(h -> h.equalsIgnoreCase(hash)).isEmpty().blockingGet();
    assertTrue("The remove message was not received for hash " + hash, found);
  }

  @Then("^(\\w+) should receive an error notification")
  public void listenForErrorNotification(final String userName) {
    final String hash = getTestContext().getSignedTransaction().getHash();
    final boolean found =
        !getErrorObservable(userName)
            .filter(status -> status.getHash().equalsIgnoreCase(hash))
            .isEmpty()
            .blockingGet();
    assertTrue("The remove message was not received for hash " + hash, found);
  }

  @Given("^(\\w+) register to receive confirmed transaction notification$")
  public void registerConfirmedListener(final String username) {
    setTransactionObservable(username, (address, listener) -> listener.confirmed(address));
  }

  @Given("^(\\w+) register to receive notification when unconfirmed transaction is removed$")
  public void registerUnconfirmedRemoveListener(final String username) {
    setHashObservable(username, (address, listener) -> listener.unconfirmedRemoved(address));
  }

  @Given("^(\\w+) register to receive error transaction notification$")
  public void registerErrorListener(final String username) {
    setObservable(username, (address, listener) -> listener.status(address));
  }

  @Given("^(\\w+) register to receive a notification when a bonded transaction requires signing$")
  public void registerBondedListener(final String username) {
    setObservable(username, (address, listener) -> listener.aggregateBondedAdded(address));
  }

  @Given(
      "^(\\w+) register to receive notification when bonded transaction is signed by all cosigners$")
  public void registerBondedRemoveListener(final String username) {
    setHashObservable(username, (address, listener) -> listener.aggregateBondedRemoved(address));
  }

  @Given("^(\\w+) register to receive notification when bonded transaction is signed by (\\w+)$")
  public void registerCosignListener(final String username, final String cosigner) {
    setCosignerObservable(cosigner, (address, listener) -> listener.cosignatureAdded(address));
  }

  @Then("^(\\w+) should receive a cosign transaction notification that (\\w+) cosigned")
  public void listenForCosignTransactionNotification(final String userName, final String cosigner) {
    final SignedTransaction signedTransaction = getTestContext().getSignedTransaction();
    final CosignatureSignedTransaction cosignatureSignedTransaction =
        getCosignTransactionObservable(cosigner)
            .filter(t -> t.getParentHash().equalsIgnoreCase(signedTransaction.getHash()))
            .blockingFirst();
    assertTrue(
        "The correct message was not received for hash " + signedTransaction.getHash(),
        cosignatureSignedTransaction != null);
    assertEquals(
        "Signer does not match",
        cosignatureSignedTransaction.getSigner().toUpperCase(),
        getUser(cosigner).getPublicKey().toUpperCase());
  }

  private boolean isMultisig(
      final MultisigAccountGraphInfo multisigAccountGraphInfo, final PublicAccount publicAccount) {
    return multisigAccountGraphInfo.getMultisigAccounts().values().stream()
        .anyMatch(
            m ->
                m.stream()
                    .anyMatch(
                        s ->
                            s.getAccount()
                                .getPublicKey()
                                .toHex()
                                .equalsIgnoreCase(publicAccount.getPublicKey().toHex())));
  }

  @Given("^(\\w+) register to auto sign bonded contracts$")
  public void registerAutoSignBondedListener(final String username) {
    final Account account = getUser(username);
    final MultisigAccountGraphInfo multisigAccountGraphInfo =
        getTestContext()
            .getRepositoryFactory()
            .createMultisigRepository()
            .getMultisigAccountGraphInfo(account.getAddress())
            .onErrorReturnItem(
                new MultisigAccountGraphInfo(new HashMap<Integer, List<MultisigAccountInfo>>()))
            .blockingFirst();
    final AggregateHelper aggregateHelper = new AggregateHelper(getTestContext());
    if (multisigAccountGraphInfo.getMultisigAccounts().isEmpty()) {
      getListener()
          .aggregateBondedAdded(account.getAddress())
          .subscribe(t -> aggregateHelper.cosignAggregateBonded(account, t));
      return;
    }

    multisigAccountGraphInfo.getMultisigAccounts().values().stream()
        .flatMap(m -> m.stream())
        .distinct()
        .forEach(
            multisigAccountInfo -> {
              if (!isMultisig(multisigAccountGraphInfo, multisigAccountInfo.getAccount())) {
                getListener()
                    .aggregateBondedAdded(multisigAccountInfo.getAccount().getAddress())
                    .subscribe(
                        t ->
                            aggregateHelper.cosignAggregateBonded(
                                getUserAccountFromContext(
                                    multisigAccountInfo.getAccount().getAddress()),
                                t));
              }
            });
  }
}
