/*-
 *
 * Hedera Java SDK
 *
 * Copyright (C) 2020 - 2022 Hedera Hashgraph, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package com.hedera.hashgraph.sdk;

import com.google.protobuf.Int32Value;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.StringValue;
import com.hedera.hashgraph.sdk.proto.ContractUpdateTransactionBody;
import com.hedera.hashgraph.sdk.proto.SchedulableTransactionBody;
import com.hedera.hashgraph.sdk.proto.SmartContractServiceGrpc;
import com.hedera.hashgraph.sdk.proto.TransactionBody;
import com.hedera.hashgraph.sdk.proto.TransactionResponse;
import io.grpc.MethodDescriptor;
import org.checkerframework.checker.units.qual.A;
import org.threeten.bp.Duration;
import org.threeten.bp.Instant;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Objects;

/**
 * Modify a smart contract instance to have the given parameter values.
 * <p>
 * Any null field is ignored (left unchanged).
 * <p>
 * If only the contractInstanceExpirationTime is being modified, then no signature is
 * needed on this transaction other than for the account paying for the transaction itself.
 * <p>
 * But if any of the other fields are being modified, then it must be signed by the adminKey.
 * <p>
 * The use of adminKey is not currently supported in this API, but in the future will
 * be implemented to allow these fields to be modified, and also to make modifications
 * to the state of the instance.
 * <p>
 * If the contract is created with no admin key, then none of the fields can be
 * changed that need an admin signature, and therefore no admin key can ever be added.
 * So if there is no admin key, then things like the bytecode are immutable.
 * But if there is an admin key, then they can be changed. For example, the
 * admin key might be a threshold key, which requires 3 of 5 binding arbitration judges to
 * agree before the bytecode can be changed. This can be used to add flexibility to the management
 * of smart contract behavior. But this is optional. If the smart contract is created
 * without an admin key, then such a key can never be added, and its bytecode will be immutable.
 */
public final class ContractUpdateTransaction extends Transaction<ContractUpdateTransaction> {
    @Nullable
    private ContractId contractId = null;
    @Nullable
    private AccountId proxyAccountId = null;
    @Nullable
    private FileId bytecodeFileId = null;
    @Nullable
    private Instant expirationTime = null;
    @Nullable
    private Key adminKey = null;
    @Nullable
    private Integer maxAutomaticTokenAssociations = null;
    @Nullable
    private Duration autoRenewPeriod = null;
    @Nullable
    private String contractMemo = null;

    /**
     * Contract.
     */
    public ContractUpdateTransaction() {
    }

    /**
     * Contract.
     *
     * @param txs                       Compound list of transaction id's list of (AccountId, Transaction) record
     * @throws InvalidProtocolBufferException       when there is an issue with the protobuf
     */
    ContractUpdateTransaction(LinkedHashMap<TransactionId, LinkedHashMap<AccountId, com.hedera.hashgraph.sdk.proto.Transaction>> txs) throws InvalidProtocolBufferException {
        super(txs);
        initFromTransactionBody();
    }
    /**
     * Constructor.
     *
     * @param txBody protobuf TransactionBody
     */
    ContractUpdateTransaction(com.hedera.hashgraph.sdk.proto.TransactionBody txBody) {
        super(txBody);
        initFromTransactionBody();
    }

    /**
     * Extract the contract id.
     *
     * @return                          the contract id
     */
    @Nullable
    public ContractId getContractId() {
        return contractId;
    }

    /**
     * Sets the Contract ID instance to update.
     *
     * @param contractId The ContractId to be set
     * @return {@code this}
     */
    public ContractUpdateTransaction setContractId(ContractId contractId) {
        Objects.requireNonNull(contractId);
        requireNotFrozen();
        this.contractId = contractId;
        return this;
    }

    /**
     * Extract the contract expiration time.
     *
     * @return                          the contract expiration time
     */
    @Nullable
    public Instant getExpirationTime() {
        return expirationTime;
    }

    /**
     * Sets the expiration of the instance and its account to this time (
     * no effect if it already is this time or later).
     *
     * @param expirationTime The Instant to be set for expiration time
     * @return {@code this}
     */
    public ContractUpdateTransaction setExpirationTime(Instant expirationTime) {
        Objects.requireNonNull(expirationTime);
        requireNotFrozen();
        this.expirationTime = expirationTime;
        return this;
    }

    /**
     * Extract the administrator key.
     *
     * @return                          the administrator key
     */
    @Nullable
    public Key getAdminKey() {
        return adminKey;
    }

    /**
     * Sets a new admin key for this contract.
     *
     * @param adminKey The Key to be set
     * @return {@code this}
     */
    public ContractUpdateTransaction setAdminKey(Key adminKey) {
        Objects.requireNonNull(adminKey);
        requireNotFrozen();
        this.adminKey = adminKey;
        return this;
    }

    /**
     * Extract the proxy account id.
     *
     * @return                          the proxy account id
     */
    @Nullable
    public AccountId getProxyAccountId() {
        return proxyAccountId;
    }

    /**
     * Sets the ID of the account to which this account is proxy staked.
     * <p>
     * If proxyAccountID is null, or is an invalid account, or is an account
     * that isn't a node, then this account is automatically proxy staked to a
     * node chosen by the network, but without earning payments.
     * <p>
     * If the proxyAccountID account refuses to accept proxy staking, or if it is
     * not currently running a node, then it will behave as if proxyAccountID was null.
     *
     * @param proxyAccountId The AccountId to be set
     * @return {@code this}
     */
    public ContractUpdateTransaction setProxyAccountId(AccountId proxyAccountId) {
        Objects.requireNonNull(proxyAccountId);
        requireNotFrozen();
        this.proxyAccountId = proxyAccountId;
        return this;
    }

