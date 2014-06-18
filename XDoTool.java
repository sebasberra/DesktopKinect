//package desktopkinect;

import java.io.*;
import java.awt.Robot;
import java.awt.event.InputEvent;

/** Procesamiento de imagenes 2014 
*
* TP final - Escritorio con Kinect - wrapper xdotool
*
* @author Agostini, Juan - Berra, Sebastian - Nepotti, Joaquin
*/
public class XDoTool{


  public XDoTool(){
  
    System.out.println("XDoTool constructor");
    
  }
  
  /** [xdotool click] Envia click del mouse
  *
  * @param id_ventana	id de ventana
  * @param boton 	1=izquierdo 2=medio 3=derecho 4=rueda arriba 5=rueda abajo
  * @return		true=OK false=error
  */
  public static boolean clickXDoTool(String id_ventana, String boton){
  
    
    boolean salida = true;
    
    String aux = ejectutarXDoTool("click --window " + id_ventana + " " + boton);
    
    // check estado error
    if (aux.equals("error")) { salida=false; }
    
    // retorno
    return salida;
    
  }
  
  
  
  /** Envia rueda del mouse
  *
  * @param direccion 	1=abajo 2=arriba
  * @param wheelAmt	Cantidad de notch del movimiento
  * @return		true=OK false=error
  */
  public static boolean mouseWheel(int direccion, int wheelAmt){
  
    Robot robot;
    
    try{
    
      robot = new Robot();
      
      switch(direccion){
    
	case 1:
	
	  robot.mouseWheel(wheelAmt);
	  robot.delay(200);
	  break;
	  
	case 2:
	
	  robot.mouseWheel(-1*wheelAmt);
	  robot.delay(200);
	  break;
		
      }
    
    }catch(Exception e){
    
      System.out.println( e.toString() ); 
      
      return false;
      
    }
    
    
    return true;
    
  }
  
  
  /** [Java Robot] Envia solo la pulsacion abajo del boton del mouse, 
  *	sirve para obtener el efecto de mantener pulsado dicho boton
  *
  * @param boton 	1=izquierdo 2=medio 3=derecho 
  * @return		true=OK false=error
  */
  public static boolean mouseDownJava(int boton){
  
    
    Robot robot;
    
    try{
    
      robot = new Robot();
      
      switch(boton){
    
	case 1:
	
	  robot.mousePress(InputEvent.BUTTON1_MASK);
	  robot.delay(200);
	  break;
	  
	case 2:
	
	  robot.mousePress(InputEvent.BUTTON2_MASK);
	  robot.delay(200);
	  break;
	  
	case 3:
	
	  robot.mousePress(InputEvent.BUTTON3_MASK);
	  robot.delay(200);
	  break;
		
      }
    
    }catch(Exception e){
    
      System.out.println( e.toString() ); 
      
      return false;
      
    }
    
    
    return true;
    
  }
  
  /** [Java Robot] Envia solo la pulsacion arriba del boton del mouse, 
  *	sirve para obtener el efecto de mantener pulsado dicho boton
  *
  * @param boton 	1=izquierdo 2=medio 3=derecho 
  * @return		true=OK false=error
  */
  public static boolean mouseUpJava(int boton){
  
    
    Robot robot;
    
    try{
    
      robot = new Robot();
      
      switch(boton){
    
	case 1:
	
	  robot.mouseRelease(InputEvent.BUTTON1_MASK);
	  robot.delay(200);
	  break;
	  
	case 2:
	
	  robot.mouseRelease(InputEvent.BUTTON2_MASK);
	  robot.delay(200);
	  break;
	  
	case 3:
	
	  robot.mouseRelease(InputEvent.BUTTON3_MASK);
	  robot.delay(200);
	  break;
		
      }
    
    }catch(Exception e){
    
      System.out.println( e.toString() ); 
      
      return false;
      
    }
    
    
    return true;
    
  }
  
  /** [xdotool mousedown] Envia solo la pulsacion abajo del boton del mouse, 
  *	sirve para obtener el efecto de mantener pulsado dicho boton
  *
  * @param id_ventana	id de ventana
  * @param boton 	1=izquierdo 2=medio 3=derecho 4=rueda arriba 5=rueda abajo
  * @return		true=OK false=error
  */
  public static boolean mouseDown(String id_ventana, String boton){
  
    
    boolean salida = true;
    
    String aux = ejectutarXDoTool("mousedown --window " + id_ventana + " " + boton);
    
    // check estado error
    if (aux.equals("error")) { salida=false; }
    
    // retorno
    return salida;
    
  }
  
