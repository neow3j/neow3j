package io.neow3j.protocol.rx;

import io.neow3j.protocol.core.methods.response.NeoBlock;
import io.neow3j.protocol.core.methods.response.NeoGetBlock;
import io.neow3j.protocol.core.methods.response.Transaction;
import io.reactivex.Observable;

import java.math.BigInteger;

/**
 * The Observables JSON-RPC client event API.
 */
public interface Neow3jRx {

    /**
     * Create an Observable that emits newly created blocks on the blockchain.
     *
     * @param fullTransactionObjects if true, provides transactions embedded in blocks, otherwise
     *                               transaction hashes
     * @return Observable that emits all new blocks as they are added to the blockchain
     */
    Observable<NeoGetBlock> blockObservable(boolean fullTransactionObjects);

    /**
     * Create an Observable that emits all blocks from the blockchain contained within the
     * requested range.
     *
     * @param startBlock             block number to commence with
     * @param endBlock               block number to finish with
     * @param fullTransactionObjects if true, provides transactions embedded in blocks, otherwise
     *                               transaction hashes
     * @return Observable to emit these blocks
     */
    Observable<NeoGetBlock> replayBlocksObservable(
            BigInteger startBlock, BigInteger endBlock,
            boolean fullTransactionObjects);

    /**
     * Create an Observable that emits all blocks from the blockchain contained within the
     * requested range.
     *
     * @param startBlock             block number to commence with
     * @param endBlock               block number to finish with
     * @param fullTransactionObjects if true, provides transactions embedded in blocks, otherwise
     *                               transaction hashes
     * @param ascending              if true, emits blocks in ascending order between range, otherwise
     *                               in descending order
     * @return Observable to emit these blocks
     */
    Observable<NeoGetBlock> replayBlocksObservable(
            BigInteger startBlock, BigInteger endBlock,
            boolean fullTransactionObjects, boolean ascending);

    /**
     * <p>Create an Observable that emits all transactions from the blockchain starting with a
     * provided block number. Once it has replayed up to the most current block, the provided
     * Observable is invoked.</p>
     * <br>
     * <p>To automatically subscribe to new blocks, use
     * {@link #catchUpToLatestAndSubscribeToNewBlocksObservable(BlockParameter, boolean)}.</p>
     *
     * @param startBlock             the block number we wish to request from
     * @param fullTransactionObjects if we require full {@link Transaction} objects to be provided
     *                               in the {@link NeoBlock} responses
     * @param onCompleteObservable   a subsequent Observable that we wish to run once we are caught
     *                               up with the latest block
     * @return Observable to emit all requested blocks
     */
    Observable<NeoGetBlock> catchUpToLatestBlockObservable(
            BigInteger startBlock, boolean fullTransactionObjects,
            Observable<NeoGetBlock> onCompleteObservable);

    /**
     * Creates an Observable that emits all blocks from the requested block number to the most
     * current. Once it has emitted the most current block, onComplete is called.
     *
     * @param startBlock             the block number we wish to request from
     * @param fullTransactionObjects if we require full {@link Transaction} objects to be provided
     *                               in the {@link NeoBlock} responses
     * @return Observable to emit all requested blocks
     */
    Observable<NeoGetBlock> catchUpToLatestBlockObservable(
            BigInteger startBlock, boolean fullTransactionObjects);

    /**
     * Creates an Observable that emits all blocks from the requested block number to the most
     * current. Once it has emitted the most current block, it starts emitting new blocks as they
     * are created.
     *
     * @param startBlock             the block number we wish to request from
     * @param fullTransactionObjects if we require full {@link Transaction} objects to be provided
     *                               in the {@link NeoBlock} responses
     * @return Observable to emit all requested blocks and future
     */
    Observable<NeoGetBlock> catchUpToLatestAndSubscribeToNewBlocksObservable(
            BigInteger startBlock, boolean fullTransactionObjects);

}
