class Collision {
boolean hit;
Vec2 pos;

Collision (boolean _hit, int _x, int _y) { hit = _hit; pos = new Vec2(_x,_y); }
Collision (boolean _hit) { hit = _hit; pos = new Vec2(0,0); }

}//end class


//--------------------------------------------------------------------------------------
Collision collideblocks(char[] _find, Vec2 _v, Room _room){
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
boolean colliderect(int _ax1,int _ay1,int _ax2,int _ay2,
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
boolean colliderect(int _ax,int _ay, int _bx,int _by) {
  return colliderect(_ax,_ay,_ax+TILE,_ay+TILE,_bx,_by,_bx+TILE,_by+TILE);
}
