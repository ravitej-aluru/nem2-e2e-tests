package io.nem.automation.restriction;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import io.nem.automation.common.BaseTest;
import io.nem.automationHelpers.common.TestContext;
import io.nem.automationHelpers.helper.AccountRestrictionHelper;
import io.nem.sdk.model.account.Account;
import io.nem.sdk.model.account.UnresolvedAddress;
import io.nem.sdk.model.transaction.AccountRestrictionType;
import org.apache.commons.lang3.RandomStringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AccountRestrictionAddress extends BaseTest {
    private final AccountRestrictionHelper accountRestrictionHelper;
    private final List<String> selfPronouns = new ArrayList<>(Arrays.asList("herself", "himself", "self"));

    /**
     * Constructor.
     *
     * @param testContext Test context.
     */
    public AccountRestrictionAddress(TestContext testContext) {
        super(testContext);
        accountRestrictionHelper = new AccountRestrictionHelper(testContext);
    }

    @Given("^the following accounts exist:$")
    public void theFollowingAccountsExists(final List<String> usernames) {
        usernames.parallelStream().forEach(username -> getUserWithCurrency(username, 1000000));
        usernames.forEach(username -> getTestContext().getLogger().LogInfo(getAccountInfoFromContext(username).toString()));
    }

    @When("^(\\w+) blocked receiving transactions from:$")
    public void blocksReceivingTransactionsFrom(final String blockerAccount, final List<String> blockedAccounts) {
        final Account signerAccount = getUser(blockerAccount);
        final List<UnresolvedAddress> additions = new ArrayList<>();
        blockedAccounts.forEach(blockedAccount -> additions.add(getAccountInfoFromContext(blockedAccount).getAddress()));
        accountRestrictionHelper.createAccountAddressRestrictionTransactionAndWait(
                signerAccount, AccountRestrictionType.BLOCK_ADDRESS, additions, new ArrayList<>());
    }

    @And("^receiving transactions from the stated addresses should be blocked$")
    public void receivingTransactionsFromTheStatedAddressesShouldBeBlocked() {
    }

    @When("^(\\w+) only allowed receiving transactions from:$")
    public void onlyAllowsReceivingTransactionsFrom(final String account, final List<String> allowedAccounts) {
        final Account signerAccount = getUser(account);
        final List<UnresolvedAddress> additions = new ArrayList<>();
        allowedAccounts.forEach(allowedAccount -> additions.add(getAccountInfoFromContext(allowedAccount).getAddress()));
        accountRestrictionHelper.createAccountAddressRestrictionTransactionAndWait(
                signerAccount, AccountRestrictionType.ALLOW_INCOMING_ADDRESS, additions, new ArrayList<>());
    }

    @When("^(\\w+) unblocks (\\w+) address$")
    public void userUnblocksAddress(final String blocker, final String unblocked) {
        final Account signerAccount = getUser(blocker);
        final List<UnresolvedAddress> deletions = new ArrayList<>();
        deletions.add(getAccountInfoFromContext(unblocked).getAddress());
        accountRestrictionHelper.createAccountAddressRestrictionTransactionAndWait(
                signerAccount, AccountRestrictionType.BLOCK_ADDRESS, new ArrayList<>(), deletions);
    }

    @When("^(\\w+) removes (\\w+) from allowed addresses$")
    public void userRemovesFromTheAllowedAddresses(final String blocker, final String blocked) {
        final Account signerAccount = getUser(blocker);
        final List<UnresolvedAddress> deletions = new ArrayList<>();
        deletions.add(getAccountInfoFromContext(blocked).getAddress());
        accountRestrictionHelper.createAccountAddressRestrictionTransactionAndWait(
                signerAccount, AccountRestrictionType.ALLOW_INCOMING_ADDRESS, new ArrayList<>(), deletions);
    }

    @When("^(\\w+) tries to unblock (\\w+) address$")
    public void userTriesToUnblockAddress(final String blocker, final String blocked) {
        final Account signerAccount = getUser(blocker);
        final List<UnresolvedAddress> deletions = new ArrayList<>();
        deletions.add(getAccountInfoFromContext(blocked).getAddress());
        accountRestrictionHelper.createAccountAddressRestrictionTransactionAndAnnounce(
                signerAccount, AccountRestrictionType.BLOCK_ADDRESS, new ArrayList<>(), deletions);
    }

    @When("^(\\w+) tries to remove (\\w+) from allowed addresses$")
    public void userTriesToRemoveFromTheAllowedAddresses(final String username, final String allowed) {
        final Account signerAccount = getUser(username);
        final List<UnresolvedAddress> deletions = new ArrayList<>();
        deletions.add(getAccountInfoFromContext(allowed).getAddress());
        accountRestrictionHelper.createAccountAddressRestrictionTransactionAndAnnounce(
                signerAccount, AccountRestrictionType.ALLOW_INCOMING_ADDRESS, new ArrayList<>(), deletions);
    }

    @When("^(\\w+) tries to only allow receiving transactions from (\\w+)$")
    public void userTriesToOnlyAllowAddress(final String username, String allowed) {
        allowed = selfPronouns.parallelStream().anyMatch(allowed::equalsIgnoreCase) ? username : allowed;
        final Account signerAccount = getUser(allowed);
        final List<UnresolvedAddress> additions = new ArrayList<>();
        additions.add(getAccountInfoFromContext(allowed).getAddress());
        accountRestrictionHelper.createAccountAddressRestrictionTransactionAndAnnounce(
                signerAccount, AccountRestrictionType.ALLOW_INCOMING_ADDRESS, additions, new ArrayList<>());
    }

    @When("^(\\w+) tries to block receiving transactions from (\\w+)$")
    public void userTriesToBlockAddress(final String blocker, String blocked) {
        blocked = selfPronouns.parallelStream().anyMatch(blocked::equalsIgnoreCase) ? blocker : blocked;
        final Account signerAccount = getUser(blocker);
        final List<UnresolvedAddress> additions = new ArrayList<>();
        additions.add(getAccountInfoFromContext(blocked).getAddress());
        accountRestrictionHelper.createAccountAddressRestrictionTransactionAndAnnounce(
                signerAccount, AccountRestrictionType.BLOCK_ADDRESS, additions, new ArrayList<>());
    }

    @Given("^(\\w+) has (allowed|blocked) receiving transactions from (\\d+) different addresses$")
    public void hasAllowedOrBlockedReceivingFromDifferentAddresses(final String username, final String restrictionType, final int count) {
        // first register given number of addresses.
        this.thereAreAtLeastDifferentAddressesRegistered(count);
        List<String> addresses = getTestContext().getScenarioContext().getContext("randomAddressesList");
        if (restrictionType.equalsIgnoreCase("allowed")) {
            this.onlyAllowsReceivingTransactionsFrom(username, addresses);
        } else {
            this.blocksReceivingTransactionsFrom(username, addresses);
        }
    }

    @When("^(\\w+) tries to block receiving transactions from (\\d+) different addresses$")
    public void userTriesToBlockReceivingTransactionsFromDifferentAddresses(final String username, final int count) {
        final Account signerAccount = getUser(username);
        List<UnresolvedAddress> modifications = new ArrayList<>();
        //The tester will have run the step to create loads of addresses
        List<String> randomAddresses = getTestContext().getScenarioContext().getContext("randomAddressesList");
        //TODO: assuming that at least count + 1 addresses are registered. May be better to check and throw if not.
        randomAddresses.stream().limit(count + 1).collect(Collectors.toList()).parallelStream().forEach(address -> {
            modifications.add(getUser(address).getAddress());
        });
        accountRestrictionHelper.createAccountAddressRestrictionTransactionAndAnnounce(signerAccount,
                AccountRestrictionType.BLOCK_ADDRESS, modifications, new ArrayList<>());
    }

    @When("^(\\w+) tries to only allow receiving transactions from (\\d+) different addresses$")
    public void userTriesToAllowReceivingTransactionsFromDifferentAddresses(final String username, final int count) {
        final Account signerAccount = getUser(username);
        List<UnresolvedAddress> modifications = new ArrayList<>();
        List<String> randomAddresses = getTestContext().getScenarioContext().getContext("randomAddressesList");
        //TODO: assuming that at least count + 1 addresses are registered. May be better to check and throw if not.
        randomAddresses.stream().limit(count + 1).collect(Collectors.toList()).parallelStream().forEach(address -> {
            modifications.add(getUser(address).getAddress());
        });
        accountRestrictionHelper.createAccountAddressRestrictionTransactionAndAnnounce(signerAccount,
                AccountRestrictionType.ALLOW_INCOMING_ADDRESS, modifications, new ArrayList<>());
    }

    @Given("^there are at least (\\d+) different addresses registered$")
    public void thereAreAtLeastDifferentAddressesRegistered(int count) {
        List<String> addresses = new ArrayList<>(count);
        for (int i = 0; i < count; i++) addresses.add(RandomStringUtils.randomAlphanumeric(10));
        this.theFollowingAccountsExists(addresses);
        getTestContext().getScenarioContext().setContext("randomAddressesList", addresses);
    }

    @When("^(\\w+) tries to block receiving transactions from \"([^\"]*)\"$")
    public void userTriesToBlockReceivingTransactionsFrom(final String blocker, final String blocked) {
        this.userTriesToBlockAddress(blocker, blocked);
    }

    @When("^(\\w+) tries to only allow receiving transactions from \"([^\"]*)\"$")
    public void userTriesToOnlyAllowReceivingTransactionsFrom(final String username, final String allowed) {
        this.userTriesToOnlyAllowAddress(username, allowed);
    }
}
