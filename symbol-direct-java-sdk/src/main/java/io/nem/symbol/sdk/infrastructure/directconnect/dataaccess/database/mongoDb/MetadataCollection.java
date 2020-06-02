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

package io.nem.symbol.sdk.infrastructure.directconnect.dataaccess.database.mongoDb;

import com.mongodb.client.model.Filters;
import io.nem.symbol.sdk.infrastructure.directconnect.dataaccess.common.DataAccessContext;
import io.nem.symbol.sdk.infrastructure.directconnect.dataaccess.mappers.MetadataEntryMapper;
import io.nem.symbol.sdk.model.metadata.Metadata;
import org.bson.conversions.Bson;
import org.bson.types.Binary;

import java.util.List;
import java.util.Optional;

/* Metadata Collection */
public class MetadataCollection {
  /** Catapult collection */
  private final CatapultCollection<Metadata, MetadataEntryMapper> catapultCollection;
  /* Catapult context. */
  private final DataAccessContext context;

  /**
   * Constructor.
   *
   * @param context Catapult context.
   */
  public MetadataCollection(final DataAccessContext context) {
    this.context = context;
    catapultCollection =
        new CatapultCollection<>(
            context.getCatapultMongoDbClient(), "metadata", MetadataEntryMapper::new);
  }

  private Bson andFilter(final Bson... filters) {
    return Filters.and(filters);
  }

  private Bson getTargetPublicKeyFilter(final byte[] publicKeyBytes) {
    return Filters.eq("metadataEntry.targetPublicKey", new Binary((byte) 0, publicKeyBytes));
  }

  private Bson getKeyFilter(final long key) {
    return Filters.eq("metadataEntry.scopedMetadataKey", key);
  }

  private Bson getSenderPublicKeyFilter(final byte[] senderPublicKeyBytes) {
    return Filters.eq("metadataEntry.targetPublicKey", new Binary((byte) 0, senderPublicKeyBytes));
  }

  private Bson getTargetIdFilter(final long targetId) {
    return Filters.eq("metadataEntry.targetId", targetId);
  }

  /**
   * Returns the account metadata given an account id.
   *
   * @param publicKeyBytes public key of the account that holds the metadata values.
   * @return Observable of {@link Metadata} {@link List}
   */
  public List<Metadata> getAccountMetadata(final byte[] publicKeyBytes) {
    return catapultCollection.findR(
        getTargetPublicKeyFilter(publicKeyBytes), context.getDatabaseTimeoutInSeconds());
  }

  /**
   * Returns the account metadata given an account id and a key
   *
   * @param publicKeyBytes public key of the account that holds the metadata values.
   * @param key Metadata key
   * @return Observable of {@link Metadata} {@link List}
   */
  public List<Metadata> getAccountMetadataByKey(final byte[] publicKeyBytes, final long key) {
    return catapultCollection.findR(
        andFilter(getTargetPublicKeyFilter(publicKeyBytes), getKeyFilter(key)),
        context.getDatabaseTimeoutInSeconds());
  }

  /**
   * Returns the account metadata given an account id and a key
   *
   * @param publicKeyBytes public key of the account that holds the metadata values.
   * @param key - Metadata key
   * @param senderPublicKey The public key of the account that created the metadata.
   * @return Observable of {@link Metadata}
   */
  public Optional<Metadata> getAccountMetadataByKeyAndSender(
      final byte[] publicKeyBytes, final long key, final byte[] senderPublicKey) {
    return catapultCollection.findOneR(
        andFilter(
            getTargetPublicKeyFilter(publicKeyBytes),
            getKeyFilter(key),
            getSenderPublicKeyFilter(senderPublicKey)),
        context.getDatabaseTimeoutInSeconds());
  }

  /**
   * Returns the mosaic metadata given a mosaic id.
   *
   * @param targetMosaicId The mosaic id that holds the metadata values.
   * @return Observable of {@link Metadata} {@link List}
   */
  public List<Metadata> getMosaicMetadata(final long targetMosaicId) {
    return catapultCollection.findR(
        getTargetIdFilter(targetMosaicId), context.getDatabaseTimeoutInSeconds());
  }

  /**
   * Returns the mosaic metadata given a mosaic id and metadata key.
   *
   * @param targetMosaicId The mosaic id that holds the metadata values.
   * @param key Metadata key.
   * @return Observable of {@link Metadata} {@link List}
   */
  public List<Metadata> getMosaicMetadataByKey(final long targetMosaicId, final long key) {
    return catapultCollection.findR(
        andFilter(getTargetIdFilter(targetMosaicId), getKeyFilter(key)),
        context.getDatabaseTimeoutInSeconds());
  }

  /**
   * Returns the mosaic metadata given a mosaic id and metadata key.
   *
   * @param targetMosaicId The mosaic id that holds the metadata values.
   * @param key Metadata key.
   * @param senderPublicKey The public key of the account that created the metadata.
   * @return Observable of {@link Metadata} {@link List}
   */
  public Optional<Metadata> getMosaicMetadataByKeyAndSender(
      final long targetMosaicId, final long key, final byte[] senderPublicKey) {
    return catapultCollection.findOneR(
            andFilter(getTargetIdFilter(targetMosaicId), getKeyFilter(key), getSenderPublicKeyFilter(senderPublicKey)),
            context.getDatabaseTimeoutInSeconds());
  }

  /**
   * Returns the mosaic metadata given a mosaic id.
   *
   * @param targetNamespaceId The namespace id that holds the metadata values.
   * @return Observable of {@link Metadata} {@link List}
   */
  public List<Metadata> getNamespaceMetadata(final long targetNamespaceId) {
    return catapultCollection.findR(
            getTargetIdFilter(targetNamespaceId), context.getDatabaseTimeoutInSeconds());
  }

  /**
   * Returns the mosaic metadata given a mosaic id and metadata key.
   *
   * @param targetNamespaceId The namespace id that holds the metadata values.
   * @param key Metadata key.
   * @return Observable of {@link Metadata} {@link List}
   */
  public List<Metadata> getNamespaceMetadataByKey(
      final long  targetNamespaceId, final long key) {
    return catapultCollection.findR(
            andFilter(getTargetIdFilter(targetNamespaceId), getKeyFilter(key)),
            context.getDatabaseTimeoutInSeconds());
  }

  /**
   * Returns the namespace metadata given a mosaic id and metadata key.
   *
   * @param targetNamespaceId The namespace id that holds the metadata values.
   * @param key Metadata key.
   * @param senderPublicKey The public key of the account that created the metadata.
   * @return Observable of {@link Metadata}
   */
  public Optional<Metadata> getNamespaceMetadataByKeyAndSender(
      final long targetNamespaceId, final long key, final byte[] senderPublicKey) {
    return catapultCollection.findOneR(
            andFilter(getTargetIdFilter(targetNamespaceId), getKeyFilter(key), getSenderPublicKeyFilter(senderPublicKey)),
            context.getDatabaseTimeoutInSeconds());  }
}
