# CazApp
Aplicación Android para la búsqueda de canciones relacionadas con la liturgia católica, en específico las contenidas en el libreto Caz.  
## Características
Algunas de las funcionalidades que provee esta aplicación son:  

- Búsqueda de canciones por título o cuerpo.  <p><img src="/imagenes%20readme/busqueda.png" width="300" /></p>
- Cambios de tono.  <p><img src="/imagenes%20readme/tono_tamaño.png" width="300" /> <img src="/imagenes%20readme/tono%202.png" width="300"/></p>
- Cambios de tamaño de letra.  <p><img src="/imagenes%20readme/tono_tamaño.png" width="300" /> <img src="/imagenes%20readme/tamaño%202.png" width="300"/></p>
- Añadir canción a lista de favoritos.  <p><img src="/imagenes%20readme/Favoritos.png" width="300" /></p>
- Creación de listas personalizadas de canciones.  <p><img src="/imagenes%20readme/lista.png" width="300" /> <img src="/imagenes%20readme/lista 2.png" width="300" /> <img src="/imagenes%20readme/lista 3.png" width="300" /></p>
- Publicación de listas personalizadas.  <p><img src="/imagenes%20readme/publicar.png" width="300" /></p>

## Funcionamiento
Las canciones están almacenadas en una base de datos remota de **MongoDB**. La aplicación realiza peticiones contra esta base de datos y muestra los resultados. Aquellas canciones que se marquen como **favoritas se almacenarán localmente**, pudiendo consultarse sin necesidad de conectarse a internet. Las **publicaciones** se harán en la misma base de datos de MongoDB y están pensadas para ser **temporales** por lo que solamente estarán disponibles durante un tiempo limitado, después se despublicarán.

Compatible con **API 21 en adelante**.
