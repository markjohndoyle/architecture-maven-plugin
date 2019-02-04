package org.mark.maven.amp.model;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.google.common.collect.ImmutableSet;
import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mark.maven.amp.testsupport.ClassDefBuilder.aClassDef;
import static org.mark.maven.amp.testsupport.ComponentBuilder.aComponent;
import static org.mark.maven.amp.testsupport.ModuleSourceBuilder.aSourceModule;


public class ComponentDependenciesTest
{
    private final ClassDef classA = aClassDef("org.a.classA").build();
    private final ClassDef classB = aClassDef("org.b.classB").build();
    private final Component testComp = aComponent().setName("Woodrich").build();
    private final Component outsideComp = aComponent().setName("Outsider").
                                              setSourceModule(aSourceModule().
                                                  withClassDef(classA).
                                                  withClassDef(classB)).
                                          build();
    private final List<ClassDef> classesAandB = Stream.of(classA, classB).collect(Collectors.toList());
    private final ImmutableSet<Component> outComp = ImmutableSet.of(outsideComp);

    @Test
    public void testGetDependencyCount()
    {
        ComponentDependencies mdUnderTest = new ComponentDependencies(testComp, classesAandB, outComp);
        assertThat(mdUnderTest.getNumClassDeps(), is(equalTo(2)));
    }

    @Test
    public void testGetDependencies()
    {
        ComponentDependencies mdUnderTest = new ComponentDependencies(testComp, classesAandB, outComp);
        assertThat(mdUnderTest.getDependencies(), hasItems(classA, classB));
    }

    @Test
    public void testCorrectComponentDependenciesReturned()
    {
        ComponentDependencies mdUnderTest = new ComponentDependencies(testComp, classesAandB, outComp);
        Set<Component> compDeps = mdUnderTest.getCompDependencies();
        assertThat(compDeps, hasSize(1));
        assertThat(compDeps, hasItem(outsideComp));
    }
}
