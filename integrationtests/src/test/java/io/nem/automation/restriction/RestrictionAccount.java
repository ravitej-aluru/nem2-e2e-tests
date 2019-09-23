package io.nem.automation.restriction;

import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import io.nem.automation.common.BaseTest;
import io.nem.automationHelpers.common.TestContext;

public class RestrictionAccount extends BaseTest {

    /**
     * Constructor.
     *
     * @param testContext Test context.
     */
    public RestrictionAccount(final TestContext testContext) {
        super(testContext);
    }

    @Given("^the following assets are registered and active:$")
    public void theFollowingAssetsAreRegisteredAndActive() {
    }

    @And("^an account can only define up to (\\d+) mosaic filters$")
    public void anAccountCanOnlyDefineUpToMosaicFilters(int arg0) {
    }

    @When("^(\\w+) blocks receiving transactions containing the following assets:$")
    public void aliceBlocksReceivingTransactionsContainingTheFollowingAssets() {
    }

    @And("^receiving the stated assets should be blocked$")
    public void receivingTheStatedAssetsShouldBeBlocked() {
    }

    @When("^(\\w+) only allows receiving transactions containing type:$")
    public void aliceOnlyAllowsReceivingTransactionsContainingType() {
    }

    @And("^receiving the stated assets should be allowed$")
    public void receivingTheStatedAssetsShouldBeAllowed() {
    }

    @Given("^(\\w+) blocked receiving transactions containing the following assets:$")
    public void aliceBlockedReceivingTransactionsContainingTheFollowingAssets() {
    }

    @When("^(\\w+) unblocks \"([^\"]*)\"$")
    public void aliceUnblocks(String arg0) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("^receiving \"([^\"]*)\" assets should remain blocked$")
    public void receivingAssetsShouldRemainBlocked(String arg0) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Given("^(\\w+) only allowed receiving \"([^\"]*)\" assets$")
    public void aliceOnlyAllowedReceivingAssets(String arg0) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("^(\\w+) removes \"([^\"]*)\" from the allowed assets$")
    public void aliceRemovesFromTheAllowedAssets(String arg0) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("^only receiving \"([^\"]*)\" assets should remain allowed$")
    public void onlyReceivingAssetsShouldRemainAllowed(String arg0) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Given("^(\\w+) blocked receiving \"([^\"]*)\" assets$")
    public void aliceBlockedReceivingAssets(String arg0) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("^(\\w+) only allows receiving \"([^\"]*)\" assets$")
    public void aliceOnlyAllowsReceivingAssets(String arg0) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("^(\\w+) blocks receiving \"([^\"]*)\" assets$")
    public void aliceBlocksReceivingAssets(String arg0) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Given("^(\\w+) blocked receiving (\\d+) different assets$")
    public void aliceBlockedReceivingDifferentAssets(int arg0) {
    }

    @Given("^(\\w+) only allowed receiving (\\d+) different assets$")
    public void aliceOnlyAllowedReceivingDifferentAssets(int arg0) {
    }

    @When("^(\\w+) blocks receiving (\\d+) different assets$")
    public void aliceBlocksReceivingDifferentAssets(int arg0) {
    }

    @When("^(\\w+) only allows receiving (\\d+) different assets$")
    public void aliceOnlyAllowsReceivingDifferentAssets(int arg0) {
    }
}