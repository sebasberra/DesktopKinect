import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import java.io.*;
import javax.imageio.*;
import java.util.*;
import java.text.DecimalFormat;

import java.nio.ByteBuffer;

import org.OpenNI.*;
import com.primesense.NITE.*;


// Estados del tracking
enum SessionState {
    IN_SESSION, NOT_IN_SESSION, QUICK_REFOCUS
}


/** Procesamiento de imagenes 2014 
*
* TP final - Escritorio con Kinect - Visor Kinect - Clase JPanel que refresca la visualizacion
*
* Basado en ViewerPanel.java v3 de Andrew Davison, August 2011, ad@fivedots.psu.ac.th
*
* @author Agostini, Juan - Berra, Sebastian - Nepotti, Joaquin
*/

public class JPanelDesktopKinect extends JPanel implements Runnable
{

  private static final int MAX_DEPTH_SIZE = 10000;

  private static final int CIRCLE_SIZE = 25;

  private static final int ZTOL = 150;
  
  // image vars
  private BufferedImage image = null;
  private int imWidth, imHeight;

  private int scWidth, scHeight;
  private float escalaX, escalaY;
  
  private volatile boolean isRunning;

  // used for the average ms processing information
  private int imageCount = 0;
  private long totalTime = 0;
  private DecimalFormat df;
  private Font msgFont;

  // OpenNI and NITE vars
  private Context context;
  private Player player;
  private ImageGenerator imageGen;
  private DepthGenerator depthGen;

  private SessionManager sessionMan;
  private SessionState sessionState;

  
  // configuracion de aplicaciones
  private String id_ventana = null;
  private String archivo_oni = null;
  private int prog_id = 0;
  private Point pt;
  
  // flags manejo de comportamiento del mouse
  private int flag_zreal;
  private boolean flag_mouseactivo;
  private boolean flag_mousedown;
  private boolean flag_global_mousedown_activo;
  private boolean flag_global_click_activo;

  /** Constructor de la clase
  *
  * @param id_ventana	id de la ventana donde se mandaran el eventos de teclas y mouse
  * @param archivo_oni  archivo de captura OpenNI o Null en caso de usar el Kinect conectado
  * @param prog_id	1:tuxpaint 2:okular 3:xbmc 4:libreoffice (presentacion) programas para los
  *			cuales se han configurado alguna caracterista especifica, si id es distinto
  *			se usaran los eventos por defecto
  */
  public JPanelDesktopKinect(String id_ventana, String archivo_oni, int prog_id)
  {
    
    // propiedades
    this.id_ventana = id_ventana;
    this.archivo_oni = archivo_oni;
    this.prog_id = prog_id;
    
    // point para dibujar el circulo que sigue la mano
    pt = new Point();
    pt.x = 0;
    pt.y = 0;
    
    iniciarFlags();
    
    setBackground(Color.DARK_GRAY);

    df = new DecimalFormat("0.#");  // 1 dp
    msgFont = new Font("SansSerif", Font.BOLD, 18);

    configKinect();


    // hilo principal
    new Thread(this).start();   
    
  } 


  /** Configura las clase de OpenNI y NITE
  *
  */
  private void configKinect()
  
  {
    try {
    
      context = new Context();
      
      // abre archivo .oni
      if (archivo_oni != null) {
      
	// inicializa objeto Player
	player = context.openFileRecordingEx(archivo_oni);
	
      }

      
      // Licencia NITE
      License licence = new License("PrimeSense", "0KOIk2JeIBYClPWVnMoRKn5cdY4=");
      context.addLicense(licence);

      
      //
      // ImageGenerator
      //
   
      imageGen = ImageGenerator.create(context);
      
      
      //
      // DepthGenerator
      //
      
      depthGen = DepthGenerator.create(context);
           

      // Kinect conectado
      if (archivo_oni==null){
      
	MapOutputMode mapMode = new MapOutputMode(640, 480, 30);   // xRes, yRes, FPS
	imageGen.setMapOutputMode(mapMode);
	depthGen.setMapOutputMode(mapMode);

	imageGen.setPixelFormat(PixelFormat.RGB24);
	
	context.setGlobalMirror(true);
	
      }

      
      // establece las escalas de mapeo entre la vision de kinect 
      // y la configuracion de la pantalla de la PC
      setDimensiones();
      
      
      //
      // HandsGenerator
      //
      
      HandsGenerator hands = HandsGenerator.create(context);
      hands.SetSmoothing(0.1f);

      GestureGenerator gesture = GestureGenerator.create(context);

      
      //
      // inicio la generacion del contexto [OpenNI]
      //
      
      context.startGeneratingAll();
      System.out.println("Generacion del contexto OpenNI iniciada...");

      
      //
      // session manager [NITE] 
      //
      
      sessionMan = new SessionManager(context, "Click,Wave", "RaiseHand");
          
      setSessionEvents(sessionMan);
      sessionState = SessionState.NOT_IN_SESSION;

      // pointcontrol listener
      PointControl pointCtrl = initPointControl();
      sessionMan.addListener(pointCtrl);
      
      // wavedetector listener
      WaveDetector wave = initWaveDetector();
      sessionMan.addListener(wave);
      
      // pushDetector
      PointControl push = initPushDetector();
      sessionMan.addListener(push);
      
      SwipeDetector swipe = initSwipeDetector();
      sessionMan.addListener(swipe);
      
      
    }
    catch (GeneralException e) {
      e.printStackTrace();
      
      DesktopKinect.cerrarAplicacion(1);
    }
  }  
  // fin configKinect()



