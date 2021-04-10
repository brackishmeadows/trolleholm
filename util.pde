void drawtile(PImage _tiles, int _u, int _v){
  //_u and _v parameters are in tiles, not pixels


  int u1 = _u*TILE;
  int u2 = _u*TILE + TILE;

  int v1 = _v*TILE;
  int v2 = _v*TILE + TILE;
  
  pushMatrix();    
    beginShape();
    texture(_tiles);
    vertex(0,0,       u1,v1);
    vertex(0,TILE,    u1,v2); 
    vertex(TILE,TILE, u2,v2); 
    vertex(TILE,0,    u2,v1); 
    endShape(CLOSE);
  popMatrix();
}

//--------------------------------------------------------------------------------------
float cycle(float value, float inc, float min, float max){
//cycle a value within a given range
  value+=inc;  
  if (value>max)
    value=min+(value-max);  
  return value;
}
