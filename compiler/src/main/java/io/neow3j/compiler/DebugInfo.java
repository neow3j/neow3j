package io.neow3j.compiler;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import io.neow3j.contract.ScriptHash;
import java.util.List;

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
