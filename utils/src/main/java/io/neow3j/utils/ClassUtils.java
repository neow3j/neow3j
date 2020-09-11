package io.neow3j.utils;

public class ClassUtils {

    public static String getFullyQualifiedNameForInternalName(String internalName) {
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

    /**
     * Gets the class name for the given internal name.
     * <p>
     * An internal name is similar to a fully qualified name but the '.' are replaced by '/'. The
     * returned name is only the last part of the fully qualified name. E.g. {@code
     * io.neow3j.core.Transaction} becomes {@code Transaction}.
     *
     * @param internalName The internal name to get the simple class name for.
     * @return the class name.
     */
    public static String getClassNameForInternalName(String internalName) {
        return getClassName(getFullyQualifiedNameForInternalName(internalName));
    }

}