  private void setSessionEvents(SessionManager sessionMan)
  // create session callbacks
  {
    try {
      // session start (S1)
      sessionMan.getSessionStartEvent().addObserver( new IObserver<PointEventArgs>() {
        public void update(IObservable<PointEventArgs> observable, PointEventArgs args)
        { System.out.println("Evento comienzo de sesion (SessionManager)");
          sessionState = SessionState.IN_SESSION;
        }
      });

      // session end (S2)
      sessionMan.getSessionEndEvent().addObserver( new IObserver<NullEventArgs>() {
        public void update(IObservable<NullEventArgs> observable, NullEventArgs args)
        { System.out.println("Evento fin de sesion (SessionManager)");
          isRunning = false;
          sessionState = SessionState.NOT_IN_SESSION;
        }
      });
    }
    catch (StatusException e) {
      e.printStackTrace();
    }
  }  
  // fin setSessionEvents()




  /** PointControl para seguimiento del mouse con la mano
  *
  */
  private PointControl initPointControl()
  
  
  {
    PointControl pointCtrl = null;
    try {
      pointCtrl = new PointControl();

      // Hand Point
      pointCtrl.getPointCreateEvent().addObserver( new IObserver<HandEventArgs>() {
        public void update(IObservable<HandEventArgs> observable, HandEventArgs args)
        {
        
          sessionState = SessionState.IN_SESSION;
          
          HandPointContext handContext = args.getHand();
          
          int id = handContext.getID();
                    
          Point3D posReal = handContext.getPosition();
          
          System.out.println("Creacion de nueva mano "+ id);
          System.out.println("X = "+ posReal.getX() + " Y = " + posReal.getY() + " Z = " + posReal.getZ() );
          
          if (posReal!=null){
          
	    movermouse(posReal);
	    
	  }
          
        }
      });

      // movimiento de mano (P2)
      pointCtrl.getPointUpdateEvent().addObserver( new IObserver<HandEventArgs>() {
        public void update(IObservable<HandEventArgs> observable, HandEventArgs args)
        {
          sessionState = SessionState.IN_SESSION;
          HandPointContext handContext = args.getHand();
          int id = handContext.getID();
          
          
          Point3D posReal = handContext.getPosition();
          
          if (posReal!=null){
          
	    movermouse(posReal);
	    
	  }
	  
	  
        }
      });

      // destroy hand point and its trail (P3)
      pointCtrl.getPointDestroyEvent().addObserver( new IObserver<IdEventArgs>() {
        public void update(IObservable<IdEventArgs> observable, IdEventArgs args)
        {
          int id = args.getId();
          System.out.println("Destroy mano "+ id);
        }
      });

      // no active hand point, which triggers refocusing (P4)
      pointCtrl.getNoPointsEvent().addObserver( new IObserver<NullEventArgs>() {
        public void update(IObservable<NullEventArgs> observable, NullEventArgs args)
        {
          if (sessionState != SessionState.NOT_IN_SESSION) {
            System.out.println("  Se perdio la mano, entonces comienza refocusing");
            sessionState = SessionState.QUICK_REFOCUS;
          }
        }
      });

    }
    catch (GeneralException e) {
      e.printStackTrace();
    }
    return pointCtrl;
  }  
  // fin initPointControl()


