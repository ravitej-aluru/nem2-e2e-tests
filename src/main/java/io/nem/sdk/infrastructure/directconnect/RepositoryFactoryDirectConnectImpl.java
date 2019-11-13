package io.nem.sdk.infrastructure.directconnect;

import io.nem.sdk.api.AccountRepository;
import io.nem.sdk.api.BlockRepository;
import io.nem.sdk.api.ChainRepository;
import io.nem.sdk.api.DiagnosticRepository;
import io.nem.sdk.api.JsonSerialization;
import io.nem.sdk.api.MetadataRepository;
import io.nem.sdk.api.MosaicRepository;
import io.nem.sdk.api.MultisigRepository;
import io.nem.sdk.api.NamespaceRepository;
import io.nem.sdk.api.NetworkRepository;
import io.nem.sdk.api.NodeRepository;
import io.nem.sdk.api.ReceiptRepository;
import io.nem.sdk.api.RepositoryFactory;
import io.nem.sdk.api.RestrictionAccountRepository;
import io.nem.sdk.api.RestrictionMosaicRepository;
import io.nem.sdk.api.TransactionRepository;
import io.nem.sdk.infrastructure.Listener;
import io.nem.sdk.infrastructure.common.CatapultContext;
import io.nem.sdk.infrastructure.directconnect.dataaccess.dao.AccountsDao;
import io.nem.sdk.infrastructure.directconnect.dataaccess.dao.BlockchainDao;
import io.nem.sdk.infrastructure.directconnect.dataaccess.dao.MosaicsDao;
import io.nem.sdk.infrastructure.directconnect.dataaccess.dao.MultisigDao;
import io.nem.sdk.infrastructure.directconnect.dataaccess.dao.NamespaceDao;
import io.nem.sdk.infrastructure.directconnect.dataaccess.dao.NetworkDao;
import io.nem.sdk.infrastructure.directconnect.dataaccess.dao.TransactionDao;

public class RepositoryFactoryDirectConnectImpl implements RepositoryFactory {

    private final CatapultContext context;

    public RepositoryFactoryDirectConnectImpl(
        CatapultContext context) {
        this.context = context;
    }

    @Override
    public AccountRepository createAccountRepository() {
        return new AccountsDao(context);
    }

    @Override
    public MultisigRepository createMultisigRepository() {
        return new MultisigDao(context);
    }

    @Override
    public BlockRepository createBlockRepository() {
        return new BlockchainDao(context);
    }

    @Override
    public ReceiptRepository createReceiptRepository() {
        return new BlockchainDao(context);
    }

    @Override
    public ChainRepository createChainRepository() {
        return new BlockchainDao(context);
    }

    @Override
    public DiagnosticRepository createDiagnosticRepository() {
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public MosaicRepository createMosaicRepository() {
        return new MosaicsDao(context);
    }

    @Override
    public NamespaceRepository createNamespaceRepository() {
        return new NamespaceDao(context);
    }

    @Override
    public NetworkRepository createNetworkRepository() {
        return new NetworkDao(context);
    }

    @Override
    public NodeRepository createNodeRepository() {
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public TransactionRepository createTransactionRepository() {
        return new TransactionDao(context);
    }

    @Override
    public MetadataRepository createMetadataRepository() {
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public RestrictionAccountRepository createRestrictionAccountRepository() {
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public RestrictionMosaicRepository createRestrictionMosaicRepository() {
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public Listener createListener() {
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public JsonSerialization createJsonSerialization() {
        throw new IllegalStateException("Method not implemented");
    }

    @Override
    public void close() {

    }
}
