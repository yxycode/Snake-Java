
import javax.swing.*;
import java.awt.*;
    
import java.awt.event.*;
import java.awt.Color;
import java.awt.Font;
import java.util.*;
import java.awt.image.BufferedImage;

import javax.sound.sampled.*;
import java.io.File;

//###############################################################
abstract class GameObject
{
    public static World ItsWorld;
    public static int ScreenWidth = 640;
    public static int ScreenHeight = 480;
    
    public static int CellWidthPixels = 10;
    public static int CellHeightPixels = 10;
    public static int GridWidth = ScreenWidth/CellWidthPixels;
    public static int GridHeight = ScreenHeight/CellHeightPixels;   
    
    public static int KeyPressDelayCount = 0;
    public static int KeyPressDelayCountMax = 2;    
    
    public static final float pi = 3.141594f;
    public static final Random Rand = new Random();
    protected float x, y;
    protected float Angle;
    public int ActiveFlag = 1;
    
    public int DrawLayer = 0;
    public static final int DRAW_LAYER_MAX = 4;
    
    public static int vk_up = 0, vk_down = 0, vk_left = 0, vk_right = 0, vk_r = 0, vk_f = 0, vk_enter = 0;
    
    public final static int ID_NOTHING = 0, ID_INFORMATION_BAR = 1, ID_SNAKE_HEAD = 2, ID_SNAKE_SEGMENT = 3,
                    ID_PLAY_FIELD = 4;
    public int Id;
    
