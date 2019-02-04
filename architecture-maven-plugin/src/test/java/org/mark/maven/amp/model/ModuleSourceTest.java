package org.mark.maven.amp.model;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Test;

import static java.util.stream.Collectors.toCollection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mark.maven.amp.testsupport.ClassDefBuilder.aClassDef;

public class ModuleSourceTest
{
    private ClassDef classA = aClassDef("ClassA").withImport("org.a.classB").build();
    private ClassDef classB = aClassDef("ClassB").withImport("org.a.classB").build();

    private List<ClassDef> testClassDefList = Stream.of(classA, classB).collect(toCollection(ArrayList::new));

    private ModuleSource compUnderTest;

    @Before
    public void setupPerTest()
    {
        compUnderTest = new ModuleSource("SunSums", testClassDefList);
    }

    @Test
    public void testGetName()
    {
        assertThat(compUnderTest.getName(), is("SunSums"));
    }

    @Test
    public void testGetNumberOfClasses()
    {
        assertThat(compUnderTest.getNumberOfClasses(), is(2));
    }

    @Test
    public void testIsClassIn()
    {
        assertThat(compUnderTest.containsClass("ClassB"), is(true));
    }

    @Test
    public void testImportCount()
    {
        assertThat(compUnderTest.numClassesThatImport("org.a.classB"), is(2L));
    }

    @Test
    public void testImportedPositiveCase()
    {
        assertThat(compUnderTest.anyImports("org.a.classB"), is(true));
    }

    @Test
    public void testImportedNegativeCase()
    {
        assertThat(compUnderTest.anyImports(String.class.getName()), is(false));
    }

    @Test
    public void testPositiveEquality()
    {
        assertThat(compUnderTest, is(equalTo(compUnderTest)));
    }

    @Test
    public void testNegativeEqualityDifferentNameSourceModule()
    {
        ModuleSource compDiffName = new ModuleSource("different!", testClassDefList);
        assertThat(compUnderTest, is(not(equalTo(compDiffName))));
    }

    @Test
    public void testNegativeEqualityDifferentClassDefListSourceModule()
    {
        ModuleSource compDiffClasses = new ModuleSource("SunSums", new ArrayList<>());
        assertThat(compUnderTest, is(not(equalTo(compDiffClasses))));
    }

    @Test
    public void testNegativeEqualityDifferentClass()
    {
        String diffClass = "string";
        assertThat(compUnderTest, is(not(equalTo(diffClass))));
    }

    @Test
    public void testIterable()
    {
        for(ClassDef srcMod : compUnderTest)
        {
            assertNotNull(srcMod);
        }
    }

    @Test
    public void testToString()
    {
        assertNotNull(compUnderTest.toString());
    }

    @Test
    public void testEqualsSymmetric() {
        ModuleSource x = new ModuleSource("SunSums", testClassDefList);
        ModuleSource y = new ModuleSource("SunSums", testClassDefList);
        assertTrue(x.equals(y) && y.equals(x));
        assertTrue(x.hashCode() == y.hashCode());
    }
}
