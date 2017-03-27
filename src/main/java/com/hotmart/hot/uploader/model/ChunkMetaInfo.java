/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.hotmart.hot.uploader.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiParam;
import java.io.File;
import java.util.regex.Matcher;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.QueryParam;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author julio
 */
public class ChunkMetaInfo {
    
    private static final String CONTENT_RANGE_REGEX = "bytes (\\d+)-(\\d+)/(\\d+)";
    private static final java.util.regex.Pattern CONTENT_RANGE_REGEX_PATTERN = java.util.regex.Pattern.compile(CONTENT_RANGE_REGEX);
    private static final String BASE_PATH = FileUtils.getTempDirectoryPath()+File.separator+"hot-uploader"+File.separator;

    @ApiParam(value = "Tamanho em bytes do body da requisição")
    @HeaderParam("Content-Length")
    @Max(value = 1048576) //1MB - validação adicional a realizada pelo container
    private Long contentLength;
    
    @ApiParam(value = "Parâmetro definido na especificação do protocolo HTTP para conter informações da faixa de bytes em envios parciais de dados. (inicio da faixa - final da faixa/tamanho total)", example = "bytes 0-60/200")
    @HeaderParam("Content-Range")
    @Pattern(regexp = CONTENT_RANGE_REGEX)
    @NotNull
    private String contentRange;
    
    @ApiParam(value = "Nome do arquivo submetido")
    @JsonIgnore
    @HeaderParam("File-Name")
    private String fileName;
        
    @ApiParam(value = "Content type do arquivo", example = "image/png")
    @JsonIgnore
    @HeaderParam("Content-Type")
    private String contentType;
    
    @ApiParam(value = "Hash MD5 do body da requisição. Utilizado para verificação de integridade do todo da requisição e não apenas pacote a pacote (padrão do protocolo HTTP). Utilização detalhada da RFC1864")
    @HeaderParam("Content-MD5")
    private String chunkMD5;
    
    @ApiParam(value = "Hash MD5 do conteúdo total do aquivo submetido. Utilizado como identificador único do aquivo e para verificação de integridade.")
    @JsonIgnore
    @HeaderParam("X-Content-MD5")
    @NotNull
    @Size(min = 32, max = 32)
    private String fileMD5;
    
    @ApiParam(value = "Identificador do usuário responsável pelo upload.")
    @JsonIgnore
    @QueryParam("user")
    @NotNull
    private String user;

    public Long getContentLength() {
        return contentLength == null ? getEndByte()-getStartByte() : contentLength;
    }

    public void setContentLength(Long contentLength) {
        this.contentLength = contentLength;
    }

    public String getContentRange() {
        return contentRange;
    }

    public void setContentRange(String contentRange) {
        this.contentRange = contentRange;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getChunkMD5() {
        return chunkMD5;
    }

    public void setChunkMD5(String chunkMD5) {
        this.chunkMD5 = chunkMD5;
    }

    public String getFileMD5() {
        return fileMD5;
    }

    public void setFileMD5(String fileMD5) {
        this.fileMD5 = fileMD5;
    }

   
    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public Long getStartByte() {
        Matcher matcher = CONTENT_RANGE_REGEX_PATTERN.matcher(contentRange);
        return matcher.find() ? Long.parseLong(matcher.group(1)) : null;
    }

    public Long getEndByte() {
        Matcher matcher = CONTENT_RANGE_REGEX_PATTERN.matcher(contentRange);
        return matcher.find() ? Long.parseLong(matcher.group(2)) : null;
    }

    @JsonIgnore
    public Long getTotaFileLength() {
        Matcher matcher = CONTENT_RANGE_REGEX_PATTERN.matcher(contentRange);
        return matcher.find() ? Long.parseLong(matcher.group(3)) : null;
    }
    
    @JsonIgnore
    public File getFile() {
        String pwd = BASE_PATH+getUser()+File.separator+getFileMD5()+File.separator;
        String partFileName = getStartByte()+"-"+getEndByte()+".part";
        return new File(pwd+partFileName);
    }

}
