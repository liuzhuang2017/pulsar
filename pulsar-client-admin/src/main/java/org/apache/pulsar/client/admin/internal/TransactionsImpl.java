/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pulsar.client.admin.internal;

import static com.google.common.base.Preconditions.checkArgument;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.apache.pulsar.client.admin.PulsarAdminException;
import org.apache.pulsar.client.admin.Transactions;
import org.apache.pulsar.client.api.Authentication;
import org.apache.pulsar.client.api.transaction.TxnID;
import org.apache.pulsar.common.naming.TopicName;
import org.apache.pulsar.common.policies.data.TransactionBufferStats;
import org.apache.pulsar.common.policies.data.TransactionCoordinatorInternalStats;
import org.apache.pulsar.common.policies.data.TransactionCoordinatorStats;
import org.apache.pulsar.common.policies.data.TransactionInBufferStats;
import org.apache.pulsar.common.policies.data.TransactionInPendingAckStats;
import org.apache.pulsar.common.policies.data.TransactionMetadata;
import org.apache.pulsar.common.policies.data.TransactionPendingAckInternalStats;
import org.apache.pulsar.common.policies.data.TransactionPendingAckStats;
import org.apache.pulsar.common.stats.PositionInPendingAckStats;

public class TransactionsImpl extends BaseResource implements Transactions {
    private final WebTarget adminV3Transactions;

    public TransactionsImpl(WebTarget web, Authentication auth, long readTimeoutMs) {
        super(auth, readTimeoutMs);
        adminV3Transactions = web.path("/admin/v3/transactions");
    }

    @Override
    public CompletableFuture<TransactionCoordinatorStats> getCoordinatorStatsByIdAsync(int coordinatorId) {
        WebTarget path = adminV3Transactions.path("coordinatorStats");
        path = path.queryParam("coordinatorId", coordinatorId);
        final CompletableFuture<TransactionCoordinatorStats> future = new CompletableFuture<>();
        asyncGetRequest(path,
                new InvocationCallback<TransactionCoordinatorStats>() {
                    @Override
                    public void completed(TransactionCoordinatorStats stats) {
                        future.complete(stats);
                    }

                    @Override
                    public void failed(Throwable throwable) {
                        future.completeExceptionally(getApiException(throwable.getCause()));
                    }
                });
        return future;
    }

    @Override
    public TransactionCoordinatorStats getCoordinatorStatsById(int coordinatorId) throws PulsarAdminException {
        return sync(() -> getCoordinatorStatsByIdAsync(coordinatorId));
    }

    @Override
    public CompletableFuture<Map<Integer, TransactionCoordinatorStats>> getCoordinatorStatsAsync() {
        WebTarget path = adminV3Transactions.path("coordinatorStats");
        final CompletableFuture<Map<Integer, TransactionCoordinatorStats>> future = new CompletableFuture<>();
        asyncGetRequest(path,
                new InvocationCallback<Map<Integer, TransactionCoordinatorStats>>() {
                    @Override
                    public void completed(Map<Integer, TransactionCoordinatorStats> stats) {
                        future.complete(stats);
                    }

                    @Override
                    public void failed(Throwable throwable) {
                        future.completeExceptionally(getApiException(throwable.getCause()));
                    }
                });
        return future;
    }

    @Override
    public Map<Integer, TransactionCoordinatorStats> getCoordinatorStats() throws PulsarAdminException {
        return sync(() -> getCoordinatorStatsAsync());
    }

    @Override
    public CompletableFuture<TransactionInBufferStats> getTransactionInBufferStatsAsync(TxnID txnID, String topic) {
        TopicName topicName = TopicName.get(topic);
        WebTarget path = adminV3Transactions.path("transactionInBufferStats");
        path = path.path(topicName.getRestPath(false));
        path = path.path(txnID.getMostSigBits() + "");
        path = path.path(txnID.getLeastSigBits() + "");
        final CompletableFuture<TransactionInBufferStats> future = new CompletableFuture<>();
        asyncGetRequest(path,
                new InvocationCallback<TransactionInBufferStats>() {
                    @Override
                    public void completed(TransactionInBufferStats stats) {
                        future.complete(stats);
                    }

                    @Override
                    public void failed(Throwable throwable) {
                        future.completeExceptionally(getApiException(throwable.getCause()));
                    }
                });
        return future;
    }

    @Override
    public TransactionInBufferStats getTransactionInBufferStats(TxnID txnID, String topic) throws PulsarAdminException {
        return sync(() -> getTransactionInBufferStatsAsync(txnID, topic));
    }

