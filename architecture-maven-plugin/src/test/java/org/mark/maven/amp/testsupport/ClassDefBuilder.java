package org.mark.maven.amp.testsupport;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.mark.maven.amp.model.ClassDef;

public final class ClassDefBuilder
{
    private String name;
    private Set<String> imports = new HashSet<>();
    private boolean isAbstract;


    public ClassDefBuilder(String className)
    {
        name = className;
    }

    public ClassDefBuilder withImport(String fullClassName)
    {
        imports.add(fullClassName);
        return this;
    }

    public ClassDefBuilder withImport(String ... fullQualNames)
    {
        imports.addAll(Arrays.asList(fullQualNames));
        return this;
    }

    public ClassDefBuilder setAbstract(boolean isAbstract)
    {
        this.isAbstract = isAbstract;
        return this;
    }

    public ClassDef build()
    {
        return new ClassDef(name, imports, isAbstract);
    }

    public static ClassDefBuilder aClassDef(String name)
    {
        return new ClassDefBuilder(name);
    }

}
