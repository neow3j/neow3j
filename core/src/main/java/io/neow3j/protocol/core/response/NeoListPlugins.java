package io.neow3j.protocol.core.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.neow3j.protocol.core.Response;
import io.neow3j.protocol.exceptions.RpcResponseErrorException;

import java.util.List;
import java.util.Objects;

public class NeoListPlugins extends Response<List<NeoListPlugins.Plugin>> {

    /**
     * @return the result.
     * @throws RpcResponseErrorException if the Neo node returned an error.
     */
    public List<Plugin> getPlugins() {
        return getResult();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Plugin {

        @JsonProperty("name")
        private String name;

        @JsonProperty("version")
        private String version;

        @JsonProperty("interfaces")
        private List<String> interfaces;

        public Plugin() {
        }

        public Plugin(String name, String version, List<String> interfaces) {
            this.name = name;
            this.version = version;
            this.interfaces = interfaces;
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }

        public List<String> getInterfaces() {
            return interfaces;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Plugin plugin = (Plugin) o;
            return Objects.equals(name, plugin.name) &&
                    Objects.equals(version, plugin.version) &&
                    Objects.equals(interfaces, plugin.interfaces);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, version, interfaces);
        }

        @Override
        public String toString() {
            return "Plugin{" +
                    "name='" + name + '\'' +
                    ", version='" + version + '\'' +
                    ", interfaces=" + interfaces +
                    '}';
        }

    }

}