    @Override
    public CompletableFuture<TransactionInPendingAckStats> getTransactionInPendingAckStatsAsync(TxnID txnID,
                                                                                                String topic,
                                                                                                String subName) {
        WebTarget path = adminV3Transactions.path("transactionInPendingAckStats");
        path = path.path(TopicName.get(topic).getRestPath(false));
        path = path.path(subName);
        path = path.path(txnID.getMostSigBits() + "");
        path = path.path(txnID.getLeastSigBits() + "");
        final CompletableFuture<TransactionInPendingAckStats> future = new CompletableFuture<>();
        asyncGetRequest(path,
                new InvocationCallback<TransactionInPendingAckStats>() {
                    @Override
                    public void completed(TransactionInPendingAckStats stats) {
                        future.complete(stats);
                    }

                    @Override
                    public void failed(Throwable throwable) {
                        future.completeExceptionally(getApiException(throwable.getCause()));
                    }
                });
        return future;
    }

    @Override
    public TransactionInPendingAckStats getTransactionInPendingAckStats(TxnID txnID, String topic,
                                                                        String subName) throws PulsarAdminException {
        return sync(() -> getTransactionInPendingAckStatsAsync(txnID, topic, subName));
    }

    @Override
    public CompletableFuture<TransactionMetadata> getTransactionMetadataAsync(TxnID txnID) {
        WebTarget path = adminV3Transactions.path("transactionMetadata");
        path = path.path(txnID.getMostSigBits() + "");
        path = path.path(txnID.getLeastSigBits() + "");
        final CompletableFuture<TransactionMetadata> future = new CompletableFuture<>();
        asyncGetRequest(path,
                new InvocationCallback<TransactionMetadata>() {
                    @Override
                    public void completed(TransactionMetadata metadata) {
                        future.complete(metadata);
                    }

                    @Override
                    public void failed(Throwable throwable) {
                        future.completeExceptionally(getApiException(throwable.getCause()));
                    }
                });
        return future;
    }

    @Override
    public TransactionMetadata getTransactionMetadata(TxnID txnID) throws PulsarAdminException {
        return sync(() -> getTransactionMetadataAsync(txnID));
    }

    @Override
    public CompletableFuture<TransactionBufferStats> getTransactionBufferStatsAsync(String topic,
                                                                                    boolean lowWaterMarks) {
        WebTarget path = adminV3Transactions.path("transactionBufferStats");
        path = path.path(TopicName.get(topic).getRestPath(false));
        path = path.queryParam("lowWaterMarks", lowWaterMarks);
        final CompletableFuture<TransactionBufferStats> future = new CompletableFuture<>();
        asyncGetRequest(path,
                new InvocationCallback<TransactionBufferStats>() {
                    @Override
                    public void completed(TransactionBufferStats stats) {
                        future.complete(stats);
                    }

                    @Override
                    public void failed(Throwable throwable) {
                        future.completeExceptionally(getApiException(throwable.getCause()));
                    }
                });
        return future;
    }

    @Override
    public TransactionBufferStats getTransactionBufferStats(String topic,
                                                            boolean lowWaterMarks) throws PulsarAdminException {
        return sync(() -> getTransactionBufferStatsAsync(topic, lowWaterMarks));
    }

    @Override
    public CompletableFuture<TransactionPendingAckStats> getPendingAckStatsAsync(String topic, String subName,
                                                                                 boolean lowWaterMarks) {
        WebTarget path = adminV3Transactions.path("pendingAckStats");
        path = path.path(TopicName.get(topic).getRestPath(false));
        path = path.path(subName);
        path = path.queryParam("lowWaterMarks", lowWaterMarks);
        final CompletableFuture<TransactionPendingAckStats> future = new CompletableFuture<>();
        asyncGetRequest(path,
                new InvocationCallback<TransactionPendingAckStats>() {
                    @Override
                    public void completed(TransactionPendingAckStats stats) {
                        future.complete(stats);
                    }

                    @Override
                    public void failed(Throwable throwable) {
                        future.completeExceptionally(getApiException(throwable.getCause()));
                    }
                });
        return future;
    }

    @Override
    public TransactionPendingAckStats getPendingAckStats(String topic, String subName, boolean lowWaterMarks)
            throws PulsarAdminException {
        return sync(() -> getPendingAckStatsAsync(topic, subName, lowWaterMarks));
    }

    @Override
    public CompletableFuture<Map<String, TransactionMetadata>> getSlowTransactionsByCoordinatorIdAsync(
            Integer coordinatorId, long timeout, TimeUnit timeUnit) {
        WebTarget path = adminV3Transactions.path("slowTransactions");
        path = path.path(timeUnit.toMillis(timeout) + "");
        if (coordinatorId != null) {
            path = path.queryParam("coordinatorId", coordinatorId);
        }
        final CompletableFuture<Map<String, TransactionMetadata>> future = new CompletableFuture<>();
        asyncGetRequest(path,
                new InvocationCallback<Map<String, TransactionMetadata>>() {
                    @Override
                    public void completed(Map<String, TransactionMetadata> metadataMap) {
                        future.complete(metadataMap);
                    }

                    @Override
                    public void failed(Throwable throwable) {
                        future.completeExceptionally(getApiException(throwable.getCause()));
                    }
                });
        return future;
    }