  /** [xdotool mouseup] Envia solo la pulsacion arriba del boton del mouse, 
  *	sirve para desactivar el efecto de pulsado de dicho boton
  *
  * @param id_ventana	id de ventana
  * @param boton 	1=izquierdo 2=medio 3=derecho 4=rueda arriba 5=rueda abajo
  * @return		true=OK false=error
  */
  public static boolean mouseUp(String id_ventana, String boton){
  
    
    boolean salida = true;
    
    String aux = ejectutarXDoTool("mouseup --window " + id_ventana + " " + boton);
    
    // check estado error
    if (aux.equals("error")) { salida=false; }
    
    // retorno
    return salida;
    
  }
  
  /** [xdotool getactivewindow] Captura la ventana activa actual
  *
  * @return		id_ventana activa actual
  */
  public static String getActiveWindow(){
  
    String salida;
    
    salida = ejectutarXDoTool("getactivewindow");
    
    return salida;
    
  }
  
  /** [xdotool windowactivate] Activa una ventana determinada sin importar que este en otro escritorio
  *
  * @param id_ventana	id de ventana
  * @return		true=OK false=error
  */
  public static boolean setWindowActivate(String id_ventana){
  
    
    boolean salida = true;
    
    String aux = ejectutarXDoTool("windowactivate --sync " + id_ventana );
    
    // check estado error
    if (aux.equals("error")) { salida=false; }
    
    // retorno
    return salida;
    
  }
  
  
  /** [xdotool windowminimize] Minimiza la ventana 
  *
  * @param id_ventana	id de ventana
  * @return		true=OK false=error
  */
  public static boolean setWindowMinimize(String id_ventana){
  
    
    boolean salida = true;
    
    String aux = ejectutarXDoTool("windowminimize --sync " + id_ventana );
    
    // check estado error
    if (aux.equals("error")) { salida=false; }
    
    // retorno
    return salida;
    
  }
  
  
  
  /** [xdotool key] Envia una tecla a determinada ventana
  *
  * @param id_ventana	id de la ventana
  * @param tecla	tecla o teclas separadas por espacio. ej: "alt+r", "Control_L+J", "ctrl+alt+n", "BackSpace"
  * @return		true=OK false=error
  */
  public static boolean key (String id_ventana, String tecla){
  
    boolean salida = true;
    
    String aux = ejectutarXDoTool("key --window " + id_ventana + " " + tecla);
    
    // check estado error
    if (aux.equals("error")) { salida=false; }
    
    // retorno
    return salida;
    
  }

  
  /** [xdotool search] Obtiene el id_ventana del programa
  *   IMPORTANTE: la ventana tiene que estar maximizada
  *
  * @param programa	nombre del programa a buscar
  * @return		id_ventana
  */
  public static String search(String programa){
  
    //
    // IMPORTANTE: la ventana tiene que estar maximizada
    //
    
    String salida;
    
    salida = ejectutarXDoTool("search --class --onlyvisible --sync " + programa);
    
    return salida;
    
  }
  

