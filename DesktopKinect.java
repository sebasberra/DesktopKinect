

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.*;


/** Procesamiento de imagenes 2014 
*
* TP final - Escritorio con Kinect - Clase principal
*
* Basado en ViewerPanel.java v3 de Andrew Davison, August 2011, ad@fivedots.psu.ac.th
*
* @author Agostini, Juan - Berra, Sebastian - Nepotti, Joaquin
*/
public class DesktopKinect extends JFrame 
{


  private JPanelDesktopKinect jpanelKinect;

  
  
  private static String aplicacion = null;
  private String aplicacion_param = null;
  private String archivo_oni = null;
  
  private int cant_param = 0;
  private int prog_id = 0;
  
  private static String pid = null;
  private static String id_ventana = null;

  


  /** Inicializa las variables segun los parametros de entrada
  *
  * @param args Arreglo de los parametros pasados en la llamada al programa
  */
  private void configParam(String[] args){
  
    // cantidad de parametros
    cant_param = args.length;
    
    // check al menos un parametro
    if (cant_param==0){
    
      System.out.println("Debe al menos seleccionar un programa. ");
      System.out.println("Aplicaciones posibles: tuxpaint, okular, xbmc, libreoffice o algun otro" );
      System.exit(0);
      
    }
    
    //
    // aplicacion
    aplicacion = args[0];
    aplicacion_param = "";
    System.out.println("Aplicacion: " + aplicacion );
    
    
    //
    // parametros de aplicacion y parametro archivo .oni
    
    switch (cant_param){
    
      // 2 parametros
      case 2:
    
      
	//
	// check si el segundo parametro es el archivo .oni
	//
	String[] str_aux = args[1].split("\\.");
	
	if (str_aux.length==2){
	
	  // check .oni
	  if ( str_aux[1].equals("oni") ){
	  
	    // segundo parametro archivo .oni
	    archivo_oni = args[1];
	    
	  }
	  
	}
	
	break;
	
	
      // 3 parametros
      case 3:
      
	// parametro de aplicacion
	aplicacion_param = args[1];
	System.out.println("Parametros de aplicacion: " + aplicacion_param);
	
	// archivo .oni
	archivo_oni = args[2];
	System.out.println("Archivo .oni: " + archivo_oni);
      
	break;
	
    }
    
    
          
    // programas reconocidos
    if ( aplicacion.equals("tuxpaint") ){ prog_id=1; }
    if ( aplicacion.equals("okular") ){ prog_id=2; }
    if ( aplicacion.equals("xbmc") ){ prog_id=3; }
    if ( aplicacion.equals("libreoffice") ){ prog_id=4; }
      
    // check programa reconocido
    if (prog_id==0){
      
      System.out.println("Programa: " + aplicacion + " no reconocido");
      System.exit(0);
    }
    
    
  }
  
  
  
  /** Inicializa un programa de aplicacion para ser controlado con Kinect
  *	El nombre del programa es obligatorio, los programas soportados
  *	son: tuxpaint, okular y xbmc
  *	Si se pasa el parametro opcional "parametro_programa" se adjunta
  *	tal parametro a la llamada del programa
  *	Si se pasa el parametro opcional "archivo.oni", entonces carga
  *	genera la visualizacion usando dicha demo
  *
  * @param args nombre_programa opcional_parametro_programa opcional_archivo_oni
  */
  public DesktopKinect(String[] args){
  
    super("Escritorio con Kinect");

    configParam(args);
    
      
    // instancia la clase que capturara los eventos segun el programa llamado
    switch (prog_id) {
    
	
      // tuxpaint
      case 1: 
      
	System.out.println("Programa: " + aplicacion + " reconocido");
	break;
	
      // okular
      case 2:
      
	System.out.println("Programa: " + aplicacion + " reconocido");
	break;
      
      // xbmc
      case 3:
      
	System.out.println("Programa: " + aplicacion + " reconocido");
	break;
	
      // libreoffice
      case 4:
      
	System.out.println("Programa: " + aplicacion + " reconocido");
	break;
	
      default:
      
	System.out.println("Programa: " + aplicacion + " no reconocido, se usaran valores por defecto");
	break;
	
    }
    
    //
    // ejecuta el programa pasado como parametro
    //
    
    try {
     
      // ejecuta la aplicacion
      Runtime.getRuntime().exec(aplicacion + " " + aplicacion_param);
	
      // id ventana principal
      id_ventana = XDoTool.search(aplicacion);
      
      System.out.println("ventana id: " + id_ventana );
      
      
      // id proceso ventana
      pid = XDoTool.getWindowPID(id_ventana);
      
      System.out.println("proceso id: " + pid );
      
      
    }
    catch (IOException ioe) {
    
      System.out.println("Error al abrir aplicacion");
      System.exit(1);
      
    }
    
    
    Container c = getContentPane();
    c.setLayout( new BorderLayout() );   

    //
    // JPanelDesktopKinect
    //
    
    jpanelKinect = new JPanelDesktopKinect(id_ventana,archivo_oni,prog_id); 
                       
    c.add( jpanelKinect, BorderLayout.CENTER);

    addWindowListener( new WindowAdapter() {
      public void windowClosing(WindowEvent e)
      { jpanelKinect.closeDown();  }  
    });

    pack();  
    setResizable(false);
    setLocationRelativeTo(null);
    setVisible(true);
    
            
  }
  
  
  /** Cierra la aplicacion llamada junto con el DesktopKinect
  * usando un comando kill -9 con el pid
  *
  * @param salida Entero que se pasa al metodo System.exit(...)
  */
  public static void cerrarAplicacion(int salida){
  
    System.out.println("Se cerrara la aplicacion: " + aplicacion);
      
    // kill -9 pid
    XDoTool.kill9(pid);
    
    // exit
    System.exit(salida);
      
  }
  
  // -------------------------------------------------------

  public static void main( String args[] )
  {  new DesktopKinect(args);  }

} 
