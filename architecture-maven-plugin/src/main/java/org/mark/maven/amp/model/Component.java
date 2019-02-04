package org.mark.maven.amp.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;


/**
 * Represents an architectural component. In this system a Maven module is considered a component.
 * It consists of a name (artifact ID) and a source module.
 *
 * Architecturally this offers methods to get the details of the component, number of classes etc, also clients can
 * calculate abstractness, and create an {@link ComponentInstability} with respect to a List of other {@link Component}
 * instances.
 *
 * @author markjohndoyle@googlemail.com
 *
 */
public final class Component
{
    private final String name;
    private final ModuleSource srcModule;
    private final int numClasses;
    private final long numAbstractClasses;
    private final double abstractness;

    public Component(String componentName, ModuleSource sourceModule)
    {
        this.name = componentName;
        this.srcModule = sourceModule;
        this.numClasses = getNumClasses();
        this.numAbstractClasses = getNumAbstractClasses();
        this.abstractness = (double) numAbstractClasses / (double) numClasses;
    }

    public ComponentInstability instabilityAgainst(List<Component> components)
    {
        ComponentDependencies fanIn = calculateFanIn(components);
        ComponentDependencies fanOut = calculateFanOut(components);
        return new ComponentInstability(this, fanIn, fanOut);
    }

    public String getName()
    {
        return name;
    }

    public int getNumClasses()
    {
        return srcModule.getNumberOfClasses();
    }

    public long getNumAbstractClasses()
    {
        return srcModule.getNumberOfAbstractClasses();
    }

    public double getAbstraction()
    {
        return abstractness;
    }

    public boolean dependsUpon(Component otherComp)
    {
        // Can't depend upon yourself..
        if(otherComp == this) { return false; }

        for(ClassDef otherClass : otherComp.srcModule)
        {
            if(srcModule.anyImports(otherClass.getFullQualName()))
            {
                return true;
            }
        }
        return false;
    }

    public Map<ClassDef, Set<ClassDef>> dependents(Component otherComp)
    {
        Map<ClassDef, Set<ClassDef>> results = new HashMap<>();
        // Can't depend upon yourself..
        if(!this.dependsUpon(otherComp)) { return results; }

        otherComp.srcModule.forEach(cd -> {
            Set<ClassDef> importers = srcModule.classesThatImport(cd.getFullQualName());
            results.put(cd, importers);
        });
        return results;
    }

    private ModuleSource getSourceModule()
    {
        return srcModule;
    }

    /**
     * Outgoing dependencies. The number of classes inside this component that depend upon classes in
     * the given List of other components.
     *
     * @param otherComponents
     *            the other components to calculate this components fan out against.
     * @return
     */
    private ComponentDependencies calculateFanOut(List<Component> otherComponents)
    {
        List<ClassDef> outsideClassDeps = new ArrayList<>();
        Set<Component> outsideCompDeps = new HashSet<>();
        for (Component outsideComponent : otherComponents)
        {
            if(!outsideComponent.equals(this))
            {
                for(ClassDef outsideClass : outsideComponent.srcModule)
                {
                    Set<ClassDef> classesThatImport = srcModule.classesThatImport(outsideClass.getFullQualName());
                    outsideClassDeps.addAll(classesThatImport);
                    if(!classesThatImport.isEmpty())
                    {
                        outsideCompDeps.add(outsideComponent);
                    }
                }
            }
        }
        return new ComponentDependencies(this, outsideClassDeps, outsideCompDeps);
    }

    /**
     * Incoming dependencies. The number of classes outside this component that depend upon classes within this
     * component.
     *
     * @param otherComponents
     *            the other components to calculate this components fan out against.
     * @return
     */
    private ComponentDependencies calculateFanIn(List<Component> otherComponents)
    {
        List<ClassDef> incomingDependents = new ArrayList<>();
        Set<Component> incomingCompDeps = new HashSet<>();
        for(ClassDef srcClass : srcModule)
        {
            for(Component outsideComponent : otherComponents)
            {
                if(!outsideComponent.equals(this))
                {
                    ModuleSource outsideSource = outsideComponent.getSourceModule();
                    Set<ClassDef> outsideDependents = outsideSource.classesThatImport(srcClass.getFullQualName());
                    incomingDependents.addAll(outsideDependents);
                    if(!outsideDependents.isEmpty())
                    {
                        incomingCompDeps.add(outsideComponent);
                    }
                }
            }
        }
        return new ComponentDependencies(this, incomingDependents, incomingCompDeps);
    }

    @Override
    public String toString()
    {
        return name;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(srcModule);
    }

    @Override
    public boolean equals(Object other)
    {
        if (other == this)
        {
            return true;
        }
        if (!(other instanceof Component))
        {
            return false;
        }
        Component comp = (Component) other;
        return Objects.equals(srcModule, comp.srcModule);
    }
}
