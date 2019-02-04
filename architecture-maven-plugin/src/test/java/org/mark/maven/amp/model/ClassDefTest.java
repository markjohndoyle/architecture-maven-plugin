package org.mark.maven.amp.model;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mark.maven.amp.testsupport.ClassDefBuilder.aClassDef;

/**
 * {@link ClassDef} unit tests.
 */
public class ClassDefTest
{
    private ClassDef classA = aClassDef("ClassA").withImport("org.a.classB").build();

    private ClassDef classB = aClassDef("ClassA").withImport(String.class.getName()).
                                               withImport("org.c.classC").
                                               build();

    @Test
    public void testGetFullQualName()
    {
        assertThat(classA.getFullQualName(), is("ClassA"));
    }

    @Test
    public void testImportsPositiveCase()
    {
        assertThat(classA.imports("org.a.classB"), is(true));
    }

    @Test
    public void testImportsPositiveCaseWithMultipleImports()
    {
        assertThat(classB.imports("org.c.classC"), is(true));
        assertThat(classB.imports(String.class.getName()), is(true));
    }

    @Test
    public void testImportsNegativeCase()
    {
        assertThat(classA.imports(String.class.getName()), is(false));
    }

    @Test
    public void testImportsNegativeCaseWithMutipleImports()
    {
        assertThat(classA.imports(Double.class.getName()), is(false));
    }

    @Test
    public void testToStringDoesntThrow()
    {
        classA.toString();
    }
}
