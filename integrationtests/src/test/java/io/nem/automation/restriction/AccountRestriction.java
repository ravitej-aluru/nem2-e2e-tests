package io.nem.automation.restriction;

import cucumber.api.java.en.And;
import cucumber.api.java.en.Given;
import cucumber.api.java.en.When;
import io.nem.automation.asset.AssetRegistration;
import io.nem.automation.common.BaseTest;
import io.nem.automation.transaction.SendTransaction;
import io.nem.automation.transfer.SendAsset;
import io.nem.automationHelpers.common.TestContext;
import io.nem.automationHelpers.helper.AccountRestrictionHelper;
import io.nem.automationHelpers.helper.MosaicHelper;
import io.nem.sdk.model.account.Account;
import io.nem.sdk.model.mosaic.MosaicId;
import io.nem.sdk.model.mosaic.MosaicInfo;
import io.nem.sdk.model.transaction.AccountRestrictionModification;
import io.nem.sdk.model.transaction.AccountRestrictionModificationType;
import io.nem.sdk.model.transaction.AccountRestrictionType;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
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

//    @And("^an account can only define up to (\\d+) mosaic filters$")
//    public void anAccountCanOnlyDefineUpToMosaicFilters(final Integer noOfMosaicFilters) {
//    }

    /**
     * Gets the Account from userName and adds the specified restriction to the account
     *
     * @param username
     * @param restrictionOperation
     * @param restrictedItemType
     * @param restrictedItems
     */
    @When("^(\\w+) (allows|blocks) receiving transactions containing the following " +
            "(assets?|addresses|transaction types?):$")
    public void allowsOrBlocksReceivingTransactionsContainingTheFollowingItems(
            final String username, final String restrictionOperation, final String restrictedItemType,
            final List<String> restrictedItems) {
        final Account signerAccount = getUser(username);
        final AccountRestrictionType accountRestrictionType = accountRestrictionHelper.getAccountRestrictionType(
                restrictionOperation, restrictedItemType);
        accountRestrictionHelper.addAppropriateModificationTransactionAndWait(restrictedItemType, restrictedItems, signerAccount, accountRestrictionType);
        // setting recipient since one who blocks/allows will most probably be the recipient when testing
        getTestContext().getScenarioContext().setContext("recipient", username);
    }

    @When("^(\\w+) removes (allowed|blocked) receiving the following " +
            "(assets?|addresses|transaction types?):$")
    public void removesAllowedOrBlockedReceivingTransactionsContainingTheFollowingItems(
            final String username, final String restrictionOperation, final String restrictedItemType,
            final List<String> restrictedItems) {
        final Account signerAccount = getUser(username);
        final AccountRestrictionType accountRestrictionType = accountRestrictionHelper.getAccountRestrictionType(
                restrictionOperation, restrictedItemType);
        accountRestrictionHelper.removeAppropriateModificationTransactionAndWait(restrictedItemType, restrictedItems, signerAccount, accountRestrictionType);
        // setting recipient since one who blocks/allows will most probably be the recipient when testing
        getTestContext().getScenarioContext().setContext("recipient", username);
    }

//    @And("^receiving the stated assets should be blocked$")
//    public void receivingTheStatedAssetsShouldBeBlocked() {
//        // Validate that the appropriate error code is returned.
//        // And potentially validate Alice's account hasn't changed for completeness?
//    }

    //    @When("^(\\w+) only allows receiving transactions containing type:$")
//    public void onlyAllowsReceivingTransactionsContainingType(final String userName, final List<String> assets) {
//        final Account signerAccount = getUser(userName);
//        // get a list of all account restrictions for the given account and confirm that
//        // passed in assets do not exist in that list. That means passed in assets are allowed
//        // How do we check those are the only assets allowed?
//    }
//
    @And("^receiving the stated assets should be allowed$")
    public void receivingTheStatedAssetsShouldBeAllowed() {
    }
