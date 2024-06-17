# FitZone - Aplicación de Gestión de Gimnasios

![fitzone_logo](https://github.com/kinp4ku/FITZONE_TFG/assets/124914959/a46a5e9b-0996-4c34-921b-9705a6bbc124)

## Tabla de Contenidos
- [Resumen](#resumen)
- [Características](#características)
- [Tecnologías Utilizadas](#tecnologías-utilizadas)
- [Instalación](#instalación)
- [Uso](#uso)
- [Contribuciones](#contribuciones)
- [Licencia](#licencia)
- [Contacto](#contacto)

## Resumen

FitZone es una aplicación integral para la gestión de gimnasios, diseñada para facilitar la interacción entre usuarios y técnicos. Utiliza Firebase para la gestión de datos y ofrece funcionalidades como la integración con códigos QR y NFC.

## Características

- **Interfaz de Usuario**: Los usuarios pueden ver el horario del gimnasio, acceder a sus rutinas asignadas y buscar ejercicios.
- **Interfaz de Técnico**: Los técnicos pueden asignar rutinas a los usuarios y gestionar diversos aspectos del gimnasio.
- **Gestión de Datos**: Utiliza Firebase para la base de datos en tiempo real, autenticación y almacenamiento.
- **Integración con NFC y Códigos QR**: Los usuarios pueden escanear códigos QR y etiquetas NFC para obtener información detallada sobre ejercicios.

## Tecnologías Utilizadas

- **Plataforma**: Android Studio
- **Base de Datos**: Firebase (Firestore Database, Authentication, Storage)
- **Lenguaje de Programación**: Java
- **Bibliotecas**: 
  - [Picasso](https://square.github.io/picasso/) y [Glide](https://github.com/bumptech/glide) para la gestión de imágenes.
  - [ZXing](https://github.com/zxing/zxing) para funcionalidades de códigos QR.
  - AndroidX para componentes modernos y compatibilidad.
  - Material Design para una interfaz de usuario moderna.
  - [JUnit](https://junit.org/junit5/) y [Espresso](https://developer.android.com/training/testing/espresso) para pruebas.

## Instalación

1. Clona el repositorio:
    ```bash
    git clone https://github.com/kinp4ku/FITZONE_TFG.git
    ```
2. Abre el proyecto en Android Studio.

3. Configura Firebase:
    - Crea un nuevo proyecto en Firebase.
    - Configura Firestore, Authentication y Storage.
    - Descarga el archivo `google-services.json` y colócalo en el directorio `app` de tu proyecto en Android Studio.

4. Instala las dependencias necesarias utilizando Gradle.

## Uso

1. Compila y ejecuta la aplicación en un dispositivo Android o en un emulador.

2. Regístrate como usuario o técnico para explorar las funcionalidades.

3. Usa la aplicación para gestionar las actividades del gimnasio, asignar rutinas y obtener información de ejercicios escaneando códigos QR o etiquetas NFC.

## Contribuciones

Las contribuciones son bienvenidas. Para contribuir, sigue estos pasos:

1. Haz un fork del proyecto.

2. Crea una nueva rama para tu funcionalidad o corrección de errores:
    ```bash
    git checkout -b nueva-funcionalidad
    ```

3. Realiza tus cambios y haz un commit:
    ```bash
    git commit -m 'Añadir nueva funcionalidad'
    ```

4. Envía tus cambios al repositorio remoto:
    ```bash
    git push origin nueva-funcionalidad
    ```

5. Abre un pull request en GitHub.

## Licencia

--

## Contacto

Para más información o preguntas sobre el proyecto, puedes contactar a los desarrolladores en:

- **Lorena Rojas Espejo**: [srtalorena29@gmail.com](mailto:srtalorena29@gmail.com)
- **Manuel Alejandro Rodríguez Vega**: [manuelalejandrorv1809@gmail.com](mailto:manuelalejandrorv1809@gmail.com)
- **Raquel López Sánchez**: [raquel.lpz.sz@gmail.com](mailto:raquel.lpz.sz@gmail.com)
