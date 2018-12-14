package org.softlang.qegal.process.maven;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Parent;
import org.apache.maven.model.Repository;
import org.apache.maven.model.building.ModelSource;
import org.apache.maven.model.building.UrlModelSource;
import org.apache.maven.model.resolution.InvalidRepositoryException;
import org.apache.maven.model.resolution.ModelResolver;
import org.apache.maven.model.resolution.UnresolvableModelException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class DefaultMavenModelResolver implements ModelResolver {

    private final GitModelSource gitModelSource;
    private final CloseableHttpClient httpClient;
    private final Repository defaultMavenRepository;
    private Path currentPath;
    private final Map<Path, String> pathToObjectId;

    private Set<Repository> repositories = new HashSet<>();
    private Map<Coordinate, ModelSource> modelSourceCache = new HashMap<>();


    public DefaultMavenModelResolver(GitModelSource gitModelSource, CloseableHttpClient httpClient, Repository defaultMavenRepository, Path currentPath, Map<Path, String> pathToObjectId) {
        this.gitModelSource = gitModelSource;
        this.httpClient = httpClient;
        this.defaultMavenRepository = defaultMavenRepository;
        this.currentPath = currentPath;
        this.pathToObjectId = pathToObjectId;
        repositories.add(defaultMavenRepository);
    }


    private URL buildPomUrl(String groupId, String artifactId, String version, String baseUrl) throws MalformedURLException {
        String path = baseUrl + "/" + groupId.replace(".", "/");
        String filename = artifactId + "/" + version + "/" + artifactId + "-" + version + ".pom";

        return new URL(path + "/" + filename);
    }

    @Override
    public ModelSource resolveModel(String groupId, String artifactId, String version) throws UnresolvableModelException {
    	System.out.println(artifactId);
        try {
            Coordinate coordinate = new Coordinate(groupId, artifactId, version);
            if (modelSourceCache.containsKey(coordinate))
                return modelSourceCache.get(coordinate);

            Set<URL> repositoryUrls = new HashSet<>();
            for (Repository repository : repositories) {
                String url = repository.getUrl();
                repositoryUrls.add(buildPomUrl(groupId, artifactId, version, url));
            }

            URL url = null;
            for (URL x : repositoryUrls) {
                URI uri = x.toURI();
                HttpHead httpUriRequest = new HttpHead(uri);
                CloseableHttpResponse httpResponse = httpClient.execute(httpUriRequest);
                Boolean is200 = httpResponse.getStatusLine().getStatusCode() == 200;
                httpResponse.close();
                if (is200) {
                    url = x;
                    break;
                }
            }

            if (url != null) {
                UrlModelSource urlModelSource = new UrlModelSource(url);
                modelSourceCache.put(coordinate, urlModelSource);
                return urlModelSource;
            }

            String message = "(" + groupId + "," + artifactId + "," + version + ") not found in repositories" + repositoryUrls;

            throw new UnresolvableModelException(message, groupId, artifactId, version);
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ModelSource resolveModel(Parent parent) throws UnresolvableModelException {
        String groupId = parent.getGroupId();
        String artifactId = parent.getArtifactId();
        String version = parent.getVersion();

        if (parent.getRelativePath() != null) {
            Path relativePath = Paths.get(parent.getRelativePath());
            Path newPath = currentPath.getParent().resolve(relativePath).normalize();
            if (!newPath.endsWith("pom.xml")) {
                newPath = currentPath.resolve("pom.xml");
            }
            ModelSource modelSource = null;
            if (pathToObjectId.containsKey(newPath) && !newPath.equals(currentPath)) {
                modelSource = gitModelSource.accessGit(pathToObjectId.get(newPath));
            } else {
                modelSource = resolveModel(groupId, artifactId, version);
            }
            // Not sure why that.
            currentPath = newPath;
            return modelSource;
        } else {
            resolveModel(groupId, artifactId, version);
        }

        return null;
    }

    @Override
    public ModelSource resolveModel(Dependency dependency) throws UnresolvableModelException {
        return resolveModel(dependency.getGroupId(), dependency.getArtifactId(), dependency.getVersion());
    }

    @Override
    public void addRepository(Repository repository) throws InvalidRepositoryException {
        repositories.add(repository);
    }

    @Override
    public void addRepository(Repository repository, boolean replace) throws InvalidRepositoryException {
        List<Repository> removes = repositories.stream().filter(x -> x.getId().equals(repository.getId())).collect(Collectors.toList());
        repositories.removeAll(removes);
        repositories.add(repository);
    }

    @Override
    public ModelResolver newCopy() {
        return new DefaultMavenModelResolver(gitModelSource, httpClient, defaultMavenRepository, currentPath, pathToObjectId);
    }
}
