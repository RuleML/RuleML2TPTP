package org.ruleml.translation.ruleml2tptp;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import static org.apache.commons.io.FileUtils.*;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * @author edmonluan@gmail.com (Meng Luan)
 */
@RunWith(Parameterized.class)
public class TranslatorTest {

    private static Translator translator;

    @BeforeClass
    public static void setUpClass() {
        translator = new Translator((SAXTransformerFactory) (SAXTransformerFactory.newInstance()));
        translator.loadTemplates();
    }

    @Parameters(name = "{index}: {0}")
    public static Object[] baseNames() {
        return new Object[]{
            "Atom", "Implies", "Forall", "Exists", "Equal", "And", "Or", "Expr",
            "RelFunIndVar", "comments", "Equivalent", "Neg", "iri",
            "Uniterm"
        };
    }

    private String baseName;

    public TranslatorTest(String baseName) {
        this.baseName = baseName;
    }

    @Test
    public void test() throws IOException, TransformerException, URISyntaxException {
        final File input = new File(getClass().getResource("/TranslatorTest/" + baseName + "Test.ruleml").toURI());
        final File result = File.createTempFile("TranslatorTest_" + baseName + "Test_", ".tptp.tmp");
        translator.translate(new StreamSource(input), new StreamResult(result));
        final String expected = readFileToString(
            new File(getClass().getResource("/TranslatorTest/" + baseName + "Test.tptp").toURI()),
            StandardCharsets.UTF_8);
        final String resultFilePath = result.getCanonicalPath();
        final String resultContent = readFileToString(result, StandardCharsets.UTF_8);
        assertEquals("Result file: " + resultFilePath, expected, resultContent);
        assertTrue("Failed to delete " + resultFilePath, result.delete());
    }

}
