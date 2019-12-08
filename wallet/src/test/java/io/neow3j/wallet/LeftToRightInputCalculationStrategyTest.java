package io.neow3j.wallet;

import io.neow3j.model.types.GASAsset;
import io.neow3j.model.types.NEOAsset;
import io.neow3j.wallet.exceptions.InsufficientFundsException;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class LeftToRightInputCalculationStrategyTest {

    private LeftToRightInputCalculationStrategy strategy;

    @Before
    public void setup() {
        strategy = new LeftToRightInputCalculationStrategy();
    }

    private Utxo createNeoUtxo(int id, String value) {
        return new Utxo(NEOAsset.HASH_ID, Integer.toString(id), null, new BigDecimal(value));
    }

    private List<Utxo> createNeoUtxos(int count, String value) {
        return IntStream.range(0, count).mapToObj(i -> createNeoUtxo(i, value)).collect(Collectors.toList());
    }

    @Test
    public void test_utxos_match_required_amount_exactly() {
        BigDecimal requiredAmount = new BigDecimal(60);
        List<Utxo> initialUtxos = createNeoUtxos(3, "20");
        List<Utxo> chosenUtxos = strategy.calculateInputs(initialUtxos, requiredAmount);
        assertEquals(3, chosenUtxos.size());
        assertEquals(initialUtxos.get(0).getTxId(), chosenUtxos.get(0).getTxId());
        assertEquals(initialUtxos.get(1).getTxId(), chosenUtxos.get(1).getTxId());
        assertEquals(initialUtxos.get(2).getTxId(), chosenUtxos.get(2).getTxId());
    }

    @Test
    public void test_utxos_are_not_enough_for_required_amount() {
        BigDecimal requiredAmount = new BigDecimal(61);
        List<Utxo> initialUtxos = createNeoUtxos(3, "20");
        try {
            List<Utxo> chosenUtxos = strategy.calculateInputs(initialUtxos, requiredAmount);
        } catch (InsufficientFundsException e) {
            return;
        }
        fail("InsufficientFundsException should have been thrown, but wasn't.");
    }

    @Test
    public void test_utxos_amount_to_more_than_required_but_all_utxos_are_needed() {
        BigDecimal requiredAmount = new BigDecimal(60);
        List<Utxo> initialUtxos = createNeoUtxos(3, "22");
        List<Utxo> chosenUtxos = strategy.calculateInputs(initialUtxos, requiredAmount);
        assertEquals(3, chosenUtxos.size());
        assertEquals(initialUtxos.get(0).getTxId(), chosenUtxos.get(0).getTxId());
        assertEquals(initialUtxos.get(1).getTxId(), chosenUtxos.get(1).getTxId());
        assertEquals(initialUtxos.get(2).getTxId(), chosenUtxos.get(2).getTxId());
    }

    @Test
    public void test_more_utxos_than_required() {
        BigDecimal requiredAmount = new BigDecimal(40);
        List<Utxo> initialUtxos = createNeoUtxos(5, "20");
        List<Utxo> chosenUtxos = strategy.calculateInputs(initialUtxos, requiredAmount);
        assertEquals(2, chosenUtxos.size());
        assertEquals(initialUtxos.get(0).getTxId(), chosenUtxos.get(0).getTxId());
        assertEquals(initialUtxos.get(1).getTxId(), chosenUtxos.get(1).getTxId());

        requiredAmount = new BigDecimal(39);
        chosenUtxos = strategy.calculateInputs(initialUtxos, requiredAmount);
        assertEquals(2, chosenUtxos.size());
        assertEquals(initialUtxos.get(0).getTxId(), chosenUtxos.get(0).getTxId());
        assertEquals(initialUtxos.get(1).getTxId(), chosenUtxos.get(1).getTxId());
    }

    @Test
    public void test_many_small_utxos() {
        BigDecimal requiredAmount = new BigDecimal(40);
        List<Utxo> initialUtxos = createNeoUtxos(40000, "0.001");
        List<Utxo> chosenUtxos = strategy.calculateInputs(initialUtxos, requiredAmount);
        assertEquals(40000, chosenUtxos.size());
        assertEquals(initialUtxos.get(0).getTxId(), chosenUtxos.get(0).getTxId());
        assertEquals(initialUtxos.get(39999).getTxId(), chosenUtxos.get(39999).getTxId());

        try {
            initialUtxos = createNeoUtxos(39999, "0.001");
            chosenUtxos = strategy.calculateInputs(initialUtxos, requiredAmount);
        } catch (InsufficientFundsException e) {
            return;
        }
        fail();
    }

    @Test(expected = IllegalArgumentException.class)
    public void failWithDifferentAssetUtxos() {
        BigDecimal requiredAmount = new BigDecimal(40);
        List<Utxo> initialUtxos = createNeoUtxos(2, "0.001");
        initialUtxos.add(new Utxo(GASAsset.HASH_ID, "txId", 0, BigDecimal.ONE));
        strategy.calculateInputs(initialUtxos, requiredAmount);
    }

    @Test(expected = IllegalArgumentException.class)
    public void failWithoutUtxos() {
        strategy.calculateInputs(new ArrayList<>(), BigDecimal.ONE);
    }
}