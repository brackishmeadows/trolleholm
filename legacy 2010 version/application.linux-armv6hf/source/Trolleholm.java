import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.opengl.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class Trolleholm extends PApplet {



//this still isn't finished but it's mostly there.
//move with UP, DOWN, LEFT, RIGHT.
//sword is Z, bomb is S, map is TAB.

//todo:
//hide stuff behind blocks
//..arrows
  //...rangers shoot arrows
  //sword deflects arrows
//..level switching
  //...level class
  //...stair interaction
  //return to surface
//.score
  //...displayed during game
  //hiscore on death
  //hiscore from menu
//key mapping


//bugs:
//6 - arrow hitbox too large?
//10- null pointer crash bug when descending stairs
//does this happen when youve been to a second level om a previous run in the same session


//possibly fixed
//1 - i've had at least one map generate without an exit. -only ever saw this once.
//2 - sometimes the player starts in the wrong room, or inside walls. -think i fixed this.
//4 - arrow crashbug. possibly resurfaced after i adjusted the arrow origin to center. -fixed.
//5 -stopped arrows still kill the player. -fixed.
//7 - trolls in doorways. -fixed.
//9 - weird player spawn bullshit. -possibly fixed maybe.
//3 - you can explode bombs in other rooms, but you can't blow up trolls that way.
  //-fix: each bomb has its own list of trolls.
//8 - freakout arrows. - fixed.

Level[] levels;
int currlevel;
int highestvisitedlevel;
Player player;
PImage tiles;
boolean showmap = false;
boolean gameon = false;
PFont largetext, smalltext;

int blueblack, purple, green;

//if this is true, it'll take a screenshot each time the player dies.
boolean screenshotondeath = false;

//if this is true, it'll take a screenshot every 10000 millis 
//with some hacks so that it doesn't take the same screen a bunch of times, if you leave the game running.
boolean screenshotoninterval = false;
int lastscreenshot = 0;
int sshotinterval = 10000;
int lastkeypress = 1;

int TILE = 16; int HALFTILE = 8;

Key [] keys = new Key [11];
  Key KEYUP, KEYDOWN, KEYLEFT, KEYRIGHT, KEYSWORD, KEYBOMB, KEYUSE, KEYMAP, KEYYES, KEYNO, KEYSCREENSHOT; 

boolean doscreenshot = false;

//------------------------------------------------------
public void setup(){
  
  //frameRate(100);
  
  noStroke();
  
  tiles = loadImage("tiles.png");
  
  blueblack = color(0,0,5);
  purple = color(120,70,120);
  green = color(90,150,84);
  
  largetext = loadFont("eucrosiaupc32.vlw");  
  smalltext = loadFont("fangsong14.vlw");
  
  keys[0] = new Key ("Up",     38);
  keys[1] = new Key ("Down",   40);
  keys[2] = new Key ("Left",   37);
  keys[3] = new Key ("Right",  39);
  keys[4] = new Key ("Sword",  90); //z
  keys[5] = new Key ("Bomb",   83); //s
  keys[6] = new Key ("Use",    88); //c (doesn't do anything)
  keys[7] = new Key ("Map",    9);  //tab
  keys[8] = new Key ("Accept", 10); //enter
  keys[9] = new Key ("Cancel", 32); //space (doesn't do anything)
  keys[10] = new Key ("Screenshot", 80); //p
  
  KEYUP    = keys[0];
  KEYDOWN  = keys[1];
  KEYLEFT  = keys[2];
  KEYRIGHT = keys[3];
  KEYSWORD = keys[4]; 
  KEYBOMB  = keys[5]; 
  KEYUSE   = keys[6]; 
  KEYMAP   = keys[7]; 
  KEYYES   = keys[8]; 
  KEYNO    = keys[9];
  KEYSCREENSHOT = keys[10];
  
  

}

//------------------------------------------------------
public void newgame(){
  currlevel = 0;
  int highestvisitedlevel = 0;
  levels = new Level[2];
  levels[0] = new Level(false);    
  
  player = new Player(levels[0].map.hubs[0].x, levels[0].map.hubs[0].y); 
  gameon = true; 
}

//------------------------------------------------------
public void setlevel(int _addlevel) {
  currlevel += _addlevel;
  println("moving to level "+currlevel);
  if (currlevel == levels.length-1) println ("this is the last level");
  if ( currlevel < 0 ) 
     finish();
  else {
    
    //generate the level if it's not been visited yet   
    if (currlevel > highestvisitedlevel) {
      highestvisitedlevel = currlevel;
      levels[currlevel] = new Level(currlevel == levels.length-1? true: false); 
    }

    
    Vec2[] hubs = levels[currlevel].map.hubs;
    int starthub = (_addlevel>0)? 0: hubs.length-1; //if travelling down, start at stairsup
    
  
    player.setmap(hubs[starthub].x,hubs[starthub].y);
  }

}
//------------------------------------------------------
public void draw(){
  
  handlescreenshots();
  
  fill(blueblack);
  rect(0,0,256,320);
    
  if(gameon) {
    if (KEYMAP.toggle) levels[currlevel].map.draw();    
    else drawgame();
  }  
  
  else drawmenu();
  
  repeatkeys();
    //call key events that need to happen every frame  
}

//------------------------------------------------------
public void drawgame() {
  levels[currlevel].rooms[player.mappos.x][player.mappos.y].draw(); 
  player.draw(); 
  fill(green);
  
  if (player.hasbombs != 0) 
    text(player.hasbombs +" Bomb" +((player.hasbombs==1)?"":"s"), 64,287); 
    
  text(player.hasgems +" Gem"+((player.hasgems==1)?"":"s"), 192,287); 
  
  if (player.haschalice != 0) 
    text(player.haschalice +" Chalice"+((player.haschalice==1)?"":"s"), 128,308); 
}

//------------------------------------------------------
public void drawmenu() {
  fill(purple);
  textFont(largetext);
  textAlign(CENTER);
  text("TROLLEHOLM", 128,128);
  
  fill(green);
  textFont(smalltext);
  text("Enter.", 128,200);
  text("Matthew Rundle, 2010", 128,300);
}

//------------------------------------------------------
public void die() {
  gameon = false;
  if (screenshotondeath)
    doscreenshot = true;
}

//------------------------------------------------------
public void finish() {
  gameon = false;
  println("finished with "+player.hasgems+" gems");
}


//------------------------------------------------------
public void handlescreenshots() {
  if (screenshotoninterval)
    if (millis() > lastscreenshot + sshotinterval)
    if (lastkeypress > lastscreenshot)
    if (gameon) {
      doscreenshot=true;
      lastscreenshot = millis();
  } 
    
  if (doscreenshot) {
    screenshot();
    doscreenshot =false;
  }
}

//------------------------------------------------------
public void screenshot(){
  File file;
  String filestring; //filenames in the form random(MAX_INT)+".png"
  do { 
    filestring = PApplet.parseInt(random(MAX_INT))+".png";
    file = new File(filestring);
  }
  while(file.exists()); //get new filename, don't save over an existing file
                        //I guess it could happen
  saveFrame(filestring);
  println("saved "+filestring);
}
class Arrow implements Constants{
Vec2 pos;
Vec2 target;
Room room;
float angle =0;
boolean hitwall = false;
boolean hurtsplayer = true;

//-------------------------------------------------------------------------
Arrow(Room _room, int _posx, int _posy, int _targx, int _targy) {
  pos = new Vec2(_posx, _posy);
  target = new Vec2(_targx, _targy);
  room = _room;
}

//-------------------------------------------------------------------------
public boolean update() {
  PVector unit = new PVector(target.x,target.y);
  unit.sub(new PVector(pos.x,pos.y));
  unit.normalize();
  unit.mult(ARROWSPEED);
  angle = unit.heading2D();
  
  
  pos = new Vec2(pos.x+round(unit.x),pos.y+round(unit.y));

  Collision hitsolid = collideblocks(SOLID, pos, room);
    if (hitsolid.hit) return false;

  if (dist(pos.x, pos.y, target.x, target.y) <ARROWSPEED*1.5f) return false;
  
  if (colliderect(pos.x,pos.y,player.pos.x,player.pos.y)) die();
  return true;
}

//-------------------------------------------------------------------------
public void draw() {

  pushMatrix();
  translate(pos.x,pos.y);
  
  //noFill();
  //stroke(purple);
  //rect(0,0, TILE,TILE);
  
  translate(HALFTILE,HALFTILE);
  rotate(angle);
  translate(-HALFTILE,-HALFTILE);
  
  //stroke(green);
  //rect(0,0, TILE,TILE);
  //noStroke();
  
  drawtile(tiles,3,5);

  popMatrix();
}

}//end class
class Bomb implements Constants{
Vec2 pos;
int timeout= millis()+ BOMBEXPLODES;
boolean isexploding = false;
boolean doneexploding = false;
ArrayList trolls;
Room room;

//-------------------------------------------------------------------------
Bomb(int _x, int _y, ArrayList _trolls, Room _r){
  pos = new Vec2((_x+TILE/2)/TILE, (_y+TILE/2)/TILE);
  trolls = _trolls;
  room = _r;
}

//-------------------------------------------------------------------------
public boolean update() {
  //check timer
  
  if (isexploding)
    doneexploding = (millis()>timeout);   
  else {
    isexploding = (millis()>timeout);
    if (isexploding) timeout = millis()+ BOMBSTOPS;
  } 
       
  if (doneexploding) return false; //and gets deleted
  else return true;
}

//-----------------------------------------------------------------------
public Collision collidetrolls(Vec2 _v){
for (int i = 0; i<trolls.size(); i++) {
  Troll troll = (Troll) trolls.get(i);
  if (colliderect(_v.x,_v.y, troll.pos.x, troll.pos.y)) {
     trolls.remove(i);
     return new Collision(true, troll.startpos.x,troll.startpos.y);       
  }
}
return new Collision(false);
}
  
//-------------------------------------------------------------------------
public void draw(){

  boolean draw = false;
  if (room == levels[currlevel].rooms[player.mappos.x][player.mappos.y])  draw = true;
  //does not draw when the player and the bomb are in different rooms
  //but still explodes, and clears blocks.
  
  
  if (isexploding) {
    for (int x = pos.x-2; x<pos.x+3; x++)
    for (int y = pos.y-2; y<pos.y+3; y++) {
      if (dist(x,y,pos.x,pos.y)<2.5f)
      if (room.has(BOMBABLE,x,y)){
        if (draw) drawexplosion(x,y);
        room.cell[x][y] = EMPTY;                        
      }
      else if (room.has(TROLL,x,y)) //troll start pos
        if (draw) drawexplosion(x,y);
          //but don't remove it.
      
      Collision hittrolls = collidetrolls(new Vec2(x*TILE,y*TILE));
      if (hittrolls.hit) room.cell[hittrolls.pos.x][hittrolls.pos.y] = EMPTY;
      
      if(abs(dist(x*TILE,y*TILE,player.pos.x,player.pos.y))<2.5f)
        die(); //player dies if in bomb range.
    }
  }
  else {
    pushMatrix();
    translate(pos.x*TILE,pos.y*TILE);
    if (draw)drawtile(tiles,1,4);
    popMatrix();
  }    
}

//-------------------------------------------------------------------------  
public void drawexplosion (int _x, int _y){
  pushMatrix();
  translate(_x*TILE,_y*TILE);   
    drawtile(tiles,2,4);
  popMatrix();  
}
  
}//end class
class Collision {
boolean hit;
Vec2 pos;

Collision (boolean _hit, int _x, int _y) { hit = _hit; pos = new Vec2(_x,_y); }
Collision (boolean _hit) { hit = _hit; pos = new Vec2(0,0); }

}//end class


//--------------------------------------------------------------------------------------
public Collision collideblocks(char[] _find, Vec2 _v, Room _room){
  Vec2 tile = new Vec2(_v.x/TILE, _v.y/TILE);
  for (int x = tile.x-1; x<=tile.x+1; x++)
  for (int y = tile.y-1; y<=tile.y+1; y++) {
    if (_room.has(_find,x,y))
    if (colliderect(_v.x, _v.y, x*TILE, y*TILE))
       return new Collision(true, x,y);
  }
  return new Collision(false);
}
  
//--------------------------------------------------------------------------------------
public boolean colliderect(int _ax1,int _ay1,int _ax2,int _ay2,
                    int _bx1,int _by1,int _bx2,int _by2) {
  //returns true if the rect a intersects the rect b.
  //x1,y1 is top left and x2,y2 is bottom right.
  
  if (_ax1 >= _bx2) return false;
  if (_ay1 >= _by2) return false;
  if (_ax2 <= _bx1) return false;
  if (_ay2 <= _by1) return false;
  
  return true;
}

//--------------------------------------------------------------------------------------
public boolean colliderect(int _ax,int _ay, int _bx,int _by) {
  return colliderect(_ax,_ay,_ax+TILE,_ay+TILE,_bx,_by,_bx+TILE,_by+TILE);
}
interface Constants {
  int ROOMSIZE = 16;
  int MAPSIZE = 16;
  int MINDIST = MAPSIZE; //minimum distance between two map hubs
  
  int BOMBDELAY =600; //millis it waits before it drops a second bomb
  int BOMBEXPLODES = 1000; //millis it takes for a bomb to go off
  int BOMBSTOPS = 200; //millis the explosion is displayed for
  
  int SWORDDELAY = 180; //time between slashes
  int SWORDSTOPS = 10; //length of sword animation
  
  int ARROWDELAY = 300; //min time between arrows
  int ARROWSPEED = 3;
  
  int ENEMYDELAY = 300;
  
  int FLURRYLENGTH = 2000;
  int SLASHESPERFLURRY = 3; //after this many slashes, it makes you wait.
  
  float LIKELY = .2f; float EVEN = .5f; float UNLIKELY = .8f; float TERRIBLYUNLIKELY = .99f;
  //used like: if(random(1)>LIKELY) dostuff();
  
  char EMPTY = '0'; char STONE = '1'; char EARTH = '2'; char PERMASTONE = '3';
  char STAIRSUP = '4'; char STAIRSDOWN = '5'; char HUB = '6'; char GEM = '7';
  char BOMBDROP = '8'; char CHALICE = '9';
  char BRUTE ='B'; char RANGER ='R'; //enemy start positions
  
  char[] SOLID = {STONE, PERMASTONE, EARTH};
  char[] BOMBABLE = {EARTH, STONE, EMPTY, GEM, BOMBDROP};
  char[] SWORDABLE = {EARTH};
  char[] COLLECTIBLE = {GEM, BOMBDROP, CHALICE};
  char[] TROLL = {BRUTE, RANGER};
  char[] USABLE = {STAIRSUP, STAIRSDOWN};
  
  //these affect how the sword draws
  int ARMLENGTH = 12;
  int NUMANGLES = 6;
  float SWORDANGLE = TWO_PI/NUMANGLES;
  
  int PLAYERSPEED = 2;
  
  //also, these constants are in the global scope:
  //Key KEYUP, KEYDOWN, KEYLEFT, KEYRIGHT, KEYSWORD, KEYBOMB, KEYUSE, KEYMAP, KEYYES, KEYNO;
  //int TILE =16;

}
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
public void fill(char _c) {
  for (int x=0; x<cell.length; x++)
  for (int y=0; y<cell[0].length; y++)
    cell[x][y] = _c;
}

//------------------------------------------------------------------------
public void fill(char _c, int _x1, int _y1, int _x2, int _y2) {
  for (int x=_x1; x<=_x2; x++)
  for (int y=_y1; y<=_y2; y++)
    cell[x][y] = _c;
}


//------------------------------------------------------------------------
public void noise(char[] _values) {
  for (int x=0; x<cell.length; x++)
  for (int y=0; y<cell[0].length; y++)
    cell[x][y] = _values[(int)random(_values.length)];
}

//------------------------------------------------------------------------
public void noise(char[] _values, int _x1, int _y1, int _x2, int _y2) {
  for (int x=_x1; x<=_x2; x++)
  for (int y=_y1; y<=_y2; y++)
    cell[x][y] = _values[(int)random(_values.length)];
}

//------------------------------------------------------------------------
public void insert(Grid _other, int _x, int _y) {
  //copies _other into this grid, overwriting whatever values are present
  for (int x=0; x<_other.cell.length; x++)
  for (int y=0; y<_other.cell[0].length; y++)
    cell[x+_x][y+_y] = _other.cell[x][y];
  //does not check that _other will actually fit into this grid  
}

//------------------------------------------------------------------------
public void merge(Grid _other, char[] _alpha, int _x, int _y){
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
public void replace(char[] _replace, char _fill, int _x1, int _y1, int _x2, int _y2) {
  for (int x=_x1; x<=_x2; x++)
  for (int y=_y1; y<=_y2; y++)
  for (int i=0; i<_replace.length; i++)
    if (cell[x][y] == _replace[i]) cell[x][y] = _fill;
}

//------------------------------------------------------------------------
public void floodfill(char _fill, char _replace, int _x, int _y){
  cell[_x][_y] = _fill;

  if (has(_replace, _x-1 ,_y)) floodfill(_fill, _replace, _x-1, _y);
  if (has(_replace, _x+1, _y)) floodfill(_fill, _replace, _x+1, _y);
  if (has(_replace, _x, _y-1)) floodfill(_fill, _replace, _x, _y-1);
  if (has(_replace, _x, _y+1)) floodfill(_fill, _replace, _x, _y+1);
}

//------------------------------------------------------------------------
public void floodfill(char _fill, int _x, int _y){
  char replace = cell[_x][_y];
  if (replace != _fill)
    floodfill(_fill, replace, _x, _y);
}

//------------------------------------------------------------------------
public boolean exists (int _x, int _y) {
 
  //check for out of bounds
  if (_x<0) return false;
  if (_x>cell.length-1) return false;
  if (_y<0) return false;
  if (_y>cell[0].length-1) return false;
  else return true;

}

//------------------------------------------------------------------------
public boolean has (char[] _c) {
  //true if anyy value in _c exists somewhere in the grid
  for (int x=0; x<cell.length; x++)
  for (int y=0; y<cell[0].length; y++)
  for (int i=0; i<_c.length; i++)
    if (cell[x][y] == _c[i]) return true;
  
  return false;
}

//------------------------------------------------------------------------
public boolean has (char _c, int _x, int _y) {
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
public boolean has (char[] _c, int _x, int _y) {
  //true if [_x][_y] exists and contains one of the values in _c
  for (int i = 0; i<_c.length; i++)
    if (has (_c[i], _x, _y)) return true;
    
  return false;    
}

//------------------------------------------------------------------------
public Vec2 find (char _c) {
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

public Vec2 find (char[] _c) {
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
public Vec2 find (char _c, int _x1, int _y1, int _x2, int _y2) {
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
public Vec2[] findall (char _c) {
  //returns all points that have _c
  Vec2[] points = new Vec2[0];
  for (int x=0; x<cell.length; x++) 
  for (int y=0; y<cell[0].length; y++)
    if (cell[x][y] == _c)
      points = (Vec2[]) append(points, new Vec2(x,y));
  return points;
}

//------------------------------------------------------------------------
public Vec2[] findall (char[] _c) {
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
public Vec2 findnearest (char _c, int _x, int _y, int _inc) {
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

public Vec2 findinradius (char _c, int _x, int _y, float _r) {
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
public void grow(char _fill, char _replace, int _threshold){
  //does cellular automata stuff, makes noise look less random
  Grid prev = new Grid(this); 
  
  for (int x=0; x<cell.length; x++) 
  for (int y=0; y<cell[0].length; y++) 
    cell[x][y] =  (prev.countneighbors(_fill, x, y) > _threshold)? _fill : _replace;
}

//------------------------------------------------------------------------
public int countneighbors(char _find, int _x, int _y){
  int count = -1; 

  for (int x = _x-1; x<=_x+1; x++)
  for (int y = _y-1; y<=_y+1; y++)
    if (has(_find,x,y)) count++;
  
  return count;
}

//------------------------------------------------------------------------
public Grid swapxy() { //must be square
  Grid r = new Grid(cell);
  for (int x=0; x<cell.length; x++)
  for (int y=0; y<cell[0].length; y++)
    cell[x][y] = r.cell[y][x];
  return r;
}

//------------------------------------------------------------------------
public Grid flip(boolean _flipx, boolean _flipy) { 
  Grid r = new Grid(cell);
  for (int x=0; x<cell.length; x++)
  for (int y=0; y<cell[0].length; y++)
    r.cell[x][y] = cell[_flipx?cell.length-1-x:x][_flipy?cell.length-1-y:y];
  return r;
}

//-------------------------------------------------------------------------
public Grid subset(int _x1, int _y1, int _x2, int _y2) {
  Grid r = new Grid(_x2-_x1, _y2-_y1);
  
  for (int x=0; x<r.cell.length; x++)
  for (int y=0; y<r.cell[0].length; y++)
    r.cell[x][y] = cell[_x1+x][_y1+y];
    
  return r; 
}

//-------------------------------------------------------------------------
public boolean lineofsight( char[] _solid, int _x1, int _y1,int _x2, int _y2) {
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
public void dig(char _fill, int _x1, int _y1, int _x2, int _y2) {
  //digs out a random path between two points
  //replacing all cells on the path with _fill
  int dirx = (_x2 > _x1? 1 : -1);
  int diry = (_y2 > _y1? 1 : -1);
  boolean atx, aty;
  
  do {
    cell[_x1][_y1] = _fill;
    atx = (_x1==_x2);
    aty = (_y1==_y2);

    if (random(1) > 0.5f){
      if (!atx) _x1 += dirx;
    }
    else if (!aty) _y1 += diry;
  }
  while(!(atx && aty));
  
}

//-------------------------------------------------------------------------
public void print(){
  String[] strings = getstrings();
  for (int i = 0; i< strings.length; i++)
    println(strings[i]);
}

//-------------------------------------------------------------------------
public String[] getstrings(){
  String[] strings = new String[cell[0].length];
  for (int y=0; y<cell[0].length; y++){
    strings[y] = "";
    for (int x=0; x<cell.length; x++)
      strings[y] += cell[x][y];
  }
  return strings;
} 

}// end class
class Key{ 
//for tracking key input

  String label = "..."; // should get set something descriptive.

  int code;
  boolean down = false; // true on keypress, false on keyrelease
  boolean toggle = false; //true every odd keypress
  
  Key(String _label, int _code){ label = label; code = _code; }
  Key(int _code){ code = code; }
  
}//end class


//------------------------------------------------------
public void keyPressed(){
  lastkeypress = millis();
  //println(keyCode);
  for (int i = 0; i < keys.length; i++)
    if (keys[i].code == keyCode) {
      keys[i].down = true;
      keys[i].toggle = !keys[i].toggle;
      break;
    }
  checkkeys();
} 

//------------------------------------------------------
public void keyReleased(){
  for (int i = 0; i < keys.length; i++)
    if (keys[i].code == keyCode) {
      keys[i].down = false;
      break;
    }
} 

//------------------------------------------------------
public void checkkeys(){ // called on keypress  
  if (KEYSCREENSHOT.down) doscreenshot = true;
  if (gameon) player.checkkeys();
  else if (KEYYES.down) newgame();
}

public void repeatkeys(){ //called on draw
  if (gameon) player.repeatkeys();  
}
class Level implements Constants {
  Map map = new Map(false);
  Room[][] rooms = new Room[MAPSIZE][MAPSIZE];
  boolean last = false; //true for the last level.
  
//--------------------------------------------------------------------------------------
  Level(boolean _last){
  last = _last;
  for (int x = 0; x<MAPSIZE;x++)
  for (int y = 0; y<MAPSIZE;y++)
    rooms[x][y] = new Room(map,x,y, false); 
    
  if (last) {
    Vec2 lasthub = map.hubs[map.hubs.length-1];
    println("last hub is" + lasthub.x+ " " + lasthub.y);
    rooms[lasthub.x][lasthub.y] = new Room(map,lasthub.x,lasthub.y, true); 
  }
  
  }
  

  
  Level(String _filename){
  }
  
//--------------------------------------------------------------------------------------
  public void save(){
  }

}
class Map extends Grid implements Constants{
boolean last;
Grid grid;
Vec2[] hubs; //hubs[0] is the room that has stairsup, hubs[length-1] is stairsdown
  //all other hubs have bombs

//----------------------------------------------------------------------  
Map(boolean _last) { 
  super(MAPSIZE);
  last = _last;
  sethubs();
  digpaths();
}
//----------------------------------------------------------------------
public void sethubs(){
    hubs = new Vec2[(int)random(4)+4];//4-7
    
    hubs[0] = newhub();
    for (int i = 1; i< hubs.length-1; i++) {
       do hubs[i] = newhub();
         while (hubs[i].dist(hubs[i-1])<MINDIST);
       //each hub must have dist >= MINDIST from previous hub
    }
    do hubs[hubs.length-1] = newhub();
      while (hubs[hubs.length-1].dist(hubs[0])<MINDIST);
    //except the last, which must have dist >= MINDIST from first hub
  }
  
public Vec2 newhub() { return new Vec2((int)random(MAPSIZE),(int)random(MAPSIZE)); }

//----------------------------------------------------------------------
public void digpaths(){
  fill('1');
  for (int i=1; i< hubs.length; i++){
    int paths = (int)random(3)+1; //1-3
    //int paths = 1;
    for (int j=0; j< paths; j++)
      dig('0',hubs[i].x,hubs[i].y,hubs[i-1].x,hubs[i-1].y);
  }    
}

//----------------------------------------------------------------------
public void draw(){
  for (int x=0; x<cell.length; x++)
  for (int y=0; y<cell[0].length; y++) {
    int u = 0;
    int v = -1;//init
    if (levels[currlevel].rooms[x][y].visitedadj) {
      if (cell[x][y] == '1') v = 1;//stone
    }
    else {
      u=2;
      v=1;//fog
    }
    
    if (levels[currlevel].rooms[x][y].visited) {
      if (cell[x][y] == '1') v = 1;   
      for (int i=0; i< hubs.length; i++)      
        if ((hubs[i].x == x )&&(hubs[i].y == y)){
          v = 3;
          if (i ==0) u=1;//stairsup
          else if (i ==hubs.length-1) u=2;//stairsdown
          else v = -1;
        }
    }
    
    if (v != -1) { //draw tiles
      pushMatrix();
      translate(x*TILE, y*TILE);
      drawtile(tiles,u,v);
      popMatrix();
    }
    
    if (millis()%1000 > 500) { //draw player
      pushMatrix();
      translate(player.mappos.x*TILE,player.mappos.y*TILE);
      drawtile(tiles,0,2);
      popMatrix();
    }
  }
}

}//end class
class Player implements Constants{
  
ArrayList bombs; //the bombs currently in play
ArrayList trolls; //the trolls in the current room

Vec2 mappos;
Vec2 pos;

int hasgems = 0;
int hasbombs = 2;
int haschalice = 0;

int lastbomb = 0; //millis when last bomb was dropped
int lastsword = 0; //millis when sword last activated

int swordslashes= 0;
int lastflurry = 0;

boolean dangernear = false;


//----------------------------------------------------------------------  
Player(int _x, int _y) {
  setmap(_x,_y);
  bombs = new ArrayList();
  trolls = new ArrayList();
}

public void setmap(int _x, int _y) {
  mappos = new Vec2(_x,_y);
  Vec2 stairsat = levels[currlevel].rooms[mappos.x][mappos.y].find(USABLE);
  Vec2 tilepos = levels[currlevel].rooms[mappos.x][mappos.y].findnearest(EMPTY, stairsat.x,stairsat.y, 1);
  pos = tilepos;
  pos.mult(TILE);
  shiftmap(pos);
}

//---------------------------------------------------------------------- 
public void checkkeys(){
  if (KEYYES.down) use();
  if (KEYBOMB.down)
    if (millis()>lastbomb+BOMBDELAY)
      dropbomb();    
}

  
//---------------------------------------------------------------------- 
public void repeatkeys(){
  if (KEYUP.down) addpos(0,-PLAYERSPEED);
  if (KEYDOWN.down) addpos(0,PLAYERSPEED);
  if (KEYLEFT.down) addpos(-PLAYERSPEED,0);
  if (KEYRIGHT.down) addpos(PLAYERSPEED,0);
  if (KEYSWORD.down) dosword();
}

//----------------------------------------------------------------------  
public void use() {
  Collision hitusable = collideblocks(USABLE, pos, levels[currlevel].rooms[mappos.x][mappos.y]);
  if (hitusable.hit)  {  
    char at = levels[currlevel].rooms[mappos.x][mappos.y].cell[hitusable.pos.x][hitusable.pos.y];    
    
    if (at == STAIRSUP) setlevel(-1);
    else if (at == STAIRSDOWN) setlevel(+1); 
  }
}




//----------------------------------------------------------------------  
public void draw(){
  updatebombs();
  updatetrolls();
  
  if (millis()<=lastsword+SWORDSTOPS)
   drawsword();
 
 Collision hittrolls = collidetrolls(pos, false);
 if (hittrolls.hit) die();
 
  pushMatrix();
  translate(pos.x,pos.y);
  drawtile(tiles,0,2);
  popMatrix();    
}

//----------------------------------------------------------------------
public void drawsword() {

  for (int i = 0; i<NUMANGLES; i++) {
    pushMatrix();
    int millis = millis();
    translate(pos.x+HALFTILE,pos.y+HALFTILE);
    if (millis%200 >100) rotate (SWORDANGLE/2);
    rotate (i*SWORDANGLE);
    translate(0,-ARMLENGTH);
    rotate (-HALF_PI);
    if (dangernear) drawtile(tiles,1,5);
    else drawtile(tiles,0,5);
    popMatrix();
  }
}

//-----------------------------------------------------------------------
public Collision collidetrolls(Vec2 _v, boolean _removetroll){
  for (int i = 0; i<trolls.size(); i++) {
    Troll troll = (Troll) trolls.get(i);
    if (colliderect(_v.x,_v.y, troll.pos.x, troll.pos.y)) {
       if (_removetroll) trolls.remove(i);
       return new Collision(true, troll.startpos.x,troll.startpos.y);       
    }
  }
  return new Collision(false);
}

//-----------------------------------------------------------------------
public Collision collidetrolls(Vec2 _v1, Vec2 _v2, boolean _removetroll){
  for (int i = 0; i<trolls.size(); i++) {
    Troll troll = (Troll) trolls.get(i);
    if (colliderect(_v1.x,_v1.y,_v2.x,_v2.y, troll.pos.x, troll.pos.y, troll.pos.x+TILE, troll.pos.y+TILE)) {
       if (_removetroll) trolls.remove(i);
       return new Collision(true, troll.startpos.x,troll.startpos.y);
    }
  }
  return new Collision(false);
}

//----------------------------------------------------------------------  
public void updatebombs() {
  for (int i = 0; i<bombs.size(); i++) {
    Bomb bomb = (Bomb) bombs.get(i);
    if(bomb.update()) bomb.draw();
    else bombs.remove(i); //bomb.update() returns false after the bomb is done
  }
}
//----------------------------------------------------------------------  
public void updatetrolls(){
  for (int i = 0; i<trolls.size(); i++) {
    Troll troll = (Troll) trolls.get(i);
    troll.update();
    troll.draw();
  }
}
    
//----------------------------------------------------------------------  
public void addmappos(int _x, int _y) {
  Vec2 target = new Vec2 (mappos.x+_x, mappos.y+_y);
  if (levels[currlevel].map.has(EMPTY,target.x,target.y))
    mappos = target;
}

//----------------------------------------------------------------------   
public void addpos(int _x, int _y){
  Vec2 target = new Vec2(pos.x+_x,pos.y+_y);
  target = shiftmap(target); //do room transitions
  Collision hitsolid;
  do {
    hitsolid = (collideblocks(SOLID, target, levels[currlevel].rooms[mappos.x][mappos.y]));
    if (hitsolid.hit) target.approach(pos);
    target.snap(PLAYERSPEED);
  } 
  while (hitsolid.hit);
  pos = target;  

  Collision hitcollect = collideblocks(COLLECTIBLE, pos, levels[currlevel].rooms[mappos.x][mappos.y]);
  if (hitcollect.hit) collect(new Vec2(hitcollect.pos.x, hitcollect.pos.y));   

   
}

//----------------------------------------------------------------------  
public void collect(Vec2 _v){ 
char at = levels[currlevel].rooms[mappos.x][mappos.y].cell[_v.x][_v.y];
levels[currlevel].rooms[mappos.x][mappos.y].cell[_v.x][_v.y] = EMPTY;
if (at == GEM) hasgems +=1;
if (at == BOMBDROP) hasbombs+=1;
if (at == CHALICE) haschalice+=1;
}

//----------------------------------------------------------------------
public Vec2 shiftmap(Vec2 _pos) {

  //accepts pos in the range (0-256)(0-256) - the position of the character on screen, not the mappos.
  //does room transitions and returns the new character pos.
  
  Vec2 newpos;
  
  if (_pos.x<0){
    addmappos(-1,0);
    newpos = new Vec2(_pos.x+256-TILE,_pos.y);
  }  
  else if (_pos.x+TILE>256){
    addmappos(1,0);
    newpos = new Vec2(_pos.x-256+TILE,_pos.y);
  }
  else if (_pos.y<0){
    addmappos(0,-1);
    newpos = new Vec2(_pos.x,_pos.y+256-TILE);
  }
  else if(_pos.y+TILE>256){
    addmappos(0,1);
    newpos = new Vec2(_pos.x,_pos.y-256+TILE);
  }
  else newpos = _pos;
  

 //update which cells are visited, and check dangernear
  
  levels[currlevel].rooms[mappos.x][mappos.y].visited = true;
    
  dangernear = levels[currlevel].rooms[mappos.x][mappos.y].has(TROLL);  
  
  Vec2[] points = new Vec2[4];
  points[0] = new Vec2 (mappos.x-1, mappos.y);
  points[1] = new Vec2 (mappos.x+1, mappos.y); 
  points[2] = new Vec2 (mappos.x, mappos.y+1);
  points[3] = new Vec2 (mappos.x, mappos.y-1); 

  for (int i=0; i<points.length; i++)
  if (levels[currlevel].map.exists(points[i].x,points[i].y)) {
    
    levels[currlevel].rooms[points[i].x][points[i].y].visitedadj = true;
    
    if (levels[currlevel].map.has(EMPTY, points[i].x, points[i].y))
      dangernear = (dangernear || levels[currlevel].rooms[points[i].x][points[i].y].has(TROLL));
  }
  
  //if the room changes, get new trolls 
  if (_pos != newpos) {
  trolls = new ArrayList();
  Vec2[] rangers = levels[currlevel].rooms[mappos.x][mappos.y].findall(RANGER);
  for (int i=0; i<rangers.length; i++)
    trolls.add(new Troll(rangers[i].x, rangers[i].y, levels[currlevel].rooms[mappos.x][mappos.y], RANGER));
  Vec2[] brutes = levels[currlevel].rooms[mappos.x][mappos.y].findall(BRUTE);
  for (int i=0; i<brutes.length; i++)
    trolls.add(new Troll(brutes[i].x, brutes[i].y, levels[currlevel].rooms[mappos.x][mappos.y], BRUTE));
  }
  
  return newpos;
}

//-----------------------------------------------------------------------
public void dropbomb(){
  if (hasbombs >0 ) {
    hasbombs-=1;
    bombs.add(new Bomb(pos.x,pos.y, trolls, levels[currlevel].rooms[mappos.x][mappos.y]));
    lastbomb = millis();
  }
}

//-----------------------------------------------------------------------
public void dosword(){
  
   int currflurry = millis() / FLURRYLENGTH;     
   boolean newflurry = (currflurry != lastflurry);   
   if (newflurry) swordslashes = 0;      
   boolean endofflurry = (swordslashes >= SLASHESPERFLURRY);
   
   if ( newflurry || !endofflurry ) 
   if (millis()>lastsword+SWORDDELAY) {    
    lastsword = millis() +SWORDDELAY;
    lastflurry = millis() / FLURRYLENGTH;
    swordslashes +=1;
    
    Vec2 tile = new Vec2(pos.x/TILE, pos.y/TILE);
    Vec2 offset = new Vec2(pos.x%TILE, pos.y%TILE);
    for (int x = tile.x-1; x<=tile.x+1; x++)
    for (int y = tile.y-1; y<=tile.y+1; y++) {
      if (levels[currlevel].rooms[mappos.x][mappos.y].has(SWORDABLE,x,y))
        levels[currlevel].rooms[mappos.x][mappos.y].cell[x][y] = EMPTY;
    }
    Collision hittrolls = collidetrolls(new Vec2(pos.x-HALFTILE,pos.y-HALFTILE), new Vec2(pos.x+TILE+HALFTILE, pos.y+TILE+HALFTILE), true);
    if (hittrolls.hit)
      levels[currlevel].rooms[mappos.x][mappos.y].cell[hittrolls.pos.x][hittrolls.pos.y] = EMPTY;
  }
}

}//end class
class Room extends Grid implements Constants{
Map map;
Vec2 mappos;

boolean last;
boolean visited = false;    //true if you visit this room
boolean visitedadj = false; //true if you visit an adjacent room

int tileseed = (int)random(MAX_INT);

int border = 2;
int doorlength = 4;


//--------------------------------------------------------------------------------------
Room(Map _map, int _mapx, int _mapy, boolean _last){
  super("roominit.txt");
  map = _map;
  mappos = new Vec2(_mapx,_mapy);
  last = _last;
  generatestuff();

}
//--------------------------------------------------------------------------------------
public void generatestuff(){
  if(random(1)> EVEN) generatestone();
  if(random(1)> EVEN) generateearth();
  if(random(1)> UNLIKELY) generateloot();

  addspecials();
  capends();
}

//--------------------------------------------------------------------------------------
public void generatestone(){
  
  int sectionsize = (ROOMSIZE)/2-border;
  int numsections = (int)random(3)+1;
  
  Grid[] section = new Grid[numsections];
   
  for (int i = 0; i<numsections; i++) {
    //make 1-4 square sections 
    section[i] = new Grid(sectionsize);
    
    //fill with stone
    section[i].fill(STONE);
    
    //dig out paths across one diagonal
    int paths = (int)random(5)+1; //1-5 paths
    for (int j = 0; j<paths; j++)
      section[i].dig(EMPTY,0,0,sectionsize-1,sectionsize-1);
      
    //dig out paths to the center sometimes
    if(random(1)> UNLIKELY) section[i].dig(EMPTY,0,0,sectionsize-1,0);
    if(random(1)> UNLIKELY) section[i].dig(EMPTY,sectionsize-1,0,sectionsize-1,sectionsize-1);
    
    //remove some stone with floodfill
    if(random(1)>EVEN){
      int fillat = (int)random(sectionsize);
      section[i].floodfill(EMPTY,fillat,fillat);
    }
  }

  //each quadrant of the play area gets one of the sections generated before,
  //chosen randomly and flipped to face the right way
  insert(section[(int)random(numsections)].flip(false,true), border, border);
  insert(section[(int)random(numsections)], border, border+sectionsize);
  insert(section[(int)random(numsections)].flip(true,true), border+sectionsize, border);
  insert(section[(int)random(numsections)].flip(true,false), border+sectionsize, border+sectionsize);
}

//--------------------------------------------------------------------------------------
public void generateearth(){
  //make a grid, a bit smaller than the play area
  Grid egrid = new Grid(ROOMSIZE-border*2);
  
  //fill it with random earth and empty tiles
  char[] cavestuff = {EMPTY,EARTH};
  egrid.noise(cavestuff);
  
  //smooth it out a bit
  int growpasses = (int)random(4)+1; //1-4 passes
  int growthreshold = (int)random(3)+3; //3-6 adjacent tiles required for each cell, each pass
  for(int i=0; i<growpasses; i++)
    egrid.grow(EARTH,EMPTY,growthreshold);
  
  //merge it with the room grid, leaving prexisting stone tiles intact  
  char[] keepstuff = {STONE,PERMASTONE};
  merge(egrid, keepstuff, border, border);
}

//--------------------------------------------------------------------------------------
public void generateloot(){
  int lsize = ROOMSIZE/2-border;

  Grid lgrid = new Grid(lsize);
  lgrid.fill(EMPTY);
  Vec2 p;
  int numgems = 0;
  do {
    p = lgrid.find(EMPTY);
    lgrid.cell[p.x][p.y] = GEM;
  }
  while ((random(1)>EVEN) && (numgems<2));

  char[] keepstuff = { STONE,  EARTH, PERMASTONE, STAIRSUP, STAIRSDOWN, HUB } ;

  if(random(1)>UNLIKELY) merge(lgrid.flip(false,true), keepstuff, border, border);
  if(random(1)>EVEN) merge(lgrid, keepstuff, border, border+lsize);
  if(random(1)>EVEN) merge(lgrid.flip(true,true),  keepstuff, border+lsize, border);
  if(random(1)>UNLIKELY) merge(lgrid.flip(true,false),  keepstuff, border+lsize, border+lsize);
  
  while (random(1)>UNLIKELY) {
    p = find(EMPTY,border,border,ROOMSIZE-border-border,ROOMSIZE-border-border);
    cell[p.x][p.y] = BOMBDROP;
  }
    
}


//--------------------------------------------------------------------------------------
public void capends(){
  int x = mappos.x; int y = mappos.y;
  if (!(map.has(EMPTY,x-1,y)))
    fill(PERMASTONE, 0,6,1,9);
  else replace(TROLL, EMPTY, 2,6,3,9);
  
  if (!(map.has(EMPTY,x+1,y)))
    fill(PERMASTONE, 14,6,15,9);
  else replace(TROLL, EMPTY, 12,6,13,9);
  
  if (!(map.has(EMPTY,x,y-1)))
    fill(PERMASTONE, 6,0,9,1);
  else replace(TROLL, EMPTY, 6,2,9,3);
  
  if (!(map.has(EMPTY,x,y+1)))
    fill(PERMASTONE, 6,14,9,15);
  else replace(TROLL, EMPTY, 6,12,9,13);
}

//--------------------------------------------------------------------------------------
public void addspecials(){
  int numhubs = map.hubs.length;
  boolean ishub = false;
  for (int i=0; i<numhubs; i++)
  if ((mappos.x == map.hubs[i].x) && (mappos.y == map.hubs[i].y)) {
    ishub = true;
    Vec2 p = find(EMPTY,border,border,ROOMSIZE-border-border,ROOMSIZE-border-border);
    if (i == 0)
      cell[p.x][p.y] = STAIRSUP;
    else if (i == numhubs-1)
      cell[p.x][p.y] = last? CHALICE:STAIRSDOWN;      
    else if (random(1)> EVEN)
      cell[p.x][p.y] = BOMBDROP;
    break;
  }
  if(!ishub) generateenemies();  
}


//--------------------------------------------------------------------------------------
public void generateenemies() {
  int numtrolls = 0;
  while ((random(1)>EVEN) &&(numtrolls<5)) {
    Vec2 p = find(EMPTY,border,border,ROOMSIZE-border-border,ROOMSIZE-border-border);
    cell[p.x][p.y] = (random(1)>UNLIKELY)? BRUTE:RANGER;
    numtrolls++;
  }
}

//--------------------------------------------------------------------------------------
public void draw(){

  randomSeed(tileseed);
  //this way it draws the same tiles each frame, but I don't have to record which tiles are where
  
  for (int x=0; x<cell.length; x++)
  for (int y=0; y<cell[0].length; y++) {
    Vec2 tile = new Vec2 (-1,-1);
    if      ((cell[x][y] == STONE)
    ||      (cell[x][y] == PERMASTONE)) { tile.x = (int)random(2); tile.y = 1; }
    else if (cell[x][y] == EARTH)       { tile.x = (int)random(2); tile.y = 0; }
    else if (cell[x][y] == STAIRSUP)    { tile.x = 1; tile.y = 3; }   
    else if (cell[x][y] == STAIRSDOWN)  { tile.x = 2; tile.y = 3; } 
    else if (cell[x][y] == BOMBDROP)    { tile.x = 1; tile.y = 4; random(1); } 
    else if (cell[x][y] == GEM)         { tile.x = 0; tile.y = 4; random(1); }
    else if (cell[x][y] == CHALICE)     { tile.x = 3; tile.y = 4; random(1); }
    
    if (tile.y != -1) { 
      pushMatrix();
      translate(x*TILE, y*TILE);
      drawtile(tiles, tile.x,tile.y);
      popMatrix();
    }
    else random(1);
    
    //need to discard a random for each empty tile
    //otherwise all the tiles change when a cell is changed from something random to EMPTY
    //like when digging out earth or whatever
    //but because of this, I have to discard for anything which draws but could become empty,
    //like collectible stuff.
    //but not for stuff that doesn't draw but could become empty,
    //like enemy start positions.
  }
  
  randomSeed(millis());

}

}//end class
class Troll implements Constants{

  
Vec2 pos, startpos, target;
Room room;
char type; //BRUTE or RANGER
int lastarrow = 0;
int spawnat;
ArrayList arrows = new ArrayList();

//--------------------------------------------------------------------------------------
Troll(int _x, int _y, Room _room, char _type){
  startpos = new Vec2(_x,_y);
  pos = new Vec2(_x*TILE,_y*TILE);
  target = pos;

  room = _room;
  type = _type;
  if((random(1)>TERRIBLYUNLIKELY)) getrandomtarget();
  
  spawnat = millis();
}

//--------------------------------------------------------------------------------------
public void getrandomtarget() {
  target = room.find(EMPTY); target.mult(TILE);
}


//--------------------------------------------------------------------------------------
public void update(){
  updatearrows();
  
  if(random(1)>TERRIBLYUNLIKELY) getrandomtarget(); 
  
  //if the player is in view, do stuff
  if (millis() > spawnat + ENEMYDELAY)
  if (room.lineofsight(SOLID,pos.x+HALFTILE,pos.y+HALFTILE,player.pos.x+HALFTILE,player.pos.y+HALFTILE)) { 
    if (type == BRUTE)
        target = (player.pos);
    else if ((random(1)>TERRIBLYUNLIKELY)
         && (millis() > lastarrow+ARROWDELAY))
      shoot();
  }
  
  //otherwise wander around randomly
  else if((random(1)>TERRIBLYUNLIKELY)) getrandomtarget(); 
  
  PVector unit = new PVector(target.x,target.y);
  unit.sub(new PVector(pos.x,pos.y));
  unit.normalize();
  
  Vec2 newposx = new Vec2(pos.x+round(unit.x),pos.y);
  Collision hitsolid;  
  do {
  hitsolid = collideblocks(SOLID, newposx, room);
    if (hitsolid.hit)
      newposx.approach(pos);
  }
  while (hitsolid.hit); 
  
  Vec2 newpos = new Vec2(newposx.x,pos.y+round(unit.y));
  do {
  hitsolid = collideblocks(SOLID, newpos, room);
    if (hitsolid.hit)
      newpos.approach(newposx);
  }  
  while (hitsolid.hit); 
  
  pos = newpos;   
}

//--------------------------------------------------------------------------------------
public void updatearrows() {
  for (int i = 0; i<arrows.size(); i++) {
    Arrow arrow = (Arrow) arrows.get(i);
    if (arrow.update() == false) arrows.remove(i);
    else arrow.draw();
  }
}

//--------------------------------------------------------------------------------------
public void shoot(){
println("pew");
lastarrow = millis();
arrows.add(new Arrow(room, pos.x,pos.y,player.pos.x,player.pos.y));
} 


//--------------------------------------------------------------------------------------
public void draw(){
    Vec2 tile = new Vec2((type == BRUTE)?1:2,2);
    
    pushMatrix();
    translate(pos.x, pos.y);
    drawtile(tiles, tile.x,tile.y);
    popMatrix();
}

}
//uses ints
class Vec2{
  int x;
  int y;
//--------------------------------------------------------------------------------------  
  Vec2(int _x, int _y) {
    x = _x;
    y = _y;
  }
  
  Vec2(Vec2 _v) {
    x = _v.x;
    y = _v.y;
  }
//--------------------------------------------------------------------------------------
  public int mag() {
    int xdist = abs(x-0);
    int ydist = abs(y-0);
    return xdist+ydist;
  }
//--------------------------------------------------------------------------------------  
  public int dist(Vec2 _v) {
    int xdist = abs(x-_v.x);
    int ydist = abs(y-_v.y);
    return xdist+ydist;
  }
//--------------------------------------------------------------------------------------  
  public void add(Vec2 _v) { x += _v.x;  y += _v.y; }   
  public void sub(Vec2 _v) { x -= _v.x;  y -= _v.y; }  
  
  public void mult (int _n) { x *= _n; y *= _n; }  
  public void div (int _n) { x /= _n; y /= _n; }
//--------------------------------------------------------------------------------------
  public void norm() {
    div(mag());
  } 
//--------------------------------------------------------------------------------------
  public void approach(Vec2 _v){    
    if (x != _v.x) 
      x += (_v.x > x)? 1 : -1;    
    if (y != _v.y)
      y += (_v.y > y)? 1 : -1;
  }
 //--------------------------------------------------------------------------------------
  public void snap(int _i) {
    _i-=1;
    x -= x%_i;
    y -= y%_i;
  } 
}
public void drawtile(PImage _tiles, int _u, int _v){
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
public float cycle(float value, float inc, float min, float max){
//cycle a value within a given range
  value+=inc;  
  if (value>max)
    value=min+(value-max);  
  return value;
}
  public void settings() {  size(256,320,OPENGL);  noSmooth(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "Trolleholm" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
