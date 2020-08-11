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
import java.util.Arrays;
import java.util.List;

import static java.lang.Float.NaN;
import static java.lang.Float.intBitsToFloat;

public class Main {

    private static boolean isclosed = false;

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
        if(path.charAt(path.length()-1)=='Z' || path.charAt(path.length()-1)=='z') isclosed = true;
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
        ArrayList<Quadrilateral> listOfQuads = new ArrayList<>();
        List<Point2D> listofPoints = new ArrayList();
        int size = 0;
        while (true){
            pathIterator.currentSegment(currentPathPos);
            Point2D A = new Point2D.Double(currentPathPos[0],currentPathPos[1]);
            if (currentPathPos[0] != 0.0 && currentPathPos[1] != 0.0) listofPoints.add(A);
            size = listofPoints.size();
            System.out.println("\ncurrent point is:"+listofPoints.get(size-1).getX()+" , "+listofPoints.get(size-1).getY());
            System.out.println("path is done :"+pathIterator.isDone());
            if (size>2){
                if (size==3){
                    listOfQuads.add(Quadrilateral.firstQuadrilateral(listofPoints.get(size-3),listofPoints.get(size-2),listofPoints.get(size-1),width));
                }
                else {


                if (pathIterator.isDone()){
                    if(isclosed) {
                        System.out.println("path is closed");
                        listOfQuads.remove(0); //remove first quad that is not calculated properly
                        listofPoints.remove(size-1);
                        size --;
                        System.out.println(Arrays.deepToString(listofPoints.toArray()));
                        listOfQuads.add(Quadrilateral.nextQuadrilateral(listOfQuads.get(listOfQuads.size()-1),listofPoints.get(size-3),listofPoints.get(size-2),listofPoints.get(size-1),width));
                        listOfQuads.add(Quadrilateral.nextQuadrilateral(listOfQuads.get(listOfQuads.size()-1),listofPoints.get(size-2),listofPoints.get(size-1),listofPoints.get(0),width));
                        listOfQuads.add(Quadrilateral.nextQuadrilateral(listOfQuads.get(listOfQuads.size()-1),listofPoints.get(size-1),listofPoints.get(0),listofPoints.get(1),width));
                        listOfQuads.add(Quadrilateral.nextQuadrilateral(listOfQuads.get(listOfQuads.size()-1),listofPoints.get(0),listofPoints.get(1),listofPoints.get(2),width));
                    }
                    else {
                        System.out.println("path is not closed");
                        listOfQuads.add(Quadrilateral.lastQuadrilateral(listOfQuads.get(listOfQuads.size()-1),listofPoints.get(size-2),listofPoints.get(size-1),width));
                    }
                    break;
                }
                else {
                    listOfQuads.add(Quadrilateral.nextQuadrilateral(listOfQuads.get(listOfQuads.size()-1),listofPoints.get(size-3),listofPoints.get(size-2),listofPoints.get(size-1),width));
                }
            }
            }
            pathIterator.next();
        }
        return listOfQuads;
    }


    public static void main(String[] args) throws IOException {
        double width = 2;
        double spacing = 0.4;
    Shape original_shape = getShapeFromSVG("test4.svg");
    ArrayList<Quadrilateral> l = createListOfQuadrilaterals(original_shape,width);
    PathIterator pathIterator = original_shape.getPathIterator(new AffineTransform());
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
        List<Area> quadsAreas = new ArrayList<Area>();
        for (int i = 0;i<l.size();i++){
            quadsAreas.add(l.get(i).getArea());
        }
        for (int index = 0;index<quadsAreas.size();index++){
            Area quadrilateral = quadsAreas.get(index);
                for (int i=0;i<quadsAreas.size();i++){
                    List<Integer> nonCheckedAreas = new ArrayList<>();
                    if (isclosed){
                        nonCheckedAreas.addAll(Arrays.asList(((index-1)%l.size()+l.size())%l.size(),index,(index+1)%l.size()));
                    }
                    else {
                        if(i>=2){
                            nonCheckedAreas.addAll(Arrays.asList(index,index-1));
                        }
                        else {
                            nonCheckedAreas.addAll(Arrays.asList(0,1));
                        }
                    }
                    if(!nonCheckedAreas.contains(i)){
                        Area toCutOff = new Area(quadsAreas.get(i));
                        toCutOff.intersect(quadrilateral);
                        if(!toCutOff.isEmpty()){
                            Area largerQuad = new Area(l.get(index).changeQuadrilateralWidth(spacing).getArea());
                            Area cuttedArea = new Area(quadsAreas.get(i));
                            cuttedArea.subtract(largerQuad);
                            quadsAreas.set(i,cuttedArea);
                            //final_Area.add(quadsAreas.get(index-1));
                            //final_Area.add(quadrilateral.getArea());
                        }
                    }

                    else {
                        //final_Area.add(quadrilateral.getArea());
                    }
                }
            //else {
                //final_Area.add(quadrilateral.getArea());
            //}


            //final_shape.add(new Area());
        }
        for (Area quad : quadsAreas){
            final_Area.add(quad);
        }
        if(isclosed){
            /*
            not working as intended : create unwanted cuts or remove them, see todo ^^
            final_Area.subtract(l.get(0).changeQuadrilateralWidth(spacing).getArea());
            final_Area.add(tmp);

             */
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