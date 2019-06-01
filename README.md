![picture alt](https://raw.githubusercontent.com/nachocastineira/Salvo/master/src/main/resources/static/web/img/salvobn_2.png)
------------------------------------

#### Proyecto desarrollado en el Campus Java de IT Resources ___(Mayo de 2019)___ 
 El objetivo del proyecto es la creación de una versión online del famoso juego __Batalla Naval__, donde el usuario podrá enfrentarse a otros rivales en una interfaz moderna y amigable. 

En cada partida se determinará un __ganador__ y un __perdedor__, o un __empate entre ambos__ según cada caso. El jugador obtendrá 1 punto por cada partida ganada, 1/2 punto si empata y 0 puntos si pierde. Esos valores se verán reflejados en el ranking de jugadores de la página principal. 

------------------------------------

### :wrench: Tecnologias Utilizadas: ####

* Proyecto desarrollado en lenguaje de programación __Java__

* Utilización de __SpringBoot 2.1.4__

* Utilización de __Gradle 5.4.1__

* Base de Datos __MySQL__ y manejo de __Java Persistence API (JPA)__

* FrontEnd desarrollado en __HTML5__, __CSS3__ y __Javascript__

* Utilización del framework __Bootstrap v4.1.3__
------------------------------------
 ### :exclamation: Puntos importante para correr el proyecto:
  * Descargar e instalar __JAVA (v8 o posterior)__

  * Descargar e instalar __Java SE [JDK] (v8u111 o posterior)__

  * Descargar e instalar __Gradle__
  
  * Descargar e instalar __MySQL (Enterprise o Community Edition)__

  * Descargar e instalar IDE de preferencia. Se recomienda __IntelliJ IDEA__
  
 * Generar la base de datos y el user admin, corriendo en consola los siguientes comandos:

       mysql -u root -p

       create database salvo;
      
       create user 'admin'@'localhost' identified with mysql_native_password by 'admin';
      
       grant all privileges on salvo.* to 'admin'@'localhost';
 
* Correr en consola, desde la raíz del proyecto, los siguientes comandos:

      gradle wrapper

      gradlew bootRun


 * Si los comandos dan error, revisar que estén configuradas las variables de entorno de Java, Gradle y MySQL en panel de control. 


* Si los comandos fueron ingresados correctamente, podrá acceder al proyecto desde la siguiente url:

     :globe_with_meridians: <http://localhost:8080/web/games.html>
 

***
### :eyes: Interfaz de SALVO Batalla Naval: ####

![picture alt](https://oi410.photobucket.com/albums/pp182/nacho_0804/salvo_interfaz%201_zpsxb2xbt7v.png)

![picture alt](https://oi410.photobucket.com/albums/pp182/nacho_0804/salvo_interfaz%202_zps9jouktwg.png)

![picture alt](https://oi410.photobucket.com/albums/pp182/nacho_0804/salvo_interfaz%203_zpsmibqlp9i.png)

![picture alt](https://oi410.photobucket.com/albums/pp182/nacho_0804/salvo_interfaz%205_zpsre5buue0.png)


***
#### :computer:  Proyecto realizado por Ignacio Castiñeira <https://github.com/nachocastineira>