  /** [xdotool getwindowpid] Obtiene el pid de la ventana
  *
  * @param id_ventana	id de la ventana
  * @return		process id del sistema
  */
  public static String getWindowPID(String id_ventana){
  
    String salida;
    
    salida = ejectutarXDoTool("getwindowpid " + id_ventana);
    
    return salida;
    
  }
  
  
  /** [xdotool getwindowfocus] Obtiene la ventana que tiene el foco
  *   Es mas confiable usar getActiveWindow
  *
  * @return 	id de la ventana que tiene el foco
  */
  public static String getWindowFocus(){
  
    String salida;
    
    salida = ejectutarXDoTool("getwindowfocus");
    
    return salida;
    
  }
  
  
  /** [xdotool windowsize] Setea dimension de ventana
  *
  * @param id_ventana	id de la ventana
  * @param width	nuevo ancho de ventana
  * @param height	nuevo alto de ventana
  * @return 	true=OK false=error
  */
  public static boolean setWindowSize(String id_ventana, int width, int height){
  
    boolean salida = true;
    
    String aux = ejectutarXDoTool(
	"windowsize --sync " + 
	id_ventana + 
	" " + 
	String.valueOf(width) +
	" " +
	String.valueOf(height)
	);
    
    // check estado error
    if (aux.equals("error")) { salida=false; }
    
    // retorno
    return salida;
    
  }
    
  
  /** [kill -9] Ejecuta un comando kill -9
  *
  * @param 	pid	id de proceso del sistema operativo
  * @return 	true=OK false=error
  */
  public static boolean kill9(String pid){
  
    try {
  
      
      System.out.println("kill -9 " + pid);
      
      Runtime.getRuntime().exec("kill -9 " + pid);
      
      return true;
      
    }catch (IOException ioe) {
    
      System.out.println("Error al ejecutar: " + "kill -9 " + pid);
      
      return false;
      
    }
    
  }
  
  
  /** [xdotool mousemove] Mueve el mouse a la posicion absoluta
  *
  * @param id_ventana	id de la ventana donde se la cual se tomara como (0,0) la coordenada de origen
  * @param x_abs	X
  * @param y_abs	Y
  * @return 	true=OK false=error
  */
  public static boolean mouseMove(String id_ventana, int x_abs, int y_abs){
  
  
    // estado OK
    boolean salida=true;
    
    // comando xdotool mousemove
    //
    // desplamiento en x, y
    String mousemove = 
	"mousemove --sync " + 
	id_ventana + 
	" " + 
	String.valueOf(x_abs) + 
	" " + 
	String.valueOf(y_abs);
    
    // exec xDoTool
    String aux = ejectutarXDoTool(mousemove);
    
    // check estado error
    if (aux.equals("error")) { salida=false; }
    
    // retorno
    return salida;
    
  }
  
  
  /** Mueve el mouse a la posicion absoluta con la clase java.awt.Robot
  *
  * @param x_abs	X
  * @param y_abs	Y
  * @return 	true=OK false=error
  */
  public static boolean mouseMove(int x_abs, int y_abs){
  
    Robot robot;
    
    try{
    
      robot = new Robot();
    
    }catch(Exception e){
    
      System.out.println( e.toString() ); 
      
      return false;
      
    }
    
    robot.mouseMove(x_abs,y_abs);
    
    return true;
    
  }
  
  
  /** [xdotool mousemove] Mueve el mouse a la posicion absoluta
  *
  * @param x_abs	X
  * @param y_abs	Y
  * @return 	true=OK false=error
  */
  private static boolean mouseMoveXDoTool(int x_abs, int y_abs){
  
    // estado OK
    boolean salida=true;
    
    // comando xdotool mousemove
    //
    // desplamiento en x, y
    String mousemove = 
	"mousemove --sync " + 
	" " + 
	String.valueOf(x_abs) + 
	" " + 
	String.valueOf(y_abs);
    
    // exec xDoTool
    String aux = ejectutarXDoTool(mousemove);
    
    // check estado error
    if (aux.equals("error")) { salida=false; }
    
    // retorno
    return salida;
    
  }
  
  
  /** [xdotool mousemove_relative] Mueve el mouse a la posicion relativa
  *
  * @param id_ventana	id de la ventana donde se la cual se tomara como (0,0) la coordenada de origen
  * @param x_desp	X
  * @param y_desp	Y
  * @return 	true=OK false=error
  */
  public static boolean mouseMoveRelative(String id_ventana, int x_desp, int y_desp){
  
    // estado OK
    boolean salida=true;
    
    
    // comando xdotool mousemove
    //
    // desplamiento en x, y
    String mousemove = 
	"mousemove_relative --sync " + 
	id_ventana + 
	" " +  
	String.valueOf(x_desp) + 
	" " + 
	String.valueOf(y_desp);
    
    // exec xDoTool
    String aux = ejectutarXDoTool(mousemove);
    
    // check estado error
    if (aux.equals("error")) { salida=false; }
    
    // retorno
    return salida;
    
  }
  
  
  /** [xdotool mousemove_relative] Mueve el mouse a la posicion relativa
  *
  * @param x_desp	X
  * @param y_desp	Y
  * @return 	true=OK false=error
  */
  public static boolean mouseMoveRelative(int x_desp, int y_desp){
  
    // estado OK
    boolean salida=true;
    
    
    // comando xdotool mousemove
    //
    // desplamiento en x, y
    String mousemove = 
	"mousemove_relative --sync " + 
	" " +  
	String.valueOf(x_desp) + 
	" " + 
	String.valueOf(y_desp);
    
    // exec xDoTool
    String aux = ejectutarXDoTool(mousemove);
    
    // check estado error
    if (aux.equals("error")) { salida=false; }
    
    // retorno
    return salida;
    
  }
  
  
  /** Llama a traves del Runtime.getRuntime().exec de Java
  *   a la utilidad xdotool que debe estar instalada en
  *   en el sistema
  *
  * @param comando Comando que se concatenara a la sentencia 'xdotool'
  * @return Devuelve la salida de consola del comando ejecutado o 'nulo'
  */
  private static String ejectutarXDoTool(String comando){
  
  
    InputStream istream;
    InputStreamReader isreader;
    BufferedReader bufferreader;
    
    String xdotool = "xdotool " + comando;
    String stdout = null;
    
    try {
    
      // msg comando
      System.out.println(xdotool);

      //
      // exec comando xdotool
      //
      // proceso
      //
      Process proc = Runtime.getRuntime().exec(xdotool);

      // espera a terminar
      proc.waitFor();
      
      //
      // captura salida
      //
      // inputstream
      //
      istream = proc.getInputStream();
      
      // inputstream reader
      isreader = new InputStreamReader(istream);
      
      // buffer reader
      bufferreader = new BufferedReader(isreader);
      
      // lee buffer de salida
      stdout = bufferreader.readLine();

      // check retorno null
      if (stdout==null){
      
	stdout = "nulo";
	
      }
      
      // salida
      System.out.println("STDOUT: " + stdout);

      // cierra buffer
      bufferreader.close();
      
      // destroy
      proc.destroy();
      
    }
    catch (InterruptedException ie){
    
      System.out.println("Error en el comando waitFor del proceso: " + xdotool);
      
      stdout = "error";
    
    }
    catch (IOException ioe) {
    
      System.out.println("Error al ejecutar: " + xdotool);
      
      stdout = "error";
      
    }
    
    return stdout;
    
  }
  
  
  
