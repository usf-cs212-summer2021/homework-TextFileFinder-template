import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests the {@link TextFileFinder} class.
 *
 * @author CS 212 Software Development
 * @author University of San Francisco
 * @version Spring 2021
 */
@TestMethodOrder(MethodName.class)
public class TextFileFinderTest {
	/** Path to directory of text files */
	public static final Path root = Path.of("src", "test", "resources", "text", "simple");

	/**
	 * Runs before any tests to make sure environment is setup.
	 */
	@BeforeAll
	public static void checkEnvironment() {
		Assumptions.assumeTrue(Files.isDirectory(root));
		Assumptions.assumeTrue(Files.exists(root.resolve("hello.txt")));
	}

	/**
	 * Tests that text extensions are detected properly.
	 */
	@Nested
	@TestMethodOrder(OrderAnnotation.class)
	public class A_TextExtensionTests {

		/**
		 * Tests files that SHOULD be considered text files.
		 *
		 * @param file the file name
		 */
		@Order(1)
		@ParameterizedTest
		@ValueSource(strings = { "animals_copy.text", "capital_extension.TXT",
				"empty.txt", "position.teXt", "words.tExT", "digits.tXt" })
		public void testIsTextFile(String file) {
			Path path = root.resolve(file);
			Assertions.assertTrue(TextFileFinder.IS_TEXT.test(path));
		}

		/**
		 * Tests files that SHOULD NOT be considered text files.
		 *
		 * @param file the file name
		 */
		@Order(2)
		@ParameterizedTest
		@ValueSource(strings = { "double_extension.txt.html", "no_extension",
				"wrong_extension.html", "dir.txt", "nowhere.txt", ".txt" })
		public void testIsNotTextFile(String file) {
			Path path = root.resolve(file);
			Assertions.assertFalse(TextFileFinder.IS_TEXT.test(path));
		}
	}

	/**
	 * Tests the directory listing.
	 */
	@Nested
	@TestMethodOrder(OrderAnnotation.class)
	public class B_FindListTests {

		/**
		 * Tests the list has the expected number of paths.
		 *
		 * @throws IOException if an I/O error occurs
		 */
		@Test
		@Order(1)
		public void testListSize() throws IOException {
			Assertions.assertEquals(14, TextFileFinder.list(root).size());
		}

		/**
		 * Tests the stream has the expected number of paths.
		 *
		 * @throws IOException if an I/O error occurs
		 */
		@Test
		@Order(2)
		public void testStreamSize() throws IOException {
			Assertions.assertEquals(14, TextFileFinder.find(root).count());
		}

		/**
		 * Tests the listing includes all of the expected paths.
		 *
		 * @throws IOException if an I/O error occurs
		 */
		@Test
		@Order(3)
		public void testPaths() throws IOException {
			Set<Path> actual = TextFileFinder.find(root).collect(Collectors.toSet());

			Set<Path> expected = new HashSet<>();
			Collections.addAll(expected, root.resolve("symbols.txt"),
					root.resolve("dir.txt").resolve("findme.Txt"),
					root.resolve("empty.txt"), root.resolve(".txt").resolve("hidden.txt"),
					root.resolve("position.teXt"), root.resolve("animals_copy.text"),
					root.resolve("digits.tXt"), root.resolve("capital_extension.TXT"),
					root.resolve("animals_double.text"),
					root.resolve("a").resolve("b").resolve("c").resolve("d").resolve("subdir.txt"),
					root.resolve("words.tExT"), root.resolve("animals.text"),
					root.resolve("hello.txt"), root.resolve("capitals.txt"));

			Assertions.assertEquals(expected, actual);
		}
	}

	/**
	 * Tests that the other find method that takes a predicate.
	 */
	@Nested
	@TestMethodOrder(OrderAnnotation.class)
	public class C_AlternateFindTests {

		/**
		 * Tests the general {@link TextFileFinder#find(Path, Predicate)} method
		 * works as expected.
		 *
		 * @throws IOException if an IO error occurs.
		 */
		@Test
		@Order(1)
		public void testMarkdown() throws IOException {
			Stream<Path> stream = TextFileFinder.find(root,
					p -> p.toString().endsWith(".md"));
			Path first = stream.findFirst().get();
			Assertions.assertEquals(root.resolve("sentences.md"), first);
		}

		/**
		 * Tests the general {@link TextFileFinder#find(Path, Predicate)} method
		 * works as expected.
		 *
		 * @throws IOException if an IO error occurs.
		 */
		@Test
		@Order(2)
		public void testHtmlFiles() throws IOException {
			Predicate<Path> html = p -> p.toString().endsWith(".html");
			
			Path[] actual = TextFileFinder.find(root, html)
					.sorted()
					.toArray(Path[]::new);
			
			Path[] expected = new Path[] { 
					root.resolve("double_extension.txt.html"),
					root.resolve("wrong_extension.html") };
			
			Assertions.assertArrayEquals(expected, actual);
		}

		/**
		 * Tests that symbolic links are working as expected.
		 *
		 * @throws IOException if an IO error occurs.
		 */
		@Test
		@Order(3)
		public void testSymbolicLinks() throws IOException {
			Path parent = root.getParent();
			Path symbolic = parent.resolve("symbolic");

			Files.deleteIfExists(symbolic);
			Files.createSymbolicLink(symbolic.toAbsolutePath(), root.toAbsolutePath());

			Stream<Path> expected = TextFileFinder.find(root);
			Stream<Path> actual = TextFileFinder.find(symbolic);

			Assertions.assertEquals(expected.count(), actual.count());
		}

	}

	/**
	 * Tests that the expected approach is taken.
	 */
	@Nested
	@TestMethodOrder(OrderAnnotation.class)
	public class D_ApproachTests {
		/*
		 * These only approximately determine if a lambda function was used and the
		 * File class was NOT used.
		 */

		/**
		 * Tests that the {@link TextFileFinder#IS_TEXT} is not an anonymous class.
		 */
		@Test
		@Order(1)
		public void testAnonymous() {
			Assertions.assertFalse(TextFileFinder.IS_TEXT.getClass().isAnonymousClass());
		}

		/**
		 * Tests that the {@link TextFileFinder#IS_TEXT} is not an enclosing class.
		 */
		@Test
		@Order(2)
		public void testEnclosingClass() {
			Assertions.assertNull(TextFileFinder.IS_TEXT.getClass().getEnclosingClass());
		}

		/**
		 * Tests that the {@link TextFileFinder#IS_TEXT} is not a synthetic class.
		 */
		@Test
		@Order(3)
		public void testSyntheticClass() {
			Assertions.assertTrue(TextFileFinder.IS_TEXT.getClass().isSynthetic());
		}

		/**
		 * Tests that the {@link TextFileFinder#IS_TEXT} is likely a lambda
		 * function.
		 */
		@Test
		@Order(4)
		public void testClassName() {
			String actual = TextFileFinder.IS_TEXT.getClass().getTypeName();
			String[] parts = actual.split("[$]+");
			Assertions.assertTrue(parts[1].contentEquals("Lambda"));
		}

		/**
		 * Tests that the java.io.File class does not appear in the implementation
		 * code.
		 *
		 * @throws IOException if an I/O error occurs
		 */
		@Test
		@Order(5)
		public void testFileClass() throws IOException {
			String source = Files.readString(
					Path.of("src", "main", "java", "TextFileFinder.java"),
					StandardCharsets.UTF_8);
			
			Assertions.assertFalse(source.contains("import java.io.File;"));
			Assertions.assertFalse(source.contains(".toFile()"));
		}
	}
}
