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
        // Alice already has cat.currency registered to her. What happens if we try to register again?
        assets.forEach(asset -> {
                assetRegistration.registerAsset(userName, asset);
        });
    }

    @And("^an account can only define up to (\\d+) mosaic filters$")
    public void anAccountCanOnlyDefineUpToMosaicFilters(final Integer noOfMosaicFilters) {
    }

    /**
     * Gets the Account from userName and adds the specified restriction to the account
     *
     * @param userName
     * @param restrictionOperation
     * @param restrictedItem
     * @param restrictedItems
     */
    @When("^(\\w+) (allows|blocks) receiving transactions containing the following " +
            "(assets?|addresses|transaction types?):$")
    public void allowsOrBlocksReceivingTransactionsContainingTheFollowingItems(
            final String userName, final String restrictionOperation, final String restrictedItem,
            final List<String> restrictedItems) {
        final Account signerAccount = getUser(userName);
        final AccountRestrictionType accountRestrictionType = accountRestrictionHelper.getAccountRestrictionType(
                restrictionOperation, restrictedItem);
        accountRestrictionHelper.createAppropriateModificationTransactionAndWait(restrictedItem, restrictedItems, signerAccount, accountRestrictionType);
    }

    @And("^receiving the stated assets should be blocked$")
    public void receivingTheStatedAssetsShouldBeBlocked() {
        //Validate that the appropriate error code is returned.
        // And potentially validate Alice's account hasn't changed for completeness?
    }

    @When("^(\\w+) only allows receiving transactions containing type:$")
    public void onlyAllowsReceivingTransactionsContainingType(final String userName, final List<String> assets) {
        final Account signerAccount = getUser(userName);
        // get a list of all account restrictions for the given account and confirm that
        // passed in assets do not exist in that list. That means passed in assets are allowed
        // How do we check those are the only assets allowed?
    }

    @And("^receiving the stated assets should be allowed$")
    public void receivingTheStatedAssetsShouldBeAllowed() {
    }

    @Given("^(\\w+) blocked receiving transactions containing the following assets:$")
    public void blockedReceivingTransactionsContainingTheFollowingAssets(final String userName) {
    }

    @When("^(\\w+) unblocks \"([^\"]*)\"$")
    public void unblocksGivenAsset(final String userName, final String assetType) throws Throwable {
        final Account signerAccount = getUser(userName);
        List<AccountRestrictionModification<MosaicId>> modifications = new ArrayList<>();
        MosaicInfo mosaicInfo = getTestContext().getScenarioContext().getContext(assetType);
        modifications.add(accountRestrictionHelper.createMosaicRestriction(AccountRestrictionModificationType.REMOVE, mosaicInfo.getMosaicId()));
        accountRestrictionHelper.createAccountMosaicRestrictionTransactionAndWait(signerAccount, AccountRestrictionType.BLOCK_MOSAIC_ID, modifications);
    }

    @And("^receiving \"([^\"]*)\" assets should remain blocked$")
    public void receivingAssetsShouldRemainBlocked(final String assetType) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @Given("^(\\w+) only allowed receiving \"([^\"]*)\" assets$")
    public void onlyAllowedReceivingAssets(String userName) throws Throwable {
        // Write code here that turns the phrase above into concrete actions
        throw new PendingException();
    }

    @When("^(\\w+) removes \"([^\"]*)\" from the allowed assets$")
    public void removesFromTheAllowedAssets(String userName) throws Throwable {
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
    public void aliceBlockedReceivingDifferentAssets(String userName, int amount) {
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