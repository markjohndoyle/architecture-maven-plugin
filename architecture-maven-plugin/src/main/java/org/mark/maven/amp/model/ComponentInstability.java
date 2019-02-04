package org.mark.maven.amp.model;

import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class ComponentInstability implements Comparable<ComponentInstability>
{
    private final Component comp;
    private final ComponentDependencies in;
    private final ComponentDependencies out;
    private final double instability;

    public ComponentInstability(Component component, ComponentDependencies fanIn, ComponentDependencies fanOut)
    {
        this.comp = component;
        this.in = fanIn;
        this.out = fanOut;
        this.instability = calculateInstabilty();
    }

    public List<ClassDef> getOutClassDeps()
    {
        return out.getDependencies();
    }

    public List<ClassDef> getInClassDeps()
    {
        return in.getDependencies();
    }

    public Set<Component> getInCompDeps()
    {
        return in.getCompDependencies();
    }

    public Set<Component> getOutCompDeps()
    {
        return out.getCompDependencies();
    }

    public int getFanIn()
    {
        return in.getNumClassDeps();
    }

    public int getFanOut()
    {
        return out.getNumClassDeps();
    }

    public double getInstabilityValue()
    {
        return instability;
    }

    public Component getComp()
    {
        return comp;
    }

    @Override
    public int compareTo(ComponentInstability o)
    {
        // Standard comparison of double. For instability, less is
        return Double.valueOf(o.instability).compareTo(Double.valueOf(instability));
    }

    @Override
    public boolean equals(Object obj)
    {
        if (obj == this) {return true;}
        if (!(obj instanceof ComponentInstability)) { return false;}
        ComponentInstability otherInstability = (ComponentInstability) obj;
        return Double.valueOf(instability).equals(Double.valueOf(otherInstability.instability));
    }

    private double calculateInstabilty()
    {
        double calculatedInstability = 0.5;
        if(in.getNumClassDeps() + out.getNumClassDeps() != 0)
        {
            double inPlusOut = (double)in.getNumClassDeps() + (double)out.getNumClassDeps();
            calculatedInstability = out.getNumClassDeps() / inPlusOut;
        }
        return calculatedInstability;
    }

    @Override
    public String toString()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("InstabilityMetrics [fanIn=");
        builder.append(in);
        builder.append(", fanOut=");
        builder.append(out);
        builder.append(", instability=");
        builder.append(instability);
        builder.append("]");
        return builder.toString();
    }

    @Override
    public int hashCode() { return Objects.hash(comp, in, out, instability); }


}
