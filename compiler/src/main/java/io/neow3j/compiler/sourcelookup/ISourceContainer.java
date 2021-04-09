package io.neow3j.compiler.sourcelookup;

import java.io.File;
import java.util.List;

public interface ISourceContainer {

    List<File> findSourceElements(String name);

}
