package org.mark.maven.amp;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import org.mark.maven.amp.model.ClassDef;


public interface ClassDefCreator
{
    public static final class ClassDefCreationException extends Exception
    {

        public ClassDefCreationException(String string, IOException e)
        {
            super(string, e);
        }

    }

    List<ClassDef> createClassDefFor(Path srcFilePath) throws ClassDefCreationException;
}