    public void Do() {};
    public void Draw( Graphics g ) {};
//---------------------------------------------------------------    
    protected void DrawOvalCenter( Graphics g, int x, int y, int width, int height )
    {
       g.drawOval( x - width/2, y - height/2, width, height );
    }
//---------------------------------------------------------------    
    protected void DrawFilledRect( Graphics g, int x, int y, int width, int height, Color c )
    {
       g.setColor(c);
       g.fillRect( x, y, width, height );
    }
//--------------------------------------------------------------- 
    public static int RandNum( int lower, int upper )
    {
      return Rand.nextInt(upper-lower+1) + lower;
    }   
//--------------------------------------------------------------- 
};        
//###############################################################
class SnakeSegment extends GameObject
{
   public static int TickCounterMax = 10;
   protected int TickCounter = 0;
   public static int LifeSpanMax = 3;
   public int LifeSpanCounter = 0;
   protected int GridX, GridY;
   protected Color ItsColor;
   
//---------------------------------------------------------------   
   public SnakeSegment( int nGridX, int nGridY )
   {
      int i; float angle; int distance;
      int min_radius, max_radius;
      
      Id = ID_SNAKE_SEGMENT;
      
      GridX = nGridX; GridY = nGridY;      
      x = GridX * CellWidthPixels;
      y = GridY * CellHeightPixels;
 
      ItsColor = new Color( 0, 255, 0 );
      DrawLayer = 1;
   }
//--------------------------------------------------------------- 
   public static void Reset()
   {
      LifeSpanMax = 3;
   }
//---------------------------------------------------------------   
   public void Do()
   {
      TickCounter++;
      
      if( TickCounter >= TickCounterMax )
      {
          TickCounter = 0;
          LifeSpanCounter++;
          if( LifeSpanCounter >= LifeSpanMax )
              ActiveFlag = 0;
      }
   }
//---------------------------------------------------------------   
   public void Draw( Graphics g ) 
   {
     DrawFilledRect( g, (int)x, (int)y, CellWidthPixels, CellHeightPixels, ItsColor );
   }  
//---------------------------------------------------------------   
} 
//###############################################################
class SnakeHead extends GameObject
{
   protected int GridX, GridY;
   protected Color ItsColor;
   protected int Xdir, Ydir;
   public static int TickCounterMax = 10;
   protected int TickCounter = 0;   
   public static SnakeHead me;
   protected static int CollideBoundaryFlag = 0;
   
//---------------------------------------------------------------   
public SnakeHead()
{
  Id = ID_SNAKE_HEAD;
  
  ItsColor = new Color( 255, 0, 0 );
  
  Init();
  
  DrawLayer = 2;
  me = this;
}
//---------------------------------------------------------------
public void Init()
{
  GridX = RandNum(1, GridWidth-2);    
  GridY = RandNum(1, GridHeight-2); 
  
  x = GridX * CellWidthPixels;
  y = GridY * CellHeightPixels;  
  int[] DirectionListX = { 0, 0, -1, 1 };
  int[] DirectionListY = { -1, 1, 0, 0 };
  int RandIndex = RandNum( 0, 3 );
  
  Xdir = DirectionListX[RandIndex];
  Ydir = DirectionListY[RandIndex];
  CollideBoundaryFlag = 0;
}
//---------------------------------------------------------------
protected void Move()
{
  TickCounter++;
  
 if( TickCounter >= TickCounterMax )
 {   
   TickCounter = 0;
 
   int GridX0 = GridX;
   int GridY0 = GridY;
   
   GridX += Xdir;
   GridY += Ydir;
 
   if( GridX < 0 || GridY < 0 || GridX > GridWidth - 1 || GridY > GridHeight - 1 )
   {
      GridX = GridX0; GridY = GridY0;      
      CollideBoundaryFlag = 1;
   }  
   
      x = GridX * CellWidthPixels;
      y = GridY * CellHeightPixels;   
      
   World.me.AddGameObject( new SnakeSegment( GridX, GridY ));
   CheckCollide();
 }  
}
//---------------------------------------------------------------
protected void ChangeDirection( int nXdir, int nYdir )
{
  if( nXdir + Xdir == 0 && nYdir + Ydir == 0 )
       ; // null statement
  else     
  {
     Xdir = nXdir; Ydir = nYdir;
  }  
}
//---------------------------------------------------------------
public void Do()
{
 Move();
 
 if( KeyPressDelayCount >= KeyPressDelayCountMax )
 {
   if( vk_left == 1 )
   {
       ChangeDirection( -1, 0 );    
       KeyPressDelayCount = 0; 
   } 
   else
   if( vk_right == 1 )
   {  
       ChangeDirection( 1, 0 );
       KeyPressDelayCount = 0;  
   }
   
   if( vk_up == 1 )
   {
     ChangeDirection( 0, -1 );
     KeyPressDelayCount = 0;  
   }
   else
   if( vk_down == 1 )
   {
     ChangeDirection( 0, 1 );
     KeyPressDelayCount = 0;  
   }   
   
 }
 

}
//---------------------------------------------------------------
public void Draw( Graphics g )
{
     DrawFilledRect( g, (int)x, (int)y, CellWidthPixels, CellHeightPixels, ItsColor );
}
//---------------------------------------------------------------
protected void CheckCollide()
{
  int i = 0, cell;
  int FatalCollisionFlag = 0, EatFoodFlag = 0;
  SnakeSegment ss;
  
  GameObject[] objlist = World.me.GetGameObjectList();    
  
  for( i = 0; i < objlist.length; i++ )
    if( objlist[i] != null )
     if( objlist[i].ActiveFlag >= 1 && objlist[i].Id == ID_SNAKE_SEGMENT )
     {
       ss = (SnakeSegment)objlist[i];
       if( ss.LifeSpanCounter != 0 && ss.GridX == GridX && ss.GridY == GridY )
       {
         FatalCollisionFlag = 1;
         break;
       }       
     }
     
  cell = PlayField.me.GetCell( GridX, GridY );
  
  if( cell == PlayField.ID_OBSTACLE )
      FatalCollisionFlag = 1;
  else
  if( cell == PlayField.ID_FOOD )
      EatFoodFlag = 1;
      
  if( CollideBoundaryFlag == 1 )
      FatalCollisionFlag = 1;
        
  if( FatalCollisionFlag >= 1 )
  {
      Init();
      World.me.RemoveObjectsId( ID_SNAKE_SEGMENT );
            
      if( InformationBar.me.Lives >= 2 )
      {
          PlayField.me.Init(InformationBar.me.Level);
          InformationBar.me.Lives--;     
      }
      else
      if( InformationBar.me.Lives <= 1 )
      {
        if( InformationBar.me.Score > InformationBar.me.HighScore )
            InformationBar.me.HighScore = InformationBar.me.Score;        
        InformationBar.me.Lives--;  
        
        InformationBar.me.GameOverFlag = 1;
      }
      SoundBox.me.Play(2);       
  }
  else
  if( EatFoodFlag >= 1 )
  {
     PlayField.me.SetCell( GridX, GridY, PlayField.me.ID_EMPTY );      
     SnakeSegment.LifeSpanMax++;
     InformationBar.me.Score += 10;
      
      if( PlayField.me.GetFoodCount() == 0 )
      {
         World.me.RemoveObjectsId( ID_SNAKE_SEGMENT );
         InformationBar.me.Level++;
         PlayField.me.Init(InformationBar.me.Level);
         Init();
         SoundBox.me.Play(3);
      }
    SoundBox.me.Play(0);
  }
}
//---------------------------------------------------------------
}
//###############################################################
class PlayField extends GameObject
{

public static PlayField me;
public static int Level;

public static final int ID_EMPTY = 0;
public static final int ID_OBSTACLE = 1;
public static final int ID_FOOD = 2;

public static final int OBSTACLE_LENGTH_CELLS = 5;

public static int[][] Map;

protected Color EmptyColor;
protected Color FoodColor;
protected Color ObstacleColor;

//---------------------------------------------------------------
public PlayField()
{
   Id = ID_PLAY_FIELD;
   me = this;
   Map = new int[GridWidth][GridHeight];
   
   FoodColor = new Color( 0, 0, 255 );
   ObstacleColor = new Color( 150, 150, 150 );
   DrawLayer = 0;
}
//---------------------------------------------------------------
public void Init( int nLevel )
{
   Level = nLevel;
   
   int X, Y, i;

   for( Y = 0; Y < GridHeight; Y++ )
    for( X = 0; X < GridWidth; X++ )
      Map[X][Y] = 0;   
      
   for( i = 1; i <= nLevel; i++ )
     CreateObstacle( RandNum( 0, GridWidth-1 ), RandNum( 0, GridHeight-1 ), RandNum( 0, 1 ) );
     
   CreateFood( nLevel );
}
//---------------------------------------------------------------
protected void CreateObstacle( int nGridX, int nGridY, int VerticalFlag )
{
  int Xdir, Ydir, i, X, Y;
  
  if( VerticalFlag >= 1 )
  {
     Xdir = 0; Ydir = 1;
  }    
  else
  {
     Xdir = 1; Ydir = 0;
  }
  
  X = nGridX; Y = nGridY;
  
  for( i = 1; i <= OBSTACLE_LENGTH_CELLS; i++ )  
  {
     Map[X][Y] = ID_OBSTACLE;
     X += Xdir;  Y += Ydir;
     
     if( X < 0 || Y < 0 || X > GridWidth - 1 || Y > GridHeight - 1 )
         break;
  }  
}
//---------------------------------------------------------------
protected void CreateFood( int Count )
{
   int X, Y, i, Xc, Yc, Xd, Yd;

for( i = 0; i < Count; i++ )
{
   X = RandNum( 0, GridWidth-1 );
   Y = RandNum( 0, GridHeight-1 );
   Map[X][Y] = ID_FOOD;
   
   for( Yc = -1; Yc <= 1; Yc++ )
    for( Xc = -1; Xc <= 1; Xc++ )
    {
       Xd = Xc + X;  Yd = Yc + Y;
       
       if( Xd < 0 || Yd < 0 || Xd > GridWidth - 1 || Yd > GridHeight - 1 )   
           continue;
 
       if( Map[Xd][Yd] == ID_OBSTACLE )
           Map[Xd][Yd] = ID_EMPTY;  
    }
}

}
//---------------------------------------------------------------
public int GetFoodCount()
{
  int FoodCount = 0;
  
   int X, Y, i;

   for( Y = 0; Y < GridHeight; Y++ )
    for( X = 0; X < GridWidth; X++ )
      if( Map[X][Y] == ID_FOOD )
        FoodCount++;

   return FoodCount;  
}
//---------------------------------------------------------------
public void Draw( Graphics g )
{
   int X, Y;
   Color c;
   
   for( Y = 0; Y < GridHeight; Y++ )
    for( X = 0; X < GridWidth; X++ )
    { 
      c = null;
      
      if( Map[X][Y] == ID_FOOD )
          c = FoodColor;
      else
      if( Map[X][Y] == ID_OBSTACLE )
          c = ObstacleColor;
      
      if( c != null )
         DrawFilledRect( g, X * CellWidthPixels, Y * CellHeightPixels, CellWidthPixels, CellHeightPixels, c );
    }
}
//---------------------------------------------------------------
public int GetCell( int nGridX, int nGridY )
{
   return Map[nGridX][nGridY];
}
//---------------------------------------------------------------
public void SetCell( int nGridX, int nGridY, int Value )
{
   Map[nGridX][nGridY] = Value;
}
//---------------------------------------------------------------
}
//###############################################################
class InformationBar extends GameObject
{

public int Score = 0;
public int Lives = 3;
public int Level = 1;
public int HighScore = 0;
public int GameOverFlag = 0;

public final static InformationBar me = new InformationBar();

//---------------------------------------------------------------
protected InformationBar()
{
   Init();
}
//---------------------------------------------------------------
public void Init()
{
  Score = 0; Lives = 3; Level = 1; GameOverFlag = 0;
}
//---------------------------------------------------------------
public void Draw( Graphics g ) 
{
  g.drawString("LIVES x " + Lives + "     SCORE " + Score + "     LEVEL " + Level + "    HIGHSCORE " + HighScore, 30, 20 );
  
  if( Lives <= 0 )
      g.drawString( "GAME OVER", GameObject.ScreenWidth/2 - 50, GameObject.ScreenHeight/2 );
} 
//---------------------------------------------------------------
public void Do()
{
  if( GameOverFlag == 1 )
   if( vk_enter == 1 )
   {
     Init();
     PlayField.me.Init(InformationBar.me.Level);
     SnakeHead.me.Init();
     World.me.RemoveObjectsId( ID_SNAKE_SEGMENT );
     SnakeSegment.Reset();
   }
}
//---------------------------------------------------------------
}
//###############################################################
class World
{
  public final static int MAX_GAME_OBJECT_COUNT = 500;
  protected static GameObject[] GameObjectList;
  
