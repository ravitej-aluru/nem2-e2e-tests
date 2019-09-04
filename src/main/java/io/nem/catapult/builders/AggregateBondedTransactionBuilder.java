/**
*** Copyright (c) 2016-present,
*** Jaguar0625, gimre, BloodyRookie, Tech Bureau, Corp. All rights reserved.
***
*** This file is part of Catapult.
***
*** Catapult is free software: you can redistribute it and/or modify
*** it under the terms of the GNU Lesser General Public License as published by
*** the Free Software Foundation, either version 3 of the License, or
*** (at your option) any later version.
***
*** Catapult is distributed in the hope that it will be useful,
*** but WITHOUT ANY WARRANTY; without even the implied warranty of
*** MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
*** GNU Lesser General Public License for more details.
***
*** You should have received a copy of the GNU Lesser General Public License
*** along with Catapult. If not, see <http://www.gnu.org/licenses/>.
**/

package io.nem.catapult.builders;

import java.io.DataInput;
import java.util.ArrayList;

/** Binary layout for an aggregate bonded transaction. */
public final class AggregateBondedTransactionBuilder extends TransactionBuilder {
    /** Aggregate transaction body. */
    private final AggregateTransactionBodyBuilder aggregateTransactionBody;

    /**
     * Constructor - Creates an object from stream.
     *
     * @param stream Byte stream to use to serialize the object.
     */
    protected AggregateBondedTransactionBuilder(final DataInput stream) {
        super(stream);
        this.aggregateTransactionBody = AggregateTransactionBodyBuilder.loadFromBinary(stream);
    }

    /**
     * Constructor.
     *
     * @param signature Entity signature.
     * @param signer Entity signer's public key.
     * @param version Entity version.
     * @param type Entity type.
     * @param fee Transaction fee.
     * @param deadline Transaction deadline.
     * @param transactions Sub-transaction data (transactions are variable sized and payload size is in bytes).
     * @param cosignatures Cosignatures data (fills remaining body space after transactions).
     */
    protected AggregateBondedTransactionBuilder(final SignatureDto signature, final KeyDto signer, final short version, final EntityTypeDto type, final AmountDto fee, final TimestampDto deadline, final ArrayList<TransactionBuilder> transactions, final ArrayList<CosignatureBuilder> cosignatures) {
        super(signature, signer, version, type, fee, deadline);
        this.aggregateTransactionBody = AggregateTransactionBodyBuilder.create(transactions, cosignatures);
    }

    /**
     * Creates an instance of AggregateBondedTransactionBuilder.
     *
     * @param signature Entity signature.
     * @param signer Entity signer's public key.
     * @param version Entity version.
     * @param type Entity type.
     * @param fee Transaction fee.
     * @param deadline Transaction deadline.
     * @param transactions Sub-transaction data (transactions are variable sized and payload size is in bytes).
     * @param cosignatures Cosignatures data (fills remaining body space after transactions).
     * @return Instance of AggregateBondedTransactionBuilder.
     */
    public static AggregateBondedTransactionBuilder create(final SignatureDto signature, final KeyDto signer, final short version, final EntityTypeDto type, final AmountDto fee, final TimestampDto deadline, final ArrayList<TransactionBuilder> transactions, final ArrayList<CosignatureBuilder> cosignatures) {
        return new AggregateBondedTransactionBuilder(signature, signer, version, type, fee, deadline, transactions, cosignatures);
    }

    /**
     * Gets sub-transaction data (transactions are variable sized and payload size is in bytes).
     *
     * @return Sub-transaction data (transactions are variable sized and payload size is in bytes).
     */
    public ArrayList<TransactionBuilder> getTransactions() {
        return this.aggregateTransactionBody.getTransactions();
    }

    /**
     * Gets cosignatures data (fills remaining body space after transactions).
     *
     * @return Cosignatures data (fills remaining body space after transactions).
     */
    public ArrayList<CosignatureBuilder> getCosignatures() {
        return this.aggregateTransactionBody.getCosignatures();
    }

    /**
     * Gets the size of the object.
     *
     * @return Size in bytes.
     */
    @Override
    public int getSize() {
        int size = super.getSize();
        size += this.aggregateTransactionBody.getSize();
        return size;
    }

    /**
     * Creates an instance of AggregateBondedTransactionBuilder from a stream.
     *
     * @param stream Byte stream to use to serialize the object.
     * @return Instance of AggregateBondedTransactionBuilder.
     */
    public static AggregateBondedTransactionBuilder loadFromBinary(final DataInput stream) {
        return new AggregateBondedTransactionBuilder(stream);
    }

    /**
     * Serializes an object to bytes.
     *
     * @return Serialized bytes.
     */
    public byte[] serialize() {
        return GeneratorUtils.serialize(dataOutputStream -> {
            final byte[] superBytes = super.serialize();
            dataOutputStream.write(superBytes, 0, superBytes.length);
            final byte[] aggregateTransactionBodyBytes = this.aggregateTransactionBody.serialize();
            dataOutputStream.write(aggregateTransactionBodyBytes, 0, aggregateTransactionBodyBytes.length);
        });
    }
}
