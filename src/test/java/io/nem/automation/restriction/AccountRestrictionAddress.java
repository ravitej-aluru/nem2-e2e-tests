package io.nem.automation.restriction;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import io.nem.automation.common.BaseTest;
import io.nem.automationHelpers.common.TestContext;
import io.nem.automationHelpers.helper.AccountRestrictionHelper;
import io.nem.automationHelpers.helper.MosaicHelper;
import io.nem.sdk.model.account.Account;
import io.nem.sdk.model.account.UnresolvedAddress;
import io.nem.sdk.model.transaction.AccountRestrictionType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.apache.commons.lang3.RandomStringUtils;

public class AccountRestrictionAddress extends BaseTest {

    private final MosaicHelper mosaicHelper;
    private final AccountRestrictionHelper accountRestrictionHelper;
    private final AccountRestrictionMosaic accountRestrictionMosaic;
    private final List<String> pronouns = new ArrayList<>(
        Arrays.asList("herself", "himself", "self"));

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
        usernames.forEach(username -> getTestContext().getLogger()
            .LogInfo(getAccountInfoFromContext(username).toString()));
    }

    @When("^(\\w+) blocks receiving transactions from:$")
    public void blocksReceivingTransactionsFrom(final String blockerAccount,
        final List<String> blockedAccounts) {
        accountRestrictionMosaic.allowsOrBlocksReceivingTransactionsContainingTheFollowingItems(
            blockerAccount, "blocks", "addresses", blockedAccounts);
    }

    @And("^receiving transactions from the stated addresses should be blocked$")
    public void receivingTransactionsFromTheStatedAddressesShouldBeBlocked() {
    }

    @When("^(\\w+) only allowed receiving transactions from:$")
    public void onlyAllowsReceivingTransactionsFrom(final String account,
        final List<String> allowedAccounts) {
        accountRestrictionMosaic.allowsOrBlocksReceivingTransactionsContainingTheFollowingItems(
            account, "allows", "addresses", allowedAccounts);
    }

    @Given("^(\\w+) blocked receiving transactions from:$")
    public void userBlockedReceivingTransactionsFrom(final String blocker,
        final List<String> blocked) {
        accountRestrictionMosaic
            .allowsOrBlocksReceivingTransactionsContainingTheFollowingItems(blocker,
                "blocked", "addresses", blocked);
    }

    @When("^(\\w+) unblocks (\\w+) address$")
    public void userUnblocksAddress(final String blocker, final String unblocked) throws Throwable {
        accountRestrictionMosaic
            .removesAllowedOrBlockedReceivingTransactionsContainingTheFollowingItems(blocker,
                "blocked", "addresses", new ArrayList<>(Arrays.asList(unblocked)));
    }

    @When("^(\\w+) removes (\\w+) from the allowed addresses$")
    public void userRemovesFromTheAllowedAddresses(final String blocker, final String blocked) {
        accountRestrictionMosaic
            .removesAllowedOrBlockedReceivingTransactionsContainingTheFollowingItems(blocker,
                "allowed", "addresses", new ArrayList<>(Arrays.asList(blocked)));
    }

    @When("^(\\w+) tries to unblock (\\w+) address$")
    public void userTriesToUnblockAddress(final String blocker, final String blocked) {
        final Account signerAccount = getUser(blocker);
        accountRestrictionHelper
            .createAccountAddressRestrictionTransactionAndAnnounce(signerAccount,
                AccountRestrictionType.BLOCK_ADDRESS, Collections.emptyList(),
                Collections.singletonList(getUser(blocked).getAddress()));
    }

    @When("^(\\w+) tries to remove (\\w+) from the allowed addresses$")
    public void userTriesToRemoveFromTheAllowedAddresses(final String username,
        final String allowed) {
        final Account signerAccount = getUser(username);
        accountRestrictionHelper
            .createAccountAddressRestrictionTransactionAndAnnounce(signerAccount,
                AccountRestrictionType.ALLOW_INCOMING_ADDRESS, Collections.emptyList(),
                Collections.singletonList(getUser(allowed).getAddress()));
    }

    @When("^(\\w+) tries to only allow receiving transactions from (\\w+)$")
    public void userTriesToOnlyAllowAddress(final String username, String allowed) {
        allowed =
            pronouns.parallelStream().anyMatch(allowed::equalsIgnoreCase) ? username : allowed;
        final Account signerAccount = getUser(username);
        accountRestrictionHelper
            .createAccountAddressRestrictionTransactionAndAnnounce(signerAccount,
                AccountRestrictionType.ALLOW_INCOMING_ADDRESS,
                Collections.singletonList(getUser(allowed).getAddress()), Collections.emptyList());
    }

    @When("^(\\w+) tries to block receiving transactions from (\\w+)$")
    public void userTriesToBlockAddress(final String blocker, String blocked) {
        blocked = pronouns.parallelStream().anyMatch(blocked::equalsIgnoreCase) ? blocker : blocked;
        final Account signerAccount = getUser(blocker);
        accountRestrictionHelper
            .createAccountAddressRestrictionTransactionAndAnnounce(signerAccount,
                AccountRestrictionType.BLOCK_ADDRESS,
                Collections.singletonList(getUser(blocked).getAddress()), Collections.emptyList());
    }

    @Given("^(\\w+) has (allowed|blocked) receiving transactions from (\\d+) different addresses$")
    public void hasAllowedOrBlockedReceivingFromDifferentAddresses(final String username,
        final String restrictionType, final int count) {
        // first register given number of addresses.
        this.thereAreAtLeastDifferentAddressesRegistered(count);
        List<String> addresses = getTestContext().getScenarioContext()
            .getContext("randomAddressesList");
        if (restrictionType.equalsIgnoreCase("allowed")) {
            accountRestrictionMosaic
                .allowsOrBlocksReceivingTransactionsContainingTheFollowingItems(username, "allows",
                    "addresses", addresses);
        } else {
            accountRestrictionMosaic
                .allowsOrBlocksReceivingTransactionsContainingTheFollowingItems(username, "blocks",
                    "addresses", addresses);
        }
    }

    @When("^(\\w+) tries to block receiving transactions from (\\d+) different addresses$")
    public void userTriesToBlockReceivingTransactionsFromDifferentAddresses(final String username,
        final int count) {
        final Account signerAccount = getUser(username);
        List<String> randomAddresses = getTestContext().getScenarioContext()
            .getContext("randomAddressesList");
        //TODO: assuming that at least count + 1 addresses are registered. May be better to check and throw if not.
        List<UnresolvedAddress> modifications = randomAddresses.stream().limit(count + 1)
            .collect(Collectors.toList())
            .parallelStream().map(address -> getUser(address).getAddress())
            .collect(Collectors.toList());
        accountRestrictionHelper
            .createAccountAddressRestrictionTransactionAndAnnounce(signerAccount,
                AccountRestrictionType.BLOCK_ADDRESS, modifications, Collections.emptyList());
    }

    @When("^(\\w+) tries to only allow receiving transactions from (\\d+) different addresses$")
    public void userTriesToAllowReceivingTransactionsFromDifferentAddresses(final String username,
        final int count) {
        final Account signerAccount = getUser(username);
        List<String> randomAddresses = getTestContext().getScenarioContext()
            .getContext("randomAddressesList");
        //TODO: assuming that at least count + 1 addresses are registered. May be better to check and throw if not.
        List<UnresolvedAddress> modifications = randomAddresses.stream().limit(count + 1)
            .collect(Collectors.toList())
            .parallelStream().map(address -> getUser(address).getAddress())
            .collect(Collectors.toList());
        accountRestrictionHelper
            .createAccountAddressRestrictionTransactionAndAnnounce(signerAccount,
                AccountRestrictionType.ALLOW_INCOMING_ADDRESS, modifications,
                Collections.emptyList());
    }

    @Given("^there are at least (\\d+) different addresses registered$")
    public void thereAreAtLeastDifferentAddressesRegistered(int count) {
        List<String> addresses = new ArrayList<>(count);
        for (int i = 0; i < count; i++) {
            addresses.add(RandomStringUtils.randomAlphanumeric(10));
        }
        this.theFollowingAccountsExists(addresses);
        getTestContext().getScenarioContext().setContext("randomAddressesList", addresses);
    }

    @When("^(\\w+) tries to block receiving transactions from \"([^\"]*)\"$")
    public void userTriesToBlockReceivingTransactionsFrom(final String blocker,
        final String blocked) {
        this.userTriesToBlockAddress(blocker, blocked);
    }

    @When("^(\\w+) tries to only allow receiving transactions from \"([^\"]*)\"$")
    public void userTriesToOnlyAllowReceivingTransactionsFrom(final String username,
        final String allowed) {
        this.userTriesToOnlyAllowAddress(username, allowed);
    }
}
