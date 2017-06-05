package org.ruleml.translation.ruleml2tptp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Properties;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.ArgumentAction;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.ArgumentType;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 * @author edmonluan@gmail.com (Meng Luan)
 */
public class Main {

  private static final String DUMMY_XML = "<?xml version='1.0'?><blank/>";
  private static final String XSLT_PROPERTIES = String.format(
      "<?xml version='1.0'?>"
      + "<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>"
      + "  <xsl:output method='text'/>"
      + "  <xsl:template match='/'>"
      + "    <xsl:text>xsl:vendor = </xsl:text>"
      + "    <xsl:value-of select=\"system-property('xsl:vendor')\"/>"
      + "    <xsl:text>%nxsl:vendor-url = </xsl:text>"
      + "    <xsl:value-of select=\"system-property('xsl:vendor-url')\"/>"
      + "    <xsl:text>%nxsl:version = </xsl:text>"
      + "    <xsl:value-of select=\"system-property('xsl:version')\"/>"
      + "  </xsl:template>"
      + "</xsl:stylesheet>");

  private static final int EC_GENERAL = 1;
  private static final int EC_TRANSFORM = 2;
  private static final Properties PROPERTIES = new Properties();
  private static SAXTransformerFactory transFactory;

  public static void main(final String args[]) throws IOException {
    try (final Reader reader
        = new InputStreamReader(Main.class.getResourceAsStream("application.properties"), StandardCharsets.UTF_8)) {
      PROPERTIES.load(reader);
    }
    try {
      transFactory = (SAXTransformerFactory) (SAXTransformerFactory.newInstance());
    } catch (TransformerFactoryConfigurationError err) {
      throw new IllegalStateException(err);
    }
    final Namespace opts = parseArgs(args);
    final Translator translator = new Translator(transFactory);
    translator.loadTemplates();
    try {
      translate(translator, getInput(opts), getOutput(opts));
    } catch (TransformerException ex) {
      System.err.println(ex.getMessageAndLocation());
      System.exit(EC_TRANSFORM);
    }
  }

  private static class XSLTVersionAction implements ArgumentAction {

    @Override
    public void run(final ArgumentParser parser, final Argument arg, final Map<String, Object> attrs, final String flag,
        final Object value) throws ArgumentParserException {
      try {
        final StringWriter writer = new StringWriter();
        transFactory.newTransformer(new StreamSource(new StringReader(XSLT_PROPERTIES)))
            .transform(new StreamSource(new StringReader(DUMMY_XML)), new StreamResult(writer));
        System.out.println(writer.toString());
      } catch (final TransformerException ex) {
        throw new IllegalStateException(ex);
      }
      System.exit(0);
    }

    @Override
    public void onAttach(final Argument arg) {
    }

    @Override
    public boolean consumeArgument() {
      return false;
    }
  }

  private static class InputStreamType implements ArgumentType<InputStream> {

    @Override
    public InputStream convert(final ArgumentParser parser, final Argument arg, final String value)
        throws ArgumentParserException {
      try {
        return new BufferedInputStream(new FileInputStream(value));
      } catch (final FileNotFoundException | SecurityException e) {
        throw new ArgumentParserException("Couldn't read input file: " + value, e, parser, arg);
      }
    }
  }

  private static class OutputStreamType implements ArgumentType<OutputStream> {

    @Override
    public OutputStream convert(final ArgumentParser parser, final Argument arg, final String value)
        throws ArgumentParserException {
      try {
        return new BufferedOutputStream(new FileOutputStream(value));
      } catch (final FileNotFoundException | SecurityException e) {
        throw new ArgumentParserException("Couldn't write to output file: " + value, e, parser, arg);
      }
    }
  }

  private static InputStream getInput(final Namespace opts) {
    final InputStream in = opts.get("input");
    return in == null ? System.in : in;
  }

  private static OutputStream getOutput(final Namespace opts) {
    final OutputStream out = opts.get("output");
    return out == null ? System.out : out;
  }

  private static Namespace parseArgs(final String[] args) {
    final ArgumentParser parser = ArgumentParsers.newArgumentParser("ruleml2tptp", true)
        .description("Translate RuleML into TPTP.")
        .version(PROPERTIES.getProperty("application.name") + " " + PROPERTIES.getProperty("application.version"))
        .defaultHelp(true);
    parser.addArgument("--version").action(Arguments.version()).help("show version and exit");
    parser.addArgument("--xslt-vendor").action(new XSLTVersionAction()).help("show XSLT vendor and exit");
    parser.addArgument("input").metavar("<input>").nargs("?").type(new InputStreamType())
        .help("input file path or standard input if not specified");
    parser.addArgument("-o", "--output").metavar("<output>").type(new OutputStreamType())
        .help("output file path or standard output if not specified");
    return parser.parseArgsOrFail(args);
  }

  private static void translate(Translator translator, InputStream in, OutputStream out) throws TransformerException {
    translator.translate(new StreamSource(in), new StreamResult(out));
  }
}
