This folder is used to let gradle download the dependencies. 
Because there is no atomic gradle task to download the dependencies it needs a fake project to do so.
Therefore in this project is a single java dummy file so it can start the build process and actually download the dependencies.
