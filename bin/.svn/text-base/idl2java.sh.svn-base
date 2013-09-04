#!/bin/sh
#
# MORFEO Project
# http://www.morfeo-project.org
#
# Component: TIDIdlc
# Programming Language: Java
#
# File: $Source: /cvsroot/morfeo/TIDIdlc/bin/Attic/idl2cpp,v $
# Version: $Revision: 1.1.2.4 $
# Date: $Date: 2005/02/25 11:35:25 $
# Last modified by: $Author: caceres $
#
# (C) Copyright 2004 Telefónica Investigación y Desarrollo
#     S.A.Unipersonal (Telefónica I+D)
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

set +u

if [ -z "$JAVA_HOME" ]; then
	echo "Environment variable JAVA_HOME must be set"
	exit 1
fi

if [ -z "$TIDIDLC_HOME" ]; then
	echo "Environment variable TIDIDLC_HOME must be set"
	exit 1	
fi

$JAVA_HOME/bin/java -jar $TIDIDLC_HOME/lib/idl2java.jar $*
