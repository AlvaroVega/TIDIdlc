@echo off
rem
rem MORFEO Project
rem http://www.morfeo-project.org
rem
rem Component: TIDIdlc
rem Programming Language: Java
rem
rem File: $Source: /cvsroot/morfeo/TIDIdlc/build/build.bat,v $
rem Version: $Revision: 1.2 $
rem Date: $Date: 2004/11/18 14:17:52 $
rem Last modified by: $Author: franc $
rem
rem (C) Copyright 2004 Telefónica Investigación y Desarrollo
rem     S.A.Unipersonal (Telefónica I+D)
rem
rem Info about members and contributors of the MORFEO project
rem is available at:
rem
rem   http://www.morfeo-project.org/TIDIdlc/CREDITS
rem
rem This program is free software; you can redistribute it and/or modify
rem it under the terms of the GNU General Public License as published by
rem the Free Software Foundation; either version 2 of the License, or
rem (at your option) any later version.
rem
rem This program is distributed in the hope that it will be useful,
rem but WITHOUT ANY WARRANTY; without even the implied warranty of
rem MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
rem GNU General Public License for more details.
rem
rem You should have received a copy of the GNU General Public License
rem along with this program; if not, write to the Free Software
rem Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
rem
rem If you want to use this software an plan to distribute a
rem proprietary application in any way, and you are not licensing and
rem distributing your source code under GPL, you probably need to
rem purchase a commercial license of the product.  More info about
rem licensing options is available at:
rem
rem   http://www.morfeo-project.org/TIDIdlc/Licensing
rem

rem Variables que deben revisarse segun el entorno de compilacion
rem Variables del entorno de integracion


%ANT_HOME%\bin\ant -f ..\build.xml -Dmorfeo.dependencies.home=$MORFEO_DEPENDENCIES -verbose %1 %2 %3 %4 %5 %6 %7 %8 %9 %10

