/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
 */
package com.hotmart.hot.uploader.rest;

import com.hotmart.hot.uploader.model.StatusUpload;
import com.hotmart.hot.uploader.model.FileMetaInfoRepository;
import com.hotmart.hot.uploader.model.FileMetaInfo;
import com.hotmart.hot.uploader.model.ChunkMetaInfo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.ws.rs.BeanParam;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Api
@Path("file")
@Produces(MediaType.APPLICATION_JSON)
public class UploadREST {
    
    @Inject
    private FileMetaInfoRepository fileMetaInfoRepository;
 
    @ApiOperation(value = "Upload de arquivos em chunks com suporte a reinicio", notes = "Serviço implementado com inspiração na API de upload do google drive e obedecendo as definições da RFC2616 e suas atualizações")
    @ApiResponses({@ApiResponse(code = 204, message = "Chunk salvo com sucesso")
            ,@ApiResponse(code = 200, message = "Upload do arquivo concluido")
    ,@ApiResponse(code = 500, message = "Chunk não salvo")})
    @Path("upload")
    @PUT
    @Consumes(MediaType.WILDCARD)
    public Response upload(@Valid @BeanParam ChunkMetaInfo chunkMetaInfo, InputStream chunkStream){
        try {
            fileMetaInfoRepository.saveChunk(chunkMetaInfo, chunkStream); //o inputStream permite que o tempo de upload seja computado
            FileMetaInfo fileMetaInfo = fileMetaInfoRepository.getFileMetaInfo(chunkMetaInfo.getFileMD5());
            if(fileMetaInfo.isComplete()){
                return Response.ok().build();
            }
            
            return Response.noContent().build();
        } catch (IOException ex) {
            FileMetaInfo fileMetaInfo = fileMetaInfoRepository.getFileMetaInfo(chunkMetaInfo.getFileMD5());
            if(fileMetaInfo != null){
                fileMetaInfo.setStatusUpload(StatusUpload.ERRO);
            }
            Logger.getLogger(UploadREST.class.getName()).log(Level.SEVERE, null, ex);
            return Response.serverError().build();
        }
    }
    
    @ApiOperation(value = "Serviço de download dos arquivos")
    @ApiResponses({@ApiResponse(code = 200, message = "Conteúdo do arquivo solicitado")
    ,@ApiResponse(code = 204, message = "Arquivo não encontrado")})
    @Path("/download/{md5}")
    @GET
    public Response download(
            @ApiParam(name = "md5", value = "Hash MD5 que identificar o arquivo a ser recuperado.") 
            @PathParam("md5") @NotNull String md5) throws IOException{
        FileMetaInfo fileMetaInfo = fileMetaInfoRepository.getFileMetaInfo(md5);
        if(fileMetaInfo == null){
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        return Response.ok(fileMetaInfo.getFileStream(), fileMetaInfo.getType())
                .header("Content-Disposition", "attachment; filename="+fileMetaInfo.getName()).build();
    }
    
    @ApiOperation(value = "Serviço para recuperar a informações de um arquivo especifico."
    ,response = FileMetaInfo.class)
    @Path("/find/{md5}")
    @GET
    public Response findOne(@ApiParam(name = "md5", value = "Hash MD5 que identificar o arquivo a ser recuperado.")
    @PathParam("md5") @NotNull String md5) {
        FileMetaInfo fileMetaInfo = fileMetaInfoRepository.getFileMetaInfo(md5);
        return fileMetaInfo == null ? Response.noContent().build() : Response.ok(fileMetaInfo).build();
    }

    @ApiOperation(value = "Serviço para recuperar informações de todos arquivos carregados.",
            response = FileMetaInfo.class
    ,responseContainer = "List")
    @Path("/find")
    @GET
    public Response findAll() {
        Collection<FileMetaInfo> fileMetaInfo = fileMetaInfoRepository.findFileMetaInfo();
        return Response.ok(fileMetaInfo).build();
    }
}