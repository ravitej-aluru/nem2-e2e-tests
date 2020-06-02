package io.nem.symbol.sdk.infrastructure.directconnect.dataaccess.dao;

import io.nem.symbol.core.utils.ConvertUtils;
import io.nem.symbol.sdk.api.MetadataRepository;
import io.nem.symbol.sdk.api.QueryParams;
import io.nem.symbol.sdk.infrastructure.common.CatapultContext;
import io.nem.symbol.sdk.infrastructure.directconnect.dataaccess.database.mongoDb.MetadataCollection;
import io.nem.symbol.sdk.model.account.Address;
import io.nem.symbol.sdk.model.metadata.Metadata;
import io.nem.symbol.sdk.model.mosaic.MosaicId;
import io.nem.symbol.sdk.model.namespace.NamespaceId;
import io.reactivex.Observable;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;

public class MetadataDao implements MetadataRepository {
  /* Catapult context. */
  private final CatapultContext catapultContext;
  private final MetadataCollection metadataCollection;

  /**
   * Constructor.
   *
   * @param context Catapult context.
   */
  public MetadataDao(final CatapultContext context) {
    this.catapultContext = context;
    metadataCollection = new MetadataCollection(catapultContext.getDataAccessContext());
  }

  byte[] getAccountPublicKey(final Address address) {
    return new AccountsDao(catapultContext)
        .getAccountInfo(address)
        .blockingFirst()
        .getPublicAccount()
        .getPublicKey()
        .getBytes();
  }

  private Metadata getMetadataOrThrow(final Optional<Metadata> optionalMetadata, final String error) {
    return optionalMetadata.orElseThrow(() -> new IllegalArgumentException(error));
  }

  /**
   * Returns the account metadata given an account id.
   *
   * @param targetAddress the address that holds the medata values.
   * @param queryParams Optional query parameters
   * @return Observable of {@link Metadata} {@link List}
   */
  @Override
  public Observable<List<Metadata>> getAccountMetadata(
      Address targetAddress, Optional<QueryParams> queryParams) {
    return Observable.fromCallable(
        () -> metadataCollection.getAccountMetadata(getAccountPublicKey(targetAddress)));
  }

  /**
   * Returns the account metadata given an account id and a key
   *
   * @param targetAddress the address that holds the medata values with the given key.
   * @param key Metadata key
   * @return Observable of {@link Metadata} {@link List}
   */
  @Override
  public Observable<List<Metadata>> getAccountMetadataByKey(Address targetAddress, BigInteger key) {
    return Observable.fromCallable(
        () ->
            metadataCollection.getAccountMetadataByKey(
                getAccountPublicKey(targetAddress), key.longValue()));
  }

  /**
   * Returns the account metadata given an account id and a key
   *
   * @param targetAddress the address that holds the metadata values with the given key sent by the
   *     given public key.
   * @param key - Metadata key
   * @param senderPublicKey The public key of the account that created the metadata.
   * @return Observable of {@link Metadata}
   */
  @Override
  public Observable<Metadata> getAccountMetadataByKeyAndSender(
      Address targetAddress, BigInteger key, String senderPublicKey) {
    return Observable.fromCallable(
        () ->
            getMetadataOrThrow(metadataCollection.getAccountMetadataByKeyAndSender(
                getAccountPublicKey(targetAddress),
                key.longValue(),
                ConvertUtils.fromHexToBytes(senderPublicKey)),
                    "Not found:Account metadata for address:" +
                            targetAddress.plain() + " from public key:" +
                            senderPublicKey + " with key:" +
                            key.longValue()));
  }

  /**
   * Returns the mosaic metadata given a mosaic id.
   *
   * @param targetMosaicId The mosaic id that holds the metadata values.
   * @param queryParams Optional query parameters
   * @return Observable of {@link Metadata} {@link List}
   */
  @Override
  public Observable<List<Metadata>> getMosaicMetadata(
      MosaicId targetMosaicId, Optional<QueryParams> queryParams) {
    return Observable.fromCallable(
        () -> metadataCollection.getMosaicMetadata(targetMosaicId.getIdAsLong()));
  }

  /**
   * Returns the mosaic metadata given a mosaic id and metadata key.
   *
   * @param targetMosaicId The mosaic id that holds the metadata values.
   * @param key Metadata key.
   * @return Observable of {@link Metadata} {@link List}
   */
  @Override
  public Observable<List<Metadata>> getMosaicMetadataByKey(
      MosaicId targetMosaicId, BigInteger key) {
    return Observable.fromCallable(
        () ->
            metadataCollection.getMosaicMetadataByKey(
                targetMosaicId.getIdAsLong(), key.longValue()));
  }

  /**
   * Returns the mosaic metadata given a mosaic id and metadata key.
   *
   * @param targetMosaicId The mosaic id that holds the metadata values.
   * @param key Metadata key.
   * @param senderPublicKey The public key of the account that created the metadata.
   * @return Observable of {@link Metadata} {@link List}
   */
  @Override
  public Observable<Metadata> getMosaicMetadataByKeyAndSender(
      MosaicId targetMosaicId, BigInteger key, String senderPublicKey) {
    return Observable.fromCallable(
        () ->
            getMetadataOrThrow(metadataCollection.getMosaicMetadataByKeyAndSender(
                targetMosaicId.getIdAsLong(),
                key.longValue(),
                ConvertUtils.fromHexToBytes(senderPublicKey)),
                    "Not found:Mosaic metadata for id:" +
                            targetMosaicId.getId() + " from public key:" +
                            senderPublicKey + " with key:" +
                            key.longValue()));
  }

  /**
   * Returns the mosaic metadata given a mosaic id.
   *
   * @param targetNamespaceId The namespace id that holds the metadata values.
   * @param queryParams Optional query parameters
   * @return Observable of {@link Metadata} {@link List}
   */
  @Override
  public Observable<List<Metadata>> getNamespaceMetadata(
      NamespaceId targetNamespaceId, Optional<QueryParams> queryParams) {
    return Observable.fromCallable(
        () -> metadataCollection.getNamespaceMetadata(targetNamespaceId.getIdAsLong()));
  }

  /**
   * Returns the mosaic metadata given a mosaic id and metadata key.
   *
   * @param targetNamespaceId The namespace id that holds the metadata values.
   * @param key Metadata key.
   * @return Observable of {@link Metadata} {@link List}
   */
  @Override
  public Observable<List<Metadata>> getNamespaceMetadataByKey(
      NamespaceId targetNamespaceId, BigInteger key) {
    return Observable.fromCallable(
        () ->
            metadataCollection.getNamespaceMetadataByKey(
                targetNamespaceId.getIdAsLong(), key.longValue()));
  }

  /**
   * Returns the namespace metadata given a mosaic id and metadata key.
   *
   * @param targetNamespaceId The namespace id that holds the metadata values.
   * @param key Metadata key.
   * @param senderPublicKey The public key of the account that created the metadata.
   * @return Observable of {@link Metadata}
   */
  @Override
  public Observable<Metadata> getNamespaceMetadataByKeyAndSender(
      NamespaceId targetNamespaceId, BigInteger key, String senderPublicKey) {
    return Observable.fromCallable(
        () ->
            getMetadataOrThrow(metadataCollection.getNamespaceMetadataByKeyAndSender(
                targetNamespaceId.getIdAsLong(),
                key.longValue(),
                ConvertUtils.fromHexToBytes(senderPublicKey)),
                    "Not found:Namespace metadata for id:" +
                            targetNamespaceId.getIdAsLong() + " from public key:" +
                            senderPublicKey + " with key:" +
                            key.longValue()));
  }
}
