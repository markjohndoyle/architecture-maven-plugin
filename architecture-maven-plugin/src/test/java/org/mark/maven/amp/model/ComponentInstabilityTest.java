package org.mark.maven.amp.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertTrue;
import static org.mark.maven.amp.testsupport.ClassDefBuilder.aClassDef;
import static org.mark.maven.amp.testsupport.ComponentBuilder.aComponent;

/**
 * <pre>
 * Scenario is as follows:
 *
 *     classes from other components
 *          +---+  +---+  +---+
 *          | q |  | r |  | s |
 *          +-+-+  +-+-+  +-+-+
 *            |      |      |
 *            |      |      |
 *            +-|    |      |
 *              |    +- ----+
 *              |     | |
 *              |     | |
 *      +------------------+                 +------------------+
 *      |    Cc |     | |  |                 |    Cd            |
 * +-------+    |     | |  |            +-------+               |
 * +-------+  +-v-+  +v-v+ |            |-------|  +-+-+        |
 *      |     | t |  | u +-------------------------> v |        |
 * +-------+  +---+  +---- |            |-------|  +-+-+        |
 * +-------|               |            +-------|               |
 *      +------------------+                 +------------------+
 *
 *  Component Cc has a fan in of 3 (q, r, s) and a fan out of 1 (v)
 *  Component Cd has a fan in of 1 (u) and a fan out of 0
 * </pre>
 *
 * @author mark
 *
 */
public class ComponentInstabilityTest
{
    private Component cC = aComponent().setName("Cc").build();
    private Component cD = aComponent().setName("Cd").build();

    private ClassDef classQ = aClassDef("q").withImport("t").build();
    private ClassDef classR = aClassDef("r").withImport("u").build();
    private ClassDef classS = aClassDef("s").withImport("u").build();
    private ClassDef classU = aClassDef("u").withImport("v").build();
    private ClassDef classV = aClassDef("v").build();

    private List<ClassDef> classesCcIn = Stream.of(classQ, classR, classS).collect(Collectors.toList());
    private List<ClassDef> classesCcOut = Stream.of(classV).collect(Collectors.toList());
    private List<ClassDef> classesCdIn = Stream.of(classU).collect(Collectors.toList());
    private List<ClassDef> classesCdOut = new ArrayList<>();

    private int fanInCountCc = classesCcIn.size();
    private int fanOutCountCc = classesCcOut.size();

    private ComponentDependencies fanInCa = new ComponentDependencies(cC, classesCcIn, new HashSet<>());
    private ComponentDependencies fanOutCa = new ComponentDependencies(cC, classesCcOut, new HashSet<>());
    private ComponentDependencies fanInCd = new ComponentDependencies(cD, classesCdIn, new HashSet<>());
    private ComponentDependencies fanOutCd = new ComponentDependencies(cD, classesCdOut, new HashSet<>());

    @Test
    public void testCalculations()
    {
        ComponentInstability reportUnderTest = new ComponentInstability(cC, fanInCa, fanOutCa);

        assertThat(reportUnderTest.getFanIn(), is(fanInCountCc));
        assertThat(reportUnderTest.getFanOut(), is(fanOutCountCc));
        assertThat(reportUnderTest.getComp(), is(cC));
        assertThat(reportUnderTest.getInClassDeps(), is(contains(classQ, classR, classS)));
        assertThat(reportUnderTest.getOutClassDeps(), is(contains(classV)));
        assertThat(reportUnderTest.getInstabilityValue(), is(0.25));
    }

    @Test
    public void testComparisionEqual()
    {
        ComponentInstability left = new ComponentInstability(cC, fanInCa, fanOutCa);
        ComponentInstability right = new ComponentInstability(cC, fanInCa, fanOutCa);

        assertThat(left, comparesEqualTo(right));
    }

    @Test
    public void testComparisionLessStable()
    {
        ComponentInstability lessStable = new ComponentInstability(cC, fanInCa, fanOutCa);
        ComponentInstability moreStable = new ComponentInstability(cD, fanInCd, fanOutCd);

        assertThat(moreStable.getInstabilityValue(), is(0.0));

        assertThat(lessStable, is(lessThan(moreStable)));
    }

    @Test
    public void testComparisionMoreStable()
    {
        ComponentInstability lessStable = new ComponentInstability(cC, fanInCa, fanOutCa);
        ComponentInstability moreStable = new ComponentInstability(cD, fanInCd, fanOutCd);

        assertThat(moreStable.getInstabilityValue(), is(0.0));

        assertThat(moreStable, is(greaterThan(lessStable)));
    }

    @Test
    public void testEqualityPositiveAndNegative()
    {
        ComponentInstability instabilityCc = new ComponentInstability(cC, fanInCa, fanOutCa);
        ComponentInstability sameDiffRef = new ComponentInstability(cC, fanInCa, fanOutCa);
        ComponentInstability instabilityCd = new ComponentInstability(cD, fanInCd, fanOutCd);

        assertThat(instabilityCc, is(equalTo(instabilityCc)));
        assertThat(instabilityCc, is(not(equalTo(instabilityCd))));
        assertTrue(instabilityCc.equals(sameDiffRef) && sameDiffRef.equals(instabilityCc));
        assertTrue(instabilityCc.hashCode() == sameDiffRef.hashCode());
    }
}
