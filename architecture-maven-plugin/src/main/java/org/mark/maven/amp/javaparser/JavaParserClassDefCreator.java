package org.mark.maven.amp.javaparser;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.mark.maven.amp.ClassDefCreator;
import org.mark.maven.amp.model.ClassDef;

public class JavaParserClassDefCreator implements ClassDefCreator
{

    @Override
    public List<ClassDef> createClassDefFor(Path srcFilePath) throws ClassDefCreationException
    {
      List<ClassDef> classDefs = new ArrayList<>();
      try(FileInputStream in = new FileInputStream(srcFilePath.toFile()))
      {
          CompilationUnit compUnit = JavaParser.parse(in);

          compUnit.accept(new VoidVisitorAdapter<Void>()
          {

              @Override
              public void visit(ClassOrInterfaceDeclaration n, Void arg)
              {
                  String name = compUnit.getPackageDeclaration().get().getNameAsString() + "." + n.getNameAsString();
                  classDefs.add(createClassDef(name, n.isAbstract() || n.isInterface()));
                  super.visit(n, arg);
              }

              @Override
              public void visit(EnumDeclaration n, Void arg) {
                  String name = compUnit.getPackageDeclaration().get().getNameAsString() + "." + n.getNameAsString();
                  classDefs.add(createClassDef(name, false));
                  super.visit(n, arg);
              }

              private ClassDef createClassDef(String name, boolean isAbstract)
              {
                  NodeList<ImportDeclaration> imports = compUnit.getImports();
                  Set<String> importNames = imports.stream().
                                                    map(ImportDeclaration::getNameAsString).
                                                    collect(Collectors.toSet());
                  return new ClassDef(name, importNames, isAbstract);
              }

          }, null);
      }
      catch (IOException e)
      {
          throw new ClassDefCreationException("Exception parsing src file " + srcFilePath, e);
      }
      return classDefs;
    }

}
