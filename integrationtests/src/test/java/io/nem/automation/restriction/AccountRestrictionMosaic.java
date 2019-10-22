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
import io.nem.sdk.model.transaction.AccountRestrictionModificationAction;
import io.nem.sdk.model.transaction.AccountRestrictionType;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AccountRestrictionMosaic extends BaseTest {
    private final MosaicHelper mosaicHelper;
    private final AccountRestrictionHelper accountRestrictionHelper;

    /**
     * Constructor.
     *
     * @param testContext Test context.
     */
    public AccountRestrictionMosaic(final TestContext testContext) {
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
        final List<Object> restrictedItemsList = new ArrayList<>();
        final AccountRestrictionType accountRestrictionType = accountRestrictionHelper.getAccountRestrictionType(
                restrictionOperation, restrictedItemType);
        if (restrictedItemType == "addresses") {
            restrictedItems.forEach(user -> restrictedItemsList.add(getUser(user).getAddress()));
        }
        accountRestrictionHelper.addAppropriateModificationTransactionAndWait(restrictedItemType,
                restrictedItemsList, signerAccount, accountRestrictionType);
        // setting recipient since one who blocks/allows will most probably be the recipient when testing
        getTestContext().getScenarioContext().setContext("recipient", username);
    }

    @When("^(\\w+) removes (allowed|blocked) receiving the following " +
            "(assets?|addresses|transaction types?):$")
    public void removesAllowedOrBlockedReceivingTransactionsContainingTheFollowingItems(
            final String username, final String restrictionOperation, final String restrictedItemType,
            final List<String> restrictedItems) {
        final Account signerAccount = getUser(username);
        final List<Object> restrictedItemsList = new ArrayList<>();
        final AccountRestrictionType accountRestrictionType = accountRestrictionHelper.getAccountRestrictionType(
                restrictionOperation == "allowed" ? "allows" : "blocks", restrictedItemType);
        if (restrictedItemType == "addresses") {
            restrictedItems.parallelStream().forEach(user -> restrictedItemsList.add(getUser(user).getAddress()));
        }
        accountRestrictionHelper.removeAppropriateModificationTransactionAndWait(restrictedItemType,
                restrictedItemsList, signerAccount, accountRestrictionType);
        // setting recipient since one who blocks/allows will most probably be the recipient when testing
        getTestContext().getScenarioContext().setContext("recipient", username);
    }

    @And("^receiving the stated assets should be allowed$")
    public void receivingTheStatedAssetsShouldBeAllowed() {
    }

    @When("^(\\w+) unblocks \"([^\"]*)\"$")
    public void unblocksGivenAsset(final String userName, final String assetType) throws Throwable {
        final Account signerAccount = getUser(userName);
        List<AccountRestrictionModification<MosaicId>> modifications = new ArrayList<>();
        MosaicInfo mosaicInfo = getTestContext().getScenarioContext().getContext(assetType);
        modifications.add(accountRestrictionHelper.createMosaicRestriction(AccountRestrictionModificationAction.REMOVE,
                mosaicInfo.getMosaicId()));
        accountRestrictionHelper.createAccountMosaicRestrictionTransactionAndWait(signerAccount,
                AccountRestrictionType.BLOCK_MOSAIC, modifications);
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
        this.allowsOrBlocksReceivingTransactionsContainingTheFollowingItems(username, "allows",
                "assets", assets);
    }

    @When("^(\\w+) removes \"([^\"]*)\" from the allowed assets$")
    public void removesFromTheAllowedAssets(final String username, final String asset) {
        this.removesAllowedOrBlockedReceivingTransactionsContainingTheFollowingItems(username,
                "allowed", "assets", new ArrayList<>(Arrays.asList(asset)));
    }

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
        modifications.add(accountRestrictionHelper.createMosaicRestriction(AccountRestrictionModificationAction.REMOVE,
                mosaicInfo.getMosaicId()));
        accountRestrictionHelper.createAccountMosaicRestrictionTransactionAndAnnounce(signerAccount,
                AccountRestrictionType.BLOCK_MOSAIC, modifications);
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
        modifications.add(accountRestrictionHelper.createMosaicRestriction(AccountRestrictionModificationAction.ADD,
                mosaicInfo.getMosaicId()));
        accountRestrictionHelper.createAccountMosaicRestrictionTransactionAndAnnounce(signerAccount,
                AccountRestrictionType.ALLOW_INCOMING_MOSAIC, modifications);
    }

    @When("^(\\w+) tries to remove \"([^\"]*)\" from allowed assets$")
    public void triesToRemoveFromAllowedAssets(final String username, final String asset) throws Throwable {
        final Account signerAccount = getUser(username);
        List<AccountRestrictionModification<MosaicId>> modifications = new ArrayList<>();
        MosaicInfo mosaicInfo = getTestContext().getScenarioContext().getContext(asset);
        modifications.add(accountRestrictionHelper.createMosaicRestriction(AccountRestrictionModificationAction.REMOVE,
                mosaicInfo.getMosaicId()));
        accountRestrictionHelper.createAccountMosaicRestrictionTransactionAndAnnounce(signerAccount,
                AccountRestrictionType.ALLOW_INCOMING_MOSAIC, modifications);
    }

    @Given("^(\\w+) has blocked receiving (\\d+) different assets$")
    public void hasBlockedReceivingDifferentAssets(final String username, final int count) {
        
    }

    @Given("^(\\w+) has only allowed receiving \"([^\"]*)\" assets$")
    public void hasOnlyAllowedReceivingAssets(final String username, final String asset) {
        this.allowsOrBlocksReceivingTransactionsContainingTheFollowingItems(username, "allows",
                "assets", new ArrayList<>(Arrays.asList(asset)));
    }
}