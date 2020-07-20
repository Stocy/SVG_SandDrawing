package com.company;

import org.apache.batik.anim.dom.SAXSVGDocumentFactory;
import org.apache.batik.parser.AWTPathProducer;
import org.apache.batik.parser.PathParser;
import org.apache.batik.svggen.SVGGraphics2D;
import org.apache.batik.util.XMLResourceDescriptor;
import org.w3c.dom.Document;
import org.w3c.dom.svg.SVGDocument;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.awt.geom.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Main {

    private static SVGDocument loadSVGDocument(String uri) {
        String parser = XMLResourceDescriptor.getXMLParserClassName();
        SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
        SVGDocument svgDocument = null;
        try {
            //svgDocument = factory.createSVGDocument(IMAGE_SVG);
            svgDocument = factory.createSVGDocument(uri);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return svgDocument;
    }

    static private Shape getShapeFromSVG(String file_name){
        SVGDocument svg_in = loadSVGDocument(file_name);
        SVGGraphics2D svgGraphics2D_in = new SVGGraphics2D(svg_in);
        System.out.println("width :" +svg_in.getRootElement().getAttribute("width"));
        String width = svg_in.getRootElement().getAttribute("width");
        String height = svg_in.getRootElement().getAttribute("height");
        if (width.equals("") || height.equals("")){
            width="1000px";
            height="1000px";
        }
        String path = svg_in.getElementsByTagName("path").item(0).getAttributes().getNamedItem("d").getTextContent();
        System.out.println("PATH TEXT :" + path);
        AWTPathProducer pathProducer = new AWTPathProducer();
        PathParser pathParser = new PathParser();
        pathParser.setPathHandler(pathProducer);
        pathParser.parse(path);
        Shape ss = pathProducer.getShape();
        return ss;
    }

    static private SVGGraphics2D createSvgGraphics2D(){
        Document svg_out = null;
        DocumentBuilderFactory factory = null;

        try {
            factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            svg_out = builder.newDocument();
        } catch (Exception e) {
            e.printStackTrace();
        }
        SVGGraphics2D svgGraphics2D_out = new SVGGraphics2D(svg_out);
        return svgGraphics2D_out;

    }

    static private void export_SvgGraphics2D(SVGGraphics2D svgGraphics2D,String filename) throws IOException {
        new FileOutputStream("out.svg").close();
        OutputStream fileOutputStream = new FileOutputStream(filename);
        Writer out = new OutputStreamWriter(fileOutputStream, "UTF-8");
        svgGraphics2D.stream(out);
    }

    static private ArrayList<Quadrilateral> createListOfQuadrilaterals(Shape original_shape, double width){
        PathIterator pathIterator = original_shape.getPathIterator(new AffineTransform());
        double[] currentPathPos = new double[6];
        ArrayList<Quadrilateral> l = new ArrayList<>();
        List<Point2D> listofPoints = new ArrayList();
        int size = 0;
        while (true){
            pathIterator.currentSegment(currentPathPos);
            Point2D A = new Point2D.Double(currentPathPos[0],currentPathPos[1]);
            listofPoints.add(A);
            size ++;
            if (size>2){
                if (size==3){
                    l.add(Quadrilateral.firstQuadrilateral(listofPoints.get(size-3),listofPoints.get(size-2),listofPoints.get(size-1),width));
                }
                if (pathIterator.isDone()){
                    l.add(Quadrilateral.lastQuadrilateral(l.get(l.size()-1),listofPoints.get(size-3),listofPoints.get(size-2),width));
                    break;
                }
                else {
                    l.add(Quadrilateral.nextQuadrilateral(l.get(l.size()-1),listofPoints.get(size-3),listofPoints.get(size-2),listofPoints.get(size-1),width));
                }
            }
            pathIterator.next();
        }
        return l;
    }


    public static void main(String[] args) throws IOException {
        double width = 2;
        double spacing = 1;
    Shape original_shape = getShapeFromSVG("test3.svg");
    ArrayList<Quadrilateral> l = createListOfQuadrilaterals(original_shape,width);
    PathIterator pathIterator = original_shape.getPathIterator(new AffineTransform());
    //TODO CLOSED LOOPS
    /*
    double[] currentPathPos = new double[6];
    List<Point2D> listofPoints = new ArrayList();
    int size = 0;
    while (true){
        pathIterator.currentSegment(currentPathPos);
        Point2D A = new Point2D.Double(currentPathPos[0],currentPathPos[1]);
        listofPoints.add(A);
        size ++;
        if (size>2){
            if (size==3){
                l.add(Quadrilateral.firstQuadrilateral(listofPoints.get(size-3),listofPoints.get(size-2),listofPoints.get(size-1),width));
            }
            if (pathIterator.isDone()){
                l.add(Quadrilateral.lastQuadrilateral(l.get(l.size()-1),listofPoints.get(size-3),listofPoints.get(size-2),width));
                break;
            }
            else {
                l.add(Quadrilateral.nextQuadrilateral(l.get(l.size()-1),listofPoints.get(size-3),listofPoints.get(size-2),listofPoints.get(size-1),width));
            }
        }
        pathIterator.next();
    }

     */

        SVGGraphics2D svgGraphics2D_out = createSvgGraphics2D();
        svgGraphics2D_out.setSVGCanvasSize(new Dimension(500,500));


        Area final_Area = new Area();
        for (Quadrilateral quadrilateral : l){
            int index = l.indexOf(quadrilateral);
            if (index>1){
                for (int i=0;i<=index-2;i++){
                    Area tocut = l.get(i).getArea();
                    tocut.intersect(quadrilateral.getArea());
                    if(!tocut.isEmpty()){
                        Area a = new Area(quadrilateral.changeQuadrilateralWidth(0.2).getArea());
                        final_Area.subtract(a);
                        final_Area.add(quadrilateral.getArea());
                        final_Area.add(l.get(index-1).getArea());
                    }
                    else {
                        final_Area.add(quadrilateral.getArea());
                    }
                }
            }
            else {
                final_Area.add(quadrilateral.getArea());
            }
            //final_shape.add(new Area());
        }

        svgGraphics2D_out.fill(final_Area);


        //s.fill(r);
        export_SvgGraphics2D(svgGraphics2D_out,"out.svg");
        /*
        new FileOutputStream("out.svg").close();
        OutputStream fileOutputStream = new FileOutputStream("out.svg");
        Writer out = new OutputStreamWriter(fileOutputStream, "UTF-8");
        svgGraphics2D_out.stream(out);

         */

        //SVGGraphics2D o = new SVGGraphics2D(out);
        //o.create();


    }
}