package org.openpnp.model.eagle;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;

import org.openpnp.model.eagle.xml.Drawing;
import org.openpnp.model.eagle.xml.Eagle;
import org.openpnp.model.eagle.xml.Library;
import org.openpnp.model.eagle.xml.Package;
import org.openpnp.model.eagle.xml.Packages;
import org.openpnp.model.eagle.xml.Smd;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

public class EagleLoader {
    private static final String FEATURE_NAMESPACES = "http://xml.org/sax/features/namespaces";
    private static final String FEATURE_NAMESPACE_PREFIXES = "http://xml.org/sax/features/namespace-prefixes";
    
    private final URL url;
    
    public EagleLoader(URL url) {
        this.url = url;
    }
    
    public static void main(String[] args) throws Exception {
        String packageName = "org.openpnp.model.eagle.xml";

        JAXBContext ctx = JAXBContext.newInstance(packageName);
        Unmarshaller unmarshaller = ctx.createUnmarshaller();

        XMLReader xmlreader = XMLReaderFactory.createXMLReader();
        xmlreader.setFeature(FEATURE_NAMESPACES, true);
        xmlreader.setFeature(FEATURE_NAMESPACE_PREFIXES, true);
        xmlreader.setEntityResolver(new EntityResolver() {
            public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
                return new InputSource(Eagle.class.getResourceAsStream("eagle.dtd"));
            }
        });

        InputSource input = new InputSource(new FileInputStream("/Users/jason/Desktop/adafruit.lbr"));
        Source source = new SAXSource(xmlreader, input);

        Eagle eagle = (Eagle) unmarshaller.unmarshal(source);
        Drawing drawing = (Drawing) eagle.getCompatibilityOrDrawing().get(0);
        Library library = (Library) drawing.getLibraryOrSchematicOrBoard().get(0);
        Packages packages = library.getPackages();
        HashSet<Object> stuff = new HashSet<Object>();
        
        System.out.println("<openpnp-packages>");
        
        for (Package pkg : packages.getPackage()) {
            System.out.println(String.format("<package id=\"%s\" name=\"%s\">", pkg.getName(), pkg.getName()));
            System.out.println(String.format("<footprint units=\"Millimeters\">"));
            for (Object o : pkg.getPolygonOrWireOrTextOrDimensionOrCircleOrRectangleOrFrameOrHoleOrPadOrSmd()) {
                if (o instanceof Smd) {
                    Smd smd = (Smd) o;
                    System.out.println(String.format("<pad x=\"%s\" y=\"%s\" width=\"%s\" height=\"%s\"/>", smd.getX(), smd.getY(), smd.getDx(), smd.getDy()));
                }
            }
            System.out.println(String.format("</footprint>"));
            System.out.println("</package>");
        }
        
        System.out.println("</openpnp-packages>");
    }
}
