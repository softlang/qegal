package org.softlang.qegal.process.regex;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class POM implements Serializable{
	private String location;
	private String artifactId;
	private String groupId;
	private String content;
	private POM parent;
	private List<POM> children;
	private List<Dependency> dependencies;
	private Map<String, String> properties;

	public POM() {
		children = new ArrayList<>();
		dependencies = new ArrayList<>();
		properties = new HashMap<>();
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public String getArtifactId() {
		return artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public POM getParent() {
		return parent;
	}

	public void setParent(POM parent) {
		this.parent = parent;
	}

	public List<POM> getChildren() {
		return children;
	}

	public void setChildren(List<POM> children) {
		this.children = children;
	}

	public void addChildren(POM child) {
		if (!children.contains(child)) {
			children.add(child);
		}
	}

	public void addDependency(Dependency dependency) {
		if (!dependencies.contains(dependency)) {
			dependencies.add(dependency);
		}
	}

	public List<Dependency> getDependencies() {
		return dependencies;
	}

	public void setDependencies(List<Dependency> dependencies) {
		this.dependencies = dependencies;
	}
	
	public void addProperty(String name, String value) {
		if(!properties.containsKey(name)) {
			properties.put(name, value);
		}
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	@Override
	public boolean equals(Object o) {
		POM other = (POM) o;
		return other.artifactId == this.artifactId && other.groupId == this.groupId;
	}

	@Override
	public String toString() {
		return "POM with artifactId: " + artifactId + " and groupId: " + groupId + " at location: " + location + "\n"
				+ "and parent pom: " + (parent != null ? parent.getArtifactId() : "none");
	}

}
