package org.softlang.qegal.process.maven;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.maven.model.Model;
import org.apache.maven.model.Repository;
import org.apache.maven.model.building.*;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

public class DefaultModelResolver {

    private RequestConfig requestConfig = RequestConfig
            .custom()
            .setConnectTimeout(5000)
            .setConnectionRequestTimeout(5000)
            .setSocketTimeout(5000)
            .build();

    private CloseableHttpClient httpClient = HttpClientBuilder
            .create()
            .useSystemProperties()
            .setDefaultRequestConfig(requestConfig)
            .build();

    private ModelBuilder modelBuilder;
    private Properties systemProperties = new Properties();
    private Repository defaultMavenRepository = new Repository();

    private DefaultModelCache modelCache = new DefaultModelCache();

    public DefaultModelResolver() throws ModelBuildingException {
        try {
            DefaultPlexusContainer container = new DefaultPlexusContainer();
            modelBuilder = container.lookup(ModelBuilder.class);
            String javaVersion = System.getProperty("java.version");
            systemProperties.setProperty("java.version", javaVersion);
            defaultMavenRepository.setId("central");
            defaultMavenRepository.setName("Central Repository");
            String defaultRepositoryUrl = "https://repo.maven.apache.org/maven2";
            defaultMavenRepository.setUrl(defaultRepositoryUrl);
        } catch (PlexusContainerException | ComponentLookupException e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Model> resolveModel(GitModelSource gitModelSource, String pathString, String objectId, Map<String, String> pathStringToObjectId) {
        try {
            Path path = Paths.get("/" + pathString);

            Map<Path, String> pathToObjectId = new HashMap<>();
            for (Map.Entry<String, String> e : pathStringToObjectId.entrySet()) {
                pathToObjectId.put(Paths.get("/" + e.getKey()), e.getValue());
            }

            DefaultModelBuildingRequest modelBuildingRequest = new DefaultModelBuildingRequest();
            DefaultMavenModelResolver modelResolver = new DefaultMavenModelResolver(gitModelSource, httpClient, defaultMavenRepository, path, pathToObjectId);

            ModelSource modelSource = gitModelSource.accessGit(objectId);
            modelBuildingRequest.setModelSource(modelSource);
            modelBuildingRequest.setModelResolver(modelResolver);
            modelBuildingRequest.setValidationLevel(ModelBuildingRequest.VALIDATION_LEVEL_MINIMAL);
            modelBuildingRequest.setTwoPhaseBuilding(true);
            modelBuildingRequest.setProcessPlugins(false);
            modelBuildingRequest.setSystemProperties(systemProperties);
            modelBuildingRequest.setModelCache(modelCache);
            modelBuildingRequest.getUserProperties().put("basedir", "/");
            modelBuildingRequest.getUserProperties().put("pom.basedir", "/");
            modelBuildingRequest.getUserProperties().put("project.basedir", "/");

            ModelBuildingResult modelBuildingResult = modelBuilder.build(modelBuildingRequest);
            modelBuilder.build(modelBuildingRequest, modelBuildingResult);

            modelBuilder.build(modelBuildingRequest, modelBuildingResult);

            Model model = modelBuildingResult.getEffectiveModel();
            return Optional.of(model);
        } catch (ModelBuildingException e) {
            return Optional.empty();
        }
    }
}
