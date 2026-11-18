# 🏢 Nothingan Pri S.A. — Sistema de Gestión Empresarial

<p align="center">
  <img src="banner.jpeg" alt="Nothingan Pri S.A. Banner" width="100%">
</p>

Aplicación web empresarial desarrollada con **Java y Spring Boot** siguiendo el patrón **MVC (Modelo-Vista-Controlador)**. Diseñada para la gestión interna, control de acceso basado en roles y administración de operaciones corporativas.

---

## 🛠️ Stack Tecnológico

![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Spring Security](https://img.shields.io/badge/Spring_Security-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)
![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white)
![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)
![Maven](https://img.shields.io/badge/Apache_Maven-C71A36?style=for-the-badge&logo=apache-maven&logoColor=white)

---

## ✨ Características Principales

- **🔐 Autenticación y Control de Roles:** Gestión segura de usuarios con permisos diferenciados (Administrador, Usuario estándar, etc.) y protección de rutas mediante **Spring Security**.
- **🎨 Sistema de Diseño Propio:** Interfaz web responsiva y modular construida con **Thymeleaf** y hojas de estilo personalizadas (`styles.css`).
- **🗄️ Persistencia de Datos:** Conexión e integración con base de datos relacional (**MySQL / MariaDB**) utilizando **Spring Data JPA / JDBC**.
- **📐 Arquitectura Limpia:** Separación clara entre controladores, servicios, repositorios y entidades para facilitar el mantenimiento y la escalabilidad.

---

## 📁 Estructura del Proyecto

```text
nothinganprisa/
├── src/
│   └── main/
│       ├── java/            # Controladores, Entidades, Repositorios y Servicios
│       └── resources/
│           ├── static/      # Archivos estáticos (CSS, JS, imágenes)
│           │   └── css/styles.css
│           ├── templates/   # Vistas dinámicas HTML con Thymeleaf
│           └── application.properties
└── pom.xml                  # Dependencias y configuración de Maven
