package org.mark.maven.amp.model;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public final class ComponentDependencies
{
    private final Component comp;
    private final List<ClassDef> classDeps;
    private final Set<Component> compDeps;
    private final int numberClassDeps;

    public ComponentDependencies(Component component, List<ClassDef> directClassDeps, Set<Component> directCompDeps)
    {
        this.comp = component;
        this.classDeps = directClassDeps;
        this.numberClassDeps = classDeps.size();
        this.compDeps = directCompDeps;
    }

    /**
     * @return the number of classes in this {@link ComponentDependencies}
     */
    public int getNumClassDeps()
    {
        return numberClassDeps;
    }

    public List<ClassDef> getDependencies()
    {
        return Collections.unmodifiableList(classDeps);
    }

    public Set<Component> getCompDependencies()
    {
        return Collections.unmodifiableSet(compDeps);
    }

}
