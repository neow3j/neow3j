package io.neow3j.devpack;

import io.neow3j.devpack.constants.ParameterType;

/**
 * A contract's manifest.
 */
public class Manifest {

    /**
     * The contract name.
     */
    public String name;

    /**
     * The groups to which the contract belongs and to which it has mutual trust.
     */
    public List<Group> groups;

    /**
     * Features that the contract has.
     */
    public Map<String, String> features;

    /**
     * The standards that the contract supports, e.g., NEP-17.
     */
    public List<String> supportedStandards;

    /**
     * The contract's ABI, i.e., the definition of its methods and events.
     */
    public ABI abi;

    /**
     * The permissions that the contract has, i.e., which contracts and methods it is allowed to
     * call.
     */
    public List<Permission> permissions;

    /**
     * The contracts and groups that are trusted, i.e., can call this contract without warnings.
     */
    public List<Hash160> trusts;

    /**
     * Extra information.
     */
    public Object extra;

    private Manifest() {
        this.name = null;
        this.groups = null;
        this.features = null;
        this.supportedStandards = null;
        this.abi = null;
        this.permissions = null;
        this.trusts = null;
        this.extra = null;
    }

    public static class Group {

        /**
         * The public key of this group.
         */
        public ECPoint pubKey;

        /**
         * The signature created from the contract's hash with the private key corresponding to
         * this group's public key.
         */
        public ByteString signature;

        private Group() {
            this.pubKey = null;
            this.signature = null;
        }

    }

    public static class ABI {

        /**
         * The methods in this contract ABI.
         */
        public List<Method> methods;

        /**
         * The events in this contract ABI.
         */
        public List<Event> events;

        private ABI() {
            this.methods = null;
            this.events = null;
        }

        public static class Method {

            /**
             * The name of this method.
             */
            public String name;

            /**
             * The parameters that this method takes.
             */
            public List<Parameter> parameters;

            /**
             * The return type of this method.
             */
            public Parameter returnType;

            /**
             * The offset in the contract's script, i.e., number of bytes to skip till this method.
             */
            public int offset;

            /**
             * If the method is safe to call, i.e., if it changes state or not.
             */
            public boolean safe;

            private Method() {
                this.name = null;
                this.parameters = null;
                this.offset = 0;
                this.returnType = null;
                this.safe = false;
            }

        }

        public static class Event {

            /**
             * The name of the event.
             */
            public String name;

            /**
             * The list of parameters that can be past with the event.
             */
            public List<Parameter> parameters;

            private Event() {
                this.name = null;
                this.parameters = null;
            }

        }

        public static class Parameter {

            /**
             * The name of the parameter.
             */
            public String name;

            /**
             * The parameter type
             * <p>
             * One of the values given in {@link ParameterType}.
             */
            public byte type;

            private Parameter() {
                this.name = null;
                this.type = 0;
            }
        }
    }

    public static class Permission {

        public Hash160 contract;

        public List<String> methods;

        public Permission() {
            this.contract = null;
            this.methods = null;
        }
    }
}

