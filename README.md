hot-uploader [![Build Status](https://travis-ci.org/juliogb/hot-uploader.svg?branch=master)](https://travis-ci.org/juliogb/hot-uploader)
==================
Upload de arquivos em chunks!!! 

Implementação de serviço JAX-RS inspirada na API do google drive para upload de arquivos. A implementação segue as definições previstas na especificação do protocolo HTTP/1.1 (RFC2616).

Caracteristicas:
* O hot-uploader permite reinicio do upload em caso de falhas sem perda dos dados já carregados. Não é necessário reiniciar o upload na mesma maquina em que o processo foi interrompido, o aplicativo reconhece o arquivo a ser carregado sem persistir dados no cliente.

* O hot-uploader utiliza como critério de unicidade do arquivo o Hash md5 do seu conteúdo. A depender da característica dos arquivos a serem carrados, em um projeto real pode ser necessário adicionar outros critérios à verificação Ex: tamanho do arquivo.
* Por utilizar File API o hot-uploader é compativel com IE 10, Firefox 28 e Chrome 38 ou versões superiores. Garantindo a compatibilidae com 96,68% dos navegadores em uso no Brasil e 94,12% no mundo (http://caniuse.com/#search=file%20api).

Passos para execução:
1. Obter o codigo do projeto (git clone https://github.com/juliogb/hot-uploader.git)
2. Compilar/executar (mvn wildfly:run)
3. Acessar (http://localhost:8080/hot-uploader)


 	
