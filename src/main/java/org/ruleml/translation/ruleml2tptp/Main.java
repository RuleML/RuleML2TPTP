package org.ruleml.translation.ruleml2tptp;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.annotation.Arg;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;

/**
 * @author edmonluan@gmail.com (Meng Luan)
 */
public class Main {

    private static class Options {

        @Arg(dest = "input")
        public String input;

        @Arg(dest = "output")
        public String output;
    }

    private static final String DUMMY_XML = "<?xml version='1.0'?><blank/>";
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

    private static SAXTransformerFactory transFactory;

    public static void main(final String args[]) {
        final Options opts = parseArgs(args);

        try {
            transFactory = (SAXTransformerFactory) (SAXTransformerFactory.newInstance());
        } catch (TransformerFactoryConfigurationError err) {
            throw new IllegalStateException(err);
        }

        final Translator translator = new Translator(transFactory);
        translator.loadTemplates();
        try {
            if (opts.input.equals("-")) {
                if (opts.output.equals("-")) {
                    translate(translator, System.in, System.out);
                } else {
                    translate(translator, System.in, new FileOutputStream(opts.output));
                }
            } else if (opts.output.equals("-")) {
                translate(translator, new FileInputStream(opts.input), System.out);
            } else {
                translate(translator, new FileInputStream(opts.input), new FileOutputStream(opts.output));
            }
        } catch (TransformerException ex) {
            System.err.println(ex.getMessageAndLocation());
            System.err.println();
            System.exit(EC_TRANSFORM);
        } catch (IOException ex) {
            System.err.println("Failed to operate file: " + ex.getLocalizedMessage());
            System.err.println();
            System.exit(EC_GENERAL);
        }
    }

    private static Options parseArgs(final String[] args) {
        final ArgumentParser parser = ArgumentParsers.newArgumentParser("ruleml2tptp")
            .description("Translate RuleML into TPTP.").version("RuleML2TPTP 2.0.1-SNAPSHOT");
        parser.addArgument("--version").action(Arguments.version()).help("print version");
        parser.addArgument("input").metavar("<input>").nargs("?").setDefault("-").help("input file path or \"-\" for standard input");
        parser.addArgument("output").metavar("<output>").nargs("?").setDefault("-").help("output file path or \"-\" for standard output");
        Options opts = new Options();
        try {
            parser.parseArgs(args, opts);
        } catch (final ArgumentParserException ex) {
            parser.handleError(ex);
            System.exit(EC_GENERAL);
        }
        return opts;
    }

    private static void translate(Translator translator, InputStream in, OutputStream out)
        throws TransformerException {
        translator.translate(new StreamSource(in), new StreamResult(out));
    }
}
