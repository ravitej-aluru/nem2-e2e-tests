package io.nem.automation.restriction;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import io.nem.automation.common.BaseTest;
import io.nem.automationHelpers.common.TestContext;
import io.nem.automationHelpers.helper.AccountRestrictionHelper;
import io.nem.sdk.model.account.Account;
import io.nem.sdk.model.transaction.AccountRestrictionType;
import io.nem.sdk.model.transaction.TransactionType;

import java.util.ArrayList;
import java.util.List;


public class AccountRestrictionOperation extends BaseTest {
    private final AccountRestrictionHelper accountRestrictionHelper;

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
    public void blocksSendingTransactionsOfType(String userName, List<String> transactionTypes) {
        final Account userAccount = getUser(userName);
        List<TransactionType> additions = new ArrayList<>();
        transactionTypes.parallelStream().forEach(transactionType -> additions.add(TransactionType.valueOf(transactionType)));
        accountRestrictionHelper.createAccountTransactionTypeRestrictionTransactionAndWait(userAccount,
                AccountRestrictionType.BLOCK_OUTGOING_TRANSACTION_TYPE, additions, new ArrayList<>());
    }

    @When("^(\\w+) tries to block sending transactions of type:$")
    public void triesToBlockSendingTransactionsOfType(String userName, List<String> transactionTypes) {
        final Account userAccount = getUser(userName);
        List<TransactionType> additions = new ArrayList<>();
        transactionTypes.parallelStream().forEach(transactionType -> additions.add(TransactionType.valueOf(transactionType)));
        accountRestrictionHelper.createAccountTransactionTypeRestrictionTransactionAndAnnounce(userAccount,
                AccountRestrictionType.BLOCK_OUTGOING_TRANSACTION_TYPE, additions, new ArrayList<>());
    }

    @When("^(\\w+) only allows sending transactions of type:$")
    public void onlyAllowsSendingTransactionsOfType(String userName, List<String> transactionTypes) {
        final Account userAccount = getUser(userName);
        List<TransactionType> additions = new ArrayList<>();
        transactionTypes.parallelStream().forEach(transactionType -> additions.add(TransactionType.valueOf(transactionType)));
        accountRestrictionHelper.createAccountTransactionTypeRestrictionTransactionAndWait(userAccount,
                AccountRestrictionType.ALLOW_OUTGOING_TRANSACTION_TYPE, additions, new ArrayList<>());
    }

    @When("^(\\w+) tries to only allow sending transactions of type:$")
    public void triesToOnlyAllowSendingTransactionsOfType(String userName, List<String> transactionTypes) {
        final Account userAccount = getUser(userName);
        List<TransactionType> additions = new ArrayList<>();
        transactionTypes.parallelStream().forEach(transactionType -> additions.add(TransactionType.valueOf(transactionType)));
        accountRestrictionHelper.createAccountTransactionTypeRestrictionTransactionAndAnnounce(userAccount,
                AccountRestrictionType.ALLOW_OUTGOING_TRANSACTION_TYPE, additions, new ArrayList<>());
    }

    @When("^(\\w+) unblocks ([^\"]*) transaction type$")
    public void unblocksAnOperation(String userName, String operation) {
        final Account userAccount = getUser(userName);
        List<TransactionType> deletions = new ArrayList<>();
        deletions.add(TransactionType.valueOf(operation));
        accountRestrictionHelper.createAccountTransactionTypeRestrictionTransactionAndWait(userAccount,
                AccountRestrictionType.BLOCK_OUTGOING_TRANSACTION_TYPE, new ArrayList<>(), deletions);
    }

    @When("^(\\w+) removes ([^\"]*) from allowed transaction types$")
    public void removesFromTheAllowedTransactionTypes(String userName, String operation) {
        final Account userAccount = getUser(userName);
        List<TransactionType> deletions = new ArrayList<>();
        deletions.add(TransactionType.valueOf(operation));
        accountRestrictionHelper.createAccountTransactionTypeRestrictionTransactionAndWait(userAccount,
                AccountRestrictionType.ALLOW_OUTGOING_TRANSACTION_TYPE, new ArrayList<>(), deletions);
    }
}
