# AVRConfig

AVRConfig is a simple multiplatform GUI for avrdude written in JavaFX. It was created to make working with AVR microcontrollers easier (primarily for those who don't like working in the command line). It can also be used by those who previously did their projects with Arduino or another simple programming kit and now want to try programming in C.

## Features
- Upload to/read from flash memory or EEPROM
- Write/read standard fuse bits
- Write/read lock bits
- Check the connection to the microcontroller
- Erase the contents of the microcontroller

## Building avrconfig
AVRConfig requires JDK 11 and Maven. If both is set up right, this should suffice to build it:

	mvn package
