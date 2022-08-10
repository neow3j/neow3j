package io.neow3j.wallet.nep6;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.neow3j.crypto.NEP2;
import io.neow3j.crypto.ScryptParams;
import io.neow3j.types.ContractParameterType;
import io.neow3j.wallet.Wallet;
import io.neow3j.wallet.nep6.NEP6Contract.NEP6Parameter;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NEP6WalletTest {

    @Test
    public void testReadWallet() throws IOException {
        URL nep6WalletFile = Thread.currentThread().getContextClassLoader().getResource("wallet/wallet.json");
        ObjectMapper mapper = new ObjectMapper();
        NEP6Wallet w = mapper.readValue(nep6WalletFile, NEP6Wallet.class);
        assertEquals(w.getName(), "Wallet");
        assertEquals(w.getVersion(), Wallet.CURRENT_VERSION);
        assertEquals(w.getScrypt(), new ScryptParams(NEP2.N_STANDARD, NEP2.P_STANDARD, NEP2.R_STANDARD));
        assertEquals(w.getAccounts().size(), 2);

        // Account 1
        NEP6Account a1 = w.getAccounts().get(0);
        assertEquals(a1.getAddress(), "NLnyLtep7jwyq1qhNPkwXbJpurC4jUT8ke");
        assertEquals(a1.getLabel(), "Account1");
        assertTrue(a1.getDefault());
        assertFalse(a1.getLock());
        assertEquals(a1.getKey(), "6PYVEi6ZGdsLoCYbbGWqoYef7VWMbKwcew86m5fpxnZRUD8tEjainBgQW1");
        assertNull(a1.getExtra());
        NEP6Contract c1 = a1.getContract();
        assertEquals(c1.getScript(), "DCECJJQloGtaH45hM/x5r6LCuEML+TJyl/F2dh33no2JKcULQZVEDXg=");
        assertFalse(c1.getDeployed());
        NEP6Parameter p1 = c1.getParameters().get(0);
        assertEquals(p1.getParamName(), "signature");
        assertEquals(p1.getParamType(), ContractParameterType.SIGNATURE);

        // Account 2
        NEP6Account a2 = w.getAccounts().get(1);
        assertEquals(a2.getAddress(), "NWcx4EfYdfqn5jNjDz8AHE6hWtWdUGDdmy");
        assertEquals(a2.getLabel(), "Account2");
        assertFalse(a2.getDefault());
        assertFalse(a2.getLock());
        assertEquals(a2.getKey(), "6PYSQWBqZE5oEFdMGCJ3xR7bz6ezz814oKE7GqwB9i5uhtUzkshe9B6YGB");
        assertNull(a1.getExtra());
        NEP6Contract c2 = a2.getContract();
        assertEquals(c2.getScript(), "DCEDHMqqRt98SU9EJpjIwXwJMR42FcLcBCy9Ov6rpg+kB0ALQZVEDXg=");
        assertFalse(c2.getDeployed());
        NEP6Parameter p2 = c2.getParameters().get(0);
        assertEquals(p2.getParamName(), "signature");
        assertEquals(p2.getParamType(), ContractParameterType.SIGNATURE);
    }

}
