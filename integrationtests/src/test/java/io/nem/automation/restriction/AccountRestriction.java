package io.nem.automation.restriction;

import cucumber.api.PendingException;
import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import io.nem.automation.asset.AssetRegistration;
import io.nem.automation.common.BaseTest;
import io.nem.automationHelpers.common.TestContext;
import io.nem.automationHelpers.helper.AccountRestrictionHelper;
import io.nem.automationHelpers.helper.MosaicHelper;
import io.nem.sdk.model.account.Account;
import io.nem.sdk.model.mosaic.MosaicId;
import io.nem.sdk.model.mosaic.MosaicInfo;
import io.nem.sdk.model.transaction.AccountRestrictionModification;
import io.nem.sdk.model.transaction.AccountRestrictionModificationType;
import io.nem.sdk.model.transaction.AccountRestrictionType;

import java.util.ArrayList;
import java.util.List;

public class AccountRestriction extends BaseTest {
    private final MosaicHelper mosaicHelper;
    private final AccountRestrictionHelper accountRestrictionHelper;

    /**
     * Constructor.
     *
     * @param testContext Test context.
     */
    public AccountRestriction(final TestContext testContext) {
        super(testContext);
        mosaicHelper = new MosaicHelper(testContext);
        accountRestrictionHelper = new AccountRestrictionHelper(testContext);
    }

    @Given("^(\\w+) has the following assets registered and active:$")
    public void theFollowingAssetsAreRegisteredAndActive(final String userName, final List<String> assets) {
        final AssetRegistration assetRegistration = new AssetRegistration(getTestContext());
        assets.forEach(asset -> {
                assetRegistration.registerAsset(userName, asset);
        });
    }

    @And("^an account can only define up to (\\d+) mosaic filters$")
    public void anAccountCanOnlyDefineUpToMosaicFilters(final Integer noOfMosaicFilters) {
    }

    @When("^(\\w+) blocks receiving transactions containing the following assets:$")
    public void blocksReceivingTransactionsContainingTheFollowingAssets(final String userName, final List<String> assets) {
        final Account signerAccount = getUser(userName);
        final AssetRegistration assetRegistration = new AssetRegistration(getTestContext());
        List<AccountRestrictionModification<MosaicId>> modifications = new ArrayList<>();
        assets.forEach(asset -> {
            {
                assetRegistration.registerAsset(userName, asset);
                MosaicInfo mosaicInfo = getTestContext().getScenarioContext().getContext(asset);
                modifications.add(accountRestrictionHelper.createMosaicRestriction(AccountRestrictionModificationType.ADD, mosaicInfo.getMosaicId()));
            }
        });
        accountRestrictionHelper.createAccountMosaicRestrictionTransactionAndWait(signerAccount, AccountRestrictionType.BLOCK_MOSAIC_ID, modifications);
    }

    @And("^receiving the stated assets should be blocked$")
    public void receivingTheStatedAssetsShouldBeBlocked() {
        //validate Alice's account hasn't changed. there is
    }

    @When("^(\\w+) only allows receiving transactions containing type:$")
    public void onlyAllowsReceivingTransactionsContainingType(final String userName) {
    }

    @And("^receiving the stated assets should be allowed$")
    public void receivingTheStatedAssetsShouldBeAllowed() {
    }

    @Given("^(\\w+) blocked receiving transactions containing the following assets:$")
    public void blockedReceivingTransactionsContainingTheFollowingAssets(final String userName) {
    }

    @When("^(\\w+) unblocks \"([^\"]*)\"$")
    public void unblocksGivenAsset(final String userName, final String assetType) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @And("^receiving \"([^\"]*)\" assets should remain blocked$")
    public void receivingAssetsShouldRemainBlocked(final String assetType) throws Throwable {
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