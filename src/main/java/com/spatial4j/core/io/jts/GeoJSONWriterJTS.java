package com.spatial4j.core.io.jts;

import java.io.IOException;
import java.io.Writer;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import org.noggit.JSONParser;

import com.spatial4j.core.context.SpatialContextFactory;
import com.spatial4j.core.context.jts.JtsSpatialContext;
import com.spatial4j.core.io.GeoJSONReader;
import com.spatial4j.core.io.GeoJSONWriter;
import com.spatial4j.core.io.LegacyShapeReadWriterFormat;
import com.spatial4j.core.shape.Shape;
import com.spatial4j.core.shape.jts.JtsGeometry;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.PackedCoordinateSequenceFactory;

public class GeoJSONWriterJTS extends GeoJSONWriter {

  protected final JtsSpatialContext ctx;
  
  public GeoJSONWriterJTS(JtsSpatialContext ctx,  SpatialContextFactory factory) {
    super(ctx, factory);
    this.ctx = ctx;
  }

  //--------------------------------------------------------------
  // Write JTS To GeoJSON
  //--------------------------------------------------------------
  
  protected void write(Writer output, NumberFormat nf, Coordinate coord) throws IOException {
    output.write('[');
    output.write(nf.format(coord.x));
    output.write(',');
    output.write(nf.format(coord.y));
    output.write(']');
  }

  protected void write(Writer output, NumberFormat nf, CoordinateSequence coordseq) throws IOException {
    output.write('[');
    int dim = coordseq.getDimension();
    for (int i = 0; i < coordseq.size(); i++) {
      if(i>0) {
        output.write(',');
      }
      output.write('[');
      output.write(nf.format(coordseq.getOrdinate(i, 0)));
      output.write(',');
      output.write(nf.format(coordseq.getOrdinate(i, 1)));
      if(dim>2) {
        double v = coordseq.getOrdinate(i, 2);
        if (!Double.isNaN(v)) {
          output.write(',');
          output.write(nf.format(v));
        }
      }
      output.write(']');
    }
    output.write(']');
  }

  protected void write(Writer output, NumberFormat nf, Coordinate[] coord) throws IOException {
    output.write('[');
    for(int i=0;i<coord.length; i++) {
      if(i>0) {
        output.append(',');
      }
      write(output,nf,coord[i]);
    }
    output.write(']');
  }

  protected void write(Writer output, NumberFormat nf, Polygon p) throws IOException {
    output.write('[');
    write(output, nf, p.getExteriorRing().getCoordinateSequence());
    for (int i = 0; i < p.getNumInteriorRing(); i++) {
      output.append(',');
      write(output, nf, p.getInteriorRingN(i).getCoordinateSequence());
    }
    output.write(']');
  }

  public void write(Writer output, Geometry geom) throws IOException{
    NumberFormat nf = LegacyShapeReadWriterFormat.makeNumberFormat(6);
    if(geom instanceof Point) {
      Point v = (Point)geom;
      output.append("{\"type\":\"Point\",\"coordinates\":");
      write(output,nf,v.getCoordinateSequence());
      output.append("}");
      return;
    }
    else if(geom instanceof Polygon) { 
      output.append("{\"type\":\"Polygon\",\"coordinates\":");
      write(output,nf,(Polygon)geom);
      output.append("}");
      return;
    }
    else if(geom instanceof LineString) {
      LineString v = (LineString)geom;
      output.append("{\"type\":\"LineString\",\"coordinates\":");
      write(output,nf,v.getCoordinateSequence());
      output.append("}");
      return;
    }
    else if(geom instanceof MultiPoint) {
      MultiPoint v = (MultiPoint)geom;
      output.append("{\"type\":\"MultiPoint\",\"coordinates\":");
      write(output,nf,v.getCoordinates());
      output.append("}");
      return;
    }
    else if(geom instanceof MultiLineString) {
      MultiLineString v = (MultiLineString)geom;
      output.append("{\"type\":\"MultiLineString\",\"coordinates\":[");
      for(int i=0; i<v.getNumGeometries(); i++) {
        if(i>0) {
          output.append(',');
        }
        write(output,nf,v.getGeometryN(i).getCoordinates());
      }
      output.append("]}");
    }
    else if(geom instanceof MultiPolygon) {
      MultiPolygon v = (MultiPolygon)geom;
      output.append("{\"type\":\"MultiPolygon\",\"coordinates\":[");
      for(int i=0; i<v.getNumGeometries(); i++) {
        if(i>0) {
          output.append(',');
        }
        write(output,nf,(Polygon)v.getGeometryN(i));
      }
      output.append("]}");
    }
    else if(geom instanceof GeometryCollection) {
      GeometryCollection v = (GeometryCollection)geom;
      output.append("{\"type\":\"GeometryCollection\",\"geometries\":");
      for(int i=0; i<v.getNumGeometries(); i++) {
        write(output, v.getGeometryN(i));
      }
      output.append("]}");
    }
    else {
      throw new UnsupportedOperationException("unknown: "+geom);
    }
  }

  public void write(Writer output, Shape shape) throws IOException {
    if(shape==null) {
      throw new NullPointerException("Shape can not be null");
    }
    if(shape instanceof JtsGeometry) {
      write( output, ((JtsGeometry)shape).getGeom() );
      return;
    }
    super.write(output, shape);
  }
}
