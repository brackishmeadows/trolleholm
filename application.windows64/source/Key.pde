class Key{ 
//for tracking key input

  String label = "..."; // should get set something descriptive.

  int code;
  boolean down = false; // true on keypress, false on keyrelease
  boolean toggle = false; //true every odd keypress
  
  Key(String _label, int _code){ label = label; code = _code; }
  Key(int _code){ code = code; }
  
}//end class


//------------------------------------------------------
void keyPressed(){
  lastkeypress = millis();
  //println(keyCode);
  for (int i = 0; i < keys.length; i++)
    if (keys[i].code == keyCode) {
      keys[i].down = true;
      keys[i].toggle = !keys[i].toggle;
      break;
    }
  checkkeys();
} 

//------------------------------------------------------
void keyReleased(){
  for (int i = 0; i < keys.length; i++)
    if (keys[i].code == keyCode) {
      keys[i].down = false;
      break;
    }
} 

//------------------------------------------------------
void checkkeys(){ // called on keypress  
  if (KEYSCREENSHOT.down) doscreenshot = true;
  if (gameon) player.checkkeys();
  else if (KEYYES.down) newgame();
}

void repeatkeys(){ //called on draw
  if (gameon) player.repeatkeys();  
}
