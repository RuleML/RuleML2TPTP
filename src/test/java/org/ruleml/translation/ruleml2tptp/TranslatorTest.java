package org.ruleml.translation.ruleml2tptp;

import java.io.IOException;
import java.io.StringWriter;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.Paths;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author edmonluan@gmail.com (Meng Luan)
 */
public class TranslatorTest {

  private static final String RULEML_FILE_NAME_SUFFIX = ".ruleml";
  private static final String TPTP_FILE_NAME_SUFFIX = ".tptp";
  private static final Translator TRANSLATOR
      = new Translator((SAXTransformerFactory) (SAXTransformerFactory.newInstance()));
  private static final PathMatcher RULEML_FILE_MATCHER
      = FileSystems.getDefault().getPathMatcher("glob:**/*" + RULEML_FILE_NAME_SUFFIX);

  @BeforeClass
  public static void setUpClass() {
    TRANSLATOR.loadTemplates();
  }

  @Test
  public void testFiles() throws IOException, URISyntaxException {
    Files.walk(Paths.get(getClass().getResource("/test/translator/fol").toURI()), FileVisitOption.FOLLOW_LINKS)
        .filter(p -> RULEML_FILE_MATCHER.matches(p))
        .forEach(p -> testInputFile(p));
  }

  private static void testInputFile(final Path path) {
    final String fileName = path.getFileName().toString();
    final String baseName = fileName.substring(0, fileName.length() - RULEML_FILE_NAME_SUFFIX.length());
    System.out.println("Running " + baseName);
    try {
      final StringWriter writer = new StringWriter();
      TRANSLATOR.translate(new StreamSource(path.toFile()), new StreamResult(writer));
      assertEquals(baseName + " failed",
          new String(Files.readAllBytes(path.resolveSibling(baseName + TPTP_FILE_NAME_SUFFIX)), StandardCharsets.UTF_8),
          writer.toString());
    } catch (final IOException | TransformerException ex) {
      throw new RuntimeException(ex);
    }
  }
}