  /** WaveDetector para deteccion del gesto WAVE
  *
  */
  private WaveDetector initWaveDetector()
  {
    WaveDetector waveDetector = null;
    try {
      waveDetector = new WaveDetector();

      // some wave settings; change with set
      waveDetector.setFlipCount(3);
      int flipCount = waveDetector.getFlipCount();
      int flipLen = waveDetector.getMinLength();
      System.out.println(
		"Wave settings -- nro. de flips: " + 
		flipCount +
                "; min longitud: " + 
                flipLen + 
                "mm");  

      // callback
      waveDetector.getWaveEvent().addObserver( new IObserver<NullEventArgs>() {
        public void update(IObservable<NullEventArgs> observable, NullEventArgs args)
        { 
        
          System.out.println("Gesto wave detectado");
          
          if (flag_mouseactivo==false){
          
	    System.out.println("Puntero de mouse activado");
	    flag_mouseactivo=true;
	    
          }else{
          
          
	    if (flag_mousedown==false){
	    
	      System.out.println("Puntero de mouse desactivado");
	      resetFlags();
	      
	    }
	    
	  }
          
        }
      });
    }
    catch (GeneralException e) {
      e.printStackTrace();
    }
    return waveDetector;
  }  
  // fin initWaveDetector()
  
 
  /** Inicializa el detector de Gesto Click
  */
  public PushDetector initPushDetector()
  {
    PushDetector pushDetector = null;
    try {
      pushDetector = new PushDetector();

      
      // settings; se cambia con set
      
      // minimum velocity in the time span to define as push, in m/s
      float minVel = pushDetector.getPushImmediateMinimumVelocity();
            
      // time used to detect push, in ms
      float duration = pushDetector.getPushImmediateDuration();
            
      // max angle between immediate direction and  Z-axis, in degrees
      float angleZ = pushDetector.getPushMaximumAngleBetweenImmediateAndZ();
           
      // muestra la configuracion del push
      System.out.printf("Push settings -- min velocidad: %.1f m/s; min duracion: %.1f ms; max angulo eje-z: %.1f grados \n",
                            minVel, duration, angleZ);  

      // callback
      pushDetector.getPushEvent().addObserver( new IObserver<VelocityAngleEventArgs>() {
        public void update(IObservable<VelocityAngleEventArgs> observable, 
                           VelocityAngleEventArgs args)
        { 
          System.out.printf("Push: velocidad %.1f m/s, angulo %.1f grados \n",
                                               args.getVelocity(), args.getAngle());
          
          // check que no este en esta down y que el mouse este activo y el click este activo
          if (
	    flag_mousedown==false && 
	    flag_mouseactivo && 
	    flag_global_click_activo){
          
	    switch(prog_id){
	    
	      // tuxpaint
	      case 1:
	      
		System.out.println("tuxpaint click mouse");
		// click mouse                              
		XDoTool.clickXDoTool(id_ventana,"1");
		break;
	      
	      // okular
	      case 2:
	      
		System.out.println("okular click mouse");
		// click mouse                              
		XDoTool.clickXDoTool(id_ventana,"1");
		break;
		
	      // xbmc
	      case 3:
		System.out.println("xbmc click mouse");
		// click mouse                              
		XDoTool.clickXDoTool(id_ventana,"1");
		break;
		
	      // libreoffice (presentacion)
	      case 4:
		System.out.println("libreoffice click mouse");
		// click mouse                              
		XDoTool.clickXDoTool(id_ventana,"1");
		break;
		
	      default: 
	      
		System.out.println("ventana: " + id_ventana + " click mouse");
		// click mouse                              
		XDoTool.clickXDoTool(id_ventana,"1");
		break;
	      
	    }
	    
	    
	    
	  }
          
        }
        
      });
    }
    catch (GeneralException e) {
      e.printStackTrace();
    }
    return pushDetector;
  }  
  // fin initPushDetector()
  
  
  /** Inicializa el detector de evento golpe
  */
  public SwipeDetector initSwipeDetector()
  {
    SwipeDetector swipeDetector = null;
    try {
      swipeDetector = new SwipeDetector();

      // some swipe settings; change with set
      System.out.println("Golpe setting -- min timpo de movimiento: " + 
                                      swipeDetector.getMotionTime() + " ms");  

      // general swipe callback
      swipeDetector.getGeneralSwipeEvent().addObserver( 
                             new IObserver<DirectionVelocityAngleEventArgs>() {
        public void update(IObservable<DirectionVelocityAngleEventArgs> observable, 
                           DirectionVelocityAngleEventArgs args)
        { 
          System.out.printf("Golpe %s: velocity %.1f m/s, angulo %.1f grados \n",
                           args.getDirection(), args.getVelocity(), args.getAngle());  
          
        }
      });

      // callback for left swipes only;
      swipeDetector.getSwipeLeftEvent().addObserver( 
                             new IObserver<VelocityAngleEventArgs>() {
        public void update(IObservable<VelocityAngleEventArgs> observable, 
                           VelocityAngleEventArgs args)
        { 
          System.out.printf("Golpe izquierda: velocidad %.1f m/s, angulo %.1f grados \n",
                                       args.getVelocity(), args.getAngle());
          
          if (flag_mousedown==false && flag_mouseactivo){
          
          
	    switch(prog_id){
	    
	      // tuxpaint
	      case 1:
	      
		break;
	      
	      // okular
	      case 2:
	      
		System.out.println("Okular tecla enviada: shift+alt+Left");
		XDoTool.key(id_ventana,"shift+alt+Left");
		break;
		
	      // xbmc
	      case 3:
	      
		break;
		
	      // libreoffice
	      case 4:
	      
		System.out.println("libreoffice tecla enviada: Left");
		XDoTool.key(id_ventana,"Left");
		break;
		
	      default: 
	      
		break;
	      
	    }
          
          }
          
        }
      });
      
      // callback for right swipes only;
      swipeDetector.getSwipeRightEvent().addObserver( 
                             new IObserver<VelocityAngleEventArgs>() {
        public void update(IObservable<VelocityAngleEventArgs> observable, 
                           VelocityAngleEventArgs args)
        { 
          System.out.printf("Golpe derecha: velocidad %.1f m/s, angulo %.1f grados \n",
                                       args.getVelocity(), args.getAngle());
          
          // check que no este en esta down y que el mouse este activo
          if (flag_mousedown==false && flag_mouseactivo){
          
          
	    switch(prog_id){
	    
	      // tuxpaint
	      case 1:
	      
		break;
	      
	      // okular
	      case 2:
	      
		System.out.println("Okular tecla enviada: shift+alt+Right");
		XDoTool.key(id_ventana,"shift+alt+Right");
		break;
		
	      // xbmc
	      case 3:
	      
		break;
		
	      // libreoffice
	      case 4:
	      
		System.out.println("libreoffice tecla enviada: Right");
		XDoTool.key(id_ventana,"Right");
		break;
		
	      default: 
	      
		
		break;
	      
	    }
	  }  
          
        }
      });
      
      // callback for up swipes only;
      swipeDetector.getSwipeUpEvent().addObserver( 
                             new IObserver<VelocityAngleEventArgs>() {
        public void update(IObservable<VelocityAngleEventArgs> observable, 
                           VelocityAngleEventArgs args)
        { 
          System.out.printf("Golpe arriba: velocidad %.1f m/s, angulo %.1f grados \n",
                                       args.getVelocity(), args.getAngle());
          
          
          // check que no este en esta down y que el mouse este activo
          if (flag_mousedown==false && flag_mouseactivo){
          
          
	    switch(prog_id){
	    
	      // tuxpaint
	      case 1:
	      
		// rueda mouse arriba                              
		XDoTool.clickXDoTool(id_ventana,"4");
		break;
	      
	      // okular
	      case 2:
	      
		System.out.println("Okular tecla enviada: shift+Control_L+P");
		XDoTool.key(id_ventana,"shift+Control_L+P");
		break;
		
	      // xbmc
	      case 3:
	      
		// rueda mouse arriba                              
		XDoTool.clickXDoTool(id_ventana,"4");
		break;
		
	      // libreoffice
	      case 4:
	      
		System.out.println("libreoffice tecla enviada: F5");
		XDoTool.key(id_ventana,"F5");
		break;
		
	      default: 
	      
		// rueda mouse arriba                              
		XDoTool.clickXDoTool(id_ventana,"4");
		
		break;
	      
	    }
	  }  
          
        }
      });
      
      // callback for abajo swipes only;
      swipeDetector.getSwipeDownEvent().addObserver( 
                             new IObserver<VelocityAngleEventArgs>() {
        public void update(IObservable<VelocityAngleEventArgs> observable, 
                           VelocityAngleEventArgs args)
        { 
          System.out.printf("Golpe Abajo: velocidad %.1f m/s, angulo %.1f grados \n",
                                       args.getVelocity(), args.getAngle()); 
          
          // check que no este en esta down y que el mouse este activo
          if (flag_mousedown==false && flag_mouseactivo){
          
          
	    switch(prog_id){
	    
	      // tuxpaint
	      case 1:
	      
		// rueda mouse abajo
		System.out.println("tuxpaint tecla enviada: Down");
		XDoTool.clickXDoTool(id_ventana,"5");
		break;
	      
	      // okular
	      case 2:
	      
		System.out.println("Okular tecla enviada: Escape");
		XDoTool.key(id_ventana,"Escape");
		break;
		
	      // xbmc
	      case 3:
	      
		// rueda mouse abajo                              
		System.out.println("xbmc tecla enviada: Down");
		XDoTool.clickXDoTool(id_ventana,"5");
		break;
		
	      // libreoffice
	      case 4:
	      
		System.out.println("libreoffice tecla enviada: Escape");
		XDoTool.key(id_ventana,"Escape");
		break;
		
	      default: 
	      
		// rueda mouse abajo                              
		XDoTool.clickXDoTool(id_ventana,"5");
		
		break;
	      
	    }
	  }
          
        }
      });
      
    }
    catch (GeneralException e) {
      e.printStackTrace();
    }
    return swipeDetector;
  }  
  // fin initSwipeDetector()
  


