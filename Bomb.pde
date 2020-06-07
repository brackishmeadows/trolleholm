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
boolean update() {
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
Collision collidetrolls(Vec2 _v){
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
void draw(){

  boolean draw = false;
  if (room == levels[currlevel].rooms[player.mappos.x][player.mappos.y])  draw = true;
  //does not draw when the player and the bomb are in different rooms
  //but still explodes, and clears blocks.
  
  
  if (isexploding) {
    for (int x = pos.x-2; x<pos.x+3; x++)
    for (int y = pos.y-2; y<pos.y+3; y++) {
      if (dist(x,y,pos.x,pos.y)<2.5)
      if (room.has(BOMBABLE,x,y)){
        if (draw) drawexplosion(x,y);
        room.cell[x][y] = EMPTY;                        
      }
      else if (room.has(TROLL,x,y)) //troll start pos
        if (draw) drawexplosion(x,y);
          //but don't remove it.
      
      Collision hittrolls = collidetrolls(new Vec2(x*TILE,y*TILE));
      if (hittrolls.hit) room.cell[hittrolls.pos.x][hittrolls.pos.y] = EMPTY;
      
      if(abs(dist(x*TILE,y*TILE,player.pos.x,player.pos.y))<2.5)
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
void drawexplosion (int _x, int _y){
  pushMatrix();
  translate(_x*TILE,_y*TILE);   
    drawtile(tiles,2,4);
  popMatrix();  
}
  
}//end class
