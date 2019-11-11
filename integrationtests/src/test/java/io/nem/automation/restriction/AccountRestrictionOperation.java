package io.nem.automation.restriction;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import io.nem.automation.common.BaseTest;
import io.nem.automationHelpers.common.TestContext;
import io.nem.automationHelpers.helper.AccountRestrictionHelper;
import io.nem.automationHelpers.helper.NamespaceHelper;
import io.nem.sdk.model.account.Account;
import io.nem.sdk.model.transaction.AccountRestrictionType;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;


public class AccountRestrictionOperation extends BaseTest {
    private final AccountRestrictionHelper accountRestrictionHelper;
    private final String transactionType = "TRANSACTION_TYPE";
    private final String userBobby = "Bobby";
    private final String userAlex = "Alex";
    private final String userCarol = "Carol";

    /**
     * Constructor.
     *
     * @param testContext Test context.
     */
    public AccountRestrictionOperation(final TestContext testContext) {
        super(testContext);
        accountRestrictionHelper = new AccountRestrictionHelper(testContext);
    }

    @Given("^the following transaction types are available:$")
    public void theFollowingTransactionTypesAreAvailable(List<String> transactionTypes) {
        // By default all operations are available for all accounts unless
        // a restriction is added. Hence nothing to do for this step
    }

    @When("^(\\w+) blocks sending transactions of type:$")
    public void blocksSendingTransactionsOfType(String userName, List<Object> transactionTypes) {
        final Account userAccount = getUser(userName);
        accountRestrictionHelper.addAppropriateModificationTransactionAndWait(transactionType,
                transactionTypes,
                userAccount,
                AccountRestrictionType.BLOCK_OUTGOING_TRANSACTION_TYPE);
    }

    @When("^(\\w+) tries to block sending transactions of type:$")
    public void triesToBlockSendingTransactionsOfType(String userName, List<Object> transactionTypes) {
        final Account userAccount = getUser(userName);
        accountRestrictionHelper.addAppropriateModificationTransactionAndAnnounce(transactionType,
                transactionTypes,
                userAccount,
                AccountRestrictionType.BLOCK_OUTGOING_TRANSACTION_TYPE);
    }

    @When("^(\\w+) tries to register a Namespace$")
    public void triesToRegisterANamespace() {
        NamespaceHelper namespaceHelper = new NamespaceHelper(getTestContext());
        namespaceHelper.createRootNamespaceAndWait(getUser(userAlex),
                "sample.namespace",
                BigInteger.valueOf(100));
    }

    @When("^(\\w+) only allows sending transactions of type:$")
    public void onlyAllowsSendingTransactionsOfType(String userName, List<Object> transactionTypes) {
        final Account userAccount = getUser(userName);
        accountRestrictionHelper.addAppropriateModificationTransactionAndWait(transactionType,
                transactionTypes,
                userAccount,
                AccountRestrictionType.ALLOW_OUTGOING_TRANSACTION_TYPE);
    }

    @When("^(\\w+) tries to only allow sending transactions of type:$")
    public void triesToOnlyAllowSendingTransactionsOfType(String userName, List<Object> transactionTypes) {
        final Account userAccount = getUser(userName);
        accountRestrictionHelper.addAppropriateModificationTransactionAndAnnounce(transactionType,
                transactionTypes,
                userAccount,
                AccountRestrictionType.ALLOW_OUTGOING_TRANSACTION_TYPE);
    }

    @When("^(\\w+) unblocks \"([^\"]*)\" transaction type$")
    public void unblocksAnOperation(String userName, String operation) {
        final Account userAccount = getUser(userName);
        List<Object> operationToUnblock = Arrays.asList(operation);
        accountRestrictionHelper.removeAppropriateModificationTransactionAndAnnounce(transactionType,
                operationToUnblock,
                userAccount,
                AccountRestrictionType.BLOCK_OUTGOING_TRANSACTION_TYPE);
    }

    @When("^(\\w+) removes \"([^\"]*)\" from the allowed transaction types$")
    public void removesFromTheAllowedTransactionTypes(String userName, String operation) {
        final Account userAccount = getUser(userName);
        List<Object> transferTransaction = Arrays.asList(operation);
        accountRestrictionHelper.removeAppropriateModificationTransactionAndAnnounce(transactionType,
                transferTransaction,
                userAccount,
                AccountRestrictionType.ALLOW_OUTGOING_TRANSACTION_TYPE);
    }
}