  /* Cierra la ventana del visor, entonces termina el hilo del kinect
  */
  public void closeDown()
  {  isRunning = false;  }


  
  /* Hilo principal del Kinect
  */
  public void run()
  /* update the Kinect info and redraw
  */
  {
    isRunning = true;
    while (isRunning) {
      try {
        context.waitAnyUpdateAll();
        sessionMan.update(context);
      }
      catch(StatusException e)
      {  System.out.println(e);
         System.exit(1);
      }
      long startTime = System.currentTimeMillis();
      updateCameraImage();
      totalTime += (System.currentTimeMillis() - startTime);
      repaint();
    }

    // close down
    try {
      context.stopGeneratingAll();
    }
    catch (StatusException e) {}
    context.release();
    System.exit(1);
  }  // end of run()




  private void updateCameraImage()
  // update Kinect camera's image
  {
    try {
      ByteBuffer imageBB = imageGen.getImageMap().createByteBuffer();
      image = bufToImage(imageBB);
      imageCount++;
    }
    catch (GeneralException e) {
      System.out.println(e);
    }
  }  // end of updateCameraImage()


  private BufferedImage bufToImage(ByteBuffer pixelsRGB)
  /* Transform the ByteBuffer of pixel data into a BufferedImage
     Converts RGB bytes to ARGB ints with no transparency.
  */
  {
    int[] pixelInts = new int[imWidth * imHeight];

    int rowStart = 0;
        // rowStart will index the first byte (red) in each row;
        // starts with first row, and moves down

    int bbIdx;               // index into ByteBuffer
    int i = 0;               // index into pixels int[]
    int rowLen = imWidth * 3;    // number of bytes in each row
    for (int row = 0; row < imHeight; row++) {
      bbIdx = rowStart;
      // System.out.println("bbIdx: " + bbIdx);
      for (int col = 0; col < imWidth; col++) {
        int pixR = pixelsRGB.get( bbIdx++ );
        int pixG = pixelsRGB.get( bbIdx++ );
        int pixB = pixelsRGB.get( bbIdx++ );
        pixelInts[i++] =
           0xFF000000 | ((pixR & 0xFF) << 16) |
           ((pixG & 0xFF) << 8) | (pixB & 0xFF);
      }
      rowStart += rowLen;   // move to next row
    }

    // create a BufferedImage from the pixel data
    BufferedImage im =
       new BufferedImage( imWidth, imHeight, BufferedImage.TYPE_INT_ARGB);
    im.setRGB( 0, 0, imWidth, imHeight, pixelInts, 0, imWidth );
    return im;
  }  // end of bufToImage()




