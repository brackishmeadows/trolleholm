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
void sethubs(){
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
  
Vec2 newhub() { return new Vec2((int)random(MAPSIZE),(int)random(MAPSIZE)); }

//----------------------------------------------------------------------
void digpaths(){
  fill('1');
  for (int i=1; i< hubs.length; i++){
    int paths = (int)random(3)+1; //1-3
    //int paths = 1;
    for (int j=0; j< paths; j++)
      dig('0',hubs[i].x,hubs[i].y,hubs[i-1].x,hubs[i-1].y);
  }    
}

//----------------------------------------------------------------------
void draw(){
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
