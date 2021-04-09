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
  
  float LIKELY = .2; float EVEN = .5; float UNLIKELY = .8; float TERRIBLYUNLIKELY = .99;
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
