import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import ddf.minim.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class carrom_final extends PApplet {

//to play audio files
AudioPlayer player;
Minim minim;
AudioPlayer strikersound;
//game states
final int GAME_START=-1;
final int GAME_SET=0;
final int GAME_STRIKER_POSITION=1;
final int GAME_STRIKER_ANGLE=2;
final int GAME_STRIKER_VELOCITY=3;
final int GAME_REARRANGE=4;
final int GAME_END=5;
int maxScore=9;
int player1Score=0;
int player2Score=0;

//starting state
int GAME_POSITION=-1;

PFont font;

//size of carromboard
int carrom_board_size;
boolean flag=true;
//variables containing the X and Y coordinate values of board
int Xupper =0,Xlower=0,Yupper=0,Ylower=0;
float r=5,g=5,b=5;//board colours
PImage plank;
PImage pl1;
PImage pl2;
PImage img;

public void  calculate_Outer_Coordinates()
{
  /*
  This function calculates the board position
  */
    Xlower=width/2-(carrom_board_size)/2;
    Ylower=height/2-(carrom_board_size)/2;
    Xupper=width/2+(carrom_board_size)/2;
    Yupper=height/2+(carrom_board_size)/2;
}  

public void boardDisplay(boolean colour)
{
  /*
  This function displays the board
  */
  fill(0,255,0);
     if(frameCount%100==1)  //changes board colour
     {
       r=random(50);
       g=random(50);
       b=random(50);
     }
  fill(255-r,255-g,255-b);
  
  //draws the board and energy bar
  rect(Xlower,Ylower,carrom_board_size,carrom_board_size);
  rect(Xlower+carrom_board_size+Xlower/6,Ylower+carrom_board_size,20,-carrom_board_size);
  fill(0,0,100,50);
  
  //pocket placements
  ellipse(Xlower+23,Ylower+23,45,45); 
  ellipse(Xlower+23,Yupper-23,45,45);
  ellipse(Xupper-23,Ylower+23,45,45);
  ellipse(Xupper-23,Yupper-23,45,45);
  rect(Xlower+60,Ylower+60,carrom_board_size-120,20);
  rect(Xlower+60,Yupper-80,carrom_board_size-120,20);
  int coinCount=0;
  for(;coinCount<18;coinCount++)
    C[coinCount].drawCoin(colour);
  //change coin's position is it is moving
  if(S.velocity>0)
    S.movePosition();
  for(coinCount=0;coinCount<18;coinCount++)
  {
    if(C[coinCount].velocity>0&&C[coinCount].active==true)
      C[coinCount].movePosition();
  }
  
  //draws the striker  
  S.drawCoin(true);
}

public class Coin
{
  
  public float radius;
  public boolean colour;
  public float x;
  public float y;
  public float slope;
  public int velocity;
  public int axis;
  public int move;
  public boolean active;
  public Coin()
  {
    radius=12;
    colour=true;
    x=y=400;
    velocity=axis=move=0;
    active=true;    
  }
  public void setXY(float valx,float valy)
  {
    x=valx;
    y=valy;
  }
  public boolean collides(Coin c)
  {
    /*
    Checks if moving striker or coin collides with another coin
    If it collides, the velocity is transferred. New slope is assigned to the coin.
    */
    if((x-c.x)*(x-c.x)+(y-c.y)*(y-c.y)<(radius+c.radius)*(radius+c.radius))
    {
      strikersound=minim.loadFile("strike.wav");
      strikersound.play();
      c.velocity=velocity;
      velocity=0;
      c.slope=(y-c.y)/(x-c.x);
      
      if(abs(c.x-x)>=abs(c.y-y))
      {
        c.axis=1;
        if(x<c.x)
          c.move=1;
        else
          c.move=-1;
      }
      else
      {
        c.axis=2;
        if(y<c.y)
          c.move=1;
        else
          c.move=-1;
      }
      return true;
    }
    return false;
  }
  
   public boolean collides(Striker c)
  {
    /*
    Checks if moving coin collides with striker
    If it collides, the velocity is transferred. New slope is assigned to the coin.
    */
    if((x-c.x)*(x-c.x)+(y-c.y)*(y-c.y)<(radius+c.radius)*(radius+c.radius))
    {
      strikersound=minim.loadFile("strike.wav");
      strikersound.play(); 
      c.velocity=velocity;
      velocity=0;
      c.slope=(y-c.y)/(x-c.x);
      
      if(abs(c.x-x)>=abs(c.y-y))
      {
        c.axis=1;
        if(x<c.x)
          c.move=1;
        else
          c.move=-1;
      }
      else
      {
        c.axis=2;
        if(y<c.y)
          c.move=1;
        else
          c.move=-1;
      }
      if((radius+c.radius)*(radius+c.radius)-(x-c.x)*(x-c.x)+(y-c.y)*(y-c.y)>move*move)
        move=PApplet.parseInt(move*(radius+c.radius)*(radius+c.radius)-(x-c.x)*(x-c.x)+(y-c.y)*(y-c.y));
      return true;
    }
    return false;
  }
  
  public void movePosition()
  {
    /*
    It updates the positions of the coins. If the coins collide with the board's frame, its slope is negated.
    */
    int temp=velocity/60;
    if(temp==0)
    {
      temp=1;
    }
    move=move*temp;
    velocity-=5;
    if(velocity<0)
      velocity=0;
    if (axis==1)
    {
      x=x+move;
      y=slope*move+y;
    }
    else
    {
      y=y+move;
      x=move/slope+x;
    }
    if(y<Ylower+radius+5)
    {
      strikersound=minim.loadFile("framestrike.wav");
      strikersound.play(); 
      slope=slope*(-1);
      if(axis==2)
        move=move*(-1);
      y=Ylower+radius+5;
    }
    else if(y>Yupper-radius-5)
    {
      strikersound=minim.loadFile("framestrike.wav");
      strikersound.play(); 
      slope=slope*(-1);
      if(axis==2)
        move=move*(-1);
      y=Yupper-radius-5;
    }
    if(x<Xlower+radius+5)
    {
      strikersound=minim.loadFile("framestrike.wav");
      strikersound.play(); 
      slope=slope*(-1);
      if(axis==1)
        move=move*(-1);
        x=Xlower+radius+5;
    }
    else if(x>=Xupper-radius-5)
    {
      strikersound=minim.loadFile("framestrike.wav");
      strikersound.play(); 
      slope=slope*(-1);
      if(axis==1)
        move=move*(-1);
        x=Xupper-radius-5;
    }
    pocket();
    move=move/temp;
  }
  public void pocket()
  {
    /*
    It checks if the moving coin has fallen into the pocket. If it has, then the score is updated according to the coin's colour and the coin is deactivated.
    */
    if((x<Xlower+45&&(y<Ylower+45||y>Ylower+carrom_board_size-45))||(x>Xlower+carrom_board_size-45&&(y<Ylower+45||y>Ylower+carrom_board_size-45)))
    {
        if(radius==S.radius)
        {
        velocity=0;
        return;
        }
        active=false;
        velocity=0;
        if(colour==true)
        {
          if(S.player1==true)
            strikersound=minim.loadFile("winsound.wav");
          else
            strikersound=minim.loadFile("losesound.wav");
          player1Score++;
        }
        else
        {
          if(S.player1!=true)
            strikersound=minim.loadFile("winsound.wav");
          else
            strikersound=minim.loadFile("losesound.wav");
          player2Score++;
        }
        strikersound.play();
        fill(255,255,255);
        image(pl1,Xlower/4,Ylower,Xlower/2,Xlower/3);
        image(pl2,Xlower/4+carrom_board_size+Xlower,Ylower,Xlower/2,Xlower/3);
        text(str(player1Score),Xlower/2-10,Ylower+Xlower/4);
        text(str(player2Score),Xlower/2-10+carrom_board_size+Xlower,Ylower+Xlower/4);
     }
  }
  public void drawCoin(boolean col)
  {
    /*
    The coin is displayed
    */
    if(active==false)
      return;
    fill(255,255,255);
    ellipse(x,y,2*radius,2*radius);
    fill(0,255,0);
    if (colour==true)
      fill(255,51,51);
    if(radius==S.radius)
      fill(255,255,0);
    if(col==false)
    fill(0,153,255);
    ellipse(x,y,2*radius,2*radius);
  }
};

public class Striker extends Coin
{
  public boolean player1;
  public Striker()
  {
    player1=true;
    radius=15;
  }
  public void position()
  {
    /*
    The striker's horizontal position is set
    */
    if(player1==true)
      y=Yupper-70;
    else
      y=Ylower+70;
    x=mouseX;
    if(x+radius>Xupper-50)
      x=Xupper-50-radius;
    else if(x-radius<Xlower+50)
      x=Xlower+50+radius;
    translate(x,y);
  }
  public void strikerAngle()
  {
    /*
    The striker's shooting angle is set
    */
    float angleRadians=atan2(mouseY-y, mouseX-x)-PI/4;
    pushMatrix();
    translate(x,y);
    rotate(angleRadians);
    line(0,0,20,20);
    popMatrix();
  }
  public void shootStriker()
  {
    /*
    The slope for the striker is set
    */
    float x=mouseX,y=mouseY,x1=this.x,y1=this.y;
    slope=(y-y1)/(x-x1);
    if(abs(x-x1)>=abs(y-y1))
    {
      axis=1;
      if(x1<x)
        move=1;
      else
        move=-1;
    }
    else
    {
      axis=2;
      if(y1<y)
        move=1;
      else
        move=-1;
    }  
}
};
Striker S=new Striker();
Coin[] C=new Coin[18];
public void setup()
{
  /*
  The carrom board's size is set. The initial positions of the coins are set.
  */
  
  minim = new Minim(this);
  carrom_board_size=height-140;
  background(100,100,100);
  //calculates the board size for the screen
  calculate_Outer_Coordinates();
      //calculating initial placement of coins in two lines
      float Initial_Y_Position=Ylower+(carrom_board_size/5);
      float Initial_X_Position=Xlower+(carrom_board_size/7);
       C[0]=new Coin();
       C[0].setXY(Xlower+carrom_board_size/2,Initial_Y_Position);
       Initial_Y_Position+=carrom_board_size/10;
       Initial_X_Position=Xlower+(carrom_board_size/3);
     for(int CoinCount=1;CoinCount<3;CoinCount++)
     {
       C[CoinCount]=new Coin();
       C[CoinCount].setXY(Initial_X_Position,Initial_Y_Position);    
       Initial_X_Position+=(carrom_board_size/3);                                  
     }
     Initial_Y_Position+=(carrom_board_size/10);
     Initial_X_Position=Xlower+(carrom_board_size/4);
    for(int CoinCount=3;CoinCount<6;CoinCount++)
     {
       C[CoinCount]=new Coin();
       C[CoinCount].setXY(Initial_X_Position,Initial_Y_Position);   
       Initial_X_Position+=(carrom_board_size/4);                                  
     }
     Initial_Y_Position+=(carrom_board_size/10);
     Initial_X_Position=Xlower+(carrom_board_size/7);
    for(int CoinCount=6;CoinCount<12;CoinCount++)
     {
       C[CoinCount]=new Coin();
       C[CoinCount].setXY(Initial_X_Position,Initial_Y_Position);   
       Initial_X_Position+=(carrom_board_size/7);                                  
     }
     Initial_Y_Position+=(carrom_board_size/10);
     Initial_X_Position=Xlower+(carrom_board_size/4);
    for(int CoinCount=12;CoinCount<15;CoinCount++)
     {
       C[CoinCount]=new Coin();
       C[CoinCount].setXY(Initial_X_Position,Initial_Y_Position);   
       Initial_X_Position+=(carrom_board_size/4);                                  
     }
     Initial_Y_Position+=carrom_board_size/10;
       Initial_X_Position=Xlower+(carrom_board_size/3);
     for(int CoinCount=15;CoinCount<17;CoinCount++)
     {
       C[CoinCount]=new Coin();
       C[CoinCount].setXY(Initial_X_Position,Initial_Y_Position);    
       Initial_X_Position+=(carrom_board_size/3);                                  
     }
     Initial_Y_Position+=carrom_board_size/10;
     C[17]=new Coin();
     C[17].setXY(Xlower+carrom_board_size/2,Initial_Y_Position);
      for(int CoinCount=0;CoinCount<18;CoinCount+=2)
     {
      C[CoinCount].colour=false;
     }
     font=createFont("Xoxoxa.ttf",24);
     textFont(font); 
}


public void velocityDisplay()
{
  /*
  sets the velocity  
  */
  if(mouseY-carrom_board_size>0)
    return;
    fill(255-mouseY/4,0,0);
    rect(Xlower+carrom_board_size+Xlower/6,Ylower+carrom_board_size,20,mouseY-carrom_board_size);
}

public void collisionChecker()
{
  /*
  checks all the combinations of collisions
  */
  int coinCount,coinCount2;
  if(S.velocity>0)
  {
    for(coinCount=0;coinCount<18;coinCount++)
    {
      if(C[coinCount].active==false)
        continue;
      if(S.collides(C[coinCount]))
        return;
    }
    return;
  }
  for(coinCount=0;coinCount<18;coinCount++)
  {
    if(C[coinCount].active==false)
        continue;
    if(C[coinCount].velocity>0)
    {
      for(coinCount2=0;coinCount2<18;coinCount2++)
      {
        if(C[coinCount2].active==false)
          continue;
        if(coinCount2==coinCount)
          continue;
        if(C[coinCount].collides(C[coinCount2]))
          return;
      }
      if(C[coinCount].collides(S))
        return;
      return;
    }
  }
}
public void playerChange()
{
  /*
  toggles the player's turn
  */
  int coinCount;
  if(S.velocity>0)
    return;
  for(coinCount=0;coinCount<18;coinCount++)
  {
    if(C[coinCount].active==true && C[coinCount].velocity>0)
      return;
  }
  if(S.player1==true)
    S.player1=false;
  else
    S.player1=true;
  GAME_POSITION=GAME_STRIKER_POSITION;
  delay(500);
}
public void endDetector()
{
  /*
  checks if the game has ended
  */
  if(player1Score==maxScore||player2Score==maxScore)
    GAME_POSITION=GAME_END;
}
public void winnerDisplay()
{
  /*
  Displays the winner
  */
  if(player1Score==maxScore)
    plank=loadImage("winner1.png");
  else
    plank=loadImage("winner2.png");
    image(plank,0,0,width,height);
    strikersound=minim.loadFile("winsound.wav");
    strikersound.play();
    GAME_POSITION=GAME_END+1;
}
public void stop()
{
  /*
  stops the background music
  */
  player.close();
  strikersound.close();
  minim.stop();
  super.stop();
}
public void setbackground()
{
  /*
  Sets the initial background
  */
     plank=loadImage("plank.png");
     image(plank,0,0,width,height);
     pl1=loadImage("player1.png");
     pl2=loadImage("player2.png");
     image(pl1,Xlower/4,Ylower,Xlower/2,Xlower/3);
     image(pl2,Xlower/4+carrom_board_size+Xlower,Ylower,Xlower/2,Xlower/3);
     text(str(player1Score),Xlower/2-10,Ylower+Xlower/4);
     text(str(player2Score),Xlower/2-10+carrom_board_size+Xlower,Ylower+Xlower/4);
     GAME_POSITION++;
}
int imagecount,pos=-20;
public void displaywelcome()
{
  /*
  Displays the game opening
  */
  if(imagecount==0&&frameCount%50==1)
   {
      img=loadImage("startbkround1.jpg");
      image(img,0,0,width,height);
      imagecount++;
   }
   else if(imagecount==1&&frameCount%50==1)
   {
     img=loadImage("startbkround2.jpg");
     image(img,0,0,width,height);
     imagecount++; 
   }
   else if(imagecount==2&&frameCount%50==1)
   {
     strikersound=minim.loadFile("swish2.wav");
      strikersound.play(); 
     img=loadImage("startbkround3.jpg");
     image(img,0,0,width,height);
     imagecount++;  
   }
   else if(imagecount==3&&frameCount%50==1)
   {
     strikersound=minim.loadFile("swish.wav");
      strikersound.play(); 
     img=loadImage("startbkround4.jpg");
     image(img,0,0,width,height);
     imagecount++; 
   }
   else if (imagecount==4&&frameCount%50==1)
   {strikersound=minim.loadFile("swish3.wav");
      strikersound.play(); 
     img=loadImage("startbkround5.jpg");
     image(img,0,0,width,height);
     imagecount++; 
   }
   else if(imagecount==10&&frameCount%50==1)
   {
     player = minim.loadFile("background.mp3");
      player.loop();
     img=loadImage("startbkround6.jpg");
     image(img,0,0,width,height);
     imagecount++; 
   }
   else if(frameCount%50==1)
     imagecount++;
}

public void rulesDisplay()
{
  /*
  Displays the game rules
  */
   flag=false;
   img=loadImage("startbkround7.jpg");
   image(img,0,0,width,height);
}
public void draw()
{
  /*
  Calls different functions based on the game state
  */
  switch(GAME_POSITION)
  {
    case GAME_START:
      displaywelcome();
      break;
    case GAME_SET:
      setbackground();
      break;
    case GAME_STRIKER_POSITION:
      boardDisplay(false);
      S.position();
      break;
    case GAME_STRIKER_ANGLE:
      boardDisplay(false);
      S.strikerAngle();
      break;
    case GAME_STRIKER_VELOCITY:
      boardDisplay(false);
      velocityDisplay();
      break;
    case GAME_REARRANGE:
      boardDisplay(true);
      textSize(24);
      collisionChecker();
      playerChange();
      endDetector();
      break;
    case GAME_END:
      winnerDisplay();
      break;
  }
}
public void mousePressed()
{
  if(GAME_POSITION==GAME_STRIKER_POSITION)
  {
    int i=0;
    for(i=0;i<18;i++)
      if(S.collides(C[i]))
        return;
    GAME_POSITION++;   
  }
  else if(GAME_POSITION==GAME_START)
    {
      if(flag==true)
         rulesDisplay();
      else
      {
         GAME_POSITION++;
         img=loadImage("try.png");
      }
    }
  else if(GAME_POSITION==GAME_STRIKER_ANGLE)
  {
    S.shootStriker();
    GAME_POSITION++;   
  } 
  else if(GAME_POSITION==GAME_STRIKER_VELOCITY)
  {
    S.velocity=(int)((mouseY-carrom_board_size)*(-1.3f));
    GAME_POSITION++;
  }
  else if(GAME_POSITION == GAME_END)
  {
    stop();
  }  
}
  public void settings() {  fullScreen(); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "--present", "--window-color=#666666", "--stop-color=#cccccc", "carrom_final" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
