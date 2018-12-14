package org.softlang.qegal.process.regex;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.softlang.qegal.io.IOLayer;

public class RegexMiner {

	private IOLayer iolayer;
	private List<String> pomPaths;

	private Pattern rgxProject = Pattern.compile("<project.*?>(?<projectBlock>.*)</project>", Pattern.DOTALL);
	private Pattern rgxRemoveMultiElements = Pattern.compile("<[^/>]+>([\\t\\n\\r\\s]+.+[\\t\\n\\r\\s]+)+</[^>]+>");
	private Pattern rgxParent = Pattern.compile("<parent>(?<parentBlock>.*)</parent>", Pattern.DOTALL);
	private Pattern rgxDependency = Pattern.compile("<dependency>(?<dependencyBlock>.*?)</dependency>", Pattern.DOTALL);
	private Pattern rgxProperties = Pattern.compile("<properties>(?<properties>.*)</properties>", Pattern.DOTALL);
	private Pattern rgxProperty = Pattern.compile("<(?<name>[^>]+)>(?<value>[^<>]+)</[^>]+>");
	private Pattern rgxArtifactId = Pattern.compile("<artifactId>(?<artifact>.*)</artifactId>");
	private Pattern rgxGroupId = Pattern.compile("<groupId>(?<group>.*)</groupId>");
	private Pattern rgxVersion = Pattern.compile("<version>(?<version>.*)</version>");
	private Pattern rgxIsVariable = Pattern.compile("\\$\\{(.*)\\}");

	public RegexMiner(IOLayer iolayer) {
		this.iolayer = iolayer;
		pomPaths = new ArrayList<>();
	}

	public List<POM> mine() {
		String root = iolayer.root();
		// Get POM paths
		getPOMs(root);
		List<POM> tempPomObjects = new ArrayList<>();

		// Convert every path to a (temporary) object just in order to get it's content
		// and information
		for (String pomPath : pomPaths) {
			POM p = new POM();
			p.setLocation(pomPath);
			// Get content
			String content = readPOMContent(pomPath);
			p.setContent(content);
			// Identify project block
			String projectBlock = getMatchResult(rgxProject.matcher(content), "projectBlock");
			projectBlock = rgxRemoveMultiElements.matcher(projectBlock).replaceAll("");
			// Extract artifactId and groupId
			String artifactId = getMatchResult(rgxArtifactId.matcher(projectBlock), "artifact");
			String groupId = getMatchResult(rgxGroupId.matcher(projectBlock), "group");
			p.setArtifactId(artifactId);
			p.setGroupId(groupId);
			// Get properties
			String properties = getMatchResult(rgxProperties.matcher(content), "properties");
			Matcher matcherProperties = rgxProperty.matcher(properties);
			while (matcherProperties.find()) {
				p.addProperty(matcherProperties.group("name"), matcherProperties.group("value"));
			}
			String projectVersion = getMatchResult(rgxVersion.matcher(projectBlock), "version");
			if (!projectVersion.equals("")) {
				p.addProperty("project.version", projectVersion);
			}
			// Get dependencies
			Matcher matcherDependencies = rgxDependency.matcher(content);
			while (matcherDependencies.find()) {
				String dependencyBlock = matcherDependencies.group("dependencyBlock");
				Dependency d = new Dependency();
				String dependencyArtifactId = getMatchResult(rgxArtifactId.matcher(dependencyBlock), "artifact");
				String dependencyGroupId = getMatchResult(rgxGroupId.matcher(dependencyBlock), "group");
				String dependencyVersion = getMatchResult(rgxVersion.matcher(dependencyBlock), "version");
				d.setArtifactId(dependencyArtifactId);
				d.setGroupId(dependencyGroupId);
				d.setVersion(dependencyVersion);
				p.addDependency(d);
			}
			tempPomObjects.add(p);
		}

		// Let's identify parent relationships
		for (int i = 0; i < tempPomObjects.size(); i++) {

			String parentBlock = getMatchResult(rgxParent.matcher(tempPomObjects.get(i).getContent()), "parentBlock");
			String parentArtifactId = getMatchResult(rgxArtifactId.matcher(parentBlock), "artifact");
			for (int j = 0; j < tempPomObjects.size(); j++) {
				if (j == i)
					continue;
				String artifactId2 = tempPomObjects.get(j).getArtifactId();
				if (parentArtifactId.equals(artifactId2)) {
					// Found a relationship
					tempPomObjects.get(j).addChildren(tempPomObjects.get(i));
					tempPomObjects.get(i).setParent(tempPomObjects.get(j));
				}
			}
		}

		// Let's search for poms, that don't have any children
		List<POM> pomsToStartWith = new ArrayList<>();
		for (POM p : tempPomObjects) {
			if (p.getChildren().size() == 0) {
				pomsToStartWith.add(p);
			}
		}
		List<POM> pomsDone = new ArrayList<>();
		pomsDone = resolve(pomsToStartWith);
		while (pomsDone.size() < tempPomObjects.size()) {
			List<POM> next = new ArrayList<>();
			for (int i = 0; i < pomsDone.size(); i++) {
				if (pomsDone.get(i).getParent() != null && !pomsDone.contains(pomsDone.get(i).getParent()) && !next.contains(pomsDone.get(i).getParent())) {
					next.add(pomsDone.get(i).getParent());
				}
			}
			if (next.size() == 0)
				break;
			List<POM> newResolved = resolve(next);
			pomsDone.addAll(newResolved);
		}
		
		return pomsDone;
		
	}

	private void getPOMs(String root) {
		List<String> children = iolayer.children(root);
		for (String child : children) {
			if (iolayer.isDirectory(child)) {
				getPOMs(child);
			} else {
				if (child.endsWith("pom.xml")) {
					pomPaths.add(child);
				}
			}
		}
	}

	private String readPOMContent(String path) {
		try {
			return IOUtils.toString(iolayer.access(path), StandardCharsets.UTF_8);
		} catch (IOException e) {
			return "";
		}
	}

	private String getMatchResult(Matcher m, String groupName) {
		if (m.find()) {
			return m.group(groupName);
		}
		return "";
	}

	private List<POM> resolve(List<POM> pomsToStartWith) {
		for (int i = 0; i < pomsToStartWith.size(); i++) {
			for (int j = 0; j < pomsToStartWith.get(i).getDependencies().size(); j++) {
				if (rgxIsVariable.matcher(pomsToStartWith.get(i).getDependencies().get(j).getVersion()).matches()) {
					String variableToSearchFor = pomsToStartWith.get(i).getDependencies().get(j).getVersion()
							.replace("${", "").replace("}", "");
					boolean isResolved = false;
					POM parent = pomsToStartWith.get(i).getParent();
					while (parent != null && !isResolved) {
						if (parent.getProperties().get(variableToSearchFor) != null) {
							String resolvedVariable = parent.getProperties().get(variableToSearchFor);
							pomsToStartWith.get(i).getDependencies().get(j).setVersion(resolvedVariable);
							if (!resolvedVariable.contains("${")) {
								isResolved = true;
								break;
							} else {
								variableToSearchFor = resolvedVariable.replace("${", "").replace("}", "");
								continue;
							}
						}
						parent = parent.getParent();
					}
				}
			}
		}
		return pomsToStartWith;
	}
}