  // -------------------- drawing ---------------------------------


  public void paintComponent(Graphics g)
  /* Draw the camera image, hand trails, user instructions
     and statistics. */
  {
    super.paintComponent(g);
    Graphics2D g2 = (Graphics2D) g;

    if (image != null)
      g2.drawImage(image, 0, 0, this);    // draw camera's image

    if (pt.x!=0 && pt.y!=0){  
    
      if (flag_mouseactivo==false){
      
	g2.setColor(Color.BLACK);
	
      }else{
      
	if (flag_mousedown){
	
	  g2.setColor(Color.RED);
	  
	}else{
	
	  g2.setColor(Color.GREEN);
	  
	}
	
      }
      g2.fillOval(pt.x-CIRCLE_SIZE/2, pt.y-CIRCLE_SIZE/2, CIRCLE_SIZE, CIRCLE_SIZE);
    }
    
    writeMessage(g2);

    writeStats(g2);
  } 
  // fin paintComponent()

  
  public Dimension getPreferredSize()
  { return new Dimension(imWidth, imHeight); }

  
  private void writeMessage(Graphics2D g2)
  // draw user information based on the session state
  {
    g2.setColor(Color.YELLOW);
    g2.setFont(msgFont);

    String msg = null;
    switch (sessionState) {
      case IN_SESSION:
        msg = "Tracking...";
        break;
      case NOT_IN_SESSION:
        msg = "Click/Wave para comenzar el tracking";
        break;
      case QUICK_REFOCUS:
        msg = "Click/Wave/Raise para recomenzar el tracking";
        break;
    }
    if (msg != null)
      g2.drawString(msg, 5, 20);  // top left
  }  
  // fin writeMessage()


