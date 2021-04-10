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
boolean update() {
  PVector unit = new PVector(target.x,target.y);
  unit.sub(new PVector(pos.x,pos.y));
  unit.normalize();
  unit.mult(ARROWSPEED);
  angle = unit.heading2D();
  
  
  pos = new Vec2(pos.x+round(unit.x),pos.y+round(unit.y));

  Collision hitsolid = collideblocks(SOLID, pos, room);
    if (hitsolid.hit) return false;

  if (dist(pos.x, pos.y, target.x, target.y) <ARROWSPEED*1.5) return false;
  
  if (colliderect(pos.x,pos.y,player.pos.x,player.pos.y)) die();
  return true;
}

//-------------------------------------------------------------------------
void draw() {

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
