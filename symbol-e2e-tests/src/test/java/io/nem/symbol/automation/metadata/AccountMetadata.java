package io.nem.symbol.automation.metadata;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import io.nem.symbol.automation.common.BaseTest;
import io.nem.symbol.automationHelpers.common.TestContext;
import io.nem.symbol.automationHelpers.helper.AccountMetadataHelper;
import io.nem.symbol.automationHelpers.helper.AggregateHelper;
import io.nem.symbol.automationHelpers.helper.CommonHelper;
import io.nem.symbol.automationHelpers.helper.TransactionHelper;
import io.nem.symbol.sdk.api.MetadataRepository;
import io.nem.symbol.sdk.model.account.Account;
import io.nem.symbol.sdk.model.metadata.Metadata;
import io.nem.symbol.sdk.model.metadata.MetadataType;
import io.nem.symbol.sdk.model.transaction.AccountMetadataTransaction;
import io.nem.symbol.sdk.model.transaction.AggregateTransaction;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class AccountMetadata extends BaseTest {

  public AccountMetadata(final TestContext testContext) {
    super(testContext);
  }

  private void storeDocumentInfo(
      final String documentName, final BigInteger key, final String document) {
    getTestContext().getScenarioContext().setContext(documentName, Pair.of(key, document));
  }

  private Pair<BigInteger, String> getDocumentInfo(final String documentName) {
    return getTestContext().getScenarioContext().getContext(documentName);
  }

  @Given("^(\\w+) request (\\w+) to notarized her \"(.+?)\"$")
  public void createDigitalDocument(
      final String userName, final String notaryName, final String documentName) {
    final Account userAccount = getUserWithCurrency(userName);
    final Account notaryAccount = getUser(notaryName);
    final BigInteger documentKey = BigInteger.TEN; //BigInteger.valueOf(new Random().nextLong());
    final String document = CommonHelper.getRandonStringWithMaxLength(512);
    final AccountMetadataTransaction accountMetadataTransaction =
        new AccountMetadataHelper(getTestContext())
            .createAccountMetadataTransaction(
                userAccount.getPublicAccount(),
                documentKey,
                (short) document.getBytes().length,
                document);
    final AggregateTransaction aggregateTransaction =
        new AggregateHelper(getTestContext())
            .createAggregateBondedTransaction(
                Arrays.asList(
                    accountMetadataTransaction.toAggregate(notaryAccount.getPublicAccount())));
    final TransactionHelper transactionHelper = new TransactionHelper(getTestContext());
    transactionHelper.signTransaction(aggregateTransaction, notaryAccount);
    storeDocumentInfo(documentName, documentKey, document);
  }

  @Then("^(\\w+) should have her \"(.+?)\" attached to the account by (\\w+)$")
  public void verifyDigitDocument(
      final String targetName, final String documentName, final String senderName) {
    waitForLastTransactionToComplete();
    final Account targetAccount = getUser(targetName);
    final Account senderAccount = getUser(senderName);
    final Pair<BigInteger, String> documentInfoKey = getDocumentInfo(documentName);
    final MetadataRepository metadataRepository =
        getTestContext().getRepositoryFactory().createMetadataRepository();
//    final List<Metadata> metadata =
//        metadataRepository
//            .getAccountMetadataByKey(
//                targetAccount.getAddress(), documentInfoKey.getKey())
//            .blockingFirst();
    final Metadata metadata =
            metadataRepository
                    .getAccountMetadataByKeyAndSender(
                            targetAccount.getAddress(), documentInfoKey.getKey(), senderAccount.getPublicKey())
                    .blockingFirst();
    assertEquals("Document did not match", documentInfoKey.getValue(), metadata.getMetadataEntry().getValue());
    assertEquals("Document type doesn't match", MetadataType.ACCOUNT, metadata.getMetadataEntry().getMetadataType());
    assertEquals("Document key doesn't match", documentInfoKey.getKey(), metadata.getMetadataEntry().getScopedMetadataKey());
    assertEquals("Sender public key doesn't match", senderAccount.getPublicKey(), metadata.getMetadataEntry().getSenderPublicKey());
    assertEquals("Owner public key doesn't match", targetAccount.getPublicKey(), metadata.getMetadataEntry().getTargetPublicKey());
    if (metadata.getMetadataEntry().getMetadataType() != MetadataType.ACCOUNT) {
      assertEquals("Target id does not match", 0, metadata.getMetadataEntry().getTargetId().get());
    }
  }
}
