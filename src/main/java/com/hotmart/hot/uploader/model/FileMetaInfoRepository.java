/*
* To change this license header, choose License Headers in Project Properties.
* To change this template file, choose Tools | Templates
* and open the template in the editor.
*/
package com.hotmart.hot.uploader.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import org.apache.commons.io.FileUtils;

/**
 * Repositorio responsavel pelo armazenamento das meta informações do arquivo e
 * operações de consulta e salvamento
 * @author julio
 */
@Startup
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class FileMetaInfoRepository {
    
    private Map<String, FileMetaInfo> fileMetaInfoCache = new HashMap<>();
    
    public FileMetaInfo getFileMetaInfo(String md5Hash) {
        return fileMetaInfoCache.get(md5Hash);
    }
    
    public Collection<FileMetaInfo> findFileMetaInfo(){
        return fileMetaInfoCache.values();
    }
    
    public Collection<FileMetaInfo> findFileMetaInfo(String user){
        return fileMetaInfoCache.entrySet().stream().filter(map->user.equals(map.getValue().getUser()))
                .map(map->map.getValue()).collect(Collectors.toList());
        
    }
    
    public void putFileMetaInfo(FileMetaInfo fileMetaInfo){
        fileMetaInfoCache.put(fileMetaInfo.getMd5(), fileMetaInfo);
    }
    
    public void saveChunk(ChunkMetaInfo chunkMetaInfo, InputStream chunkStream) throws IOException {
        long startTime = System.nanoTime();
        
        FileMetaInfo fileMetaInfo = getFileMetaInfo(chunkMetaInfo.getFileMD5());      
        if(fileMetaInfo == null){
            fileMetaInfo = new FileMetaInfo(chunkMetaInfo);
            putFileMetaInfo(fileMetaInfo);
        }
        
        FileUtils.copyInputStreamToFile(chunkStream, chunkMetaInfo.getFile());
        
        fileMetaInfo.addChunk(chunkMetaInfo);
        long endTime = System.nanoTime();
        fileMetaInfo.addElapsedTime((endTime-startTime)/ 1000000);
    }
    
}
