package io.neow3j.wallet.nep6;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.neow3j.crypto.NEP2;
import io.neow3j.crypto.ScryptParams;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.wallet.Wallet;
import io.neow3j.wallet.nep6.NEP6Contract.NEP6Parameter;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

public class NEP6WalletTest {

    @Test
    public void testReadWallet() throws IOException {
        URL nep6WalletFile = Thread.currentThread().getContextClassLoader().getResource("wallet.json");
        ObjectMapper mapper = new ObjectMapper();
        NEP6Wallet w = mapper.readValue(nep6WalletFile, NEP6Wallet.class);
        assertEquals(w.getName(), "Wallet");
        assertEquals(w.getVersion(), Wallet.CURRENT_VERSION);
        assertEquals(w.getScrypt(), new ScryptParams(NEP2.N_STANDARD, NEP2.P_STANDARD, NEP2.R_STANDARD));
        assertEquals(w.getAccounts().size(), 2);

        // Account 1
        NEP6Account a1 = w.getAccounts().get(0);
        assertEquals(a1.getAddress(), "AWUfbdLYUeJ5X6gvbPQYkjL4JZ78z2X9Pk");
        assertEquals(a1.getLabel(), "Account1");
        assertFalse(a1.getDefault());
        assertFalse(a1.getLock());
        assertEquals(a1.getKey(), "6PYUnzmokRh7JwfYntrMq6LYw4pF4QJ343fJHMKoKDvCqNgfV6msFGGcEH");
        assertNotNull(a1.getExtra());
        NEP6Contract c1 = a1.getContract();
        assertEquals(c1.getScript(), "210302a6afbfd72400a72bc2af86c45f269f7f694036dae6270d570a1a06531079d9ac");
        assertFalse(c1.getDeployed());
        NEP6Parameter p1 = c1.getParameters().get(0);
        assertEquals(p1.getParamName(), "signature");
        assertEquals(p1.getParamType(), ContractParameterType.SIGNATURE);

        // Account 2
        NEP6Account a2 = w.getAccounts().get(1);
        assertEquals(a2.getAddress(), "AThCriBXLBQxyPNYHUwa8NVoKYM5JwL1Yg");
        assertEquals(a2.getLabel(), "Account2");
        assertFalse(a2.getDefault());
        assertFalse(a2.getLock());
        assertEquals(a2.getKey(), "6PYRUJuaSqrvkQVdfn9MBdzJDNDwXMdHNNiNAMYJhGk7MUgdiU4KshyuGX");
        assertNotNull(a1.getExtra());
        NEP6Contract c2 = a2.getContract();
        assertEquals(c2.getScript(), "21021bc6c4e94c7ce751ccfd1f86139ed6a01ca85d4b279aa01d77c9afe85a3666dfac");
        assertFalse(c2.getDeployed());
        NEP6Parameter p2 = c2.getParameters().get(0);
        assertEquals(p2.getParamName(), "signature");
        assertEquals(p2.getParamType(), ContractParameterType.SIGNATURE);
    }
}