  /** Muestra en el visor del kinect la cantidad de frames y el promedio
  */
  private void writeStats(Graphics2D g2)
  {
  
    g2.setColor(Color.GREEN);
    g2.setFont(msgFont);
    
    int panelHeight = getHeight();
    
    /* estadisticas o "Cargando" al comienzo */
    if (imageCount > 0) {
      double avgGrabTime = (double) totalTime / imageCount;
      g2.drawString("Img " + imageCount + "  " +
                   df.format(avgGrabTime) + " ms",
                   5, panelHeight-10);  // bottom left
    }
    else  // no image yet
      g2.drawString("Cargando...", 5, panelHeight-10);
  }  
  // fin writeStats()

  
  
  /** Convierte las coordenadas del kinect a coordenadas de pantalla
  *
  * @param posReal	Coordenadas del mundo real (kinect)
  */
  private Point3D getCoordenadasPantalla(Point3D posReal){
  
    Point3D posPant = null;
    
    try {
    
      // convierte coordenada del mundo real a coordenadas de pantalla
      posPant = depthGen.convertRealWorldToProjective(posReal);
	
    }catch (StatusException e) {  
    
      // mensaje de error
      System.out.println("Problema convertir coordenada del mundo real a pantalla"); 
      System.out.println("El trazado de la pila devolvio:");
      e.printStackTrace();
      
      DesktopKinect.cerrarAplicacion(1);
      
    }
    
    return posPant;
    
  }
  
  
  /** Funcion que calcula la posicion de 'x' de la pantalla segun
  * la posicion 'x' de las coordenadas de la pantalla del Kinect
  * 
  * @param x Coordenada x de la pantalla del Kinect
  */
  private int escalarX(int x){
  
    int res;
    
    res = (int)(escalaX*(float)x);
    
    return res;
  }
  
  
  /** Funcion que calcula la posicion de 'y' de la pantalla segun
  * la posicion 'y' de las coordenadas de la pantalla del Kinect
  *
  * @param y Coordenada y de la pantalla del Kinect
  */
  private int escalarY(int y){
  
    int res;
    
    res = (int)(escalaY*(float)y);
    
    return res;
  }
  
  
  
