//general grid class. 


class Grid{
char[][] cell;

//constructors--------------------------------------------------
Grid(int _depth){
  cell = new char[_depth][_depth];
}

Grid(int _xdepth, int _ydepth) {
  cell = new char[_xdepth][_ydepth];
}

Grid(char[][] _cell) {
  cell = new char[_cell.length][_cell[0].length];
  for (int x=0; x<_cell.length; x++)
  for (int y=0; y<_cell[0].length; y++)
    cell[x][y] = _cell[x][y];
}

Grid(Grid _other) {
  cell = new char[_other.cell.length][_other.cell[0].length];
  for (int x=0; x<_other.cell.length; x++)
  for (int y=0; y<_other.cell[0].length; y++)
    cell[x][y] = _other.cell[x][y];
}

Grid(String _filename) {
  //all of the lines in the file must have the same length
  String[] strings = loadStrings(_filename);
  cell = new char[strings.length][strings[0].length()];
  
  for (int x=0; x<strings[0].length(); x++)
  for (int y=0; y<strings.length; y++)
    cell[x][y]= strings[y].charAt(x);
}

//----------------------------------------------------------------------
void fill(char _c) {
  for (int x=0; x<cell.length; x++)
  for (int y=0; y<cell[0].length; y++)
    cell[x][y] = _c;
}

//------------------------------------------------------------------------
void fill(char _c, int _x1, int _y1, int _x2, int _y2) {
  for (int x=_x1; x<=_x2; x++)
  for (int y=_y1; y<=_y2; y++)
    cell[x][y] = _c;
}


//------------------------------------------------------------------------
void noise(char[] _values) {
  for (int x=0; x<cell.length; x++)
  for (int y=0; y<cell[0].length; y++)
    cell[x][y] = _values[(int)random(_values.length)];
}

//------------------------------------------------------------------------
void noise(char[] _values, int _x1, int _y1, int _x2, int _y2) {
  for (int x=_x1; x<=_x2; x++)
  for (int y=_y1; y<=_y2; y++)
    cell[x][y] = _values[(int)random(_values.length)];
}

//------------------------------------------------------------------------
void insert(Grid _other, int _x, int _y) {
  //copies _other into this grid, overwriting whatever values are present
  for (int x=0; x<_other.cell.length; x++)
  for (int y=0; y<_other.cell[0].length; y++)
    cell[x+_x][y+_y] = _other.cell[x][y];
  //does not check that _other will actually fit into this grid  
}

//------------------------------------------------------------------------
void merge(Grid _other, char[] _alpha, int _x, int _y){
  //copies _other into this grid
  //except where the existing cell is one of the values in _alpha
  for (int x=0; x<_other.cell.length; x++)
  for (int y=0; y<_other.cell[0].length; y++) {
    boolean docontinue = false;
    for (int i=0; i<_alpha.length; i++)
      if (cell[x+_x][y+_y] == _alpha[i]) docontinue = true;
    if (docontinue) continue;
    cell[x+_x][y+_y] = _other.cell[x][y];
  }
}

//------------------------------------------------------------------------
void replace(char[] _replace, char _fill, int _x1, int _y1, int _x2, int _y2) {
  for (int x=_x1; x<=_x2; x++)
  for (int y=_y1; y<=_y2; y++)
  for (int i=0; i<_replace.length; i++)
    if (cell[x][y] == _replace[i]) cell[x][y] = _fill;
}

//------------------------------------------------------------------------
void floodfill(char _fill, char _replace, int _x, int _y){
  cell[_x][_y] = _fill;

  if (has(_replace, _x-1 ,_y)) floodfill(_fill, _replace, _x-1, _y);
  if (has(_replace, _x+1, _y)) floodfill(_fill, _replace, _x+1, _y);
  if (has(_replace, _x, _y-1)) floodfill(_fill, _replace, _x, _y-1);
  if (has(_replace, _x, _y+1)) floodfill(_fill, _replace, _x, _y+1);
}

//------------------------------------------------------------------------
void floodfill(char _fill, int _x, int _y){
  char replace = cell[_x][_y];
  if (replace != _fill)
    floodfill(_fill, replace, _x, _y);
}

//------------------------------------------------------------------------
boolean exists (int _x, int _y) {
 
  //check for out of bounds
  if (_x<0) return false;
  if (_x>cell.length-1) return false;
  if (_y<0) return false;
  if (_y>cell[0].length-1) return false;
  else return true;

}

//------------------------------------------------------------------------
boolean has (char[] _c) {
  //true if anyy value in _c exists somewhere in the grid
  for (int x=0; x<cell.length; x++)
  for (int y=0; y<cell[0].length; y++)
  for (int i=0; i<_c.length; i++)
    if (cell[x][y] == _c[i]) return true;
  
  return false;
}

//------------------------------------------------------------------------
boolean has (char _c, int _x, int _y) {
  //true if [_x][_y] exists and contains the value _c
  
  //check for out of bounds  
  if (_x<0) return false;
  if (_x>cell.length-1) return false;
  if (_y<0) return false;
  if (_y>cell[0].length-1) return false;
  
  //check value
  if (cell[_x][_y] == _c) return true;
  else return false;  
}

//------------------------------------------------------------------------
boolean has (char[] _c, int _x, int _y) {
  //true if [_x][_y] exists and contains one of the values in _c
  for (int i = 0; i<_c.length; i++)
    if (has (_c[i], _x, _y)) return true;
    
  return false;    
}

//------------------------------------------------------------------------
Vec2 find (char _c) {
  //returns a random cell that has _c
  Vec2[] points = new Vec2[0];
  for (int x=0; x<cell.length; x++) 
  for (int y=0; y<cell[0].length; y++)
    //actually it find all the cells that have _c and picks one
    if (cell[x][y] == _c)
      points = (Vec2[]) append(points, new Vec2(x,y));
      //println(points.length);
  return points[(int)random(points.length)];
   //probably there's a more efficient way to do this
}

Vec2 find (char[] _c) {
  //returns a random cell that has _c
  Vec2[] points = new Vec2[0];
  for (int x=0; x<cell.length; x++) 
  for (int y=0; y<cell[0].length; y++)
  for (int i=0; i< _c.length; i++)
    //actually it find all the cells that have _c and picks one
    if (cell[x][y] == _c[i])
      points = (Vec2[]) append(points, new Vec2(x,y));
      //println(points.length);
  return points[(int)random(points.length)];
   //probably there's a more efficient way to do this
}

//------------------------------------------------------------------------
Vec2 find (char _c, int _x1, int _y1, int _x2, int _y2) {
  //returns a random cell that has _c
  Vec2[] points = new Vec2[0];
  for (int x=_x1; x<=_x2; x++)
  for (int y=_y1; y<=_y2; y++)
    //actually it find all the cells that have _c and picks one
    if (cell[x][y] == _c)
      points = (Vec2[]) append(points, new Vec2(x,y));
  return points[(int)random(points.length)];
   //probably there's a more efficient way to do this
}

//------------------------------------------------------------------------
Vec2[] findall (char _c) {
  //returns all points that have _c
  Vec2[] points = new Vec2[0];
  for (int x=0; x<cell.length; x++) 
  for (int y=0; y<cell[0].length; y++)
    if (cell[x][y] == _c)
      points = (Vec2[]) append(points, new Vec2(x,y));
  return points;
}

//------------------------------------------------------------------------
Vec2[] findall (char[] _c) {
  //returns all points that have _c
  Vec2[] points = new Vec2[0];
  for (int x=0; x<cell.length; x++) 
  for (int y=0; y<cell[0].length; y++)
  for (int i=0; i<_c.length; i++)
    if (cell[x][y] == _c[i])
      points = (Vec2[]) append(points, new Vec2(x,y));
  return points;
}

//------------------------------------------------------------------------
Vec2 findnearest (char _c, int _x, int _y, int _inc) {
  float radius = _inc; 
  Vec2 result;
  while (radius < cell.length) {
    result = findinradius (_c,_x,_y,radius);
    if (result.x != -1) 
      return result;
    radius += _inc;    
  }
  
  return new Vec2(-1,-1);
}

Vec2 findinradius (char _c, int _x, int _y, float _r) {
  Vec2 center = new Vec2(_x,_y);
  int minx = ceil(max(0, _x-_r));
  int maxx = floor(min(cell.length, _x+_r));
  int miny = ceil(max(0, _y-_r));
  int maxy = floor(min(cell[0].length, _y+_r));
  
  for (int y=miny; y<=maxy; y++)   
  for (int x=minx; x<=maxx; x++) {
    Vec2 target = new Vec2(x,y);
    if (target.dist(center) < _r)
    if (has(_c, target.x,target.y))
      return target; //find.
  }
  
  return new Vec2 (-1,-1); //did not find.
}
  


//------------------------------------------------------------------------
void grow(char _fill, char _replace, int _threshold){
  //does cellular automata stuff, makes noise look less random
  Grid prev = new Grid(this); 
  
  for (int x=0; x<cell.length; x++) 
  for (int y=0; y<cell[0].length; y++) 
    cell[x][y] =  (prev.countneighbors(_fill, x, y) > _threshold)? _fill : _replace;
}

//------------------------------------------------------------------------
int countneighbors(char _find, int _x, int _y){
  int count = -1; 

  for (int x = _x-1; x<=_x+1; x++)
  for (int y = _y-1; y<=_y+1; y++)
    if (has(_find,x,y)) count++;
  
  return count;
}

//------------------------------------------------------------------------
Grid swapxy() { //must be square
  Grid r = new Grid(cell);
  for (int x=0; x<cell.length; x++)
  for (int y=0; y<cell[0].length; y++)
    cell[x][y] = r.cell[y][x];
  return r;
}

//------------------------------------------------------------------------
Grid flip(boolean _flipx, boolean _flipy) { 
  Grid r = new Grid(cell);
  for (int x=0; x<cell.length; x++)
  for (int y=0; y<cell[0].length; y++)
    r.cell[x][y] = cell[_flipx?cell.length-1-x:x][_flipy?cell.length-1-y:y];
  return r;
}

//-------------------------------------------------------------------------
Grid subset(int _x1, int _y1, int _x2, int _y2) {
  Grid r = new Grid(_x2-_x1, _y2-_y1);
  
  for (int x=0; x<r.cell.length; x++)
  for (int y=0; y<r.cell[0].length; y++)
    r.cell[x][y] = cell[_x1+x][_y1+y];
    
  return r; 
}

//-------------------------------------------------------------------------
boolean lineofsight( char[] _solid, int _x1, int _y1,int _x2, int _y2) {
  PVector p1 = new PVector(_x1, _y1);
  PVector p2 = new PVector(_x2, _y2);

  PVector unit = new PVector(p2.x,p2.y);
  unit.sub(p1);
  unit.normalize();
  
  do {
    if (has( _solid, (int)p1.x/TILE, (int)p1.y/TILE)) return false; 
    p1.add(unit);
//    stroke(purple);
//    strokeWeight(2);
//    point(p1.x,p1.y);
//    noStroke();
  }
  while(p1.dist(p2)>2);
  
//   stroke(green);
//   line(_x1,_y1,_x2,_y2);
//   noStroke();  

  
  return true;  
}

//-------------------------------------------------------------------------
void dig(char _fill, int _x1, int _y1, int _x2, int _y2) {
  //digs out a random path between two points
  //replacing all cells on the path with _fill
  int dirx = (_x2 > _x1? 1 : -1);
  int diry = (_y2 > _y1? 1 : -1);
  boolean atx, aty;
  
  do {
    cell[_x1][_y1] = _fill;
    atx = (_x1==_x2);
    aty = (_y1==_y2);

    if (random(1) > 0.5){
      if (!atx) _x1 += dirx;
    }
    else if (!aty) _y1 += diry;
  }
  while(!(atx && aty));
  
}

//-------------------------------------------------------------------------
void print(){
  String[] strings = getstrings();
  for (int i = 0; i< strings.length; i++)
    println(strings[i]);
}

//-------------------------------------------------------------------------
String[] getstrings(){
  String[] strings = new String[cell[0].length];
  for (int y=0; y<cell[0].length; y++){
    strings[y] = "";
    for (int x=0; x<cell.length; x++)
      strings[y] += cell[x][y];
  }
  return strings;
} 

}// end class


