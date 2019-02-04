package org.mark.maven.amp;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.io.FilenameUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.mark.maven.amp.ClassDefCreator.ClassDefCreationException;
import org.mark.maven.amp.graph.DirectedComponentGraph;
import org.mark.maven.amp.graph.jgrapht.JGraphComponentGraph;
import org.mark.maven.amp.javaparser.JavaParserClassDefCreator;
import org.mark.maven.amp.model.ClassDef;
import org.mark.maven.amp.model.Component;
import org.mark.maven.amp.model.ComponentInstability;
import org.mark.maven.amp.model.ModuleSource;

@Mojo(name = "stability",
      defaultPhase = LifecyclePhase.PROCESS_SOURCES,
      aggregator = true,
      threadSafe = true)
public final class StabilityMojo extends AbstractMojo
{
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    /**
     * The projects in the reactor.
     *
     * @parameter expression="${reactorProjects}"
     * @readonly
     */
    @Parameter(defaultValue = "${reactorProjects}", readonly = true)
    private List<MavenProject> reactorProjects;

    /**
     * Location of the file.
     */
    @Parameter(defaultValue = "${project.build.directory}", property = "outputDir", required = true)
    private File outputDirectory;

    @Parameter(property = "modules", required = false)
    private List<String> modules;

    private final ClassDefCreator classDefCreator;

    public StabilityMojo()
    {
        this.classDefCreator = new JavaParserClassDefCreator();
    }

    @Override
    public void execute() throws MojoExecutionException
    {
        List<ModuleSource> srcModules = new ArrayList<>();
        for(MavenProject moduleProject : reactorProjects)
        {
            getLog().debug("Scanning module " + moduleProject.getArtifactId());
            List<ClassDef> moduleClasses = new ArrayList<>();
            File srcDir = new File(moduleProject.getBuild().getSourceDirectory());
            if(srcDir.exists())
            {
                getLog().debug("Source dir available, gathering stability metrics from " + srcDir.toString());
                List<Path> srcs = gatherSrcPaths(srcDir);
                getLog().debug("Number source files: " +  srcs.size());
                for (Path srcFilePath : srcs)
                {
                    try
                    {
                        moduleClasses.addAll(classDefCreator.createClassDefFor(srcFilePath));
                    }
                    catch (ClassDefCreationException e)
                    {
                        throw new MojoExecutionException("Error creating class def", e);
                    }
                }
            }

            if(!moduleClasses.isEmpty())
            {
                srcModules.add(new ModuleSource(moduleProject.getArtifactId(), moduleClasses));
            }
        }


        getLog().debug("All sources processed");
        List<Component> components = new ArrayList<>();
        for(ModuleSource srcModule : srcModules)
        {
            Component component = new Component(srcModule.getName(), srcModule);
            components.add(component);
        }

        // report
        Map<Component, ComponentInstability> allReports = new HashMap<>();
        for(Component comp : components)
        {
            ComponentInstability instability = comp.instabilityAgainst(components);
            allReports.put(comp, instability);

            getLog().info("------ Module -------");
            getLog().info(comp.getName());
            getLog().info("Number of classes = " + comp.getNumClasses());
            getLog().info("Number of abstract classes = " + comp.getNumAbstractClasses());
            getLog().info("Abstraction = " + comp.getAbstraction());
            getLog().info("FanIn = " + instability.getFanIn() + " FanOut = " + instability.getFanOut());
            getLog().info("Instability = " + instability.getInstabilityValue());
            getLog().info("Main sequence = (" + instability.getInstabilityValue() + ", " + comp.getAbstraction() + ")");
            getLog().info("Distance = " + Math.abs(comp.getAbstraction() + instability.getInstabilityValue() -1));
        }

        try
        {
            DirectedComponentGraph depGraph = new JGraphComponentGraph(allReports);
            depGraph.exportDot();
        }
        catch (IOException e1)
        {
            throw new MojoExecutionException(e1.getMessage(), e1);
        }

        StringBuilder builder = new StringBuilder();
        for(Entry<Component, ComponentInstability> report  : allReports.entrySet())
        {
            Component comp = report.getKey();
            ComponentInstability instabilityReport = report.getValue();
            double instability = instabilityReport.getInstabilityValue();
            double abstraction = comp.getAbstraction();
            double distance = Math.abs(abstraction + instability -1);
            builder.append(comp.getName() + "," + instability + "," + abstraction + "," + distance);
            builder.append(System.lineSeparator());
            checkStabilityDirection(report, allReports);
        }

        try
        {
            Files.write(Paths.get("mainsequence.csv"), builder.toString().getBytes());
        }
        catch (IOException e)
        {
            throw new MojoExecutionException("Error creating CSV file", e);
        }
    }

