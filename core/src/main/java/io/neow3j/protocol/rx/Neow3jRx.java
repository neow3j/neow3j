package io.neow3j.protocol.rx;

import io.neow3j.protocol.core.response.NeoBlock;
import io.neow3j.protocol.core.response.NeoGetBlock;
import io.neow3j.protocol.core.response.Transaction;
import io.reactivex.Observable;

import java.io.IOException;
import java.math.BigInteger;

/**
 * The Observables JSON-RPC client event API.
 */
public interface Neow3jRx {

    /**
     * Create an Observable that emits newly created blocks on the blockchain.
     *
     * @param fullTransactionObjects if true, provides transactions embedded in blocks, otherwise transaction hashes.
     * @return an Observable that emits all new blocks as they are added to the blockchain.
     */
    Observable<NeoGetBlock> blockObservable(boolean fullTransactionObjects);

    /**
     * Create an Observable that emits all blocks from the blockchain contained within the requested range.
     *
     * @param startBlock             the block number to commence with.
     * @param endBlock               the block number to finish with.
     * @param fullTransactionObjects if true, provides transactions embedded in blocks, otherwise transaction hashes.
     * @return an Observable to emit these blocks.
     */
    Observable<NeoGetBlock> replayBlocksObservable(BigInteger startBlock, BigInteger endBlock,
            boolean fullTransactionObjects);

    /**
     * Create an Observable that emits all blocks from the blockchain contained within the requested range.
     *
     * @param startBlock             the block number to commence with.
     * @param endBlock               the block number to finish with.
     * @param fullTransactionObjects if true, provides transactions embedded in blocks, otherwise transaction hashes.
     * @param ascending              if true, emits blocks in ascending order between range, otherwise, in descending
     *                               order.
     * @return an Observable to emit these blocks.
     */
    Observable<NeoGetBlock> replayBlocksObservable(BigInteger startBlock, BigInteger endBlock,
            boolean fullTransactionObjects, boolean ascending);

    /**
     * Create an Observable that emits all transactions from the blockchain starting with a provided block number.
     * Once it has replayed up to the most current block, the provided Observable is invoked.
     * <p>
     * To automatically subscribe to new blocks, use
     * {@link #catchUpToLatestAndSubscribeToNewBlocksObservable(BigInteger, boolean)}.
     *
     * @param startBlock             the block number we wish to request from.
     * @param fullTransactionObjects if full {@link Transaction} objects should be provided in the {@link NeoBlock}
     *                               responses.
     * @param onCompleteObservable   a subsequent Observable that should be run once the latest block was caught up
     *                               with.
     * @return an Observable to emit all requested blocks.
     */
    Observable<NeoGetBlock> catchUpToLatestBlockObservable(BigInteger startBlock, boolean fullTransactionObjects,
            Observable<NeoGetBlock> onCompleteObservable);

    /**
     * Creates an Observable that emits all blocks from the requested block number to the most current. Once it has
     * emitted the most current block, onComplete is called.
     *
     * @param startBlock             the block number we wish to request from.
     * @param fullTransactionObjects if full {@link Transaction} objects should be provided in the {@link NeoBlock}
     *                               responses.
     * @return an Observable to emit all requested blocks.
     */
    Observable<NeoGetBlock> catchUpToLatestBlockObservable(BigInteger startBlock, boolean fullTransactionObjects);

    /**
     * Creates an Observable that emits all blocks from the requested block number to the most current. Once it has
     * emitted the most current block, it starts emitting new blocks as they are created.
     *
     * @param startBlock             the block number we wish to request from.
     * @param fullTransactionObjects if full {@link Transaction} objects should be provided in the {@link NeoBlock}
     *                               responses.
     * @return an Observable to emit all requested blocks and future.
     */
    Observable<NeoGetBlock> catchUpToLatestAndSubscribeToNewBlocksObservable(BigInteger startBlock,
            boolean fullTransactionObjects);


    /**
     * Creates an Observable that emits new blocks as they are created on the blockchain (starting from the latest
     * block).
     *
     * @param fullTransactionObjects if full {@link Transaction} objects should be provided in the {@link NeoBlock}
     *                               responses
     * @return an Observable to emit all requested blocks and future.
     * @throws IOException if the latest block number cannot be fetched.
     */
    Observable<NeoGetBlock> subscribeToNewBlocksObservable(boolean fullTransactionObjects) throws IOException;

}
