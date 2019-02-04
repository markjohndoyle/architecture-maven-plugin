package org.mark.maven.amp.testsupport;

import java.util.ArrayList;
import java.util.List;

import org.mark.maven.amp.model.ClassDef;
import org.mark.maven.amp.model.ModuleSource;

public final class ModuleSourceBuilder
{
    private String name = "aSourceModule";
    private List<ClassDef> allClassDefs = new ArrayList<>();

    public ModuleSourceBuilder setName(String newSourceName)
    {
        this.name = newSourceName;
        return this;
    }

    public ModuleSourceBuilder withClassDef(ClassDef classDef)
    {
        this.allClassDefs.add(classDef);
        return this;
    }

    public ModuleSourceBuilder withClassDef(ClassDefBuilder classDef)
    {
        this.allClassDefs.add(classDef.build());
        return this;
    }

    public ModuleSource build()
    {
        return new ModuleSource(name, allClassDefs );
    }

    public static ModuleSourceBuilder aSourceModule()
    {
        return new ModuleSourceBuilder();
    }
}
