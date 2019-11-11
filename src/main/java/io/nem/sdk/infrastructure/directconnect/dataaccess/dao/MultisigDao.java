/**
 * ** Copyright (c) 2016-present, ** Jaguar0625, gimre, BloodyRookie, Tech Bureau, Corp. All rights
 * reserved. ** ** This file is part of Catapult. ** ** Catapult is free software: you can
 * redistribute it and/or modify ** it under the terms of the GNU Lesser General Public License as
 * published by ** the Free Software Foundation, either version 3 of the License, or ** (at your
 * option) any later version. ** ** Catapult is distributed in the hope that it will be useful, **
 * but WITHOUT ANY WARRANTY; without even the implied warranty of ** MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the ** GNU Lesser General Public License for more details. ** ** You
 * should have received a copy of the GNU Lesser General Public License ** along with Catapult. If
 * not, see <http://www.gnu.org/licenses/>.
 **/

package io.nem.sdk.infrastructure.directconnect.dataaccess.dao;

import io.nem.sdk.api.AccountRepository;
import io.nem.sdk.api.MultisigRepository;
import io.nem.sdk.infrastructure.SerializationExtUtils;
import io.nem.sdk.infrastructure.common.CatapultContext;
import io.nem.sdk.infrastructure.directconnect.dataaccess.database.mongoDb.AccountsCollection;
import io.nem.sdk.infrastructure.directconnect.dataaccess.database.mongoDb.MultisigsCollection;
import io.nem.sdk.infrastructure.directconnect.dataaccess.database.mongoDb.PartialTransactionsCollection;
import io.nem.sdk.infrastructure.directconnect.dataaccess.database.mongoDb.UnconfirmedTransactionsCollection;
import io.nem.sdk.model.account.AccountInfo;
import io.nem.sdk.model.account.Address;
import io.nem.sdk.model.account.MultisigAccountGraphInfo;
import io.nem.sdk.model.account.MultisigAccountInfo;
import io.nem.sdk.model.account.PublicAccount;
import io.nem.sdk.model.transaction.AggregateTransaction;
import io.nem.sdk.model.transaction.Transaction;
import io.reactivex.Observable;
import java.util.List;
import java.util.stream.Collectors;

/** Account dao repository. */
public class MultisigDao implements MultisigRepository {

    /* Catapult context. */
    private final CatapultContext catapultContext;

    /**
     * Constructor.
     *
     * @param context Catapult context.
     */
    public MultisigDao(final CatapultContext context) {
        this.catapultContext = context;
    }

    /**
     * Gets Multisig account info for address.
     *
     * @param address Account's address.
     * @return Multisig account info.
     */
    @Override
    public Observable<MultisigAccountInfo> getMultisigAccountInfo(final Address address) {
        return Observable.fromCallable(
            () ->
                new MultisigsCollection(catapultContext.getDataAccessContext())
                    .findByAddress(SerializationExtUtils.fromAddressToByteBuffer(address).array())
                    .get());
    }

    @Override
    public Observable<MultisigAccountGraphInfo> getMultisigAccountGraphInfo(Address address) {
        throw new IllegalStateException("Method not implemented");
    }
}
