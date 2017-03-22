'use strict';
        var app = angular.module('hot-uploader', []);

        angular.module('hot-uploader')
            .controller('MainCtrl', function($scope, $q, $http) {
                $scope.fileUploadSession;
                $scope.uploadedFiles;
                $scope.user = 'Anonimo';
                $scope.chunkSize = 1048576; //1MB
                $scope.Math = window.Math;

                $scope.findFiles = function() {
                    $http({
                        method: 'GET',
                        url: 'v1/file/find'
                    }).then(function successCallback(response) {
                        $scope.uploadedFiles = response.data;
                    }, function errorCallback(response) {
                        $scope.error = "Erro ao recuperar lista de arquivos";
                    });
                }

                $scope.fileChange = function(event) {
                    var file = event.currentTarget.files[0];
                    if (file) {
                        $scope.fileUploadSession = new FileUploadSession(file, $scope.chunkSize, $scope.user);


                        var inicio = new Date();
                        $scope.fileUploadSession.calcMD5Hash().then(function(md5) {
                                console.log("Tempo de procesammento md5: " + (Date.now() - inicio.getTime()));

                                //verifica a existencia do aquivo
                                $http({
                                    method: 'GET',
                                    url: 'v1/file/find/' + md5
                                }).then(function successCallback(response) {
                                    if (response.status == 200) {
                                        var fileMeta = response.data;
                                        for (var i = 0; i < fileMeta.chunks.length; i++) {
                                            $scope.fileUploadSession.finishChunk(fileMeta.chunks[i].startByte);
                                        }
                                    }

                                });

                            }, null,
                            function(progress) {
                                $scope.progressMD5 = progress;
                            });
                    } else {
                        $scope.fileUploadSession = null;
                    }

                    $scope.$apply();
                }

                function FileUploadSession(file, chunkSize, user) {
                    this.file = file;
                    this.chunkSize = chunkSize;
                    this.chunks = [];
                    this.user = user;
                    this.progressFile = 0;

                    //calcula chunks
                    var chunksLengh = Math.ceil(file.size / chunkSize);
                    for (var currentChunk = 0; currentChunk < chunksLengh; currentChunk++) {
                        var chunk = {};
                        chunk.index = currentChunk;
                        chunk.start = currentChunk * chunkSize;
                        chunk.end = ((chunk.start + chunkSize) >= file.size) ? file.size : chunk.start + chunkSize;
                        chunk.blob = file.slice(chunk.start, chunk.end);
                        chunk.status = 'PENDENTE';
                        chunk.progressChunk = 0;
                        chunk.getChunkRange = function() {
                            return this.start + '-' + this.end
                        };
                        chunk.getChunkSize = function() {
                            return this.end - this.start;
                        };
                        this.chunks.push(chunk);
                    }

                    this.calcMD5Hash = function() {
                        var deferred = $q.defer();

                        var spark = new SparkMD5.ArrayBuffer(),
                            fileReader = new FileReader(),
                            chunks = this.chunks,
                            fileSession = this;

                        currentChunk = 0;
                        fileReader.readAsArrayBuffer(chunks[currentChunk].blob);

                        fileReader.onloadend = function(e) {
                            spark.append(e.target.result); // Append array buffer
                            deferred.notify(Math.trunc(++currentChunk / chunks.length * 100)); //percentual de progresso

                            if (currentChunk < chunks.length) {
                                fileReader.readAsArrayBuffer(chunks[currentChunk].blob); //le proximo chunk
                            } else {
                                console.log('Termino do procesammento md5');
                                fileSession.md5 = spark.end();
                                deferred.resolve(fileSession.md5);
                            }
                        };

                        return deferred.promise;
                    }

                    this.uploadChunk = function(index) {
                        var deferred = $q.defer();

                        var xhr = new XMLHttpRequest();
                        xhr.upload.onprogress = function(e) {
                            if (e.lengthComputable) {
                                deferred.notify(e.loaded); //bytes enviados
                            }
                        };
                        xhr.onloadend = function(e) {
                            deferred.resolve(e)
                        };
                        xhr.onerror = function(e) {
                            chunk.status = 'ERRO';
                            deferred.reject(e);
                        };
                        xhr.onreadystatechange = function(e) {
                            if (xhr.readyState === 4) {
                                if (xhr.status == 200 || xhr.status == 201 || xhr.status == 204) {
                                    chunk.status = 'CONCLUIDO';
                                } else {
                                    chunk.status = 'ERRO';
                                    deferred.reject(e);
                                }
                            }
                        }

                        var chunk = this.chunks[index];
                        chunk.status = 'EM_ANDAMENTO';
                        xhr.open('PUT', 'v1/file/upload?user=' + this.user, true);
                        xhr.setRequestHeader("content-range", "bytes " + chunk.getChunkRange() + "/" + this.file.size);
                        xhr.setRequestHeader("X-Content-MD5", this.md5);
                        xhr.setRequestHeader("Content-Type", this.file.type == null ? "application/octet-stream" : this.file.type);
                        xhr.setRequestHeader("File-Name", this.file.name);
                        xhr.send(chunk.blob);
                        return deferred.promise;
                    }

                    this.uploadFile = function() {
                        var fileSession = this;
                        var progressFileLast = fileSession.progressFile;
                        for (var i = 0; i < fileSession.chunks.length; i++) {
                            if (fileSession.chunks[i].status == 'PENDENTE') {
                                fileSession.status = 'EM_ANDAMENTO';
                                fileSession.currentChunkSending = fileSession.chunks[i];
                                fileSession.uploadChunk(i).then(function(e) {
                                        fileSession.uploadFile();
                                    },
                                    function(erro) {
                                        fileSession.status = 'ERRO';
                                        return;
                                    },
                                    function(bytesSent) {
                                        fileSession.currentChunkSending.progressChunk = bytesSent;
                                        fileSession.progressFile = progressFileLast + bytesSent;
                                    });
                                break;
                            }
                        }
                        if (fileSession.progressFile == fileSession.file.size) {
                            fileSession.status = 'CONCLUIDO';
                        }
                    }

                    this.finishChunk = function(startByte, endByte) { //finaliza chunks já recebidos
                        for (var i = 0; i < this.chunks.length; i++) { //como o tamanho maximo o chunk é fixo apenas o byte inicial é suficiente
                            if (this.chunks[i].start == startByte) {
                                this.chunks[i].status = 'CONCLUIDO';
                                this.progressFile += this.chunks[i].getChunkSize();
                                break;
                            }
                        }
                    }
                }

                $scope.uploadFile = function() {
                    $scope.fileUploadSession.uploadFile();
                }

            });

        app.directive('customOnChange', function() {
            return {
                restrict: 'A',
                link: function(scope, element, attrs) {
                    var onChangeHandler = scope.$eval(attrs.customOnChange);
                    element.bind('change', onChangeHandler);
                }
            };
        });