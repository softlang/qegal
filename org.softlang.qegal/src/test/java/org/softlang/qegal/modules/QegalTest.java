package org.softlang.qegal.modules;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeFalse;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.any23.encoding.TikaEncodingDetector;
import org.apache.jena.ext.com.google.common.io.Files;
import org.apache.jena.rdf.model.Model;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.softlang.qegal.jutils.JUtils;
import org.softlang.qegal.utils.QegalUtils;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;

@RunWith(Parameterized.class)
public abstract class QegalTest {
	// TODO: Add something for checking exclusion respective alpha and beta phase.
	// TODO: Compare Testing also to DL things.
	private static final String SL = "http://org.softlang.com/";

	private QegalState state;

	private Model model;

	private String path;

	private String address;

	// This is initialised with parameters before the test methods are executed.
	public QegalTest(String path, String address, IModel model, QegalState state) {
		this.address = address;
		this.path = path;
		if (model != null)
			this.model = model.delegate();
		this.state = state;
	}

	/**
	 * Counting on the repository folder.
	 * 
	 * @param extension
	 *            (if "*" all files are checked)
	 * @param regex
	 * @return
	 */
	public Set<String> search(String extension, String pattern) {
		Pattern compiledPattern = Pattern.compile(pattern);
		Set<String> set = new HashSet<>();
		for (File file : Files.fileTreeTraverser().breadthFirstTraversal(new File(path))) {
			// Filtering extensions.
			String actualExtension = Files.getFileExtension(file.getName());
			if (!extension.equals("*") && !extension.equals(actualExtension))
				continue;

			// Continue if directory.
			if (file.isDirectory())
				continue;

			// Scanning files.
			//if(file.getAbsoluteFile().)

			try {
				Charset charset = guessCharset(new FileInputStream(file));

				Scanner scanner = new Scanner(file, charset.name());
				String match = "";
				int index = 0;
				while (match != null) {
					match = scanner.findWithinHorizon(compiledPattern, 0);
					if (match != null)
						set.add(file.getAbsolutePath() + "_" + String.valueOf(index++));
				}
				scanner.close();
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
		return set;
	}

	public static Charset guessCharset(InputStream is) throws IOException {
		try {
			return Charset.forName(new TikaEncodingDetector().guessEncoding(is));
		}catch(UnsupportedCharsetException e) {
			return Charset.defaultCharset();
		}
	}

	/**
	 * @param expected
	 * @param actual
	 */
	public void assertEqualSize(Set<String> expected, Set<String> actual) {
		assertEquals(
				Joiner.on("\n").join(new TreeSet<>(expected)) + " \n\n " + Joiner.on("\n").join(new TreeSet<>(actual)),
				expected.size(), actual.size());
	}

	public void whitelist(QegalState... states) {
		boolean condition = false;
		for (QegalState state : states)
			condition = condition | this.state == state;

		whitelist(condition);
	}

	public void blacklist(QegalState... states) {
		boolean condition = false;
		for (QegalState state : states)
			condition = condition | this.state == state;

		blacklist(condition);
	}

	/**
	 * Adds the sl prefix to this string.
	 * 
	 * @param name
	 * @return
	 */
	public String sl(String name) {
		return SL + name;
	}

	/**
	 * Makes relative path absolute.
	 * 
	 * @param path
	 * @return
	 */
	public String path(String path) {
		return this.path + path;
	}

	protected void whitelist(Boolean condition) {
		assumeTrue(condition);
	}

	protected void blacklist(Boolean condition) {
		assumeFalse(condition);
	}

	protected void whitelist(String address) {
		whitelist(address.equals(this.address));
	}

	protected void blacklist(String address) {
		blacklist(address.equals(this.address));
	}

	@Parameters(name = "{index} : {3} : {1}")
	public static Collection<Object[]> data() {
		List<Object[]> data = new ArrayList<>();
		// TODO: This file needs to be read out from configuration file.
		for (Map<String, String> row : JUtils.readCsv(new File(JUtils.configuration("rule_mde_workdir")+"/output.csv"))) {
			String workingdir = row.get("workingdir").replace("\\", "/");
			QegalState state = QegalState.valueOf(JUtils.read(new File(workingdir + "/state.txt"), Charsets.UTF_8));

			// Loading specifci model for state.
			IModel model = null;
			if (state == QegalState.DONE)
				model = new ModelPersisted(workingdir + "/beta_triples.ttl");
			if (state == QegalState.EXCLUDED)
				model = new ModelPersisted(workingdir + "/alpha_triples.ttl");

			String path = workingdir + "/content/";
			String address = row.get("id");

			data.add(new Object[] { path, address, model, state });
		}
		return data;
	}

	/**
	 * Count a given language's instances in the model.
	 * 
	 * @param language
	 * @return
	 */
	protected Set<String> elementsOf(String language) {
		return QegalUtils.elementsOf(model, language);
	}
	
	protected Set<String> getSubjectsWith(String property){
		return QegalUtils.getSubjectsWith(model, property);
	}
	
	protected String getSubjectWith(String property, String object) {
		return QegalUtils.getSubjectWith(model, property, object);
	}
	
	protected Set<String> getSubjectsWith(String property, String object){
		return QegalUtils.getSubjectsWith(model, property, object);
	}
	
	protected String getObjectWith(String subject, String property){
		return QegalUtils.getObjectWith(model, subject, property);
	}
	
	protected Set<String> getObjectsWith(String subject, String property){
		return QegalUtils.getObjectsWith(model, subject, property);
	}

	protected void assertTriple(String subject, String predicated, String object) {
		assertEquals(1, model.listStatements(model.getResource(subject), model.createProperty(predicated),
				model.createResource(object)).toList().size());
	}
	
	protected void assertNoTriple(String subject, String predicated, String object) {
		assertEquals(0, model.listStatements(model.getResource(subject), model.createProperty(predicated),
				model.createResource(object)).toList().size());
	}

}