    /**
     * Extract the auto renew period.
     *
     * @return                          the auto renew period
     */
    @Nullable
    public Integer getMaxAutomaticTokenAssociations() {
        return maxAutomaticTokenAssociations;
    }

    /**
     * Sets the new maximum number of tokens that this contract can be
     * automatically associated with (i.e., receive air-drops from).
     *
     * @param maxAutomaticTokenAssociations The maximum automatic token associations
     * @return  {@code this}
     */

    public ContractUpdateTransaction setMaxAutomaticTokenAssociations(int maxAutomaticTokenAssociations) {
        requireNotFrozen();
        this.maxAutomaticTokenAssociations = maxAutomaticTokenAssociations;
        return this;
    }

    @Nullable
    public Duration getAutoRenewPeriod() {
        return autoRenewPeriod;
    }

    /**
     * Sets the auto renew period for this contract.
     *
     * @param autoRenewPeriod The Duration to be set for auto renewal
     * @return {@code this}
     */
    public ContractUpdateTransaction setAutoRenewPeriod(Duration autoRenewPeriod) {
        Objects.requireNonNull(autoRenewPeriod);
        requireNotFrozen();
        this.autoRenewPeriod = autoRenewPeriod;
        return this;
    }

    /**
     * @deprecated with no replacement
     */
    @Nullable
    @Deprecated
    public FileId getBytecodeFileId() {
        return bytecodeFileId;
    }

    /**
     * @deprecated with no replacement
     *
     * Sets the file ID of file containing the smart contract byte code.
     * <p>
     * A copy will be made and held by the contract instance, and have the same expiration
     * time as the instance.
     *
     * @param bytecodeFileId The FileId to be set
     * @return {@code this}
     */
    @Deprecated
    public ContractUpdateTransaction setBytecodeFileId(FileId bytecodeFileId) {
        Objects.requireNonNull(bytecodeFileId);
        requireNotFrozen();
        this.bytecodeFileId = bytecodeFileId;
        return this;
    }

    /**
     * Extract the contents of the memo.
     *
     * @return                          the contents of the memo
     */
    @Nullable
    public String getContractMemo() {
        return contractMemo;
    }

    /**
     * Sets the memo associated with the contract (max: 100 bytes).
     *
     * @param memo The memo to be set
     * @return {@code this}
     */
    public ContractUpdateTransaction setContractMemo(String memo) {
        Objects.requireNonNull(memo);
        requireNotFrozen();
        contractMemo = memo;
        return this;
    }

    /**
     * Remove the memo contents.
     *
     * @return {@code this}
     */
    public ContractUpdateTransaction clearMemo() {
        requireNotFrozen();
        contractMemo = "";
        return this;
    }

    /**
     * Initialize from the transaction body.
     */
    void initFromTransactionBody() {
        var body = sourceTransactionBody.getContractUpdateInstance();
        if (body.hasContractID()) {
            contractId = ContractId.fromProtobuf(body.getContractID());
        }
        if (body.hasProxyAccountID()) {
            proxyAccountId = AccountId.fromProtobuf(body.getProxyAccountID());
        }
        if (body.hasExpirationTime()) {
            expirationTime = InstantConverter.fromProtobuf(body.getExpirationTime());
        }
        if (body.hasAdminKey()) {
            adminKey = Key.fromProtobufKey(body.getAdminKey());
        }
        if (body.hasMaxAutomaticTokenAssociations()) {
            maxAutomaticTokenAssociations = body.getMaxAutomaticTokenAssociations().getValue();
        }
        if (body.hasAutoRenewPeriod()) {
            autoRenewPeriod = DurationConverter.fromProtobuf(body.getAutoRenewPeriod());
        }
        if (body.hasMemoWrapper()) {
            contractMemo = body.getMemoWrapper().getValue();
        }
    }

    /**
     * Build the correct transaction body.
     *
     * @return {@link com.hedera.hashgraph.sdk.proto.ContractUpdateTransactionBody builder }
     */
    ContractUpdateTransactionBody.Builder build() {
        var builder = ContractUpdateTransactionBody.newBuilder();
        if (contractId != null) {
            builder.setContractID(contractId.toProtobuf());
        }
        if (proxyAccountId != null) {
            builder.setProxyAccountID(proxyAccountId.toProtobuf());
        }
        if (expirationTime != null) {
            builder.setExpirationTime(InstantConverter.toProtobuf(expirationTime));
        }
        if (adminKey != null) {
            builder.setAdminKey(adminKey.toProtobufKey());
        }
        if (maxAutomaticTokenAssociations != null) {
            builder.setMaxAutomaticTokenAssociations(Int32Value.of(maxAutomaticTokenAssociations));
        }
        if (autoRenewPeriod != null) {
            builder.setAutoRenewPeriod(DurationConverter.toProtobuf(autoRenewPeriod));
        }
        if (contractMemo != null) {
            builder.setMemoWrapper(StringValue.of(contractMemo));
        }
        return builder;
    }

    @Override
    void validateChecksums(Client client) throws BadEntityIdException {
        if (contractId != null) {
            contractId.validateChecksum(client);
        }

        if (proxyAccountId != null) {
            proxyAccountId.validateChecksum(client);
        }
    }

    @Override
    MethodDescriptor<com.hedera.hashgraph.sdk.proto.Transaction, TransactionResponse> getMethodDescriptor() {
        return SmartContractServiceGrpc.getUpdateContractMethod();
    }

    @Override
    void onFreeze(TransactionBody.Builder bodyBuilder) {
        bodyBuilder.setContractUpdateInstance(build());
    }

    @Override
    void onScheduled(SchedulableTransactionBody.Builder scheduled) {
        scheduled.setContractUpdateInstance(build());
    }
}
