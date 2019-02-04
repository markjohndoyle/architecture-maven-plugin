package org.mark.maven.amp.model;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Container of a modules source files represented as {@link ClassDef} instances.
 *
 * @author markjohndoyle@googlemail.com
 *
 */
public final class ModuleSource implements Iterable<ClassDef>
{
    private final String name;
    private final List<ClassDef> classes;

    public ModuleSource(String sourceModuleName, List<ClassDef> allClasses)
    {
        name = sourceModuleName;
        classes = allClasses;
    }

    public String getName()
    {
        return name;
    }

    public int getNumberOfClasses()
    {
        return classes.size();
    }

    public boolean containsClass(String fullClassName)
    {
        return classes.stream().anyMatch(classDef -> classDef.getFullQualName().equals(fullClassName));
    }

    /**
     * @param fullClassName the fully qualified class name to check
     * @return whether any class in this {@link ModuleSource} imports the given class.
     */
    public boolean anyImports(String fullClassName)
    {
        return classes.stream().anyMatch(cd -> cd.imports(fullClassName));
    }

    public long numClassesThatImport(String fullClassName)
    {
        return classes.stream().filter(cd -> cd.imports(fullClassName)).count();
    }

    public Set<ClassDef> classesThatImport(String fullClassName)
    {
        return classes.stream().filter(cd -> cd.imports(fullClassName)).collect(Collectors.toSet());
    }

    public long getNumberOfAbstractClasses()
    {
        return classes.stream().filter(ClassDef::isAbstract).count();
    }

    @Override
    public Iterator<ClassDef> iterator()
    {
        return classes.iterator();
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Component [name=");
        builder.append(name);
        builder.append(", classes=");
        builder.append(classes);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name, classes);
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if (!(other instanceof ModuleSource))
        {
            return false;
        }
        ModuleSource srcModule = (ModuleSource) other;
        return Objects.equals(name, srcModule.name) &&
                        Objects.equals(classes, srcModule.classes);
    }

}
