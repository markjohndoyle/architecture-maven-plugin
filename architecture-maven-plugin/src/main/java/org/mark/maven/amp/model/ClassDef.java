package org.mark.maven.amp.model;

import java.util.Set;

public final class ClassDef
{
    private final String name;
    private final Set<String> imports;
    private final boolean abstractOrInterface;

    public ClassDef(String fullName, Set<String> imports, boolean isAbstract)
    {
        this.name = fullName;
        this.imports = imports;
        this.abstractOrInterface = isAbstract;
    }

    public boolean imports(String fullClassName)
    {
        return imports.stream().anyMatch(i -> i.equals(fullClassName));
    }

    public String getFullQualName()
    {
        return name;
    }

    public boolean isAbstract()
    {
        return abstractOrInterface;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("ClassDef [fullName=").append(name).append(", imports=").append(imports).append("]");
        return builder.toString();
    }
}
