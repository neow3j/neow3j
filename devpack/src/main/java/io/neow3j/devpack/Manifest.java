package io.neow3j.devpack;

import io.neow3j.devpack.annotations.Instruction;
import io.neow3j.devpack.constants.ParameterType;
import io.neow3j.script.OpCode;

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
     * The permissions that the contract has, i.e., which contracts and methods it is allowed to call.
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

    /**
     * Compares this manifest to the given object. The comparison happens by reference only. I.e., if you retrieve
     * the same manifest twice, e.g., from
     * {@link io.neow3j.devpack.contracts.ContractManagement#getContract(Hash160)}, then comparing the two will
     * return false.
     *
     * @param other the object to compare with.
     * @return true if this and {@code other} reference the same manifest. False otherwise.
     */
    @Override
    @Instruction(opcode = OpCode.EQUAL)
    public native boolean equals(Object other);

    /**
     * Compares this and the given manifest by value.
     *
     * @param m another manifest to compare this manifest to.
     * @return true if all fields of the two manifests are equal. False, otherwise.
     */
    public boolean equals(Manifest m) {
        if (this == m) {
            return true;
        }
        if (this.name != m.name || this.extra != m.extra || !this.abi.equals(m.abi)) {
            return false;
        }

        if (this.groups != m.groups) { // equal by reference?
            if (this.groups.size() != m.groups.size()) {
                return false;
            }
            for (int i = 0; i < this.groups.size(); i++) {
                if (!this.groups.get(i).equals(m.groups.get(i))) {
                    return false;
                }
            }
        }

        if (this.features != m.features) { // equal by reference?
            String[] thisKeys = this.features.keys();
            String[] otherKeys = m.features.keys();
            if (thisKeys.length != otherKeys.length) {
                return false;
            }
            String[] thisValues = this.features.values();
            String[] otherValues = m.features.values();
            for (int i = 0; i < thisKeys.length; i++) {
                if (thisKeys[i] != otherKeys[i] || thisValues[i] != otherValues[i]) {
                    return false;
                }
            }
        }

        if (this.supportedStandards != m.supportedStandards) { // equal by reference?
            if (this.supportedStandards.size() != m.supportedStandards.size()) {
                return false;
            }
            for (int i = 0; i < this.supportedStandards.size(); i++) {
                if (this.supportedStandards.get(i) != m.supportedStandards.get(i)) {
                    return false;
                }
            }
        }

        if (this.permissions != m.permissions) { // equal by reference?
            if (this.permissions.size() != m.permissions.size()) {
                return false;
            }
            for (int i = 0; i < this.permissions.size(); i++) {
                if (!this.permissions.get(i).equals(m.permissions.get(i))) {
                    return false;
                }
            }
        }

        if (this.trusts != m.trusts) { // equal by reference?
            if (this.trusts.size() != m.trusts.size()) {
                return false;
            }
            for (int i = 0; i < this.trusts.size(); i++) {
                if (this.trusts.get(i) != m.trusts.get(i)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static class Group {

        /**
         * The public key of this group.
         */
        public ECPoint pubKey;

        /**
         * The signature created from the contract's hash with the private key corresponding to this group's public key.
         */
        public ByteString signature;

        private Group() {
            this.pubKey = null;
            this.signature = null;
        }

        public boolean equals(Group g) {
            if (this == g) {
                return true;
            }
            return this.pubKey == g.pubKey
                    && this.signature == g.signature;
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

        public boolean equals(ABI abi) {
            if (this == abi) {
                return true;
            }
            if (this.methods != abi.methods) { // equal by reference?
                if (this.methods.size() != abi.methods.size()) {
                    return false;
                }
                for (int i = 0; i < this.methods.size(); i++) {
                    if (!this.methods.get(i).equals(abi.methods.get(i))) {
                        return false;
                    }
                }
            }

            if (this.events != abi.events) { // equal by reference?
                if (this.events.size() != abi.events.size()) {
                    return false;
                }
                for (int i = 0; i < this.events.size(); i++) {
                    if (!this.events.get(i).equals(abi.events.get(i))) {
                        return false;
                    }
                }
            }
            return true;
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

            public boolean equals(Method m) {
                if (this == m) {
                    return true;
                }
                if (this.name != m.name ||
                        !this.returnType.equals(m.returnType) ||
                        this.offset != m.offset ||
                        this.safe != m.safe) {
                    return false;
                }
                if (this.parameters != m.parameters) { // equal by reference?
                    if (this.parameters.size() != m.parameters.size()) {
                        return false;
                    }
                    for (int i = 0; i < this.parameters.size(); i++) {
                        if (!this.parameters.get(i).equals(m.parameters.get(i))) {
                            return false;
                        }
                    }
                }
                return true;
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

            public boolean equals(Event e) {
                if (this == e) {
                    return true;
                }
                if (this.name != e.name) {
                    return false;
                }
                if (this.parameters != e.parameters) { // equal by reference?
                    if (this.parameters.size() != e.parameters.size()) {
                        return false;
                    }
                    for (int i = 0; i < this.parameters.size(); i++) {
                        if (!this.parameters.get(i).equals(e.parameters.get(i))) {
                            return false;
                        }
                    }
                }
                return true;
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

            public boolean equals(Parameter p) {
                if (this == p) {
                    return true;
                }
                return this.name == p.name &&
                        this.type == p.type;
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

        public boolean equals(Permission p) {
            if (this == p) {
                return true;
            }
            if (this.contract != p.contract) {
                return false;
            }
            if (this.methods != p.methods) { // equal by reference?
                if (this.methods.size() != p.methods.size()) {
                    return false;
                }
                for (int i = 0; i < this.methods.size(); i++) {
                    if (this.methods.get(i) != p.methods.get(i)) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

}