    @Override
    public Map<String, TransactionMetadata> getSlowTransactionsByCoordinatorId(Integer coordinatorId,
                                                                               long timeout,
                                                                               TimeUnit timeUnit)
            throws PulsarAdminException {
        return sync(() -> getSlowTransactionsByCoordinatorIdAsync(coordinatorId, timeout, timeUnit));
    }

    @Override
    public CompletableFuture<Map<String, TransactionMetadata>> getSlowTransactionsAsync(long timeout,
                                                                                        TimeUnit timeUnit) {
        return getSlowTransactionsByCoordinatorIdAsync(null, timeout, timeUnit);
    }

    @Override
    public Map<String, TransactionMetadata> getSlowTransactions(long timeout,
                                                                TimeUnit timeUnit) throws PulsarAdminException {
        return sync(() -> getSlowTransactionsAsync(timeout, timeUnit));
    }

    @Override
    public CompletableFuture<TransactionCoordinatorInternalStats> getCoordinatorInternalStatsAsync(int coordinatorId,
                                                                                                   boolean metadata) {
        WebTarget path = adminV3Transactions.path("coordinatorInternalStats");
        path = path.path(coordinatorId + "");
        path = path.queryParam("metadata", metadata);
        final CompletableFuture<TransactionCoordinatorInternalStats> future = new CompletableFuture<>();
        asyncGetRequest(path,
                new InvocationCallback<TransactionCoordinatorInternalStats>() {
                    @Override
                    public void completed(TransactionCoordinatorInternalStats stats) {
                        future.complete(stats);
                    }

                    @Override
                    public void failed(Throwable throwable) {
                        future.completeExceptionally(getApiException(throwable.getCause()));
                    }
                });
        return future;
    }

    @Override
    public TransactionCoordinatorInternalStats getCoordinatorInternalStats(int coordinatorId,
                                                                           boolean metadata)
            throws PulsarAdminException {
        return sync(() -> getCoordinatorInternalStatsAsync(coordinatorId, metadata));
    }

    @Override
    public CompletableFuture<TransactionPendingAckInternalStats> getPendingAckInternalStatsAsync(String topic,
                                                                                                 String subName,
                                                                                                 boolean metadata) {
        TopicName tn = TopicName.get(topic);
        WebTarget path = adminV3Transactions.path("pendingAckInternalStats");
        path = path.path(tn.getRestPath(false));
        path = path.path(subName);
        path = path.queryParam("metadata", metadata);
        final CompletableFuture<TransactionPendingAckInternalStats> future = new CompletableFuture<>();
        asyncGetRequest(path,
                new InvocationCallback<TransactionPendingAckInternalStats>() {
                    @Override
                    public void completed(TransactionPendingAckInternalStats stats) {
                        future.complete(stats);
                    }

                    @Override
                    public void failed(Throwable throwable) {
                        future.completeExceptionally(getApiException(throwable.getCause()));
                    }
                });
        return future;
    }

    @Override
    public TransactionPendingAckInternalStats getPendingAckInternalStats(String topic,
                                                                         String subName,
                                                                         boolean metadata) throws PulsarAdminException {
        return sync(() -> getPendingAckInternalStatsAsync(topic, subName, metadata));
    }

    @Override
    public void scaleTransactionCoordinators(int replicas) throws PulsarAdminException {
         sync(() -> scaleTransactionCoordinatorsAsync(replicas));
    }

    @Override
    public CompletableFuture<Void> scaleTransactionCoordinatorsAsync(int replicas) {
        checkArgument(replicas > 0, "Number of transaction coordinators must be more than 0");
        WebTarget path = adminV3Transactions.path("transactionCoordinator");
        path = path.path("replicas");
        return asyncPostRequest(path, Entity.entity(replicas, MediaType.APPLICATION_JSON));
    }

    @Override
    public CompletableFuture<PositionInPendingAckStats> checkPositionInPendingAckStateAsync(String topic,
                                                                                            String subName,
                                                                                            Long ledgerId,
                                                                                            Long entryId,
                                                                                            Integer batchIndex) {
        TopicName tn = TopicName.get(topic);
        WebTarget path = adminV3Transactions.path("pendingAckStats");
        path = path.path(tn.getRestPath(false));
        path = path.path(subName);
        path = path.path(ledgerId.toString());
        path = path.path(entryId.toString());
        path = path.queryParam("batchIndex", batchIndex);
        final CompletableFuture<PositionInPendingAckStats> future = new CompletableFuture<>();
        asyncGetRequest(path,
                new InvocationCallback<PositionInPendingAckStats>() {
                    @Override
                    public void completed(PositionInPendingAckStats stats) {
                        future.complete(stats);
                    }

                    @Override
                    public void failed(Throwable throwable) {
                        future.completeExceptionally(getApiException(throwable.getCause()));
                    }
                });
        return future;
    }


    @Override
    public PositionInPendingAckStats checkPositionInPendingAckState(String topic, String subName, Long ledgerId,
                                                                    Long entryId, Integer batchIndex)
            throws PulsarAdminException {
        return sync(() -> checkPositionInPendingAckStateAsync(topic, subName, ledgerId, entryId, batchIndex));
    }
}
