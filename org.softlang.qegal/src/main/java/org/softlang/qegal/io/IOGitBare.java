package org.softlang.qegal.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.jena.ext.com.google.common.io.Files;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectLoader;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.TreeWalk;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.softlang.qegal.jutils.Gits;

import com.google.common.base.Charsets;

// TODO: Create an indexed versions of this class.
public class IOGitBare implements IOLayer {

	// private final Git git;
	public static void main(String[] args) throws IncorrectObjectTypeException, IOException {

		Git git = Gits.collect("lukashaertel/xtext-fsml", true);
		Repository repository = git.getRepository();
		IOLayer iolayer = new IOGitBare(repository, "9affc328b003dc5fb69f22ba39e9a428c5866633");

		System.out.println("Root Children:");
		for (String c : iolayer.children(iolayer.root()))
			System.out.println(c);

		System.out.println("Some Children:");
		for (String c : iolayer.children("repository:/MegaL/eu.pasusu.megal/src/eu/pasusu/megal"))
			System.out.println(c);

		System.out.println(
				"Is directory: " + iolayer.isDirectory("repository:/MegaL/eu.pasusu.megal/src/eu/pasusu/megal"));
		System.out.println(
				"Is directory file: " + iolayer.isFile("repository:/MegaL/eu.pasusu.megal/src/eu/pasusu/megal"));
		System.out.println("Is file: "
				+ iolayer.isFile("repository:/MegaL/eu.pasusu.megal/src/eu/pasusu/megal/GenerateMegaL.mwe2"));

		System.out.println(IOUtils.toString(
				iolayer.access("repository:/MegaL/eu.pasusu.megal/src/eu/pasusu/megal/GenerateMegaL.mwe2"),
				Charsets.UTF_8));

	}

	final RevTree tree;

	final Repository repository;

	public IOGitBare(Repository repository, String ssh)
			throws MissingObjectException, IncorrectObjectTypeException, IOException {
		// Find the commit for this ssh.
		this.repository = repository;
		System.out.println(ssh);
		try (RevWalk revWalk = new RevWalk(repository)) {
			tree = revWalk.parseCommit(ObjectId.fromString(ssh)).getTree();
		}
	}

	@Override
	public InputStream access(String uri) throws IOException {
		System.out.println(uri);
		TreeWalk walk = forPath(uri);
		ObjectId objectId = walk.getObjectId(0);
		ObjectLoader loader = repository.open(objectId);
		InputStream inputStream = loader.openStream();

		walk.close();
		return inputStream;

	}

	private TreeWalk forPath(String uri) {
		// Remove root at beginning of uri.
		uri = uri.substring("repository:/".length());

		try {
			if ("".equals(uri)) {
				TreeWalk treeWalk = new TreeWalk(repository);
				treeWalk.addTree(tree);
				treeWalk.setRecursive(false);
				treeWalk.setPostOrderTraversal(false);
				return treeWalk;
			} else
				return TreeWalk.forPath(repository, uri, tree);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isDirectory(String uri) {
		// I don't like such hacks.
		if (uri.equals("repository:/"))
			return true;
		TreeWalk walk = forPath(uri);
		boolean result = walk.isSubtree();
		walk.close();
		return result;

	}

	@Override
	public boolean isFile(String uri) {
		return !isDirectory(uri);
	}

	@Override
	public String extension(String uri) {
		return Files.getFileExtension(uri);
	}

	@Override
	public List<String> children(String uri) {
		try {
			TreeWalk walk = forPath(uri);
			walk.enterSubtree();

			List<String> result = new ArrayList<String>();
			while (walk.next())
				result.add("repository:/" + walk.getPathString());

			walk.close();
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String navigate(String uri, String relativePath) {
		if ("repository:/".equals(uri))
			return uri + relativePath;
		else
			return uri + "/" + relativePath;
	}

	@Override
	public String parent(String uri) {
		if (uri.lastIndexOf("/") == -1)
			return "repository:/";
		else
			return uri.substring(0, uri.lastIndexOf("/"));
	}

	@Override
	public String root() {
		return "repository:/";
	}
}
