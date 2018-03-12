package org.softlang.qegal.jutils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.FetchResult;

/**
 * Created by Johannes on 12.10.2017.
 */
public class Gits {

	public static Git collect(List<Exception> exceptions, String gitAddress, boolean bare) {
		return collect(exceptions, gitAddress, JUtils.configuration("temp") + "/gitrepos/" + gitAddress, bare);
	}

	public static Git collect(String gitAddress, String target, boolean bare) {
		return collect(new ArrayList<>(), gitAddress, target, bare);
	}

	public static Git collect(String gitAddress, boolean bare) {
		return collect(new ArrayList<>(), gitAddress, bare);
	}

	public static Git collect(List<Exception> exceptions, String gitAddress, String target, boolean bare) {
		String url = "https://github.com/" + gitAddress;
		File data = new File(target);
		Git git = null;

		// Test if repo hast to be checked out.
		if (!data.exists()) {
			try {
				// Clone it.
				if (bare)
					git = Git.cloneRepository().setBare(bare).setGitDir(data).setURI(url).call();
				else
					git = Git.cloneRepository().setBare(false).setDirectory(data).setURI(url).call();

			} catch (Exception e) {
				exceptions.add(e);
				git = null;
			}
		} else {
			// Regular access to the repository.
			try {
				if (bare)
					git = Git.init().setBare(bare).setGitDir(data).call();
				else
					git = Git.init().setBare(bare).setDirectory(data).call();
			} catch (GitAPIException e) {
				git = null;
				exceptions.add(e);
			}
		}

		// Update repository if necessary.
		// if (git != null) {
		// try {
		// FetchResult fetch = git.fetch().call();
		// if (!bare)
		// git.pull().call();
		//
		// if (!bare) {
		// Status status = git.status().call();
		//
		// if (!status.isClean()) {
		// exceptions.add(new Exception("Not clean"));
		// git = null;
		// }
		// }
		//
		// } catch (GitAPIException e) {
		// exceptions.add(e);
		// }
		// }

		return git;
	}
}
