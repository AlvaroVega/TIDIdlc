@echo off
rem
rem MORFEO Project
rem http://www.morfeo-project.org
rem
rem Component: TIDIdlc
rem Programming Language: Java
rem
rem File: $Source: /cvsroot/morfeo/TIDIdlc/bin/idl2cpp.bat,v $
rem Version: $Revision: 1.1.2.7 $
rem Date: $Date: 2005/03/30 14:30:40 $
rem Last modified by: $Author: pra $
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

if .%JAVA_HOME%.==.. (goto JDK_NOT_DEF) else (goto JDK_OK)

:JDK_NOT_DEF
echo Environment variable JAVA_HOME must be set
echo
exit /b 1

:JDK_OK

if .%TIDIDLC_HOME%.==.. (goto TIDIDLC_NOT_DEF) else (goto TIDIDLC_OK)

:TIDIDLC_NOT_DEF
echo Environment variable TIDIDLC_HOME must be set
echo
exit /b 1

:TIDIDLC_OK

%JAVA_HOME%\bin\java -jar %TIDIDLC_HOME%\lib\idl2cpp.jar %*
