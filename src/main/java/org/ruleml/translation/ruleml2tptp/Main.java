package org.ruleml.translation.ruleml2tptp;

import org.kohsuke.args4j.Argument;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.stream.StreamResult;

/**
 * @author edmonluan@gmail.com (Meng Luan)
 */
public class Main {

    private static final String dummyXML = "<?xml version='1.0'?><blank/>";
    private static final String xsltProperties = String.format(
          "<?xml version='1.0'?>"
        + "<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>"
        + "  <xsl:output method='text'/>"
        + "  <xsl:template match='/'>"
        + "    <xsl:text> xsl:version = </xsl:text>"
        + "    <xsl:value-of select=\"system-property('xsl:version')\"/>"
        + "    <xsl:text>%n xsl:vendor = </xsl:text>"
        + "    <xsl:value-of select=\"system-property('xsl:vendor')\"/>"
        + "    <xsl:text>%n xsl:vendor-url = </xsl:text>"
        + "    <xsl:value-of select=\"system-property('xsl:vendor-url')\"/>"
        + "  </xsl:template>"
        + "</xsl:stylesheet>");

    private static final int EC_GENERAL = 1;
    private static final int EC_TRANSFORM = 2;

    @Option(name="-h",aliases={"-?","-help"},help=true,usage="print this message")
    private boolean help;

    @Option(name="-o",aliases={"-output"},metaVar="<path>",usage="use given output path (the standard output by default)")
    private File output;

    @Option(name="-w",aliases={"-overwrite"},usage="overwrite the output file")
    private boolean overwrite;

    @Argument
    private String input;

    private SAXTransformerFactory transFactory;

    public static void main(String args[]) {
        final Main appMain = new Main();
        CmdLineParser parser = new CmdLineParser(appMain);
        try {
            parser.parseArgument(args);
        } catch (CmdLineException ex) {
            System.err.println(ex.getLocalizedMessage() + ".");
            System.err.println("Try option \"-h\" for usage.");
            System.err.println();
            System.exit(EC_GENERAL);
        }
        try {
            appMain.transFactory =
                (SAXTransformerFactory) (SAXTransformerFactory.newInstance());
        } catch (TransformerFactoryConfigurationError err) {
            throw new IllegalStateException(err);
        }
        if (appMain.help) {
            System.out.println("Usage: [options] <path>");
            System.out.println();
            System.out.println("Options:");
            parser.printUsage(System.out);
            System.out.println();
            final StringWriter noteWriter = new StringWriter();
            try {
                appMain.transFactory.newTransformer(new StreamSource(new StringReader(xsltProperties)))
                    .transform(new StreamSource(new StringReader(dummyXML)), new StreamResult(noteWriter));
            } catch (TransformerException ex) {
                throw new IllegalStateException(ex);
            }
            System.out.println("XSLT Properties:");
            System.out.println(noteWriter.toString());
            System.out.println();
            return;
        }
        appMain.run();
    }

    private void run() {
        final Translator translator = new Translator(transFactory);
        translator.loadTemplates();
        try {
            if (output != null && !overwrite && !output.createNewFile()) {
                System.err.println("The output file has existed. Use option \"-w\" to overwrite it.");
                System.exit(EC_GENERAL);
            }
            if (input == null) {
                if (output == null) {
                    translate(translator, System.in, System.out);
                } else {
                    translate(translator, System.in, new FileOutputStream(output));
                }
            } else if (output == null) {
                translate(translator, new FileInputStream(input), System.out);
            } else {
                translate(translator, new FileInputStream(input), new FileOutputStream(output));
            }

        } catch (TransformerException ex) {
            System.err.println(ex.getMessageAndLocation());
            System.err.println();
            System.exit(EC_TRANSFORM);
        } catch (IOException ex) {
            System.err.println("Failed to operate file: " + ex.getLocalizedMessage());
            System.err.println();
            System.exit(EC_TRANSFORM);
        }
    }

    private void translate(Translator translator, InputStream in, OutputStream out)
        throws TransformerException {
        translator.translate(new StreamSource(in), new StreamResult(out));
    }

}
