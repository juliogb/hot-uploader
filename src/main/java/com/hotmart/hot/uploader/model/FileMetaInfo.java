/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.hotmart.hot.uploader.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.hash.Hashing;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author julio
 */
public class FileMetaInfo {
    
    private String name;
    private String type;
    private String md5;
    private String user;
    private long currentLength;
    private long length;
    private long elapsedTime;
    private StatusUpload statusUpload;
    private List<ChunkMetaInfo> chunks =  new ArrayList<>();
    
    public FileMetaInfo(ChunkMetaInfo chunk) {
        this.name = chunk.getFileName();
        this.type = chunk.getContentType();
        this.md5 = chunk.getFileMD5();
        this.user = chunk.getUser();
        this.length = chunk.getTotaFileLength();
        this.statusUpload = StatusUpload.EM_ANDAMENTO;
    }
    
    public void addChunk(ChunkMetaInfo chunk) {
        chunks.add(chunk);
        this.currentLength += chunk.getContentLength();
        
        if(this.currentLength == this.length){
            if (isCorrupted()) {
                this.statusUpload = StatusUpload.ERRO;
            }else{
                this.statusUpload = StatusUpload.CONCLUIDO;
            }
        }
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
    }
    
    public String getMd5() {
        return md5;
    }
    
    public void setMd5(String md5) {
        this.md5 = md5;
    }
    
    public String getUser() {
        return user;
    }
    
    public void setUser(String user) {
        this.user = user;
    }
    
    public long getCurrentLength() {
        return currentLength;
    }
    
    public void setCurrentLength(long currentLength) {
        this.currentLength = currentLength;
    }
    
    public long getLength() {
        return length;
    }
    
    public void setLength(long length) {
        this.length = length;
    }
    
    public long getElapsedTime() {
        return elapsedTime;
    }
    
    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }
    
    public StatusUpload getStatusUpload() {
        return statusUpload;
    }
    
    public void setStatusUpload(StatusUpload statusUpload) {
        this.statusUpload = statusUpload;
    }
    
    public boolean isComplete(){
        return StatusUpload.CONCLUIDO.equals(this.statusUpload);
    }
    
    public List<ChunkMetaInfo> getChunks() {
        return chunks;
    }
    
    public void setChunks(List<ChunkMetaInfo> chunks) {
        this.chunks = chunks;
    }
    
    public void addElapsedTime(long time) {
        this.elapsedTime += time;
    }
    
    public boolean isCorrupted() {
        return false; //TODO implementar verificacao md5
    }
    
    @JsonIgnore
    public StreamingOutput getFileStream() throws IOException {
        StreamingOutput stream = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                for (ChunkMetaInfo chunk : chunks) {
                    FileUtils.copyFile(chunk.getFile(), output);
                }
            }
        };
        return stream;
    }
    
}
