#
# MORFEO Project
# http://www.morfeo-project.org
#
# Component: TIDIdlc
# Programming Language: Java
#
# File: $Source$
# Version: $Revision: 2 $
# Date: $Date: 2005-04-15 14:20:45 +0200 (Fri, 15 Apr 2005) $
# Last modified by: $Author: rafa $
#
# (C) Copyright 2004 Telef�nica Investigaci�n y Desarrollo
#     S.A.Unipersonal (Telef�nica I+D)
#
# Info about members and contributors of the MORFEO project
# is available at:
#
#   http://www.morfeo-project.org/TIDIdlc/CREDITS
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program; if not, write to the Free Software
# Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
#
# If you want to use this software an plan to distribute a
# proprietary application in any way, and you are not licensing and
# distributing your source code under GPL, you probably need to
# purchase a commercial license of the product.  More info about
# licensing options is available at:
#
#   http://www.morfeo-project.org/TIDIdlc/Licensing
#


COMPILADOR TIDIdlc
==================

1. INTRODUCCIÓN
---------------

Con este README.txt se trata de proporcionar la ayuda necesaria para empezar a trabajar con el 
compilador TIDIdlc. 

2. COMPILACIÓN
--------------

Los scripts de ejecución del compilador se encuentran en el directorio "dist" que cuelga de la raíz del proyecto,
y que se crea una vez se ha compilado el mismo. En dicho directorio se encuentran dos scripts, uno para lanzar el 
compilador de C++ (idl2cpp.*) y el que lanza el compilador de Java (idl2java.*), en sus versiones UNIX y Windows.

La forma de lanzar el compilador es la siguiente:
	./idls2cpp.sh [OPCIONES] -output [Directorio de salida] [Fichero IDL de entrada]
	

Para más información: ./idl2cpp.sh -h

Junto con el proyecto se proporciona una carpeta "samples" en el que se encuentran
tanto un fichero .idl de ejemplo, como las salidas que genera el compilador para C++ y Java.

