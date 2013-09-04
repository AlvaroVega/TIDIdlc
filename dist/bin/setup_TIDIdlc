DEFAULT_HOME=/home/avega/morfeo/TIDIdlc/dist

set +u
SRV_DIR=${TIDIDLC_HOME:-$DEFAULT_HOME}
SRV_BIN=$SRV_DIR/bin
SRV_LIB=$SRV_DIR/lib

if [ ! -d "$SRV_DIR" ]; then
	echo "Directorio de instalacion $SRV_DIR incorrecto"
	echo
elif [ -z "$JDK_HOME" ]; then
	echo "Debe configurar la variable de entorno JDK_HOME"
	echo
else
	# Variable de entorno del servicio
	export TIDIDLC_HOME=$SRV_DIR

	# Configura el PATH para los procesos del servicio
	if [ -z "$(echo $PATH | egrep "$SRV_BIN")" ]; then
		PATH=$SRV_BIN${PATH:+:$PATH}
	fi

# 	# Configura el CLASSPATH con las librerias del servicio
# 	for LIB in $SRV_LIB/tidorbj.jar; do
# 		if [ -z "$(echo $CLASSPATH | egrep "$LIB")" ]; then
# 			CLASSPATH=$LIB${CLASSPATH:+:$CLASSPATH}
# 		fi
# 	done

	export PATH #CLASSPATH
fi
