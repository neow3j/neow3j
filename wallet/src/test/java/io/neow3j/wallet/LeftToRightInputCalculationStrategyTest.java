package io.neow3j.wallet;

import io.neow3j.wallet.exceptions.InsufficientFundsException;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
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

    private Utxo createUtxo(int id, String value) {
        return new Utxo(Integer.toString(id), null, new BigDecimal(value));
    }

    private List<Utxo> createUtxos(int count, String value) {
        return IntStream.range(0, count).mapToObj(i -> createUtxo(i, value)).collect(Collectors.toList());
    }

    @Test
    public void test_utxos_match_required_amount_exactly() {
        BigDecimal requiredAmount = new BigDecimal(60);
        List<Utxo> initialUtxos = createUtxos(3, "20");
        List<Utxo> chosenUtxos = strategy.calculateInputs(initialUtxos, requiredAmount);
        assertEquals(3, chosenUtxos.size());
        assertEquals(initialUtxos.get(0).getTxId(), chosenUtxos.get(0).getTxId());
        assertEquals(initialUtxos.get(1).getTxId(), chosenUtxos.get(1).getTxId());
        assertEquals(initialUtxos.get(2).getTxId(), chosenUtxos.get(2).getTxId());
    }

    @Test
    public void test_utxos_are_not_enough_for_required_amount() {
        BigDecimal requiredAmount = new BigDecimal(61);
        List<Utxo> initialUtxos = createUtxos(3, "20");
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
        List<Utxo> initialUtxos = createUtxos(3, "22");
        List<Utxo> chosenUtxos = strategy.calculateInputs(initialUtxos, requiredAmount);
        assertEquals(3, chosenUtxos.size());
        assertEquals(initialUtxos.get(0).getTxId(), chosenUtxos.get(0).getTxId());
        assertEquals(initialUtxos.get(1).getTxId(), chosenUtxos.get(1).getTxId());
        assertEquals(initialUtxos.get(2).getTxId(), chosenUtxos.get(2).getTxId());
    }

    @Test
    public void test_more_utxos_than_required() {
        BigDecimal requiredAmount = new BigDecimal(40);
        List<Utxo> initialUtxos = createUtxos(5, "20");
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
        List<Utxo> initialUtxos = createUtxos(40000, "0.001");
        List<Utxo> chosenUtxos = strategy.calculateInputs(initialUtxos, requiredAmount);
        assertEquals(40000, chosenUtxos.size());
        assertEquals(initialUtxos.get(0).getTxId(), chosenUtxos.get(0).getTxId());
        assertEquals(initialUtxos.get(39999).getTxId(), chosenUtxos.get(39999).getTxId());

        try {
            initialUtxos = createUtxos(39999, "0.001");
            chosenUtxos = strategy.calculateInputs(initialUtxos, requiredAmount);
        } catch(InsufficientFundsException e) {
            return;
        }
        fail();
    }
}