package org.softlang.qegal.process.maven;


import org.apache.maven.model.building.ModelSource;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

public interface GitModelSource {

    public ModelSource accessGit(String objectid);
}
