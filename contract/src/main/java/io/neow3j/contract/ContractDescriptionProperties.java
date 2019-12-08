package io.neow3j.contract;

import io.neow3j.io.BinaryReader;
import io.neow3j.io.BinaryWriter;
import io.neow3j.io.NeoSerializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Objects;

public class ContractDescriptionProperties extends NeoSerializable {

    private static final Logger LOG = LoggerFactory.getLogger(ContractDescriptionProperties.class);

    private String name;

    private String version;

    private String author;

    private String email;

    private String description;

    public ContractDescriptionProperties() {
    }

    public ContractDescriptionProperties(String name, String version, String author, String email, String description) {
        this.name = name;
        this.version = version;
        this.author = author;
        this.email = email;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getAuthor() {
        return author;
    }

    public String getEmail() {
        return email;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ContractDescriptionProperties)) return false;
        ContractDescriptionProperties that = (ContractDescriptionProperties) o;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getVersion(), that.getVersion()) &&
                Objects.equals(getAuthor(), that.getAuthor()) &&
                Objects.equals(getEmail(), that.getEmail()) &&
                Objects.equals(getDescription(), that.getDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getVersion(), getAuthor(), getEmail(), getDescription());
    }

    @Override
    public String toString() {
        return "ContractDescriptionProperties{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", author='" + author + '\'' +
                ", email='" + email + '\'' +
                ", description='" + description + '\'' +
                '}';
    }

    @Override
    public void deserialize(BinaryReader reader) throws IOException {
        this.description = reader.readPushString();
        this.email = reader.readPushString();
        this.author = reader.readPushString();
        this.version = reader.readPushString();
        this.name = reader.readPushString();
    }

    @Override
    public void serialize(BinaryWriter writer) throws IOException {
        writer.write(new ScriptBuilder()
                .pushData(this.description)
                .pushData(this.email)
                .pushData(this.author)
                .pushData(this.version)
                .pushData(this.name)
                .toArray());
    }
}