  /** Mueve el mouse tomando las coordenadas reales, haciendo el
  * mapeo a las coordenadas de la ventana del kinect, y luego
  * escalando a las coordenadas de la configuracion de pantalla
  *
  * @param posicionMundoReal Coordenadas del mundo real (kinect)
  */
  private void movermouse(Point3D posicionMundoReal){
  
  
    Point3D posPant = getCoordenadasPantalla(posicionMundoReal);
    
    int x = (int)posPant.getX();
    int scX = escalarX(x);
    
    int y = (int)posPant.getY();
    int scY = escalarY(y);
    
    pt.x = x;
    pt.y = y;
    
    
    // check si el puntero de mouse esta activo
    if (flag_mouseactivo){
    
      
      //
      // check mouse down - up esta activado
      if (flag_global_mousedown_activo){
      
      
	// check inicializacion de la posicion de z para mouse down
	if (flag_zreal==0){
	
	  // inicializa la posicion z
	  flag_zreal = (int)posicionMundoReal.getZ();
	  
	}else{
	
	  //
	  // z esta inicializada, ahora controla el mouse down-up
	  
	  // posicion de z mundo real actual
	  int z = (int)posicionMundoReal.getZ();
	  
	  // check si acciona, libera o no hace nada con el mouse down-up
	  if (flag_mousedown){
	  
	    // check sigue presionando
	    if (z<flag_zreal){
	    
	      flag_zreal = z;
	      
	    }else{
	    
	      // check libera mouse press
	      if (z>flag_zreal+ZTOL){
	      
		flag_mousedown=false;
		flag_zreal = z;
		
		//XDoTool.mouseUp(id_ventana,"1");
		XDoTool.mouseUpJava(1);
		System.out.println("Mouse up en z = " + z);
	      }
	      
	    } 
	      
	  }else{
	  
	    // check si realiza mouse down
	    if (z>flag_zreal){
	    
	      flag_zreal = z;
	      
	    }else{
	    
	      // check libera mouse press
	      if (z<flag_zreal-ZTOL){
	      
		flag_mousedown=true;
		flag_zreal = z;
		
		//XDoTool.mouseDown(id_ventana,"1");
		XDoTool.mouseDownJava(1);
		
		System.out.println("Mouse down en z = " + z);
		
	      } // check tolerancia de mouse down
	      
	    } // fin ejecuta mouse down
	  
	  } // fin check si mouse down o up
	  
	} // fin check inicializacion de la posicion de z para mouse down
	
      }
            
      
      // movimiento de mouse
      XDoTool.mouseMove(scX,scY);
      
    }
    
	  
  }
  
  /** Obtiene los valores de las dimensiones de pantalla del PC y 
  * del visor del Kinect, ademas calcula el escalado entre ellos
  * para hacer el seguimiento del puntero del mouse
  */
  private void setDimensiones(){
  
    ImageMetaData imageMD = imageGen.getMetaData();
    imWidth = imageMD.getFullXRes();
    imHeight = imageMD.getFullYRes();
    System.out.println("Dimensiones de imagen(kinect) (" + imWidth + ", " + imHeight + ")");

    GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    scWidth = gd.getDisplayMode().getWidth();
    scHeight = gd.getDisplayMode().getHeight();
    
    // escalas
    escalaX = (float)scWidth/(float)imWidth;
    escalaY = (float)scHeight/(float)imHeight;
    
    System.out.println("Dimensiones de pantalla: (" + scWidth + ", " + scHeight + ")");
    
  }
  
  
  /** Reset flags de: z real detectada en eventos
  * mouse down o mouse up y las banderas de mouse down activo
  * y puntero de mouse activo
  */
  private void resetFlags(){
  
    flag_zreal = 0;
    flag_mousedown = false;
    flag_mouseactivo = false;
    
  }
  
  
  /** Inicializa flags globales (mouse down y click mouse)
  * segun la aplicacion se habilitan o deshabilitan
  */
  private void iniciarFlags(){
  
    resetFlags();
    
    
    switch (prog_id){
    
      // tuxpaint
      case 1:
      
	flag_global_click_activo = false;
	flag_global_mousedown_activo = true;
	break;
	
      // okular
      case 2:
      
	flag_global_click_activo = true;
	flag_global_mousedown_activo = false;
	break;
	
      // xbmc
      case 3:
      
	flag_global_click_activo = true;
	flag_global_mousedown_activo = false;
	break;
	
      // libre office (presentacion)
      case 4:
	
	flag_global_click_activo = true;
	flag_global_mousedown_activo = false;
	break;
	
      // por defecto
      default:
      
	flag_global_click_activo = true;
	flag_global_mousedown_activo = false;
	
	break;
    }
    
    
    System.out.println();
    System.out.println("Configuracion global para la aplicacion:");
    System.out.println("---------------------------------------");
    System.out.println("Click de mouse activo: " + flag_global_click_activo);
    System.out.println("Mouse down activo: " + flag_global_mousedown_activo);
    System.out.println();
    
  }
  
} 
// fin de clase