//
//    @Given("^(\\w+) blocked receiving transactions containing the following assets:$")
//    public void blockedReceivingTransactionsContainingTheFollowingAssets(final String userName) {
//    }

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
        // This step is basically calling the two below steps
        //    When Alice tries to send 1 asset "ticket" to Bob
        //    And Alice should receive the error "Failure_RestrictionAccount_Mosaic_Transfer_Prohibited"
        final SendAsset sendAsset = new SendAsset(getTestContext());
        final String recipient = getTestContext().getScenarioContext().getContext("recipient");
        sendAsset.triesToTransferAsset(BaseTest.AUTOMATION_USER_ALICE, BigInteger.ONE, assetType, recipient);
        final SendTransaction sendTransaction = new SendTransaction(getTestContext());
        sendTransaction.verifyTransactionError(BaseTest.AUTOMATION_USER_ALICE,
                "Failure_RestrictionAccount_Mosaic_Transfer_Prohibited");
    }

    @Given("^(\\w+) has only allowed receiving the following assets$")
    public void onlyAllowedReceivingAssets(final String username, final List<String> assets) {
        this.allowsOrBlocksReceivingTransactionsContainingTheFollowingItems(username, "allows", "assets", assets);
    }

    @When("^(\\w+) removes \"([^\"]*)\" from the allowed assets$")
    public void removesFromTheAllowedAssets(final String username, final String asset) {
        this.removesAllowedOrBlockedReceivingTransactionsContainingTheFollowingItems(username,
                "allowed", "assets", new ArrayList<>(Arrays.asList(asset)));
    }

//    @And("^only receiving \"([^\"]*)\" assets should remain allowed$")
//    public void onlyReceivingAssetsShouldRemainAllowed(final String arg0) throws Throwable {
//        // Write code here that turns the phrase above into concrete actions
//        throw new PendingException();
//    }

    @Given("^(\\w+) has blocked receiving \"([^\"]*)\" assets$")
    public void hasBlockedReceivingAssets(final String username, final String asset) throws Throwable {
        // calling another step in this class to allow multiple step grammar possibilities with minimum code duplication
        this.allowsOrBlocksReceivingTransactionsContainingTheFollowingItems(username, "blocks",
                "assets", new ArrayList<String>(Arrays.asList(asset)));
    }

    @When("^(\\w+) tries to unblock receiving \"([^\"]*)\" assets$")
    public void triesToUnblockReceivingAssets(final String username, final String asset) throws Throwable {
        final Account signerAccount = getUser(username);
        List<AccountRestrictionModification<MosaicId>> modifications = new ArrayList<>();
        MosaicInfo mosaicInfo = getTestContext().getScenarioContext().getContext(asset);
        modifications.add(accountRestrictionHelper.createMosaicRestriction(AccountRestrictionModificationType.REMOVE, mosaicInfo.getMosaicId()));
        accountRestrictionHelper.createAccountMosaicRestrictionTransactionAndAnnounce(signerAccount, AccountRestrictionType.BLOCK_MOSAIC_ID, modifications);
    }

    @When("^(\\w+) tries to block receiving \"([^\"]*)\" assets$")
    public void triesToBlockReceivingAssets(final String username, final String asset) throws Throwable {
        // calling another step in this class to allow multiple step grammar possibilities with minimum code duplication
        this.allowsOrBlocksReceivingTransactionsContainingTheFollowingItems(username, "blocks",
                "assets", new ArrayList<String>(Arrays.asList(asset)));
    }

    @When("^(\\w+) tries to only allow receiving \"([^\"]*)\" assets$")
    public void triesToOnlyAllowReceivingAssets(final String username, final String asset) throws Throwable {
        final Account signerAccount = getUser(username);
        List<AccountRestrictionModification<MosaicId>> modifications = new ArrayList<>();
        MosaicInfo mosaicInfo = getTestContext().getScenarioContext().getContext(asset);
        modifications.add(accountRestrictionHelper.createMosaicRestriction(AccountRestrictionModificationType.ADD, mosaicInfo.getMosaicId()));
        accountRestrictionHelper.createAccountMosaicRestrictionTransactionAndAnnounce(signerAccount, AccountRestrictionType.MOSAIC_ID, modifications);
    }

    @When("^(\\w+) tries to remove \"([^\"]*)\" from allowed assets$")
    public void triesToRemoveFromAllowedAssets(final String username, final String asset) throws Throwable {
        final Account signerAccount = getUser(username);
        List<AccountRestrictionModification<MosaicId>> modifications = new ArrayList<>();
        MosaicInfo mosaicInfo = getTestContext().getScenarioContext().getContext(asset);
        modifications.add(accountRestrictionHelper.createMosaicRestriction(AccountRestrictionModificationType.REMOVE, mosaicInfo.getMosaicId()));
        accountRestrictionHelper.createAccountMosaicRestrictionTransactionAndAnnounce(signerAccount, AccountRestrictionType.MOSAIC_ID, modifications);
    }
}