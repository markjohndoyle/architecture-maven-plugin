package org.mark.maven.amp.model;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mark.maven.amp.testsupport.ClassDefBuilder.aClassDef;
import static org.mark.maven.amp.testsupport.ComponentBuilder.aComponent;
import static org.mark.maven.amp.testsupport.ModuleSourceBuilder.aSourceModule;

public class ComponentTest
{
    /**
     * Component hierarchy for tests.
     *
     * +-----+         +-----+       +-----+
     * |     |         |     |       |     |
     * |  A  +<--------|  B  +<------|  D  |
     * |     |         |     |       |     |
     * +-----+         +-----+       +-----+
     *                    ^             ^
     *          +-----+   |             |
     *          |     |   |             |
     *          |  C  | ----------------+
     *          |     |
     *          +-----+
     *
     */
    ClassDef classA = aClassDef("classA").setAbstract(true).build();
    ClassDef classB = aClassDef("classB").withImport("classA").build();
    ClassDef classC = aClassDef("classC").withImport("classB", "classD").build();
    ClassDef classD = aClassDef("classD").withImport("classB").build();
    ModuleSource srcModA = aSourceModule().setName("srcModA").withClassDef(classA).build();
    ModuleSource srcModB = aSourceModule().setName("srcModB").withClassDef(classB).build();
    ModuleSource srcModC = aSourceModule().setName("srcModC").withClassDef(classC).build();
    ModuleSource srcModD = aSourceModule().setName("srcModD").withClassDef(classD).build();
    Component compA = aComponent().setName("A").setSourceModule(srcModA).build();
    Component compB = aComponent().setName("B").setSourceModule(srcModB).build();
    Component compC = aComponent().setName("C").setSourceModule(srcModC).build();
    Component compD = aComponent().setName("D").setSourceModule(srcModD).build();

    List<Component> allComps = Stream.of(compA, compB, compC, compD).collect(Collectors.toList());

    ComponentInstability reportCompA = compA.instabilityAgainst(allComps);
    ComponentInstability reportCompB = compB.instabilityAgainst(allComps);
    ComponentInstability reportCompC = compC.instabilityAgainst(allComps);
    ComponentInstability reportCompD = compD.instabilityAgainst(allComps);

    @Test
    public void testAbstractness()
    {
        assertThat(compA.getAbstraction(), is(1.0));
    }

    @Test
    public void testCalcFanInZero()
    {
        assertThat(reportCompC.getFanIn(), is(0));
    }

    @Test
    public void testCalcFanOutZero()
    {
        assertThat(reportCompA.getFanOut(), is(0));
    }

    @Test
    public void testCalcFanInOne()
    {
        assertThat(reportCompA.getFanIn(), is(1));
        assertThat(reportCompD.getFanIn(), is(1));
    }

    @Test
    public void testCalcFanOutOne()
    {
        assertThat(reportCompB.getFanOut(), is(1));
        assertThat(reportCompD.getFanOut(), is(1));
    }

    @Test
    public void testCalcFanInTwo()
    {
        assertThat(reportCompB.getFanIn(), is(2));
    }

    @Test
    public void testCalcFanOutTwo()
    {
        assertThat(reportCompC.getFanOut(), is(2));
    }

    @Test
    public void testGetName()
    {
        assertThat(compA.getName(), is("A"));
    }

    @Test
    public void testDependsUponPostiveCase()
    {
        assertThat(compC.dependsUpon(compB), is(true));
        assertThat(compC.dependsUpon(compD), is(true));
    }

    @Test
    public void testDependsUponNegativeCase()
    {
        assertThat(compC.dependsUpon(compC), is(false));
        assertThat(compC.dependsUpon(compA), is(false));
    }

    @Test
    public void testDependents()
    {
        Map<ClassDef, Set<ClassDef>> dependents = compC.dependents(compB);
        dependents.forEach((imported, importers) -> {
            importers.forEach(cd -> System.out.println(cd.getFullQualName() + " imports " + imported.getFullQualName()));
        });
    }

    @Test
    public void testComponentDependenciesWithNoIn()
    {
        Set<Component> inCompDeps = reportCompC.getInCompDeps();
        assertThat(inCompDeps, is(empty()));

        Set<Component> outCompDeps = reportCompC.getOutCompDeps();
        assertThat(outCompDeps, hasSize(2));
        assertThat(outCompDeps, hasItems(compB, compD));
        assertThat(outCompDeps, not(hasItem(compA)));
        assertThat(outCompDeps, not(hasItem(compC)));
    }

    @Test
    public void testComponentDependenciesWithInAndOut()
    {
        Set<Component> inCompDeps = reportCompB.getInCompDeps();
        assertThat(inCompDeps, hasSize(2));
        assertThat(inCompDeps, hasItems(compD, compC));

        Set<Component> outCompDeps = reportCompB.getOutCompDeps();
        assertThat(outCompDeps, hasSize(1));
        assertThat(outCompDeps, hasItems(compA));
        assertThat(outCompDeps, not(hasItem(compB)));
        assertThat(outCompDeps, not(hasItem(compC)));
        assertThat(outCompDeps, not(hasItem(compD)));
    }

    @SuppressWarnings("unlikely-arg-type") // The test checks equality against a different class.
    @Test
    public void testNegativeEqualityDifferentClass()
    {
        String string = "blah";
        assertThat(compA.equals(string), is(not(true)));
    }

    @Test
    public void testEqualsSymmetric() {
        Component x = compA;
        Component y = compA;
        assertTrue(x.equals(y) && y.equals(x));
        assertTrue(x.hashCode() == y.hashCode());
        assertFalse(x.equals(compB) && compB.equals(x));
        assertFalse(x.hashCode() == compB.hashCode());
    }
}
