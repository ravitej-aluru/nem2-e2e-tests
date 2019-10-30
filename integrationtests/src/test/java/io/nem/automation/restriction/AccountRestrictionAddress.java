package io.nem.automation.restriction;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import io.nem.automation.common.BaseTest;
import io.nem.automationHelpers.common.TestContext;
import io.nem.automationHelpers.helper.AccountRestrictionHelper;
import io.nem.automationHelpers.helper.MosaicHelper;

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

    @When("^(\\w+) only allows receiving transactions from:$")
    public void onlyAllowsReceivingTransactionsFrom(final String account, final List<String> allowedAccounts) {
        accountRestrictionMosaic.allowsOrBlocksReceivingTransactionsContainingTheFollowingItems(
                account, "allows", "addresses", allowedAccounts);
    }

}
