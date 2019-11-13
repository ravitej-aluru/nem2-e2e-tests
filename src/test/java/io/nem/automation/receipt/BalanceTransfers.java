/*
 * Copyright (c) 2016-present,
 * Jaguar0625, gimre, BloodyRookie, Tech Bureau, Corp. All rights reserved.
 *
 * This file is part of Catapult.
 *
 * Catapult is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Catapult is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Catapult.  If not, see <http://www.gnu.org/licenses/>.
 */

package io.nem.automation.receipt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import io.nem.automation.common.BaseTest;
import io.nem.automationHelpers.common.TestContext;
import io.nem.automationHelpers.helper.BlockChainHelper;
import io.nem.sdk.model.account.Account;
import io.nem.sdk.model.account.PublicAccount;
import io.nem.sdk.model.mosaic.MosaicInfo;
import io.nem.sdk.model.namespace.NamespaceInfo;
import io.nem.sdk.model.receipt.BalanceTransferReceipt;
import io.nem.sdk.model.receipt.ReceiptType;
import io.nem.sdk.model.receipt.Statement;
import io.nem.sdk.model.transaction.NamespaceRegistrationTransaction;
import io.nem.sdk.model.transaction.TransactionType;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Optional;

public class BalanceTransfers extends BaseTest {

    public BalanceTransfers(final TestContext testContext) {
        super(testContext);
    }

    private BigInteger getBalanceTransferCost(final PublicAccount publicAccount,
        final BigInteger height, final ReceiptType receiptType) {
        final Statement statement = new BlockChainHelper(getTestContext()).getBlockReceipts(height);
        final Optional<BalanceTransferReceipt> receiptCost =
            statement.getTransactionStatements().stream().map(s -> s.getReceipts())
                .flatMap(Collection::stream).filter(receipt -> {
                if (receipt.getType() == receiptType) {
                    final BalanceTransferReceipt balanceTransferReceipt = (BalanceTransferReceipt) receipt;
                    if (balanceTransferReceipt.getSender().equals(publicAccount)) {
                        return true;
                    }
                    ;
                }
                return false;
            }).findAny().map(f -> (BalanceTransferReceipt) f);
        assertTrue(receiptCost.isPresent(), "Transaction statement was not found");
        return receiptCost.get().getAmount();
    }

    @When("^(\\w+) checks how much cost registering the asset$")
    public void checkRegisteringAsset(final String userName) {

    }

    @When("^she checks how much cost registering the namespace$")
    public void checksRegisteringNamespaceCost() {

    }

    @When("^she checks how much cost extending the namespace$")
    public void checksExtendingNamespaceCost() {

    }

    @Then("^(\\w+) should get that registering the asset \"(.*)\" cost \"(\\d+)\" cat.currency$")
    public void verifyAssetCost(final String userName, final String assetName,
        final BigInteger cost) {
        final Account account = getUser(userName);
        final MosaicInfo mosaicInfo = getTestContext().getScenarioContext().getContext(assetName);
        final BigInteger actualCost = getBalanceTransferCost(account.getPublicAccount(),
            mosaicInfo.getStartHeight(),
            ReceiptType.MOSAIC_RENTAL_FEE);
        final BigInteger exceptedCost = getCalculatedDynamicFee(cost.longValue());
        assertEquals(
            exceptedCost.longValue(),
            actualCost.longValue(),
            "Asset registration cost did not match for asset id:" + mosaicInfo.getMosaicId()
                .getIdAsLong());
    }

    @Then("^(\\w+) should get that registering the namespace cost \"(\\d+)\" cat.currency$")
    public void verifyNamespaceRegisterCost(final String userName, final BigInteger cost) {
        final Account account = getUser(userName);
        final NamespaceInfo namespaceInfo = getTestContext().getScenarioContext()
            .getContext(NAMESPACE_INFO_KEY);
        final BigInteger actualCost =
            getBalanceTransferCost(account.getPublicAccount(), namespaceInfo.getStartHeight(),
                ReceiptType.NAMESPACE_RENTAL_FEE);
        final BigInteger exceptedCost = getCalculatedDynamicFee(cost.longValue());
        assertEquals(
            exceptedCost.longValue(),
            actualCost.longValue(),
            "Namespace registration cost did not match for namespace id:" + namespaceInfo.getId()
                .getIdAsLong());
    }

    @Then("^(\\w+) should get that extending the namespace cost \"(\\d+)\" cat.currency$")
    public void verifyNamespaceExtendCost(final String userName, final BigInteger cost) {
        final Account account = getUser(userName);
        final NamespaceRegistrationTransaction namespaceRegistrationTransaction =
            getTestContext().<NamespaceRegistrationTransaction>findTransaction(
                TransactionType.REGISTER_NAMESPACE).get();
        final BigInteger actualCost =
            getBalanceTransferCost(account.getPublicAccount(),
                namespaceRegistrationTransaction.getTransactionInfo().get().getHeight(),
                ReceiptType.NAMESPACE_RENTAL_FEE);
        final BigInteger exceptedCost = getCalculatedDynamicFee(cost.longValue());
        assertEquals(
            exceptedCost.longValue(),
            actualCost.longValue(), "Namespace extension cost did not match for namespace id:" +
                namespaceRegistrationTransaction.getNamespaceId().getIdAsLong());
    }
}