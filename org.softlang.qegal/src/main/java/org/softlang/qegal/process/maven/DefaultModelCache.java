package org.softlang.qegal.process.maven;

import org.apache.maven.model.building.ModelCache;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DefaultModelCache implements ModelCache {

    private Map<TaggedDependency, Object> cache = new HashMap<>();

    @Override
    public void put(String groupId, String artifactId, String version, String tag, Object data) {
        TaggedDependency key = new TaggedDependency(groupId, artifactId, version, tag);
        cache.put(key, data);
    }

    @Override
    public Object get(String groupId, String artifactId, String version, String tag) {
        TaggedDependency key = new TaggedDependency(groupId, artifactId, version, tag);
        return cache.getOrDefault(key, null);
    }

    private class TaggedDependency {
        private final String groupId;
        private final String artifactId;
        private final String version;
        private final String tag;

        private TaggedDependency(String groupId, String artifactId, String version, String tag) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
            this.tag = tag;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            TaggedDependency that = (TaggedDependency) o;
            return Objects.equals(groupId, that.groupId) &&
                    Objects.equals(artifactId, that.artifactId) &&
                    Objects.equals(version, that.version) &&
                    Objects.equals(tag, that.tag);
        }

        @Override
        public int hashCode() {

            return Objects.hash(groupId, artifactId, version, tag);
        }
    }


}
