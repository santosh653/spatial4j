package org.apache.lucene.spatial.core;

import org.apache.lucene.spatial.grid.SpatialGrid;

public interface Shape
{
  /**
   * Describe the relationship between the two objects.  For example
   *
   *   this is WITHIN other
   *   this CONTAINS other
   *   this is OUTSIDE other
   *   this INTERSECTS other
   *
   * The context object is optional -- it may include spatial reference
   */
  IntersectCase intersect( Shape other, Object context );

  /**
   * Get the bounding box for this Shape
   */
  Extent getExtent();
}