package io.neow3j.compiler;

import static io.neow3j.compiler.Compiler.mapTypeToParameterType;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.neow3j.contract.ScriptHash;
import io.neow3j.model.types.ContractParameterType;
import io.neow3j.utils.ClassUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.objectweb.asm.Type;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DebugInfo {

    @JsonProperty("hash")
    private String hash;

    @JsonProperty("documents")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<String> documents;

    @JsonProperty("methods")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<Method> methods;

    @JsonProperty("events")
    @JsonSetter(nulls = Nulls.AS_EMPTY)
    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    private List<Event> events;

    public DebugInfo() {
    }

    public DebugInfo(ScriptHash hash, List<String> documents,
            List<Method> methods, List<Event> events) {
        this.hash = hash.toString();
        this.documents = documents;
        this.methods = methods;
        this.events = events;
    }

    public String getHash() {
        return hash;
    }

    public List<String> getDocuments() {
        return documents;
    }

    public List<Method> getMethods() {
        return methods;
    }

    public List<Event> getEvents() {
        return events;
    }

    public static DebugInfo buildDebugInfo(CompilationUnit compUnit) {
        // Build method information.
        List<Method> methods = new ArrayList<>();
        List<String> documents = new ArrayList<>();
        for (NeoMethod neoMethod : compUnit.getNeoModule().getSortedMethods()) {
            String sourceFilePath = compUnit.getSourceFile(neoMethod.getOwnerClassName());
            if (sourceFilePath == null) {
                continue;
            }
            int docIdx = documents.indexOf(sourceFilePath);
            if (docIdx == -1) {
                documents.add(sourceFilePath);
                docIdx = documents.size() - 1;
            }
            String name = ClassUtils.getFullyQualifiedNameForInternalName(
                    neoMethod.getOwnerClass().name) + "," + neoMethod.getName();
            String range = (neoMethod.getStartAddress() + neoMethod.getInstructions().firstKey())
                    + "-" + (neoMethod.getStartAddress() + neoMethod.getInstructions().lastKey());
            List<String> params = collectVars(neoMethod.getParametersByNeoIndex().values());
            List<String> vars = collectVars(neoMethod.getVariablesByNeoIndex().values());
            String returnType = mapTypeToParameterType(
                    Type.getMethodType(neoMethod.getAsmMethod().desc).getReturnType()).jsonValue();
            List<String> sequencePoints = collectSequencePoints(neoMethod, docIdx);
            methods.add(new Method(neoMethod.getId(), name, range, params, returnType, vars,
                    sequencePoints));
        }

        List<Event> events = compUnit.getNeoModule().getEvents().stream()
                .map(NeoEvent::getAsDebugInfoEvent)
                .collect(Collectors.toList());

        return new DebugInfo(compUnit.getNefFile().getScriptHash(), documents, methods, events);
    }

    private static List<String> collectSequencePoints(NeoMethod neoMethod, int documentIndex) {
        List<String> sequencePoints = new ArrayList<>();
        for (NeoInstruction insn : neoMethod.getInstructions().values()) {
            if (insn.getLineNr() == null) {
                continue;
            }
            sequencePoints.add(new StringBuilder()
                    .append(neoMethod.getStartAddress() + insn.getAddress())
                    .append("[").append(documentIndex).append("]")
                    .append(insn.getLineNr())
                    // TODO: Change once it is possible to know the instruction's column number.
                    .append(":0-")
                    .append(insn.getLineNr())
                    .append(":0")
                    .toString());
        }
        return sequencePoints;
    }

    private static List<String> collectVars(Collection<NeoVariable> vars) {
        List<String> varStrings = new ArrayList<>();
        for (NeoVariable var : vars) {
            String name = var.getName();
            if (name == null) {
                continue;
            }
            String type = mapTypeToParameterType(Type.getType(var.getDescriptor())).jsonValue();
            varStrings.add(name + "," + type);
        }
        return varStrings;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Method {

        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name; // format: "{namespace},{display-name}

        @JsonProperty("range")
        private String range; // format: "{start-address}-{end-address}

        @JsonProperty("params")
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        private List<String> params; // format: "{name},{type}

        @JsonProperty("return")
        private String returnType;

        @JsonProperty("variables")
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        private List<String> variables; // format: "{name},{type}

        @JsonProperty("sequence-points")
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        // format: "{address}[{document-index}]{start-line}:{start-column}-{end-line}:{end-column}"
        private List<String> sequencePoints;

        public Method() {
        }

        public Method(String id, String name, String range, List<String> params,
                String returnType, List<String> variables, List<String> sequencePoints) {
            this.id = id;
            this.name = name;
            this.range = range;
            this.params = params;
            this.returnType = returnType;
            this.variables = variables;
            this.sequencePoints = sequencePoints;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getRange() {
            return range;
        }

        public List<String> getParams() {
            return params;
        }

        public String getReturnType() {
            return returnType;
        }

        public List<String> getVariables() {
            return variables;
        }

        public List<String> getSequencePoints() {
            return sequencePoints;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Event {

        @JsonProperty("id")
        private String id;

        @JsonProperty("name")
        private String name; // format: "{namespace},{display-name}"

        @JsonProperty("params")
        @JsonSetter(nulls = Nulls.AS_EMPTY)
        @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
        private List<String> params; // format: "{name},{type}"

        public Event() {
        }

        public Event(String id, String name, List<String> params) {
            this.id = id;
            this.name = name;
            this.params = params;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public List<String> getParams() {
            return params;
        }
    }
}
