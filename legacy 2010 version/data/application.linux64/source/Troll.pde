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
void getrandomtarget() {
  target = room.find(EMPTY); target.mult(TILE);
}


//--------------------------------------------------------------------------------------
void update(){
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
void updatearrows() {
  for (int i = 0; i<arrows.size(); i++) {
    Arrow arrow = (Arrow) arrows.get(i);
    if (arrow.update() == false) arrows.remove(i);
    else arrow.draw();
  }
}

//--------------------------------------------------------------------------------------
void shoot(){
println("pew");
lastarrow = millis();
arrows.add(new Arrow(room, pos.x,pos.y,player.pos.x,player.pos.y));
} 


//--------------------------------------------------------------------------------------
void draw(){
    Vec2 tile = new Vec2((type == BRUTE)?1:2,2);
    
    pushMatrix();
    translate(pos.x, pos.y);
    drawtile(tiles, tile.x,tile.y);
    popMatrix();
}

}
