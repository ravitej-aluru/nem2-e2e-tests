package io.nem.automation.restriction;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import io.nem.automation.common.BaseTest;
import io.nem.automationHelpers.common.TestContext;
import io.nem.automationHelpers.helper.AccountRestrictionHelper;
import io.nem.automationHelpers.helper.MosaicHelper;
import io.nem.sdk.model.account.Account;
import io.nem.sdk.model.account.Address;
import io.nem.sdk.model.transaction.AccountRestrictionModification;
import io.nem.sdk.model.transaction.AccountRestrictionModificationAction;
import io.nem.sdk.model.transaction.AccountRestrictionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AccountRestrictionAddress extends BaseTest {
    private final MosaicHelper mosaicHelper;
    private final AccountRestrictionHelper accountRestrictionHelper;
    private final AccountRestrictionMosaic accountRestrictionMosaic;

    /**
     * Constructor.
     *
     * @param testContext Test context.
     */
    public AccountRestrictionAddress(TestContext testContext) {
        super(testContext);
        mosaicHelper = new MosaicHelper(testContext);
        accountRestrictionHelper = new AccountRestrictionHelper(testContext);
        accountRestrictionMosaic = new AccountRestrictionMosaic(testContext);
    }

    @Given("^the following accounts exist:$")
    public void theFollowingAccountsExists(final List<String> usernames) {
        usernames.parallelStream().forEach(username -> getUserWithCurrency(username, 1000000));
    }

    @When("^(\\w+) blocks receiving transactions from:$")
    public void blocksReceivingTransactionsFrom(final String blockerAccount, final List<String> blockedAccounts) {
        accountRestrictionMosaic.allowsOrBlocksReceivingTransactionsContainingTheFollowingItems(
                blockerAccount, "blocks", "addresses", blockedAccounts);
    }

    @And("^receiving transactions from the stated addresses should be blocked$")
    public void receivingTransactionsFromTheStatedAddressesShouldBeBlocked() {
    }

    @When("^(\\w+) only allowed receiving transactions from:$")
    public void onlyAllowsReceivingTransactionsFrom(final String account, final List<String> allowedAccounts) {
        accountRestrictionMosaic.allowsOrBlocksReceivingTransactionsContainingTheFollowingItems(
                account, "allows", "addresses", allowedAccounts);
    }

    @Given("^(\\w+) blocked receiving transactions from:$")
    public void userBlockedReceivingTransactionsFrom(final String blocker, final List<String> blocked) {
        accountRestrictionMosaic.allowsOrBlocksReceivingTransactionsContainingTheFollowingItems(blocker,
                "blocked", "addresses", blocked);
    }

    @When("^(\\w+) unblocks (\\w+) address$")
    public void userUnblocksAddress(final String blocker, final String unblocked) throws Throwable {
        accountRestrictionMosaic.removesAllowedOrBlockedReceivingTransactionsContainingTheFollowingItems(blocker,
                "blocked", "addresses", new ArrayList<>(Arrays.asList(unblocked)));
    }

    @When("^(\\w+) removes (\\w+) from the allowed addresses$")
    public void userRemovesFromTheAllowedAddresses(final String blocker, final String blocked) {
        accountRestrictionMosaic.removesAllowedOrBlockedReceivingTransactionsContainingTheFollowingItems(blocker,
                "allowed", "addresses", new ArrayList<>(Arrays.asList(blocked)));
    }

    @When("^(\\w+) tries to unblock (\\w+) address$")
    public void userTriesToUnblockAddress(final String blocker, final String blocked) {
        final Account signerAccount = getUser(blocker);
        List<AccountRestrictionModification<Address>> modifications = new ArrayList<>();
        modifications.add(accountRestrictionHelper.createAddressRestriction(
                AccountRestrictionModificationAction.REMOVE, getUser(blocked).getAddress()));
        accountRestrictionHelper.createAccountAddressRestrictionTransactionAndAnnounce(signerAccount,
                AccountRestrictionType.BLOCK_ADDRESS, modifications);
    }

    @When("^(\\w+) tries to remove (\\w+) from the allowed addresses$")
    public void userTriesToRemoveFromTheAllowedAddresses(final String username, final String allowed) {
        final Account signerAccount = getUser(username);
        List<AccountRestrictionModification<Address>> modifications = new ArrayList<>();
        modifications.add(accountRestrictionHelper.createAddressRestriction(
                AccountRestrictionModificationAction.REMOVE, getUser(allowed).getAddress()));
        accountRestrictionHelper.createAccountAddressRestrictionTransactionAndAnnounce(signerAccount,
                AccountRestrictionType.ALLOW_INCOMING_ADDRESS, modifications);
    }
}
