 -------------------
 Desktop with Kinect
 -------------------
 
* Description:

  This is a prototype of control the desktop pc with kinect
  Use Java class Robot for move the mouse and xdotool command line
  program for send keys to the window program
  
* Future work

  Re scale mouse move depends of z position
  Use javacv finger tracking for click mouse actions
  Configuration utility for gesture and program actions
  
* Bugs

  When more than one program called with DesktopKinect is open, the utility
  xdotool not identify the windows id for key send
  
* Requirements and tested on OpenSUSE 13.1:


- OpenCV

- OpenNI Version 1.5.7 or higher

- SensorKinect Version 5.1.2.1 (Unstable version - May 15th 2012) [On OpenSUSE 13.1]

- NITE 1.5.2 or higher

- JavaCV

- xdotool
