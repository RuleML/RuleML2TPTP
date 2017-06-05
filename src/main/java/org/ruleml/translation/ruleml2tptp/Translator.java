package org.ruleml.translation.ruleml2tptp;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Templates;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamSource;

/**
 * @author edmonluan@gmail.com (Meng Luan)
 */
public class Translator {

  private static final String XSLT_NORMALIZER_RES_PATH = "xslt/101_naffologeq_normalizer.xslt";
  private static final String XSLT_TRANSLATOR_RES_PATH = "xslt/ruleml2tptp.xslt";

  private SAXTransformerFactory transFactory;
  private Templates normalizerTemplates;
  private Templates translatorTemplates;

  public Translator(final SAXTransformerFactory transFactory) {
    if (transFactory == null) {
      throw new IllegalArgumentException();
    }
    this.transFactory = transFactory;
  }

  public synchronized void loadTemplates() {
    try {
      if (normalizerTemplates == null) {
        normalizerTemplates
            = transFactory.newTemplates(new StreamSource(getClass().getResourceAsStream(XSLT_NORMALIZER_RES_PATH)));
      }
      if (translatorTemplates == null) {
        translatorTemplates
            = transFactory.newTemplates(new StreamSource(getClass().getResourceAsStream(XSLT_TRANSLATOR_RES_PATH)));
      }
    } catch (final TransformerConfigurationException ex) {
      throw new IllegalStateException(ex);
    }
  }

  public void translate(final Source src, final Result result) throws TransformerException {
    if (src == null || result == null) {
      throw new IllegalArgumentException("Null arguments");
    }
    loadTemplates();
    try {
      final TransformerHandler transHandler = transFactory.newTransformerHandler(translatorTemplates);
      transHandler.setResult(result);
      normalizerTemplates.newTransformer().transform(src, new SAXResult(transHandler));
    } catch (final TransformerConfigurationException ex) {
      throw new IllegalStateException(ex);
    }
  }
}
