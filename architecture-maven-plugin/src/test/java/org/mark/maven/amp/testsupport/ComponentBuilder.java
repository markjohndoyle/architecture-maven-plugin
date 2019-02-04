package org.mark.maven.amp.testsupport;

import org.mark.maven.amp.model.Component;
import org.mark.maven.amp.model.ModuleSource;

import static org.mark.maven.amp.testsupport.ModuleSourceBuilder.aSourceModule;

public class ComponentBuilder
{
    private String name = "aComponent";
    private ModuleSource module = aSourceModule().build();

    public ComponentBuilder setName(String componentName)
    {
        this.name = componentName;
        return this;
    }

    public ComponentBuilder setSourceModule(ModuleSource sourceModule)
    {
        this.module = sourceModule;
        return this;
    }

    public ComponentBuilder setSourceModule(ModuleSourceBuilder sourceModule)
    {
        this.module = sourceModule.build();
        return this;
    }

    public Component build()
    {
        return new Component(name, module);
    }

    public static ComponentBuilder aComponent()
    {
        return new ComponentBuilder();
    }

}
