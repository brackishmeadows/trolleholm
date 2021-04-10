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

void setmap(int _x, int _y) {
  mappos = new Vec2(_x,_y);
  Vec2 stairsat = levels[currlevel].rooms[mappos.x][mappos.y].find(USABLE);
  Vec2 tilepos = levels[currlevel].rooms[mappos.x][mappos.y].findnearest(EMPTY, stairsat.x,stairsat.y, 1);
  pos = tilepos;
  pos.mult(TILE);
  shiftmap(pos);
}

//---------------------------------------------------------------------- 
void checkkeys(){
  if (KEYYES.down) use();
  if (KEYBOMB.down)
    if (millis()>lastbomb+BOMBDELAY)
      dropbomb();    
}

  
//---------------------------------------------------------------------- 
void repeatkeys(){
  if(random(1)< .99) {
    if (KEYUP.down) addpos(0,-PLAYERSPEED);
    if (KEYDOWN.down) addpos(0,PLAYERSPEED);
  }
  if(random(1)< .99) {
    if (KEYLEFT.down) addpos(-PLAYERSPEED,0);
    if (KEYRIGHT.down) addpos(PLAYERSPEED,0);
  }
  if (KEYSWORD.down) dosword();
}


//----------------------------------------------------------------------  
void use() {
  Collision hitusable = collideblocks(USABLE, pos, levels[currlevel].rooms[mappos.x][mappos.y]);
  if (hitusable.hit)  {  
    char at = levels[currlevel].rooms[mappos.x][mappos.y].cell[hitusable.pos.x][hitusable.pos.y];    
    
    if (at == STAIRSUP) setlevel(-1);
    else if (at == STAIRSDOWN) setlevel(+1); 
  }
}




//----------------------------------------------------------------------  
void draw(){
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
void drawsword() {

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
Collision collidetrolls(Vec2 _v, boolean _removetroll){
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
Collision collidetrolls(Vec2 _v1, Vec2 _v2, boolean _removetroll){
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
void updatebombs() {
  for (int i = 0; i<bombs.size(); i++) {
    Bomb bomb = (Bomb) bombs.get(i);
    if(bomb.update()) bomb.draw();
    else bombs.remove(i); //bomb.update() returns false after the bomb is done
  }
}
//----------------------------------------------------------------------  
void updatetrolls(){
  for (int i = 0; i<trolls.size(); i++) {
    Troll troll = (Troll) trolls.get(i);
    troll.update();
    troll.draw();
  }
}
    
//----------------------------------------------------------------------  
void addmappos(int _x, int _y) {
  Vec2 target = new Vec2 (mappos.x+_x, mappos.y+_y);
  if (levels[currlevel].map.has(EMPTY,target.x,target.y))
    mappos = target;
}

//----------------------------------------------------------------------   
void addpos(int _x, int _y){
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
void collect(Vec2 _v){ 
char at = levels[currlevel].rooms[mappos.x][mappos.y].cell[_v.x][_v.y];
levels[currlevel].rooms[mappos.x][mappos.y].cell[_v.x][_v.y] = EMPTY;
if (at == GEM) hasgems +=1;
if (at == BOMBDROP) hasbombs+=1;
if (at == CHALICE) haschalice+=1;
}

//----------------------------------------------------------------------
Vec2 shiftmap(Vec2 _pos) {

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
void dropbomb(){
  if (hasbombs >0 ) {
    hasbombs-=1;
    bombs.add(new Bomb(pos.x,pos.y, trolls, levels[currlevel].rooms[mappos.x][mappos.y]));
    lastbomb = millis();
  }
}

//-----------------------------------------------------------------------
void dosword(){
  
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
