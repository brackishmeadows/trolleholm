import processing.opengl.*;

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

color blueblack, purple, green;

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
void setup(){
  
  //frameRate(100);
  
  noStroke();
  noSmooth();
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
  
  size(256,320,OPENGL);

}

//------------------------------------------------------
void newgame(){
  currlevel = 0;
  int highestvisitedlevel = 0;
  levels = new Level[2];
  levels[0] = new Level(false);    
  
  player = new Player(levels[0].map.hubs[0].x, levels[0].map.hubs[0].y); 
  gameon = true; 
}

//------------------------------------------------------
void setlevel(int _addlevel) {
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
void draw(){
  
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
void drawgame() {
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
void drawmenu() {
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
void die() {
  gameon = false;
  if (screenshotondeath)
    doscreenshot = true;
}

//------------------------------------------------------
void finish() {
  gameon = false;
  println("finished with "+player.hasgems+" gems");
}


//------------------------------------------------------
void handlescreenshots() {
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
void screenshot(){
  File file;
  String filestring; //filenames in the form random(MAX_INT)+".png"
  do { 
    filestring = int(random(MAX_INT))+".png";
    file = new File(filestring);
  }
  while(file.exists()); //get new filename, don't save over an existing file
                        //I guess it could happen
  saveFrame(filestring);
  println("saved "+filestring);
}
