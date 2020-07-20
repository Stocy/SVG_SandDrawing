package com.company;


import Jama.Matrix;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Quadrilateral {
        private List<Point2D> coordinates;
        private double width;

        Quadrilateral(double width, List<Point2D> point2DList){
            this.coordinates = point2DList;
            this.width = coordinates.get(0).distance(coordinates.get(1));

        }



        Quadrilateral(Quadrilateral previousQuad,Point2D point_3,Point2D point_4){
            this.coordinates = new ArrayList<>();
            this.coordinates.add(previousQuad.getCoordinates().get(3));
            this.coordinates.add(previousQuad.getCoordinates().get(2));
            this.coordinates.add(point_3);
            this.coordinates.add(point_4);
        }

        static Quadrilateral firstQuadrilateral(Point2D A, Point2D B,Point2D C, double width){
            List<Point2D> result = new ArrayList<>();
            List<Point2D> first_Two = createLeftandRightPoints_atA(A,B,2*width);
            List<Point2D> last_Two = createAverageLeftandRight_atB(A,B,C,width);
            result.addAll( Arrays.asList(first_Two.get(0),first_Two.get(1),last_Two.get(1),last_Two.get(0)) );
            return new Quadrilateral(width,result);
        }

        static Quadrilateral nextQuadrilateral(Quadrilateral previous,Point2D A, Point2D B,Point2D C, double width){
            List<Point2D> last_Two = createAverageLeftandRight_atB(A,B,C,width);
            return new Quadrilateral(previous,last_Two.get(0),last_Two.get(1));
        }

        static Quadrilateral lastQuadrilateral(Quadrilateral previous,Point2D A,Point2D lastPoint,double width){
            List<Point2D> last_two = createLeftandRightPoints_atA(lastPoint, A,2*width);
            return new Quadrilateral(previous,last_two.get(1),last_two.get(0));
        }

        static List<Point2D> createLeftandRightPoints_atA(Point2D A, Point2D B, double width){
            //TODO >> simplifier
        double d = width/2;
        double factor = Math.sqrt(Math.pow(d,2)/(Math.pow(B.getX()- A.getX(),2)+Math.pow(B.getY()- A.getY(),2)));
        Point2D normVect = getNormalVector(A,B);
        double fact = getNormFactor(width,normVect);
        double leftX = A.getX() + factor*(-(B.getY()-A.getY()));
        double leftY = A.getY() + factor*(B.getX()-A.getX());
        double rightX = A.getX() - factor*(-(B.getY()-A.getY()));
        double rightY = A.getY() - factor*(B.getX()-A.getX());
        Point2D leftPoint = new Point2D.Double(leftX,leftY);
        Point2D rightPoint = new Point2D.Double(rightX,rightY);
        List<Point2D> l = new ArrayList<>();
        l.add(leftPoint);
        l.add(rightPoint);
        return l;
    }
    static Point2D getSlopeVector(Point2D A,Point2D B){
        return new Point2D.Double(B.getX()-A.getX(),B.getY()-A.getY());
    }

    static Point2D getNormalVector(Point2D A,Point2D B){
            Point2D slope = getSlopeVector(A,B);
            return new Point2D.Double(slope.getY(),-slope.getX());
    }

    static double getNormFactor(double width_norm,Point2D vector){
            return width_norm/getNorm(vector);
    }

    static Point2D Translate(Point2D A,Point2D B){
            return new Point2D.Double(A.getX()+B.getX(),A.getY()+B.getY());
    }

    static Point2D Scale(double factor,Point2D vector){
            return new Point2D.Double(factor*vector.getX(),factor*vector.getY());
    }

    static Point2D setNorm(double norm,Point2D vector){
            double factor = getNormFactor(norm,vector);
            return Scale(factor,vector);
    }

    static double getNorm(Point2D vector){
        return Math.sqrt(Math.pow(vector.getX(),2)+Math.pow(vector.getY(),2));
    }

    static Point2D Negate(Point2D vector){
            return new Point2D.Double(-vector.getX(),-vector.getY());
    }

    static Line2D line2DFromPoints(Point2D a,Point2D b){
            Line2D l = new Line2D.Double();
            l.setLine(a,b);
            return l;
    }

    static Point2D getNormalVector(Point2D vect){
            return new Point2D.Double(vect.getY(),-vect.getX());
    }
    static Line2D line2D_NormalFromVect(Point2D vect){
            Line2D l = new Line2D.Double();
            l.setLine(vect,Translate(vect,getNormalVector(vect)));
            return l;
    }

    static double[] getAandB_oflineEquation(Line2D line2D){
            Point2D slope = getSlopeVector(line2D.getP1(),line2D.getP2());
            double A = slope.getY()/slope.getX();
            double B = line2D.getP2().getY()-A*line2D.getP2().getX();
            double[] result = new double[2];
            result[0] = A;
            result[1] = B;
            return result;
    }

    static boolean iscoolinear(Point2D vect_a,Point2D vect_b){
        double factor = vect_a.getX()/vect_b.getX();
        return vect_b.getY()*factor == vect_a.getY();
    }

    static boolean hasSameDirection(Point2D vect_a,Point2D vect_b){
            return iscoolinear(vect_a, vect_b) && (vect_a.getX() > 0 && (vect_b.getX() > 0));
    }

    static Point2D getTurnVector(Point2D u,Point2D v){
            if (iscoolinear(u,v)){
                if (!hasSameDirection(u,v)){
                  //TODO PENTAGON
                }
                return u;

            }
            double[] lu = getAandB_oflineEquation(line2D_NormalFromVect(u));
            double[] lv = getAandB_oflineEquation(line2D_NormalFromVect(v));
            double[][] lhs = {{1,-lu[0]},{1,-lv[0]}};
            double[] rhs = {lu[1],lv[1]};
            Matrix matrix = new Matrix(lhs);
            Matrix m = new Matrix(rhs,2);
            Matrix solve = matrix.solve(m);
            System.out.println("turn vector : "+Arrays.deepToString(solve.getArray()));
            return new Point2D.Double(solve.get(0,0),solve.get(1,0));
    }
//DO NOT WORK AS INTEDENDED USE SHAPE INTERSECTION INSTEAD
    boolean contains(Quadrilateral quadrilateral){
            Shape a = this.getQuadShape();
            Shape b = this.getQuadShape();
            for(int i = 0;i<4;i++){
                if (a.contains(quadrilateral.coordinates.get(i)) || b.contains(this.coordinates.get(i))){
                    return true;
                }
            }
            return false;
    }





        static List<Point2D> createAverageLeftandRight_atB(Point2D A,Point2D B,Point2D C,double width){
            double d = width/2;
            Point2D ABnormal = setNorm(width,getNormalVector(A,B));
            Point2D BCnormal = setNorm(width,getNormalVector(B,C));
            double factorAB = getNormFactor(width,ABnormal);
            double factorBC = getNormFactor(width,BCnormal);
            Point2D turnCorrect = getTurnVector(ABnormal,BCnormal);
            Point2D averageVect = setNorm(getNorm(turnCorrect),Translate(ABnormal,BCnormal));

            Point2D leftAVG =Translate(B,averageVect);
            Point2D rightAVG = Translate(B,Negate(averageVect));
            Point2D correctLeft = Translate(B,turnCorrect);
            Point2D correctRight = Translate(B,Negate(turnCorrect));
            return Arrays.asList(rightAVG,leftAVG);
            //return Arrays.asList(correctRight,correctLeft);
        }

    GeneralPath getQuadShape(){
            GeneralPath quad = new GeneralPath();
            quad.moveTo(this.getX(0),this.getY(0));
            for (int i = 1;i<4;i++){
                quad.lineTo(this.getX(i),this.getY(i));
            }
            return quad;
    }

    double getX(int point_index){
        return this.coordinates.get(point_index).getX();
    }
    double getY(int point_index){
        return this.coordinates.get(point_index).getY();
    }

    public Area getArea(){
            return new Area(this.getQuadShape());
    }

    public Quadrilateral changeQuadrilateralWidth(double extra_thickness){
            List<Point2D> points = new ArrayList<>();
            Point2D translate_vect = setNorm(extra_thickness,getNormalVector(this.coordinates.get(1),this.coordinates.get(2)));
            points.addAll(Arrays.asList(
                    Translate(translate_vect,this.coordinates.get(0)),
                    Translate(Negate(translate_vect),this.coordinates.get(1)),
                    Translate(Negate(translate_vect),this.coordinates.get(2)),
                    Translate(translate_vect,this.coordinates.get(3))));

            Quadrilateral result = new Quadrilateral(this.width+extra_thickness,points);
            //result.translate_quad(Negate(translate_vect));
            return result;
    }

    public void translate_quad(Point2D translationVector){
            for(Point2D p:this.coordinates){
                Translate(p,translationVector);
            }
    }






        public double getWidth() {
            return width;
        }

        public List<Point2D> getCoordinates() {
            return this.coordinates;
        }

    public static void main(String[] args) {
            List<Point2D> ctest = Arrays.asList(new Point2D.Double(0,0),new Point2D.Double(0,1),
                    new Point2D.Double(1,1),new Point2D.Double(1,0));
        Quadrilateral test = new Quadrilateral(200, ctest);
        Shape stest = test.getQuadShape();
        double[] coords = new double[6];
        PathIterator ptest = stest.getPathIterator(new AffineTransform());
        while(!ptest.isDone()) {
            ptest.currentSegment(coords);
            System.out.println(coords[0]+"  :  "+coords[1]);
            ptest.next();
        }
        Point2D a = getTurnVector(new Point2D.Double(0,5),new Point2D.Double(4.33,2.3));
        //getTurnVector(new Point2D.Double(0,5),new Point2D.Double(0,6));
        System.out.println();
        System.out.println(iscoolinear(new Point2D.Double(2,2),new Point2D.Double(-3,-3)));
        System.out.println(hasSameDirection(new Point2D.Double(2,2),new Point2D.Double(-3,-3)));

    }
    }



