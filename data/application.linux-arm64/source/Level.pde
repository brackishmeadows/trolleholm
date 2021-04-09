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
  void save(){
  }

}
