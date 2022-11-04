package io.neow3j.contract.types;

import io.neow3j.contract.exceptions.InvalidNeoNameException;
import io.neow3j.contract.exceptions.InvalidNeoNameServiceRootException;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Represents a NeoNameService domain name.
 */
public class NNSName {

    private final String name;

    /**
     * Creates a NNS name and checks its validity.
     *
     * @param name the domain name.
     * @throws InvalidNeoNameException if the format of the provided name is invalid.
     */
    public NNSName(String name) {
        if (!isValidNNSName(name, true)) {
            throw new InvalidNeoNameException(name);
        }
        this.name = name;
    }

    /**
     * @return the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @return the UTF-8 encoded name.
     */
    public byte[] getBytes() {
        return name.getBytes(UTF_8);
    }

    /**
     * @return if the name is a second-level domain name or not.
     */
    public boolean isSecondLevelDomain() {
        return isValidNNSName(name, false);
    }

    @Override
    public String toString() {
        return "NNSName{" + this.name + '}';
    }

    // region validity checks
    // Note: The following checks are based on the official NNS smart contract.

    protected static boolean isValidNNSName(String name, boolean allowMultipleFragments) {
        int length = name.length();
        if (length < 3 || length > 255) {
            return false;
        }
        String[] fragments = name.split("\\.");
        length = fragments.length;
        if (length < 2 || length > 8) {
            return false;
        }
        if (length > 2 && !allowMultipleFragments) {
            return false;
        }
        for (int i = 0; i < length; i++) {
            if (!checkFragment(fragments[i], i == length - 1)) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkFragment(String fragment, boolean isRoot) {
        int maxLength = isRoot ? 16 : 63;
        if (fragment.length() == 0 || fragment.length() > maxLength) {
            return false;
        }
        char c = fragment.charAt(0);
        if (isRoot) {
            if (!isAlpha(c)) {
                return false;
            }
        } else {
            if (!isAlphaNum(c)) {
                return false;
            }
        }
        if (fragment.length() == 1) {
            return true;
        }
        for (int i = 1; i < fragment.length() - 1; i++) {
            c = fragment.charAt(i);
            if (!(isAlphaNum(c) || c == '-')) {
                return false;
            }
        }
        c = fragment.charAt(fragment.length() - 1);
        return isAlphaNum(c);
    }

    private static boolean isAlpha(char c) {
        return c >= 'a' && c <= 'z';
    }

    private static boolean isAlphaNum(char c) {
        return isAlpha(c) || c >= '0' && c <= '9';
    }

    // endregion

    /**
     * Represents a NeoNameService root.
     */
    public static class NNSRoot {

        private final String root;

        /**
         * Creates a NNS root and checks its validity.
         *
         * @param root the root.
         * @throws InvalidNeoNameServiceRootException if the format of the provided root is invalid.
         */
        public NNSRoot(String root) {
            if (!isValidNNSRoot(root)) {
                throw new InvalidNeoNameServiceRootException(root);
            }
            this.root = root;
        }

        /**
         * @return the root.
         */
        public String getRoot() {
            return root;
        }

        private static boolean isValidNNSRoot(String root) {
            return checkFragment(root, true);
        }

        @Override
        public String toString() {
            return "NNSRoot{" + this.root + '}';
        }

    }

}
