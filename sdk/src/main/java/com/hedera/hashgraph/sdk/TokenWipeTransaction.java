package com.hedera.hashgraph.sdk;

import com.hedera.hashgraph.sdk.proto.TokenWipeAccountTransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import com.hedera.hashgraph.sdk.proto.TokenServiceGrpc;
import io.grpc.MethodDescriptor;

public class TokenWipeTransaction extends com.hedera.hashgraph.sdk.Transaction<TokenWipeTransaction> {
    private final TokenWipeAccountTransactionBody.Builder builder;

    public TokenWipeTransaction() {
        builder = TokenWipeAccountTransactionBody.newBuilder();
    }

    TokenWipeTransaction(TransactionBody body) {
        super(body);

        builder = body.getTokenWipe().toBuilder();
    }

    public TokenId getTokenId() {
        return TokenId.fromProtobuf(builder.getToken());
    }

    public TokenWipeTransaction setTokenId(TokenId tokenId) {
        requireNotFrozen();
        builder.setToken(tokenId.toProtobuf());
        return this;
    }

    public AccountId getAccountId() {
        return AccountId.fromProtobuf(builder.getAccount());
    }

    public TokenWipeTransaction setAccountId(AccountId accountId) {
        requireNotFrozen();
        builder.setAccount(accountId.toProtobuf());
        return this;
    }

    public long getAmount() {
        return builder.getAmount();
    }

    public TokenWipeTransaction setAmount(long amount) {
        requireNotFrozen();
        builder.setAmount(amount);
        return this;
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return TokenServiceGrpc.getWipeTokenAccountMethod();
    }

    @Override
    boolean onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setTokenWipe(builder);
        return true;
    }
}

