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

/** Binary layout for an aggregate transaction. */
final class AggregateTransactionBodyBuilder {
    /** Sub-transaction data (transactions are variable sized and payload size is in bytes). */
    private final ArrayList<TransactionBuilder> transactions;
    /** Cosignatures data (fills remaining body space after transactions). */
    private final ArrayList<CosignatureBuilder> cosignatures;

    /**
     * Constructor - Creates an object from stream.
     *
     * @param stream Byte stream to use to serialize the object.
     */
    protected AggregateTransactionBodyBuilder(final DataInput stream) {
        try {
            final int payloadSize = Integer.reverseBytes(stream.readInt());
            this.transactions = new java.util.ArrayList<>(payloadSize);
            for (int i = 0; i < payloadSize; i++) {
                transactions.add(TransactionBuilder.loadFromBinary(stream));
            }
            this.cosignatures = new java.util.ArrayList<>(0);
            for (int i = 0; i < 0; i++) {
                cosignatures.add(CosignatureBuilder.loadFromBinary(stream));
            }
        } catch(Exception e) {
            throw GeneratorUtils.getExceptionToPropagate(e);
        }
    }

    /**
     * Constructor.
     *
     * @param transactions Sub-transaction data (transactions are variable sized and payload size is in bytes).
     * @param cosignatures Cosignatures data (fills remaining body space after transactions).
     */
    protected AggregateTransactionBodyBuilder(final ArrayList<TransactionBuilder> transactions, final ArrayList<CosignatureBuilder> cosignatures) {
        GeneratorUtils.notNull(transactions, "transactions is null");
        GeneratorUtils.notNull(cosignatures, "cosignatures is null");
        this.transactions = transactions;
        this.cosignatures = cosignatures;
    }

    /**
     * Creates an instance of AggregateTransactionBodyBuilder.
     *
     * @param transactions Sub-transaction data (transactions are variable sized and payload size is in bytes).
     * @param cosignatures Cosignatures data (fills remaining body space after transactions).
     * @return Instance of AggregateTransactionBodyBuilder.
     */
    public static AggregateTransactionBodyBuilder create(final ArrayList<TransactionBuilder> transactions, final ArrayList<CosignatureBuilder> cosignatures) {
        return new AggregateTransactionBodyBuilder(transactions, cosignatures);
    }

    /**
     * Gets sub-transaction data (transactions are variable sized and payload size is in bytes).
     *
     * @return Sub-transaction data (transactions are variable sized and payload size is in bytes).
     */
    public ArrayList<TransactionBuilder> getTransactions() {
        return this.transactions;
    }

    /**
     * Gets cosignatures data (fills remaining body space after transactions).
     *
     * @return Cosignatures data (fills remaining body space after transactions).
     */
    public ArrayList<CosignatureBuilder> getCosignatures() {
        return this.cosignatures;
    }

    /**
     * Gets the size of the object.
     *
     * @return Size in bytes.
     */
    public int getSize() {
        int size = 0;
        size += 4; // payloadSize
        size += this.transactions.stream().mapToInt(o -> o.getSize()).sum();
        size += this.cosignatures.stream().mapToInt(o -> o.getSize()).sum();
        return size;
    }

    /**
     * Creates an instance of AggregateTransactionBodyBuilder from a stream.
     *
     * @param stream Byte stream to use to serialize the object.
     * @return Instance of AggregateTransactionBodyBuilder.
     */
    public static AggregateTransactionBodyBuilder loadFromBinary(final DataInput stream) {
        return new AggregateTransactionBodyBuilder(stream);
    }

    /**
     * Serializes an object to bytes.
     *
     * @return Serialized bytes.
     */
    public byte[] serialize() {
        return GeneratorUtils.serialize(dataOutputStream -> {
            dataOutputStream.writeInt(Integer.reverseBytes((int) this.transactions.size()));
            for (int i = 0; i < this.transactions.size(); i++) {
                final byte[] transactionsBytes = this.transactions.get(i).serialize();
                dataOutputStream.write(transactionsBytes, 0, transactionsBytes.length);
            }
            for (int i = 0; i < this.cosignatures.size(); i++) {
                final byte[] cosignaturesBytes = this.cosignatures.get(i).serialize();
                dataOutputStream.write(cosignaturesBytes, 0, cosignaturesBytes.length);
            }
        });
    }
}