  public static final World me = new World();
//---------------------------------------------------------------  
  protected World()
  {
    int i;
    GameObjectList = new GameObject[MAX_GAME_OBJECT_COUNT];
    for( i = 0; i < MAX_GAME_OBJECT_COUNT; i++ )
         GameObjectList[i] = null;
    GameObject.ItsWorld = this;
  }
//---------------------------------------------------------------  
  public static void AddGameObject( GameObject obj )
  {
    int i;
    for( i = 0; i < MAX_GAME_OBJECT_COUNT; i++ )
      if( GameObjectList[i] == null )
      {
          GameObjectList[i] = obj;
          break;
      }
  }
//---------------------------------------------------------------  
  public static void AddGameObjectArray( GameObject[] objlist )
  {
    int i, length = objlist.length;
    
    for( i = 0; i < length; i++ )
         AddGameObject( objlist[i] );
  }
//---------------------------------------------------------------  
  public GameObject[] GetGameObjectList()
  {
     return GameObjectList;
  }
//---------------------------------------------------------------  
  public static void Do()
  {
    if( InformationBar.me.GameOverFlag == 0 )
    {
        
    int i;
    for( i = 0; i < MAX_GAME_OBJECT_COUNT; i++ )
      if( GameObjectList[i] != null )
      {
        if( GameObjectList[i].ActiveFlag == 0 )        
           GameObjectList[i] = null;
        else
           GameObjectList[i].Do();           
      }
    }
  
    InformationBar.me.Do();
   
        GameObject.KeyPressDelayCount++;
        
        if( GameObject.KeyPressDelayCount >= GameObject.KeyPressDelayCountMax )
            GameObject.KeyPressDelayCount = GameObject.KeyPressDelayCountMax;              
  }
//---------------------------------------------------------------  
  public static void Draw( Graphics g )
  {
    int i, k;
    
   for( k = 0; k <= GameObject.DRAW_LAYER_MAX; k++ )   
    for( i = 0; i < MAX_GAME_OBJECT_COUNT; i++ )
      if( GameObjectList[i] != null )
        if( GameObjectList[i].DrawLayer == k )    
          GameObjectList[i].Draw(g);
          
    InformationBar.me.Draw(g);
  }
//--------------------------------------------------------------- 
public void RemoveObjectsId( int Id )
{
    int i;
    for( i = 0; i < MAX_GAME_OBJECT_COUNT; i++ )
      if( GameObjectList[i] != null )
       if( GameObjectList[i].Id == Id )
         GameObjectList[i] = null;
} 
//--------------------------------------------------------------- 
}
//###############################################################
class SoundBox
{

public static final SoundBox me = new SoundBox();
public static final int MAX_SOUND_COUNT = 4;

protected Clip[] ClipList;

//--------------------------------------------------------------- 
protected SoundBox()
{
  ClipList = new Clip[MAX_SOUND_COUNT];
  LoadClip( 0, "projectile.wav");
  LoadClip( 1, "breakup.wav");
  LoadClip( 2, "death.wav" );   
  LoadClip( 3, "warp.wav" );   
}
//--------------------------------------------------------------- 
public void LoadClip( int index, String filename )
{
try
{
   ClipList[index] = AudioSystem.getClip();
   File SoundFile =  new File(filename);
   
   if( !SoundFile.isFile())
   {
      ClipList[index] = null;
      return;
   }         
   AudioInputStream ais_obj = AudioSystem.getAudioInputStream(SoundFile);
   ClipList[index].open(ais_obj);
}
catch(Exception e)
{
}

}
//--------------------------------------------------------------- 
public void Play( int index )
{
   if( ClipList[index] != null )
       ClipList[index].loop(1);
}
//--------------------------------------------------------------- 
}
//###############################################################
//* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
//###############################################################  
public class snakegame extends JApplet     
implements KeyListener, MouseListener, Runnable
{     
      int MouseX, MouseY, MouseState;
      int KeyCode;
      int ScreenWidth, ScreenHeight;
      int FontWidth, FontHeight;
      Thread MainThread;

      Random Rand = new Random();     

      BufferedImage GraphicsBuffer = null;       
      Graphics GraphicsObj;
      
//--------------------------------------------------------------- 
   
      public void init() 
      {       
       int i;              
             
       addKeyListener( this );
       addMouseListener( this );

       ScreenWidth = getSize().width;
       ScreenHeight = getSize().height;
       setBackground(Color.black);
       setForeground(Color.green);
       
       GameObject.ScreenWidth = ScreenWidth;
       GameObject.ScreenHeight = ScreenHeight;
       
       World.me.AddGameObject( new SnakeHead() );
       World.me.AddGameObject( new PlayField() );
       PlayField.me.Init( InformationBar.me.Level );
       
       GraphicsBuffer = new BufferedImage(ScreenWidth, ScreenHeight, BufferedImage.TYPE_INT_RGB);
       GraphicsObj = GraphicsBuffer.createGraphics();
       
       FontWidth = FontHeight = 10;    
       setFocusable(true);
       MouseState = 0;
       KeyCode = 0;       
      }
//---------------------------------------------------------------  
  public void start()
  {
    MainThread = new Thread(this);
    MainThread.start();
  }
//---------------------------------------------------------------  
  public void run()
  {
    long FramesPerSecond = 30;         
    long TicksPerSecond = 1000 / FramesPerSecond;
    long StartTime;
    long SleepTime;
    long ActualSleepTime;
  
    while (MainThread != null ) 
    {    
      StartTime = System.currentTimeMillis();
      repaint();
      
      SleepTime = TicksPerSecond-(System.currentTimeMillis() - StartTime);
 
      if (SleepTime > 0)
          ActualSleepTime = SleepTime;
      else
          ActualSleepTime = 10;    
      try 
      {
        MainThread.sleep(ActualSleepTime);
      } 
      catch (InterruptedException e) 
      {
      }          
    }      
  }
//---------------------------------------------------------------  
  public void stop()
  {
     MainThread = null;
  }
//--------------------------------------------------------------- 
  public void update(Graphics g)
  {    
  }  
//---------------------------------------------------------------  
      public void paint(Graphics g)
      {       
         
         Font f = new Font("monospace", Font.PLAIN, 16);   
         
         GraphicsObj.setFont(f);          
         GraphicsObj.setColor(Color.black);
         GraphicsObj.fillRect(0,0,ScreenWidth,ScreenHeight);          
         GraphicsObj.setColor(new Color(100,100,255));         

         World.me.Do();
         World.me.Draw(GraphicsObj); 

         g.drawImage(GraphicsBuffer, 0, 0, this);
        
      }      
//---------------------------------------------------------------  
   public void keyPressed( KeyEvent e ) 
     { 
      char c = e.getKeyChar();
      int k = e.getKeyCode();
      KeyCode = k;

          if( KeyEvent.VK_UP == k )
              GameObject.vk_up = 1;
          else
          if( KeyEvent.VK_DOWN == k  )
              GameObject.vk_down = 1;
          else
          if( KeyEvent.VK_LEFT == k )
              GameObject.vk_left = 1;
          else 
          if( KeyEvent.VK_RIGHT == k )
              GameObject.vk_right = 1;             
          else 
          if( KeyEvent.VK_F == k )
              GameObject.vk_f = 1; 
          else 
          if( KeyEvent.VK_R == k )
              GameObject.vk_r = 1;   
          else 
          if( KeyEvent.VK_ENTER == k )
              GameObject.vk_enter = 1;               
       
          e.consume();  
     }
//---------------------------------------------------------------       
   public void keyReleased( KeyEvent e ) 
     {
      char c = e.getKeyChar();
      int k = e.getKeyCode();
      KeyCode = k;

          if( KeyEvent.VK_UP == k )
              GameObject.vk_up = 0;
          else
          if( KeyEvent.VK_DOWN == k  )
              GameObject.vk_down = 0;
          else
          if( KeyEvent.VK_LEFT == k )
              GameObject.vk_left = 0;
          else 
          if( KeyEvent.VK_RIGHT == k )
              GameObject.vk_right = 0;             
          else 
          if( KeyEvent.VK_F == k )
              GameObject.vk_f = 0; 
          else 
          if( KeyEvent.VK_R == k )
              GameObject.vk_r = 0; 
          else 
          if( KeyEvent.VK_ENTER == k )
              GameObject.vk_enter = 0;                
       
          e.consume();       
     }
//---------------------------------------------------------------       
   public void keyTyped( KeyEvent e ) 
   {
   }
//---------------------------------------------------------------  
   public void mouseEntered( MouseEvent e ) 
   { 
   }
//---------------------------------------------------------------     
   public void mouseExited( MouseEvent e ) 
   { 
   }
//--------------------------------------------------------------- 
   public void mouseMoved( MouseEvent e )
   {
   }   
//---------------------------------------------------------------
  public void mouseDragged(MouseEvent e) 
  {
  }     
//---------------------------------------------------------------  
   public void mousePressed( MouseEvent e ) 
   {
      MouseX = e.getX();
      MouseY = e.getY();
      MouseState = 1;
      e.consume(); 
   }
//---------------------------------------------------------------     
   public void mouseReleased( MouseEvent e ) 
   { 
      MouseX = e.getX();
      MouseY = e.getY();
      MouseState = 0;      
      e.consume();   
   }
//---------------------------------------------------------------     
   public void mouseClicked( MouseEvent e ) 
   {
   }       
//------------------------------------------------------   
} ///:~