    private void checkStabilityDirection(Entry<Component, ComponentInstability> instability, Map<Component, ComponentInstability> allReports)
    {
        for(ComponentInstability outsideInstability : allReports.values())
        {
            Component sourceComp = instability.getKey();
            Component outsideComp = outsideInstability.getComp();

            if(!sourceComp.dependsUpon(outsideComp))
            {
                getLog().debug("Bypassing stability check as " + sourceComp.getName() + " does not depend upon " + outsideComp.getName());
                continue;
            }

            getLog().debug("Checking stability as " + sourceComp.getName() + " depends upon " + outsideComp.getName());
            if(modules.contains(sourceComp.getName()))
            {
                getLog().info("DETAILS for " + sourceComp.getName());
                Map<ClassDef, Set<ClassDef>> dependents = sourceComp.dependents(outsideComp);
                getLog().info("There are " + dependents.size());
                dependents.forEach((imported, importers) ->
                    importers.forEach(importer -> getLog().info(importer.getFullQualName() + " imports " + imported.getFullQualName()))
                );
            }

            // Unstable ---> stable is good
            if(instability.getValue().compareTo(outsideInstability) > 0)
            {
                getLog().warn("More stable " + sourceComp.getName() +
                              "(" + instability.getValue().getInstabilityValue() + ") " +
                              "depends upon less stable component " +
                              outsideComp.getName() +
                              "(" + outsideInstability.getInstabilityValue() + ")");

                if(modules.stream().anyMatch(modIn -> modIn.contains(sourceComp.getName())))
                {
                    ComponentInstability inst = instability.getValue();
//                    getLog().warn("Fan In Explode");
//                    for(ClassDef dep : inst.getIncomingDependencies())
//                    {
//                        getLog().info(dep.getFullQualName());
//                    }
                    getLog().warn("Fan Out Explode");
                    for(ClassDef dep : inst.getOutClassDeps())
                    {
                        getLog().info(dep.getFullQualName());
                    }
                }
            }
            else
            {
                getLog().debug("Stability ok " + instability.getValue().getInstabilityValue() + " > " + outsideInstability.getInstabilityValue());
            }
        }
    }

    private List<Path> gatherSrcPaths(File path) throws MojoExecutionException
    {
        getLog().debug("gathering src paths for " + path);
        List<Path> classes = new ArrayList<>();
        try (DirectoryStream<Path> srcStream = Files.newDirectoryStream(path.toPath()))
        {
            Iterator<Path> it = srcStream.iterator();
            while (it.hasNext())
            {
                Path srcFilePath = it.next();
                if (srcFilePath.toFile().isDirectory())
                {
                    classes.addAll(gatherSrcPaths(srcFilePath.toFile()));
                }
                else if (FilenameUtils.getExtension(srcFilePath.toString()).equals("java") &&
                         !srcFilePath.endsWith("package-info.java"))
                {
                    getLog().debug("Adding " + srcFilePath);
                    classes.add(srcFilePath);
                }
            }
            getLog().debug("Added " + classes.size() + " classes after " + path);
            return classes;
        }
        catch (IOException e)
        {
            throw new MojoExecutionException(e.getMessage(), e);
        }
    }
}