  /** Se define un procedimiento main para ejecutar un test
  *   de las funcionalidades, pero la clase esta diseñada 
  *   para llamar a los metodos estaticos que la componen
  */
  public static void main(String[] args){
  
    test();
    
    
  }
  
  
  /** Test de los metodos de la clase
  *   
  */
  private static void test(){
  
    // id ventana principal
    String id_ventana = XDoTool.search("okular");
    
    System.out.println("ventana id: " + id_ventana );
    
    
    // id proceso ventana
    String pid = XDoTool.getWindowPID(id_ventana);
    
    System.out.println("proceso id: " + pid );
    
    
    // mueve mouse con coordenadas absolutas
    boolean b = XDoTool.mouseMove(id_ventana,200,200);
    
    System.out.println("mouseMove (" + id_ventana + ",200,200): " + b );
    
    
    // desplazamiento del mouse
    b = XDoTool.mouseMoveRelative(id_ventana,100,150);
    
    System.out.println("mouseMoveRelative (" + id_ventana + "100,150): " + b );
    
    // setea la ventana activa
    b = setWindowActivate(id_ventana);
    
    System.out.println("setWindowActivate id_ventana: " + id_ventana + "  estado = " + b );
    
    // envia pulsado de tecla a la ventana
    b = XDoTool.key(id_ventana,"Control_L+o");
    
    System.out.println("key = Control_L+o; ventana = : " + id_ventana + "  estado = " + b );
    
    // minimiza la ventana
    b = XDoTool.setWindowMinimize(id_ventana);
    
    System.out.println("setWindowMinimize; ventana = : " + id_ventana + "  estado = " + b );
    
    
    // kill proceso
    //b = XDoTool.kill9(pid);
    
    System.out.println("kill -9 pid = " + pid + "  estado = " + b );
    
    
  }
}