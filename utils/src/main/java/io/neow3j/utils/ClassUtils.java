package io.neow3j.utils;

public class ClassUtils {

    public static String internalNameToFullyQualifiedName(String internalName) {
        if (internalName == null) {
            return null;
        }
        return internalName.replace("/", ".");
    }

    public static String getClassName(String fqClassName) {
        if (fqClassName == null) {
            return null;
        }
        int firstChar = fqClassName.lastIndexOf('.') + 1;
        if (firstChar > 0) {
            fqClassName = fqClassName.substring(firstChar);
        }
        if (fqClassName.length() == 0) {
            return null;
        }
        return fqClassName;
    }

}
