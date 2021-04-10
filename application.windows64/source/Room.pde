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
void generatestuff(){
  if(random(1)> EVEN) generatestone();
  if(random(1)> EVEN) generateearth();
  if(random(1)> UNLIKELY) generateloot();

  addspecials();
  capends();
}

//--------------------------------------------------------------------------------------
void generatestone(){
  
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
void generateearth(){
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
void generateloot(){
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
void capends(){
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
void addspecials(){
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
void generateenemies() {
  int numtrolls = 0;
  while ((random(1)>EVEN) &&(numtrolls<5)) {
    Vec2 p = find(EMPTY,border,border,ROOMSIZE-border-border,ROOMSIZE-border-border);
    cell[p.x][p.y] = (random(1)>UNLIKELY)? BRUTE:RANGER;
    numtrolls++;
  }
}

//--------------------------------------------------------------------------------------
void draw(){

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
